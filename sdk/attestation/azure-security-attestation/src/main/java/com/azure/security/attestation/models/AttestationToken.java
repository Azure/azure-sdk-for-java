// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.util.BinaryData;

import java.time.Instant;

/**
 * An AttestationToken represents an <a href='https://datatracker.ietf.org/doc/html/rfc7515'>RFC 7515 JSON Web Signature</a> or
 * <a href='https://datatracker.ietf.org/doc/html/rfc7519'>RFC 7519 JSON Web Token</a> as returned from the
 * attestation service.
 * <p>
 * It can be used to perform additional validations on the data sent from the attestation service beyond
 * the validations normally performed by the attestation SDK if desired.
 */
public interface AttestationToken {

    /**
     * Retrieves the body of an attestation token.
     *
     * @param returnType The "Type" of the body of the token.
     * @param <T> The type of the body of the token.
     * @return Returns the deserialized body of the token.
     */
    <T> T getBody(Class<T> returnType);

    /**
     * Serializes the attestation token as a string.
     *
     * @return Returns the serialized attestation token.
     */
    String serialize();

    /**
     * Returns the "algorithm" token header property.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.1'>RFC 7515 section 4.1.1</a>
     *
     * @return The value of the "alg" header parameter.
     */
    String getAlgorithm();

    /**
     * Returns the "Key ID" token header property.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.4'>RFC 7515 section 4.1.4</a>
     *
     * @return The value of the "kid" header parameter.
     */
    String getKeyId();

    /**
     * Returns the signing certificate chain as an AttestationSigner.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.6'>RFC 7515 section 4.1.6</a> for more information.
     *
     * @return an AttestationSigner encapsulating the certificate chain.
     */
    AttestationSigner getCertificateChain();

    /**
     * Returns a URI which can be used to retrieve a JSON Web Key which can verify the signature on
     * this token.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.5'>RFC 7515 section 4.1.5</a> for more information.
     *
     * @return URI at which a JWK can be retrieved.
     */
    String getJsonWebKeyUrl();

    /**
     * Returns the signer for this token if the caller provided a JSON Web Key.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.3'>RFC 7515 section 4.1.3</a> for more information.
     *
     * @return Attestation signer representing the signer of the token.
     */
    AttestationSigner getJsonWebKey();

    /**
     * Returns the SHA-256 thumbprint of the leaf certificate in the getCertificateChain.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.8'>RFC 7515 section 4.1.8</a> for more information.
     *
     * @return the SHA-256 thumbprint of the leaf certificate returned by getCertificateChain.
     */
    BinaryData getSha256Thumbprint();

    /**
     * Returns the SHA-1 thumbprint of the leaf certificate in the getCertificateChain.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.7'>RFC 7515 section 4.1.7</a> for more information.
     *
     * @return the SHA-1 thumbprint of the leaf certificate returned by getCertificateChain.
     */
    BinaryData getThumbprint();

    /**
     * Returns a URI which can be used to retrieve an X.509 certificate which can verify the signature
     * on this token.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.5'>RFC 7515 section 4.1.5</a> for more information.
     *
     * @return URI at which an X.509 certificate can be retrieved.
     */
    String getX509Url();

    /**
     * Returns the "crit" header property from the JSON Web Signature object.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.11'>RFC 7515 section 4.1.11</a> for more information.
     *
     * @return URI at which an X.509 certificate can be retrieved.
     */
    String[] getCritical();

    /**
     * Returns the "typ" header property from the JWS.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.9'>RFC 7515 section 4.1.9</a> for more information.
     *
     * @return URI at which an X.509 certificate can be retrieved.
     */
    String getType();

    /**
     * Returns the "cty" header property of the JWS.
     *
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.10'>RFC 7515 section 4.1.10</a> for more information.
     *
     * @return URI at which an X.509 certificate can be retrieved.
     */
    String getContentType();

    /**
     * Retrieve the issuer of the attestation token. The issuer corresponds to the "iss" claim
     * in a Json Web Token. See <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.1">RFC 7519 section 4.1.1</a>
     * for more information.
     *
     * The issuer will always be the same as the attestation service instance endpoint URL.
     *
     * @return the iss value.
     */
    String getIssuer();

    /**
     * Get the Issued At property: The time at which the token was issued. The IssuedAt property
     * corresponds to the "iat" claim in a Json Web Token. See <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.6">RFC 7519 section 4.1.6</a>
     * for more information.
     *
     * @return the IssuedAt value.
     */
    Instant getIssuedAt();

    /**
     * Get the ExpiresOn property: The expiration time after which the token is no longer valid. The ExpiresOn property
     * corresponds to the "exp" claim in a Json Web Token.  See <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4">RFC 7519 section 4.1.4</a>
     *
     * @return the expiration time for the token.
     */
    Instant getExpiresOn();

    /**
     * Get the NotBefore property: The time before which a token cannot be considered valid. The ExpiresOn property
     * corresponds to the "exp" claim in a Json Web Token.  See <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4">RFC 7519 section 4.1.4</a>
     *
     * @return the expiration time for the token.
     */
    /**
     * Get the nbf property: The not before time before which the token cannot be considered valid, in the number of
     * seconds since 1970-01-0T00:00:00Z UTC.
     *
     * @return the nbf value.
     */
    Instant getNotBefore();
}
