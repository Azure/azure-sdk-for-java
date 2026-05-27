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
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class SessionLogSyncTest extends ClientTestBase {
    private static final String AGENT_NAME = "MySessionHostedAgent3";
    private static final String AGENT_VERSION = "16";
    private static final String SESSION_ID = "sse-validation-record-sync";

    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    @Disabled
    public void validatesSessionLogStream(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        RequestOptions featureOptions = new RequestOptions().setHeader(HttpHeaderName.fromString("Foundry-Features"),
            AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW.toString());

        deleteSession(client);
        AgentSessionResource session = client
            .createSessionWithResponse(AGENT_NAME,
                BinaryData.fromObject(new com.azure.ai.agents.implementation.models.CreateSessionRequest(
                    new VersionRefIndicator(AGENT_VERSION)).setAgentSessionId(SESSION_ID)),
                featureOptions)
            .getValue()
            .toObject(AgentSessionResource.class);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> scheduledDelete = scheduleSessionDelete(client, executor);
        try {
            assertNotNull(session);

            List<SessionLogEvent> events = assertTimeoutPreemptively(Duration.ofSeconds(120), () -> {
                List<SessionLogEvent> sessionLogEvents = new ArrayList<>();
                for (SessionLogEvent event : client.getSessionLogStream(AGENT_NAME, AGENT_VERSION, SESSION_ID)) {
                    sessionLogEvents.add(event);
                    if (sessionLogEvents.size() == 3) {
                        break;
                    }
                }
                return sessionLogEvents;
            });

            assertSessionLogEvents(events);
        } finally {
            scheduledDelete.cancel(true);
            executor.shutdownNow();
            deleteSession(client);
        }
    }

    private ScheduledFuture<?> scheduleSessionDelete(AgentsClient client, ScheduledExecutorService executor) {
        return executor.schedule(() -> {
            if (getTestMode() != TestMode.PLAYBACK) {
                deleteSession(client);
            }
        }, 20, TimeUnit.SECONDS);
    }

    private static void deleteSession(AgentsClient client) {
        try {
            client.deleteSession(AGENT_NAME, SESSION_ID, AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null);
        } catch (RuntimeException ignored) {
            // Cleanup best effort.
        }
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
