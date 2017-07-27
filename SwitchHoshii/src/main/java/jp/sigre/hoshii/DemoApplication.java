/**
 *
 */
package jp.sigre.hoshii;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * @author sigre
 *
 */


@SpringBootApplication
@LineMessageHandler //----- ココを追加
public class DemoApplication {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(DemoApplication.class, args);
	}

	//----- ここから -----
	@Autowired
	private LineMessagingService lineMessagingService;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {


		String fixieUrl = System.getenv("FIXIE_URL");
		String[] fixieValues = fixieUrl.split("[/(:\\/@)/]+");
		String fixieUser = fixieValues[1];
		String fixiePassword = fixieValues[2];
		String fixieHost = fixieValues[3];
		int fixiePort = Integer.parseInt(fixieValues[4]);

		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		Authenticator proxyAuthenticator = new Authenticator() {
			@Override public Request authenticate(Route route, Response response) throws IOException {
				String credential = Credentials.basic(fixieUser, fixiePassword);
				return response.request().newBuilder()
						.header("Proxy-Authorization", credential)
						.build();
			}
		};
		clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort)))
		.proxyAuthenticator(proxyAuthenticator);

		OkHttpClient client = clientBuilder.build();
//		Request request = new Request.Builder().url("http://www.example.com").build();
//		Response response = client.newCall(request).execute();
//
//		System.out.println(response.body().string());
//
//		lineMessagingService = (LineMessagingService) clientBuilder.build();

		System.out.println("event: " + event);
		final BotApiResponse apiResponse = lineMessagingService
				.replyMessage(new ReplyMessage(event.getReplyToken(),
						Collections.singletonList(new TextMessage(event.getSource().getUserId()))))
				.execute().body();
		System.out.println("Sent messages: " + apiResponse);
	}

	@EventMapping
	public void defaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}
	//----- ここまで追加 -----

}
