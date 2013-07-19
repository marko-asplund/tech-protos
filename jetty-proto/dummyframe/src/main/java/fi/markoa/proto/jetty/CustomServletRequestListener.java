package fi.markoa.proto.jetty;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class CustomServletRequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("CustomServletRequestListener.requestDestroyed");
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("CustomServletRequestListener.requestInitialized");
    }
}
