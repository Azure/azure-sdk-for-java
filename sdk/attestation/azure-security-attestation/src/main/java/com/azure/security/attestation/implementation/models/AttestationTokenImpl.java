// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationToken;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSSignerOption;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.crypto.opts.AllowWeakRSAKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
    @Override public String getJsonWebKeyUrl() {
        return jwsHeader != null ? jwsHeader.getJWKURL().toString() : null;
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
    @Override public BinaryData getSha256Thumbprint() {
        return jwsHeader != null ? BinaryData.fromBytes(jwsHeader.getX509CertSHA256Thumbprint().decode()) : null;
    }

    /**
     * Returns the SHA-1 thumbprint of the leaf certificate in the getCertificateChain.
     * @return the SHA-1 thumbprint of the leaf certificate returned by getCertificateChain.
     */
    @Override public BinaryData getThumbprint() {
        return jwsHeader != null ? BinaryData.fromBytes(jwsHeader.getX509CertThumbprint().decode()) : null;
    }

    /**
     * Returns a URI which can be used to retrieve an X.509 certificate which can verify the signature
     * on this token.
     * @return URI at which an X.509 certificate can be retrieved.
     */
    @Override public String getX509Url() {
        return jwsHeader != null ? jwsHeader.getX509CertURL().toString() : null;
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

    final AtomicReference<String> issuer = new AtomicReference<>();
    @Override
    public String getIssuer() {
        if (issuer.get() == null) {
            JWTClaimsSet claimsSet;
            try {
                claimsSet = JWTClaimsSet.parse(payload.toJSONObject());
            } catch (ParseException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
            issuer.set(claimsSet.getIssuer());
        }
        return issuer.get();
    }

    final AtomicReference<LocalDateTime> issuedAt = new AtomicReference<>();
    @Override
    public LocalDateTime getIssuedAt() {
        if (issuedAt.get() == null) {
            Map<String, Object> claimSet = payload.toJSONObject();
            Object iatObject = claimSet.get("iat");
            if (!(iatObject instanceof Long)) {
                throw logger.logExceptionAsError(new RuntimeException(String.format("Invalid type for IssuedAt: %s", iatObject.getClass().getName())));
            }

            long iat = (long) iatObject;
            issuedAt.set(LocalDateTime.ofInstant(Instant.ofEpochSecond(iat), ZoneId.systemDefault()));
        }
        return issuedAt.get();
    }

    final AtomicReference<LocalDateTime> expiresOn = new AtomicReference<>();
    @Override
    public LocalDateTime getExpiresOn() {
        if (expiresOn.get() == null) {
            Map<String, Object> claimSet = payload.toJSONObject();
            Object expObject = claimSet.get("exp");
            if (!(expObject instanceof Long)) {
                throw logger.logExceptionAsError(new RuntimeException(String.format("Invalid type for ExpiresOn: %s", expiresOn.getClass().getName())));
            }

            long exp = (long) expObject;
            expiresOn.set(LocalDateTime.ofInstant(Instant.ofEpochSecond(exp), ZoneId.systemDefault()));
        }
        return expiresOn.get();
    }

    final AtomicReference<LocalDateTime> notBeforeTime = new AtomicReference<>();
    @Override
    public LocalDateTime getNotBefore() {
        if (notBeforeTime.get() == null) {
            Map<String, Object> claimSet = payload.toJSONObject();
            Object nbfObject = claimSet.get("nbf");
            if (!(nbfObject instanceof Long)) {
                throw logger.logExceptionAsError(new RuntimeException(String.format("Invalid type for NotBefore: %s", nbfObject.getClass().getName())));
            }

            long nbf = (long) nbfObject;
            notBeforeTime.set(LocalDateTime.ofInstant(Instant.ofEpochSecond(nbf), ZoneId.systemDefault()));
        }
        return notBeforeTime.get();
    }


    /**
     * Validate the attestation token.
     *
     * The validate method verifies the following elements in the attestation token are valid.
     *
     * <ul>
     *     <li>The token signature (if it is signed)</li>
     *     <li>The token expiration time (if it has an expiration time)</li>
     *     <li>The token 'not before' time (if it has a not before time)</li>
     *     <li>The issuer of the token</li>
     *     <li>Any customer provided validations.</li>
     * </ul>
     * @param signers - a list of potential signers for the attestation token.
     * @param options - Options providing finer granular control over the validation.
     */
    public void validate(List<AttestationSigner> signers, AttestationTokenValidationOptions options) {
        if (!options.getValidateToken()) {
            return;
        }

        // First thing we do is to cryptographically verify the signature of the token.
        AttestationSigner signer = validateTokenSignature(signers);

        validateTokenTimeProperties(options);
        validateTokenIssuer(options);

        // Finally, give the developer a chance to validate the token.
        if (options.getValidationCallback() != null) {
            options.getValidationCallback().accept(this, signer);
        }
    }

    /**
     * Validate the issuer for the token, if desired.
     * @param options - Options controlling the validation.
     */
    private void validateTokenIssuer(AttestationTokenValidationOptions options) {
        if (options.getExpectedIssuer() != null && this.getIssuer() != null) {
            if (!this.getIssuer().equals(options.getExpectedIssuer())) {
                throw logger.logExceptionAsError(new RuntimeException(String.format("Token Validation Failed due to mismatched issuer. Expected issuer %s, but found %s", options.getExpectedIssuer(), getIssuer())));
            }
        }
    }

    private void validateTokenTimeProperties(AttestationTokenValidationOptions options) {
        LocalDateTime timeNow = LocalDateTime.now();
        timeNow = timeNow.minusNanos(timeNow.getNano());

        if (this.getExpiresOn() != null && options.getValidateExpiresOn()) {
            final LocalDateTime expirationTime = this.getExpiresOn();
            if (timeNow.isAfter(expirationTime)) {
                final Duration timeDelta = Duration.between(timeNow, expirationTime);
                if (timeDelta.abs().compareTo(options.getValidationSlack()) > 0) {
                    throw logger.logExceptionAsError(
                        new RuntimeException(
                            String.format("Token Validation Failed due to expiration time. Current time: %s Expiration time: %s", timeNow.toString(), this.getExpiresOn().toString())));
                }
            }
        }

        if (this.getNotBefore() != null && options.getValidateNotBefore()) {
            final LocalDateTime notBefore = this.getNotBefore();
            if (timeNow.isBefore(notBefore)) {
                final Duration timeDelta = Duration.between(timeNow, notBefore);
                if (timeDelta.abs().compareTo(options.getValidationSlack()) > 0) {
                    throw logger.logExceptionAsError(new RuntimeException(String.format("Token Validation Failed due to NotBefore time. Current time: %s Token becomes valid at: %s", timeNow.toString(), this.getNotBefore().toString())));
                }
            }
        }
    }

    /**
     * Validates the signature and ensures that one of the signers signed the attestation token.
     * @param signers - candidate signers for the token.
     * @return the signer who signed this token, or null if the token is unsigned.
     *
     * @throws RuntimeException if there is a validation error.
     */
    private AttestationSigner validateTokenSignature(List<AttestationSigner> signers) {
        // Early out if we have an unsecured token.
        if (this.getAlgorithm().equals("none")) {
            return null;
        }

        AtomicReference<JWSObject> jwt = new AtomicReference<>();
        try {
            jwt.set(JWSObject.parse(rawToken));
        } catch (ParseException e) {
            logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
        AtomicReference<AttestationSigner> tokenSigner = new AtomicReference<>();
        List<AttestationSigner> candidateSigners = getCandidateSigners(signers);
        for (AttestationSigner signer : candidateSigners) {
            final PublicKey key = signer.getCertificates().get(0).getPublicKey();

            JWSVerifier verifier = null;
            if (key instanceof RSAPublicKey) {
                RSAPublicKey publicKey = (RSAPublicKey) key;
                verifier = new RSASSAVerifier(publicKey);
            } else if (key instanceof ECPublicKey) {
                ECPublicKey publicKey = (ECPublicKey) key;
                try {
                    verifier = new ECDSAVerifier(publicKey);
                } catch (JOSEException e) {
                    throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
                }
            }

            // Attempt to verify the token with the signer.
            try {
                if (jwt.get().verify(verifier)) {
                    tokenSigner.set(signer);
                    break;
                }
            } catch (JOSEException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
        }
        return tokenSigner.get();
    }

    /**
     * Get a list of possible signers for this attestation token. If the "signers" parameter
     * is supplied, pick from that list, otherwise consult the JWS header for possible signers.
     * @param signers - possible list of candidate signers.
     * @return A list of possible signers for this token.
     */
    private List<AttestationSigner> getCandidateSigners(List<AttestationSigner> signers) {
        List<AttestationSigner> candidates = new ArrayList<>();
        final String desiredKeyId = this.getKeyId();

        // If we have a Key ID and a list of signers, use the list of signers to find the key.
        if (desiredKeyId != null && signers != null) {
            signers.forEach(signer -> {
                if (desiredKeyId.equals(signer.getKeyId())) {
                    candidates.add(signer);
                }
            });
        }

        // If we didn't find a certificate in the previous step, just return the candidates provided
        // by the caller - we can't do better than just the whole list of possible signers.
        if (candidates.size() == 0) {
            // We didn't find a candidate, so if the caller provided a list of candidates, use that
            // as the possible signers.
            if (signers != null && signers.size() != 0) {
                signers.forEach(signer -> candidates.add(signer));
            } else {
                // The caller didn't provide a set of signers, maybe there's one in the token itself.
                if (this.getCertificateChain() != null) {
                    candidates.add(this.getCertificateChain());
                }
                if (this.getJsonWebKey() != null) {
                    candidates.add(this.getJsonWebKey());
                }
            }
        }
        return candidates;
    }


    static final String EMPTY_TOKEN = "eyJhbGciOiJub25lIn0..";

    /**
     * Create an unsecured attestation token with an empty body. Used for resetting attestation
     * policies.
     * @return Newly created unsecured attestation token with an empty body.
     */
    public static AttestationToken createUnsecuredToken() {
        // Create a AttestationToken using the well known unsecured JWT header.
        // See <a href='https://datatracker.ietf.org/doc/html/rfc7519#section-6.1' RFC 7519 section 6.1/>.
        return new AttestationTokenImpl(EMPTY_TOKEN);
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
        JWSSigner signer;
        try {
            if (signingKey.getPrivateKey() instanceof RSAPrivateKey) {
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
        Base64URL signature;
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
