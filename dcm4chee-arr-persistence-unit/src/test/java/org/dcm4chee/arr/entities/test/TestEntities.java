package org.dcm4chee.arr.entities.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
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
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(Arquillian.class)
public class TestEntities {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class).addClasses(
                AuditRecord.class, Code.class, ParticipantObject.class,
                ActiveParticipant.class);
        File[] libs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importTestDependencies().importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity().asFile();
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
        

        if(tmpList.size()>0) {
            AuditRecord rec = tmpList.get(0);
        assertNotNull(rec.getEventID());
        System.out.println(rec.getEventDateTime().toString());
        }
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
        if(tmpList.size()>0) {
            Long pk = tmpList.get(0);
        utx.begin();
        em.remove(em.find(AuditRecord.class, pk));
        utx.rollback();
        }
    }

    @Test
    public void addRecord() throws IOException, SAXException,
            TransformerConfigurationException, NotSupportedException,
            SystemException, SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException {
        utx.begin();
        Code c = new Code();
        c.setValue("11111");
        c.setMeaning("blabla");
        c.setDesignator("DCM");
        em.persist(c);
        utx.rollback();
    }
}