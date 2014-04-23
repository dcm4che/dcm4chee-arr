package org.dcm4chee.arr.mdb.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.xml.transform.TransformerConfigurationException;
import org.dcm4chee.arr.listeners.mdb.AuditRecordHandler;
import org.dcm4chee.arr.listeners.mdb.ReceiverHelperBean;
import org.dcm4chee.arr.listeners.mdb.ReceiverHelperBeanLocal;
import org.dcm4chee.arr.listeners.mdb.ReceiverMDB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(Arquillian.class)
public class TestJMS {

    @Deployment
    public static WebArchive createDeployment() {
	WebArchive war1 = ShrinkWrap
		.create(WebArchive.class)
		.addClasses(ReceiverHelperBean.class,ReceiverHelperBeanLocal.class, AuditRecordHandler.class,
			ReceiverMDB.class)
		.addAsManifestResource(new FileAsset(
			new File(
				"src/main/resources/META-INF/beans.xml")),
		"beans.xml");
	File[] libs =  Maven.resolver().loadPomFromFile("testpom.xml").importTestDependencies().importRuntimeAndTestDependencies().resolve().withoutTransitivity().asFile();
	war1.addAsLibraries(libs);
	return war1;
    }
 
    @Resource(mappedName = "/queue/ARRIncoming")
    Queue testQueue;

    @Resource(mappedName = "/ConnectionFactory")
    ConnectionFactory factory;

    @Inject
    private ReceiverHelperBeanLocal myMDBMock;

    @Test
    public void sendMessageAndPersist() throws Exception {

	Connection connection = null;
	Session session = null;
	try {
	    connection = factory.createConnection();
	    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = session.createProducer(testQueue);
	    connection.start();

	    BytesMessage request = session.createBytesMessage();
	    byte[] msginbytes = "<AuditMessage><EventIdentification EventActionCode=\"E\" EventDateTime=\"2014-03-05T13:35:46.881+01:00\" EventOutcomeIndicator=\"4\"><EventID code=\"110114\" codeSystemName=\"DCM\" displayName=\"User Authentication\"/><EventTypeCode code=\"110122\" codeSystemName=\"DCM\" displayName=\"Login\"/></EventIdentification><ActiveParticipant UserID=\"admin\" NetworkAccessPointID=\"10.231.163.243\" NetworkAccessPointTypeCode=\"2\"/><AuditSourceIdentification AuditSourceID=\"IMPAX1234AGFA\"><AuditSourceTypeCode code=\"4\"/></AuditSourceIdentification></AuditMessage>"
		    .getBytes();
	    request.writeBytes(msginbytes);
	    producer.send(request);
	
	} finally {
	    if (connection != null) {
		connection.close();
	    }
	}
    }
    @Test
    public void testMessageProcessed()
    {
	    assertNotNull(myMDBMock.getCache());
	    System.out.println(new String( myMDBMock.getCache()));
    }

    @Test
    public void testIsIHE() throws TransformerConfigurationException, SAXException, IOException {
	byte[] msginbytes = "<AuditMessage>"
		.getBytes();
	assertFalse(((ReceiverHelperBean) myMDBMock).isIHEYr4(msginbytes));
	msginbytes = "<IHE-Syslog-Audit-Message ".getBytes();
	assertTrue(((ReceiverHelperBean)myMDBMock).isIHEYr4(msginbytes));

    } 

}