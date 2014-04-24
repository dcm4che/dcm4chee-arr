package org.dcm4chee.arr.cdi.Impl.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4chee.arr.cdi.AuditRecordRepositoryService;
import org.dcm4chee.arr.cdi.Impl.AuditRecordRepositoryHandlerImpl;
import org.dcm4chee.arr.cdi.Impl.LocalSource;
import org.dcm4chee.arr.cdi.Impl.RemoteSource;
import org.dcm4chee.arr.cdi.Impl.StartStopEvent;
import org.dcm4chee.arr.cdi.Impl.UsedEvent;
import org.dcm4chee.arr.cdi.log.ActivityLogger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestService {

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war1 = ShrinkWrap.create(WebArchive.class)
                .addAsManifestResource(
                        new FileAsset(new File(
                                "src/main/resources/META-INF/beans.xml")),
                        "beans.xml");
        File[] libs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importTestDependencies().importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity().asFile();
        war1.addAsLibraries(libs);
        return war1;
    }

    @Resource(mappedName = "/queue/ARRIncoming")
    private Queue dlq;

    @Resource(mappedName = "/ConnectionFactory")
    private ConnectionFactory factory;
    @Inject
    private AuditRecordRepositoryHandlerImpl handler;

    @Inject
    private AuditRecordRepositoryService service;

    @Test
    public void TestServiceStop() {
        if (service.isRunning()) {
            service.stop();
        }
        assertNotNull(service.getDevice().getDeviceName());

    }

    @Test
    public void TestServiceStart() {
        try {
            if (!service.isRunning()) {
                service.start();
            }
        } catch (Exception e) {

        }

    }

    @Test
    public void TestServiceReload() throws Exception {
        service.reload();
    }

    @Test
    public void TestHandler() throws UnknownHostException, JMSException {
        byte[] msginbytes = "<AuditMessage><EventIdentification EventActionCode=\"E\" EventDateTime=\"2014-03-05T13:35:46.881+01:00\" EventOutcomeIndicator=\"4\"><EventID code=\"110114\" codeSystemName=\"DCM\" displayName=\"User Authentication\"/><EventTypeCode code=\"110122\" codeSystemName=\"DCM\" displayName=\"Login\"/></EventIdentification><ActiveParticipant UserID=\"admin\" NetworkAccessPointID=\"10.231.163.243\" NetworkAccessPointTypeCode=\"2\"/><AuditSourceIdentification AuditSourceID=\"IMPAX1234AGFA\"><AuditSourceTypeCode code=\"4\"/></AuditSourceIdentification></AuditMessage>"
                .getBytes();

        handler.onMessage(msginbytes, 0, msginbytes.length, service.getDevice()
                .getDeviceExtension(AuditRecordRepository.class)
                .getConnections().get(0), InetAddress.getByName("localhost"));
        Connection connection = factory.createConnection();
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(dlq);
        try {
            connection.start();
            BytesMessage msg = (BytesMessage) consumer.receive();
            assertNotNull(msg);
            int bodyLength = (int) msg.getBodyLength();
            byte[] xmldata = new byte[bodyLength];
            msg.readBytes(xmldata, bodyLength);
            System.out.println(new String(xmldata));
        } finally {
            connection.close();
            session.close();
        }

    }

    @Test
    public void TestLogStartIsSuppressed() {
        AuditLogger logger = service.getDevice().getDeviceExtension(
                AuditLogger.class);
        assertNotNull(logger.isAuditMessageSuppressed((new ActivityLogger())
                .initialize(new StartStopEvent(true, service.getDevice(),
                        new LocalSource()), ((AuditLogger) service.getDevice()
                        .getDeviceExtension(AuditLogger.class)))));
    }

    @Test
    public void TestLogStopIsSuppressed() {
        AuditLogger logger = service.getDevice().getDeviceExtension(
                AuditLogger.class);
        assertNotNull(logger.isAuditMessageSuppressed((new ActivityLogger())
                .initialize(new StartStopEvent(false, service.getDevice(),
                        new LocalSource()), ((AuditLogger) service.getDevice()
                        .getDeviceExtension(AuditLogger.class)))));
    }

    @Test
    public void TestLogUsedIsSuppressed() {
        AuditLogger logger = service.getDevice().getDeviceExtension(
                AuditLogger.class);
        assertNotNull(logger.isAuditMessageSuppressed((new ActivityLogger())
                .initialize(
                        new UsedEvent(false, service.getDevice(),
                                new RemoteSource("127.0.0.1", "aprta",
                                        "/dcm4chee-arr/view/xmllist")),
                        ((AuditLogger) service.getDevice().getDeviceExtension(
                                AuditLogger.class)))));
    }

}