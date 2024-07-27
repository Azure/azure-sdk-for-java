// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.implementation.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.util.JSONObjectUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

public class AttestationOpenIdMetadataImpl implements AttestationOpenIdMetadata {
    @JsonProperty(value = "jwks_uri")
    private String jwksUri;

    @JsonProperty(value = "issuer")
    private String issuer;

    @JsonProperty(value = "response_types_supported")
    private String[] responseTypesSupported;
    @JsonProperty(value = "id_token_signing_alg_values_supported")
    private String[] tokenSigningAlgorithmsSupported;
    @JsonProperty(value = "claims_supported")
    private String[] supportedClaims;

    @Override public String getJsonWebKeySetUrl() {
        return jwksUri;
    }

    @Override public String getIssuer() {
        return issuer;
    }
    @Override public String[] getResponseTypesSupported() {
        return responseTypesSupported.clone();
    }

    @Override public String[] getTokenSigningAlgorithmsSupported() {
        return tokenSigningAlgorithmsSupported.clone();
    }
    @Override public String[] getSupportedClaims() {
        return supportedClaims.clone();
    }

    public static AttestationOpenIdMetadata fromGenerated(Object generated) {

        ClientLogger logger = new ClientLogger(AttestationOpenIdMetadataImpl.class);
        SerializerAdapter serializerAdapter = new JacksonAdapter();
        AttestationOpenIdMetadataImpl metadataImpl;

        try {
            @SuppressWarnings("unchecked")
            String generatedString = JSONObjectUtils.toJSONString((LinkedHashMap<String, Object>) generated);
            metadataImpl = serializerAdapter.deserialize(generatedString, AttestationOpenIdMetadataImpl.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return metadataImpl;
    }
}
