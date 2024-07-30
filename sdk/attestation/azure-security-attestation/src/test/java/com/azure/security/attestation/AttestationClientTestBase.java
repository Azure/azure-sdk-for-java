// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.provider.Arguments;

import javax.security.auth.x500.X500Principal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//import io.github.cdimascio.dotenv.Dotenv;

/**
 * Specialization of the TestBase class for the attestation tests.
 * <p>
 * Provides convenience methods for retrieving attestation client builders, verifying attestation tokens, and accessing
 * test environments.
 */
@Isolated
public class AttestationClientTestBase extends TestProxyTestBase {
    protected static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    protected static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    protected static final ClientLogger LOGGER = new ClientLogger(AttestationClientTestBase.class);
    protected Tracer tracer;

    @Override
    protected void beforeTest() {
        super.beforeTest();

        GlobalOpenTelemetry.resetForTest();
        tracer = configureLoggingExporter(testContextManager.getTestName());

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Collections.singletonList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Collections.singletonList("Authorization"))));
        }
    }

    private static final TestMode TEST_MODE = getTestModeLocal();

    static TestMode getTestModeLocal() {
        final String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        LOGGER.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
        return TestMode.PLAYBACK;
    }

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
    public void afterTest() {
        GlobalOpenTelemetry.resetForTest();
    }

    @SuppressWarnings({"deprecation", "resource"})
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
     *
     * @param clientUri - URI for the attestation client.
     * @return the ClientTypes corresponding to the specified client URI.
     */
    static ClientTypes classifyClient(String clientUri) {
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
     *
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationClientBuilder getAuthenticatedAttestationBuilder(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder builder = getAttestationBuilder(httpClient, clientUri);
        if (!interceptorManager.isPlaybackMode()) {
            builder.credential(TestUtil.getIdentityTestCredential(interceptorManager, httpClient));
        } else {
            builder.credential(new MockTokenCredential());
        }

        return builder;
    }

    /**
     * Retrieve an attestationClientBuilder for the specified HTTP client and client URI
     *
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationClientBuilder getAttestationBuilder(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder builder = new AttestationClientBuilder().endpoint(clientUri);

        if (interceptorManager.isPlaybackMode()) {
            // In playback mode, we want to disable expiration times, since the tokens in the recordings
            // will almost certainly expire.
            builder.httpClient(interceptorManager.getPlaybackClient())
                .tokenValidationOptions(new AttestationTokenValidationOptions()
                    .setValidateExpiresOn(false)
                    .setValidateNotBefore(false));
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isPlaybackMode()) {
            builder.httpClient(httpClient);
        }

        return builder;
    }

    /**
     * Retrieve an attestationClientBuilder for the specified HTTP client and client URI
     *
     * @param httpClient - HTTP client ot be used for the attestation client.
     * @param clientUri - Client base URI to access the service.
     * @return Returns an attestation client builder corresponding to the httpClient and clientUri.
     */
    AttestationAdministrationClientBuilder getAttestationAdministrationBuilder(HttpClient httpClient,
        String clientUri) {
        AttestationAdministrationClientBuilder builder = new AttestationAdministrationClientBuilder()
            .endpoint(clientUri);

        if (interceptorManager.isPlaybackMode()) {
            // In playback mode, we want to disable expiration times, since the tokens in the recordings
            // will almost certainly expire.
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isPlaybackMode()) {
            // Add a 10-second slack time to account for clock drift between the client and server.
            builder.tokenValidationOptions(new AttestationTokenValidationOptions()
                    .setValidationSlack(Duration.ofSeconds(10)))
                .credential(TestUtil.getIdentityTestCredential(interceptorManager, httpClient));
        } else {
            builder.tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidateExpiresOn(false)
                .setValidateNotBefore(false))
                .credential(new MockTokenCredential());
        }

        return builder;
    }

    /**
     * Retrieve the signing certificate used for the isolated attestation instance.
     *
     * @return Returns a base64 encoded X.509 certificate used to sign policy documents.
     */
    static String getIsolatedSigningCertificateBase64() {
        return TEST_MODE == TestMode.PLAYBACK
            ? readResource("ISOLATED_SIGNING_CERTIFICATE") // Use a pre-canned certificate captured at provisioning time.
            : Configuration.getGlobalConfiguration().get("ISOLATED_SIGNING_CERTIFICATE");
    }

    protected static X509Certificate getIsolatedSigningCertificate() {
        String base64Certificate = getIsolatedSigningCertificateBase64();
        return X509CertUtils.parse(Base64.getDecoder().decode(base64Certificate));
    }

    /**
     * Retrieve the signing key used for the isolated attestation instance.
     *
     * @return Returns a base64 encoded RSA Key used to sign policy documents.
     */
    static String getIsolatedSigningKeyBase64() {
        return TEST_MODE == TestMode.PLAYBACK
            ? readResource("ISOLATED_SIGNING_KEY") // Use a pre-canned certificate captured at provisioning time.
            : Configuration.getGlobalConfiguration().get("ISOLATED_SIGNING_KEY");
    }

    static PrivateKey privateKeyFromBase64(String base64) {
        byte[] signingKey = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(signingKey);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }

        PrivateKey privateKey;
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
        return privateKey;
    }

    protected static PrivateKey getIsolatedSigningKey() {
        return privateKeyFromBase64(getIsolatedSigningKeyBase64());
    }

    private static String readResource(String resourceName) {
        try (InputStream resource = AttestationClientTestBase.class.getClassLoader().getResourceAsStream(resourceName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            // Only read the first line as that will contain the entire Base64 encoded content. It's possible that an
            // IDE or commandline process could have added a newline character to the end of the file.
            return reader.readLine();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    /**
     * Retrieves a certificate which can be used to sign attestation policies.
     *
     * @return Returns a base64 encoded X.509 certificate which can be used to sign attestation policies.
     */
    static String getPolicySigningCertificate0Base64() {
        return TEST_MODE == TestMode.PLAYBACK
            ? readResource("POLICY_SIGNING_CERTIFICATE0") // Use a pre-canned certificate captured at provisioning time.
            : Configuration.getGlobalConfiguration().get("POLICY_SIGNING_CERTIFICATE0");
    }

    static String getPolicySigningKey0Base64() {
        return TEST_MODE == TestMode.PLAYBACK
            ? readResource("POLICY_SIGNING_KEY0") // Use a pre-canned certificate captured at provisioning time.
            : Configuration.getGlobalConfiguration().get("POLICY_SIGNING_KEY0");
    }

    protected static X509Certificate getPolicySigningCertificate0() {
        return X509CertUtils.parse(Base64.getDecoder().decode(getPolicySigningCertificate0Base64()));
    }

    protected static PrivateKey getPolicySigningKey0() {
        return privateKeyFromBase64(getPolicySigningKey0Base64());
    }

    protected static KeyPair createKeyPair(String algorithm) throws NoSuchAlgorithmException {

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

    @SuppressWarnings("deprecation")
    protected static X509Certificate createSelfSignedCertificate(String subjectName, KeyPair certificateKey)
        throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
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
     *
     * @return returns the location in which the tests are running.
     */
    private static String getLocationShortName() {
        return TEST_MODE == TestMode.PLAYBACK ? "wus"
            : Configuration.getGlobalConfiguration().get("LOCATION_SHORT_NAME");
    }

    /**
     * Returns the url associated with the isolated MAA instance.
     *
     * @return the url associated with the isolated MAA instance.
     */
    private static String getIsolatedUrl() {
        return TEST_MODE == TestMode.PLAYBACK
            ? "https://attestation_isolated_url"
            : Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL");
    }

    /**
     * Returns the url associated with the AAD MAA instance.
     *
     * @return the url associated with the AAD MAA instance.
     */
    private static String getAadUrl() {
        return TEST_MODE == TestMode.PLAYBACK
            ? "https://attestation_aad_url" : Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL");
    }

    /**
     * Returns the set of clients to be used to test the attestation service.
     *
     * @return a stream of Argument objects associated with each of the regions on which to run the attestation test.
     */
    static Stream<Arguments> getAttestationClients() {
        final String regionShortName = getLocationShortName();
        return getHttpClients().flatMap(httpClient -> Stream.of(
            Arguments.of(httpClient, "https://shared" + regionShortName + "." + regionShortName + ".attest.azure.net"),
            Arguments.of(httpClient, getIsolatedUrl()),
            Arguments.of(httpClient, getAadUrl())));
    }

    /**
     * Returns the set of clients and attestation types used for attestation policy APIs.
     *
     * @return a stream of Argument objects associated with each of the regions on which to run the attestation test.
     */
    static Stream<Arguments> getPolicyClients() {
        return getAttestationClients().flatMap(clientParams -> Stream.of(
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.OPEN_ENCLAVE),
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.TPM),
            Arguments.of(clientParams.get()[0], clientParams.get()[1], AttestationType.SGX_ENCLAVE)));
    }
}
