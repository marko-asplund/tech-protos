package fi.markoa.proto.hc;

import java.io.IOException;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.ClientExecChain;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;

public class RequestInterceptionDemo {

    public static void main(String ... args) throws Exception {
        HttpGet get = new HttpGet("http://nytimes.com/");
        
        CloseableHttpClient hc = new MyHttpClientBuilder().build();
        try {
            CloseableHttpResponse r = hc.execute(get);
            try {
                System.out.println("r: "+r.getStatusLine());
                HttpEntity entity = r.getEntity();
                if(entity != null) {
                    System.out.println("content: "+EntityUtils.toString(entity));
                }
            } finally {
                r.close();
            }
        } finally {
            hc.close();
        }

    }

    public static class MyHttpClientBuilder extends HttpClientBuilder {

        @Override
        protected ClientExecChain decorateMainExec(final ClientExecChain mainExec) {
            return new MyExec(super.decorateMainExec(mainExec));
        }

    }

    public static class MyExec implements ClientExecChain {

        private final ClientExecChain mainExec;

        MyExec(final ClientExecChain mainExec) {
            super();
            this.mainExec = mainExec;
        }

        @Override
        public CloseableHttpResponse execute(
                final HttpRoute route,
                final HttpRequestWrapper request,
                final HttpClientContext clientContext,
                final HttpExecutionAware execAware) throws IOException, HttpException {
            HttpHost target = route.getTargetHost();
            System.out.println("* execute(): " + target);
            if("www.nytimes.com".equals(target.getHostName())) {
                MyHttpResponse hr = new MyHttpResponse(HttpVersion.HTTP_1_1, 200, "ok");
                hr.setEntity(new StringEntity("hello, world"));
                return hr;
            }
            CloseableHttpResponse hr = this.mainExec.execute(route, request, clientContext, execAware);
            System.out.println("** res: "+hr.getStatusLine());
            return hr;
        }

    }

    public static class MyHttpResponse extends BasicHttpResponse implements CloseableHttpResponse {

        public MyHttpResponse(final ProtocolVersion ver, final int code, final String reason) {
            super(ver, code, reason);
        }

        @Override
        public void close() throws IOException {
        }

    }

}
