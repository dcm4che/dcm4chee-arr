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

import org.dcm4che3.audit.ActiveParticipant;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectIDTypeCode;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.EventOutcomeIndicator;
import org.dcm4che3.audit.EventIdentification;
import org.dcm4che3.audit.ParticipantObjectIdentification;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.arr.cdi.Impl.Participant;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;

/**
 * The Class AuditLogUsed.
 * Create auditLogUsed message as specified in the DICOM standard
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class AuditLogUsed extends AuditMessage {

  private RemoteSource source;
  private Device device;
  private AuditLogger logger;
  private boolean state;

  /**
     * Instantiates a new audit log used.
     * 
     * @param state
     *            the state
     * @param device
     *            the device
     * @param source
     *            the source
     */
  public AuditLogUsed(boolean state, Device device, Participant source) {
    this.device = device;
    this.source = (RemoteSource) source;
    this.logger = device.getDeviceExtension(AuditLogger.class);
    this.state = state;
    initialize();
  }

  /**
     * Initialize.
     * creates the message used latter by the logger to create an audit log used message.
     */
  private void initialize() {
    // Event
    EventIdentification eventIdentification = new EventIdentification();
    eventIdentification.setEventID(EventID.AuditLogUsed);
    eventIdentification.setEventActionCode(EventActionCode.Read);
    eventIdentification.setEventDateTime(logger.timeStamp());
    eventIdentification.setEventOutcomeIndicator(EventOutcomeIndicator.Success);
    eventIdentification.setEventOutcomeDescription(null);
    this.setEventIdentification(eventIdentification);

    // Active Participant 2: Persons and or processes that started the
    // Application

    ActiveParticipant ap = new ActiveParticipant();
    ap.setUserIsRequestor(true);
    ap.setUserID(source.getRemoteIdentity());
    ap.setNetworkAccessPointID(source.getRemoteHost());
    ap.setNetworkAccessPointTypeCode("2");
    this.getActiveParticipant().add(ap);
    this.getAuditSourceIdentification().add(logger.createAuditSourceIdentification());
    ParticipantObjectIdentification po = new ParticipantObjectIdentification();
    po.setParticipantObjectID(source.getURI());
    po.setParticipantObjectTypeCode("2");
    po.setParticipantObjectTypeCodeRole("13");
    po.setParticipantObjectIDTypeCode(ParticipantObjectIDTypeCode.URI);
    po.setParticipantObjectName("Security Audit Log");
    this.getParticipantObjectIdentification().add(po);
  }

}
