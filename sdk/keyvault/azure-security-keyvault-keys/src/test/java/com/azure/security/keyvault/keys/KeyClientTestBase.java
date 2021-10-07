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
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
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
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class KeyClientTestBase extends TestBase {
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
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION,  Configuration.getGlobalConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));

        if (credential != null) {
            policies.add(new KeyVaultCredentialPolicy(credential));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    @Test
    public abstract void setKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void setKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyOptions = new CreateKeyOptions(generateResourceId(KEY_NAME), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(keyOptions);
    }

    @Test
    public abstract void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void createRsaKeyRunner(Consumer<CreateRsaKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions(generateResourceId(KEY_NAME))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        if (runManagedHsmTest) {
            createRsaKeyOptions.setHardwareProtected(true);
        }

        testRunner.accept(createRsaKeyOptions);
    }

    @Test
    public abstract void setKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void setKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void setKeyEmptyValueRunner(Consumer<CreateKeyOptions> testRunner) {
        CreateKeyOptions key = new CreateKeyOptions(KEY_NAME, null);

        testRunner.accept(key);
    }

    @Test public abstract void setKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion);


    @Test
    public abstract void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void updateKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        final String keyName = generateResourceId("testKey1");
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
        final String keyName = generateResourceId("testKey2");
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
        final CreateKeyOptions originalKey = new CreateKeyOptions(generateResourceId("testKey4"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey);
    }

    @Test
    public abstract void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void getKeySpecificVersionRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final String keyName = generateResourceId("testKey3");
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions key = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        final CreateKeyOptions keyWithNewVal = new CreateKeyOptions(keyName, keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(key, keyWithNewVal);
    }

    @Test
    public abstract void getKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void deleteKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDelete = new CreateKeyOptions(generateResourceId("testKey5"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDelete);
    }

    @Test
    public abstract void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void getDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDeleteAndGet = new CreateKeyOptions(generateResourceId("testKey6"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void recoverDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToDeleteAndRecover = new CreateKeyOptions(generateResourceId("testKey7"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void backupKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToBackup = new CreateKeyOptions(generateResourceId("testKey8"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToBackup);
    }

    @Test
    public abstract void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void restoreKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        final CreateKeyOptions keyToBackupAndRestore = new CreateKeyOptions(generateResourceId("testKey9"), keyType)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToBackupAndRestore);
    }

    @Test
    public abstract void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;

        for (int i = 0; i < 2; i++) {
            keyName = generateResourceId("listKey" + i);
            CreateKeyOptions key =  new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

            keys.put(keyName, key);
        }

        testRunner.accept(keys);
    }

    @Test
    public abstract void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listKeyVersionsRunner(Consumer<List<CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        List<CreateKeyOptions> keys = new ArrayList<>();
        String keyName = generateResourceId("listKeyVersion");

        for (int i = 1; i < 5; i++) {
            keys.add(new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keys);
    }

    @Test
    public abstract void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void listDeletedKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        final KeyType keyType = isHsmEnabled ? KeyType.RSA_HSM : KeyType.RSA;
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;

        for (int i = 0; i < 3; i++) {
            keyName = generateResourceId("listDeletedKeysTest" + i);

            keys.put(keyName, new CreateKeyOptions(keyName, keyType)
                .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keys);
    }

    void createRsaKeyWithPublicExponentRunner(Consumer<CreateRsaKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateRsaKeyOptions keyOptions = new CreateRsaKeyOptions(testResourceNamer.randomName("testRsaKey", 20))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags)
            .setKeySize(2048)
            .setPublicExponent(3);

        if (runManagedHsmTest) {
            keyOptions.setHardwareProtected(true);
        }

        testRunner.accept(keyOptions);
    }

    void createOctKeyRunner(Consumer<CreateOctKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateOctKeyOptions keyOptions = new CreateOctKeyOptions(generateResourceId("testRsaKey"))
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        if (runManagedHsmTest) {
            keyOptions.setHardwareProtected(true);
        }

        testRunner.accept(keyOptions);
    }

    void getRandomBytesRunner(Consumer<Integer> testRunner) {
        int count = 12;

        testRunner.accept(count);
    }

    @Test
    public abstract void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void releaseKeyRunner(BiConsumer<CreateRsaKeyOptions, String> testRunner) {
        final String attestationUrl = Configuration.getGlobalConfiguration()
            .get("AZURE_KEYVAULT_ATTESTATION_URL", "http://localhost:8080");
        final String releasePolicyContents =
            "{"
                + "\"anyOf\": ["
                    + "{"
                        + "\"anyOf\": ["
                            + "{"
                                + "\"claim\": \"sdk-test\","
                                + "\"condition\": \"equals\","
                                + "\"value\": \"true\""
                            + "}"
                        + "],"
                        + "\"authority\": \"" + attestationUrl + "\""
                    + "}"
                + "],"
                + "\"version\": \"1.0\""
            + "}";

        final CreateRsaKeyOptions keyToRelease =
            new CreateRsaKeyOptions(testResourceNamer.randomName("keyToRelease", 20))
                .setKeySize(2048)
                .setHardwareProtected(runManagedHsmTest)
                .setReleasePolicy(new KeyReleasePolicy(releasePolicyContents.getBytes(StandardCharsets.UTF_8)))
                .setExportable(true);

        testRunner.accept(keyToRelease, attestationUrl);
    }

    @Test
    public abstract void createRsaKeyWithPublicExponent(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getKeyRotationPolicyOfNonExistentKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void getKeyRotationPolicyWithNoPolicySet(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    public abstract void updateGetKeyRotationPolicyWithMinimumProperties(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void updateGetKeyRotationPolicyWithMinimumPropertiesRunner(BiConsumer<String, KeyRotationPolicyProperties> testRunner) {
        String keyName = testResourceNamer.randomName("rotateKey", 20);

        KeyRotationPolicyProperties keyRotationPolicyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(Collections.emptyList());

        testRunner.accept(keyName, keyRotationPolicyProperties);
    }

    @Test
    public abstract void updateGetKeyRotationPolicyWithAllProperties(HttpClient httpClient, KeyServiceVersion serviceVersion);

    void updateGetKeyRotationPolicyWithAllPropertiesRunner(BiConsumer<String, KeyRotationPolicyProperties> testRunner) {
        String keyName = testResourceNamer.randomName("rotateKey", 20);

        List<KeyRotationLifetimeAction> keyRotationLifetimeActionList = new ArrayList<>();
        KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
            .setTimeAfterCreate("P7D")
            .setTimeBeforeExpiry("P7D");
        KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY)
            .setTimeAfterCreate("P7D")
            .setTimeBeforeExpiry("P7D");

        keyRotationLifetimeActionList.add(rotateLifetimeAction);
        keyRotationLifetimeActionList.add(notifyLifetimeAction);

        KeyRotationPolicyProperties keyRotationPolicyProperties = new KeyRotationPolicyProperties()
            .setLifetimeActions(keyRotationLifetimeActionList)
            .setExpiryTime("P6M");

        testRunner.accept(keyName, keyRotationPolicyProperties);
    }

    @Test
    public abstract void rotateKey(HttpClient httpClient, KeyServiceVersion serviceVersion);

    String generateResourceId(String suffix) {
        if (interceptorManager.isPlaybackMode()) {
            return suffix;
        }

        String id = UUID.randomUUID().toString();

        return suffix.length() > 0 ? id + "-" + suffix : id;
    }

    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of 200.
     *
     * @param expected Key expected to be returned by the service
     * @param response Response returned by the service, the body should contain a Key
     */
    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response) {
        assertKeyEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        assertKeyEquals(expected, response.getValue());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
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
            ? Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT", "http://localhost:8080")
            : Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "http://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
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
     * @param exception Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
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
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
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
     * @param serviceVersion ServiceVersion needs to check
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

        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
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
        assertEquals(expected.getExpiryTime(), actual.getExpiryTime());

        List<KeyRotationLifetimeAction> expectedLifetimeActions = expected.getLifetimeActions();
        List<KeyRotationLifetimeAction> actualLifetimeActions = actual.getLifetimeActions();

        assertTrue(expectedLifetimeActions == null && actualLifetimeActions == null
            || expectedLifetimeActions != null && actualLifetimeActions != null);

        if (expectedLifetimeActions != null) {
            assertEquals(expectedLifetimeActions.size(), actualLifetimeActions.size());

            for (int i = 0; i < expectedLifetimeActions.size(); i++) {
                KeyRotationLifetimeAction expectedLifetimeAction = expectedLifetimeActions.get(i);
                KeyRotationLifetimeAction actualLifetimeAction = actualLifetimeActions.get(i);

                assertEquals(expectedLifetimeAction.getType(), actualLifetimeAction.getType());
                assertEquals(expectedLifetimeAction.getTimeAfterCreate(), actualLifetimeAction.getTimeAfterCreate());
                assertEquals(expectedLifetimeAction.getTimeBeforeExpiry(), actualLifetimeAction.getTimeBeforeExpiry());
            }
        }
    }
}
