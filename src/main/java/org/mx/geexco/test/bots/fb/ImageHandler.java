package org.mx.geexco.test.bots.fb;

import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class ImageHandler implements HttpHandler {

	@Override
	public void handleRequest(HttpServerExchange hse) throws Exception {
		if (hse.isInIoThread()) {
			hse.dispatch(this);
			return;
		}
		// Obtiene entrada POST
		hse.startBlocking();
		hse.getResponseHeaders().put(Headers.CONTENT_TYPE, "image/png");
		InputStream is=new Base64InputStream(ImageHandler.class.getClassLoader().getResourceAsStream("header.png"));
		IOUtils.copy(is, hse.getOutputStream());
		hse.endExchange();
		hse.unDispatch();

	}

}
