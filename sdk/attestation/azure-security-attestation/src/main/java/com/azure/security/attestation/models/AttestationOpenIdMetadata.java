// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents the <a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OpenId metadata discovery document</a>
 * for the attestation service instance.
 */
@Immutable
public interface AttestationOpenIdMetadata {

    /**
     * Returns the URL of the location which can be used to retrieve the keys which can be used to verify
     * the signature of tokens returned by the attestatiom service.
     *
     * Contains the "jwks_uri" metadata value from the OpenId Metadata.
     * See <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenId Provider Metadata</a>
     * for more information.
     * @return String containing the URL of the location which can be used to retrieve the signing keys.
     */
    String getJsonWebKeySetUrl();

    /**
     * Returns the expected issuer of tokens issued by the attestation service.
     *
     * Contains the "issuer" metadata value from the OpenId Metadata.
     *
     * See <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenId Provider Metadata</a>
     * for more information.
     * @return String containing the expected issuer for attestation tokens.
     */
    String getIssuer();


    /**
     * Returns the response types which are supported by the attestation service.
     *
     * Contains the "response_types_supported" metadata value from the OpenId Metadata.
     *
     * See <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenId Provider Metadata</a>
     * for more information.
     * @return String array containing the response types supported by the service.
     */
    String[] getResponseTypesSupported();

    /**
     * Returns the signing algorithms supported by the attestation service.
     *
     * Contains the "id_token_signing_alg_values_supported" metadata value from the OpenId Metadata.
     *
     * See <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenId Provider Metadata</a>
     * for more information.
     * @return String array containing the signing algorithms supported by the attestation service.
     */
    String[] getTokenSigningAlgorithmsSupported();

    /**
     * Returns the set of supported claims returned by the attestation service.
     *
     * Contains the "claims_supported" metadata value from the OpenId Metadata.
     *
     * See <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenId Provider Metadata</a>
     * for more information.
     * @return String array containing the supported claim names returned by the attestation service.
     */
    String[] getSupportedClaims();
}
