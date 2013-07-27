package fi.markoa.proto.jersey2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("hello")
public class HelloResource {

    @GET
    public String get(@Context HttpHeaders headers) {
        String r = headers.getHeaderString("X-greetingRecipient");
        System.out.println("get(): "+r);
        return "hello, "+r;
    }
}
