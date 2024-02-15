// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class KeyClientTestBase extends TestProxyTestBase {
    private static final String KEY_NAME = "javaKeyTemp";
    private static final String AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS);

    private static final int MAX_RETRIES = 5;
    private static final RetryOptions LIVE_RETRY_OPTIONS = new RetryOptions(new ExponentialBackoffOptions()
        .setMaxRetries(MAX_RETRIES)
        .setBaseDelay(Duration.ofSeconds(2))
        .setMaxDelay(Duration.ofSeconds(16)));

    private static final ClientLogger LOGGER = new ClientLogger(KeyClientTestBase.class);

    public static final TestMode TEST_MODE = initializeTestMode();

    private static final RetryOptions PLAYBACK_RETRY_OPTIONS =
        new RetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;
    protected boolean runReleaseKeyTest = false;
    // TODO (vcolin7): Un-comment after the service rolls out a fix for the version issue (late Nov 2023).
    //protected boolean runReleaseKeyTest = getTestMode() == TestMode.PLAYBACK
    //    || Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ATTESTATION_URL") != null;

    private static TestMode initializeTestMode() {
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

    void beforeTestSetup() {
        System.getProperties().put("IS_SKIP_ROTATION_POLICY_TEST",
            String.valueOf(!".vault.azure.net".equals(
                Configuration.getGlobalConfiguration()
                    .get("KEY_VAULT_ENDPOINT_SUFFIX", ".vault.azure.net"))
                && interceptorManager.isLiveMode()));

        KeyVaultCredentialPolicy.clearCache();
    }

    KeyClientBuilder getKeyClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        KeyServiceVersion serviceVersion) {
        TokenCredential credential;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = testTenantId == null
                ? Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID")
                : testTenantId;

            credential = new ClientSecretCredentialBuilder()
                .clientSecret(Objects.requireNonNull(clientKey, "The client key cannot be null"))
                .clientId(Objects.requireNonNull(clientId, "The client id cannot be null"))
                .tenantId(Objects.requireNonNull(tenantId, "The tenant id cannot be null"))
                .additionallyAllowedTenants("*")
                .build();
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyClientBuilder builder = new KeyClientBuilder()
            .vaultUrl(endpoint)
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
        final Map<String, String> tags = Collections.singletonMap("foo", "baz");

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

    static void assertRestException(Runnable exceptionThrower,
        Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        HttpResponseException ex = assertThrows(expectedExceptionType, exceptionThrower::run);
        assertEquals(expectedStatusCode, ex.getResponse().getStatusCode());
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType,
                                    int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
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

    public static class AttestationToken implements JsonSerializable<AttestationToken> {
        String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("token", token)
                .writeEndObject();
        }

        public static AttestationToken fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                AttestationToken token = new AttestationToken();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("token".equals(fieldName)) {
                        token.token = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }

                return token;
            });
        }
    }

    public static String getAttestationToken(String attestationUrl) {
        HttpClient attestationClient = HttpClient.createDefault();

        return attestationClient.send(new HttpRequest(HttpMethod.GET, attestationUrl))
            .flatMap(HttpResponse::getBodyAsByteArray)
            .flatMap(bytes -> Mono.fromCallable(() -> {
                try (JsonReader jsonReader = JsonProviders.createReader(bytes)) {
                    return AttestationToken.fromJson(jsonReader).getToken();
                }
            }))
            .block();
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
