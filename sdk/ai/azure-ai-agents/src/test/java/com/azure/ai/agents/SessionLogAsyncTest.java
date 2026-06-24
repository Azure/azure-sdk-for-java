// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.SessionLogEvent;
import com.azure.ai.agents.models.SessionLogEventType;
import com.azure.ai.agents.models.VersionRefIndicator;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SessionLogAsyncTest extends ClientTestBase {
    private static final String AGENT_NAME = "MySessionHostedAgent3";
    private static final String AGENT_VERSION = "16";
    private static final String SESSION_ID = "sse-validation-record-async";

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    @Disabled
    public void validatesSessionLogStream(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        BetaAgentsAsyncClient client = getClientBuilder(httpClient, serviceVersion).beta().buildBetaAgentsAsyncClient();

        deleteSession(client);
        AgentSessionResource session = client
            .createSessionWithResponse(AGENT_NAME,
                BinaryData.fromObject(new com.azure.ai.agents.implementation.models.CreateSessionRequest(
                    new VersionRefIndicator(AGENT_VERSION)).setAgentSessionId(SESSION_ID)),
                new RequestOptions())
            .map(response -> response.getValue().toObject(AgentSessionResource.class))
            .block(Duration.ofSeconds(60));

        try {
            assertNotNull(session);

            Disposable scheduledDelete = scheduleSessionDelete(client);
            try {
                List<SessionLogEvent> events = client.getSessionLogStream(AGENT_NAME, AGENT_VERSION, SESSION_ID)
                    .take(3)
                    .timeout(Duration.ofSeconds(120))
                    .collectList()
                    .block();

                assertSessionLogEvents(events);
            } finally {
                scheduledDelete.dispose();
            }
        } finally {
            deleteSession(client);
        }
    }

    private static Disposable scheduleSessionDelete(BetaAgentsAsyncClient client) {
        return Mono.delay(Duration.ofSeconds(20)).then(deleteSessionAsync(client)).subscribe();
    }

    private static void deleteSession(BetaAgentsAsyncClient client) {
        deleteSessionAsync(client).block(Duration.ofSeconds(60));
    }

    private static Mono<Void> deleteSessionAsync(BetaAgentsAsyncClient client) {
        return client.deleteSession(AGENT_NAME, SESSION_ID, null).onErrorResume(error -> Mono.empty());
    }

    private static void assertSessionLogEvents(List<SessionLogEvent> events) {
        assertNotNull(events);
        assertFalse(events.isEmpty());
        for (SessionLogEvent event : events) {
            assertEquals(SessionLogEventType.LOG, event.getEvent());
            assertNotNull(event.getData());
            assertFalse(event.getData().isEmpty());
        }
    }
}
