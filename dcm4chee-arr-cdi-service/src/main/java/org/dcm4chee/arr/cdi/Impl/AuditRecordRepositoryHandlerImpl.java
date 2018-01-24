/*
 * 
 */
package org.dcm4chee.arr.cdi.Impl;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.BytesMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.audit.AuditRecordHandler;

// TODO: Auto-generated Javadoc

/**
 * The Class AuditRecordRepositoryHandlerImpl.
 * Handles receiving messages from the connections registered on the device 
 * then forwards received messages to the jms queue
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi bsdreko@gmail.com
 */
@ApplicationScoped
public class AuditRecordRepositoryHandlerImpl implements AuditRecordHandler {
    
    private static final Logger log=LoggerFactory.getLogger(AuditRecordRepositoryHandlerImpl.class);

    private static final String QUEUE_FACTORY = "/ConnectionFactory";
    private static final String QUEUE = "queue/ARRIncoming";

    private static final int MSG_PROMPT_LEN = 200;

    private static final int JMS_RETRY_COUNT = 6;

    private static final int JMS_RETRY_INTERVAL = 3000;

    private QueueConnectionFactory connFactory;
    
    private QueueConnection connJMS;
    
    private Queue queue;

	private ThreadLocal<QueueSession> session = new ThreadLocal();

	private ThreadLocal<QueueSender> sender = new ThreadLocal();

	/**
     * closeJMS
     * Closes jms connection and session.
     */
    protected synchronized void closeJMS() {
	if (connJMS != null) {
	    try {
		connJMS.close();
	    } catch (Exception ignore) {
	    }
	    connJMS = null;
	    session = null;
	    sender = null;
	}
    }

    /**
     * initJMS
     * Initializes jms.
     * 
     * @throws Exception
     *             the exception
     */
    protected synchronized void initJMS() throws Exception {
    	if (connJMS == null)
		{
			InitialContext jndiCtx = new InitialContext();
			connFactory = (QueueConnectionFactory)jndiCtx.lookup( QUEUE_FACTORY );
			queue = (Queue)jndiCtx.lookup( QUEUE );
			connJMS = connFactory.createQueueConnection();
			log.info("initialized jms queue");
		}

		if (session.get() == null)
		{
			session.set( connJMS.createQueueSession( false, QueueSession.AUTO_ACKNOWLEDGE ) );
			sender.set(session.get().createSender(queue));
			log.info("initialized jms session for thread {}", Thread.currentThread().getId());
		}
    }

    /**
     * onMessage
     * Receives incoming messages from the registered listeners and forwards them to the jms queue using sendMessage
     * @throws Exception
     *             the exception
     */
    public void onMessage(byte[] data, int xmlOffset, int xmlLength,
	    Connection conn, InetAddress from) {

	if (log.isDebugEnabled()) {
	    log.debug("Received message from " + from + " - " + prompt(data));
	}

	try {
		initJMS();
	    sendMessage(data, xmlOffset, xmlLength, from);
	} catch (Exception e) {

	} finally {

	}

    }

    /**
     * Send message.
     * Sends a message to the ARR queue
     * @param data
     *            the data
     * @param off
     *            the off
     * @param length
     *            the length
     * @param from
     *            the from
     */
    protected void sendMessage(byte[] data, int off, int length,
	    InetAddress from) {
	for (int i = 0; i < JMS_RETRY_COUNT; i++) {
	    try {
		BytesMessage msg = session.get().createBytesMessage();
		msg.setStringProperty("sourceHostAddress",
			from.getHostAddress());

		msg.setStringProperty("sourceHostName", "unknown");
		msg.writeBytes(data, off, length);
		sender.get().send(msg);
		return;
	    } catch (javax.jms.IllegalStateException e) {
		// typically caused by "session.createBytesMessage"
		handleJMSDisconnection();
		continue;
	    } catch (Exception e) {
		if (e.getCause() instanceof IllegalStateException) {
		    // typically caused by "sender.send"
		    handleJMSDisconnection();
		    continue;
		}
		log.error(
			"Failed to schedule processing message received from "
				+ from + " - " + prompt(data), e);
		return;
	    }
	}
    }

    /**
     * returns the message if smaller than the log prompt constant (200 here) for logging purposes.
     *
     * @param data the data
     * @return the string
     */
    private static String prompt(byte[] data) {
	try {
	    return data.length > MSG_PROMPT_LEN ? (new String(data, 0,
		    MSG_PROMPT_LEN, "UTF-8") + "...") : new String(data,
		    "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Handle jms disconnection.
     * Reinitializes jms in case of packet received and in need to send while the connection is null
     */
    private void handleJMSDisconnection() {
	try {
	    initJMS();
	    log.info("Reconnected to JMS");
	} catch (Exception ex) {
	    try {
		Thread.sleep(JMS_RETRY_INTERVAL);
	    } catch (InterruptedException e1) {
	    }
	}
    }

}
