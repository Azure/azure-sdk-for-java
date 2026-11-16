// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.validation.http.MtlsTestCertificates;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link ClientCertificateBinding}.
 */
public class ClientCertificateBindingTests {
    // The expected key IDs were computed outside Java, from each certificate's DER encoding, the same way a token's
    // x5t#S256 confirmation claim is.
    @ParameterizedTest
    @CsvSource({
        MtlsTestCertificates.CLIENT_A + ", lIgeYBYWa24x8HwYMtbmIeC2676b4g4DOYpMwEItebk",
        MtlsTestCertificates.CLIENT_B + ", YfIUbkCN5fkf8_ST6W37j9K8ACIUCjV-OaJaJXwShhY" })
    public void keyIdIsTheCertificateThumbprint(String keyStoreResource, String expectedKeyId) {
        assertEquals(expectedKeyId, MtlsTestCertificates.loadBinding(keyStoreResource).getKeyId());
    }
}
