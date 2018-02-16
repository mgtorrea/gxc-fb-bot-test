package org.mx.geexco.test.bots.fb;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import java.io.ByteArrayOutputStream;
import java.util.Deque;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJSONProcessorHandler implements HttpHandler{
	public abstract String handleGETJSONMessage(Map<String,Deque<String>> params, HeaderMap reqHeaders);
	public abstract String handlePOSTJSONMessage(String msg, HeaderMap reqHeaders);

	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 5);
	StringBuffer sb = new StringBuffer();


	public synchronized void handleRequest(HttpServerExchange hse) throws Exception { // TODO ESTE ELEMENTO SYNCRHONIZED																			// OBJETO UTILIZADO NO SEA
																						// SINGLETON.
		if (hse.isInIoThread()) {
			hse.dispatch(this);
			return;
		}		
		// Obtiene entrada POST
		String data = "";
		hse.startBlocking();
		IOUtils.copy(hse.getInputStream(), baos);
		data = baos.toString("UTF-8");
		baos.reset();
		String res ="Method not supported";
		if(hse.getRequestMethod().equals(Methods.GET))
			res = handleGETJSONMessage(hse.getQueryParameters(), hse.getRequestHeaders());
		if(hse.getRequestMethod().equals(Methods.POST))
			res = handlePOSTJSONMessage(data, hse.getRequestHeaders());
		byte[] b = res.getBytes();
		// hse.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "attachment;
		// filename=output.json");
		hse.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		hse.setResponseContentLength(b.length);

		hse.getOutputStream().write(b);
		// bos.writeTo(hse.getOutputStream());
		hse.endExchange();
		hse.unDispatch();
	}

}
