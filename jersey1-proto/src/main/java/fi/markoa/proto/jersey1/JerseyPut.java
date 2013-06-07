package fi.markoa.proto.jersey1;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
//import com.sun.jersey.api.json.JSONConfiguration;

import javax.ws.rs.core.MediaType;

/**
 * demonstrate an issue with Jersey 1 & Jackson 2 JAX-RS JSON provider.
 *
 * UniformInterface.put() throws an NPE when unable to connect to host.
 * Jersey 1 default JAX-RS JSON provider throws java.net.ConnectException.
 *
 */
public class JerseyPut {

    public static void main(String ... args) {
        ClientConfig cc = new DefaultClientConfig();
//        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        //cc.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE);
        Client c = Client.create(cc);
//        c.addFilter(new LoggingFilter()); // this seems to make NPE go away
        String url = "http://127.0.0.1:7483/foo/bar";
        url = "http://127.0.0.1:7070/foo/bar";
//        url = "http://www.google.com/";
        WebResource w = c.resource(url);
        w.type(MediaType.APPLICATION_JSON_TYPE).put(new SomeData("abc"));
    }
}
