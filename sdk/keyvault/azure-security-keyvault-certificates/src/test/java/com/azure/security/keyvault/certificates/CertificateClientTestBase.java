// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.credential.AccessToken;
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
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.AdministratorContact;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class CertificateClientTestBase extends TestBase {

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

        TokenCredential credential;

        if (interceptorManager.isPlaybackMode()) {
            credential = resource -> Mono.just(new AccessToken("Some fake token", OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofMinutes(30))));
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        HttpClient httpClient;
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION,  Configuration.getGlobalConfiguration().clone(), CertificateServiceVersion.getLatest()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        policies.add(new BearerTokenAuthenticationPolicy(credential, CertificateAsyncClient.KEY_VAULT_SCOPE));
        policies.addAll(policies);
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

//    @Test
//    public abstract void listCertificateVersions();
//
//    void listCertificateVersionsRunner(Consumer<List<String>> testRunner) {
//        List<String> certificates = new ArrayList<>();
//        String keyName;
//        for (int i = 1; i < 5; i++) {
//            keyName = "listKeyVersion";
//            certificates.add(new CreateKeyOptions(keyName, RSA_KEY_TYPE)
//                .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
//        }
//
//        testRunner.accept(keys);
//    }

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

    CertificateIssuer setupIssuer(String issuerName) {
        return new CertificateIssuer(issuerName, "Test")
            .setAdministratorContacts(Arrays.asList(new AdministratorContact("first", "last", "first.last@hotmail.com", "12345")))
            .setAccountId("issuerAccountId")
            .setEnabled(true)
            .setOrganizationId("orgId")
            .setPassword("test123");
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

//    /**
//     * Helper method to verify that the Response matches what was expected. This method assumes a response status of 200.
//     *
//     * @param expected Key expected to be returned by the service
//     * @param response Response returned by the service, the body should contain a Key
//     */
//    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response) {
//        assertKeyEquals(expected, response, 200);
//    }
//
//    /**
//     * Helper method to verify that the RestResponse matches what was expected.
//     *
//     * @param expected ConfigurationSetting expected to be returned by the service
//     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
//     * @param expectedStatusCode Expected HTTP status code returned by the service
//     */
//    static void assertKeyEquals(CreateKeyOptions expected, Response<KeyVaultKey> response, final int expectedStatusCode) {
//        assertNotNull(response);
//        assertEquals(expectedStatusCode, response.getStatusCode());
//
//        assertKeyEquals(expected, response.getValue());
//    }
//
//    /**
//     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
//     *
//     * @param expected ConfigurationSetting expected to be returned by the service
//     * @param actual ConfigurationSetting contained in the RestResponse body
//     */
//    static void assertKeyEquals(CreateKeyOptions expected, KeyVaultKey actual) {
//        assertEquals(expected.getName(), actual.getName());
//        assertEquals(expected.getKeyType(), actual.getKey().getKeyType());
//        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
//        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
//    }

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

