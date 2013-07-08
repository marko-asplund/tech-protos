package fi.markoa.proto.metrics;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("hello")
public class HelloResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloResource.class);

    @GET
    @Timed
    public String get() {
        return "hello, world";
    }
}