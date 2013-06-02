package fi.markoa.proto.hc;

import java.io.IOException;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;

public class RequestInterceptionDemo {

    public static void main(String ... args) throws Exception {
        HttpClientBuilder cb = HttpClientBuilder.create();
        cb.setRequestExecutor(new InterceptingHttpRequestExecutor());
        HttpGet get = new HttpGet("http://nytimes.com/");
        
        CloseableHttpClient hc = cb.build();
        CloseableHttpResponse r = null;
        HttpEntity entity = null;
        try {
            r = hc.execute(get);
            System.out.println("r: "+r.getStatusLine());
            entity = r.getEntity();
            if(r.getEntity() != null) {
                System.out.println("content: "+EntityUtils.toString(r.getEntity()));
            }
        } finally {
            if(entity != null) {
                EntityUtils.consumeQuietly(entity);
            }
            if(r != null) {
                r.close();
            }
            hc.close();
        }

    }

    public static class InterceptingHttpRequestExecutor extends HttpRequestExecutor {
        @Override
        public HttpResponse execute(HttpRequest rq, HttpClientConnection conn, HttpContext ctx) throws IOException, HttpException {
            HttpHost target = (HttpHost)ctx.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            System.out.println("* execute(): "+target);
            if("www.nytimes.com".equals(target.getHostName())) {
                HttpResponse hr = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "ok");
                hr.setEntity(new StringEntity("hello, world"));
                return hr;
            }
            HttpResponse hr = super.execute(rq, conn, ctx);
            System.out.println("** res: "+hr.getStatusLine());
            return hr;
        }

    }

}
