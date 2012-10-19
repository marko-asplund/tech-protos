package fi.markoa.proto.camel;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DirectoryWatcher {
	private static final Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
	private CamelContext context;

	public void configure() throws Exception {
		ConnectionFactory cf = null;
    try {
    	InitialContext ic = new InitialContext();
			cf = (ConnectionFactory)ic.lookup("/ConnectionFactory");
    } catch (NamingException e) {
    	throw new Exception("unable to create jms connection factory", e);
    }
		logger.debug("cf: "+cf);
		
		context = new DefaultCamelContext();

		JmsComponent jms = new JmsComponent();
		jms.setConnectionFactory(cf);
		context.addComponent("jms", jms);
		
		try {
			context.addRoutes(new RouteBuilder() {
	    	@Override
	      public void configure() throws Exception {
	    		from("file:///tmp/camel")
	    		.process(new LoggingProcessor())
	    		.to("jms:test1");
	      }
	    });
    } catch (Exception e) {
    	throw new Exception("unable to configure watcher routes", e);
    }
	}
	
	public void start() throws Exception {
		context.start();
	}
	
	public void stop() throws Exception {
		context.stop();
	}
	
	public static void main(String ... args) throws Exception {
		logger.debug("main");

		DirectoryWatcher dw = new DirectoryWatcher();
		dw.configure();
		dw.start();

		logger.debug("working");
		Thread.sleep(20000);

		dw.stop();
	}

	public static class LoggingProcessor implements Processor {
		@Override
    public void process(Exchange exch) throws Exception {
			logger.debug("process: "+exch);
    }
	}
	
}
