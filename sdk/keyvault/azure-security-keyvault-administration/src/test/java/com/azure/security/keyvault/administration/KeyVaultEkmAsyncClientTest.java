// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyVaultEkmAsyncClientTest extends KeyVaultEkmClientTestBase {
    private KeyVaultEkmAsyncClient asyncClient;
    private HttpClient testHttpClient;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).assertAsync().build();
    }

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        testHttpClient = httpClient;
        asyncClient
            = getClientBuilder(
                buildAsyncAssertingClient(
                    interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
                forCleanup).buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void ekmConnectionLifecycle(HttpClient httpClient) {
        getClient(httpClient, false);

        KeyVaultEkmConnection input = buildConnection();

        // --- Create ---
        StepVerifier.create(asyncClient.createEkmConnectionWithResponse(input)).assertNext(created -> {
            assertEquals(200, created.getStatusCode());
            assertNotNull(created.getValue());
            assertConnectionEquals(input, created.getValue());
        }).verifyComplete();

        // --- Get ---
        StepVerifier.create(asyncClient.getEkmConnectionWithResponse()).assertNext(got -> {
            assertEquals(200, got.getStatusCode());
            assertConnectionEquals(input, got.getValue());
            assertNotNull(got.getValue().getServerCaCertificates());
            assertFalse(got.getValue().getServerCaCertificates().isEmpty());
        }).verifyComplete();

        // --- Check ---
        StepVerifier.create(asyncClient.checkEkmConnectionWithResponse()).assertNext(check -> {
            assertEquals(200, check.getStatusCode());
            assertNotNull(check.getValue());
        }).verifyComplete();

        // --- Get certificate ---
        StepVerifier.create(asyncClient.getEkmCertificateWithResponse()).assertNext(cert -> {
            assertEquals(200, cert.getStatusCode());
            assertNotNull(cert.getValue());
        }).verifyComplete();

        // --- Update ---
        StepVerifier.create(asyncClient.updateEkmConnectionWithResponse(input))
            .assertNext(updated -> assertEquals(200, updated.getStatusCode()))
            .verifyComplete();

        // --- Delete ---
        StepVerifier.create(asyncClient.deleteEkmConnectionWithResponse())
            .assertNext(deleted -> assertEquals(200, deleted.getStatusCode()))
            .verifyComplete();
    }

    @AfterEach
    public void ensureConnectionDeleted() {
        if (interceptorManager.isPlaybackMode() || testHttpClient == null) {
            return;
        }

        // Build a cleanup client whose requests are not recorded so the safety-net delete does not alter recordings.
        KeyVaultEkmAsyncClient cleanupClient
            = getClientBuilder(buildAsyncAssertingClient(testHttpClient), true).buildAsyncClient();

        Mono<Void> cleanup = cleanupClient.deleteEkmConnection()
            .then()
            .onErrorResume(KeyVaultAdministrationException.class,
                ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                    ? Mono.empty()
                    : Mono.error(ex));

        StepVerifier.create(cleanup).verifyComplete();
    }
}
