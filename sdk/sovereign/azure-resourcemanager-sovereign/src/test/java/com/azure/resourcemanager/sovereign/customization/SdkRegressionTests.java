// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sovereign.customization;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.sovereign.fluent.models.SovereignViewRemediateResultInner;
import com.azure.resourcemanager.sovereign.implementation.SovereignManagementClientBuilder;
import com.azure.resourcemanager.sovereign.implementation.SovereignManagementClientImpl;
import com.azure.resourcemanager.sovereign.models.ComplianceDimension;
import com.azure.resourcemanager.sovereign.models.SovereignViewComplianceResourceDetailsContent;
import com.azure.resourcemanager.sovereign.models.SovereignViewDimensionDetail;
import com.azure.resourcemanager.sovereign.models.SovereignViewRemediateContent;
import com.azure.resourcemanager.sovereign.models.SovereignViewResourcePatch;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SdkRegressionTests {
    private static final String JOB = "3192dc72-05d7-4ef2-9111-ea715172390d";

    @Test
    public void typedSnapshotRetainsSeventhDigitOnActualRequest() {
        for (String timestamp : new String[] {
            "2026-11-08T20:19:50.7989851Z",
            "2026-11-08T20:19:50.0000001+05:30",
            "2026-11-08T20:19:50.123456789-04:00" }) {
            SovereignViewDimensionDetail dimension = BinaryData.fromString("{\"evaluatedAt\":\"" + timestamp + "\"}")
                .toObject(SovereignViewDimensionDetail.class);
            assertEquals(OffsetDateTime.parse(timestamp), dimension.evaluatedAt());
            assertEquals(OffsetDateTime.parse(timestamp).getNano(), dimension.evaluatedAt().getNano());
            List<HttpRequest> requests = new ArrayList<>();
            SovereignManagementClientImpl client = client(request -> {
                requests.add(request);
                assertTrue(request.getBodyAsBinaryData().toString().contains(timestamp));
                return new Reply(request, 200, "{\"evaluatedAt\":\"" + timestamp + "\"}", false);
            });
            client.getSovereignViewOperations()
                .complianceResourceDetailsWithResponse("rg", "view",
                    new SovereignViewComplianceResourceDetailsContent().withResourceId("/resource")
                        .withDimension(ComplianceDimension.DATA_RESIDENCY)
                        .withEvaluatedAt(dimension.evaluatedAt()),
                    Context.NONE);
            assertEquals(1, requests.size());
            assertTrue(requests.get(0).getUrl().getQuery().contains("api-version=2026-10-31-preview"));
        }
    }

    @Test
    public void flatResultPreservesRemediationId() {
        assertEquals(JOB,
            BinaryData.fromString("{\"remediationId\":\"" + JOB + "\"}")
                .toObject(SovereignViewRemediateResultInner.class)
                .remediationId());
    }

    @Test
    public void listContinuationProviderErrorRemainsVisible() {
        AtomicInteger pages = new AtomicInteger();
        SovereignManagementClientImpl client = client(request -> {
            if (pages.incrementAndGet() == 1) {
                return new Reply(request, 200, "{\"value\":[],\"nextLink\":"
                    + "\"https://management.azure.com/subscriptions/sub/providers/Microsoft.Sovereign/sovereignViews"
                    + "?api-version=2026-10-31-preview&$skiptoken=a%2Bb%2Fc%3D\"}", false);
            }
            assertTrue(request.getUrl().getQuery().contains("$skiptoken=a%2Bb%2Fc%3D"));
            return new Reply(request, 502, "{\"error\":{\"code\":\"ProviderError\",\"message\":\"page two failed\"}}",
                false);
        });
        com.azure.core.management.exception.ManagementException error
            = assertThrows(com.azure.core.management.exception.ManagementException.class,
                () -> client.getSovereignViewOperations().list().forEach(x -> {
                }));
        assertEquals(502, error.getResponse().getStatusCode());
        assertEquals("ProviderError", error.getValue().getCode());
    }

    @Test
    public void tagsOnlyPatchDoesNotInjectManagedIdentity() {
        SovereignManagementClientImpl client = client(request -> {
            if (request.getHttpMethod() == HttpMethod.PATCH) {
                String body = request.getBodyAsBinaryData().toString();
                assertTrue(body.contains("\"tags\""));
                assertFalse(body.contains("\"identity\""));
                return new Reply(request, 202, "{}", true);
            }
            return new Reply(request, 200,
                "{\"status\":\"Failed\",\"error\":"
                    + "{\"code\":\"SovereignViewPatchManagedIdentityError\",\"message\":\"backend identity failure\"}}",
                false);
        });
        com.azure.core.util.polling.PollResponse<com.azure.core.management.polling.PollResult<com.azure.resourcemanager.sovereign.fluent.models.SovereignViewResourceInner>> terminal
            = client.getSovereignViewOperations()
                .beginUpdate("rg", "view",
                    new SovereignViewResourcePatch().withTags(java.util.Collections.singletonMap("test", "tag")))
                .waitForCompletion();
        assertEquals(com.azure.core.util.polling.LongRunningOperationStatus.FAILED, terminal.getStatus());
        assertTrue(terminal.getValue().getError().getResponseBody().contains("SovereignViewPatchManagedIdentityError"));
    }

    @Test
    public void immediateFlatResponsePreservesRemediationId() {
        SovereignManagementClientImpl client
            = client(request -> new Reply(request, 200, "{\"remediationId\":\"" + JOB + "\"}", false));
        assertEquals(JOB,
            client.getSovereignViewOperations()
                .remediate("rg", "view", new SovereignViewRemediateContent())
                .remediationId());
    }

    @Test
    public void failedPollingRetainsTerminalStatusAndServiceError() {
        SovereignManagementClientImpl client = client(request -> request.getHttpMethod() == HttpMethod.POST
            ? new Reply(request, 202, "{}", true)
            : new Reply(request, 200,
                "{\"status\":\"Failed\",\"error\":{\"code\":\"ProviderError\",\"message\":\"service failed\"}}",
                false));
        com.azure.core.util.polling.PollResponse<com.azure.core.management.polling.PollResult<SovereignViewRemediateResultInner>> terminal
            = client.getSovereignViewOperations()
                .beginRemediate("rg", "view", new SovereignViewRemediateContent())
                .waitForCompletion();
        assertEquals(com.azure.core.util.polling.LongRunningOperationStatus.FAILED, terminal.getStatus());
        assertNotNull(terminal.getValue().getError());
        assertTrue(terminal.getValue().getError().getResponseBody().contains("ProviderError"));
    }

    @Test
    public void httpErrorsStillPropagate() {
        SovereignManagementClientImpl client = client(request -> new Reply(request, 400,
            "{\"error\":{\"code\":\"ProviderError\",\"message\":\"service failed\"}}", false));
        com.azure.core.management.exception.ManagementException error
            = assertThrows(com.azure.core.management.exception.ManagementException.class,
                () -> client.getSovereignViewOperations().remediate("rg", "view", new SovereignViewRemediateContent()));
        assertEquals("ProviderError", error.getValue().getCode());
    }

    @Test
    public void flatResultTakesPrecedenceAndNonSuccessDoesNotInventId() {
        assertEquals("flat",
            BinaryData
                .fromString("{\"properties\":{\"remediationId\":\"nested\"},"
                    + "\"status\":\"Succeeded\",\"remediationId\":\"flat\"}")
                .toObject(SovereignViewRemediateResultInner.class)
                .remediationId());
        assertNull(BinaryData.fromString("{\"status\":\"Failed\",\"properties\":{\"remediationId\":\"nested\"}}")
            .toObject(SovereignViewRemediateResultInner.class)
            .remediationId());
    }

    @Test
    public void missingPropertiesAndNullDoNotInventId() {
        for (String body : new String[] { "{}", "{\"properties\":null}", "{\"properties\":{\"unrelated\":1}}" }) {
            assertNull(BinaryData.fromString(body).toObject(SovereignViewRemediateResultInner.class).remediationId());
        }
    }

    private static SovereignManagementClientImpl
        client(java.util.function.Function<HttpRequest, HttpResponse> handler) {
        HttpClient http = request -> Mono.fromSupplier(() -> handler.apply(request));
        return new SovereignManagementClientBuilder().subscriptionId("00000000-0000-0000-0000-000000000001")
            .endpoint("https://management.azure.com")
            .defaultPollInterval(Duration.ofMillis(1))
            .pipeline(new HttpPipelineBuilder().httpClient(http).build())
            .buildClient();
    }

    private static final class Reply extends HttpResponse {
        private final int status;
        private final byte[] body;
        private final HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/json");

        Reply(HttpRequest request, int status, String body, boolean polling) {
            super(request);
            this.status = status;
            this.body = body.getBytes(StandardCharsets.UTF_8);
            if (polling) {
                headers.set("Azure-AsyncOperation",
                    "https://management.azure.com/providers/Microsoft.Sovereign/operations/job");
                headers.set("Location", "https://management.azure.com/providers/Microsoft.Sovereign/operations/job");
                headers.set("Retry-After", "0");
            }
        }

        @Override
        public int getStatusCode() {
            return status;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.just(ByteBuffer.wrap(body));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(body, charset));
        }

        @Override
        public void close() {
        }
    }
}
