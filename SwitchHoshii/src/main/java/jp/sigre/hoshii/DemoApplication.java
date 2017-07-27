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
import com.linecorp.bot.client.LineMessagingServiceBuilder;
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
public class DemoApplication{

	public static void main(String[] args) throws IOException {

		SpringApplication.run(DemoApplication.class, args);
	}

	//----- ここから -----
	@Autowired
	private LineMessagingService lineMessagingService;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {

		String channelTtoken = System.getenv("LINE_BOT_CHANNEL_TOKEN");
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

		//Proxy prx = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort));
		Proxy prx = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort));
		clientBuilder = clientBuilder.proxy(prx).proxyAuthenticator(proxyAuthenticator);
//
//		OkHttpClient client = clientBuilder.build();
//		Request request = new Request.Builder().url("http://www.example.com").build();
//		Response response = client.newCall(request).execute();
//
//		System.out.println(response.body().string());
//
//		lineMessagingService = (LineMessagingService) clientBuilder.build();

		System.out.println("event: " + event);
		System.out.println("channel token: "+channelTtoken);
		System.out.println("fixie url    : " + fixieUrl);
		System.out.println("fixie host   : " + fixieHost);
		System.out.println("fixie port   : " + fixiePort);

		System.out.println("proxy address: " + prx.address());

		ReplyMessage replyMessage = new ReplyMessage(event.getReplyToken(),
				Collections.singletonList(new TextMessage(event.getSource().getUserId())));

		retrofit2.Response<BotApiResponse> response =
		        LineMessagingServiceBuilder
		                .create(channelTtoken)
		                .okHttpClientBuilder(clientBuilder)
		                .build()
		                .replyMessage(replyMessage)
		                .execute();
		System.out.println("response: " + response.code() + " " + response.message());

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
