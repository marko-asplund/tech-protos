package fi.markoa.proto.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInitializer.class);
    JmxReporter jmxReporter = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.debug("contextInitialized");

        String appId = servletContextEvent.getServletContext().getInitParameter("fi.marko.proto.appId");
        LOGGER.debug("appId: "+appId);

        MetricRegistry registry = new MetricRegistry();
        jmxReporter = JmxReporter.forRegistry(registry).inDomain(appId).build();
        jmxReporter.start();
        JmxReporter.

        servletContextEvent.getServletContext().setAttribute(Constants.METRICS_REGISTRY_CTX_ATTR_KEY, registry);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.debug("contextDestroyed");
        jmxReporter.stop();
    }
}