// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.util.stream.Stream;

import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.AdministratorContact;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.WellKnownIssuerNames;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class CertificateClientTestBase extends TestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";
    private static final String AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_CERTIFICATE_SERVICE_VERSIONS);
    private static final String AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL = "ALL";

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_ID");
            String clientKey = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_CLIENT_SECRET");
            String tenantId = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(SDK_NAME, SDK_VERSION,
            Configuration.getGlobalConfiguration().clone(), serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, CertificateClientBuilder.KEY_VAULT_SCOPE));
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
                                    .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                    .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
                                    .build();

        return pipeline;
    }

    @Test
    public abstract void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void createCertificateRunner(Consumer<CertificatePolicy> testRunner) {

        final CertificatePolicy certificatePolicy = CertificatePolicy.getDefault();

        testRunner.accept(certificatePolicy);
    }

    @Test
    public abstract void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test public abstract void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void updateCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");


        testRunner.accept(tags, updatedTags);
    }


    @Test
    public abstract void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void updateDisabledCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {

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
        testRunner.accept(generateResourceId("testCertificate4"));
    }

    @Test
    public abstract void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateSpecificVersionRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate9"));
    }

    @Test
    public abstract void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void deleteCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCert5"));
    }

    @Test
    public abstract void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getDeletedCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCert6"));
    }

    @Test
    public abstract void getDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void recoverDeletedKeyRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCert7"));
    }

    @Test
    public abstract void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void backupCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCert8"));
    }

    @Test
    public abstract void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void restoreCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate9"));
    }

    @Test
    public abstract void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate10"));
    }

    @Test
    public abstract void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);


    void cancelCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate11"));
    }

    @Test
    public abstract void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void deleteCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate12"));
    }

    @Test
    public abstract void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate13"));
    }

    @Test
    public abstract void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void updateCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept(generateResourceId("testCertificate14"));
    }

    @Test
    public abstract void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;
        for (int i = 0; i < 2; i++) {
            certificateName = generateResourceId("listCertKey" + i);
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
            certificateName = generateResourceId("listCertKey" + i);
            certificates.add(certificateName);
        }
        testRunner.accept(certificates);
    }

    @Test
    public abstract void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void createIssuereRunner(Consumer<CertificateIssuer> testRunner) {
        final CertificateIssuer certificateIssuer = setupIssuer(generateResourceId("testIssuer01"));
        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void getCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {

        final CertificateIssuer certificateIssuer = setupIssuer(generateResourceId("testIssuer02"));

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void deleteCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {

        final CertificateIssuer certificateIssuer = setupIssuer(generateResourceId("testIssuer03"));

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listCertificateIssuersRunner(Consumer<HashMap<String, CertificateIssuer>> testRunner) {
        HashMap<String, CertificateIssuer> certificateIssuers = new HashMap<>();
        String certificateIssuerName;
        for (int i = 0; i < 10; i++) {
            certificateIssuerName = generateResourceId("listCertIssuer" + i);
            certificateIssuers.put(certificateIssuerName, setupIssuer(certificateIssuerName));
        }
        testRunner.accept(certificateIssuers);
    }

    @Test
    public abstract void setContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void listContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);


    @Test
    public abstract void deleteContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion);


    @Test
    public abstract void getCertificateOperatioNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    @Test
    public abstract void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);


    CertificateContact setupContact() {
        return new CertificateContact().setName("name").setEmail("first.last@gmail.com").setPhone("2323-31232");
    }

    Boolean validateContact(CertificateContact expected, CertificateContact actual) {
        return expected.getEmail().equals(actual.getEmail())
            && expected.getName().equals(actual.getName())
            && expected.getPhone().equals(actual.getPhone());
    }

    @Test
    public abstract void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listCertificateVersionsRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName = generateResourceId("listCertVersionTest");
        for (int i = 1; i < 5; i++) {
            certificates.add(certificateName);
        }

        testRunner.accept(certificates);
    }

    @Test
    public abstract void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void listDeletedCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;
        for (int i = 0; i < 3; i++) {
            certificateName = generateResourceId("listDeletedCertificate" + i);
            certificates.add(certificateName);
        }
        testRunner.accept(certificates);
    }

    @Test
    public abstract void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    void importCertificateRunner(Consumer<ImportCertificateOptions> testRunner) {
        String certificateContent = "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";
        String certificatePassword = "123";

        String certificateName = generateResourceId("importCertPkcs");
        HashMap<String, String> tags = new HashMap<>();
        tags.put("key", "val");
        ImportCertificateOptions importCertificateOptions = new ImportCertificateOptions(certificateName, Base64.getDecoder().decode(certificateContent))
            .setPassword(certificatePassword)
            .setEnabled(true)
            .setTags(tags);
        testRunner.accept(importCertificateOptions);
    }

    @Test
    public abstract  void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) throws IOException;

    void importPemCertificateRunner(Consumer<ImportCertificateOptions> testRunner) throws IOException {

        byte[] certificateContent = readCertificate("pemCert.pem");

        String certificateName = generateResourceId("importCertPem");
        HashMap<String, String> tags = new HashMap<>();
        tags.put("key", "val");
        ImportCertificateOptions importCertificateOptions = new ImportCertificateOptions(certificateName, certificateContent)
                                                                .setPolicy(new CertificatePolicy("Self", "CN=AzureSDK")
                                                                                .setContentType(CertificateContentType.PEM))
                                                                .setEnabled(true)
                                                                .setTags(tags);
        testRunner.accept(importCertificateOptions);
    }

    @Test
    public abstract  void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion);

    private byte[] readCertificate(String certName) throws IOException {
        String pemPath = getClass().getClassLoader().getResource(certName).getPath();
        String pemCert = "";
        BufferedReader br = new BufferedReader(new FileReader(pemPath));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                pemCert += line + "\n";
            }
        } finally {
            br.close();
        }
        return pemCert.getBytes();
    }

    CertificateIssuer setupIssuer(String issuerName) {
        return new CertificateIssuer(issuerName, "Test")
            .setAdministratorContacts(Arrays.asList(new AdministratorContact().setFirstName("first").setLastName("last").setEmail("first.last@hotmail.com").setPhone("12345")))
            .setAccountId("issuerAccountId")
            .setEnabled(true)
            .setOrganizationId("orgId")
            .setPassword("test123");
    }


    String toHexString(byte[] x5t) {
        if (x5t == null) {
            return "";
        }

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < x5t.length; i++) {
            String hex = Integer.toHexString(0xFF & x5t[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().replace("-", "");
    }

    X509Certificate loadCerToX509Certificate(KeyVaultCertificateWithPolicy certificate) throws CertificateException, IOException {
        assertNotNull(certificate.getCer());
        ByteArrayInputStream cerStream = new ByteArrayInputStream(certificate.getCer());
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
        cerStream.close();
        return x509Certificate;
    }


    Boolean validateIssuer(CertificateIssuer expected, CertificateIssuer actual) {
        return expected.getAccountId().equals(actual.getAccountId())
            && expected.isEnabled().equals(actual.isEnabled())
            && (actual.getCreatedOn() != null)
            && (actual.getUpdatedOn() != null)
            && (actual.getId() != null)
            && (actual.getId().length() > 0)
            && expected.getName().equals(actual.getName())
            && expected.getOrganizationId().equals(actual.getOrganizationId())
            && expected.getAdministratorContacts().size() == actual.getAdministratorContacts().size();
    }

    CertificatePolicy setupPolicy() {
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

    boolean validatePolicy(CertificatePolicy expected, CertificatePolicy actual) {
        return expected.getKeyType().equals(actual.getKeyType())
            && expected.getContentType().equals(actual.getContentType())
            && actual.getCreatedOn() != null
            && expected.getIssuerName().equals(actual.getIssuerName())
            && expected.getKeyCurveName().equals(actual.getKeyCurveName())
            && expected.isExportable().equals(actual.isExportable())
            && expected.isCertificateTransparent().equals(actual.isCertificateTransparent())
            && expected.isEnabled().equals(actual.isEnabled())
            && expected.isKeyReusable().equals(actual.isKeyReusable())
            && expected.getValidityInMonths().equals(actual.getValidityInMonths())
            && expected.getLifetimeActions().size() == actual.getLifetimeActions().size()
            && expected.getKeyUsage().size() == actual.getKeyUsage().size();
    }

    boolean validateCertificate(KeyVaultCertificate expected, KeyVaultCertificate actual) {
        return expected.getId().equals(actual.getId())
            && expected.getKeyId().equals(actual.getKeyId())
            && expected.getName().equals(actual.getName())
            && expected.getSecretId().equals(actual.getSecretId())
            && expected.getProperties().getVersion().equals(actual.getProperties().getVersion())
            && expected.getProperties().getCreatedOn().equals(actual.getProperties().getCreatedOn())
            && expected.getProperties().getExpiresOn().equals(actual.getProperties().getExpiresOn())
            && expected.getProperties().getRecoveryLevel().equals(actual.getProperties().getRecoveryLevel())
            && expected.getProperties().getX509Thumbprint().length == actual.getProperties().getX509Thumbprint().length
            && expected.getCer().length == actual.getCer().length;
    }


    public String getEndpoint() {
        final String endpoint =
            Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "http://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    String generateResourceId(String suffix) {
        if (interceptorManager.isPlaybackMode()) {
            return suffix;
        }
        String id = UUID.randomUUID().toString();
        return suffix.length() > 0 ? id + "-" + suffix : id;
    }

    /**
     * Helper method to verify the error was a HttpRequestException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    public void sleepInRecordMode(long millis) {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(CertificateServiceVersion.values()).filter(
                    CertificateClientTestBase::shouldServiceVersionBeTested)
                    .forEach(serviceVersion -> {
                        argumentsList.add(Arguments.of(httpClient, serviceVersion)); });
            });
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
     * @param serviceVersion ServiceVersion needs to check
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
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }
}

