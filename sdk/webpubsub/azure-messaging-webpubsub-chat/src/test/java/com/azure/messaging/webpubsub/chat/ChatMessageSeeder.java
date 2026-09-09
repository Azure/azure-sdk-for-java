// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.time.Duration;
import java.util.UUID;

final class ChatMessageSeeder {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private ChatMessageSeeder() {
    }

    static void sendTextMessage(String clientAccessUrl, String conversationId, String content) {
        String loginInvocationId = UUID.randomUUID().toString();
        String sendInvocationId = UUID.randomUUID().toString();
        String loginFrame = "{\"type\":\"invoke\",\"invocationId\":\"" + loginInvocationId
            + "\",\"target\":\"event\",\"event\":\"chat.login\",\"dataType\":\"text\",\"data\":\"\"}";
        String sendFrame = "{\"type\":\"invoke\",\"invocationId\":\"" + sendInvocationId
            + "\",\"target\":\"event\",\"event\":\"chat.sendTextMessage\",\"dataType\":\"json\","
            + "\"data\":{\"conversation\":{\"conversationId\":\"" + escape(conversationId) + "\"},\"content\":\""
            + escape(content) + "\"}}";
        Sinks.Many<String> outboundFrames = Sinks.many().unicast().onBackpressureBuffer();

        HttpClient.create()
            .websocket(WebsocketClientSpec.builder().protocols("json.webpubsub.azure.v1").build())
            .uri(clientAccessUrl)
            .handle((inbound, outbound) -> Mono.when(outbound.sendString(outboundFrames.asFlux()).then(),
                inbound.receive().asString().handle((message, sink) -> {
                    if (message.contains("\"type\":\"system\"") && message.contains("\"event\":\"connected\"")) {
                        outboundFrames.tryEmitNext(loginFrame);
                    } else if (message.contains("\"invocationId\":\"" + loginInvocationId + "\"")) {
                        if (message.contains("\"success\":true")) {
                            outboundFrames.tryEmitNext(sendFrame);
                        } else {
                            outboundFrames.tryEmitComplete();
                            sink.error(new IllegalStateException("Chat login invocation failed: " + message));
                        }
                    } else if (message.contains("\"invocationId\":\"" + sendInvocationId + "\"")) {
                        outboundFrames.tryEmitComplete();
                        if (message.contains("\"success\":true")) {
                            sink.complete();
                        } else {
                            sink.error(new IllegalStateException("Chat message invocation failed: " + message));
                        }
                    }
                }).then()))
            .then()
            .block(TIMEOUT);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
