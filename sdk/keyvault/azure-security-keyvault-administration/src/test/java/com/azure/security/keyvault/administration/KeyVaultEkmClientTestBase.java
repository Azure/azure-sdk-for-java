// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class KeyVaultEkmClientTestBase extends KeyVaultAdministrationClientTestBase {
    // Shared by the sync and async EKM tests: the HSM has a single EKM connection and a pool of at most two private
    // endpoints, so the classes must not run concurrently against a live service.
    static final String EKM_RESOURCE_LOCK = "KeyVaultEkm";

    // Placeholder values used during playback so the tests can run without live resources or secrets.
    private static final String PLAYBACK_HOST = "ekm.contoso.com";
    private static final String PLAYBACK_PATH_PREFIX = "/api/v1";
    private static final byte[] PLAYBACK_CA_CERTIFICATE = new byte[] { 0x00 };
    private static final String PLAYBACK_PRIVATE_LINK_SERVICE_ALIAS = "fake-private-link-service-id";

    KeyVaultEkmClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        if (!interceptorManager.isLiveMode() && !forCleanup) {
            List<TestProxySanitizer> sanitizers = new ArrayList<>();
            sanitizers.add(new TestProxySanitizer("$..host", null, PLAYBACK_HOST, TestProxySanitizerType.BODY_KEY));
            sanitizers.add(new TestProxySanitizer("$..privateLinkServiceId", null, PLAYBACK_PRIVATE_LINK_SERVICE_ALIAS,
                TestProxySanitizerType.BODY_KEY));
            sanitizers
                .add(new TestProxySanitizer("[A-Za-z0-9+/]{100,}={0,2}", "AA==", TestProxySanitizerType.BODY_REGEX));
            interceptorManager.addSanitizers(sanitizers);
        }

        return new KeyVaultEkmClientBuilder().vaultUrl(getEndpoint()).pipeline(getPipeline(httpClient, forCleanup));
    }

    @Test
    public abstract void ekmConnectionLifecycle(HttpClient httpClient);

    @Test
    public abstract void ekmPrivateEndpointLifecycle(HttpClient httpClient);

    String generatePrivateEndpointName() {
        return testResourceNamer.randomName("ekm-pe-", 24);
    }

    // The default $..name sanitizer would hide the private endpoint name, which is asserted on and is not a secret.
    void keepPrivateEndpointNameInRecording() {
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3493");
        }
    }

    String getPrivateLinkServiceAlias() {
        if (interceptorManager.isPlaybackMode()) {
            return PLAYBACK_PRIVATE_LINK_SERVICE_ALIAS;
        }

        String alias = Configuration.getGlobalConfiguration().get("EKM_PRIVATE_LINK_SERVICE_ID");
        Assumptions.assumeTrue(alias != null, "EKM_PRIVATE_LINK_SERVICE_ID is not defined.");

        return alias;
    }

    /**
     * Builds the {@link KeyVaultEkmConnection} used as input for the lifecycle tests. In playback mode it returns a
     * deterministic connection; in live/record mode it reads the host, path prefix and CA certificate from the
     * environment.
     *
     * @return The {@link KeyVaultEkmConnection} to create.
     */
    KeyVaultEkmConnection buildConnection() {
        return new KeyVaultEkmConnection(getEkmHost(), Collections.singletonList(readCaCertificate()))
            .setPathPrefix(getEkmPathPrefix());
    }

    private String getEkmHost() {
        if (interceptorManager.isPlaybackMode()) {
            return PLAYBACK_HOST;
        }

        String host = Configuration.getGlobalConfiguration().get("EKM_PROXY_HOST");
        Assumptions.assumeTrue(host != null, "EKM_PROXY_HOST is not defined.");

        return host;
    }

    private String getEkmPathPrefix() {
        return interceptorManager.isPlaybackMode()
            ? PLAYBACK_PATH_PREFIX
            : Configuration.getGlobalConfiguration().get("EKM_PATH_PREFIX");
    }

    private byte[] readCaCertificate() {
        if (interceptorManager.isPlaybackMode()) {
            return PLAYBACK_CA_CERTIFICATE;
        }

        String base64 = Configuration.getGlobalConfiguration().get("EKM_SERVER_CA_CERTIFICATE");
        Assumptions.assumeTrue(base64 != null, "EKM_SERVER_CA_CERTIFICATE is not defined.");

        return Base64.getDecoder().decode(base64);
    }

    static void assertConnectionEquals(KeyVaultEkmConnection expected, KeyVaultEkmConnection actual) {
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getPathPrefix(), actual.getPathPrefix());
    }
}
