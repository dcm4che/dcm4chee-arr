package org.dcm4chee.arr.entities.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4chee.arr.entities.ActiveParticipant;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.Code;
import org.dcm4chee.arr.entities.ParticipantObject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.FormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

@RunWith(Arquillian.class)
public class TestEntities {

    @Deployment
    public static WebArchive createDeployment() {
	WebArchive war1 = ShrinkWrap.create(WebArchive.class).addClasses(
		AuditRecord.class, Code.class, ParticipantObject.class,
		ActiveParticipant.class);
	File[] libs =  Maven.resolver().loadPomFromFile("testpom.xml").importTestDependencies().importRuntimeAndTestDependencies().resolve().withoutTransitivity().asFile();   
//	JavaArchive f = Maven
//		.resolver()
//		.resolve(
//			"dcm4che.dcm4chee:dcm4chee-arr-listeners-mdb:4.3.0-SNAPSHOT")
//		.withoutTransitivity().asSingle(JavaArchive.class);
//	jar1.addAsManifestResource(
//		new FileAsset(
//			new File(
//				"src/main/resources-${db}/META-INF/test-persistence.xml")),
//		"persistence.xml").addAsManifestResource(
//		new FileAsset(new File(
//			"src/main/resources-${db}/META-INF/beans.xml")),
//		"beans.xml");
war1.addAsLibraries(libs);
	return war1;
    }

    @PersistenceContext(unitName = "dcm4chee-arr")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void readRecord() {
	List<AuditRecord> tmpList = em
		.createQuery(
			"SELECT r FROM org.dcm4chee.arr.entities.AuditRecord r")
		.setMaxResults(1).getResultList();
	AuditRecord rec = tmpList.get(0);

	assertNotNull(rec.getEventID());
	System.out.println(rec.getEventDateTime().toString());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void deleteRecord() throws NotSupportedException, SystemException,
	    SecurityException, IllegalStateException, RollbackException,
	    HeuristicMixedException, HeuristicRollbackException {

	List<Long> tmpList = em
		.createQuery(
			"SELECT r.pk FROM org.dcm4chee.arr.entities.AuditRecord r")
		.setMaxResults(1).getResultList();
	Long pk = tmpList.get(0);
	utx.begin();
	em.remove(em.find(AuditRecord.class, pk));
	utx.rollback();
    }

    @Test
    public void addRecord() throws IOException, SAXException,
	    TransformerConfigurationException, NotSupportedException,
	    SystemException, SecurityException, IllegalStateException,
	    RollbackException, HeuristicMixedException,
	    HeuristicRollbackException {
//	byte[] msginbytes = "<AuditMessage><EventIdentification EventActionCode=\"E\" EventDateTime=\"2014-03-05T13:35:46.881+01:00\" EventOutcomeIndicator=\"4\"><EventID code=\"110114\" codeSystemName=\"DCM\" displayName=\"User Authentication\"/><EventTypeCode code=\"110122\" codeSystemName=\"DCM\" displayName=\"Login\"/></EventIdentification><ActiveParticipant UserID=\"admin\" NetworkAccessPointID=\"10.231.163.243\" NetworkAccessPointTypeCode=\"2\"/><AuditSourceIdentification AuditSourceID=\"IMPAX1234AGFA\"><AuditSourceTypeCode code=\"4\"/></AuditSourceIdentification></AuditMessage>"
//		.getBytes();
//	XMLReader reader = XMLReaderFactory.createXMLReader();
//	AuditRecord rec = new AuditRecord();
//	
//	rec.setEventID(c);
//	rec.setEventDateTime(new Date(System.currentTimeMillis()));
//	rec.setEventOutcome(0);
//	ActiveParticipant ap = new ActiveParticipant();
//	ap.setUserID("sometestid");
//	ap.setUserIsRequestor(true);
//	 ap = new ActiveParticipant();
//	      ap.setAuditRecord(rec);
//	      ap.setUserID("USERID");
//	      ap.setAlternativeUserID("ALTID");
//	      ap.setUserName("NAME");
//	      ap.setUserIsRequestor(true);
//	      ap.setNetworkAccessPointID("127.0.0.1");
//	      ap.setNetworkAccessPointType(2);
	      utx.begin();
//	      em.persist(ap);
//	      ap.setAuditRecord(rec);
	      
//	rec.addActiveParticipant(ap);
//	rec.setSourceID("sometestsource");
//	rec.setXmldata(msginbytes);
//	rec.setReceiveDateTime(new Date(System.currentTimeMillis()));
	Code c = new Code();
	c.setValue("11111");
	c.setMeaning("blabla");
	c.setDesignator("DCM");
	
	em.persist(c);
	
	utx.rollback();
	/*DefaultHandler dh = new AuditRecordHandler(em, rec);
	reader.setContentHandler(dh);
	reader.setEntityResolver(dh);
	reader.setErrorHandler(dh);
	reader.setDTDHandler(dh);
	reader.parse(new InputSource(new ByteArrayInputStream(msginbytes)));
	Date d = new Date();
	d.setTime(System.currentTimeMillis());
	rec.setReceiveDateTime(d);
	rec.setIHEYr4(false);
	rec.setXmldata(msginbytes);
	utx.begin();
	em.persist(rec);
	utx.rollback();*/
    }
}