package fi.markoa.proto.jaxrs2;

import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class HelloResourceTest {
    private static WebArchive war;

    @Test
    public void test1() throws Exception {
        Client c = ClientBuilder.newClient();
        String msg = c.target(getBaseUrl()+"/app1/hello").request().get(String.class);
        assertEquals("hello, world", msg);
    }
    
    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                        .importRuntimeAndTestDependencies().asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                        .addClasses(HelloResource.class)
                        .addAsLibraries(libs)
                        .setWebXML(new File("src/main/webapp", "WEB-INF/web.xml"))
                        ;
        HelloResourceTest.war = war;
        return war;
    }
    
    private String getBaseUrl() {
        String contextRoot = war.getName().substring(0, war.getName().indexOf("."));
        return "http://127.0.0.1:9090/"+contextRoot;
    }
}
