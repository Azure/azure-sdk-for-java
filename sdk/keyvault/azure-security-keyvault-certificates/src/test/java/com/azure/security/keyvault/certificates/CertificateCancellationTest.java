// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificateCancellationTest {
    private static final String VAULT_URL = "https://cancellation.vault.azure.net";
    private static final LongRunningOperationStatus CANCELLED
        = LongRunningOperationStatus.fromString("cancelled", true);

    @ParameterizedTest
    @EnumSource(CertificateServiceVersion.class)
    void cancelManualCertificateSync(CertificateServiceVersion serviceVersion) {
        AtomicInteger cancellationRequests = new AtomicInteger();
        CertificateClient client = clientBuilder(serviceVersion, cancellationRequests).buildClient();
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> poller
            = client.beginCreateCertificate("pending", TestUtils.setupCancellationPolicy())
                .setPollInterval(Duration.ofMillis(1));

        assertEquals(LongRunningOperationStatus.IN_PROGRESS,
            poller.waitUntil(LongRunningOperationStatus.IN_PROGRESS).getStatus());
        poller.cancelOperation();
        PollResponse<CertificateOperation> response = poller.waitUntil(Duration.ofSeconds(5), CANCELLED);

        assertEquals(CANCELLED, response.getStatus());
        assertEquals("cancelled", response.getValue().getStatus());
        assertTrue(response.getValue().getCancellationRequested());
        assertEquals(1, cancellationRequests.get());
    }

    @ParameterizedTest
    @EnumSource(CertificateServiceVersion.class)
    void cancelManualCertificateAsync(CertificateServiceVersion serviceVersion) {
        AtomicInteger cancellationRequests = new AtomicInteger();
        CertificateAsyncClient client = clientBuilder(serviceVersion, cancellationRequests).buildAsyncClient();
        PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> poller
            = client.beginCreateCertificate("pending", TestUtils.setupCancellationPolicy())
                .setPollInterval(Duration.ofMillis(1));

        StepVerifier
            .create(poller.takeUntil(response -> response.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
                .flatMap(AsyncPollResponse::cancelOperation))
            .assertNext(operation -> assertTrue(operation.getCancellationRequested()))
            .verifyComplete();

        StepVerifier.create(poller.last()).assertNext(response -> {
            assertEquals(CANCELLED, response.getStatus());
            assertEquals("cancelled", response.getValue().getStatus());
            assertTrue(response.getValue().getCancellationRequested());
        }).expectComplete().verify(Duration.ofSeconds(5));
        assertEquals(1, cancellationRequests.get());
    }

    @SuppressWarnings("unchecked")
    private static CertificateClientBuilder clientBuilder(CertificateServiceVersion serviceVersion,
        AtomicInteger cancellationRequests) {
        AtomicBoolean cancellationRequested = new AtomicBoolean();
        HttpClient httpClient = request -> {
            assertEquals("api-version=" + serviceVersion.getVersion(), request.getUrl().getQuery());
            String status = cancellationRequested.get() ? "cancelled" : "inProgress";
            if (request.getHttpMethod() == HttpMethod.POST) {
                assertEquals("/certificates/pending/create", request.getUrl().getPath());
                Map<String, Object> body = request.getBodyAsBinaryData().toObject(Map.class);
                Map<String, Object> policy = (Map<String, Object>) body.get("policy");
                Map<String, Object> issuer = (Map<String, Object>) policy.get("issuer");
                assertEquals("Unknown", issuer.get("name"),
                    "Self-signing can complete before the cancellation request reaches the service.");
            } else if (request.getHttpMethod() == HttpMethod.PATCH) {
                assertEquals("/certificates/pending/pending", request.getUrl().getPath());
                Map<String, Object> body = request.getBodyAsBinaryData().toObject(Map.class);
                assertEquals(Boolean.TRUE, body.get("cancellation_requested"));
                cancellationRequests.incrementAndGet();
                cancellationRequested.set(true);
                status = "cancelled";
            } else {
                assertEquals(HttpMethod.GET, request.getHttpMethod());
                assertEquals("/certificates/pending/pending", request.getUrl().getPath());
            }

            String response = "{\"id\":\"" + VAULT_URL + "/certificates/pending/pending\","
                + "\"issuer\":{\"name\":\"Unknown\"},\"status\":\"" + status + "\"," + "\"cancellation_requested\":"
                + cancellationRequested.get() + "}";
            int statusCode = request.getHttpMethod() == HttpMethod.POST ? 202 : 200;
            return Mono.just(new MockHttpResponse(request, statusCode, response.getBytes(StandardCharsets.UTF_8)));
        };
        return new CertificateClientBuilder().vaultUrl(VAULT_URL)
            .serviceVersion(serviceVersion)
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
    }
}
