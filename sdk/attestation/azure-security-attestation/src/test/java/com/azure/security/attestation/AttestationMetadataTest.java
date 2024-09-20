// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for Attestation Metadata Configuration APIs.
 */
public class AttestationMetadataTest extends AttestationClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetMetadataConfiguration(HttpClient client, String clientUri) {
        AttestationOpenIdMetadata metadataConfig1
            = getAttestationBuilder(client, clientUri).buildClient().getOpenIdMetadata();
        verifyMetadataConfigurationResponse(clientUri, metadataConfig1);

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void getOpenIdMetadataWithResponse(HttpClient client, String clientUri) {
        Response<AttestationOpenIdMetadata> metadataConfig
            = getAttestationBuilder(client, clientUri).buildClient().getOpenIdMetadataWithResponse(Context.NONE);
        verifyMetadataConfigurationResponse(clientUri, metadataConfig.getValue());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetMetadataConfigurationAsync(HttpClient client, String clientUri) {
        StepVerifier.create(getAttestationBuilder(client, clientUri).buildAsyncClient().getOpenIdMetadata())
            .assertNext(
                metadataConfigResponse -> verifyMetadataConfigurationResponse(clientUri, metadataConfigResponse))
            .expectComplete()
            .verify();

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void getOpenIdMetadataWithResponseAsync(HttpClient client, String clientUri) {
        StepVerifier.create(getAttestationBuilder(client, clientUri).buildAsyncClient().getOpenIdMetadataWithResponse())
            .assertNext(metadataConfigResponse -> verifyMetadataConfigurationResponse(clientUri,
                metadataConfigResponse.getValue()))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies the response to the GetMetadataConfiguration (/.well-known/open-id-metadata) API.
     *
     * @param clientUri - URI associated with the operation.
     * @param metadataConfigResponse - Object representing the metadata configuration.
     */
    void verifyMetadataConfigurationResponse(String clientUri, AttestationOpenIdMetadata metadataConfigResponse) {
        assertNotNull(metadataConfigResponse.getIssuer());
        assertNotNull(metadataConfigResponse.getJsonWebKeySetUrl());

        // In playback mode, thee clientUri is a dummy value which cannot be associated with the actual response :(.
        if (!testContextManager.getTestMode().equals(TestMode.PLAYBACK)) {
            assertEquals(clientUri, metadataConfigResponse.getIssuer());
            assertEquals(clientUri + "/certs", metadataConfigResponse.getJsonWebKeySetUrl());
        }
        assertNotNull(metadataConfigResponse.getTokenSigningAlgorithmsSupported());
        assertNotNull(metadataConfigResponse.getResponseTypesSupported());
        assertNotNull(metadataConfigResponse.getSupportedClaims());
    }
}
