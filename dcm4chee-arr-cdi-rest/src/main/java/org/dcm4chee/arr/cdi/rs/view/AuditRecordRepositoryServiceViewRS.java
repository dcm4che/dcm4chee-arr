/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.arr.cdi.rs.view;

import java.nio.charset.Charset;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;

import org.dcm4che3.net.Device;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceUsed;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;
import org.dcm4chee.arr.cdi.conf.ArrDevice;
import org.dcm4chee.arr.cdi.ejb.AuditRecordAccessLocal;
import org.dcm4chee.arr.cdi.ejb.AuditRecordQueryLocal;
import org.dcm4chee.arr.cdi.ejb.XSLTUtils;
import org.dcm4chee.arr.entities.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class AuditRecordRepositoryServiceViewRS.
 * provides restful interface for the querying of audit messages
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@Path("/view")
public class AuditRecordRepositoryServiceViewRS {
	

	@Inject @ArrDevice
	private Device device;
	
	@Inject
	@AuditRecordRepositoryServiceUsed
	private Event<UsedEvent> auditRecordRepositoryServiceUsedEvent;
	
	private static final Logger log = LoggerFactory.getLogger(AuditRecordRepositoryServiceViewRS.class);
	
    /**
     * Xml view.
     * returns one xml message based on the request attribute pk
     * @param req
     *            the req
     * @return the response
     * @throws WebServiceException
     *             the web service exception
     */
    @GET
    @Path("xmlview")
    public Response xmlView(@Context HttpServletRequest req) throws  WebServiceException {
    	long pk=Long.parseLong(req.getParameter("pk"));
    	Response.ResponseBuilder rsp =  Response.ok(getXmlData(findAuditRecord(pk)),"text/xml");
    	auditRecordRepositoryServiceUsedEvent.fire(new UsedEvent(true,device,new RemoteSource(req.getRemoteHost(),req.getRemoteUser(),req.getRequestURI())));
    	return rsp.build();
    }

    /**
     * Html view.
     * returns one html message based on the request attribute pk
     * @param req
     *            the req
     * @return the response
     */
    @GET
    @Path("htmlview")
    public Response htmlView(@Context HttpServletRequest req) {
    	long pk=Long.parseLong(req.getParameter("pk"));
    	Response.ResponseBuilder rsp =  Response.ok(getHtmlData(findAuditRecord(pk)),"text/html; charset=UTF-8");
    	auditRecordRepositoryServiceUsedEvent.fire(new UsedEvent(true,device,new RemoteSource(req.getRemoteHost(),req.getRemoteUser(),req.getRequestURI())));
    	return rsp.build();
    }
    
    /**
     * Gets the ip.
     * returns the ip of a remote host
     * @param request
     *            the request
     * @return the ip
     */
    @GET
    @Produces("application/xml")
    public String getIP(@Context HttpServletRequest request){
       String ip = request.getRemoteAddr();
       return ip;
    }
    
    /**
     * Xml list.
     * returns xml messages based on the request attributes
     * @param req
     *            the req
     * @return the response
     */
    @GET
    @Path("xmllist")
    public Response xmlList(@Context HttpServletRequest req) {
    	
    	List<byte[]> list = null;
        try {
            list = query(req);
        } catch (Exception e) {
            log.error("error retreiving results via restful"+e.getCause());
        }
       String xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><AuditMessages>";
       if(list!=null)
       for (byte[] xmldata : list) {
    	   String str = new String(xmldata);
    	   String newStr = str.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
    	   
        xmlResponse+=newStr;
      
       }
       xmlResponse+="</AuditMessages>";
       String format = req.getParameter("format");
       if (!"orig".equals(format)) {
           String xslFormat = "rfc".equalsIgnoreCase(format) ? XSLTUtils.NORMALIZE_TO_RFC : XSLTUtils.NORMALIZE_TO_DICOM;
           xmlResponse = XSLTUtils.render(xslFormat, xmlResponse.getBytes(Charset.forName("UTF-8")));
       }
       Response.ResponseBuilder rsp = Response.ok(xmlResponse,"text/xml; charset=UTF-8");
       
       auditRecordRepositoryServiceUsedEvent.fire(new UsedEvent(true,device,new RemoteSource(req.getRemoteHost(),req.getRemoteUser(),req.getRequestURI())));
       
        return rsp.build();
    }

    /**
     * Gets the html data.
     * renders a string using xslt and a style
     * @param rec
     *            the rec
     * @return the html data
     */
    public String getHtmlData(AuditRecord rec)
    {
    	return XSLTUtils.render(XSLTUtils.DETAILS, rec.getXmldata());
    }
 
    /**
     * Gets the xml data.
     * retuns the xml of an audit message as a string
     * @param rec
     *            the rec
     * @return the xml data
     */
    protected String getXmlData(AuditRecord rec)
            {
           String xmlData = new String(rec.getXmldata());
           return xmlData;
       
    }

    /**
     * Find audit record.
     * searches for an audit record using the access local bean 
     * access local queries the entities with the given pk and returns
     * @param pk
     *            the pk
     * @return the audit record
     */
    private AuditRecord findAuditRecord(long pk)  {
        InitialContext jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            AuditRecordAccessLocal dao = (AuditRecordAccessLocal)
                    jndiCtx.lookup("java:comp/env/ejb/AuditRecordAccess");
            return dao.findAuditRecord(pk);
        } catch (Exception e) {
          log.error("AuditRecordAccess ejb error "+e);
            throw new WebServiceException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }
    
    /**
     * Query.
     * returns a list of messages in a List<byte[]> used to create a string response of audit messages
     * @param rq
     *            the rq
     * @return the list
     * @throws Exception
     *             the exception
     */
    private List<byte[]> query(HttpServletRequest rq) throws Exception {
        InitialContext jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            AuditRecordQueryLocal dao = (AuditRecordQueryLocal) jndiCtx
                    .lookup("java:comp/env/ejb/AuditRecordQuery");
            return dao.findRecords(rq);
        } finally {
            if (jndiCtx != null)
                try { jndiCtx.close(); } catch (NamingException ignore) {}
        }
    }

}
