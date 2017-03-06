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

package org.dcm4chee.arr.cdi.rs.ctrl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.dcm4chee.arr.cdi.AuditRecordRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuditRecordRepositoryServiceRS.
 * provides restful interface for the controling of the audit record repository
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@Path("/ctrl")
public class AuditRecordRepositoryServiceRS {

    @Inject
    private AuditRecordRepositoryService service;

    private static final Logger log = LoggerFactory.getLogger(AuditRecordRepositoryServiceRS.class);

    /**
     * isRunning
     * Returns the state of the audit record repository
     * @return the string
     */
    @GET
    @Path("running")
    public String isRunning() {
    	
        return "Device Audit Record repository current running status:<br>" +String.valueOf(service.isRunning());
    }

    /**
     * Start.
     * calls the start method of the ARR service and returns this runs info
     * @return the string
     */
    @GET
    @Path("start")
    public String start() {
    	if(!service.isRunning()){
    	try{
        service.start();
        return "Started Device Audit Record repository with the following information<br>"+service.getListenersInfo();
    	}
    	catch(Exception e)
    	{
    	  log.error("error starting service via restful"+e);
    		return e.getMessage();
    	}
    	}
    	else
    	{
    		return "Audit Record Repository already running";
    	}
    }
    @Context
    UriInfo uriInfo;

    /**
     * Stop.
     * calls the stop method of the ARR service
     * @return the string
     */
    @GET
    @Path("stop")
    public String stop() {
    	try{
            service.stop();
            System.out.println("--------------------- : "+uriInfo.getAbsolutePath().toURL());
            return "Stopped Device Audit Record Repository";
        	}
        	catch(Exception e)
        	{
        	  log.error("error stopping service via restful"+e);
        		return e.getMessage();
        	}
    	
    }

    /**
     * Reload.
     * reloads the configuration
     * will start the service if is stopped
     * @return the string
     * @throws Exception
     *             the exception
     */
    @GET
    @Path("reload")
    public String reload() throws Exception {
    	String info="";
    	info+="Reloading Audit Record Repository Device Information<br><br>";
        service.reload();
        if(service.isRunning())
        {
        	info+="Reload successfull";
        	return info;
        }
        else
        {
        	return info+"problem occurred while reloading configuration<br>Reload failed";
        }
    }

}
