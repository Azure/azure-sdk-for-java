// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import com.azure.ai.agents.models.CreateTelephonyCallJobRequest;
import com.azure.ai.agents.models.CreateTwilioTelephonyBindingRequest;
import com.azure.ai.agents.models.TelephonyBinding;
import com.azure.ai.agents.models.TelephonyBindingListItem;
import com.azure.ai.agents.models.TelephonyBindingStatus;
import com.azure.ai.agents.models.TelephonyCallRecord;
import com.azure.ai.agents.models.TelephonyCallJob;
import com.azure.ai.agents.models.TelephonyCallJobStatus;
import com.azure.ai.agents.models.TelephonyCallSummary;
import com.azure.ai.agents.models.TelephonyOutboundDestination;
import com.azure.ai.agents.models.TelephonyOutboundDestinationType;
import com.azure.ai.agents.models.TelephonyOperation;
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
    private static final String CONNECTION_1 = "twilio-sdk-testing-1";
    private static final String CONNECTION_2 = "twilio-sdk-testing-2";
    private static final String NUMBER_1 = "+13853864628";
    private static final String NUMBER_2 = "+18509702029";
    private static final HttpHeaderName IDEMPOTENCY_KEY = HttpHeaderName.fromString("Idempotency-Key");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String EMPTY_PAGE = "{\"data\":[],\"has_more\":false}";
    private static final String EMPTY_TARGETS = "{\"transfer_targets\":[]}";
    private static final String TARGETS = "{\"transfer_targets\":[{\"name\":\"sales_desk\","
        + "\"description\":\"Transfers to the sales desk for pricing questions.\","
        + "\"destination\":{\"kind\":\"pstn\",\"value\":\"+14255550123\"}}]}";

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void createsTwilioBindingAndOutboundCallJob(boolean async) {
        String bindingRequest = "{\"connection_name\":\"" + CONNECTION_1 + "\",\"label\":\"Java SDK test\","
            + "\"phone_number\":\"" + NUMBER_1 + "\",\"provider\":\"twilio\"}";
        String bindingResponse = "{\"provider\":\"twilio\",\"id\":\"binding-1\",\"label\":\"Java SDK test\","
            + "\"status\":\"active\",\"incoming_call_url\":\"https://example.test/incoming\","
            + "\"connection_name\":\"" + CONNECTION_1 + "\",\"phone_number\":\"" + NUMBER_1 + "\"}";
        String jobRequest = "{\"destination\":{\"type\":\"phone_number\",\"value\":\"" + NUMBER_1 + "\"},"
            + "\"connection_name\":\"" + CONNECTION_2 + "\",\"source\":\"" + NUMBER_2 + "\","
            + "\"purpose\":\"Java SDK telephony validation\"}";
        String jobResponse = "{\"destination\":{\"type\":\"phone_number\",\"value\":\"" + NUMBER_1 + "\"},"
            + "\"connection_name\":\"" + CONNECTION_2 + "\",\"source\":\"" + NUMBER_2 + "\","
            + "\"purpose\":\"Java SDK telephony validation\",\"id\":\"job-1\","
            + "\"object\":\"telephony.call_job\",\"agent_name\":\"" + AGENT + "\",\"status\":\"accepted\","
            + "\"retry_policy\":{\"max_attempts\":1},\"attempt_count\":0,\"revision\":1,"
            + "\"created_at\":1,\"updated_at\":1}";
        String cancelledJobResponse = jobResponse.replace("\"status\":\"accepted\"", "\"status\":\"cancelled\"")
            .replace("\"revision\":1", "\"revision\":2");
        String operationResponse = "{\"id\":\"operation-1\",\"object\":\"telephony.operation\","
            + "\"status\":\"succeeded\",\"created_at\":1,"
            + "\"resource\":{\"id\":\"job-1\",\"type\":\"telephony.call_job\"}}";
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.expect(HttpMethod.POST, ROOT + "/bindings", bindingRequest, 201, bindingResponse);
        transport.expect(HttpMethod.POST, ROOT + "/call_jobs", jobRequest,
            header(IDEMPOTENCY_KEY, "offline-idempotency-key"), 202, jobResponse, new HttpHeaders());
        transport.expect(HttpMethod.GET, ROOT + "/call_jobs/job-1", null, 200, jobResponse);
        transport.expect(HttpMethod.POST, ROOT + "/call_jobs/job-1:cancel", null, header(HttpHeaderName.IF_MATCH, "1"),
            200, cancelledJobResponse, new HttpHeaders());
        transport.expect(HttpMethod.GET, ROOT + "/operations/operation-1", null, 200, operationResponse);
        AgentsClientBuilder builder = builder(transport);
        BetaVoiceAgentsTelephonyClient syncClient = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        BetaVoiceAgentsTelephonyAsyncClient asyncClient = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();

        CreateTwilioTelephonyBindingRequest bindingRequestModel
            = new CreateTwilioTelephonyBindingRequest(CONNECTION_1, NUMBER_1).setLabel("Java SDK test");
        TelephonyBinding binding = call(async, () -> syncClient.createTelephonyBinding(AGENT, bindingRequestModel),
            () -> asyncClient.createTelephonyBinding(AGENT, bindingRequestModel));
        assertEquals("binding-1", binding.getId());
        assertEquals(TelephonyBindingStatus.ACTIVE, binding.getStatus());

        CreateTelephonyCallJobRequest jobRequestModel = new CreateTelephonyCallJobRequest(
            new TelephonyOutboundDestination(TelephonyOutboundDestinationType.PHONE_NUMBER, NUMBER_1), CONNECTION_2,
            NUMBER_2).setPurpose("Java SDK telephony validation");
        TelephonyCallJob job
            = call(async, () -> syncClient.createTelephonyCallJob(AGENT, "offline-idempotency-key", jobRequestModel),
                () -> asyncClient.createTelephonyCallJob(AGENT, "offline-idempotency-key", jobRequestModel));
        assertEquals("job-1", job.getId());
        assertEquals(TelephonyCallJobStatus.ACCEPTED, job.getStatus());
        assertEquals(1L, job.getRevision());
        assertEquals("job-1", call(async, () -> syncClient.getTelephonyCallJob(AGENT, "job-1"),
            () -> asyncClient.getTelephonyCallJob(AGENT, "job-1")).getId());
        TelephonyCallJob cancelled = call(async, () -> syncClient.cancelTelephonyCallJob(AGENT, "job-1", "1"),
            () -> asyncClient.cancelTelephonyCallJob(AGENT, "job-1", "1"));
        assertEquals(TelephonyCallJobStatus.CANCELLED, cancelled.getStatus());
        assertEquals(2L, cancelled.getRevision());
        TelephonyOperation operation = call(async, () -> syncClient.getTelephonyOperation(AGENT, "operation-1"),
            () -> asyncClient.getTelephonyOperation(AGENT, "operation-1"));
        assertEquals("operation-1", operation.getId());
        assertEquals("job-1", operation.getResource().getId());
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void bindingTransferTargetAndCallLifecycle(boolean async) {
        String bindingPath = ROOT + "/bindings/binding-1";
        String binding = "{\"provider\":\"twilio\",\"id\":\"binding-1\",\"label\":\"Java SDK test\","
            + "\"status\":\"active\",\"incoming_call_url\":\"https://example.test/incoming\","
            + "\"connection_name\":\"" + CONNECTION_1 + "\",\"phone_number\":\"" + NUMBER_1 + "\"}";
        String listedBinding = binding.substring(0, binding.length() - 1) + ",\"etag\":\"binding-etag\"}";
        String updatedBinding = binding.replace("Java SDK test", "Updated Java SDK test");
        String callPath = ROOT + "/calls/call-1";
        String activeCall = callRecord("in_progress", "bridging");
        String endedCall = callRecord("success", "completed");
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.expect(HttpMethod.GET, bindingPath, null, 200, binding);
        transport.expect(HttpMethod.GET, ROOT + "/bindings", null, 200,
            "{\"data\":[" + listedBinding + "],\"has_more\":false}");
        transport.expect(HttpMethod.PATCH, bindingPath, "{\"status\":\"active\",\"label\":\"Updated Java SDK test\"}",
            header(HttpHeaderName.IF_MATCH, "*"), 200, updatedBinding, new HttpHeaders());
        transport.expect(HttpMethod.GET, ROOT + "/transfer_targets", null, Collections.emptyMap(), 200, EMPTY_TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "targets-etag"));
        transport.expect(HttpMethod.PUT, ROOT + "/transfer_targets", TARGETS,
            header(HttpHeaderName.IF_MATCH, "targets-etag"), 200, TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "updated-targets-etag"));
        transport.expect(HttpMethod.GET, ROOT + "/calls", null, 200,
            "{\"data\":[" + activeCall + "],\"has_more\":false}");
        transport.expect(HttpMethod.GET, callPath, null, 200, activeCall);
        transport.expect(HttpMethod.POST, callPath + ":transfer", "{\"target\":\"sales_desk\"}", 200, activeCall);
        transport.expect(HttpMethod.POST, callPath + ":end", null, 200, endedCall);
        transport.expect(HttpMethod.DELETE, bindingPath, null, header(HttpHeaderName.IF_MATCH, "*"), 204, null,
            new HttpHeaders());
        AgentsClientBuilder builder = builder(transport);
        BetaVoiceAgentsTelephonyClient syncClient = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        BetaVoiceAgentsTelephonyAsyncClient asyncClient = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();

        assertEquals("binding-1", call(async, () -> syncClient.getTelephonyBinding(AGENT, "binding-1"),
            () -> asyncClient.getTelephonyBinding(AGENT, "binding-1")).getId());
        TelephonyBindingListItem listed = async
            ? asyncClient.listTelephonyBindings(AGENT).blockFirst(TIMEOUT)
            : syncClient.listTelephonyBindings(AGENT).iterator().next();
        assertNotNull(listed);
        assertEquals("binding-etag", listed.getEtag());
        UpdateTelephonyBindingRequest update
            = new UpdateTelephonyBindingRequest().setStatus(TelephonyBindingStatus.ACTIVE)
                .setLabel("Updated Java SDK test");
        assertEquals("Updated Java SDK test",
            call(async, () -> syncClient.updateTelephonyBinding(AGENT, "binding-1", "*", update),
                () -> asyncClient.updateTelephonyBinding(AGENT, "binding-1", "*", update)).getLabel());
        assertTrue(call(async, () -> syncClient.getTelephonyTransferTargets(AGENT),
            () -> asyncClient.getTelephonyTransferTargets(AGENT)).getTransferTargets().isEmpty());
        TelephonyTransferTargets desired = BinaryData.fromString(TARGETS).toObject(TelephonyTransferTargets.class);
        assertTargets(call(async,
            () -> syncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag", desired.getTransferTargets()),
            () -> asyncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag", desired.getTransferTargets())));
        TelephonyCallSummary summary = async
            ? asyncClient.listTelephonyCalls(AGENT).blockFirst(TIMEOUT)
            : syncClient.listTelephonyCalls(AGENT).iterator().next();
        assertNotNull(summary);
        assertEquals("call-1", summary.getId());
        assertEquals("call-1", call(async, () -> syncClient.getTelephonyCall(AGENT, "call-1"),
            () -> asyncClient.getTelephonyCall(AGENT, "call-1")).getId());
        TelephonyCallRecord transferred
            = call(async, () -> syncClient.transferTelephonyCall(AGENT, "call-1", "sales_desk"),
                () -> asyncClient.transferTelephonyCall(AGENT, "call-1", "sales_desk"));
        assertEquals("call-1", transferred.getId());
        assertEquals("success", call(async, () -> syncClient.endTelephonyCall(AGENT, "call-1"),
            () -> asyncClient.endTelephonyCall(AGENT, "call-1")).getStatus().toString());
        call(async, () -> {
            syncClient.deleteTelephonyBinding(AGENT, "binding-1", "*");
            return null;
        }, () -> asyncClient.deleteTelephonyBinding(AGENT, "binding-1", "*"));
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void bindingsAndTransferTargets(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.expect(HttpMethod.GET, ROOT + "/bindings", null, 200, EMPTY_PAGE);
        transport.expect(HttpMethod.GET, ROOT + "/transfer_targets", null, Collections.emptyMap(), 200, EMPTY_TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "targets-etag-1"));
        transport.expect(HttpMethod.PUT, ROOT + "/transfer_targets", TARGETS,
            header(HttpHeaderName.IF_MATCH, "targets-etag-1"), 200, TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "targets-etag-2"));
        transport.expect(HttpMethod.GET, ROOT + "/transfer_targets", null, Collections.emptyMap(), 200, TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "targets-etag-2"));
        transport.expect(HttpMethod.PUT, ROOT + "/transfer_targets", EMPTY_TARGETS,
            header(HttpHeaderName.IF_MATCH, "targets-etag-2"), 200, EMPTY_TARGETS,
            new HttpHeaders().set(HttpHeaderName.ETAG, "targets-etag-3"));
        transport.notFound(HttpMethod.GET, ROOT + "/bindings/" + MISSING, null);
        transport.notFound(HttpMethod.PATCH, ROOT + "/bindings/" + MISSING, "{\"status\":\"suspended\"}");
        transport.notFound(HttpMethod.DELETE, ROOT + "/bindings/" + MISSING, null);
        AgentsClientBuilder builder = builder(transport);
        BetaVoiceAgentsTelephonyClient syncClient = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        BetaVoiceAgentsTelephonyAsyncClient asyncClient = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();
        assertEquals(0L,
            async
                ? asyncClient.listTelephonyBindings(AGENT).count().block(TIMEOUT)
                : syncClient.listTelephonyBindings(AGENT).stream().count());
        assertTrue(call(async, () -> syncClient.getTelephonyTransferTargets(AGENT),
            () -> asyncClient.getTelephonyTransferTargets(AGENT)).getTransferTargets().isEmpty());
        TelephonyTransferTargets desired = BinaryData.fromString(TARGETS).toObject(TelephonyTransferTargets.class);
        TelephonyTransferTargets replaced = call(async,
            () -> syncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag-1", desired.getTransferTargets()),
            () -> asyncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag-1", desired.getTransferTargets()));
        assertTargets(replaced);
        assertTargets(call(async, () -> syncClient.getTelephonyTransferTargets(AGENT),
            () -> asyncClient.getTelephonyTransferTargets(AGENT)));
        assertTrue(call(async,
            () -> syncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag-2", Collections.emptyList()),
            () -> asyncClient.replaceTelephonyTransferTargets(AGENT, "targets-etag-2", Collections.emptyList()))
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
        BetaVoiceAgentsTelephonyClient syncClient = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        BetaVoiceAgentsTelephonyAsyncClient asyncClient = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();
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
                .buildBetaVoiceAgentsConversationsClient()
                .getAgentConversationGeneratedAudioItem(AGENT, MISSING, MISSING),
            () -> builder.beta()
                .buildBetaVoiceAgentsConversationsAsyncClient()
                .getAgentConversationGeneratedAudioItem(AGENT, MISSING, MISSING)),
            true);
        assertNotFound(() -> call(async,
            () -> builder.beta()
                .buildBetaVoiceAgentsConversationsClient()
                .downloadAgentConversationGeneratedAudioItem(AGENT, MISSING, MISSING),
            () -> builder.beta()
                .buildBetaVoiceAgentsConversationsAsyncClient()
                .downloadAgentConversationGeneratedAudioItem(AGENT, MISSING, MISSING)),
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
        BetaVoiceAgentsTelephonyClient syncClient = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        BetaVoiceAgentsTelephonyAsyncClient asyncClient = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();
        assertNotFound(() -> call(async, () -> syncClient.getTelephonyCallJob(AGENT, MISSING),
            () -> asyncClient.getTelephonyCallJob(AGENT, MISSING)), true);
        assertNotFound(() -> call(async, () -> syncClient.cancelTelephonyCallJob(AGENT, MISSING, null),
            () -> asyncClient.cancelTelephonyCallJob(AGENT, MISSING, null)), true);
        transport.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void operationNotFound(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.notFound(HttpMethod.GET, ROOT + "/operations/" + MISSING, null);
        AgentsClientBuilder builder = builder(transport);
        assertNotFound(
            () -> call(async,
                () -> builder.beta().buildBetaVoiceAgentsTelephonyClient().getTelephonyOperation(AGENT, MISSING),
                () -> builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient().getTelephonyOperation(AGENT, MISSING)),
            true);
        transport.assertComplete();
    }

    private static void assertTargets(TelephonyTransferTargets targets) {
        assertNotNull(targets);
        assertEquals(1, targets.getTransferTargets().size());
        assertEquals("sales_desk", targets.getTransferTargets().get(0).getName());
        assertEquals("pstn", targets.getTransferTargets().get(0).getDestination().getKind().toString());
    }

    private static Map<HttpHeaderName, String> header(HttpHeaderName name, String value) {
        return Collections.singletonMap(name, value);
    }

    private static String callRecord(String status, String phase) {
        return "{\"id\":\"call-1\",\"provider\":\"twilio\",\"status\":\"" + status + "\",\"phase\":\"" + phase
            + "\",\"started_at\":1,\"events\":[],\"events_truncated\":false,\"caller_number\":\"" + NUMBER_2
            + "\",\"provider_number\":\"" + NUMBER_1 + "\"}";
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
            expect(method, path, body, Collections.emptyMap(), status, response, new HttpHeaders());
        }

        void expect(HttpMethod method, String path, String body, Map<HttpHeaderName, String> expectedHeaders,
            int status, String response, HttpHeaders responseHeaders) {
            requests.add(request -> {
                assertEquals(method, request.getHttpMethod());
                assertEquals(path, request.getUrl().getPath());
                for (Map.Entry<HttpHeaderName, String> header : expectedHeaders.entrySet()) {
                    assertEquals(header.getValue(), request.getHeaders().getValue(header.getKey()));
                }
                if (!expectedHeaders.containsKey(HttpHeaderName.IF_MATCH)) {
                    assertNull(request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
                }
                if (body != null) {
                    assertEquals(BinaryData.fromString(body).toObject(Map.class),
                        request.getBodyAsBinaryData().toObject(Map.class));
                }
                responseHeaders.set(HttpHeaderName.CONTENT_TYPE, "application/json");
                byte[] responseBody = response == null ? new byte[0] : BinaryData.fromString(response).toBytes();
                return new MockHttpResponse(request, status, responseHeaders, responseBody);
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
