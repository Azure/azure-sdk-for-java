// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDefinitionOptInKeys;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.SessionLogEvent;
import com.azure.ai.agents.models.SessionLogEventType;
import com.azure.ai.agents.models.VersionRefIndicator;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SessionLogRecordValidationTest extends ClientTestBase {
    private static final String AGENT_NAME = "MyHostedAgent9";
    private static final String AGENT_VERSION = "9";
    private static final String ISOLATION_KEY = "sse-validation";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void validatesLiveSessionLogStream(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String sessionId = "sse-validation-record-" + System.currentTimeMillis();
        RequestOptions featureOptions = new RequestOptions().setHeader(HttpHeaderName.fromString("Foundry-Features"),
            AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW.toString());

        AgentSessionResource session = client
            .createSessionWithResponse(AGENT_NAME, ISOLATION_KEY,
                BinaryData.fromObject(new com.azure.ai.agents.implementation.models.CreateSessionRequest(
                    new VersionRefIndicator(AGENT_VERSION)).setAgentSessionId(sessionId)),
                featureOptions)
            .map(response -> response.getValue().toObject(AgentSessionResource.class))
            .block(Duration.ofSeconds(60));

        try {
            assertNotNull(session);
            assertEquals(sessionId, session.getId());

            List<SessionLogEvent> events = client.getSessionLogStream(AGENT_NAME, AGENT_VERSION, sessionId)
                .take(3)
                .timeout(Duration.ofSeconds(45))
                .collectList()
                .block();

            assertNotNull(events);
            assertFalse(events.isEmpty());
            for (SessionLogEvent event : events) {
                assertEquals(SessionLogEventType.LOG, event.getEvent());
                assertNotNull(event.getData());
                assertFalse(event.getData().isEmpty());
            }
        } finally {
            client
                .deleteSession(AGENT_NAME, sessionId, ISOLATION_KEY, AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW)
                .block(Duration.ofSeconds(60));
        }
    }
}
