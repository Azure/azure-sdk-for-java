// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

/**
 * Tests for Attestation Metadata Configuration APIs.
 */
public class AttestationMetadataTest extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetMetadataConfiguration(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        Object metadataConfigResponse = attestationBuilder.buildMetadataConfigurationClient().get();

        verifyMetadataConfigurationResponse(clientUri, metadataConfigResponse);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testGetMetadataConfigurationAsync(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        StepVerifier.create(attestationBuilder.buildMetadataConfigurationAsyncClient().get())
            .assertNext(metadataConfigResponse -> {
                verifyMetadataConfigurationResponse(clientUri, metadataConfigResponse);
            })
            .verifyComplete();
    }

}

