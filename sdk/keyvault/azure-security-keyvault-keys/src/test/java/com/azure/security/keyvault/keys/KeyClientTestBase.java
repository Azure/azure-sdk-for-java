// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public abstract class KeyClientTestBase extends TestBase {

    private static final String KEY_NAME = "javaKeyTemp";
    private static final KeyType RSA_KEY_TYPE = KeyType.RSA;
    private static final KeyType EC_KEY_TYPE = KeyType.EC;

    KeyClientBuilder clientBuilder;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
        TestMode testMode = getTestMode();

        String endpoint;
        TokenCredential credential;

        if (testMode == TestMode.PLAYBACK) {
            endpoint = "http://localhost:8080";
            credential = resource ->
                Mono.just(new AccessToken("Some fake token", OffsetDateTime.now(ZoneOffset.UTC)
                    .plus(Duration.ofMinutes(30))));
        } else {
            endpoint = System.getenv("AZURE_KEYVAULT_ENDPOINT");
            credential = new DefaultAzureCredentialBuilder().build();
        }

        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(credential);

        clientBuilder = new KeyClientBuilder()
            .vaultUrl(endpoint)
            .credential(credential);

        if (testMode == TestMode.PLAYBACK) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build());
        } else if (testMode == TestMode.RECORD) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        }
    }

    @Test
    public abstract void setKey();

    void setKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final CreateKeyOptions keyOptions = new CreateKeyOptions(KEY_NAME, RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(keyOptions);
    }

    @Test
    public abstract void setKeyEmptyName();

    @Test
    public abstract void setKeyNullType();

    void setKeyEmptyValueRunner(Consumer<CreateKeyOptions> testRunner) {
        CreateKeyOptions key = new CreateKeyOptions(KEY_NAME, null);
        testRunner.accept(key);
    }

    @Test
    public abstract void setKeyNull();


    @Test
    public abstract void updateKey();

    void updateKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final CreateKeyOptions originalKey = new CreateKeyOptions("testKey1", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setTags(tags);

        final CreateKeyOptions updatedKey = new CreateKeyOptions("testKey1", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setTags(tags);

        testRunner.accept(originalKey, updatedKey);
    }


    @Test
    public abstract void updateDisabledKey();

    void updateDisabledKeyRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();

        final CreateKeyOptions originalKey = new CreateKeyOptions("testKey2", EC_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
            .setEnabled(false);

        final CreateKeyOptions updatedKey = new CreateKeyOptions("testKey2", EC_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey, updatedKey);
    }

    @Test
    public abstract void getKey();

    void getKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions originalKey = new CreateKeyOptions("testKey4", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalKey);
    }

    @Test
    public abstract void getKeySpecificVersion();

    void getKeySpecificVersionRunner(BiConsumer<CreateKeyOptions, CreateKeyOptions> testRunner) {
        final CreateKeyOptions key = new CreateKeyOptions("testKey3", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        final CreateKeyOptions keyWithNewVal = new CreateKeyOptions("testKey3", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(key, keyWithNewVal);
    }

    @Test
    public abstract void getKeyNotFound();

    @Test
    public abstract void deleteKey();

    void deleteKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDelete = new CreateKeyOptions("testKey5", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(keyToDelete);
    }

    @Test
    public abstract void deleteKeyNotFound();

    @Test
    public abstract void getDeletedKey();

    void getDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDeleteAndGet = new CreateKeyOptions("testKey6", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedKeyNotFound();

    @Test
    public abstract void recoverDeletedKey();

    void recoverDeletedKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToDeleteAndRecover = new CreateKeyOptions("testKey7", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedKeyNotFound();

    @Test
    public abstract void backupKey();

    void backupKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToBackup = new CreateKeyOptions("testKey8", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToBackup);
    }

    @Test
    public abstract void backupKeyNotFound();

    @Test
    public abstract void restoreKey();

    void restoreKeyRunner(Consumer<CreateKeyOptions> testRunner) {
        final CreateKeyOptions keyToBackupAndRestore = new CreateKeyOptions("testKey9", RSA_KEY_TYPE)
            .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(keyToBackupAndRestore);
    }

    @Test
    public abstract void restoreKeyFromMalformedBackup();

    @Test
    public abstract void listKeys();

    void listKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;
        for (int i = 0; i < 30; i++) {
            keyName = "listKey" + i;
            CreateKeyOptions key = new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
            keys.put(keyName, key);
        }
        testRunner.accept(keys);
    }

    @Test
    public abstract void listKeyVersions();

    void listKeyVersionsRunner(Consumer<List<CreateKeyOptions>> testRunner) {
        List<CreateKeyOptions> keys = new ArrayList<>();
        String keyName;
        for (int i = 1; i < 5; i++) {
            keyName = "listKeyVersion";
            keys.add(new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
        }

        testRunner.accept(keys);
    }

    @Test
    public abstract void listDeletedKeys();

    void listDeletedKeysRunner(Consumer<HashMap<String, CreateKeyOptions>> testRunner) {
        HashMap<String, CreateKeyOptions> keys = new HashMap<>();
        String keyName;
        for (int i = 0; i < 3; i++) {
            keyName = "listDeletedKeysTest" + i;
            keys.put(keyName, new CreateKeyOptions(keyName, RSA_KEY_TYPE)
                .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        }
        testRunner.accept(keys);
    }

    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of
     * 200.
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
}
