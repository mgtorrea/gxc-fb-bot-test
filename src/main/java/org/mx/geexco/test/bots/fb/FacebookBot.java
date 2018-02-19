package org.mx.geexco.test.bots.fb;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacebookBot {
	
	 private static Logger log = LoggerFactory.getLogger("Main");
	    
	    public static void main(String[] args) {
	        log.info("INITIALIZING");
	        String hostname = "";
	        try {
	            hostname = InetAddress.getLocalHost().getHostName();
	        } catch (Exception ex) {
	            log.error("Error on getLocalhost", ex);
	            return;
	        }
	        String port = System.getProperty("server.port");
	        log.info("System port: " + port);
	        if (port == null) {
	            port = "8080";
	        }
	        int puerto = Integer.valueOf(port);
	       
	        Undertow server = Undertow.builder().setWorkerThreads(10)
	                .addHttpListener(puerto, hostname)
	                .setHandler(Handlers.path()
	                        .addExactPath("/webhook", new EagerFormParsingHandler().setNext(new WebhookHandler()))
	                        .addExactPath("/img", new EagerFormParsingHandler().setNext(new ImageHandler()))
	                ).build();
	        log.info("Server started on " + hostname + ":" + puerto);
	        server.start();
	        
	    }

}
