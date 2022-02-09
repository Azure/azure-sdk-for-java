// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.net.HttpURLConnection;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyAsyncClientManagedHsmTest extends KeyAsyncClientTest implements KeyClientManagedHsmTestBase {
    public KeyAsyncClientManagedHsmTest() {
        this.isHsmEnabled = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
        this.runManagedHsmTest = isHsmEnabled || getTestMode() == TestMode.PLAYBACK;
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(runManagedHsmTest);

        super.beforeTest();
    }

    /**
     * Tests that a RSA key created.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createRsaKeyRunner((expected) -> StepVerifier.create(client.createRsaKey(expected))
            .assertNext(response -> assertKeyEquals(expected, response))
            .verifyComplete());
    }

    /**
     * Tests that an RSA key with a public exponent can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKeyWithPublicExponent(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createRsaKeyWithPublicExponentRunner((createRsaKeyOptions) ->
            StepVerifier.create(client.createRsaKey(createRsaKeyOptions))
                .assertNext(rsaKey -> {
                    assertKeyEquals(createRsaKeyOptions, rsaKey);
                    assertEquals(BigInteger.valueOf(createRsaKeyOptions.getPublicExponent()),
                        toBigInteger(rsaKey.getKey().getE()));
                    assertEquals(createRsaKeyOptions.getKeySize(), rsaKey.getKey().getN().length * 8);
                })
                .verifyComplete());
    }

    /**
     * Tests that a symmetric key is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithDefaultSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createOctKeyRunner(null, (createOctKeyOptions) ->
            StepVerifier.create(client.createOctKey(createOctKeyOptions))
                .assertNext(octKey -> {
                    assertEquals(createOctKeyOptions.getName(), octKey.getName());
                    assertEquals(KeyType.OCT_HSM, octKey.getKey().getKeyType());
                    assertEquals(createOctKeyOptions.getExpiresOn(), octKey.getProperties().getExpiresOn());
                    assertEquals(createOctKeyOptions.getNotBefore(), octKey.getProperties().getNotBefore());
                    assertEquals(createOctKeyOptions.getTags(), octKey.getProperties().getTags());
                })
                .verifyComplete());
    }

    /**
     * Tests that a symmetric key of a valid size is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithValidSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createOctKeyRunner(256, (createOctKeyOptions) ->
            StepVerifier.create(client.createOctKey(createOctKeyOptions))
                .assertNext(octKey -> {
                    assertEquals(createOctKeyOptions.getName(), octKey.getName());
                    assertEquals(KeyType.OCT_HSM, octKey.getKey().getKeyType());
                    assertEquals(createOctKeyOptions.getExpiresOn(), octKey.getProperties().getExpiresOn());
                    assertEquals(createOctKeyOptions.getNotBefore(), octKey.getProperties().getNotBefore());
                    assertEquals(createOctKeyOptions.getTags(), octKey.getProperties().getTags());
                })
                .verifyComplete());
    }

    /**
     * Tests that a symmetric key of an invalid size cannot be created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithInvalidSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createOctKeyRunner(64, (createOctKeyOptions) ->
            StepVerifier.create(client.createOctKey(createOctKeyOptions))
                .verifyErrorSatisfies(ex ->
                    assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST)));
    }

    /**
     * Tests that random bytes can be retrieved from a Managed HSM.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getRandomBytes(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        getRandomBytesRunner((count) ->
            StepVerifier.create(client.getRandomBytes(count))
                .assertNext(randomBytes -> assertEquals(count, randomBytes.getBytes().length))
                .verifyComplete());
    }

    /**
     * Tests that an existing key can be released.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.releaseKey(httpClient, serviceVersion);
    }
}
