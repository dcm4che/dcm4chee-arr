package org.dcm4chee.arr.cdi.cleanup.test;

import static org.junit.Assert.assertNotNull;
import java.io.File;
import javax.inject.Inject;
import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartCleanUpEvent;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.dcm4chee.arr.cdi.cleanup.ejb.AuditRecordDeleteBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestCleanup {
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

    @Inject
    private AuditRecordRepositoryCleanup cleanup;
    @Inject
    private AuditRecordDeleteBean removeTool;

    @Test
    public void testCleanupConfig() {
        assertNotNull(cleanup.getCleanUpConfig().getArrCleanUpPollInterval());
    }

    @Test
    public void testcCleanupStart() {
        cleanup.startCleanUp(new StartCleanUpEvent(cleanup.getCleanUpConfig()
                .getDevice()));
    }

    @Test
    public void testcCleanupReconfig() {
        cleanup.reconfigureCleanUp(new ReloadEvent(cleanup.getCleanUpConfig()
                .getDevice(), true));
    }

    @Test
    public void testgetPKsByEventIDTypeCode() {
        assertNotNull(removeTool.getPKsByEventIDTypeCode("110100^DCM", 10,
                "MINUTES", 2));
    }

    @Test
    public void testgetPksByMaxRecords() {
        removeTool.getPksByMaxRecords(10, 2);
    }

}
