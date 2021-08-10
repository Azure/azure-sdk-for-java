// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationToken;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSSignerOption;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.opts.AllowWeakRSAKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An AttestationToken represents a Json Web Token/Json Web Signature object returned from or sent to
 * the Attestation Service
 */
@Fluent
@Immutable
public class AttestationTokenImpl implements AttestationToken {

    /**
     * Creates a new instance of an AttestationToken object.
     * @param serializedToken - Serialized JSON Web Token/JSON Web Signature object.
     */
    public AttestationTokenImpl(String serializedToken) {
        logger = new ClientLogger(AttestationTokenImpl.class);

        this.rawToken = serializedToken;
        JOSEObject tokenAsJose;
        try {
            tokenAsJose = JOSEObject.parse(serializedToken);
        } catch (ParseException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }
        header = tokenAsJose.getHeader();
        // If this is not an unsecured token, grab the JWS headers.
        if (!header.getAlgorithm().getName().equals("none")) {
            jwsHeader = (JWSHeader) header;
        } else {
            jwsHeader = null;
        }
        payload = tokenAsJose.getPayload();
    }

    private final ClientLogger logger;
    private final String rawToken;

    private final Header header;
    private final JWSHeader jwsHeader;
    private final Payload payload;

    /**
     * Retrieves the body of an attestation token.
     * @param returnType The "Type" of the body of the token.
     * @param <T> The Type of the body of the token.
     * @return Returns the deserialized body of hte token.
     */
    @Override public <T> T getBody(Class<T> returnType) {
        // If the payload looks to be empty, return null.
        if (payload.toString().length() == 0) {
            return null;
        } else {
            SerializerAdapter serializerAdapter = new JacksonAdapter();
            T t;

            try {
                t = serializerAdapter.deserialize(payload.toString(), returnType, SerializerEncoding.JSON);
            } catch (IOException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
            return t;
        }
    }

    /**
     * Serializes the attestation token as a string.
     * @return Returns the serialized attestation token.
     */
    @Override public String serialize() {
        return rawToken;
    }

    /**
     * Returns the "algorithm" token header property.
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.1'>RFC 7515 section 4.1.1</a>
     *
     * @return The value of the "alg" header parameter.
     */
    @Override public String getAlgorithm() {
        return header.getAlgorithm().getName();
    }

    /**
     * Returns the "Key ID" token header property.
     * See <a href='https://datatracker.ietf.org/doc/html/rfc7515#section-4.1.4'>RFC 7515 section 4.1.4</a>
     * @return The value of the "kid" header parameter.
     */
    @Override public String getKeyId() {
        return jwsHeader != null ? jwsHeader.getKeyID() : null;
    }

    /**
     * Returns the signing certificate chain as an AttestationSigner.
     * @return an AttestationSigner encapsulating the certificate chain.
     */
    @Override public AttestationSigner getCertificateChain() {
        if (jwsHeader != null) {
            List<Base64> certChain = jwsHeader.getX509CertChain();
            return AttestationSignerImpl.fromCertificateChain(certChain);
        }
        return null;
    }

    /**
     * Returns a URI which can be used to retrieve a JSON Web Key which can verify the signature on
     * this token.
     * @return URI at which a JWK can be retrieved.
     */
    @Override public URI getJsonWebKeyUrl() {
        return jwsHeader != null ? jwsHeader.getJWKURL() : null;
    }

    /**
     * Returns the signer for this token if the caller provided a JSON Web Key.
     * @return Attestation signer representing the signer of the token.
     */
    @Override public AttestationSigner getJsonWebKey() {
        if (jwsHeader != null) {
            JWK jwk = jwsHeader.getJWK();
            if (jwk == null) {
                return null;
            }
            return AttestationSignerImpl.fromJWK(jwk);
        }
        return null;
    }

    /**
     * Returns the SHA-256 thumbprint of the leaf certificate in the getCertificateChain.
     * @return the SHA-256 thumbprint of the leaf certificate returned by getCertificateChain.
     */
    @Override public byte[] getSha256Thumbprint() {
        return jwsHeader != null ? jwsHeader.getX509CertSHA256Thumbprint().decode() : null;
    }

    /**
     * Returns the SHA-1 thumbprint of the leaf certificate in the getCertificateChain.
     * @return the SHA-1 thumbprint of the leaf certificate returned by getCertificateChain.
     */
    @Override public byte[] getThumbprint() {
        return jwsHeader != null ? jwsHeader.getX509CertThumbprint().decode() : null;
    }

    /**
     * Returns a URI which can be used to retrieve an X.509 certificate which can verify the signature
     * on this token.
     * @return URI at which an X.509 certificate can be retrieved.
     */
    @Override public URI getX509Url() {
        return jwsHeader != null ? jwsHeader.getX509CertURL() : null;
    }

    @Override
    public String[] getCritical() {
        return jwsHeader.getCriticalParams().toArray(new String[0]);
    }

    @Override
    public String getType() {
        return jwsHeader.getType().getType();
    }

    @Override
    public String getContentType() {
        return jwsHeader.getContentType();
    }

    @Override
    public String getIssuer() {
        JWTClaimsSet claimsSet;
        try {
            claimsSet = JWTClaimsSet.parse(payload.toJSONObject());
        } catch (ParseException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return claimsSet.getIssuer();
    }

    @Override
    public Date getIssuedAt() {
        JWTClaimsSet claimsSet;
        try {
            claimsSet = JWTClaimsSet.parse(payload.toJSONObject());
        } catch (ParseException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return claimsSet.getIssueTime();
    }

    @Override
    public Date getExpiresOn() {
        JWTClaimsSet claimsSet;
        try {
            claimsSet = JWTClaimsSet.parse(payload.toJSONObject());
        } catch (ParseException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return claimsSet.getExpirationTime();
    }

    @Override
    public Date getNotBefore() {
        JWTClaimsSet claimsSet;
        try {
            claimsSet = JWTClaimsSet.parse(payload.toJSONObject());
        } catch (ParseException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        return claimsSet.getNotBeforeTime();
    }

    /**
     * Create an unsecured attestation token with an empty body. Used for resetting attestation
     * policies.
     * @return Newly created unsecured attestation token with an empty body.
     */
    public static AttestationToken createUnsecuredToken() {
        // Create a AttestationToken using the well known unsecured JWT header.
        // See <a href='https://datatracker.ietf.org/doc/html/rfc7519#section-6.1' RFC 7519 section 6.1/>.
        return new AttestationTokenImpl("eyJhbGciOiJub25lIn0..");
    }
    /**
     * Create an unsecured attestation token from the specified string body.
     * @param stringBody Body of the attestation token.
     * @return Newly created unsecured attestation token based off the serialized body.
     */
    public static AttestationToken createUnsecuredToken(String stringBody) {
        Payload payload = new Payload(stringBody);

        PlainObject plainObject = new PlainObject(payload);
        return new AttestationTokenImpl(plainObject.serialize());
    }

    /**
     * Create a secured attestation token with an empty body. Used to reset attestation
     * policies on Isolated mode attestation instances.
     *
     * @param signingKey - Signing key used to sign the attestation token.
     * @return Newly created secured attestation token with an empty body.
     *
     * @throws RuntimeException exception that occurs at runtime.
     */
    public static AttestationToken createSecuredToken(AttestationSigningKey signingKey) {
        ClientLogger logger = new ClientLogger(AttestationTokenImpl.class);
        try {
            signingKey.verify();
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        // The NimbusDS Library Payload object must have a body, so we have to
        // manually sign the attestation token.
        List<Base64> certs = new ArrayList<>();
        try {
            certs.add(Base64.encode(signingKey.getCertificate().getEncoded()));
        } catch (CertificateEncodingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(certs)
            .build();

        // Create a signer from the provided signing key.
        JWSSigner signer = null;
        try {
            if (signingKey.getPrivateKey() instanceof  RSAPrivateKey) {
                // If the caller wants to allow weak keys, allow them.
                Set<JWSSignerOption> options = new HashSet<>();
                if (signingKey.getAllowWeakKey()) {
                    options.add(AllowWeakRSAKey.getInstance());
                }
                signer = new RSASSASigner(signingKey.getPrivateKey(), options);
            } else if (signingKey.getPrivateKey() instanceof  ECPrivateKey) {
                signer = new ECDSASigner((ECPrivateKey) signingKey.getPrivateKey());
            } else {
                throw new RuntimeException("Assertion failure: Cannot have signer that is not either RSA or EC");
            }
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        String signedBody = header.toBase64URL() + ".";
        Base64URL signature = null;
        try {
            signature = signer.sign(header, signedBody.getBytes(StandardCharsets.UTF_8));
        } catch (JOSEException e) {
            throw new RuntimeException(e.toString());
        }
        return new AttestationTokenImpl(signedBody + "." + signature.toString());

    }
    /**
     * Create a secured attestation token from the specified string body which is signed with the
     * specified signing key.
     * @param stringBody - JSON body of the token in string form.
     * @param signingKey - Private Key and Certificate to be used to sign the token.
     * @return Newly created secured attestation token based off the serialized body.
     */
    public static AttestationToken createSecuredToken(String stringBody, AttestationSigningKey signingKey) {
        ClientLogger logger = new ClientLogger(AttestationTokenImpl.class);
        try {
            signingKey.verify();
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        JWSObject securedObject;
        Payload payload = new Payload(stringBody);

        List<Base64> certs = new ArrayList<>();
        try {
            certs.add(Base64.encode(signingKey.getCertificate().getEncoded()));
        } catch (CertificateEncodingException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .x509CertChain(certs)
            .build();
        JWSSigner signer = null;
        try {
            if (signingKey.getPrivateKey() instanceof RSAPrivateKey) {
                // If the caller wants to allow weak keys, allow them.
                Set<JWSSignerOption> options = new HashSet<>();
                if (signingKey.getAllowWeakKey()) {
                    options.add(AllowWeakRSAKey.getInstance());
                }
                signer = new RSASSASigner(signingKey.getPrivateKey(), options);
            } else if (signingKey.getPrivateKey() instanceof ECPrivateKey) {
                ECPrivateKey privateKey = (ECPrivateKey) signingKey.getPrivateKey();
                signer = new ECDSASigner(privateKey);
            }
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }

        securedObject = new JWSObject(header, payload);
        try {
            securedObject.sign(signer);
        } catch (JOSEException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.toString()));
        }

        return new AttestationTokenImpl(securedObject.serialize());
    }
}
