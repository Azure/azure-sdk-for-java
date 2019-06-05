package com.azure.keyvault.keys;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.keyvault.keys.models.Key;
import com.azure.keyvault.keys.models.KeyCreateOptions;
import com.azure.keyvault.keys.models.webkey.KeyType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public abstract class KeyClientTestBase extends TestBase {

    private static final String KEY_NAME = "javaKeyTemp";
    private static final KeyType RSA_KEY_TYPE = KeyType.RSA;
    private static final KeyType EC_KEY_TYPE = KeyType.EC;
    private static final String CRUD_SECRET_NAME = "crudKey";
    private static final String KEY_VALUE = "Chocolate is hidden in the toothpaste cabinet";
    private static final int MAX_SECRETS = 4;
    private static final int PAGELIST_MAX_SECRETS = 3;


    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
    }

    <T> T clientSetup(Function<TokenCredential, T> clientBuilder) {
        final String endpoint = interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : System.getenv("AZURE_KEYVAULT_ENDPOINT");

        final String tenantId = System.getenv("MICROSOFT_AD_TENANT_ID");
        final String clientId = System.getenv("ARM_CLIENT_ID");
        final String clientKey = System.getenv("ARM_CLIENT_KEY");

        Objects.requireNonNull(endpoint, "AZURE_KEYVAULT_ENDPOINT expected to be set.");
        Objects.requireNonNull(tenantId, "ARM_CLIENT_ID expected to be set.");
        Objects.requireNonNull(clientId, "ARM_CLIENT_KEY expected to be set.");
        Objects.requireNonNull(tenantId, "MICROSOFT_AD_TENANT_ID expected to be set.");


        TokenCredential credential = new TokenCredential() {
            @Override
            public Mono<String> getTokenAsync(String resource) {
                String authority = "https://login.microsoftonline.com/{tenantId}";
                String auth = authority.replace("{tenantId}", tenantId);
                KeyvaultCredentials creds =  new KeyvaultCredentials(auth,clientId,
                        clientKey, "https://vault.azure.net");
                return Mono.just(creds.authorizationHeaderValue("a"));
            }
        };

        T client;
            String authority = "https://login.microsoftonline.com/{tenantId}";
            String auth = authority.replace("{tenantId}", tenantId);
            client = clientBuilder.apply(credential);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void setKey();

    void setKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final KeyCreateOptions keyOptions = new KeyCreateOptions(KEY_NAME, RSA_KEY_TYPE)
            .expires(OffsetDateTime.of(2050,1,30,0,0,0,0, ZoneOffset.UTC))
            .notBefore(OffsetDateTime.of(2000,1,30,12,59, 59, 0, ZoneOffset.UTC))
            .tags(tags);

        testRunner.accept(keyOptions);
    }

    @Test
    public abstract void setKeyEmptyName();

    @Test
    public abstract void setKeyNullType();

    void setKeyEmptyValueRunner(Consumer<KeyCreateOptions> testRunner) {
        KeyCreateOptions key = new KeyCreateOptions(KEY_NAME, null);
        testRunner.accept(key);
    }

    @Test public abstract void setKeyNull();


    @Test
    public abstract void updateKey();

    void updateKeyRunner(BiConsumer<KeyCreateOptions, KeyCreateOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final KeyCreateOptions originalKey = new KeyCreateOptions("testKey1", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC))
                .tags(tags);

        final KeyCreateOptions updatedKey = new KeyCreateOptions("testKey1", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2060,5,25,0,0,0,0,ZoneOffset.UTC))
                .tags(tags);

        testRunner.accept(originalKey, updatedKey);
    }


    @Test
    public abstract void updateDisabledKey();

    void updateDisabledKeyRunner(BiConsumer<KeyCreateOptions, KeyCreateOptions> testRunner) {

        final Map<String, String> tags = new HashMap<>();

        final KeyCreateOptions originalKey = new KeyCreateOptions("testKey2", EC_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC))
                .enabled(false);

        final KeyCreateOptions updatedKey = new KeyCreateOptions("testKey2", EC_KEY_TYPE)
                .expires(OffsetDateTime.of(2060,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(originalKey, updatedKey);
    }

    @Test
    public abstract void getKey();

    void getKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions originalKey = new KeyCreateOptions("testKey4", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(originalKey);
    }

    @Test
    public abstract void getKeySpecificVersion();

    void getKeySpecificVersionRunner(BiConsumer<KeyCreateOptions, KeyCreateOptions> testRunner) {
        final KeyCreateOptions key = new KeyCreateOptions("testKey3", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        final KeyCreateOptions keyWithNewVal = new KeyCreateOptions("testKey3", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(key, keyWithNewVal);
    }

    @Test
    public abstract void getKeyNotFound();

    @Test
    public abstract void deleteKey();

    void deleteKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions keyToDelete = new KeyCreateOptions("testKey5", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(keyToDelete);
    }


    @Test
    public abstract void deleteKeyNotFound();

    @Test
    public abstract void getDeletedKey();

    void getDeletedKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions keyToDeleteAndGet = new KeyCreateOptions("testKey6", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedKeyNotFound();

    @Test
    public abstract void recoverDeletedKey();

    void recoverDeletedKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions keyToDeleteAndRecover = new KeyCreateOptions("testKey7", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(keyToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedKeyNotFound();

    @Test
    public abstract void backupKey();

    void backupKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions keyToBackup = new KeyCreateOptions("testKey8", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(keyToBackup);
    }

    @Test
    public abstract void backupKeyNotFound();

    @Test
    public abstract void restoreKey();

    void restoreKeyRunner(Consumer<KeyCreateOptions> testRunner) {
        final KeyCreateOptions keyToBackupAndRestore = new KeyCreateOptions("testKey9", RSA_KEY_TYPE)
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(keyToBackupAndRestore);
    }

    @Test
    public abstract void restoreKeyFromMalformedBackup();

    @Test
    public abstract void listKeys();

    void listKeysRunner(Consumer<HashMap<String, KeyCreateOptions>> testRunner) {
        HashMap<String, KeyCreateOptions> keys = new HashMap<>();
        String keyName;
        String keyVal;
        for(int i = 0; i < 10; i++){
            keyName = "listKey" + i;
            keys.put(keyName, new KeyCreateOptions(keyName, RSA_KEY_TYPE)
                    .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC)));

        }
        testRunner.accept(keys);
    }

//    @Test
//    public abstract void listDeletedKeys();
//
//    @Test
//    public abstract void listKeyVersions();
//
//    @Test
//    public abstract void listKeyVersionsNotFound();


    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of 200.
     *
     * @param expected Key expected to be returned by the service
     * @param response Response returned by the service, the body should contain a Key
     */
    static void assertKeyEquals(KeyCreateOptions expected, Response<Key> response) {
        assertKeyEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    static void assertKeyEquals(KeyCreateOptions expected, Response<Key> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.statusCode());

        assertKeyEquals(expected, response.value());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
     */
    static void assertKeyEquals(KeyCreateOptions expected, Key actual) {
        if (expected != null && actual != null) {
           // actual = cleanResponse(expected, actual);
        }
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.keyType(), actual.keyMaterial().kty());
        assertEquals(expected.expires(), actual.expires());
        assertEquals(expected.notBefore(), actual.notBefore());
    }

    public String getEndpoint(){
        //String endpoint = System.getenv("AZURE_KEYVAULT_ENDPOINT");
        String endpoint = "https://cameravault.vault.azure.net";
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
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).response().statusCode());
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

    public void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
