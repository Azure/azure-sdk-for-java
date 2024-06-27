// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.AdministratorContact;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.WellKnownIssuerNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.azure.security.keyvault.certificates.FakeCredentialsForTests.FAKE_CERTIFICATE;
import static com.azure.security.keyvault.certificates.FakeCredentialsForTests.FAKE_PEM_CERTIFICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class CertificateClientTestBase extends TestProxyTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS =
        "AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS);
    private static final String AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL = "ALL";
    private static final String TEST_CERTIFICATE_NAME = "testCert";

    private static final int MAX_RETRIES = 5;
    private static final RetryOptions LIVE_RETRY_OPTIONS = new RetryOptions(new ExponentialBackoffOptions()
        .setMaxRetries(MAX_RETRIES)
        .setBaseDelay(Duration.ofSeconds(2))
        .setMaxDelay(Duration.ofSeconds(16)));

    private static final RetryOptions PLAYBACK_RETRY_OPTIONS =
        new RetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    CertificateClientBuilder getCertificateClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        CertificateServiceVersion serviceVersion) {
        TokenCredential credential;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = testTenantId == null
                ? Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID")
                : testTenantId;

            credential = new ClientSecretCredentialBuilder()
                .clientSecret(Objects.requireNonNull(clientKey, "The client key cannot be null"))
                .clientId(Objects.requireNonNull(clientId, "The client id cannot be null"))
                .tenantId(Objects.requireNonNull(tenantId, "The tenant id cannot be null"))
                .additionallyAllowedTenants("*")
                .build();

            if (interceptorManager.isRecordMode()) {
                List<TestProxySanitizer> customSanitizers = new ArrayList<>();
                customSanitizers.add(new TestProxySanitizer("value", "-----BEGIN PRIVATE KEY-----\\n(.+\\n)*-----END PRIVATE KEY-----\\n", "-----BEGIN PRIVATE KEY-----\\nREDACTED\\n-----END PRIVATE KEY-----\\n", TestProxySanitizerType.BODY_KEY));
                interceptorManager.addSanitizers(customSanitizers);
            }
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        CertificateClientBuilder builder = new CertificateClientBuilder()
            .vaultUrl(endpoint)
            .serviceVersion(serviceVersion)
            .credential(credential)
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    @Test
    public abstract void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    static void createCertificateRunner(Consumer<CertificatePolicy> testRunner) {
        final CertificatePolicy certificatePolicy = CertificatePolicy.getDefault();

        testRunner.accept(certificatePolicy);
    }

    @Test
    public abstract void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    static void updateCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();

        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        testRunner.accept(tags, updatedTags);
    }

    @Test
    public abstract void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    static void updateDisabledCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();

        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        testRunner.accept(tags, updatedTags);
    }

    @Test
    public abstract void getCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateSpecificVersionRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void deleteCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getDeletedCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void getDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void recoverDeletedKeyRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void recoverDeletedCertificateNotFound(HttpClient httpClient,
                                                           CertificateServiceVersion serviceVersion);

    @Test
    public abstract void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void backupCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void restoreCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);


    void cancelCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void deleteCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void updateCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept(testResourceNamer.randomName(TEST_CERTIFICATE_NAME, 25));
    }

    @Test
    public abstract void restoreCertificateFromMalformedBackup(HttpClient httpClient,
                                                               CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;

        for (int i = 0; i < 2; i++) {
            certificateName = testResourceNamer.randomName("listCertKey", 25);

            certificates.add(certificateName);
        }

        testRunner.accept(certificates);
    }

    @Test
    public abstract void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listPropertiesOfCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;

        for (int i = 0; i < 2; i++) {
            certificateName = testResourceNamer.randomName("listCertKey", 25);

            certificates.add(certificateName);
        }

        testRunner.accept(certificates);
    }

    @Test
    public abstract void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void createIssuerRunner(Consumer<CertificateIssuer> testRunner) {
        final CertificateIssuer certificateIssuer = setupIssuer(testResourceNamer.randomName("testIssuer", 25));

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {
        final CertificateIssuer certificateIssuer = setupIssuer(testResourceNamer.randomName("testIssuer", 25));

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteCertificateIssuerNotFound(HttpClient httpClient,
                                                         CertificateServiceVersion serviceVersion);

    void deleteCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {
        final CertificateIssuer certificateIssuer = setupIssuer(testResourceNamer.randomName("testIssuer", 25));

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listCertificateIssuersRunner(Consumer<HashMap<String, CertificateIssuer>> testRunner) {
        HashMap<String, CertificateIssuer> certificateIssuers = new HashMap<>();
        String certificateIssuerName;

        for (int i = 0; i < 10; i++) {
            certificateIssuerName = testResourceNamer.randomName("listCertIssuer", 25);

            certificateIssuers.put(certificateIssuerName, setupIssuer(certificateIssuerName));
        }

        testRunner.accept(certificateIssuers);
    }

    void updateIssuerRunner(BiConsumer<CertificateIssuer, CertificateIssuer> testRunner) {
        String issuerName = testResourceNamer.randomName("testIssuer", 25);
        final CertificateIssuer certificateIssuer = setupIssuer(issuerName);
        final CertificateIssuer issuerForUpdate = new CertificateIssuer(issuerName, "Test")
            .setAdministratorContacts(Arrays.asList(new AdministratorContact()
                .setFirstName("otherFirst")
                .setLastName("otherLast")
                .setEmail("otherFirst.otherLast@hotmail.com")
                .setPhone("000-000-0000")))
            .setAccountId("otherIssuerAccountId")
            .setEnabled(false)
            .setOrganizationId("otherOrgId")
            .setPassword("fakePasswordPlaceholder");

        testRunner.accept(certificateIssuer, issuerForUpdate);
    }

    @Test
    public abstract void setContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificateOperationNotFound(HttpClient httpClient,
                                                         CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    static CertificateContact setupContact() {
        return new CertificateContact()
            .setName("name")
            .setEmail("first.last@gmail.com")
            .setPhone("000-000-0000");
    }

    static void validateContact(CertificateContact expected, CertificateContact actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPhone(), actual.getPhone());
    }

    @Test
    public abstract void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listDeletedCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            certificates.add(testResourceNamer.randomName("listDeletedCert", 25));
        }

        testRunner.accept(certificates);
    }

    @Test
    public abstract void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void importCertificateRunner(Consumer<ImportCertificateOptions> testRunner) {
        String certificatePassword = "fakePasswordPlaceholder";
        String certificateName = testResourceNamer.randomName("importCertPkcs", 25);
        HashMap<String, String> tags = new HashMap<>();

        tags.put("key", "val");

        ImportCertificateOptions importCertificateOptions =
            new ImportCertificateOptions(certificateName, Base64.getDecoder().decode(FAKE_CERTIFICATE))
                .setPassword(certificatePassword)
                .setEnabled(true)
                .setTags(tags);

        testRunner.accept(importCertificateOptions);
    }

    @Test
    public abstract void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion)
        throws IOException;

    void importPemCertificateRunner(Consumer<ImportCertificateOptions> testRunner) throws IOException {
        byte[] certificateContent = FAKE_PEM_CERTIFICATE.getBytes();

        String certificateName = testResourceNamer.randomName("importCertPem", 25);
        HashMap<String, String> tags = new HashMap<>();
        tags.put("key", "val");
        ImportCertificateOptions importCertificateOptions =
            new ImportCertificateOptions(certificateName, certificateContent)
                .setPolicy(new CertificatePolicy("Self", "CN=AzureSDK")
                    .setContentType(CertificateContentType.PEM))
                .setEnabled(true)
                .setTags(tags);
        testRunner.accept(importCertificateOptions);
    }

    @Test
    public abstract void mergeCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);
    protected PrivateKey loadPrivateKey(String filename)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] keyBytes = Files.readAllBytes(Paths.get("src", "test", "resources", filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);
    }

    static CertificateIssuer setupIssuer(String issuerName) {
        return new CertificateIssuer(issuerName, "Test")
            .setAdministratorContacts(Collections.singletonList(new AdministratorContact()
                .setFirstName("first")
                .setLastName("last")
                .setEmail("first.last@hotmail.com")
                .setPhone("000-000-0000")))
            .setAccountId("issuerAccountId")
            .setEnabled(true)
            .setOrganizationId("orgId")
            .setPassword("fakePasswordPlaceholder");
    }

    X509Certificate loadCerToX509Certificate(byte[] certificate) throws CertificateException, IOException {
        assertNotNull(certificate);

        ByteArrayInputStream cerStream = new ByteArrayInputStream(certificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);

        cerStream.close();

        return x509Certificate;
    }

    static void assertIssuerCreatedCorrectly(CertificateIssuer expected, CertificateIssuer actual) {
        assertEquals(expected.getAccountId(), actual.getAccountId());
        assertEquals(expected.isEnabled(), actual.isEnabled());
        assertNotNull(actual.getCreatedOn());
        assertNotNull(actual.getUpdatedOn());
        assertNotNull(actual.getId());
        assertTrue(actual.getId().length() > 0);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertEquals(expected.getAdministratorContacts().size(), actual.getAdministratorContacts().size());
    }

    static void assertIssuerUpdatedCorrectly(CertificateIssuer expected, CertificateIssuer actual) {
        assertNotEquals(expected.getAccountId(), actual.getAccountId());
        assertNotEquals(expected.isEnabled(), actual.isEnabled());
        assertNotNull(actual.getUpdatedOn());
        assertNotNull(actual.getCreatedOn());
        assertNotNull(actual.getId());
        assertTrue(actual.getId().length() > 0);
        assertEquals(expected.getName(), actual.getName());
        assertNotEquals(expected.getOrganizationId(), actual.getOrganizationId());
        assertEquals(expected.getAdministratorContacts().size(), actual.getAdministratorContacts().size());
    }

    static CertificatePolicy setupPolicy() {
        return new CertificatePolicy(WellKnownIssuerNames.SELF, "CN=default")
            .setKeyUsage(CertificateKeyUsage.KEY_CERT_SIGN, CertificateKeyUsage.KEY_AGREEMENT)
            .setContentType(CertificateContentType.PKCS12)
            .setExportable(true)
            .setKeyType(CertificateKeyType.EC)
            .setCertificateTransparent(false)
            .setEnabled(true)
            .setKeyCurveName(CertificateKeyCurveName.P_384)
            .setKeyReusable(true)
            .setValidityInMonths(24)
            .setLifetimeActions(new LifetimeAction(CertificatePolicyAction.AUTO_RENEW).setDaysBeforeExpiry(40));
    }

    static void assertPolicy(CertificatePolicy expected, CertificatePolicy actual) {
        assertEquals(expected.getKeyType(), actual.getKeyType());
        assertEquals(expected.getContentType(), actual.getContentType());
        assertNotNull(actual.getCreatedOn());
        assertEquals(expected.getIssuerName(), actual.getIssuerName());
        assertEquals(expected.getKeyCurveName(), actual.getKeyCurveName());
        assertEquals(expected.isExportable(), actual.isExportable());
        assertEquals(expected.isCertificateTransparent(), actual.isCertificateTransparent());
        assertEquals(expected.isEnabled(), actual.isEnabled());
        assertEquals(expected.isKeyReusable(), actual.isKeyReusable());
        assertEquals(expected.getValidityInMonths(), actual.getValidityInMonths());
        assertEquals(expected.getLifetimeActions().size(), actual.getLifetimeActions().size());
        assertEquals(expected.getKeyUsage().size(), actual.getKeyUsage().size());
    }

    static void assertCertificate(KeyVaultCertificate expected, KeyVaultCertificate actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getKeyId(), actual.getKeyId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSecretId(), actual.getSecretId());
        assertEquals(expected.getProperties().getVersion(), actual.getProperties().getVersion());
        assertEquals(expected.getProperties().getCreatedOn(), actual.getProperties().getCreatedOn());
        assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getProperties().getRecoveryLevel(), actual.getProperties().getRecoveryLevel());
        TestUtils.assertArraysEqual(expected.getProperties().getX509Thumbprint(),
            actual.getProperties().getX509Thumbprint());
        TestUtils.assertArraysEqual(expected.getCer(), actual.getCer());
    }

    public static String getEndpoint() {
        return Objects.requireNonNull(
            Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080"));
    }

    static void assertResponseException(Runnable exceptionThrower,
                                        Class<? extends HttpResponseException> expectedExceptionType,
                                        int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (HttpResponseException e) {
            assertResponseException(e, expectedExceptionType, expectedStatusCode);
        }
    }

    static void assertResponseException(Throwable exception,
        Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    static void assertResponseException(HttpResponseException exception,
        Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, exception.getResponse().getStatusCode());
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients().forEach(httpClient -> Arrays.stream(CertificateServiceVersion.values())
            .filter(CertificateClientTestBase::shouldServiceVersionBeTested)
            .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));

        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link CertificateServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check.
     *
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(CertificateServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return CertificateServiceVersion.getLatest().equals(serviceVersion);
        }

        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }

        String[] configuredServiceVersionList = SERVICE_VERSION_FROM_ENV.split(",");

        return Arrays.stream(configuredServiceVersionList)
            .anyMatch(configuredServiceVersion -> serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }

    static void validateMapResponse(Map<String, String> expected, Map<String, String> returned) {
        for (String key : expected.keySet()) {
            String val = returned.get(key);
            String expectedVal = expected.get(key);

            assertEquals(expectedVal, val);
        }
    }
}

