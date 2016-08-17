/*
 *  ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 * 
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at
 * http://sourceforge.net/projects/dcm4che.
 * 
 * The Initial Developer of the Original Code is Gunter Zeilinger, Huetteldorferstr. 24/10, 1150
 * Vienna/Austria/Europe. Portions created by the Initial Developer are Copyright (C) 2002-2005 the
 * Initial Developer. All Rights Reserved.
 * 
 * Contributor(s): Gunter Zeilinger <gunterze@gmail.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of either the GNU General
 * Public License Version 2 or later (the "GPL"), or the GNU Lesser General Public License Version
 * 2.1 or later (the "LGPL"), in which case the provisions of the GPL or the LGPL are applicable
 * instead of those above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your version of this file
 * under the terms of the MPL, indicate your decision by deleting the provisions above and replace
 * them with the notice and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under the terms of any one of
 * the MPL, the GPL or the LGPL.
 * 
 * ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.arr.listeners.mdb;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.AuditRecord.AuditFormat;
import org.dcm4chee.arr.listeners.mdb.AuditRecordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Session Bean implementation class mockReceiverMDB
 */
@Stateless
@LocalBean
public class ReceiverHelperBean implements ReceiverHelperBeanLocal {
    
    private static final Logger log = LoggerFactory.getLogger(ReceiverHelperBean.class);
    private static final int MSG_PROMPT_LEN = 200;
    
    private static final String IHEYR4_TO_ATNA_XSL = "arr-iheyr4-to-atna.xsl";
    
    private static final byte[] UNKNOWN_FORMAT_PREFIX = "<UnknownFormat><![CDATA[".getBytes(Charset.forName("UTF-8"));
    private static final byte[] UNKNOWN_FORMAT_SUFFIX = "]]></UnknownFormat>".getBytes(Charset.forName("UTF-8"));

    private static Templates iheYr4toATNATpl;
    
    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    
    @EJB
    private AuditRecordCodeServiceEJB codeService;
    
    private XMLReader xmlReader;
    
    private SAXTransformerFactory tf;
    
    private byte[] cache=null;
    
    
    public ReceiverHelperBean() {
        // empty
    }

    /**
     * Process checks if the schema used in the xml message is IHE or DICOM and instantiates a xml reader accordingly
     * Finally persists the content of the message
     *
     * @param xmldata the xmldata
     * @param receiveDate the receive date
     */
    public void process(byte[] xmldata, Date receiveDate) {
        setCache(xmldata);
        try {
            if (log.isDebugEnabled()) {
                 log.debug("Start processing - " + prompt(xmldata));
            }            
            boolean iheyr4 = isIHEYr4(xmldata);
            XMLReader reader = iheyr4 ? xmlFilter() : xmlReader();
            AuditRecord rec = new AuditRecord();
            if (iheyr4)
            	rec.setAuditFormat(AuditRecord.AuditFormat.IHEYR4);

            DefaultHandler dh = new AuditRecordHandler(codeService, rec);
            reader.setContentHandler(dh);
            reader.setEntityResolver(dh);
            reader.setErrorHandler(dh);
            reader.setDTDHandler(dh);
            try {
                reader.parse(new InputSource(new ByteArrayInputStream(xmldata)));
            } catch (Exception x) {
                log.warn("Parsing XML Audit message failed!", x);
                rec.setAuditFormat(AuditFormat.UNKNOWN);
                byte[] msg = new byte[xmldata.length+UNKNOWN_FORMAT_PREFIX.length+UNKNOWN_FORMAT_SUFFIX.length];
                System.arraycopy(UNKNOWN_FORMAT_PREFIX, 0, msg, 0, UNKNOWN_FORMAT_PREFIX.length);
                System.arraycopy(xmldata, 0, msg, UNKNOWN_FORMAT_PREFIX.length, xmldata.length);
                System.arraycopy(UNKNOWN_FORMAT_SUFFIX, 0, msg, UNKNOWN_FORMAT_PREFIX.length+xmldata.length, UNKNOWN_FORMAT_SUFFIX.length);
                xmldata = msg;
            }
            rec.setReceiveDateTime(receiveDate);
            rec.setXmldata(xmldata);
            rec.setDueDelete(false);
            em.persist(rec);
            if (log.isDebugEnabled()) {
                log.debug("Finished processing - " + prompt(xmldata));
            }            
        } catch (Throwable e) {
            log.error("Failed processing - " + prompt(xmldata), e);
        }
    }

    /**
     * returns the message if smaller than the log prompt constant (200 here).
     *
     * @param data the data
     * @return the string
     */
    private static String prompt(byte[] data) {
        try {
            return data.length > MSG_PROMPT_LEN
                    ? (new String(data, 0, MSG_PROMPT_LEN, "UTF-8") + "...") 
                    : new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns a Xml reader.
     *
     * @return the XML reader
     * @throws SAXException the SAX exception
     */
    public XMLReader xmlReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = XMLReaderFactory.createXMLReader();
        }
        return xmlReader;
    }

    /**
     * Xml filter .
     *
     * @return the XML filter
     * @throws TransformerConfigurationException the transformer configuration exception
     */
    public XMLFilter xmlFilter() throws TransformerConfigurationException {
        if (tf == null) {
            tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        }
        if (iheYr4toATNATpl == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            iheYr4toATNATpl = tf.newTemplates(new StreamSource(
                    cl.getResource(IHEYR4_TO_ATNA_XSL).toString()));
        }
        return tf.newXMLFilter(iheYr4toATNATpl);
    }

    /**
     * Checks if is IHE yr4.
     *
     * @param xmldata the xmldata
     * @return true, if is IHE yr4
     */
    public static boolean isIHEYr4(byte[] xmldata) {
        int i = 0;
        while (xmldata[i++] != '<');
        if (xmldata[i] == '?') { // skip <?xml version="1.0" ..?>
            while (xmldata[i++] != '<');
        }
        return xmldata[i] == 'I';
    }

    public byte[] getCache() {
        return cache;
    }

    public void setCache(byte[] cache) {
        this.cache = cache;
    }
    
}
