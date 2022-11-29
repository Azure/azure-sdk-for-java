package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.client.message.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.message.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.message.SendToGroupMessage;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

public class ClientTests {

    public static void main(String[] args) throws Exception {
        WebPubSubServiceClient client = new WebPubSubServiceClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("CONNECTION_STRING"))
            .hub("test_hub")
            .buildClient();

        Mono<WebPubSubClientAccessToken> accessToken = Mono.just(client.getClientAccessToken(new GetClientAccessTokenOptions()
            .setUserId("weidxu")
            .addRole("webpubsub.joinLeaveGroup")
            .addRole("webpubsub.sendToGroup"))).subscribeOn(Schedulers.boundedElastic());

        WebPubSubAsyncClient asyncClient = new WebPubSubClientBuilder()
            .credential(new WebPubSubClientCredential(accessToken
                .map(WebPubSubClientAccessToken::getUrl)))
            .buildAsyncClient();

        asyncClient.start().block();

        long ackId = 0;

        JoinGroupMessage joinGroupMessage = new JoinGroupMessage()
            .setGroup("group1")
            .setAckId(++ackId);
        asyncClient.joinGroup(joinGroupMessage).block();

        SendToGroupMessage sendToGroupMessage = new SendToGroupMessage()
            .setGroup("group1")
            .setAckId(++ackId)
            .setDataType("text")
            .setData(BinaryData.fromString("abc"));
        asyncClient.sendMessageToGroup(sendToGroupMessage).block();

        LeaveGroupMessage leaveGroupMessage = new LeaveGroupMessage()
            .setGroup("group1")
            .setAckId(++ackId);
        asyncClient.leaveGroup(leaveGroupMessage).block();

        sendToGroupMessage = new SendToGroupMessage()
            .setGroup("group1")
            .setAckId(++ackId)
            .setDataType("json")
            .setData(BinaryData.fromObject(Map.of("hello", "world")));
        asyncClient.sendMessageToGroup(sendToGroupMessage).block();

        Thread.sleep(10 * 1000);

        asyncClient.close().block();
    }
}
