// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpoint;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperation;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperationStatus;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperationType;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock(KeyVaultEkmClientTestBase.EKM_RESOURCE_LOCK)
public class KeyVaultEkmClientTest extends KeyVaultEkmClientTestBase {
    private KeyVaultEkmClient client;
    private HttpClient testHttpClient;
    private String privateEndpointName;

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).assertSync().build();
    }

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        testHttpClient = httpClient;
        client
            = getClientBuilder(
                buildSyncAssertingClient(
                    interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
                forCleanup).buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void ekmConnectionLifecycle(HttpClient httpClient) {
        getClient(httpClient, false);

        KeyVaultEkmConnection input = buildConnection();

        // --- Create ---
        Response<KeyVaultEkmConnection> created = client.createEkmConnectionWithResponse(input, Context.NONE);

        assertEquals(200, created.getStatusCode());
        assertNotNull(created.getValue());
        assertConnectionEquals(input, created.getValue());

        // --- Get ---
        Response<KeyVaultEkmConnection> got = client.getEkmConnectionWithResponse(Context.NONE);

        assertEquals(200, got.getStatusCode());
        assertConnectionEquals(input, got.getValue());
        assertNotNull(got.getValue().getServerCaCertificates());
        assertFalse(got.getValue().getServerCaCertificates().isEmpty());

        // --- Check ---
        Response<KeyVaultEkmProxyInfo> check = client.checkEkmConnectionWithResponse(Context.NONE);

        assertEquals(200, check.getStatusCode());
        assertNotNull(check.getValue());

        // --- Get certificate ---
        Response<KeyVaultEkmProxyClientCertificateInfo> cert = client.getEkmCertificateWithResponse(Context.NONE);

        assertEquals(200, cert.getStatusCode());
        assertNotNull(cert.getValue());

        // --- Update ---
        Response<KeyVaultEkmConnection> updated = client.updateEkmConnectionWithResponse(input, Context.NONE);

        assertEquals(200, updated.getStatusCode());

        // --- Delete ---
        Response<KeyVaultEkmConnection> deleted = client.deleteEkmConnectionWithResponse(Context.NONE);

        assertEquals(200, deleted.getStatusCode());
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void ekmPrivateEndpointLifecycle(HttpClient httpClient) {
        getClient(httpClient, false);
        keepPrivateEndpointNameInRecording();

        String privateLinkServiceAlias = getPrivateLinkServiceAlias();
        privateEndpointName = generatePrivateEndpointName();

        // --- Create ---
        SyncPoller<KeyVaultEkmPrivateEndpointOperation, KeyVaultEkmPrivateEndpoint> createPoller
            = setPlaybackSyncPollerPollInterval(client.beginCreateEkmPrivateEndpoint(privateEndpointName,
                privateLinkServiceAlias, "Please approve this connection"));
        PollResponse<KeyVaultEkmPrivateEndpointOperation> createResponse = createPoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createResponse.getStatus());
        assertEquals(privateEndpointName, createResponse.getValue().getPrivateEndpointName());
        assertEquals(KeyVaultEkmPrivateEndpointOperationType.CREATE, createResponse.getValue().getOperationType());
        assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, createResponse.getValue().getStatus());
        assertEquals(privateEndpointName, createPoller.getFinalResult().getName());

        // --- Get ---
        Response<KeyVaultEkmPrivateEndpoint> got
            = client.getEkmPrivateEndpointWithResponse(privateEndpointName, Context.NONE);

        assertEquals(200, got.getStatusCode());
        assertEquals(privateEndpointName, got.getValue().getName());
        assertNotNull(got.getValue().getProvisioningState());
        assertNotNull(got.getValue().getProperties());
        assertEquals(privateLinkServiceAlias, got.getValue().getProperties().getPrivateLinkServiceAlias());
        assertNotNull(got.getValue().getPrivateLinkServiceConnectionState());
        assertNotNull(got.getValue().getPrivateLinkServiceConnectionState().getStatus());

        // --- List ---
        Response<List<KeyVaultEkmPrivateEndpoint>> list = client.listEkmPrivateEndpointsWithResponse(Context.NONE);

        assertEquals(200, list.getStatusCode());
        assertTrue(list.getValue().stream().anyMatch(pe -> privateEndpointName.equals(pe.getName())));

        // --- Delete ---
        SyncPoller<KeyVaultEkmPrivateEndpointOperation, Void> deletePoller
            = setPlaybackSyncPollerPollInterval(client.beginDeleteEkmPrivateEndpoint(privateEndpointName));
        PollResponse<KeyVaultEkmPrivateEndpointOperation> deleteResponse = deletePoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, deleteResponse.getStatus());
        assertEquals(privateEndpointName, deleteResponse.getValue().getPrivateEndpointName());
        assertEquals(KeyVaultEkmPrivateEndpointOperationType.DELETE, deleteResponse.getValue().getOperationType());
        assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, deleteResponse.getValue().getStatus());

        // --- Get operation status ---
        String operationId = deleteResponse.getValue().getOperationId();

        assertNotNull(operationId);
        assertFalse(operationId.isEmpty());

        Response<KeyVaultEkmPrivateEndpointOperation> status
            = client.getEkmPrivateEndpointOperationStatusWithResponse(operationId, Context.NONE);

        assertEquals(200, status.getStatusCode());
        assertEquals(operationId, status.getValue().getOperationId());
        assertEquals(KeyVaultEkmPrivateEndpointOperationStatus.SUCCEEDED, status.getValue().getStatus());

        privateEndpointName = null;
    }

    @AfterEach
    public void ensureCleanedUp() {
        if (interceptorManager.isPlaybackMode() || testHttpClient == null) {
            return;
        }

        // Build a cleanup client whose requests are not recorded so the safety-net delete does not alter recordings.
        KeyVaultEkmClient cleanupClient
            = getClientBuilder(buildSyncAssertingClient(testHttpClient), true).buildClient();

        // The connection must go first; the service rejects deleting a private endpoint still referenced by it.
        try {
            cleanupClient.deleteEkmConnection();
        } catch (KeyVaultAdministrationException ex) {
            if (ex.getResponse() == null || ex.getResponse().getStatusCode() != 404) {
                throw ex;
            }
            // Already deleted.
        }

        if (privateEndpointName != null) {
            String name = privateEndpointName;
            privateEndpointName = null;

            try {
                cleanupClient.beginDeleteEkmPrivateEndpoint(name).waitForCompletion();
            } catch (KeyVaultAdministrationException ex) {
                if (ex.getResponse() == null || ex.getResponse().getStatusCode() != 404) {
                    throw ex;
                }
                // Already deleted.
            }
        }
    }
}
