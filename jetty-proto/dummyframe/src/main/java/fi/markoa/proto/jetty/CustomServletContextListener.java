package fi.markoa.proto.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CustomServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("CustomServletContextListener.contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("CustomServletContextListener.contextDestroyed");
    }
}
