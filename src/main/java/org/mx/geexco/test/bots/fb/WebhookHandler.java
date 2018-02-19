package org.mx.geexco.test.bots.fb;

import java.net.URL;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.common.WebviewHeightRatio;
import com.github.messenger4j.common.WebviewShareButtonState;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TemplateMessage;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.template.GenericTemplate;
import com.github.messenger4j.send.message.template.button.Button;
import com.github.messenger4j.send.message.template.button.CallButton;
import com.github.messenger4j.send.message.template.button.PostbackButton;
import com.github.messenger4j.send.message.template.button.UrlButton;
import com.github.messenger4j.send.message.template.common.DefaultAction;
import com.github.messenger4j.send.message.template.common.Element;
import com.github.messenger4j.webhook.Event;

import io.undertow.util.HeaderMap;

public class WebhookHandler extends AbstractJSONProcessorHandler {

	private String VERIFY_TOKEN = System.getProperty("VERIFY_TOKEN");
	private String PAGE_ACCESS_TOKEN = System.getProperty("PAGE_ACCESS_TOKEN");
	private String APP_SECRET = System.getProperty("APP_SECRET");

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

	private void sendWelcomeMessage(String senderId, Event event) throws Exception {
		final List<Button> buttons = Arrays.asList(
				UrlButton.create("Aviso de privacidad", new URL("http://www.google.com")),
				//CallButton.create("Línea Telefónica", "+15105551234"),
				PostbackButton.create("Duda sobre X concurso", "DUDAS_CONCURSO"),
				//PostbackButton.create("Duda sobre Y taller", "DUDAS_TALLER"),
				PostbackButton.create("Transparencia", "DUDAS_TRANSP"));

		final DefaultAction defaultAction = DefaultAction.create(
				new URL("https://peterssendreceiveapp.ngrok.io/view?item=103"), Optional.of(WebviewHeightRatio.TALL),
				Optional.of(true), Optional.of(new URL("https://peterssendreceiveapp.ngrok.io/")),
				Optional.of(WebviewShareButtonState.HIDE));

		final Element element = Element.create("Bienvenido a XXYYZZ",
				Optional.of("Subtítulo o saludo adicional. ¿En qué podemos ayudarte?"), 
				Optional.of(new URL("https://gxc-fb-bot-test.herokuapp.com/img")),
				Optional.of(defaultAction), Optional.of(buttons));

		final GenericTemplate genericTemplate = GenericTemplate.create(Arrays.asList(element));

		final MessagePayload payload = MessagePayload.create(senderId, MessagingType.RESPONSE,
				TemplateMessage.create(genericTemplate));

		messenger.send(payload);
	}

	private void sendSimpleTextMessage(String senderId, Event event) throws Exception {
		final String text = event.asTextMessageEvent().text();
		LOG.info("MENSAJE DE TEXTO RECIBIDO DEL USUARIO CON ID [" + senderId + "]: " + text);
		final TextMessage textMessage = TextMessage.create("==>" + text.toUpperCase() + "<==");
		final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, textMessage);
		messenger.send(messagePayload);
	}

	private void processPayload(String payload, String signature) {

		try {
			messenger.onReceiveEvents(payload, Optional.of(signature), event -> {
				final String senderId = event.senderId();
				try {
					sendWelcomeMessage(senderId, event);
				} catch (Exception e) {
					LOG.error("Error al enviar el mensaje de bienvenida.", e);
				}

				if (event.isTextMessageEvent()) {

				} else {
					LOG.warn("Evento no manejado: " + event.getClass());
				}
			});
		} catch (Exception e) {
			LOG.error("Error al procesar el evento: ", e);
		}
	}

}
