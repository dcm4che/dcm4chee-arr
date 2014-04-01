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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.arr.cdi.log;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.EventOutcomeIndicator;
import org.dcm4che3.audit.AuditMessages.EventTypeCode;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.arr.cdi.Impl.Participant;

/**
 * The Class ActivityAudit.
 * Create auditLogStartStop message as specified in the DICOM standard
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class ActivityAudit extends AuditMessage{
	
	private Participant source;
	 private Device device;
	 private AuditLogger logger;
	 private boolean state;
	 
 	/**
     * Instantiates a new activity audit.
     * 
     * @param state
     *            the state
     * @param device
     *            the device
     * @param source
     *            the source
     */
 	public ActivityAudit(boolean state,Device device, Participant source) {
		 this.device=device;
		 this.source=source;
		 this.logger=device.getDeviceExtension(AuditLogger.class);
		 this.state=state;
		 initialize();
		}
	 
 	/**
     * Initialize.
     * sets the message parameters
     */
 	private void initialize()
	 {
		 // Event
	        this.setEventIdentification(AuditMessages.createEventIdentification(
	        		EventID.ApplicationActivity, EventActionCode.Execute, logger.timeStamp(), EventOutcomeIndicator.Success, null,
	                state ? EventTypeCode.ApplicationStart
	                        : EventTypeCode.ApplicationStop));

	        // Active Participant 1: Application started (1)
	        this.getActiveParticipant().add(
	                logger.createActiveParticipant(false, RoleIDCode.Application));

	        // Active Participant 2: Persons and or processes that started the
	        // Application
	        this.getActiveParticipant().add(
	                logger.createActiveParticipant(
	                        true, source.getIdentity(), null, null,
	                        source.getHost(), RoleIDCode.ApplicationLauncher));
	        
	        this.getAuditSourceIdentification().add(
	                logger.createAuditSourceIdentification()); 
	 }
	 
	   /**
     * Gets the logger which is only used here for some of the methods in the audit logger.
     * 
     * @param device
     *            the device
     * @return the logger
     */
   	private AuditLogger getLogger(Device device) {

	        if (device.getDeviceExtension(AuditLogger.class) == null) {
	            AuditLogger auditLogger = new AuditLogger();
	            device.addDeviceExtension(auditLogger);
	        }

	        return device.getDeviceExtension(AuditLogger.class);
	    }
	 
	
}
