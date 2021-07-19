// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link AzureSasCredential}.
 */
public class AzureSasCredentialTests {
    @ParameterizedTest
    @MethodSource("invalidConstructorParametersSupplier")
    public void invalidConstructorParameters(String signature, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> new AzureSasCredential(signature));
        assertThrows(expectedException, () -> new AzureSasCredential(signature, String::toString));
    }

    private static Stream<Arguments> invalidConstructorParametersSupplier() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class)
        );
    }

    @Test
    public void baseConstructorReturnsSignatureAsIs() {
        final String signature = "sas=a bc";

        AzureSasCredential credential = new AzureSasCredential(signature);
        assertEquals(signature, credential.getSignature());
    }

    @Test
    public void constructorWithoutEncodingFunctionReturnsSignatureAsIs() {
        final String signature = "sas=a bc";

        AzureSasCredential credential = new AzureSasCredential(signature, null);
        assertEquals(signature, credential.getSignature());
    }

    @Test
    public void constructorEncodesSignature() {
        final String signature = "sas=a bc";
        final String expectedSignature = "sas=a%20bc";

        AzureSasCredential credential = new AzureSasCredential(signature, sig -> sig.replaceAll(" ", "%20"));
        assertEquals(expectedSignature, credential.getSignature());
    }

    @Test
    public void credentialWithoutEncodingFunctionDoesNotEncodeUpdates() {
        final String signature = "sas=a bc";
        final String updatedSignature = "sas=a b c";

        AzureSasCredential credential = new AzureSasCredential(signature).update(updatedSignature);
        assertEquals(updatedSignature, credential.getSignature());
    }

    @Test
    public void credentialWithEncodingFunctionEncodesSignatureUpdates() {
        final String signature = "sas=a bc";
        final String updatedSignature = "sas=a b c";
        final String expectedSignature = "sas=a%20b%20c";

        AzureSasCredential credential = new AzureSasCredential(signature, sig -> sig.replaceAll(" ", "%20"))
            .update(updatedSignature);
        assertEquals(expectedSignature, credential.getSignature());
    }
}
