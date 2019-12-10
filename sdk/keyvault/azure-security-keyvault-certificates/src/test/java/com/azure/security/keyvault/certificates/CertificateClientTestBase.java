// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.AdministratorContact;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import com.azure.security.keyvault.certificates.models.WellKnownIssuerNames;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class CertificateClientTestBase extends TestBase {
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        final String endpoint = interceptorManager.isPlaybackMode()
                                    ? "http://localhost:8080"
                                    : System.getenv("AZURE_KEYVAULT_ENDPOINT");

        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        HttpClient httpClient;
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(SDK_NAME, SDK_VERSION,
            Configuration.getGlobalConfiguration().clone(), CertificateServiceVersion.getLatest()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, CertificateAsyncClient.KEY_VAULT_SCOPE));
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
            policies.add(interceptorManager.getRecordPolicy());
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
                                    .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                    .httpClient(httpClient)
                                    .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void createCertificate();

    void createCertificateRunner(Consumer<CertificatePolicy> testRunner) {

        final CertificatePolicy certificatePolicy = CertificatePolicy.getDefault();

        testRunner.accept(certificatePolicy);
    }

    @Test
    public abstract void createCertificateEmptyName();

    @Test
    public abstract void createCertificateNullPolicy();

    @Test public abstract void createCertoificateNull();

    @Test
    public abstract void updateCertificate();

    void updateCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");


        testRunner.accept(tags, updatedTags);
    }


    @Test
    public abstract void updateDisabledCertificate();

    void updateDisabledCertificateRunner(BiConsumer<Map<String, String>, Map<String, String>> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");

        final Map<String, String> updatedTags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");


        testRunner.accept(tags, updatedTags);
    }

    @Test
    public abstract void getCertificate();

    void getCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate4");
    }

    @Test
    public abstract void getCertificateSpecificVersion();

    void getCertificateSpecificVersionRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate9");
    }

    @Test
    public abstract void getCertificateNotFound();

    @Test
    public abstract void deleteCertificate();

    void deleteCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept("testCert5");
    }

    @Test
    public abstract void deleteCertificateNotFound();

    @Test
    public abstract void getDeletedCertificate();

    void getDeletedCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept("testCert6");
    }

    @Test
    public abstract void getDeletedCertificateNotFound();

    @Test
    public abstract void recoverDeletedCertificate();

    void recoverDeletedKeyRunner(Consumer<String> testRunner) {
        testRunner.accept("testCert7");
    }

    @Test
    public abstract void recoverDeletedCertificateNotFound();

    @Test
    public abstract void backupCertificate();

    void backupCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept("testCert8");
    }

    @Test
    public abstract void backupCertificateNotFound();

    @Test
    public abstract void restoreCertificate();

    void restoreCertificateRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate9");
    }

    @Test
    public abstract void getCertificateOperation();

    void getCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate10");
    }

    @Test
    public abstract void cancelCertificateOperation();

    void cancelCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate11");
    }

    @Test
    public abstract void deleteCertificateOperation();

    void deleteCertificateOperationRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate12");
    }

    @Test
    public abstract void getCertificatePolicy();

    void getCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate13");
    }

    @Test
    public abstract void updateCertificatePolicy();

    void updateCertificatePolicyRunner(Consumer<String> testRunner) {
        testRunner.accept("testCertificate14");
    }


    @Test
    public abstract void restoreCertificateFromMalformedBackup();

    @Test
    public abstract void listCertificates();

    void listCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;
        for (int i = 0; i < 10; i++) {
            certificateName = "listCertKey" + i;
            certificates.add(certificateName);
        }
        testRunner.accept(certificates);
    }


    @Test
    public abstract void createIssuer();

    void createIssuereRunner(Consumer<CertificateIssuer> testRunner) {

        final CertificateIssuer certificateIssuer = setupIssuer("testIssuer01");

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void createIssuerEmptyName();

    @Test
    public abstract void createIssuerNullProvider();

    @Test
    public abstract void createIssuerNull();

    @Test
    public abstract void getCertificateIssuer();

    @Test
    public abstract void getCertificateIssuerNotFound();

    void getCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {

        final CertificateIssuer certificateIssuer = setupIssuer("testIssuer02");

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void deleteCertificateIssuer();

    @Test
    public abstract void deleteCertificateIssuerNotFound();

    void deleteCertificateIssuerRunner(Consumer<CertificateIssuer> testRunner) {

        final CertificateIssuer certificateIssuer = setupIssuer("testIssuer03");

        testRunner.accept(certificateIssuer);
    }

    @Test
    public abstract void listCertificateIssuers();

    void listCertificateIssuersRunner(Consumer<HashMap<String, CertificateIssuer>> testRunner) {
        HashMap<String, CertificateIssuer> certificateIssuers = new HashMap<>();
        String certificateIssuerName;
        for (int i = 0; i < 10; i++) {
            certificateIssuerName = "listCertIssuer" + i;
            certificateIssuers.put(certificateIssuerName, setupIssuer(certificateIssuerName));
        }
        testRunner.accept(certificateIssuers);
    }

    @Test
    public abstract void setContacts();

    @Test
    public abstract void listContacts();


    @Test
    public abstract void deleteContacts();


    @Test
    public abstract void getCertificateOperatioNotFound();

    @Test
    public abstract void getCertificatePolicyNotFound();


    CertificateContact setupContact() {
        return new CertificateContact("name", "first.last@gmail.com", "2323-31232");
    }

    Boolean validateContact(CertificateContact expected, CertificateContact actual) {
        return expected.getEmail().equals(actual.getEmail())
            && expected.getName().equals(actual.getName())
            && expected.getPhone().equals(actual.getPhone());
    }

    @Test
    public abstract void listCertificateVersions();

    void listCertificateVersionsRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;
        for (int i = 1; i < 5; i++) {
            certificateName = "listCertVersionTest";
            certificates.add(certificateName);
        }

        testRunner.accept(certificates);
    }

    @Test
    public abstract void listDeletedCertificates();

    void listDeletedCertificatesRunner(Consumer<List<String>> testRunner) {
        List<String> certificates = new ArrayList<>();
        String certificateName;
        for (int i = 0; i < 3; i++) {
            certificateName = "listDeletedCertificate" + i;
            certificates.add(certificateName);
        }
        testRunner.accept(certificates);
    }

    @Test
    public abstract void importCertificate();

    @Test
    public abstract  void mergeCertificateNotFound();

    void importCertificateRunner(Consumer<ImportCertificateOptions> testRunner) {
        String certificateContent = "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";
        String certificatePassword = "123";

        String certificateName = "importCertPkcs";
        HashMap<String, String> tags = new HashMap<>();
        tags.put("key", "val");
        ImportCertificateOptions importCertificateOptions = new ImportCertificateOptions(certificateName, Base64.getDecoder().decode(certificateContent))
            .setPassword(certificatePassword)
            .setEnabled(true)
            .setTags(tags);
        testRunner.accept(importCertificateOptions);
    }

    CertificateIssuer setupIssuer(String issuerName) {
        return new CertificateIssuer(issuerName, "Test")
            .setAdministratorContacts(Arrays.asList(new AdministratorContact("first", "last", "first.last@hotmail.com", "12345")))
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
            && (actual.getCreated() != null)
            && (actual.getUpdated() != null)
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
        final String endpoint = interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : "https://cameravault.vault.azure.net";
//            : System.getenv("AZURE_KEYVAULT_ENDPOINT");
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
}

