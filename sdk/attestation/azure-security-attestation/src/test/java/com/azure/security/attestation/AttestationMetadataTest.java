// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Attestation Metadata Configuration APIs.
 */
public class AttestationMetadataTest extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetMetadataConfiguration(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        Object metadataConfig1 = attestationBuilder.buildAttestationClient().getOpenIdMetadata();
        verifyMetadataConfigurationResponse(clientUri, metadataConfig1);

        Response<Object> metadataConfig2 = attestationBuilder.buildAttestationClient().getOpenIdMetadataWithResponse(null);
        verifyMetadataConfigurationResponse(clientUri, metadataConfig2.getValue());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testGetMetadataConfigurationAsync(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        StepVerifier.create(attestationBuilder.buildAttestationAsyncClient().getOpenIdMetadata())
            .assertNext(metadataConfigResponse -> verifyMetadataConfigurationResponse(clientUri, metadataConfigResponse))
            .expectComplete()
            .verify();

        StepVerifier.create(attestationBuilder.buildAttestationAsyncClient().getOpenIdMetadataWithResponse(null))
            .assertNext(metadataConfigResponse -> verifyMetadataConfigurationResponse(clientUri, metadataConfigResponse.getValue()))
            .expectComplete()
            .verify();

    }

    /**
     * Verifies the response to the GetMetadataConfiguration (/.well-known/open-id-metadata) API.
     * @param clientUri - URI associated with the operation.
     * @param metadataConfigResponse - Object representing the metadata configuration.
     */
    void verifyMetadataConfigurationResponse(String clientUri, Object metadataConfigResponse) {
        assertTrue(metadataConfigResponse instanceof LinkedHashMap);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> metadataConfig = (LinkedHashMap<String, Object>) metadataConfigResponse;

        assertTrue(metadataConfig.containsKey("issuer"));
        assertTrue(metadataConfig.containsKey("jwks_uri"));
        // In playback mode, thee clientUri is a dummy value which cannot be associated with the actual response :(.
        if (!testContextManager.getTestMode().equals(TestMode.PLAYBACK)) {
            assertEquals(clientUri, metadataConfig.get("issuer"));
            assertEquals(clientUri + "/certs", metadataConfig.get("jwks_uri"));
        }
    }


}

