// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.math.BigInteger;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyAsyncClientManagedHsmTest extends KeyAsyncClientTest {
    public KeyAsyncClientManagedHsmTest() {
        this.isManagedHsmTest = true;
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(isManagedHsmTest && getTestMode() != TestMode.PLAYBACK);

        super.beforeTest();
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
    public void createOctKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createOctKeyRunner((createOctKeyOptions) -> StepVerifier.create(client.createOctKey(createOctKeyOptions))
            .assertNext(octKey -> assertKeyEquals(createOctKeyOptions, octKey))
            .verifyComplete());
    }
}
