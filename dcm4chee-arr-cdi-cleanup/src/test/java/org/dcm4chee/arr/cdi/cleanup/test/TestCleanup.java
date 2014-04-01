package org.dcm4chee.arr.cdi.cleanup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.inject.Inject;

import org.dcm4chee.arr.cdi.Impl.ReloadEvent;
import org.dcm4chee.arr.cdi.Impl.StartCleanUpEvent;
import org.dcm4chee.arr.cdi.Impl.StopCleanUpEvent;
import org.dcm4chee.arr.cdi.cleanup.AuditRecordRepositoryCleanup;
import org.dcm4chee.arr.cdi.cleanup.CleanUpConfigurationExtension;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilter;
import org.dcm4chee.arr.cdi.cleanup.EventTypeFilterExtension;
import org.dcm4chee.arr.cdi.cleanup.EventTypeObject;
import org.dcm4chee.arr.cdi.cleanup.Impl.AuditRecordRepositoryCleanupImpl;
import org.dcm4chee.arr.cdi.cleanup.ejb.AuditRecordDeleteBean;
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

public class TestCleanup
{
    @Deployment
    public static WebArchive createDeployment() {
	JavaArchive f1 = Maven.resolver()
		.resolve("org.dcm4che:dcm4che-net-audit:3.3.2-SNAPSHOT")
		.withoutTransitivity().asSingle(JavaArchive.class);
	JavaArchive f2 = Maven.resolver()
		.resolve("org.dcm4che:dcm4che-net:3.3.2-SNAPSHOT")
		.withoutTransitivity().asSingle(JavaArchive.class);
	JavaArchive f3 = Maven.resolver()
		.resolve("org.dcm4che:dcm4che-conf-api:3.3.2-SNAPSHOT")
		.withoutTransitivity().asSingle(JavaArchive.class);
	JavaArchive f4 = Maven
		.resolver()
		.resolve(
			"dcm4che.dcm4chee:dcm4chee-arr-cdi-conf-ldap:4.3.0-SNAPSHOT")
		.withoutTransitivity()
		.asSingle(JavaArchive.class)
		.addAsManifestResource(
			new FileAsset(new File(
				"src/main/resources/META-INF/beans.xml")),
			"beans.xml");
	WebArchive war1 = ShrinkWrap.create(WebArchive.class)
		.addAsManifestResource(
			new FileAsset(new File(
				"src/main/resources/META-INF/beans.xml")),
			"beans.xml");
	JavaArchive f6 = Maven
		.resolver()
		.resolve(
			"dcm4che.dcm4chee:dcm4chee-arr-cdi-service:4.3.0-SNAPSHOT")
		.withoutTransitivity()
		.asSingle(JavaArchive.class)
		.addAsManifestResource(
			new FileAsset(new File(
				"src/main/resources/META-INF/beans.xml")),
			"beans.xml");
	File[] libs =  Maven.resolver().loadPomFromFile("testpom.xml").importTestDependencies().importRuntimeAndTestDependencies().resolve().withoutTransitivity().asFile();
	war1.addAsLibraries(f1);
	war1.addAsLibraries(f2);
	war1.addAsLibraries(f3);
	war1.addAsLibraries(f4);
	war1.addAsLibraries(f6);
	war1.addAsLibraries(libs);
	return war1;
    }

    @Inject
    private AuditRecordRepositoryCleanup cleanup;
    @Inject
    private AuditRecordDeleteBean removeTool;
    @Test
    public void testCleanupConfig()
    {
	assertNotNull(cleanup.getCleanUpConfig().getArrCleanUpPollInterval());
    }
    @Test
    public void testcCleanupStart()
    {
	cleanup.startCleanUp(new StartCleanUpEvent(cleanup.getCleanUpConfig().getDevice()));
    }
    @Test
    public void testcCleanupReconfig()
    {
	cleanup.reconfigureCleanUp(new ReloadEvent(cleanup.getCleanUpConfig().getDevice(), true));
    }
    @Test
    public void testgetPKsByEventIDTypeCode()
    {
	assertNotNull(removeTool.getPKsByEventIDTypeCode("110100^DCM", 10, "MINUTES", 2));
    }
    @Test
    public void testgetPksByMaxRecords()
    {
	removeTool.getPksByMaxRecords(10, 2);
    }

 

}
