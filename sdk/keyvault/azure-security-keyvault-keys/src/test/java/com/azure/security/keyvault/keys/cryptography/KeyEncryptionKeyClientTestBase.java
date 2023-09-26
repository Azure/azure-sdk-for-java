// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.implementation.CryptographyClientImpl;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class KeyEncryptionKeyClientTestBase extends TestProxyTestBase {
    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;

    private static final int MAX_RETRIES = 5;
    private static final RetryOptions LIVE_RETRY_OPTIONS = new RetryOptions(new ExponentialBackoffOptions()
        .setMaxRetries(MAX_RETRIES)
        .setBaseDelay(Duration.ofSeconds(2))
        .setMaxDelay(Duration.ofSeconds(16)));

    private static final RetryOptions PLAYBACK_RETRY_OPTIONS =
        new RetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    KeyEncryptionKeyClientBuilder getKeyEncryptionKeyClientBuilder(HttpClient httpClient,
        CryptographyServiceVersion serviceVersion) {

        KeyEncryptionKeyClientBuilder builder = new KeyEncryptionKeyClientBuilder()
            .serviceVersion(serviceVersion)
            .credential(getTokenCredentialAndSetMatchers())
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    KeyClientBuilder getKeyClientBuilder(HttpClient httpClient, String endpoint, KeyServiceVersion serviceVersion) {

        KeyClientBuilder builder = new KeyClientBuilder()
            .vaultUrl(endpoint)
            .serviceVersion(serviceVersion)
            .credential(getTokenCredentialAndSetMatchers())
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    CryptographyClientImpl getCryptographyClientImpl(HttpClient httpClient, String keyId,
        CryptographyServiceVersion serviceVersion) {
        CryptographyClientBuilder builder = new CryptographyClientBuilder()
            .keyIdentifier(keyId)
            .serviceVersion(serviceVersion)
            .credential(getTokenCredentialAndSetMatchers())
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            if (interceptorManager.isRecordMode()) {
                builder.addPolicy(interceptorManager.getRecordPolicy());
            }
        }

        return builder.buildClient().implClient;
    }

    private TokenCredential getTokenCredentialAndSetMatchers() {
        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID");

            return new ClientSecretCredentialBuilder()
                .clientSecret(Objects.requireNonNull(clientKey, "The client key cannot be null"))
                .clientId(Objects.requireNonNull(clientId, "The client id cannot be null"))
                .tenantId(Objects.requireNonNull(tenantId, "The tenant id cannot be null"))
                .additionallyAllowedTenants("*")
                .build();
        } else {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);

            return new MockTokenCredential();
        }
    }

    @Test
    public abstract void wrapUnwrapSymmetricAK128(HttpClient httpClient, CryptographyServiceVersion serviceVersion);

    @Test
    public abstract void wrapUnwrapSymmetricAK128Local();

    @Test
    public abstract void wrapUnwrapSymmetricAK192(HttpClient httpClient, CryptographyServiceVersion serviceVersion);

    @Test
    public abstract void wrapUnwrapSymmetricAK192Local();

    public String getEndpoint() {
        final String endpoint = runManagedHsmTest
            ? Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT", "https://hsmname.managedhsm.azure.net")
            : Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://vaultname.vault.azure.net");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }
}
