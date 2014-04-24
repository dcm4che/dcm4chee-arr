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
        WebArchive war1 = ShrinkWrap
                .create(WebArchive.class, "mytest.war")
                .addAsManifestResource(
                        new FileAsset(new File(
                                "src/main/resources/META-INF/beans.xml")),
                        "beans.xml")
                .addClasses(AuditRecordRepositoryServiceRS.class,
                        AuditRecordRepositoryServiceViewRS.class)
                .addAsWebResource(new File("src/main/resources/jboss-web.xml"),
                        "jboss-web.xml")
                .addAsWebResource(new File("src/main/resources/web.xml"),
                        "web.xml");
        File[] libs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importTestDependencies().importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity().asFile();
        JavaArchive mockLib = Maven.resolver()
                .resolve("org.mockito:mockito-all:1.8.4").withoutTransitivity()
                .asSingle(JavaArchive.class);
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
    public void TestServiceView() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Response response = viewService.xmlList(req);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

}