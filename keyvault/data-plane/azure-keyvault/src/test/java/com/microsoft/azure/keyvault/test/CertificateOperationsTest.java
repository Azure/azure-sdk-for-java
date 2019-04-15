// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.CertificateIdentifier;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.AdministratorDetails;
import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.CertificateAttributes;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.Contact;
import com.microsoft.azure.keyvault.models.Contacts;
import com.microsoft.azure.keyvault.models.IssuerBundle;
import com.microsoft.azure.keyvault.models.IssuerCredentials;
import com.microsoft.azure.keyvault.models.IssuerParameters;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.OrganizationDetails;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.keyvault.models.CertificateOperation;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretProperties;
import com.microsoft.azure.keyvault.models.X509CertificateProperties;
import com.microsoft.azure.keyvault.requests.CreateCertificateRequest;
import com.microsoft.azure.keyvault.requests.ImportCertificateRequest;
import com.microsoft.azure.keyvault.requests.SetCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateOperationRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificatePolicyRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateRequest;

public class CertificateOperationsTest extends KeyVaultClientIntegrationTestBase {

    static final String ALGO_RSA = "RSA";

    static final String X509 = "X.509";

    static final String PKCS12 = "PKCS12";

    static final String MIME_PKCS12 = "application/x-pkcs12";

    static final String MIME_PEM = "application/x-pem-file";

    static final String ISSUER_SELF = "Self";

    static final String ISSUER_TEST = "Test";

    static final String ISSUER_UNKNOWN = "Unknown";

    static final String STATUS_IN_PROGRESS = "inProgress";

    static final String STATUS_COMPLETED = "Completed";

    static final Base64 BASE_64 = new Base64(-1, null, true);

    static final Pattern PRIVATE_KEY = Pattern.compile("-{5}BEGIN PRIVATE KEY-{5}(?:\\s|\\r|\\n)+"
            + "([a-zA-Z0-9+/=\r\n]+)" + "-{5}END PRIVATE KEY-{5}(?:\\s|\\r|\\n)+");

    static final Pattern CERTIFICATE = Pattern.compile("-{5}BEGIN CERTIFICATE-{5}(?:\\s|\\r|\\n)+"
            + "([a-zA-Z0-9+/=\r\n]+)" + "-{5}END CERTIFICATE-{5}(?:\\s|\\r|\\n)+");

    private static final int MAX_CERTS = 4;
    private static final int PAGELIST_MAX_CERTS = 3;

    private static final Map<String, String> TAGS = new HashMap<String, String>();

    /**
     * Create a self-signed certificate in PKCS12 format (which includes the
     * private key) certificate.
     *
     * @throws Exception
     */
    @Test
    public void createSelfSignedCertificatePkcs12ForCertificateOperationsTest() throws Exception {
        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties()
                                        .withContentType(MIME_PKCS12);

        String subjectName = "CN=SelfSignedJavaPkcs12";
        X509CertificateProperties x509Properties = new X509CertificateProperties()
                    .withSubject(subjectName)
                    .withValidityInMonths(12);

        // Set issuer to "Self"
        IssuerParameters issuerParameters = new IssuerParameters()
                    .withName(ISSUER_SELF);

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        Attributes attribute = new CertificateAttributes()
                .withEnabled(true)
                .withExpires(new DateTime().withYear(2050).withMonthOfYear(1))
                .withNotBefore(new DateTime().withYear(2000).withMonthOfYear(1));

        String vaultUri = getVaultUri();
        String certificateName = "createSelfSignedJavaPkcs12";

        CreateCertificateRequest createCertificateRequest =
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                        .withPolicy(certificatePolicy)
                        .withAttributes(attribute)
                        .withTags(TAGS)
                        .build();

        CertificateOperation certificateOperation = keyVaultClient.createCertificate(createCertificateRequest);

        Assert.assertNotNull(certificateOperation);
        Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

        CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
        validateCertificateBundle(certificateBundle, certificatePolicy);
        compareAttributes(attribute, createCertificateRequest.certificateAttributes());

        // Load the CER part into X509Certificate object
        X509Certificate x509Certificate = loadCerToX509Certificate(certificateBundle);

        Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
        Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

        // Retrieve the secret backing the certificate
        SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
        SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier());
        Assert.assertTrue(secret.managed());

        // Retrieve the key backing the certificate
        KeyIdentifier keyIdentifier = certificateBundle.keyIdentifier();
        KeyBundle keyBundle = keyVaultClient.getKey(keyIdentifier.baseIdentifier());
        Assert.assertTrue(keyBundle.managed());

        // Load the secret into a KeyStore
        String secretPassword = "";
        KeyStore keyStore = loadSecretToKeyStore(secret, secretPassword);

        // Validate the certificate and key in the KeyStore
        validateCertificateKeyInKeyStore(keyStore, x509Certificate, secretPassword);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        Assert.assertNotNull(deletedCertificateBundle);

        pollOnCertificateDeletion(getVaultUri(), certificateName);
        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Create a self-signed certificate in PEM format (which includes the
     * private key) certificate.
     *
     * @throws Exception
     */
    @Test
    public void createSelfSignedCertificatePemForCertificateOperationsTest() throws Exception {
        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties()
                    .withContentType(MIME_PEM);

        String subjectName = "CN=SelfSignedJavaPem";
        X509CertificateProperties x509Properties = new X509CertificateProperties()
                    .withSubject(subjectName)
                    .withValidityInMonths(12);

        // Set issuer to "Self"
        IssuerParameters issuerParameters = new IssuerParameters()
                    .withName(ISSUER_SELF);

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        String vaultUri = getVaultUri();
        String certificateName = "SelfSignedJavaPem2";
        CertificateOperation certificateOperation = keyVaultClient.createCertificate(
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicy)
                    .build());

        Assert.assertNotNull(certificateOperation);
        Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

        CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
        validateCertificateBundle(certificateBundle, certificatePolicy);

        validatePem(certificateBundle, subjectName);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        Assert.assertNotNull(deletedCertificateBundle);

        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        pollOnCertificateDeletion(getVaultUri(), certificateName);

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Create a test-issuer issued certificate in PKCS12 format (which includes
     * the private key) certificate.
     *
     * @throws Exception
     */
    @Test
    public void createCertificatePkcs12ForCertificateOperationsTest() throws Exception {
        // Construct organization administrator details
        AdministratorDetails administratorDetails = new AdministratorDetails()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@contoso.com")
                    .withPhone("1234567890");

        // Construct organization details
        List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
        administratorsDetails.add(administratorDetails);
        OrganizationDetails organizationDetails = new OrganizationDetails()
                    .withAdminDetails(administratorsDetails);

        // Construct certificate issuer credentials
        IssuerCredentials credentials = new IssuerCredentials()
                    .withAccountId("account1")
                    .withPassword("Pa$$w0rd");

        String certificateIssuerName = "createCertificateJavaPkcs12Issuer01";
        IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
                new SetCertificateIssuerRequest
                    .Builder(getVaultUri(), certificateIssuerName, ISSUER_TEST)
                    .withCredentials(credentials)
                    .withOrganizationDetails(organizationDetails)
                    .build());

        validateCertificateIssuer(createdCertificateIssuer, certificateIssuerName);

        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties()
                    .withContentType(MIME_PKCS12);

        String subjectName = "CN=TestJavaPkcs12";
        X509CertificateProperties x509Properties = new X509CertificateProperties()
                    .withSubject(subjectName)
                    .withValidityInMonths(12);

        // Set issuer reference to the created issuer
        IssuerParameters issuerParameters = new IssuerParameters();
        issuerParameters.withName(createdCertificateIssuer.issuerIdentifier().name());

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        String vaultUri = getVaultUri();
        String certificateName = "createTestJavaPkcs12";
        CertificateOperation certificateOperation = keyVaultClient.createCertificate(
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicy)
                    .build());

        Assert.assertNotNull(certificateOperation);
        Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));


        CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
        validateCertificateBundle(certificateBundle, certificatePolicy);

        // Load the CER part into X509Certificate object
        X509Certificate x509Certificate = loadCerToX509Certificate(certificateBundle);

        Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
        Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

        // Retrieve the secret backing the certificate
        SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
        SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier());
        Assert.assertTrue(secret.managed());

        // Load the secret into a KeyStore
        String secretPassword = "";
        KeyStore keyStore = loadSecretToKeyStore(secret, secretPassword);

        // Validate the certificate and key in the KeyStore
        validateCertificateKeyInKeyStore(keyStore, x509Certificate, secretPassword);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        Assert.assertNotNull(deletedCertificateBundle);

        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        pollOnCertificateDeletion(getVaultUri(), certificateName);

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Create a test-issuer certificate in PEM format (which includes the
     * private key) certificate.
     *
     * @throws Exception
     */
    @Test
    public void createCertificatePemForCertificateOperationsTest() throws Exception {
        // Construct organization administrator details
        AdministratorDetails administratorDetails = new AdministratorDetails()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@contoso.com")
                    .withPhone("1234567890");

        // Construct organization details
        OrganizationDetails organizationDetails = new OrganizationDetails();
        List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
        administratorsDetails.add(administratorDetails);
        organizationDetails.withAdminDetails(administratorsDetails);

        // Construct certificate issuer credentials
        IssuerCredentials credentials = new IssuerCredentials()
                    .withAccountId("account1")
                    .withPassword("Pa$$w0rd");

        String certificateIssuerName = "createCertificateJavaPemIssuer01";
        IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
                new SetCertificateIssuerRequest
                    .Builder(getVaultUri(),    certificateIssuerName, ISSUER_TEST)
                    .withCredentials(credentials)
                    .withOrganizationDetails(organizationDetails)
                    .build());
        validateCertificateIssuer(createdCertificateIssuer, certificateIssuerName);

        // Set content type to indicate the certificate is PEM format.
        SecretProperties secretProperties = new SecretProperties();
        secretProperties.withContentType(MIME_PEM);

        X509CertificateProperties x509Properties = new X509CertificateProperties();
        String subjectName = "CN=TestJavaPem";
        x509Properties.withSubject(subjectName);
        x509Properties.withValidityInMonths(12);

        // Set issuer reference to the created issuer
        IssuerParameters issuerParameters = new IssuerParameters();
        issuerParameters.withName(createdCertificateIssuer.issuerIdentifier().name());

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        String vaultUri = getVaultUri();
        String certificateName = "createTestJavaPem";
        CertificateOperation certificateOperation = keyVaultClient.createCertificate(
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicy)
                    .build());

        Assert.assertNotNull(certificateOperation);
        Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

        CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
        validateCertificateBundle(certificateBundle, certificatePolicy);

        validatePem(certificateBundle, subjectName);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        Assert.assertNotNull(deletedCertificateBundle);


        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        pollOnCertificateDeletion(getVaultUri(), certificateName);

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Create a certificate signing request with key in Key Vault.
     * @throws Exception
     */
    @Test
    public void createCsrForCertificateOperationsTest() throws Exception {
        SecretProperties secretProperties = new SecretProperties();
        secretProperties.withContentType(MIME_PKCS12);

        X509CertificateProperties x509Properties = new X509CertificateProperties();
        String subjectName = "CN=ManualEnrollmentJava";
        x509Properties.withSubject(subjectName);
        x509Properties.withValidityInMonths(12);

        // Set issuer to "Unknown"
        IssuerParameters issuerParameters = new IssuerParameters();
        issuerParameters.withName(ISSUER_UNKNOWN);

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        String vaultUri = getVaultUri();
        String certificateName = "createManualEnrollmentJava";
        CertificateOperation certificateOperation = keyVaultClient.createCertificate(
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicy)
                    .build());

        Assert.assertNotNull(certificateOperation);
        Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));
        Assert.assertNotNull(certificateOperation.csr());

        String csr = keyVaultClient.getPendingCertificateSigningRequest(vaultUri, certificateName);
        Assert.assertNotNull(csr);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        Assert.assertNotNull(deletedCertificateBundle);

        pollOnCertificateDeletion(getVaultUri(), certificateName);

        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Cancel the certificate create asynchronously
     * @throws Exception
     *
     */
    @Test
    public void certificateAsyncRequestCancellationForCertificateOperationsTest() throws Exception {
        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties()
                                        .withContentType(MIME_PKCS12);

        String subjectName = "CN=SelfSignedJavaPkcs12";
        X509CertificateProperties x509Properties = new X509CertificateProperties()
                                    .withSubject(subjectName)
                                    .withValidityInMonths(12);

        // Set issuer to "Self"
        IssuerParameters issuerParameters = new IssuerParameters()
                                    .withName(ISSUER_SELF);

        CertificatePolicy certificatePolicy = new CertificatePolicy()
                    .withSecretProperties(secretProperties)
                    .withIssuerParameters(issuerParameters)
                    .withX509CertificateProperties(x509Properties);

        String vaultUri = getVaultUri();
        String certificateName = "cancellationRequestedCertJava";
        keyVaultClient.createCertificate(
                new CreateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicy)
                    .build());

        CertificateOperation cancelledCertificateOperation = keyVaultClient.updateCertificateOperation(
                new UpdateCertificateOperationRequest
                    .Builder(vaultUri, certificateName, true)
                    .build());

        Assert.assertNotNull(cancelledCertificateOperation);
        Assert.assertTrue(cancelledCertificateOperation.cancellationRequested());

        keyVaultClient.deleteCertificateOperation(getVaultUri(), certificateName);
        keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        pollOnCertificateDeletion(getVaultUri(), certificateName);
        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(20000);
    }

    /**
     * Import a PKCS12 format (which includes the private key) certificate.
     */
    @Test
    public void importCertificatePkcs12ForCertificateOperationsTest() throws Exception {
        String certificateContent = "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";
        String certificatePassword = "123";

        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties().withContentType(MIME_PKCS12);
        CertificatePolicy certificatePolicy = new CertificatePolicy().withSecretProperties(secretProperties);
        Attributes attribute = new CertificateAttributes().withEnabled(true);

        String vaultUri = getVaultUri();
        String certificateName = "importCertPkcs";
        CertificateBundle certificateBundle = keyVaultClient.importCertificate(
                new ImportCertificateRequest
                    .Builder(vaultUri, certificateName, certificateContent)
                    .withPassword(certificatePassword)
                    .withPolicy(certificatePolicy)
                    .withAttributes(attribute)
                    .withTags(TAGS)
                    .build());

        // Validate the certificate bundle created
        validateCertificateBundle(certificateBundle, certificatePolicy);
        Assert.assertTrue(toHexString(certificateBundle.x509Thumbprint()).equalsIgnoreCase("7cb8b7539d87ba7215357b9b9049dff2d3fa59ba"));
        Assert.assertEquals(attribute.enabled(), certificateBundle.attributes().enabled());

        // Load the CER part into X509Certificate object
        X509Certificate x509Certificate = loadCerToX509Certificate(certificateBundle);

        Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals("CN=KeyVaultTest"));
        Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals("CN=Root Agency"));

        // Retrieve the secret backing the certificate
        SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
        SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier());
        Assert.assertTrue(secret.managed());

        // Load the secret into a KeyStore
        String secretPassword = "";
        KeyStore keyStore = loadSecretToKeyStore(secret, secretPassword);

        // Validate the certificate and key in the KeyStore
        validateCertificateKeyInKeyStore(keyStore, x509Certificate, secretPassword);

        CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        pollOnCertificateDeletion(getVaultUri(), certificateName);

        try {
            keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateNotFound", e.body().error().code());
        }

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(10000);
    }

       /**
     * Import a PKCS12 format (which includes the private key) certificate.
     */
    @Test
    public void certificateUpdateForCertificateOperationsTest() throws Exception {
        String certificateContent = "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";
        String certificatePassword = "123";

        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties().withContentType(MIME_PKCS12);
        CertificatePolicy certificatePolicy = new CertificatePolicy().withSecretProperties(secretProperties);

        String vaultUri = getVaultUri();
        String certificateName = "updateCertJava";
        keyVaultClient.importCertificate(
                new ImportCertificateRequest
                    .Builder(vaultUri, certificateName, certificateContent)
                    .withPassword(certificatePassword)
                    .withPolicy(certificatePolicy)
                    .build());


        Attributes attribute = new CertificateAttributes()
                .withExpires(new DateTime().withYear(2050).withMonthOfYear(1))
                .withNotBefore(new DateTime().withYear(2000).withMonthOfYear(1));
        CertificateBundle updatedCertBundle = keyVaultClient.updateCertificate(
                new UpdateCertificateRequest
                    .Builder(vaultUri, certificateName)
                    .withAttributes(attribute.withEnabled(false))
                    .withTags(TAGS)
                    .build());
        Assert.assertEquals(attribute.enabled(), updatedCertBundle.attributes().enabled());
        Assert.assertEquals(TAGS.toString(), updatedCertBundle.tags().toString());

        CertificatePolicy certificatePolicyUpdate = certificatePolicy.withIssuerParameters(new IssuerParameters().withName(ISSUER_SELF));
        CertificatePolicy updatedCertificatePolicy = keyVaultClient.updateCertificatePolicy(
                new UpdateCertificatePolicyRequest
                    .Builder(vaultUri, certificateName)
                    .withPolicy(certificatePolicyUpdate)
                    .build());
        Assert.assertEquals(certificatePolicyUpdate.issuerParameters().name(), updatedCertificatePolicy.issuerParameters().name());

        CertificatePolicy policy = keyVaultClient.getCertificatePolicy(vaultUri, certificateName);
        Assert.assertEquals(certificatePolicyUpdate.issuerParameters().name(), policy.issuerParameters().name());

        keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        pollOnCertificateDeletion(getVaultUri(), certificateName);

        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
        SdkContext.sleep(10000);
    }

    /**
     * List certificates in a vault.
     */
    @Test
    public void listCertificatesForCertificateOperationsTest() throws Exception {
        String certificateName = "listCertificate";
        String certificateContent = "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";
        String certificatePassword = "123";

        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties();
        secretProperties.withContentType(MIME_PKCS12);
        CertificatePolicy certificatePolicy = new CertificatePolicy();
        certificatePolicy.withSecretProperties(secretProperties);

        HashSet<String> certificates = new HashSet<String>();
        for (int i = 0; i < MAX_CERTS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    CertificateBundle certificateBundle = keyVaultClient.importCertificate(
                            new ImportCertificateRequest
                                .Builder(getVaultUri(), certificateName + i, certificateContent)
                                .withPassword(certificatePassword)
                                .withPolicy(certificatePolicy)
                                .build());
                    CertificateIdentifier id = certificateBundle.certificateIdentifier();
                    certificates.add(id.baseIdentifier());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.body().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        SdkContext.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<CertificateItem> listResult = keyVaultClient.listCertificates(getVaultUri(), PAGELIST_MAX_CERTS);
        Assert.assertTrue(PAGELIST_MAX_CERTS >= listResult.currentPage().items().size());

        HashSet<String> toDelete = new HashSet<String>();

        for (CertificateItem item : listResult) {
            if (item != null) {
                CertificateIdentifier id = new CertificateIdentifier(item.id());
                toDelete.add(id.name());
                certificates.remove(item.id());
            }
        }

        Assert.assertEquals(0, certificates.size());

        for (String toDeleteCertificateName : toDelete) {
            keyVaultClient.deleteCertificate(getVaultUri(), toDeleteCertificateName);
            pollOnCertificateDeletion(getVaultUri(), toDeleteCertificateName);
            keyVaultClient.purgeDeletedCertificate(getVaultUri(), toDeleteCertificateName);
            SdkContext.sleep(10000);
        }
    }

    /**
     * List versions of a certificate in a vault.
     */
    @Test
    public void listCertificateVersionsForCertificateOperationsTest() throws Exception {
        String certificateName = "listCertificateVersions";
        String certificateContent = "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";
        String certificatePassword = "123";

        // Set content type to indicate the certificate is PKCS12 format.
        SecretProperties secretProperties = new SecretProperties();
        secretProperties.withContentType(MIME_PKCS12);
        CertificatePolicy certificatePolicy = new CertificatePolicy();
        certificatePolicy.withSecretProperties(secretProperties);

        HashSet<String> certificates = new HashSet<String>();
        for (int i = 0; i < MAX_CERTS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    CertificateBundle certificateBundle = keyVaultClient.importCertificate(
                            new ImportCertificateRequest
                                .Builder(getVaultUri(), certificateName, certificateContent)
                                .withPassword(certificatePassword)
                                .withPolicy(certificatePolicy)
                                .build());
                    CertificateIdentifier id = certificateBundle.certificateIdentifier();
                    certificates.add(id.identifier());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.body().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        SdkContext.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<CertificateItem> listResult = keyVaultClient.listCertificateVersions(getVaultUri(), certificateName, PAGELIST_MAX_CERTS);
        Assert.assertTrue(PAGELIST_MAX_CERTS >= listResult.currentPage().items().size());

        listResult = keyVaultClient.listCertificateVersions(getVaultUri(), certificateName);

        for (CertificateItem item : listResult) {
            if (item != null) {
                certificates.remove(item.id());
            }
        }

        Assert.assertEquals(0, certificates.size());

        keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
        pollOnCertificateDeletion(getVaultUri(), certificateName);
        keyVaultClient.purgeDeletedCertificate(getVaultUri(), certificateName);
    }

    /**
     * CRUD for Certificate issuers
     */
    @Test
    public void issuerCrudOperationsForCertificateOperationsTest() throws Exception {
        // Construct organization administrator details
        AdministratorDetails administratorDetails = new AdministratorDetails()
                    .withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@contoso.com")
                    .withPhone("1234567890");

        // Construct organization details
        OrganizationDetails organizationDetails = new OrganizationDetails();
        List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
        administratorsDetails.add(administratorDetails);
        organizationDetails.withAdminDetails(administratorsDetails);

        // Construct certificate issuer credentials
        IssuerCredentials credentials = new IssuerCredentials()
                    .withAccountId("account1")
                    .withPassword("Pa$$w0rd");

        IssuerBundle certificateIssuer = new IssuerBundle()
                    .withProvider(ISSUER_TEST)
                    .withCredentials(credentials)
                    .withOrganizationDetails(organizationDetails);

        IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
                new SetCertificateIssuerRequest
                    .Builder(getVaultUri(), "issuer1", certificateIssuer.provider())
                    .withCredentials(certificateIssuer.credentials())
                    .withOrganizationDetails(certificateIssuer.organizationDetails())
                    .build());

        validateCertificateIssuer(certificateIssuer, createdCertificateIssuer);

        String certificateIssuerName = createdCertificateIssuer.issuerIdentifier().name();
        IssuerBundle retrievedCertificateIssuer = keyVaultClient.getCertificateIssuer(getVaultUri(),
                certificateIssuerName);

        validateCertificateIssuer(certificateIssuer, retrievedCertificateIssuer);

        IssuerCredentials updatedCredentials = new IssuerCredentials()
                    .withAccountId("account2")
                    .withPassword("Secur!Ty");

        retrievedCertificateIssuer.withCredentials(updatedCredentials);
        IssuerBundle updatedCertificateIssuer = keyVaultClient.updateCertificateIssuer(
                new UpdateCertificateIssuerRequest
                    .Builder(getVaultUri(), certificateIssuerName)
                    .withProvider(ISSUER_TEST)
                    .withCredentials(updatedCredentials)
                    .withOrganizationDetails(retrievedCertificateIssuer.organizationDetails())
                    .withAttributes(retrievedCertificateIssuer.attributes())
                    .build());

        validateCertificateIssuer(retrievedCertificateIssuer, updatedCertificateIssuer);

        Assert.assertNotNull(updatedCertificateIssuer.organizationDetails());

        IssuerBundle deletedCertificateIssuer = keyVaultClient.deleteCertificateIssuer(getVaultUri(), certificateIssuerName);

        validateCertificateIssuer(updatedCertificateIssuer, deletedCertificateIssuer);

        try {
            keyVaultClient.getCertificateIssuer(getVaultUri(), certificateIssuerName);
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("CertificateIssuerNotFound", e.body().error().code());
        }
    }

    /**
     * CRUD for Certificate contacts
     * @throws Exception
     */
    @Test
    public void contactsCrudOperationsForCertificateOperationsTest() throws Exception {
        // Create
        Contact contact1 = new Contact();
        contact1.withName("James");
        contact1.withEmailAddress("james@contoso.com");
        contact1.withPhone("7777777777");

        Contact contact2 = new Contact();
        contact2.withName("Ethan");
        contact2.withEmailAddress("ethan@contoso.com");
        contact2.withPhone("8888888888");

        List<Contact> contacts = new ArrayList<Contact>();
        contacts.add(contact1);
        contacts.add(contact2);

        Contacts certificateContacts = new Contacts();
        certificateContacts.withContactList(contacts);
        Contacts createdCertificateContacts = keyVaultClient.setCertificateContacts(getVaultUri(), certificateContacts);
        Assert.assertNotNull(createdCertificateContacts);
        Assert.assertNotNull(createdCertificateContacts.contactList());
        Assert.assertTrue(createdCertificateContacts.contactList().size() == 2);
        Contact[] createContacts = createdCertificateContacts.contactList().toArray(new Contact[createdCertificateContacts.contactList().size()]);
        Assert.assertTrue(createContacts[0].name().equalsIgnoreCase("James"));
        Assert.assertTrue(createContacts[0].emailAddress().equalsIgnoreCase("james@contoso.com"));
        Assert.assertTrue(createContacts[0].phone().equalsIgnoreCase("7777777777"));
        Assert.assertTrue(createContacts[1].name().equalsIgnoreCase("Ethan"));
        Assert.assertTrue(createContacts[1].emailAddress().equalsIgnoreCase("ethan@contoso.com"));
        Assert.assertTrue(createContacts[1].phone().equalsIgnoreCase("8888888888"));

        // Get
        Contacts retrievedCertificateContacts = keyVaultClient.getCertificateContacts(getVaultUri());
        Assert.assertNotNull(retrievedCertificateContacts);
        Assert.assertNotNull(retrievedCertificateContacts.contactList());
        Assert.assertTrue(retrievedCertificateContacts.contactList().size() == 2);

        // Delete
        Contacts deletedCertificateContacts = keyVaultClient.deleteCertificateContacts(getVaultUri());
        Assert.assertNotNull(deletedCertificateContacts);
        Assert.assertNotNull(deletedCertificateContacts.contactList());
        Assert.assertTrue(deletedCertificateContacts.contactList().size() == 2);

        // Get after delete
        try {
            keyVaultClient.getCertificateContacts(getVaultUri());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error());
            Assert.assertEquals("ContactsNotFound", e.body().error().code());
        }
    }

    /**
     * Polls on a certificate operation for completion.
     *
     * @throws Exception
     */
    private static CertificateBundle pollOnCertificateOperation(CertificateOperation certificateOperation)
            throws Exception {

        // Wait for enrollment to complete. We will wait for 200 seconds
        int pendingPollCount = 0;
        while (pendingPollCount < 21) {
            String certificateName = certificateOperation.certificateOperationIdentifier().name();
            CertificateOperation pendingCertificateOperation = keyVaultClient
                    .getCertificateOperation(getVaultUri(), certificateName);
            if (pendingCertificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS)) {
                SdkContext.sleep(10000);
                pendingPollCount += 1;
                continue;
            }

            if (pendingCertificateOperation.status().equalsIgnoreCase(STATUS_COMPLETED)) {
                return keyVaultClient.getCertificate(pendingCertificateOperation.target());
            }

            throw new Exception(String.format(
                    "Polling on pending certificate returned an unexpected result. Error code = {1}, Error message = {2}",
                    pendingCertificateOperation.error().code(),
                    pendingCertificateOperation.error().message()));
        }

        throw new Exception("Pending certificate processing delayed");
    }

    /**
     * Extracts private key from PEM contents
     *
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private static PrivateKey extractPrivateKeyFromPemContents(String pemContents)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        Matcher matcher = PRIVATE_KEY.matcher(pemContents);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No private key found in PEM contents.");
        }

        byte[] privateKeyBytes = BASE_64.decode(matcher.group(1));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGO_RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * Extracts certificates from PEM contents
     *
     * @throws CertificateException
     * @throws IOException
     */
    private static List<X509Certificate> extractCertificatesFromPemContents(String pemContents)
            throws CertificateException, IOException {
        Matcher matcher = CERTIFICATE.matcher(pemContents);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No certificate found in PEM contents.");
        }

        List<X509Certificate> result = new ArrayList<X509Certificate>();
        int offset = 0;
        while (true) {
            if (!matcher.find(offset)) {
                break;
            }
            byte[] certBytes = BASE_64.decode(matcher.group(1));
            ByteArrayInputStream certStream = new ByteArrayInputStream(certBytes);
            CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
            X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(certStream);
            certStream.close();

            result.add(x509Certificate);
            offset = matcher.end();
        }

        return result;
    }

    /**
     * Verify a RSA key pair with a simple encrypt/decrypt test.
     *
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static void verifyRSAKeyPair(KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Validate algorithm is RSA
        Assert.assertTrue(keyPair.getPublic().getAlgorithm().equals(ALGO_RSA));
        Assert.assertTrue(keyPair.getPrivate().getAlgorithm().equals(ALGO_RSA));

        // Generate an array of 10 random bytes
        byte[] plainData = new byte[10];
        Random random = new Random();
        random.nextBytes(plainData);

        // Encrypt using the public key
        Cipher encryptCipher = Cipher.getInstance(ALGO_RSA);
        encryptCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] encryptedData = encryptCipher.doFinal(plainData);

        // Decrypt using the private key
        Cipher decryptCipher = Cipher.getInstance(ALGO_RSA);
        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decryptedData = decryptCipher.doFinal(encryptedData);

        // Validate plainData is equal to decryptedData
        Assert.assertArrayEquals(plainData, decryptedData);
    }

    private String toHexString(byte[] x5t) {
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

    private void validateCertificateBundle(CertificateBundle certificateBundle, CertificatePolicy certificatePolicy) {
        Assert.assertNotNull(certificateBundle);
        Assert.assertNotNull(certificateBundle.id());
        Assert.assertNotNull(certificateBundle.keyIdentifier());
        Assert.assertNotNull(certificateBundle.secretIdentifier());
        Assert.assertNotNull(certificateBundle.x509Thumbprint());

        if (certificatePolicy != null) {
            Assert.assertNotNull(certificateBundle.policy());
            Assert.assertNotNull(certificateBundle.policy().issuerParameters());
            Assert.assertNotNull(certificateBundle.policy().issuerParameters().name());
            if (certificatePolicy.issuerParameters() != null) {
                Assert.assertTrue(certificateBundle.policy().issuerParameters().name().equalsIgnoreCase(certificatePolicy.issuerParameters().name()));
            }
        }
    }

    private X509Certificate loadCerToX509Certificate(CertificateBundle certificateBundle) throws CertificateException, IOException {
        Assert.assertNotNull(certificateBundle.cer());
        ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
        cerStream.close();
        return x509Certificate;
    }

    private void validateCertificateIssuer(IssuerBundle expecred, IssuerBundle actual) {
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.provider());
        Assert.assertTrue(actual.provider().equals(expecred.provider()));

        Assert.assertNotNull(actual.credentials());
        Assert.assertNotNull(actual.credentials().accountId());
        Assert.assertTrue(actual.credentials().accountId().equals(expecred.credentials().accountId()));
        Assert.assertNull(actual.credentials().password());

        Assert.assertNotNull(actual.organizationDetails());
    }

    private void validateCertificateKeyInKeyStore(KeyStore keyStore, X509Certificate x509Certificate, String secretPassword) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String defaultAlias = Collections.list(keyStore.aliases()).get(0);
        X509Certificate secretCertificate = (X509Certificate) keyStore.getCertificate(defaultAlias);
        Assert.assertNotNull(secretCertificate);
        Assert.assertTrue(secretCertificate.getSubjectX500Principal().getName()
                .equals(x509Certificate.getSubjectX500Principal().getName()));
        Assert.assertTrue(secretCertificate.getIssuerX500Principal().getName()
                .equals(x509Certificate.getIssuerX500Principal().getName()));
        Assert.assertTrue(secretCertificate.getSerialNumber().equals(x509Certificate.getSerialNumber()));

        // Validate the key in the KeyStore
        Key secretKey = keyStore.getKey(defaultAlias, secretPassword.toCharArray());
        Assert.assertNotNull(secretKey);
        Assert.assertTrue(secretKey instanceof PrivateKey);
        PrivateKey secretPrivateKey = (PrivateKey) secretKey;

        // Create a KeyPair with the private key from the KeyStore and public
        // key from the certificate to verify they match
        KeyPair keyPair = new KeyPair(secretCertificate.getPublicKey(), secretPrivateKey);
        Assert.assertNotNull(keyPair);
        verifyRSAKeyPair(keyPair);
    }

    private void validateCertificateIssuer(IssuerBundle issuer, String issuerName) {
        Assert.assertNotNull(issuer);
        Assert.assertNotNull(issuer.issuerIdentifier());
        Assert.assertNotNull(issuer.issuerIdentifier().name());
        Assert.assertTrue(issuer.issuerIdentifier().name().equalsIgnoreCase(issuerName));
    }

    private KeyStore loadSecretToKeyStore(SecretBundle secret, String secretPassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        ByteArrayInputStream secretStream = new ByteArrayInputStream(BASE_64.decode(secret.value()));
        KeyStore keyStore = KeyStore.getInstance(PKCS12);
        keyStore.load(secretStream, secretPassword.toCharArray());
        secretStream.close();
        return keyStore;
    }

    private void validatePem(CertificateBundle certificateBundle, String subjectName) throws CertificateException, IOException, KeyVaultErrorException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // Load the CER part into X509Certificate object
        X509Certificate x509Certificate = loadCerToX509Certificate(certificateBundle);

        Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
        Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

        // Retrieve the secret backing the certificate
        SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
        SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier());
        Assert.assertTrue(secret.managed());
        String secretValue = secret.value();

        // Extract private key from PEM
        PrivateKey secretPrivateKey = extractPrivateKeyFromPemContents(secretValue);
        Assert.assertNotNull(secretPrivateKey);

        // Extract certificates from PEM
        List<X509Certificate> certificates = extractCertificatesFromPemContents(secretValue);
        Assert.assertNotNull(certificates);
        Assert.assertTrue(certificates.size() == 1);

        // has the public key corresponding to the private key.
        X509Certificate secretCertificate = certificates.get(0);
        Assert.assertNotNull(secretCertificate);
        Assert.assertTrue(secretCertificate.getSubjectX500Principal().getName()
                .equals(x509Certificate.getSubjectX500Principal().getName()));
        Assert.assertTrue(secretCertificate.getIssuerX500Principal().getName()
                .equals(x509Certificate.getIssuerX500Principal().getName()));
        Assert.assertTrue(secretCertificate.getSerialNumber().equals(x509Certificate.getSerialNumber()));

        // Create a KeyPair with the private key from the KeyStore and public
        // key from the certificate to verify they match
        KeyPair keyPair = new KeyPair(secretCertificate.getPublicKey(), secretPrivateKey);
        Assert.assertNotNull(keyPair);
        verifyRSAKeyPair(keyPair);
    }
}
