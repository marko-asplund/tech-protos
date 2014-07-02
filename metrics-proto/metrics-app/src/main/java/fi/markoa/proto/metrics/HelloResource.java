package fi.markoa.proto.metrics;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Path("hello")
public class HelloResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloResource.class);

    @GET
    @Timed
    public String get() {
        String code = null;
        URL url = null;
        try {
            url = new URL("http://www.hip.fi/");
            HttpURLConnection hc = (HttpURLConnection)url.openConnection();
            code = Integer.toString(hc.getResponseCode());
            System.out.println("response: "+hc.getResponseCode()+", "+hc.getResponseMessage());
            hc.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "hello, world: "+code;
    }
}