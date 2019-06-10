// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class TestBase {

    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    public String getConnectionString() {
        return CONNECTION_STRING;
    }

    public boolean isTestConfigurationSet() {
        return CONNECTION_STRING != null;
    }

    public EventHubClientBuilder getEventHubClientBuilder() {
        return EventHubClient.builder().credentials(CredentialInfo.from(CONNECTION_STRING));
    }

    public static Mono<Void> pushEventsToPartition(final EventHubClient client, final String partitionId, final int noOfEvents) {
        EventSender sender = client.createSender(new SenderOptions().partitionId(partitionId));
        final Flux<EventData> map = Flux.range(0, noOfEvents).flatMap(number -> {
            final EventData data = new EventData("testString".getBytes(UTF_8));
            return Flux.just(data);
        });
        return sender.send(map);
    }
}
