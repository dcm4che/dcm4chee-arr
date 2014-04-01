package org.dcm4chee.arr.cdi.Impl.test;

import java.io.File;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.dcm4chee.arr.cdi.rs.ctrl.AuditRecordRepositoryServiceRS;
import org.dcm4chee.arr.cdi.rs.view.AuditRecordRepositoryServiceViewRS;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
@RunWith(Arquillian.class)
public class TestRS {
    @Inject
    private AuditRecordRepositoryServiceRS ctrlService;

    @Inject
    private AuditRecordRepositoryServiceViewRS viewService;

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
	JavaArchive f5 = Maven
		.resolver()
		.resolve(
			"dcm4che.dcm4chee:dcm4chee-arr-cdi-cleanup:4.3.0-SNAPSHOT")
		.withoutTransitivity()
		.asSingle(JavaArchive.class)
				.addAsManifestResource(
			new FileAsset(new File(
				"src/main/resources/META-INF/beans.xml")),
			"beans.xml");
	WebArchive war1 = ShrinkWrap
		.create(WebArchive.class, "mytest.war")
		.addAsManifestResource(
			new FileAsset(new File(
				"src/main/resources/META-INF/beans.xml")),
			"beans.xml")
		.addClasses(AuditRecordRepositoryServiceRS.class,AuditRecordRepositoryServiceViewRS.class)
		.addAsWebResource(new File("src/main/resources/jboss-web.xml"),
			"jboss-web.xml")
		.addAsWebResource(new File("src/main/resources/web.xml"),
			"web.xml");
	// war1.addAsLibraries(jar1);
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
	JavaArchive mockLib = Maven
		.resolver()
		.resolve(
			"org.mockito:mockito-all:1.8.4")
		.withoutTransitivity()
		.asSingle(JavaArchive.class);
	war1.addAsLibraries(f1);
	war1.addAsLibraries(f2);
	war1.addAsLibraries(f3);
	war1.addAsLibraries(f4);
	war1.addAsLibraries(f5);
	war1.addAsLibraries(f6);
	war1.addAsLibraries(libs);
	war1.addAsLibraries(mockLib);
	return war1;
    }

    @Test
    public void TestServiceStopped() throws Exception {
	ctrlService.stop();
    }
    @Test
    public void TestServiceStarted() throws Exception {
	ctrlService.start();
    }
    
    @Test
    public void TestServiceReloaded() throws Exception {
	ctrlService.reload();
    }
 
    @Test
    public void TestServiceView()
    {
	HttpServletRequest req =  mock(HttpServletRequest.class);
	Response response =viewService.xmlList(req);
	assertEquals(200,response.getStatus());
	assertNotNull(response.getEntity());
    }

}