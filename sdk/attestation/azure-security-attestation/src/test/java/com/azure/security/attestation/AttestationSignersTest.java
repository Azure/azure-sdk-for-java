// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.security.attestation.models.JsonWebKeySet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test for Attestation Signing Certificates APIs.
 */
public class AttestationSignersTest extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetSigningCertificates(HttpClient client, String clientUri) throws Exception {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();


        verifySigningCertificatesResponse(clientUri, certs);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetSigningCertificatesAsync(HttpClient client, String clientUri) throws Exception {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesAsyncClient().get().block();

        verifySigningCertificatesResponse(clientUri, certs);
    }
}

