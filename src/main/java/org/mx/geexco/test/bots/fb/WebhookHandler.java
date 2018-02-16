package org.mx.geexco.test.bots.fb;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.message.TextMessage;

import io.undertow.util.HeaderMap;

public class WebhookHandler extends AbstractJSONProcessorHandler {

	private String VERIFY_TOKEN = System.getProperty("VERIFY_TOKEN");
	private String PAGE_ACCESS_TOKEN=System.getProperty("PAGE_ACCESS_TOKEN");
	private String APP_SECRET=System.getProperty("APP_SECRET");	
	
	final Messenger messenger;

	public WebhookHandler() {
		super();
		messenger = Messenger.create(PAGE_ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
	}

	@Override
	public String handleGETJSONMessage(Map<String, Deque<String>> params, HeaderMap reqHeaders) {
		if (params.get("hub.mode") == null || params.get("hub.verify_token") == null
				|| params.get("hub.challenge") == null)
			return "Missing params: hub.mode|hub.verify_token|hub.challenge";
		String hubMode = params.get("hub.mode").getFirst();
		String token = params.get("hub.verify_token").getFirst();
		String challenge = params.get("hub.challenge").getFirst();
		try {
			LOG.info("hubMode:" + hubMode, "|token:" + token + "|challenge:" + challenge);
			messenger.verifyWebhook(hubMode, token);
			return challenge;
		} catch (Exception e) {
			LOG.error("Exception processing GET", e);
			return null;
		}
	}

	@Override
	public String handlePOSTJSONMessage(String msg, HeaderMap reqHeaders) {
		String signature = reqHeaders.get("X-Hub-Signature").getFirst();
		// CONSIDER DECOUPLING
		processPayload(msg, signature);
		return "EVENT_RECEIVED";
	}

	private void processPayload(String payload, String signature) {

		try {
			messenger.onReceiveEvents(payload, Optional.of(signature), event -> {
				final String senderId = event.senderId();

				if (event.isTextMessageEvent()) {
					final String text = event.asTextMessageEvent().text();
					LOG.info("MENSAJE DE TEXTO RECIBIDO DEL USUARIO CON ID ["+senderId+"]: "+text);
					final TextMessage textMessage = TextMessage.create("==>" + text.toUpperCase() + "<==");
					final MessagePayload messagePayload = MessagePayload.create(senderId, textMessage);
					try {
						messenger.send(messagePayload);
					} catch (Exception e) {
						LOG.error("Error al enviar el mensaje", e);
					}
				} else {
					LOG.warn("Evento no manejado: " + event.getClass());
				}
			});
		} catch (Exception e) {
			LOG.error("Error al procesar el evento: ", e);
		}
	}

}
