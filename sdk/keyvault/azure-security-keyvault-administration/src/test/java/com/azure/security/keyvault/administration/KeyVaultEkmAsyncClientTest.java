// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperation;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperationStatus;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock(KeyVaultEkmClientTestBase.EKM_RESOURCE_LOCK)
public class KeyVaultEkmAsyncClientTest extends KeyVaultEkmClientTestBase {
    private KeyVaultEkmAsyncClient asyncClient;
    private HttpClient testHttpClient;
    private String privateEndpointName;

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

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void ekmPrivateEndpointLifecycle(HttpClient httpClient) {
        getClient(httpClient, false);
        keepPrivateEndpointNameInRecording();

        String privateLinkServiceAlias = getPrivateLinkServiceAlias();
        String name = generatePrivateEndpointName();
        privateEndpointName = name;

        // --- Create ---
        StepVerifier.create(setPlaybackPollerFluxPollInterval(
            asyncClient.beginCreateEkmPrivateEndpoint(name, privateLinkServiceAlias, "Please approve this connection"))
                .last()
                .flatMap(response -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                    assertEquals(name, response.getValue().getPrivateEndpointName());
                    assertEquals(KeyVaultEkmPrivateEndpointOperationType.CREATE,
                        response.getValue().getOperationType());
                    assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, response.getValue().getStatus());

                    return response.getFinalResult();
                }))
            .assertNext(created -> assertEquals(name, created.getName()))
            .verifyComplete();

        // --- Get ---
        StepVerifier.create(asyncClient.getEkmPrivateEndpointWithResponse(name)).assertNext(got -> {
            assertEquals(200, got.getStatusCode());
            assertEquals(name, got.getValue().getName());
            assertNotNull(got.getValue().getProvisioningState());
            assertNotNull(got.getValue().getProperties());
            assertEquals(privateLinkServiceAlias, got.getValue().getProperties().getPrivateLinkServiceAlias());
            assertNotNull(got.getValue().getPrivateLinkServiceConnectionState());
            assertNotNull(got.getValue().getPrivateLinkServiceConnectionState().getStatus());
        }).verifyComplete();

        // --- List ---
        StepVerifier.create(asyncClient.listEkmPrivateEndpointsWithResponse()).assertNext(list -> {
            assertEquals(200, list.getStatusCode());
            assertTrue(list.getValue().stream().anyMatch(pe -> name.equals(pe.getName())));
        }).verifyComplete();

        // --- Delete ---
        AtomicReference<String> operationId = new AtomicReference<>();

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginDeleteEkmPrivateEndpoint(name)).last())
            .assertNext(response -> {
                KeyVaultEkmPrivateEndpointOperation operation = response.getValue();

                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                assertEquals(name, operation.getPrivateEndpointName());
                assertEquals(KeyVaultEkmPrivateEndpointOperationType.DELETE, operation.getOperationType());
                assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, operation.getStatus());
                assertNotNull(operation.getOperationId());
                assertFalse(operation.getOperationId().isEmpty());

                operationId.set(operation.getOperationId());
            })
            .verifyComplete();

        // --- Get operation status ---
        StepVerifier.create(asyncClient.getEkmPrivateEndpointOperationStatusWithResponse(operationId.get()))
            .assertNext(status -> {
                assertEquals(200, status.getStatusCode());
                assertEquals(operationId.get(), status.getValue().getOperationId());
                assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, status.getValue().getStatus());
            })
            .verifyComplete();

        privateEndpointName = null;
    }

    @AfterEach
    public void ensureCleanedUp() {
        if (interceptorManager.isPlaybackMode() || testHttpClient == null) {
            return;
        }

        // Build a cleanup client whose requests are not recorded so the safety-net delete does not alter recordings.
        KeyVaultEkmAsyncClient cleanupClient
            = getClientBuilder(buildAsyncAssertingClient(testHttpClient), true).buildAsyncClient();

        // The connection must go first; the service rejects deleting a private endpoint still referenced by it.
        Mono<Void> cleanup = ignoreNotFound(cleanupClient.deleteEkmConnection().then());

        if (privateEndpointName != null) {
            cleanup = cleanup
                .then(ignoreNotFound(cleanupClient.beginDeleteEkmPrivateEndpoint(privateEndpointName).last().then()));
            privateEndpointName = null;
        }

        StepVerifier.create(cleanup).verifyComplete();
    }

    private static Mono<Void> ignoreNotFound(Mono<Void> mono) {
        return mono.onErrorResume(KeyVaultAdministrationException.class,
            ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404 ? Mono.empty() : Mono.error(ex));
    }
}
