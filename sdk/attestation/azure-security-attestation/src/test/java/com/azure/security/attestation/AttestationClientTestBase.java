// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.nimbusds.jose.util.X509CertUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//import io.github.cdimascio.dotenv.Dotenv;

/**
 * Specialization of the TestBase class for the attestation tests.
 *
 * Provides convenience methods for retrieving attestation client builders, verifying attestation tokens,
 * and accessing test environments.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class AttestationClientTestBase extends TestBase {

    protected final ClientLogger logger = new ClientLogger(AttestationClientTestBase.class);
    protected Tracer tracer;

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

    @Override
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        super.setupTest(testInfo);
        String testMethod = testInfo.getTestMethod().isPresent()
            ? testInfo.getTestMethod().get().getName()
            : testInfo.getDisplayName();
        tracer = configureLoggingExporter(testMethod);
    }

    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        String testMethod = testInfo.getTestMethod().isPresent()
            ? testInfo.getTestMethod().get().getName()
            : testInfo.getDisplayName();
        GlobalOpenTelemetry.resetForTest();
        super.teardownTest(testInfo);
    }

    public static Tracer configureLoggingExporter(String testName) {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal()
            .getTracer(testName);
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
     * Retrieve an authenticated attestationClientBuilder for the specified HTTP client and client URI
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationClientBuilder getAuthenticatedAttestationBuilder(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder builder = getAttestationBuilder(httpClient, clientUri);
        if (!interceptorManager.isPlaybackMode()) {
            builder
                .credential(new DefaultAzureCredentialBuilder()
                .httpClient(httpClient).build());
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
        } else {
            // Otherwise, add a 10-second slack time to account for clock drift between the client and server.
            builder.tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidationSlack(Duration.ofSeconds(10)));
        }
        if (!interceptorManager.isPlaybackMode()) {
            builder.credential(new DefaultAzureCredentialBuilder().httpClient(httpClient).build());
        }
        return builder;
    }

    /**
     * Retrieve the signing certificate used for the isolated attestation instance.
     * @return Returns a base64 encoded X.509 certificate used to sign policy documents.
     */
    String getIsolatedSigningCertificateBase64() {
        String signingCertificate = Configuration.getGlobalConfiguration().get("isolatedSigningCertificate");
        if (signingCertificate == null) {
            // Use a pre-canned signing certificate captured at provisioning time.
            signingCertificate = "MIIC+DCCAeCgAwIBAgIICw0n21Fl8+EwDQYJKoZIhvcNAQELBQAwMzExMC8GA1UEAxMoQXR0Z"
                + "XN0YXRpb25Jc29sYXRlZE1hbmFnZW1lbnRDZXJ0aWZpY2F0ZTAeFw0yMTA4MDUyMzQ5MDJaFw0yMjA4MDUyMzQ5MDJaMDMxM"
                + "TAvBgNVBAMTKEF0dGVzdGF0aW9uSXNvbGF0ZWRNYW5hZ2VtZW50Q2VydGlmaWNhdGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBD"
                + "wAwggEKAoIBAQDdX2I5myWt7PT/uq5J1mIK3yJb24rJIYNhjiWAgPGzjr3+n3ZG/tnzzZ4t9eIn6ZN+WruzbH/iil7aiS+2p"
                + "eCiv0xFPiap1wtCFZMOTFzpZzFJlF1tpXuT2v4PZiJa5KPa2PUB1BlvoXtXrNz6mCj+dqK6ldE21qLIH+JkZiPZ1cfi+GeV5"
                + "ANucPjKD749umarhsQGbHXK2yK2iLPeulEMekUPyv+O/MVoVt/plRl3oG/4i+ZAc3T0IVPwjtPJtf1ko/P7ytFWcaTjpeDzY"
                + "jozB8rUh/uXfjuyw3RTu1ZGmFXTyQhWl/azIZmNpV2geIUcj0SS64QmvO2QjKXV6I6FAgMBAAGjEDAOMAwGA1UdEwQFMAMBA"
                + "f8wDQYJKoZIhvcNAQELBQADggEBAFtkGTbpgX1i4wLPOQyHkJ/VMJXicxYrQOwpTltT7yM7L+nRuIy06/1JCsiszXVOkFtc1"
                + "fK18vlwLEGH7D4E+sAOz2gfbh8vUL0BuJg4vQdfdXXxAOis0tz/5ALOr7mBvsbmVA0dvA9ZcVv/6RwPezBQgCbWODDsv0CBQ"
                + "GfYTt2twZx3M0U97x8+MIE+4qSgXQ3oX7h2RyxxotMx/DDBA8lp8OdQ3fGKJ8mzNydmnsYdn378GnZW6MczTMyzbWcakuyuP"
                + "wd10RlO8gzRvFj+ep21DsRkk8xIo5l+TalG54pfnMjUcRWc8DO4Sq4FGB3WGqgFR0aQaU9bbo2vEcypCaU=";
        }
        return signingCertificate;
    }

    protected X509Certificate getIsolatedSigningCertificate() {
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
            signingKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDdX2I5myWt7PT/uq5J1mIK3yJb24rJI"
                + "YNhjiWAgPGzjr3+n3ZG/tnzzZ4t9eIn6ZN+WruzbH/iil7aiS+2peCiv0xFPiap1wtCFZMOTFzpZzFJlF1tpXuT2v4PZiJa5"
                + "KPa2PUB1BlvoXtXrNz6mCj+dqK6ldE21qLIH+JkZiPZ1cfi+GeV5ANucPjKD749umarhsQGbHXK2yK2iLPeulEMekUPyv+O/"
                + "MVoVt/plRl3oG/4i+ZAc3T0IVPwjtPJtf1ko/P7ytFWcaTjpeDzYjozB8rUh/uXfjuyw3RTu1ZGmFXTyQhWl/azIZmNpV2ge"
                + "IUcj0SS64QmvO2QjKXV6I6FAgMBAAECggEAGy9LcKeMyP8AVycloAujnpIoNf+P24MyDxjVoiIzjElLK6mJbM5/FWF6u0omq"
                + "6ATbMDXrAD282rqmwudwGA+Zb34L8iiFtlBmKvtkyPthPwXIWIG1yArPMz3xgxUy7SoKofaDo9tUDgUXX/s4xksb5NCCIe9W"
                + "W6iLtE7i/i/DlDn7SCOVCGxfTs/arMml04065QSJRUeuDWD0g4GSylWl48z8+GEl3UO5NFzrYSEirFc8r3/ycNtF+5G+Gle9"
                + "7gEOn9Hlh2f4R5cA06DYOnYieaqCCoklVlbHLQgpJkhrEl2tcuJ4WdNUaMQtsD+9VaOTdSfcG1FDRETTSrH2rJFQQKBgQDnZ"
                + "H3RlZHz+6NaBLzk89TQtetX8MtoJlYIOVpB9JQ3I/Q5LvyDouZAAWAExZO9cuebnLM+68lWez52ciSuRZa/W0A9Atcn7E/Wr"
                + "46TP9Y7LKQTBQ2GW6N7bEZ1C127dhLMTpjofqTZGBjH0CbLNVZz7wHF8fAam0j+GgAd5pKNzwKBgQD06hsMb8dyuijUBV6Dc"
                + "/ybwWiZmTcSTuHbpdakAhS9wq1gxirXwYnZkrkA1eA2Yavc4VnYc5umgjHJbqBee8G5oXPvSzxAGkiTnyTVGWymWV+Fdkeqs"
                + "HgANbecRQBEoVglSAr7S/OqTKT9tMm23HPkQEpmjDhPxMTYncx/nhdnawKBgQDYYxKCN3Q5DN6y5PFczmT7FNTT9VvStt8Ha"
                + "9LrEPS2KApQm48K7wCRZHfNnpLNvLG4xS6erdMn67L7Az0oN+2EX6pQI+Le88+pvZ0AONd3mQSKwNPoDLRyTEwLUqjCEOX9Z"
                + "5b4/M3eMvmhihdtOyDw49btrJXT29nDvr7TN3df4wKBgQDawkjKwQUbmuBhETKso+tcjFML72jbd44SDX09HYa9QKhwqlEWS"
                + "o8AwidxcZhFutQyBS+lQQ4kmmIyFBg2jMArOOU/Nqpob5GoGhxiI8WCiI3jvhShh+KH/XM1qARnSN5c3o7Ai8TntnIhE1yhc"
                + "yQpGqvaESEzTwSsn7ZLv0AUZQKBgQDIZreuQLZilRMSjf2+8eitcAcnJLmba8wkDaOBMDCQBf8WMKbEuBzvmLOBYs6G6hHJ2"
                + "Qqjy6mLXeFVdOBgdF8SHZVJ+nwGh0LfV86LhesbCwiNApooSR1HqBaS6NTNONZTYPBOOytdHLkG6RkwgIiRp2t+lbaTFllyb"
                + "T1llDajSw==";
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

    protected PrivateKey getIsolatedSigningKey() {
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

    protected X509Certificate getPolicySigningCertificate0() {
        return X509CertUtils.parse(Base64.getDecoder().decode(getPolicySigningCertificate0Base64()));
    }

    protected PrivateKey getPolicySigningKey0() {
        return privateKeyFromBase64(getPolicySigningKey0Base64());
    }

    protected KeyPair createKeyPair(String algorithm) throws NoSuchAlgorithmException {

        KeyPairGenerator keyGen;
        if ("EC".equals(algorithm)) {
            keyGen = KeyPairGenerator.getInstance(algorithm, Security.getProvider("SunEC"));
        } else {
            keyGen = KeyPairGenerator.getInstance(algorithm);
        }
        if ("RSA".equals(algorithm)) {
            keyGen.initialize(2048); // Generate a reasonably strong key.
        }
        return keyGen.generateKeyPair();
    }

    protected X509Certificate createSelfSignedCertificate(String subjectName, KeyPair certificateKey) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setIssuerDN(new X500Principal("CN=" + subjectName));
        generator.setSubjectDN(new X500Principal("CN=" + subjectName));
        generator.setPublicKey(certificateKey.getPublic());
        if (certificateKey.getPublic().getAlgorithm().equals("EC")) {
            generator.setSignatureAlgorithm("SHA256WITHECDSA");
        } else {
            generator.setSignatureAlgorithm("SHA256WITHRSA");
        }
        generator.setSerialNumber(BigInteger.valueOf(Math.abs(new Random().nextInt())));
        // Valid from now to 1 day from now.
        generator.setNotBefore(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        generator.setNotAfter(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS)));

        generator.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        return generator.generate(certificateKey.getPrivate());

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
        // when this issue is closed, the newer version of junit will have better support for
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
