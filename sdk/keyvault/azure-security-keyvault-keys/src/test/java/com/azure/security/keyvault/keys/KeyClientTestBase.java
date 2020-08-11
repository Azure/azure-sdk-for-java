// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
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
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class KeyClientTestBase extends TestBase {
    private static final String KEY_NAME = "javaKeyTemp";
    private static final KeyType RSA_KEY_TYPE = KeyType.RSA;
    private static final KeyType EC_KEY_TYPE = KeyType.EC;
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    private static final String AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS);

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("ARM_CLIENTID");
            String clientKey = System.getenv("ARM_CLIENTKEY");
            String tenantId = System.getenv("AZURE_TENANT_ID");
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
        policies.add(new UserAgentPolicy(SDK_NAME, SDK_VERSION,  Configuration.getGlobalConfiguration().clone(), serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, KeyAsyncClient.KEY_VAULT_SCOPE));
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
    public abstract void setKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void setKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateKeyOptions keyOptions = new CreateKeyOptions(generateResourceId(KEY_NAME), RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(keyOptions);
    }

    @Test
    public abstract void setKeyEmptyName(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void setKeyNullType(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void setKeyEmptyValueRunner(Consumer<CreateKeyOptions> testRunner) {
        CreateKeyOptions key = new CreateKeyOptions(KEY_NAME, null);
        testRunner.accept(key);
    }

    @Test public abstract void setKeyNull(HttpClient httpClient, KeyServiceVersion keyServiceVersion);


    @Test
    public abstract void updateKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void updateKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final String keyName = generateResourceId("testKey1");
        final CreateKeyOptions originalKey = new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags);

        final CreateKeyOptions updatedKey = new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags);

        testRunner.accept(originalKey, updatedKey);
    }


    @Test
    public abstract void updateDisabledKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void updateDisabledKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        final String keyName = generateResourceId("testKey2");
        final CreateKeyOptions originalKey = new CreateKeyOptions(keyName, EC_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false);

        final CreateKeyOptions updatedKey = new CreateKeyOptions(keyName, EC_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey, updatedKey);
    }

    @Test
    public abstract void getKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void getKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions originalKey = new CreateKeyOptions(generateResourceId("testKey4"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey);
    }

    @Test
    public abstract void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void getKeySpecificVersionRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final String keyName = generateResourceId("testKey3");
        final CreateKeyOptions key = new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        final CreateKeyOptions keyWithNewVal = new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(key, keyWithNewVal);
    }

    @Test
    public abstract void getKeyNotFound(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void deleteKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void deleteKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDelete = new CreateKeyOptions(generateResourceId("testKey5"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDelete);
    }

    @Test
    public abstract void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void getDeletedKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void getDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDeleteAndGet = new CreateKeyOptions(generateResourceId("testKey6"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void recoverDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDeleteAndRecover = new CreateKeyOptions(generateResourceId("testKey7"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void backupKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void backupKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToBackup = new CreateKeyOptions(generateResourceId("testKey8"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToBackup);
    }

    @Test
    public abstract void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void restoreKey(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void restoreKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToBackupAndRestore = new CreateKeyOptions(generateResourceId("testKey9"), RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToBackupAndRestore);
    }

    @Test
    public abstract void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    @Test
    public abstract void listKeys(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void listKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;
        for (int i = 0; i < 2; i++) {
            keyName = generateResourceId("listKey" + i);
            CreateKeyOptions key =  new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                    .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
            keys.put(keyName, key);
        }
        testRunner.accept(keys);
    }

    @Test
    public abstract void listKeyVersions(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void listKeyVersionsRunner(Consumer<List<CreateKeyOptions>> testRunner) {
        List<CreateKeyOptions> keys = new ArrayList<>();
        String keyName = generateResourceId("listKeyVersion");
        for (int i = 1; i < 5; i++) {
            keys.add(new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                    .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keys);
    }

    @Test
    public abstract void listDeletedKeys(HttpClient httpClient, KeyServiceVersion keyServiceVersion);

    void listDeletedKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;
        for (int i = 0; i < 3; i++) {
            keyName = generateResourceId("listDeletedKeysTest" + i);
            keys.put(keyName, new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                    .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        }
        testRunner.accept(keys);
    }

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
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode()
                ? "http://localhost:8080"
                : System.getenv("AZURE_KEYVAULT_ENDPOINT");
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
}
