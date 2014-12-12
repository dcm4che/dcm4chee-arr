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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.enterprise.event.Observes;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import org.dcm4che3.audit.ActiveParticipant;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.EventIdentification;
import org.dcm4che3.audit.ParticipantObjectIdentification;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.EventOutcomeIndicator;
import org.dcm4che3.audit.AuditMessages.EventTypeCode;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectIDTypeCode;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStarted;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceStopped;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryServiceUsed;
import org.dcm4chee.arr.cdi.Impl.BasicEvent;
import org.dcm4chee.arr.cdi.Impl.LocalSource;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.StartStopEvent;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The Class ActivityLogger. Manages the creation of audit log
 * started/stopped/used messages
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class ActivityLogger {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogger.class);

    private static final String QUEUE_FACTORY = "/ConnectionFactory";
    private static final String QUEUE = "queue/ARRIncoming";
    private QueueConnectionFactory connFactory;
    private QueueConnection conn;
    private Queue queue;
    private QueueSession session;
    private QueueSender sender;
    private String encoding = "UTF-8";
    private String schemaURI = AuditMessages.SCHEMA_URI;
    private boolean formatXML;
    private AuditMessage msg;
    private AuditLogger auditLogger;

    /**
     * Start fired.
     * 
     * @param start
     *            the start event
     * @throws NamingException
     *             the naming exception
     * @throws JMSException
     *             the JMS exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void startFired(
	    @Observes @AuditRecordRepositoryServiceStarted StartStopEvent start)
	    throws NamingException, JMSException, IOException {
	auditLogger = start.getDevice().getDeviceExtension(AuditLogger.class);
	msg = initialize(start, auditLogger);
	if (!auditLogger.isAuditMessageSuppressed(msg)) {
	   sendToJMS(msg);
	    log.info("Observed start event and sent auditrecordrepository started to jms queue");
	} else {
	    log.info("Observed start event and suppressed auditrecordrepository started");
	}
    }

    private void sendToJMS(AuditMessage msg) throws NamingException, JMSException, IOException {
	// TODO Auto-generated method stub
	 InitialContext jndiCtx = new InitialContext();
	    connFactory = (QueueConnectionFactory) jndiCtx
		    .lookup(QUEUE_FACTORY);
	    queue = (Queue) jndiCtx.lookup(QUEUE);
	    conn = connFactory.createQueueConnection();
	    session = conn.createQueueSession(false,
		    QueueSession.AUTO_ACKNOWLEDGE);
	    sender = session.createSender(queue);
	    BytesMessage Bmsg = session.createBytesMessage();
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    AuditMessages.toXML(msg, bos, formatXML, encoding, schemaURI);
	    byte[] msginbytes = bos.toByteArray();
	    Bmsg.writeBytes(msginbytes);
	    sender.send(Bmsg);
	    conn.close();
	    session.close();
    }

    /**
     * Stop fired.
     * 
     * @param start
     *            the stop event
     * @throws NamingException
     *             the naming exception
     * @throws JMSException
     *             the JMS exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void stopFired(
	    @Observes @AuditRecordRepositoryServiceStopped StartStopEvent stop)
	    throws NamingException, JMSException, IOException {
	auditLogger = stop.getDevice().getDeviceExtension(AuditLogger.class);
	msg = initialize(stop, auditLogger);
	if (!auditLogger.isAuditMessageSuppressed(msg)) {
	    sendToJMS(msg);
	    log.info("Observed stop event and sent auditrecordrepository stopped to jms queue");
	} else {
	    log.info("Observed stop event and suppressed auditrecordrepository stopped");
	}
    }

    /**
     * Used fired.
     * 
     * @param used
     *            the used event
     * @throws NamingException
     *             the naming exception
     * @throws JMSException
     *             the JMS exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void usedFired(
	    @Observes @AuditRecordRepositoryServiceUsed UsedEvent used)
	    throws NamingException, JMSException, IOException {
	auditLogger = used.getDevice().getDeviceExtension(AuditLogger.class);
	msg = initialize(used, auditLogger);
	if (!auditLogger.isAuditMessageSuppressed(msg)) {
	    sendToJMS(msg);
	    log.info("Observed used event and sent auditrecordrepository used to jms queue");
	} else {
	    log.info("Observed Used event and suppressed auditrecordrepository used");
	}
    }

    public AuditMessage initialize(BasicEvent event, AuditLogger logger) {
	AuditMessage msg = new AuditMessage();
	if (event.getSource() instanceof LocalSource) {

	    // Event
	    msg.setEventIdentification(AuditMessages.createEventIdentification(
		    EventID.ApplicationActivity, EventActionCode.Execute,
		    logger.timeStamp(), EventOutcomeIndicator.Success, null,
		    event.isState() ? EventTypeCode.ApplicationStart
			    : EventTypeCode.ApplicationStop));

	    // Active Participant 1: Application started (1)
	    msg.getActiveParticipant().add(
		    logger.createActiveParticipant(false,
			    RoleIDCode.Application));

	    // Active Participant 2: Persons and or processes that started the
	    // Application
	    msg.getActiveParticipant().add(
		    logger.createActiveParticipant(true, event.getSource()
			    .getIdentity(), null, null, event.getSource()
			    .getHost(), RoleIDCode.ApplicationLauncher));

	    msg.getAuditSourceIdentification().add(
		    logger.createAuditSourceIdentification());

	    return msg;
	} else if (event instanceof UsedEvent) {
	    // Event
	    EventIdentification eventIdentification = new EventIdentification();
	    eventIdentification.setEventID(EventID.AuditLogUsed);
	    eventIdentification.setEventActionCode(EventActionCode.Read);
	    eventIdentification.setEventDateTime(logger.timeStamp());
	    eventIdentification
		    .setEventOutcomeIndicator(EventOutcomeIndicator.Success);
	    eventIdentification.setEventOutcomeDescription(null);
	    msg.setEventIdentification(eventIdentification);

	    // Active Participant 2: Persons and or processes that started the
	    // Application

	    ActiveParticipant ap = new ActiveParticipant();
	    ap.setUserIsRequestor(true);
	    ap.setUserID(((RemoteSource) event.getSource()).getRemoteIdentity());
	    ap.setNetworkAccessPointID(((RemoteSource) event.getSource())
		    .getRemoteHost());
	    ap.setNetworkAccessPointTypeCode("2");
	    msg.getActiveParticipant().add(ap);
	    msg.getAuditSourceIdentification().add(
		    logger.createAuditSourceIdentification());
	    ParticipantObjectIdentification po = new ParticipantObjectIdentification();
	    po.setParticipantObjectID(((RemoteSource) event.getSource())
		    .getURI());
	    po.setParticipantObjectTypeCode("2");
	    po.setParticipantObjectTypeCodeRole("13");
	    po.setParticipantObjectIDTypeCode(ParticipantObjectIDTypeCode.URI);
	    po.setParticipantObjectName("Security Audit Log");
	    msg.getParticipantObjectIdentification().add(po);
	    return msg;
	}
	return null;
    }

}
