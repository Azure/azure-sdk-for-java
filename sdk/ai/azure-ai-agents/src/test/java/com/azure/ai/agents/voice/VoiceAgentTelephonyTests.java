// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentTelephonyAsyncClient;
import com.azure.ai.agents.BetaAgentTelephonyClient;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.models.TelephonyBindingStatus;
import com.azure.ai.agents.models.TelephonyTransferTargets;
import com.azure.ai.agents.models.UpdateTelephonyBindingRequest;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sync/async parity for the Python telephony suites. These are HTTP contract tests, not recordings or live calls.
 * Python currently skips these scenarios for service routing/version issues. No provider account is needed here.
 */
public class VoiceAgentTelephonyTests {
    private static final String AGENT = "voice-telephony-test";
    private static final String MISSING = "nonexistent-id";
    private static final String ROOT = "/agents/" + AGENT + "/telephony";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String EMPTY_PAGE = "{\"data\":[],\"has_more\":false}";
    private static final String EMPTY_TARGETS = "{\"transfer_targets\":[]}";
    private static final String TARGETS = "{\"transfer_targets\":[{\"name\":\"sales_desk\","
        + "\"description\":\"Transfers to the sales desk for pricing questions.\","
        + "\"destination\":{\"kind\":\"pstn\",\"value\":\"+14255550123\"}}]}";

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void bindingsAndTransferTargets(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.expect(HttpMethod.GET, ROOT + "/bindings", null, 200, EMPTY_PAGE);
        transport.expect(HttpMethod.GET, ROOT + "/transfer_targets", null, 200, EMPTY_TARGETS);
        transport.expect(HttpMethod.PUT, ROOT + "/transfer_targets", TARGETS, 200, TARGETS);
        transport.expect(HttpMethod.GET, ROOT + "/transfer_targets", null, 200, TARGETS);
        transport.expect(HttpMethod.PUT, ROOT + "/transfer_targets", EMPTY_TARGETS, 200, EMPTY_TARGETS);
        transport.notFound(HttpMethod.GET, ROOT + "/bindings/" + MISSING, null);
        transport.notFound(HttpMethod.PATCH, ROOT + "/bindings/" + MISSING, "{\"status\":\"suspended\"}");
        transport.notFound(HttpMethod.DELETE, ROOT + "/bindings/" + MISSING, null);
        AgentsClientBuilder builder = builder(transport);
        BetaAgentsClient syncClient = builder.beta().buildBetaAgentsClient();
        BetaAgentsAsyncClient asyncClient = builder.beta().buildBetaAgentsAsyncClient();
        assertEquals(0L,
            async
                ? asyncClient.listTelephonyBindings(AGENT).count().block(TIMEOUT)
                : syncClient.listTelephonyBindings(AGENT).stream().count());
        assertTrue(call(async, () -> syncClient.getTelephonyTransferTargets(AGENT),
            () -> asyncClient.getTelephonyTransferTargets(AGENT)).getTransferTargets().isEmpty());
        TelephonyTransferTargets desired = BinaryData.fromString(TARGETS).toObject(TelephonyTransferTargets.class);
        TelephonyTransferTargets replaced
            = call(async, () -> syncClient.replaceTelephonyTransferTargets(AGENT, null, desired.getTransferTargets()),
                () -> asyncClient.replaceTelephonyTransferTargets(AGENT, null, desired.getTransferTargets()));
        assertTargets(replaced);
        assertTargets(call(async, () -> syncClient.getTelephonyTransferTargets(AGENT),
            () -> asyncClient.getTelephonyTransferTargets(AGENT)));
        assertTrue(call(async, () -> syncClient.replaceTelephonyTransferTargets(AGENT, null, Collections.emptyList()),
            () -> asyncClient.replaceTelephonyTransferTargets(AGENT, null, Collections.emptyList()))
                .getTransferTargets()
                .isEmpty());
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyBinding(AGENT, MISSING),
            () -> asyncClient.getTelephonyBinding(AGENT, MISSING)), true);
        UpdateTelephonyBindingRequest update
            = new UpdateTelephonyBindingRequest().setStatus(TelephonyBindingStatus.SUSPENDED);
        assertNotFound(() -> call(async, () -> syncClient.updateTelephonyBinding(AGENT, MISSING, null, update),
            () -> asyncClient.updateTelephonyBinding(AGENT, MISSING, null, update)), true);
        assertNotFound(() -> call(async, () -> {
            syncClient.deleteTelephonyBinding(AGENT, MISSING, null);
            return null;
        }, () -> asyncClient.deleteTelephonyBinding(AGENT, MISSING, null)), true);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void callsNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.expect(HttpMethod.GET, ROOT + "/calls", null, 200, EMPTY_PAGE);
        transport.notFound(HttpMethod.GET, ROOT + "/calls/" + MISSING, null);
        transport.notFound(HttpMethod.POST, ROOT + "/calls/" + MISSING + ":transfer",
            "{\"target\":\"nonexistent-target\"}");
        transport.notFound(HttpMethod.POST, ROOT + "/calls/" + MISSING + ":end", null);
        AgentsClientBuilder builder = builder(transport);
        BetaAgentsClient syncClient = builder.beta().buildBetaAgentsClient();
        BetaAgentsAsyncClient asyncClient = builder.beta().buildBetaAgentsAsyncClient();
        assertEquals(0L,
            async
                ? asyncClient.listTelephonyCalls(AGENT).count().block(TIMEOUT)
                : syncClient.listTelephonyCalls(AGENT).stream().count());
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyCall(AGENT, MISSING),
            () -> asyncClient.getTelephonyCall(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.transferTelephonyCall(AGENT, MISSING, "nonexistent-target"),
            () -> asyncClient.transferTelephonyCall(AGENT, MISSING, "nonexistent-target")), false);
        assertNotFound(() -> call(async, () -> syncClient.endTelephonyCall(AGENT, MISSING),
            () -> asyncClient.endTelephonyCall(AGENT, MISSING)), false);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void generatedAudioNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        String path = "/agents/" + AGENT + "/endpoint/protocols/voice/conversations/" + MISSING + "/items/" + MISSING
            + "/audio/generated";
        transport.notFound(HttpMethod.GET, path, null);
        transport.notFound(HttpMethod.GET, path + "/content", null);
        AgentsClientBuilder builder = builder(transport);
        assertNotFound(() -> call(async,
            () -> builder.beta()
                .buildBetaAgentEndpointConversationsClient()
                .getAgentConversationItemGeneratedAudio(AGENT, MISSING, MISSING),
            () -> builder.beta()
                .buildBetaAgentEndpointConversationsAsyncClient()
                .getAgentConversationItemGeneratedAudio(AGENT, MISSING, MISSING)),
            true);
        assertNotFound(() -> call(async,
            () -> builder.beta()
                .buildBetaAgentEndpointConversationsClient()
                .getAgentConversationItemGeneratedAudioContent(AGENT, MISSING, MISSING),
            () -> builder.beta()
                .buildBetaAgentEndpointConversationsAsyncClient()
                .getAgentConversationItemGeneratedAudioContent(AGENT, MISSING, MISSING)),
            false);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void callJobNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.notFound(HttpMethod.GET, ROOT + "/call_jobs/" + MISSING, null);
        transport.notFound(HttpMethod.POST, ROOT + "/call_jobs/" + MISSING + ":cancel", null);
        AgentsClientBuilder builder = builder(transport);
        BetaAgentTelephonyClient syncClient = builder.beta().buildBetaAgentTelephonyClient();
        BetaAgentTelephonyAsyncClient asyncClient = builder.beta().buildBetaAgentTelephonyAsyncClient();
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyCallJob(AGENT, MISSING),
            () -> asyncClient.getTelephonyCallJob(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.cancelTelephonyCallJob(AGENT, MISSING, null),
            () -> asyncClient.cancelTelephonyCallJob(AGENT, MISSING, null)), true);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void campaignNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        String path = ROOT + "/campaigns/" + MISSING;
        transport.notFound(HttpMethod.GET, path, null);
        transport.notFound(HttpMethod.POST, path + ":cancel", null);
        transport.notFound(HttpMethod.POST, path + ":pause", null);
        transport.notFound(HttpMethod.POST, path + ":resume", null);
        transport.notFound(HttpMethod.GET, path + "/recipient_imports/" + MISSING, null);
        AgentsClientBuilder builder = builder(transport);
        BetaAgentTelephonyClient syncClient = builder.beta().buildBetaAgentTelephonyClient();
        BetaAgentTelephonyAsyncClient asyncClient = builder.beta().buildBetaAgentTelephonyAsyncClient();
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyCampaign(AGENT, MISSING),
            () -> asyncClient.getTelephonyCampaign(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.cancelTelephonyCampaign(AGENT, MISSING),
            () -> asyncClient.cancelTelephonyCampaign(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.pauseTelephonyCampaign(AGENT, MISSING),
            () -> asyncClient.pauseTelephonyCampaign(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.resumeTelephonyCampaign(AGENT, MISSING),
            () -> asyncClient.resumeTelephonyCampaign(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyCampaignRecipientImport(AGENT, MISSING, MISSING),
            () -> asyncClient.getTelephonyCampaignRecipientImport(AGENT, MISSING, MISSING)), true);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void operationNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.notFound(HttpMethod.GET, ROOT + "/operations/" + MISSING, null);
        AgentsClientBuilder builder = builder(transport);
        assertNotFound(() -> call(async,
            () -> builder.beta().buildBetaAgentTelephonyClient().getTelephonyOperation(AGENT, MISSING),
            () -> builder.beta().buildBetaAgentTelephonyAsyncClient().getTelephonyOperation(AGENT, MISSING)), true);
        transport.assertComplete();
    }

    private static void assertTargets(TelephonyTransferTargets targets) {
        assertNotNull(targets);
        assertEquals(1, targets.getTransferTargets().size());
        assertEquals("sales_desk", targets.getTransferTargets().get(0).getName());
        assertEquals("pstn", targets.getTransferTargets().get(0).getDestination().getKind().toString());
    }

    private static <T> T call(boolean async, Supplier<T> syncCall, Supplier<Mono<T>> asyncCall) {
        return async ? asyncCall.get().block(TIMEOUT) : syncCall.get();
    }

    private static void assertNotFound(Runnable operation, boolean typed) {
        HttpResponseException error = assertThrows(HttpResponseException.class, operation::run);
        assertEquals(404, error.getResponse().getStatusCode());
        if (typed) {
            assertInstanceOf(ResourceNotFoundException.class, error);
        }
    }

    private static AgentsClientBuilder builder(HttpClient transport) {
        return new AgentsClientBuilder().endpoint("https://localhost")
            .credential(new MockTokenCredential())
            .httpClient(transport)
            .allowPreview(true);
    }

    private static final class ScriptedTransport implements HttpClient {
        private final boolean async;
        private final Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();

        ScriptedTransport(boolean async) {
            this.async = async;
        }

        void notFound(HttpMethod method, String path, String body) {
            expect(method, path, body, 404, "{\"error\":{\"code\":\"NotFound\",\"message\":\"Resource not found\"}}");
        }

        void expect(HttpMethod method, String path, String body, int status, String response) {
            requests.add(request -> {
                assertEquals(method, request.getHttpMethod());
                assertEquals(path, request.getUrl().getPath());
                assertNull(request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
                if (body != null) {
                    assertEquals(BinaryData.fromString(body).toObject(Map.class),
                        request.getBodyAsBinaryData().toObject(Map.class));
                }
                return new MockHttpResponse(request, status,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    BinaryData.fromString(response).toBytes());
            });
        }

        private HttpResponse respond(HttpRequest request) {
            assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
            return requests.removeFirst().apply(request);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            assertTrue(async, "Sync test must use synchronous HTTP.");
            return Mono.fromSupplier(() -> respond(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            assertFalse(async, "Async test must use asynchronous HTTP.");
            return respond(request);
        }

        void assertComplete() {
            assertTrue(requests.isEmpty(), "All telephony operations must be called.");
        }
    }
}
