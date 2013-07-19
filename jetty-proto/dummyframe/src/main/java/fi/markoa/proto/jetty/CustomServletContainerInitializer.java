package fi.markoa.proto.jetty;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

@HandlesTypes({CustomAnnotation.class})
public class CustomServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        System.out.println("CustomServletContainerInitializer.onStartup");

        servletContext.addListener("fi.markoa.proto.jetty.CustomServletContextListener");
        servletContext.addListener("fi.markoa.proto.jetty.CustomServletRequestListener");
        System.out.println("listeners added");
    }
}
