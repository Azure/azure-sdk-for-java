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
import org.junit.jupiter.api.parallel.Isolated;
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
 * <p>
 * Provides convenience methods for retrieving attestation client builders, verifying attestation tokens,
 * and accessing test environments.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
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
        GlobalOpenTelemetry.resetForTest();
        super.setupTest(testInfo);
        String testMethod = testInfo.getTestMethod().isPresent()
            ? testInfo.getTestMethod().get().getName()
            : testInfo.getDisplayName();
        tracer = configureLoggingExporter(testMethod);
    }

    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
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
        if (signingCertificate != null) {
            return signingCertificate;
        }

        // Use a pre-canned signing certificate captured at provisioning time.
        return "MIIC+TCCAeGgAwIBAgIJAPEXMUFrBAdWMA0GCSqGSIb3DQEBCwUAMDMxMTAvBgNVBAMTKEF0dGVzdGF0aW9uSXNvbGF0ZWRNYW5hZ2V"
            + "tZW50Q2VydGlmaWNhdGUwHhcNMjMwNjExMTc1ODU0WhcNMjQwNjEwMTc1ODU0WjAzMTEwLwYDVQQDEyhBdHRlc3RhdGlvbklzb2xhdGV"
            + "kTWFuYWdlbWVudENlcnRpZmljYXRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvQzmhIPfNjCm8QF+icDuzDd8OCA2tb/"
            + "ZEW/bOuoKgwY22DN+t8H2T5n+iwDLevvCMJyIaSwe2NResD1Dfz3aDiGhalKZt5KFzp59FDW0QRmrTtMTruJOqGtl1h82qbKtoxvNRjp"
            + "vyRnU8aad9ODC+HLMJSSeBxIzbi9nKs/1Ok4uFp1CS8KssUY8gl+4LK/yfIXP1URxBnyD6oITuh3OkZ5LyNFvYE0VCsaspLSyy1tNpkq"
            + "hSq9s2g6TuajN5TTaNykCjNaSSB6xea2WDkGxcROBxvzYhOleU7ZZvZsOzHCgtrZU1uw9uNbb2YfogmLC0FR2UQuQpL+wPY1Sja/ehQI"
            + "DAQABoxAwDjAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBUbhuwltathZcz+MJic0K08SYqdJtvFpeBrGvmMTPUdnSIpD+"
            + "sBe8YcRR8ads8JjrdQ+YBpX8zPfUldyvLgWq7VvDZH98IJDasCimbjXqQoVVNYTm3KyyYj8yOLbJYo9lLj68nt1KCYgl/WZ1Qwf3sNbu"
            + "qwtkL9xvGyCP7Jzxzoyav46azCQiKyG4SSiJ1DWY3i1XdUeE2fU3fxlroia9Ccti5/Jo+fqxCaW6WR7RUsIJGFUMuGi/y7Rwt4stPSZQ"
            + "2mbzZXl5FEDtDH0lqbS/ZHPN0UJc9J8TPBnmlSkJdOTkkTHy1PgzMSpqA9kUoywUwRri6Yg1wfE7hJf3ZVMA6";
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
        if (signingKey != null) {
            return signingKey;
        }

        // Use a pre-canned signing key captured at provisioning time.
        return "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC9DOaEg982MKbxAX6JwO7MN3w4IDa1v9kRb9s66gqDBjbYM363wfZ"
            + "Pmf6LAMt6+8IwnIhpLB7Y1F6wPUN/PdoOIaFqUpm3koXOnn0UNbRBGatO0xOu4k6oa2XWHzapsq2jG81GOm/JGdTxpp304ML4cswlJJ4"
            + "HEjNuL2cqz/U6Ti4WnUJLwqyxRjyCX7gsr/J8hc/VRHEGfIPqghO6Hc6RnkvI0W9gTRUKxqyktLLLW02mSqFKr2zaDpO5qM3lNNo3KQK"
            + "M1pJIHrF5rZYOQbFxE4HG/NiE6V5Ttlm9mw7McKC2tlTW7D241tvZh+iCYsLQVHZRC5Ckv7A9jVKNr96FAgMBAAECggEANHxnZ3UBPJd"
            + "HUfP7QJ167uGcsCxpZA5OYqPudq3Rm0zXXzD0xmmoS7NSUx82Xv+Ui1B6ItDoA4T6gO2RVrtCVdgKa21hVfDtC08/JKRAVi6dO/YoKc6"
            + "Uzfd7P3ZNA9IimP04AX5p2Kg+UZTTl8/q8XVVrh47bXUlKDsq5lz0FWpyWY8QuwkSZEg45kdNK6ebYUlSAdJNzhPT1NyUwm8rVRXDYdE"
            + "tEH+Dpo3WPwv5PmHARNM5d6MtZJCFOAdXrSlhIJmHW9yrDb+93UIEZADWEXzDo1VgRMBlG0U8GAsIWcTrBETwSBpTPG6rcyTFd5EP0Bc"
            + "82k4C8J+R6rgsZcoixQKBgQD0KU6zk0bDAyaQ24k5/sxL2yRyYsB7skJj6ICsGePgdiS54uHAI+ZlWahd6sc6xzYcrdyVoiTiGJGZBjk"
            + "xwAl/C/m37z+qSOtOPbOv7Vwm7neeh5pwAFWS5TSTP5eYoh3Jy58C8aC7DdQEXFym14rFvn0EVeFLjbQ8GX+yHElBFwKBgQDGN4TrTGq"
            + "r2jzrSEk2MuMdol6TOFi2h7avrc5WNQ7A0CTSKS6bOB2iQRZ5NGNXbO/tjc1O25AymRvtyot1TjI0POOuUqANg+G++8NlwV+jRLt4/bh"
            + "vxq6V5poYz4vVqOt8XMoud6P2ZUEdyMbm8Mb5CyhgIgq40brP4svVrwFGwwKBgQDbZ7M9U38nBtHFIkJyRzE2iTp7P5pQjaNzPB9EiA9"
            + "qSUA/ek3650fGk4bkvnFBtHo/00yBei9CrWlI5XrH5hWigRRp4SiI0PYCXRf3Y4iHw4rifmCdfO9pDWbtWxvTIPs7tyqw7ojNDmQTdxw"
            + "bIV2mzMJy/ulxh2kLCK7jUt/RCQKBgF2GEJcc5S/Utu2km7aQ6AwxckIwUds9yXq2nSabvixHiTgH6k+eeIz3/OyPRVgYtFP3lilg+3a"
            + "EO2NFL+vXS2TOXfcAUThPQ5JjI9y9EqzsXzBFNmK28eUUexy9wJK9Mu7osRSfqqM1PzYYZ5DLhnY3R4OkTD7L5kL0FYzrF8UVAoGBAKy"
            + "hZZuD4sMS3WsDg9d+LfhnGcKTsBfovi7KSqHLLZmqJ6sxKXGoWe9IlGadfWk3VNIW/LQ+/hipZv4bsOCBcOc097+Ev23u6bl2Xm0LHq9"
            + "FFUDqTfD8qTpQF+GWOEXfdV5NfwmwRc1MLcAa3BFwvRgG+yCoMZgP4PGE3rhdakWF";
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
        if (certificate != null) {
            return certificate;
        }

        // Use a pre-canned certificate captured at provisioning time.
        return "MIIC1zCCAb+gAwIBAgIJAInmgaSoVbKAMA0GCSqGSIb3DQEBCwUAMCIxIDAeBgNVBAMTF0F0dGVzdGF0aW9uQ2VydGlmaWNhdGUwMB4"
            + "XDTIzMDYxMTE3NTg1NFoXDTI0MDYxMDE3NTg1NFowIjEgMB4GA1UEAxMXQXR0ZXN0YXRpb25DZXJ0aWZpY2F0ZTAwggEiMA0GCSqGSIb"
            + "3DQEBAQUAA4IBDwAwggEKAoIBAQDGa4rJPS71BO2YSYRo34MzlSii/dPqkNSPqGyfosw5/uZjNrFpc0KxYePcCFkMt/WGqdCS8w/Yknh"
            + "1iaS+hQZej0W6WEcsMvIVt5fN7dMGVi2wlO6XUJZJMmNJ6O92WRmxcCxaigNhkCDKeHQQ2elukpjajFIozjvIxTFRtoJB4ej9S3l1X8i"
            + "y1ft13/7dvFI4JeniN3RKXYDHti8A3S37u+nRrwSonhSrkw/nqb6BnOem3c2+GeFRCYrqx+eHExoTmebnAq6mhj6pwDK8o/x9sFarNln"
            + "Y6lp8/5GcdyPtkkbW71T5FvZ2JZuhdxB5LpwrvqTbHPZuRNSH4VMKdzRhAgMBAAGjEDAOMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQE"
            + "LBQADggEBAFifWaHWN2X2be0yiYM4HJFFsF0UDjSAQU8dEfGwLbOUFtep43gEUX8bkGaLwUBGmjXU8P1BlP+UpB+ACHbpZgOeL5qDHEF"
            + "iWm7z3/mYy0Zi4qnSrDtFB5Z+lGCnbZAJ+ixQhiNjYuSV352Va6Y7d/8tTWMOZW5x7+GsFRu64/jz7D3Blbiui0Y0ojPr+2nVB3fRYZ2"
            + "HMYnXtC/EZjNwByvbhOjf/PH0G56x1arRUSQSHNn8HYCEMrYbvL6Us8bKBGapgbJ7xJV2PGDjxHaIS6SYtx4AYTdoWxufunx7O7zeM0Y"
            + "rpOnRzW33TaMGbDuxieuG7l+LfkpgyxYPjG8ARRc=";
    }

    String getPolicySigningKey0Base64() {
        String key = Configuration.getGlobalConfiguration().get("policySigningKey0");
        if (key != null) {
            return key;
        }

        return "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGa4rJPS71BO2YSYRo34MzlSii/dPqkNSPqGyfosw5/uZjNrFpc0K"
            + "xYePcCFkMt/WGqdCS8w/Yknh1iaS+hQZej0W6WEcsMvIVt5fN7dMGVi2wlO6XUJZJMmNJ6O92WRmxcCxaigNhkCDKeHQQ2elukpjajFI"
            + "ozjvIxTFRtoJB4ej9S3l1X8iy1ft13/7dvFI4JeniN3RKXYDHti8A3S37u+nRrwSonhSrkw/nqb6BnOem3c2+GeFRCYrqx+eHExoTmeb"
            + "nAq6mhj6pwDK8o/x9sFarNlnY6lp8/5GcdyPtkkbW71T5FvZ2JZuhdxB5LpwrvqTbHPZuRNSH4VMKdzRhAgMBAAECggEAL7uzWZIkd8F"
            + "FvzsAZZJPp9scOYnCr01ENYMZDpZFdzZBCmEf8PRPeQjJnidL9OlUYZ78lD1w9HLYE76XKVYrIBhrd9bA2HsxDdRq6plqCL42o6JQe4p"
            + "hfR5oEGG/KcNpkOwhX26QPapEmTC9C0jBBV6y8vCK21Ixw6zcnoG/eEWJ0A0w0JVq0N2acjcwM+ToYeUpDsCv1KU+G9HVpg2GZpxyRo1"
            + "+N3wLjSKjBoKprp13yIpAFBUXsMuiZt5GCPBo2Uu3aYDVybTnyhEbGx+OXyF52VKxIvmvmM7dzTW8TCqTC7VOdpiViIPVK40QqnHvFHX"
            + "Akx6gRXMc8cHzYu0uWQKBgQD0mCJo71T6SWOe6gxFszfly7zRA2v0UGQW3+xbdqp/PiHSsdKuxlFQFWgtwat6l+K+h2XjtkaHY4ufw60"
            + "QcNvrqPM8wxRwXgKNwI7mOdf/M/8Tifr/spMYQqsf9i0Pv8UfVoNgTHtO/N6iiLmxs9eWvrMjDKWpSdc8MbJ21Qjh5wKBgQDPrDMFMYn"
            + "j+H33W3H/2LHiS2QUnhOVtonyivGE88mWFwYvXfTfwbknCA3ZIlxwg0hUWNGRRWlbU3ESTzNPxsZNRchlA4yR9l+/3bbhusXcHqpk5Ap"
            + "OuVdxuw2S3sVUSTqCnyt1Ld+v5j2wuFFa4b7qkBJoTK04Oinxf6vnEtX+dwKBgGYRJirL1JYtEdk6qaxcQTMvdbdyyhXFhj7VangwaK1"
            + "4a/zUH6q3Ly9P/404iCo8P25qaeZAJeZRb+5XZVy6+8q3UelFLr5FPKOSew8vjCb+t66boZYaGq/BAk/wFpW/3K5bF6+TutEXTIBetb6"
            + "l4Y0SPwadPHQMUDjaNDLKa055AoGBAL+l6m+gh+AaMiVT/qBdGcv8Rx+/wdMn4aYzEy/eyun31X1g99qGrJeYly8KxsQOY3FzhfLKW7A"
            + "sEO0UqgW1Ksuk2opd/Bu3giVONreZ5X59zkHNMLX11Pzf4mbnrrNZ4OUqfrBk5Uku8T9k2a9FJSrSSip2mGSLSKnVaI6Wk2WBAoGABLp"
            + "bcfDDrB9XOVDDY0r1fB+dXE+tNYS3bZ8DUmzfpIgZ0y5kjD6EaWlJKOmMiQps4K8OQH3zM/dkiyc2GIi18gP6HCKx7edZTx1YiFKMqxc"
            + "RiwJWdNgCYzuW1quOxr8tGCyxbS5XnUsKTmYJU3VMxR4GgBsPcCzCqxIkN0zifY4=";
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
