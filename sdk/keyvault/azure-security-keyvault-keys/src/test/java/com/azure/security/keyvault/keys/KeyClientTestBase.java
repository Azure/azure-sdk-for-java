// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyReleasePolicy;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class KeyClientTestBase extends TestProxyTestBase {
    private static final String KEY_NAME = "javaKeyTemp";
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    private static final String AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS);
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
        System.getProperties().put("IS_SKIP_ROTATION_POLICY_TEST",
            String.valueOf(!".vault.azure.net".equals(
                Configuration.getGlobalConfiguration()
                    .get("KEY_VAULT_ENDPOINT_SUFFIX", ".vault.azure.net"))
                && interceptorManager.isLiveMode()));

        KeyVaultCredentialPolicy.clearCache();
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        return getHttpPipeline(httpClient, null);
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient, String testTenantId) {
        TokenCredential credential;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = testTenantId == null
                ? Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID")
                : testTenantId;

            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");

            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .additionallyAllowedTenants("*")
                .build();
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(
            new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));

        policies.add(new RetryPolicy(strategy));

        if (credential != null) {
            // If in playback mode, disable the challenge resource verification.
            policies.add(new KeyVaultCredentialPolicy(credential, interceptorManager.isPlaybackMode()));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    @Test
    public abstract void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToCreate =
            new CreateKeyOptions(testResourceNamer.randomName(KEY_NAME, 20), keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags);

        testRunner.accept(keyToCreate);
    }

    @Test
    public abstract void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createRsaKeyRunner(Consumer<CreateRsaKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateRsaKeyOptions keyToCreate =
            new CreateRsaKeyOptions(testResourceNamer.randomName(KEY_NAME, 20))
                .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags);

        if (runManagedHsmTest) {
            keyToCreate.setHardwareProtected(true);
        }

        testRunner.accept(keyToCreate);
    }

    @Test
    public abstract void createKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void createKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createKeyEmptyValueRunner(Consumer<CreateKeyOptions> testRunner) {
        CreateKeyOptions keyToCreate = new CreateKeyOptions(KEY_NAME, null);

        testRunner.accept(keyToCreate);
    }

    @Test
    public abstract void createKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void updateKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        final String keyName = testResourceNamer.randomName("testKey1", 20);
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions originalKey = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setTags(tags);
        final CreateKeyOptions updatedKey = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(originalKey, updatedKey);
    }


    @Test
    public abstract void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void updateDisabledKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final String keyName = testResourceNamer.randomName("testKey2", 20);
        final KeyType keyType = isHsmEnabled ? KeyType.EC_HSM : KeyType.EC;
        final CreateKeyOptions originalKey = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setEnabled(false);
        final CreateKeyOptions updatedKey = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey, updatedKey);
    }

    @Test
    public abstract void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void getKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToSetAndGet =
            new CreateKeyOptions(testResourceNamer.randomName("testKey4", 20), keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToSetAndGet);
    }

    @Test
    public abstract void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void getKeySpecificVersionRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final String keyName = testResourceNamer.randomName("testKey3", 20);
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyWithOriginalValue = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        final CreateKeyOptions keyWithNewValue = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyWithOriginalValue, keyWithNewValue);
    }

    @Test
    public abstract void getKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void deleteKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDelete = new CreateKeyOptions(testResourceNamer.randomName("testKey5", 20), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDelete);
    }

    @Test
    public abstract void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void getDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDeleteAndGet =
            new CreateKeyOptions(testResourceNamer.randomName("testKey6", 20), keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void recoverDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDeleteAndRecover =
            new CreateKeyOptions(testResourceNamer.randomName("testKey7", 20), keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void backupKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToBackup = new CreateKeyOptions(testResourceNamer.randomName("testKey8", 20), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToBackup);
    }

    @Test
    public abstract void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void restoreKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToBackupAndRestore =
            new CreateKeyOptions(testResourceNamer.randomName("testKey9", 20), keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToBackupAndRestore);
    }

    @Test
    public abstract void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        HashMap<String, CreateKeyOptions> keysToList = new HashMap<>();
        String keyName;

        for (int i = 0; i < 2; i++) {
            keyName = testResourceNamer.randomName("listKey" + i, 20);
            CreateKeyOptions key = new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

            keysToList.put(keyName, key);
        }

        testRunner.accept(keysToList);
    }

    @Test
    public abstract void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listKeyVersionsRunner(Consumer<List<CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        List<CreateKeyOptions> keysToList = new ArrayList<>();
        String keyName = testResourceNamer.randomName("listKeyVersion", 20);

        for (int i = 1; i < 5; i++) {
            keysToList.add(new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keysToList);
    }

    @Test
    public abstract void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listDeletedKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        HashMap<String, CreateKeyOptions> keysToList = new HashMap<>();
        String keyName;

        for (int i = 0; i < 3; i++) {
            keyName = testResourceNamer.randomName("listDeletedKeysTest" + i, 20);

            keysToList.put(keyName, new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keysToList);
    }

    void createRsaKeyWithPublicExponentRunner(Consumer<CreateRsaKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateRsaKeyOptions keyToCreate = new CreateRsaKeyOptions(testResourceNamer.randomName("testRsaKey", 20))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags)
            .setKeySize(2048)
            .setPublicExponent(3);

        if (runManagedHsmTest) {
            keyToCreate.setHardwareProtected(true);
        }

        testRunner.accept(keyToCreate);
    }

    void createOctKeyRunner(Integer keySize, Consumer<CreateOctKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateOctKeyOptions keyToCreate = new CreateOctKeyOptions(testResourceNamer.randomName("testOctKey", 20))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setKeySize(keySize)
            .setTags(tags);

        if (runManagedHsmTest) {
            keyToCreate.setHardwareProtected(true);
        }

        testRunner.accept(keyToCreate);
    }

    void getRandomBytesRunner(Consumer<Integer> testRunner) {
        int count = 12;

        testRunner.accept(count);
    }

    @Test
    public abstract void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void releaseKeyRunner(BiConsumer<CreateRsaKeyOptions, String> testRunner) {
        final String attestationUrl =
            Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ATTESTATION_URL", "https://localhost:8080");
        final String releasePolicyContents =
            "{"
                + "\"anyOf\": ["
                    + "{"
                        + "\"allOf\": ["
                            + "{"
                                + "\"claim\": \"sdk-test\","
                                + "\"equals\": \"true\""
                            + "}"
                        + "],"
                        + "\"authority\": \"" + attestationUrl + "\""
                    + "}"
                + "],"
                + "\"version\": \"1.0.0\""
            + "}";

        final CreateRsaKeyOptions keyToRelease =
            new CreateRsaKeyOptions(testResourceNamer.randomName("keyToRelease", 20))
                .setKeySize(2048)
                .setHardwareProtected(runManagedHsmTest)
                .setReleasePolicy(new KeyReleasePolicy(BinaryData.fromString(releasePolicyContents)))
                .setExportable(true);

        testRunner.accept(keyToRelease, attestationUrl);
    }

    @Test
    public abstract void getKeyRotationPolicyOfNonExistentKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getKeyRotationPolicyWithNoPolicySet(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void updateGetKeyRotationPolicyWithMinimumProperties(HttpClient httpClient,
                                                                         KeyServiceVersion serviceVersion);

    void updateGetKeyRotationPolicyWithMinimumPropertiesRunner(BiConsumer<String, KeyRotationPolicy> testRunner) {
        String keyName = testResourceNamer.randomName("rotateKey", 20);

        KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy()
            .setLifetimeActions(Collections.emptyList());

        testRunner.accept(keyName, keyRotationPolicy);
    }

    @Test
    public abstract void updateGetKeyRotationPolicyWithAllProperties(HttpClient httpClient,
                                                                     KeyServiceVersion serviceVersion);

    void updateGetKeyRotationPolicyWithAllPropertiesRunner(BiConsumer<String, KeyRotationPolicy> testRunner) {
        String keyName = testResourceNamer.randomName("rotateKey", 20);

        List<KeyRotationLifetimeAction> keyRotationLifetimeActionList = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P7D");
        KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeBeforeExpiry("P7D");

        keyRotationLifetimeActionList.add(rotateLifetimeAction);
        keyRotationLifetimeActionList.add(notifyLifetimeAction);

        KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy()
            .setLifetimeActions(keyRotationLifetimeActionList)
            .setExpiresIn("P6M");

        testRunner.accept(keyName, keyRotationPolicy);
    }

    @Test
    public abstract void rotateKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of
     * 200.
     *
     * @param expected Key expected to be returned by the service.
     * @param response Response returned by the service, the body should contain a Key.
     */
    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response) {
        assertKeyEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting.
     * @param expectedStatusCode Expected HTTP status code returned by the service.
     */
    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response,
                                final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        assertKeyEquals(expected, response.getValue());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting contained in the RestResponse body.
     */
    static void assertKeyEquals(CreateKeyOptions expected, KeyVaultKey actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getKeyType(), actual.getKey().getKeyType());
        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getTags(), actual.getProperties().getTags());
    }

    public String getEndpoint() {
        final String endpoint = isHsmEnabled
            ? Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT", "https://localhost:8080")
            : Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower,
                                    Class<? extends HttpResponseException> expectedExceptionType,
                                    int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a HttpRequestException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test.
     * @param expectedStatusCode Expected HTTP status code contained in the error response.
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType,
                                    int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception.
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    public void sleepInRecordMode(long millis) {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(KeyServiceVersion.values()).filter(KeyClientTestBase::shouldServiceVersionBeTested)
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });

        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link KeyServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check.
     *
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(KeyServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return KeyServiceVersion.getLatest().equals(serviceVersion);
        }

        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }

        String[] configuredServiceVersionList = SERVICE_VERSION_FROM_ENV.split(",");

        return Arrays.stream(configuredServiceVersionList)
            .anyMatch(configuredServiceVersion ->
                serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    protected static BigInteger toBigInteger(byte[] b) {
        if (b[0] < 0) {
            // RSA parameters are always positive numbers, so if the first byte
            // is negative, we need to add a leading zero
            // to make the entire BigInteger positive.
            byte[] temp = new byte[1 + b.length];
            System.arraycopy(b, 0, temp, 1, b.length);
            b = temp;
        }

        return new BigInteger(b);
    }

    public static class AttestationToken {
        @JsonProperty
        String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static String getAttestationToken(String attestationUrl) throws IOException {
        HttpClient attestationClient = HttpClient.createDefault();

        try (HttpResponse httpResponse = attestationClient
            .send(new HttpRequest(HttpMethod.GET, attestationUrl)).block()) {

            assertNotNull(httpResponse);

            AttestationToken attestationToken =
                SERIALIZER_ADAPTER.deserialize(httpResponse.getBodyAsByteArray().block(),
                    AttestationToken.class, SerializerEncoding.JSON);

            return attestationToken.getToken();
        }
    }

    protected void assertKeyVaultRotationPolicyEquals(KeyRotationPolicy expected, KeyRotationPolicy actual) {
        assertTrue(expected == null && actual == null || expected != null && actual != null);

        if (expected == null) {
            return;
        }

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCreatedOn(), actual.getCreatedOn());
        assertEquals(expected.getUpdatedOn(), actual.getUpdatedOn());
        assertEquals(expected.getExpiresIn(), actual.getExpiresIn());

        List<KeyRotationLifetimeAction> expectedLifetimeActions = expected.getLifetimeActions();
        List<KeyRotationLifetimeAction> actualLifetimeActions = actual.getLifetimeActions();

        assertTrue(expectedLifetimeActions == null && actualLifetimeActions == null
            || expectedLifetimeActions != null && actualLifetimeActions != null);

        if (expectedLifetimeActions != null) {
            assertEquals(expectedLifetimeActions.size(), actualLifetimeActions.size());

            for (int i = 0; i < expectedLifetimeActions.size(); i++) {
                KeyRotationLifetimeAction expectedLifetimeAction = expectedLifetimeActions.get(i);
                KeyRotationLifetimeAction actualLifetimeAction = actualLifetimeActions.get(i);

                assertEquals(expectedLifetimeAction.getAction(), actualLifetimeAction.getAction());
                assertEquals(expectedLifetimeAction.getTimeAfterCreate(), actualLifetimeAction.getTimeAfterCreate());
                assertEquals(expectedLifetimeAction.getTimeBeforeExpiry(), actualLifetimeAction.getTimeBeforeExpiry());
            }
        }
    }
}
