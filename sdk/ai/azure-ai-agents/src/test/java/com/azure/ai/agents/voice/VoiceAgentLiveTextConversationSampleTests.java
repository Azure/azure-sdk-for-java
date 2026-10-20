// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentLiveTextConversationSampleTests {
    private static final String NAME = "sample-live-text-conversation-agent-java";
    private static final String GENERATE = "/agents:generate";
    private static final String AGENT = "/agents/" + NAME;

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void createdAgentIsDeletedOnNormalExitAndFailure(boolean failConversation) {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, 200);
        transport.expect(HttpMethod.DELETE, AGENT, 200);
        AgentsClientBuilder builder = builder(transport);
        IllegalStateException failure = new IllegalStateException("Conversation failed");
        Runnable conversation = () -> {
            try (VoiceAgentLiveTextConversationSample.SampleAgent agent = VoiceAgentLiveTextConversationSample
                .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME)) {
                assertNotNull(agent.details);
                if (failConversation) {
                    throw failure;
                }
            }
        };
        if (failConversation) {
            assertSame(failure, assertThrows(IllegalStateException.class, conversation::run));
        } else {
            conversation.run();
        }
        transport.assertConsumed();
    }

    @Test
    public void conflictDeletesExistingAgentRetriesOnceAndDeletesReplacement() {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, 409);
        transport.expect(HttpMethod.DELETE, AGENT, 200);
        transport.expect(HttpMethod.POST, GENERATE, 200);
        transport.expect(HttpMethod.DELETE, AGENT, 200);
        AgentsClientBuilder builder = builder(transport);
        try (VoiceAgentLiveTextConversationSample.SampleAgent agent = VoiceAgentLiveTextConversationSample
            .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME)) {
            assertNotNull(agent.details);
        }
        transport.assertConsumed();
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 403, 412 })
    public void nonConflictCreationFailureDoesNotDeleteAnAgent(int status) {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, status);
        AgentsClientBuilder builder = builder(transport);
        HttpResponseException error
            = assertThrows(HttpResponseException.class, () -> VoiceAgentLiveTextConversationSample
                .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME));
        assertEquals(status, error.getResponse().getStatusCode());
        transport.assertConsumed();
    }

    @Test
    public void replacementDeletionFailureStopsWithoutRetryingCreation() {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, 409);
        transport.expect(HttpMethod.DELETE, AGENT, 403);
        AgentsClientBuilder builder = builder(transport);
        HttpResponseException error
            = assertThrows(HttpResponseException.class, () -> VoiceAgentLiveTextConversationSample
                .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME));
        assertEquals(403, error.getResponse().getStatusCode());
        transport.assertConsumed();
    }

    @Test
    public void repeatedConflictIsNotRetriedOrDeletedAgain() {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, 409);
        transport.expect(HttpMethod.DELETE, AGENT, 200);
        transport.expect(HttpMethod.POST, GENERATE, 409);
        AgentsClientBuilder builder = builder(transport);
        HttpResponseException error
            = assertThrows(HttpResponseException.class, () -> VoiceAgentLiveTextConversationSample
                .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME));
        assertEquals(409, error.getResponse().getStatusCode());
        transport.assertConsumed();
    }

    @Test
    public void cleanupFailureIsPreservedAlongsideConversationFailure() {
        ScriptedTransport transport = new ScriptedTransport();
        transport.expect(HttpMethod.POST, GENERATE, 200);
        transport.expect(HttpMethod.DELETE, AGENT, 403);
        AgentsClientBuilder builder = builder(transport);
        IllegalStateException failure = new IllegalStateException("Conversation failed");
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> {
            try (VoiceAgentLiveTextConversationSample.SampleAgent agent = VoiceAgentLiveTextConversationSample
                .createAgent(builder.buildAgentsClient(), builder.beta().buildBetaAgentsClient(), NAME)) {
                assertNotNull(agent.details);
                throw failure;
            }
        });
        assertSame(failure, error);
        assertEquals(1, error.getSuppressed().length);
        assertTrue(error.getSuppressed()[0] instanceof HttpResponseException);
        transport.assertConsumed();
    }

    private static AgentsClientBuilder builder(HttpClient transport) {
        return new AgentsClientBuilder().endpoint("https://localhost")
            .credential(new MockTokenCredential())
            .configuration(Configuration.NONE)
            .httpClient(transport)
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(0)))
            .allowPreview(true);
    }

    private static final class ScriptedTransport implements HttpClient {
        private final Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();

        void expect(HttpMethod method, String path, int status) {
            requests.add(request -> {
                assertEquals(method, request.getHttpMethod());
                assertEquals(path, request.getUrl().getPath());
                if (method == HttpMethod.POST) {
                    assertEquals(NAME, request.getBodyAsBinaryData().toObject(java.util.Map.class).get("name"));
                }
                String body = status >= 400
                    ? "{\"error\":{\"code\":\"" + (status == 409 ? "conflict" : "failed")
                        + "\",\"message\":\"failed\"}}"
                    : "{}";
                return new MockHttpResponse(request, status,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    BinaryData.fromString(body).toBytes());
            });
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            throw new AssertionError("The sync sample must not use asynchronous HTTP.");
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
            return requests.removeFirst().apply(request);
        }

        void assertConsumed() {
            assertTrue(requests.isEmpty(), "Expected HTTP requests were not sent");
        }
    }
}
