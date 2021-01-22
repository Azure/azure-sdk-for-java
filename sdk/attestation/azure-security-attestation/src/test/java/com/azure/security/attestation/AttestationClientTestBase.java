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
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.JsonWebKey;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AttestationClientTestBase extends TestBase {

    private static final String DATAPLANE_SCOPE = "https://attest.azure.net/.default";

    final ClientLogger logger = new ClientLogger(AttestationClientTestBase.class);

    enum ClientTypes {
        SHARED,
        ISOLATED,
        AAD,
    }

    ClientTypes classifyClient(String clientUri) {
        assertNotNull(clientUri);
        String regionShortName = getLocationShortName();
        String sharedUri = "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net";
        if (sharedUri.equals(clientUri)) {
            return ClientTypes.SHARED;
        } else if (getIsolatedUrl().equals(clientUri)) {
            return ClientTypes.ISOLATED;
        } else if (getAadUrl().equals(clientUri)) {
            return ClientTypes.AAD;
        }
        throw new IllegalArgumentException();
    }

    InputStream base64ToStream(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(decoded);
    }

    AttestationClientBuilder getBuilder(HttpClient httpClient, String clientUri) {
        return new AttestationClientBuilder().pipeline(getHttpPipeline(httpClient)).instanceUrl(clientUri);
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
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

    Mono<JWTClaimsSet> verifyAttestationToken(HttpClient httpClient, String clientUri, String attestationToken) throws ParseException {
        SignedJWT token = SignedJWT.parse(attestationToken);

        return getSigningCertificateByKeyId(token, httpClient, clientUri)
            .flatMap(cert -> {
                PublicKey key = cert.getPublicKey();
                RSAPublicKey rsaKey = (RSAPublicKey) key;

                RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
                try {
                    assertTrue(token.verify(verifier));
                } catch (JOSEException e) {
                    logger.logExceptionAsError(new RuntimeException(e.toString()));
                }


                JWTClaimsSet claims = null;
                try {
                    claims = token.getJWTClaimsSet();
                } catch (ParseException e) {
                    logger.logExceptionAsError(new RuntimeException(e.toString()));
                }
                assertNotNull(claims);
                return Mono.just(claims);
            });
    }

    /**
     * Create a JWS Signer from the specified PKCS8 encoded signing key.
     * @param signingKeyBase64 Base64 encoded PKCS8 encoded RSA Private key.
     * @return JWSSigner created over the specified signing key.
     * @throws NoSuchAlgorithmException - should never  throws this.
     * @throws InvalidKeySpecException - Can throw this if the key is invalid.
     */
    JWSSigner getJwsSigner(String signingKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] signingKey = Base64.getDecoder().decode(signingKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(signingKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
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
    Mono<X509Certificate> getSigningCertificateByKeyId(SignedJWT token, HttpClient client, String clientUri) {
        AttestationClientBuilder builder = getBuilder(client, clientUri);
        return builder.buildSigningCertificatesAsyncClient().get()
            .map(keySet -> {
                CertificateFactory cf = null;
                try {
                    cf = CertificateFactory.getInstance("X.509");
                } catch (CertificateException e) {
                    logger.logExceptionAsError(new RuntimeException(e.toString()));
                }

                String keyId = token.getHeader().getKeyID();

                for (JsonWebKey key : keySet.getKeys()) {
                    if (keyId.equals(key.getKid())) {
                        Certificate cert = null;
                        try {
                            assert cf != null;
                            cert = cf.generateCertificate(base64ToStream(key.getX5C().get(0)));
                        } catch (CertificateException e) {
                            logger.logExceptionAsError(new RuntimeException(e.toString()));
                        }

                        assertTrue(cert instanceof X509Certificate);

                        return (X509Certificate) cert;

                    }
                }
                fail();
                throw new RuntimeException(String.format("Key %s not found in JSON Web Key Set", keyId));
            });
    }

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

    private static String getLocationShortName()
    {
        String shortName = Configuration.getGlobalConfiguration().get("locationShortName");
        if (shortName == null) {
            shortName = "wus";
        }
        return shortName;
    }

    private static String getIsolatedUrl()
    {
        String url = Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL");
        if (url == null) {
            url = "https://attestation_isolated_url";
        }
        return url;
    }

    private static String getAadUrl()
    {
        String url = Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL");
        if (url == null) {
            url = "https://attestation_aad_url";
        }
        return url;
    }

    static Stream<Arguments> getAttestationClients() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427

        List<Arguments> argumentsList = new ArrayList<>();

        String regionShortName = getLocationShortName();
        getHttpClients().forEach(httpClient -> Stream.of(
            "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net",
            getIsolatedUrl(),
            getAadUrl())
            .forEach(clientUri -> argumentsList.add(Arguments.of(httpClient, clientUri))));
        return argumentsList.stream();
    }

    static Stream<Arguments> getPolicyClients() {
        List<Arguments> argumentsList = new ArrayList<>();
        getAttestationClients().forEach(clientParams -> Arrays.asList(
            AttestationType.OPEN_ENCLAVE,
            AttestationType.TPM,
            AttestationType.SGX_ENCLAVE)
            .forEach(attestationType -> argumentsList.add(Arguments.of(clientParams.get()[0], clientParams.get()[1], attestationType))));
        return argumentsList.stream();
    }

}
