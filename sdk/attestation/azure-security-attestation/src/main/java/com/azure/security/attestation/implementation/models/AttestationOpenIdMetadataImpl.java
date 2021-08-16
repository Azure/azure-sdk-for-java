// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.implementation.models;

import com.azure.security.attestation.models.AttestationOpenIdMetadata;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class AttestationOpenIdMetadataImpl implements AttestationOpenIdMetadata {
    private String jwksUri;
    private String issuer;
    private String[] responseTypesSupported;
    private String[] tokenSigningAlgorithmsSupported;
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
        if (!(generated instanceof LinkedHashMap)) {
            throw new InvalidParameterException("generated OpenId metadata must be a hash map.");
        }

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> metadataConfig = (LinkedHashMap<String, Object>) generated;
        AttestationOpenIdMetadataImpl returnValue = new AttestationOpenIdMetadataImpl();

        if (metadataConfig.containsKey("issuer")) {
            returnValue.issuer = (String) metadataConfig.get("issuer");
        }

        if (metadataConfig.containsKey("jwks_uri")) {
            returnValue.jwksUri = (String) metadataConfig.get("jwks_uri");
        }

        if (metadataConfig.containsKey("response_types_supported")) {
            Object responseTypesObject = metadataConfig.get("response_types_supported");
            if (!(responseTypesObject instanceof ArrayList)) {
                throw new InvalidParameterException("generated ResponseTypes must be an array list.");
            }
            @SuppressWarnings("unchecked")
            ArrayList<Object> supportedTypes = (ArrayList<Object>) responseTypesObject;
            returnValue.responseTypesSupported = supportedTypes.stream().map(type -> (String) type).toArray(String[]::new);
        }

        if (metadataConfig.containsKey("id_token_signing_alg_values_supported")) {
            Object signingAlgObject = metadataConfig.get("id_token_signing_alg_values_supported");
            if (!(signingAlgObject instanceof ArrayList)) {
                throw new InvalidParameterException("generated Signing Algorithms Supported must be an array list.");
            }
            @SuppressWarnings("unchecked")
            ArrayList<Object> signingAlgorithms = (ArrayList<Object>) signingAlgObject;
            returnValue.tokenSigningAlgorithmsSupported = signingAlgorithms.stream().map(type -> (String) type).toArray(String[]::new);
        }

        if (metadataConfig.containsKey("claims_supported")) {
            Object supportedClaimsObject = metadataConfig.get("claims_supported");
            if (!(supportedClaimsObject instanceof ArrayList)) {
                throw new InvalidParameterException("generated Supported Claims must be an array list.");
            }
            @SuppressWarnings("unchecked")
            ArrayList<Object> supportedClaims = (ArrayList<Object>) supportedClaimsObject;
            returnValue.supportedClaims = supportedClaims.stream().map(type -> (String) type).toArray(String[]::new);
        }

        return returnValue;
    }
}
