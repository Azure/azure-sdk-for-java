// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyVaultEkmClientTest extends KeyVaultEkmClientTestBase {
    private KeyVaultEkmClient client;
    private HttpClient testHttpClient;

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

    @AfterEach
    public void ensureConnectionDeleted() {
        if (interceptorManager.isPlaybackMode() || testHttpClient == null) {
            return;
        }

        // Build a cleanup client whose requests are not recorded so the safety-net delete does not alter recordings.
        KeyVaultEkmClient cleanupClient
            = getClientBuilder(buildSyncAssertingClient(testHttpClient), true).buildClient();

        try {
            cleanupClient.deleteEkmConnection();
        } catch (KeyVaultAdministrationException ex) {
            if (ex.getResponse() == null || ex.getResponse().getStatusCode() != 404) {
                throw ex;
            }
            // Already deleted.
        }
    }
}
