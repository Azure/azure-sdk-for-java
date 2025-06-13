// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.TestMode;
import com.azure.v2.security.keyvault.administration.implementation.KeyVaultCredentialPolicy;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class KeyVaultAdministrationClientTestBase extends TestBase {
    static final String CLIENT_NAME;
    static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-keyvault-administration.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }
    protected static final boolean IS_MANAGED_HSM_DEPLOYED
        = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
    static final String DISPLAY_NAME = "{displayName}";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        Assumptions.assumeTrue(IS_MANAGED_HSM_DEPLOYED || getTestMode() == TestMode.PLAYBACK);
        KeyVaultCredentialPolicy.clearCache();
    }

    @Override
    protected String getTestName() {
        return "";
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    /**
     * Returns a stream of arguments that includes all eligible {@link HttpClient HttpClients}.
     *
     * @return A stream of {@link HttpClient HTTP clients} to test.
     */
    static Stream<Arguments> createHttpClients() {
        return TestBase.getHttpClients().map(Arguments::of);
    }
}
