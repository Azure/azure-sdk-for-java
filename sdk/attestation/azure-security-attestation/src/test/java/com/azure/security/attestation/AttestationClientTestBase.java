// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.X509CertUtils;
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
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
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
     * Retrieve an authenticated attestationAdministrationClientBuilder for the specified HTTP
     * client and client URI.
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationAdministrationClientBuilder getAdministrationBuilder(HttpClient httpClient, String clientUri) {
        AttestationAdministrationClientBuilder builder = getAttestationAdministrationBuilder(httpClient, clientUri);
        if (!interceptorManager.isPlaybackMode()) {
            builder.credential(new EnvironmentCredentialBuilder().httpClient(httpClient).build());
        }
        return builder;
    }


    /**
     * Retrieve an authenticated attestationClientBuilder for the specified HTTP client and client URI
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationAdministrationClientBuilder getAuthenticatedAttestationBuilder(HttpClient httpClient, String clientUri) {
        AttestationAdministrationClientBuilder builder = getAttestationAdministrationBuilder(httpClient, clientUri);
        if (!interceptorManager.isPlaybackMode()) {
            builder.credential(new EnvironmentCredentialBuilder().httpClient(httpClient).build());
        }
        return builder;
    }

    /**
     * Retrieve an attestationClientBuilder for the specified HTTP client and client URI
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationClientBuilder getAttestationBuilder(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder builder = new AttestationClientBuilder()
            .endpoint(clientUri)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
//            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(interceptorManager.getRecordPolicy());

        // In playback mode, we want to disable expiration times, since the tokens in the recordings
        // will almost certainly expire.
        if (interceptorManager.isPlaybackMode()) {
            builder.tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidateExpiresOn(false)
                .setValidateNotBefore(false)
            );
        }
        return builder;
    }
    /**
     * Retrieve an attestationClientBuilder for the specified HTTP client and client URI
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationAdministrationClientBuilder getAttestationAdministrationBuilder(HttpClient httpClient, String clientUri) {
        AttestationAdministrationClientBuilder builder = new AttestationAdministrationClientBuilder()
            .endpoint(clientUri)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
//            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(interceptorManager.getRecordPolicy());

        // In playback mode, we want to disable expiration times, since the tokens in the recordings
        // will almost certainly expire.
        if (interceptorManager.isPlaybackMode()) {
            builder.tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidateExpiresOn(false)
                .setValidateNotBefore(false)
            );
        }
        return builder;
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
        return getSigningCertificateByKeyId(token.getHeader().getKeyID(), httpClient, clientUri)
            .handle((signer, sink) -> {
                final PublicKey key = signer.getCertificates().get(0).getPublicKey();
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
     * @param client - Http Client used to retrieve signing certificates.
     * @param clientUri - Base URI for the attestation client.
     * @return X509Certificate which will have been used to sign the token.
     */
    Mono<AttestationSigner> getSigningCertificateByKeyId(String keyId, HttpClient client, String clientUri) {
        AttestationClientBuilder builder = getAttestationBuilder(client, clientUri);
        return builder.buildAsyncClient().listAttestationSigners()
            .handle((signers, sink) -> {
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
    String getIsolatedSigningCertificateBase64() {
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

    X509Certificate getIsolatedSigningCertificate() {
        return X509CertUtils.parse(Base64.getDecoder().decode(getIsolatedSigningCertificateBase64()));
    }

    /**
     * Retrieve the signing key used for the isolated attestation instance.
     * @return Returns a base64 encoded RSA Key used to sign policy documents.
     */
    String getIsolatedSigningKeyBase64() {
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

    PrivateKey privateKeyFromBase64(String base64) {
        byte[] signingKey = Base64.getDecoder().decode(base64);
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
        return privateKey;
    }

    PrivateKey getIsolatedSigningKey() {
        return privateKeyFromBase64(getIsolatedSigningKeyBase64());
    }

    /**
     * Retrieves a certificate which can be used to sign attestation policies.
     * @return Returns a base64 encoded X.509 certificate which can be used to sign attestation policies.
     */
    String getPolicySigningCertificate0Base64() {
        String certificate = Configuration.getGlobalConfiguration().get("policySigningCertificate0");
        if (certificate == null) {
            certificate = "MIIC1jCCAb6gAwIBAgIINiKXaYxzhQkwDQYJKoZIhvcNAQELBQAwIjEgMB4GA1UEAxMXQXR0ZXN0YXRpb25DZXJ"
                + "0aWZpY2F0ZTAwHhcNMjEwODA1MjM0OTAyWhcNMjIwODA1MjM0OTAyWjAiMSAwHgYDVQQDExdBdHRlc3RhdGlvbkNlcnRpZmljYXRlMD"
                + "CCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMqSkK84QAxkBvgWAsqKrVMIBl/sv1MRWXv8S8lHMhy6xCcVnYtfxlqwVqOYl"
                + "PJq6j8k4m1VLIhJqZTSAtjIksX8Gli15MGMIz6qi1QZgQpExEuWQR0WJC7y7z8rbB1y5LvKt8waxyD13n5dyJM2acEyNY0oxT019aBG"
                + "9q7Pi74JN6c18YIveG4QcCPs9v2lruXdzXK8KMcNyFNd5KkbMXYCHJgFJUCi45Zcr6+lNTD6vHwJGoWfu8c5wauAsOODHrMZjiABQJi"
                + "6pA3VarHQF6mLYCIkAl6be2nb9Dp/eH9RUxovZLn2FIu+Zn5ARGaY98kGm7L52in0dH1V/bKuxwUCAwEAAaMQMA4wDAYDVR0TBAUwAwEB"
                + "/zANBgkqhkiG9w0BAQsFAAOCAQEAxSGcikwkx2hSTwtDCoZZGQOOA1Zle/6rrNqjiq2nkg7UJpgn+zbi9a18RJSyVJJyYpwJnA2Kg9Ol9"
                + "QFTf9E8z+LufI27KE/AubhqjdxZBrgfGdjKY8olNNEeCa5Up4uZN4EkOl4dqcj+NyyFjo2Sp9YfbgafDW96CCOl+u1GDZA2IHrGMrUe"
                + "kiOfqWikqlyYy+vaNvrq6IHIA+pcaiEAWekQmVOMHB/96ubDlyW65N6ofVZ2Q1SPV+cxYFbjQQmg9m/lbLIVVdZsy3tlqFtYijcZNl/"
                + "cEC0iMoZDrz8JTVmCU/cMxYqS0EZMJYCOqjLkrdIqndCnJW4ci1J6k94L9A==";
        }
        return certificate;
    }

    String getPolicySigningKey0Base64() {
        String key = Configuration.getGlobalConfiguration().get("policySigningKey0");
        if (key == null) {
            key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKkpCvOEAMZAb4FgLKiq1TCAZf7L9TEVl7/EvJRzIcusQ"
                + "nFZ2LX8ZasFajmJTyauo/JOJtVSyISamU0gLYyJLF/BpYteTBjCM+qotUGYEKRMRLlkEdFiQu8u8/K2wdcuS7yrfMGscg9d5+XciTNm"
                + "nBMjWNKMU9NfWgRvauz4u+CTenNfGCL3huEHAj7Pb9pa7l3c1yvCjHDchTXeSpGzF2AhyYBSVAouOWXK+vpTUw+rx8CRqFn7vHOcGrg"
                + "LDjgx6zGY4gAUCYuqQN1Wqx0Bepi2AiJAJem3tp2/Q6f3h/UVMaL2S59hSLvmZ+QERmmPfJBpuy+dop9HR9Vf2yrscFAgMBAAECggEB"
                + "AID8ZkhL5ux83LsnOMvDFa4jE/wMgZ7hEzuRYKhfPxdwDOpeJxzR9TlVwzUUOPNLBLEESXEYpOx7CxIJz2o9/Mc4SYZm+6wKEX8blPA"
                + "N9U6Wju8aU4ezy4JhidmNSqBNwjuZTwMVoeno5K1OBiNGqHwt/k9NwJnDPA28YeLZoL91eNTtTfJDxjPlkKKtmcmuczE4PABIoZJqz5"
                + "bMl9ezDJQ96QnuNdKo+V9aoKCvixrhOvOVFr64yB6T1k/dE+/if2qdqiebbNZDUUvmzdGOG8Nlv9HYN2TIcfCOvN03Cwn2sKbE7Wcrw"
                + "ic9o6H4evWsVsIydWMEPT8tnt9telHWIxkCgYEA7IzXvRm8IiwbT1YgwlA+GS+Wuz3Pj/X+Ah2aY0b5FzvHlqGGdpayZg5SMvBVqkj9"
                + "UwROZB4ml+FTccjzE1otmuoYAkvVer4aphs1T8l2Gzzpx6bOoOCb0FXvMRRD3MpfXKSb5imaorEcFYJuCgcvxbf3jl6VWGb+ijsCWZa"
                + "y918CgYEA2zqGbjlNFyuwqXI3wYvSa8PgJfgv4gigjXRS8UQH8UbWp7qmSgzAeu2kubSlyP+xNUTV+RbarSjBb+wNq/vdop+daUuHoX"
                + "ov2lKFpE9UIB5xEBkhIV3ODxEfj5spCwuE9UKcok3NFm3RhgB+wQDa93PhhedelERvCaypvJQfUBsCgYABK2EVqj7n3Ff2OHLJAySLc"
                + "1THcDLKf2jWEddljkBFASKnd/z2MSCIqKF3ZwDFar713huVGyENtyt2cIvjGJsJHQcpW76ecLopABFvZ4uR7uco+YYj/XhHu2UHVRZQ"
                + "zR9TkezDYolFLKL66D4rBoYR8CrlJUqPuVKg1FHap4gS+QKBgHVNlH7IBFrgks+oAPN8GGR3U6mdaimdCiOGWZclGsbca6El+zJmLlv"
                + "Yaqq/YXHyduSU55U3yFydERwNB6e9xfLtSzH7KyCZG5/LRh0MIWxqPX8qoxKSed6P//48PLLfQA5nzR3/WTymGFWGUEx/Y6rCg6q9iV"
                + "r2Xx+jFtODwll/AoGAUw8QdBn3886mNDBRbEZECgNYil7YRBfhQ593LXAUpAzM8AqI64L5xQOCV2yX+sx793t88OVoJTP/kzmP43POo"
                + "380F4Q4cs5h57eFH7Hy4CEFoyTgI1zLPGoOAYLHLZmtUiLW0hCdctYoAHLcsJS+HzLvpka55FaTqgiF/yf46O0=";
        }
        return key;
    }

    X509Certificate getPolicySigningCertificate0() {
        return X509CertUtils.parse(Base64.getDecoder().decode(getPolicySigningCertificate0Base64()));
    }

    PrivateKey getPolicySigningKey0() {
        return privateKeyFromBase64(getPolicySigningKey0Base64());
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
