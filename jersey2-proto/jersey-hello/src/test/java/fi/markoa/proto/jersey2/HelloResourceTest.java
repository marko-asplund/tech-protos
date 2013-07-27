package fi.markoa.proto.jersey2;

import fi.markoa.proto.jersey2.filter.DemoFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

public class HelloResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HelloResource.class
                , DemoFilter.class
        );
    }

    @Test
    public void testFilter() throws Exception {
        String res = target("/hello").request().get(String.class);
        assertEquals(res, "hello, world");
    }
}
