package org.dcm4chee.arr.listeners.mdb;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

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

    private static Templates iheYr4toATNATpl;
    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    
    private XMLReader xmlReader;
    
    private SAXTransformerFactory tf;
    
    private byte[] cache=null;
    
    /**
     * Default constructor. 
     */
    public ReceiverHelperBean() {
        
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

            DefaultHandler dh = new AuditRecordHandler(em, rec);
            reader.setContentHandler(dh);
            reader.setEntityResolver(dh);
            reader.setErrorHandler(dh);
            reader.setDTDHandler(dh);
            reader.parse(new InputSource(new ByteArrayInputStream(xmldata)));
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
