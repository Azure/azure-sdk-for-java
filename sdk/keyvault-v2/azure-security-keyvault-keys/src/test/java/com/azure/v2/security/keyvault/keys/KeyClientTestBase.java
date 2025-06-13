// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.credential.TokenCredential;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.FixedDelayOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.BodilessMatcher;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestRequestMatcher;
import com.azure.v2.core.test.utils.MockTokenCredential;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.CoreUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.KeyReleasePolicy;
import com.azure.v2.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class KeyClientTestBase extends TestBase {
    private static final String KEY_NAME = "javaKeyTemp";
    private static final String AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS);

    private static final int MAX_RETRIES = 5;
    private static final HttpRetryOptions LIVE_RETRY_OPTIONS
        = new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(MAX_RETRIES)
            .setBaseDelay(Duration.ofSeconds(2))
            .setMaxDelay(Duration.ofSeconds(16)));

    private static final ClientLogger LOGGER = new ClientLogger(KeyClientTestBase.class);

    private static final HttpRetryOptions PLAYBACK_RETRY_OPTIONS
        = new HttpRetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;
    protected boolean runReleaseKeyTest = getTestMode() == TestMode.PLAYBACK
        || Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ATTESTATION_URL") != null;

    void beforeTestSetup() {
        System.getProperties()
            .put("IS_SKIP_ROTATION_POLICY_TEST",
                String.valueOf(!".vault.azure.net"
                    .equals(Configuration.getGlobalConfiguration().get("KEY_VAULT_ENDPOINT_SUFFIX", ".vault.azure.net"))
                    && interceptorManager.isLiveMode()));

        KeyVaultCredentialPolicy.clearCache();
    }

    KeyClientBuilder getKeyClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        KeyServiceVersion serviceVersion) {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        } else {
            credential = new MockTokenCredential();

            List<TestRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyClientBuilder builder = new KeyClientBuilder().endpoint(endpoint)
            .serviceVersion(serviceVersion)
            .credential(credential)
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

    @Test
    public abstract void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToCreate = new CreateKeyOptions(testResourceNamer.randomName(KEY_NAME, 20), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(keyToCreate);
    }

    @Test
    public abstract void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createRsaKeyRunner(Consumer<CreateRsaKeyOptions> testRunner) {
        final Map<String, String> tags = Collections.singletonMap("foo", "baz");

        final CreateRsaKeyOptions keyToCreate = new CreateRsaKeyOptions(testResourceNamer.randomName(KEY_NAME, 20))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        if (runManagedHsmTest) {
            keyToCreate.setHardwareProtected(true);
        }

        testRunner.accept(keyToCreate);
    }

    @Test
    public abstract void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void purgeDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    public static Stream<Arguments> getTestParameters() {
        // Combine all possible HttpClient and service version combinations
        List<Arguments> argumentsList = new ArrayList<>();

        TestUtils.getHttpClients().forEach(httpClient ->
            Arrays.stream(KeyServiceVersion.values()).forEach(serviceVersion ->
                argumentsList.add(Arguments.of(httpClient, serviceVersion))));

        return argumentsList.stream();
    }

    String getEndpoint() {
        final String customEndpointEnv = "AZURE_KEYVAULT_ENDPOINT";
        String customEndpoint = interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : System.getenv(customEndpointEnv);
        if (CoreUtils.isNullOrEmpty(customEndpoint)) {
            customEndpoint = "https://azure-sdk-tests.vault.azure.net/";
        }

        return customEndpoint;
    }

    static void sleepIfRunningAgainstService(long millis) {
        if (getTestMode() == TestMode.LIVE) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static TestMode getTestMode() {
        final String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        LOGGER.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
        return TestMode.PLAYBACK;
    }
}
