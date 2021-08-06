// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//import io.github.cdimascio.dotenv.Dotenv;

/**
 * Specialization of the TestBase class for the attestation tests.
 *
 * Provides convenience methods for retrieving attestation client builders, verifying attestation tokens,
 * and accessing test environments.
 */
public class AttestationClientTestBase extends TestBase {

    private static final String DATAPLANE_SCOPE = "https://attest.azure.net/.default";

    final ClientLogger logger = new ClientLogger(AttestationClientTestBase.class);

    enum ClientTypes {
        SHARED,
        ISOLATED,
        AAD,
    }

    @BeforeAll
    public static void beforeAll() {
        TestBase.setupClass();
//        Dotenv.configure().ignoreIfMissing().systemProperties().load();
    }

    /**
     * Determine the Attestation instance type based on the client URI provided.
     * @param clientUri - URI for the attestation client.
     * @return the ClientTypes corresponding to the specified client URI.
     */
    ClientTypes classifyClient(String clientUri) {
        assertNotNull(clientUri);
        String regionShortName = getLocationShortName();
        String sharedUri = "https://shared" + regionShortName + "." + regionShortName + ".attest.azure.net";
        if (sharedUri.equals(clientUri)) {
            return ClientTypes.SHARED;
        } else if (getIsolatedUrl().equals(clientUri)) {
            return ClientTypes.ISOLATED;
        } else if (getAadUrl().equals(clientUri)) {
            return ClientTypes.AAD;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Convert a base64 encoded string into a byte stream.
     * @param base64 - Base64 encoded string to be decoded
     * @return stream of bytes encoded in the base64 encoded string.
     */
    InputStream base64ToStream(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(decoded);
    }

    /**
     * Retrieve an attestationClientBuilder for the specified HTTP client and client URI
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationClientBuilder getBuilder(HttpClient httpClient, String clientUri) {
        return new AttestationClientBuilder().pipeline(getHttpPipeline(httpClient)).endpoint(clientUri);
    }

    /**
     * Retrieves an HTTP pipeline configured on the specified HTTP pipeline.
     *
     * Used by getBuilder().
     * @param httpClient - Client on which to configure the HTTP pipeline.
     * @return an HttpPipeline object configured for the MAA service on the specified http client.
     */
    private HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, DATAPLANE_SCOPE));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }


    /**
     * Verifies an MAA attestation token and returns the set of attestation claims embedded in the token.
     * @param httpClient - the HTTP client which was used to retrieve the token (used to retrieve the signing certificates for the attestation instance)
     * @param clientUri - the base URI used to access the attestation instance (used to retrieve the signing certificates for the attestation instance)
     * @param attestationToken - Json Web Token issued by the Attestation Service.
     * @return a JWTClaimSet containing the claims associated with the attestation token.
     */
    Mono<JWTClaimsSet> verifyAttestationToken(HttpClient httpClient, String clientUri, String attestationToken) {
        final SignedJWT token;
        try {
            token = SignedJWT.parse(attestationToken);
        } catch (ParseException e) {
            return Mono.error(logger.logThrowableAsError(e));
        }

        SignedJWT finalToken = token;
        return getSigningCertificateByKeyId(token, httpClient, clientUri)
            .handle((signer, sink) -> {
                final PublicKey key = signer.getCertificates()[0].getPublicKey();
                final RSAPublicKey rsaKey = (RSAPublicKey) key;

                final RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
                try {
                    assertTrue(finalToken.verify(verifier));
                } catch (JOSEException e) {
                    sink.error(logger.logThrowableAsError(e));
                    return;
                }


                final JWTClaimsSet claims;
                try {
                    claims = finalToken.getJWTClaimsSet();
                } catch (ParseException e) {
                    sink.error(logger.logThrowableAsError(e));
                    return;
                }

                assertNotNull(claims);
                sink.next(claims);
            });
    }

    /**
     * Create a JWS Signer from the specified PKCS8 encoded signing key.
     * @param signingKeyBase64 Base64 encoded PKCS8 encoded RSA Private key.
     * @return JWSSigner created over the specified signing key.
     */
    JWSSigner getJwsSigner(String signingKeyBase64) {
        byte[] signingKey = Base64.getDecoder().decode(signingKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(signingKey);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw logger.logThrowableAsError(new RuntimeException(e));
        }

        PrivateKey privateKey;
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }

        return new RSASSASigner(privateKey);
    }

    /**
     * Find the signing certificate associated with the specified SignedJWT token.
     *
     * This method depends on the token
     * @param token - MAA generated token on which to find the certificate.
     * @param client - Http Client used to retrieve signing certificates.
     * @param clientUri - Base URI for the attestation client.
     * @return X509Certificate which will have been used to sign the token.
     */
    Mono<AttestationSigner> getSigningCertificateByKeyId(SignedJWT token, HttpClient client, String clientUri) {
        AttestationClientBuilder builder = getBuilder(client, clientUri);
        return builder.buildAttestationAsyncClient().getAttestationSigners()
            .handle((signers, sink) -> {
                final String keyId = token.getHeader().getKeyID();
                boolean foundKey = false;

                for (AttestationSigner signer : signers) {
                    if (keyId.equals(signer.getKeyId())) {
                        foundKey = true;
                        sink.next(signer);
                    }
                }
                if (!foundKey) {
                    sink.error(logger.logThrowableAsError(new RuntimeException(String.format(
                        "Key %s not found in JSON Web Key Set", keyId))));
                }
            });
    }

    /**
     * Retrieve the signing certificate used for the isolated attestation instance.
     * @return Returns a base64 encoded X.509 certificate used to sign policy documents.
     */
    String getIsolatedSigningCertificate() {
        String signingCertificate = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
        if (signingCertificate == null) {
            // Use a pre-canned signing certificate captured at provisioning time.
            signingCertificate = "MIIC+DCCAeCgAwIBAgIITwYg6gewUZswDQYJKoZIhvcNAQELBQAwMzExMC8GA1UEAxMoQXR0ZXN0YXRpb25Jc"
                + "29sYXRlZE1hbmFnZW1lbnRDZXJ0aWZpY2F0ZTAeFw0yMTAxMTkyMDEyNTZaFw0yMjAxMTkyMDEyNTZaMDMxMTAvBgNVBAMTK"
                + "EF0dGVzdGF0aW9uSXNvbGF0ZWRNYW5hZ2VtZW50Q2VydGlmaWNhdGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBA"
                + "QDZlz+tRMz5knbLWYY+CJmgzJ4WIoKAkVs6fwm2JZt3ig8NKWDR9XC0Byixj4cCNOanvqSy2eLLhm30jNdc0o3ObLJVro+4W"
                + "sI2p19DuV5PrpyCiZHDPb5DmxtMnsXpYV1ePIxveLgNcTe4lu/pRGxaCcDxSWLG1DL4BsMXzLE2GQaCVLzPHI0NJVvd/DDXz"
                + "bHK7tX45F8kRaXhnSd3fOaS4spw57r9oZfL1fzM03DVptnEmBrpsxP8Kw7aLv5ZYLhX/rK9H7MrM4NA6g/g3dw4w/rf8025h"
                + "JaAUJ+T68oARiXXBqDWCIkPXhkmukcmmP6Sl8mnNAqRG55iRY4AqzLRAgMBAAGjEDAOMAwGA1UdEwQFMAMBAf8wDQYJKoZIh"
                + "vcNAQELBQADggEBAJzbrs1pGiT6wwApfqT8jAM5OD9ylh8U9MCJOnMbigFAdp96N+TX568NUGPIssFB2oNNqI/Ai2hovPhdC"
                + "gDuPY2ngj2t9qyBhpqnQ0JWJ/Hpl4fZfbma9O9V18z9nLDmbOvbDNm11n1txZlwd+/h8Fh4CpXePhTWK2LIMYZ6WNBRRsanl"
                + "kF83yGFWMCShNqUiMGd9sWkRaaeJY9KtXxecQB3a/+SHKV2OESfA7inT3MXpwzCWAogrOk4GxzyWNPpsU7gHgErsiw+lKF8B"
                + "KrCArm0UjKvqhKeDni2zhWTYSQS2NLWnQwNvkxVdgdCl1lqtPeJ/qYPR8ZA+ksm36c7hBQ=";
        }
        return signingCertificate;
    }

    /**
     * Retrieve the signing key used for the isolated attestation instance.
     * @return Returns a base64 encoded RSA Key used to sign policy documents.
     */
    String getIsolatedSigningKey() {
        String signingKey = Configuration.getGlobalConfiguration().get("isolatedSigningKey");
        if (signingKey == null) {
            // Use a pre-canned signing key captured at provisioning time.
            signingKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDZlz+tRMz5knbLWYY+CJmgzJ4WIoKAkVs6fwm2"
                + "JZt3ig8NKWDR9XC0Byixj4cCNOanvqSy2eLLhm30jNdc0o3ObLJVro+4WsI2p19DuV5PrpyCiZHDPb5DmxtMnsXpYV1ePIx"
                + "veLgNcTe4lu/pRGxaCcDxSWLG1DL4BsMXzLE2GQaCVLzPHI0NJVvd/DDXzbHK7tX45F8kRaXhnSd3fOaS4spw57r9oZfL1f"
                + "zM03DVptnEmBrpsxP8Kw7aLv5ZYLhX/rK9H7MrM4NA6g/g3dw4w/rf8025hJaAUJ+T68oARiXXBqDWCIkPXhkmukcmmP6Sl"
                + "8mnNAqRG55iRY4AqzLRAgMBAAECggEAU0dTJMLPXLnU47Fo8rch7WxDGR+uKPz5GKNkmSU9onvhlN0AZHt23kBbL9JKDusm"
                + "WI9bw+QmrFTQIqgBCVLA2X+6pZaBBUMfUAGxMV9yHDctSbzTYBFyj7d+tE2UW+Va8eVkrolakDKD7A9A1VvNyIwxH2hB+O1"
                + "gcJNN+f7q2FP4zpmJjEsMm9IL9sZ+6aiQSSsFQEih92yZEtHJ6Ohe8mdvSkmi3Ki0TSeqDfh4CksRnd6Bv/6oBAV48WaRa3"
                + "yQ7tnsBrhXrCRzXRbiCcJP+C/Eqe3gkXvWuzq+cgicX95qh05VPnf5Pa6w5N4wEgwmoorloYfDStYcthtKidUefQKBgQD3h"
                + "WXciacPcydjAfH+0WyoszqBup/B5OBw/ZNlv531EzongB8V7+3pCs1/gF4+H3qvIRkL7JWt4HVtZEBp4D3tpWQHoYpE6wxA"
                + "0oeGM/DXbCQttCpR3eHZXYa9hbuQZuFjkclXjDBIk/q+U178+GRiB7zZb7JGNCBwlpCkTh+WywKBgQDhC2GnDCAGjwjDHa5"
                + "Nf4qLWyISN34KoEF9hgAYIvNYzAwwp8J/xxxQ7j8hf5XJPnld1UprVrhrYL0aGSc0kNWri1pZx2PDge42XK9boRARvuuK5U"
                + "aV3VNk7xb7vHzjoNDJWzmLlEaVPLFQPHVWHobTMwQWbzKZmopTA+QuV68NUwKBgQCbMmU/9n9tTIKxrZKSd7VtwZM5rE5nQ"
                + "J8JubUl4xOjir637bmQA7RknoVjIJX21b4S+Om/dEQVlduLD4Tj3dp2m3Ew57TOqaIxMtAO8ZpdOE0m6wRt+HWX2PCW/Lcy"
                + "P4+q4sofvqK3nzFlDNlOPGCUps1eeI6LPjvo3D8tBl8AKQKBgQCHhv8sRtUSnhk8yCcsbN7Wxe9i4SB67cADBCwSXRoII/pDY"
                + "wRzR0n6Q0Cpv9hI9eLJa6YBtpwhroSzruo5ce/7+1RSNQ4Ts6/t9St2Fy1CQqQ/ZYx4vG14n7RLrlvYCgUy/klNkeJgBckS9R"
                + "YE4yV3E4YmrJjggH1FOVa1wgCeGQKBgQCbCKeM4EahWIyTBiZsTQ/l5hwhjPCrxncbyA2EZWNg3Ri6nuMIQzoBrCoX9L8t7e0"
                + "CKWAN0oM2Cn1VIJhsiE75dzN3vvGBcNZ9y+BwbwxDIAhrztyKKJS0h9YmAUVr+w5WsUPyMUPQ0/1wdTdxvKqQpriddrvyKRSJ"
                + "M9fb29+cwQ==";
        }
        return signingKey;
    }

    /**
     * Retrieves a certificate which can be used to sign attestation policies.
     * @return Returns a base64 encoded X.509 certificate which can be used to sign attestation policies.
     */
    String getPolicySigningCertificate0() {
        String certificate = Configuration.getGlobalConfiguration().get("policySigningCertificate0");
        if (certificate == null) {
            certificate = "MIIC1jCCAb6gAwIBAgIIAxfcH6Co5DowDQYJKoZIhvcNAQELBQAwIjEgMB4GA1UEAxMXQXR0ZXN0YXRpb25DZXJ"
                + "0aWZpY2F0ZTAwHhcNMjEwMTE5MjAxMjU2WhcNMjIwMTE5MjAxMjU2WjAiMSAwHgYDVQQDExdBdHRlc3RhdGlvbkNlcnRpZmlj"
                + "YXRlMDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOOpb5GvUCuOYiB4ZazIePtSazdXGDyjtFlr4ulo1VY1Ai91X"
                + "IcWIPELCV1OfQiIoJlj096u3cirP1GvCKgb4FTNHHi7omDaQvYRmuZZ6KXrqNi5Iu/jKjGgjwYt+FYV/9eqYCWdyS0RjMbKw7"
                + "sZUvBxTDeTqQunwbjPZ1y4JbxXx6xwcZJHfwD6g7aHslsblHh4zM1mhiuoIMpNUeeThLwQTD6oGSmIt+hqRbfvd3Ljr/v7W3m"
                + "SKvw5X9L85PNHaDIUd4vHSDiytZUoXyhtbC8RKGzxgZCz6gFwM5JF6QhYE/A84HFH7JZ3FKk1UJBoTjcv63BshT7Pt3fYMZqV"
                + "SzkCAwEAAaMQMA4wDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAC+YKGp6foV94lYQB/yNQ53Bk+YeR4dgAkR99U"
                + "t1VvvXKyWnka/X8QRjKoaPULDZH4+9ZXg6lSfhuEGTSPUzO294mlNbF6n6cuuuawe3OeuZUO53b4xYXPRv898FBRxdD+FCW/f"
                + "A5HLBrGItfk31+aNUFryCd5RrJfJU8Rurm+7uGPtS16Ft0P7xSnL0C7nfHNVuEKFV0ZbzgzXlzkKQT4d3fYpvOxzYoXImxzwz"
                + "W/jzZjN3aKbOlmY2LyW8J5BKKgA3C4FRWwCTmgqYp2vQhsw1HgCeBjmBN5/imnk2lsgjrvvSdlkXOnNf5atibuguYzdakz99b"
                + "wwWWsd5HddtcyA==";
        }
        return certificate;
    }

    /**
     * Returns the location in which the tests are running.
     * @return returns the location in which the tests are running.
     */
    private static String getLocationShortName() {
        String shortName = Configuration.getGlobalConfiguration().get("locationShortName");
        if (shortName == null) {
            shortName = "wus";
        }
        return shortName;
    }

    /**
     * Returns the url associated with the isolated MAA instance.
     * @return the url associated with the isolated MAA instance.
     */
    private static String getIsolatedUrl() {
        String url = Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL");
        if (url == null) {
            url = "https://attestation_isolated_url";
        }
        return url;
    }

    /**
     * Returns the url associated with the AAD MAA instance.
     * @return the url associated with the AAD MAA instance.
     */
    private static String getAadUrl() {
        String url = Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL");
        if (url == null) {
            url = "https://attestation_aad_url";
        }
        return url;
    }

    /**
     * Returns the set of clients to be used to test the attestation service.
     * @return a stream of Argument objects associated with each of the regions on which to run the attestation test.
     */
    static Stream<Arguments> getAttestationClients() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427

        final String regionShortName = getLocationShortName();
        return getHttpClients().flatMap(httpClient -> Stream.of(
            Arguments.of(httpClient, "https://shared" + regionShortName + "." + regionShortName + ".attest.azure.net"),
            Arguments.of(httpClient, getIsolatedUrl()),
            Arguments.of(httpClient, getAadUrl())));
    }

    /**
     * Returns the set of clients and attestation types used for attestation policy APIs.
     * @return a stream of Argument objects associated with each of the regions on which to run the attestation test.
     */
    static Stream<Arguments> getPolicyClients() {
        return getAttestationClients().flatMap(clientParams -> Stream.of(
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.OPEN_ENCLAVE),
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.TPM),
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.SGX_ENCLAVE)));
    }

}
