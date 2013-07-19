package fi.markoa.proto.jersey2.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

/*
MAVEN_OPTS='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=6005' mvn -Dexec.mainClass=fi.markoa.proto.jersey2.jetty.JettyWebApp exec:java

curl -D - http://127.0.0.1:8090/app1/myapp/helloworld1
 */

public class JettyWebApp {
    private static final String WAR_PATH_PROPERTY = "MY_WEB_APP_PATH";

    public static void main(String ... args) throws Exception {
        String warFilePath = System.getProperty(WAR_PATH_PROPERTY);
        if(warFilePath == null)
            throw new RuntimeException(String.format("%s system property not set", WAR_PATH_PROPERTY));

        Server server = new Server(8090);
        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/app1");
        webapp.setWar(warFilePath);
        server.setHandler(webapp);
        server.start();
        server.join();
    }
}
