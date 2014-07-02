package fi.markoa.proto.oauth2.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class ProviderInitializer implements ServletContextListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderInitializer.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOGGER.debug("contextInitialized");

    sce.getServletContext().setAttribute("clientStore", new ClientStore());
    sce.getServletContext().setAttribute("userStore", new UserStore());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    LOGGER.debug("contextDestroyed");
  }
}
