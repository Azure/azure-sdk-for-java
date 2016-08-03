/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.CertificateIdentifier;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.AdministratorDetails;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.Contact;
import com.microsoft.azure.keyvault.models.Contacts;
import com.microsoft.azure.keyvault.models.IssuerBundle;
import com.microsoft.azure.keyvault.models.IssuerCredentials;
import com.microsoft.azure.keyvault.models.IssuerReference;
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

	static final Base64 _base64 = new Base64(-1, null, true);

	static final Pattern _privateKey = Pattern.compile("-{5}BEGIN PRIVATE KEY-{5}(?:\\s|\\r|\\n)+"
			+ "([a-zA-Z0-9+/=\r\n]+)" + "-{5}END PRIVATE KEY-{5}(?:\\s|\\r|\\n)+");

	static final Pattern _certificate = Pattern.compile("-{5}BEGIN CERTIFICATE-{5}(?:\\s|\\r|\\n)+"
			+ "([a-zA-Z0-9+/=\r\n]+)" + "-{5}END CERTIFICATE-{5}(?:\\s|\\r|\\n)+");

	private static final int MAX_CERTS = 4;
    private static final int PAGELIST_MAX_CERTS = 3;

	/**
	 * Create a self-signed certificate in PKCS12 format (which includes the
	 * private key) certificate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void createSelfSignedCertificatePkcs12() throws Exception {
		// Set content type to indicate the certificate is PKCS12 format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PKCS12);

		X509CertificateProperties x509Properties = new X509CertificateProperties();
		String subjectName = "CN=SelfSignedJavaPkcs12";
		x509Properties.withSubject(subjectName);
		x509Properties.withValidityInMonths(12);

		// Set issuer to "Self"
		IssuerReference issuerReference = new IssuerReference();
		issuerReference.withName(ISSUER_SELF);

		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);
		certificatePolicy.withIssuerReference(issuerReference);
		certificatePolicy.withX509CertificateProperties(x509Properties);

		String vaultUri = getVaultUri();
		String certificateName = "createSelfSignedJavaPkcs12";
		CertificateOperation certificateOperation = keyVaultClient.createCertificate(
				new CreateCertificateRequest
						.Builder(vaultUri, certificateName)
						.withPolicy(certificatePolicy)
						.build()).getBody();

		Assert.assertNotNull(certificateOperation);
		Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

		CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.keyIdentifier());
		Assert.assertNotNull(certificateBundle.secretIdentifier());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());

		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();

		// Load the secret into a KeyStore
		ByteArrayInputStream secretStream = new ByteArrayInputStream(_base64.decode(secret.value()));
		String secretPassword = "";
		KeyStore keyStore = KeyStore.getInstance(PKCS12);
		keyStore.load(secretStream, secretPassword.toCharArray());
		secretStream.close();

		// Validate the certificate in the KeyStore
		String defaultAlias = Collections.list(keyStore.aliases()).get(0);
		X509Certificate secretCertificate = (X509Certificate) keyStore.getCertificate(defaultAlias);
		Assert.assertNotNull(secretCertificate);
		Assert.assertTrue(secretCertificate.getPublicKey().equals(x509Certificate.getPublicKey()));
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);
		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * Create a self-signed certificate in PEM format (which includes the
	 * private key) certificate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void createSelfSignedCertificatePem() throws Exception {
		// Set content type to indicate the certificate is PKCS12 format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PEM);

		X509CertificateProperties x509Properties = new X509CertificateProperties();
		String subjectName = "CN=SelfSignedJavaPem";
		x509Properties.withSubject(subjectName);
		x509Properties.withValidityInMonths(12);

		// Set issuer to "Self"
		IssuerReference issuerReference = new IssuerReference();
		issuerReference.withName(ISSUER_SELF);

		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);
		certificatePolicy.withIssuerReference(issuerReference);
		certificatePolicy.withX509CertificateProperties(x509Properties);

		String vaultUri = getVaultUri();
		String certificateName = "SelfSignedJavaPem";
		CertificateOperation certificateOperation = keyVaultClient.createCertificate(
				new CreateCertificateRequest
					.Builder(vaultUri, certificateName)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		Assert.assertNotNull(certificateOperation);
		Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

		CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.kid());
		Assert.assertNotNull(certificateBundle.sid());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());

		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * Create a test-issuer issued certificate in PKCS12 format (which includes
	 * the private key) certificate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void createCertificatePkcs12() throws Exception {
		// Construct organization administrator details
		AdministratorDetails administratorDetails = new AdministratorDetails();
		administratorDetails.withFirstName("John");
		administratorDetails.withLastName("Doe");
		administratorDetails.withEmailAddress("john.doe@contoso.com");
		administratorDetails.withPhone("1234567890");

		// Construct organization details
		OrganizationDetails organizationDetails = new OrganizationDetails();
		List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
		administratorsDetails.add(administratorDetails);
		organizationDetails.withAdminDetails(administratorsDetails);

		// Construct certificate issuer credentials
		IssuerCredentials credentials = new IssuerCredentials();
		credentials.withAccountId("account1");
		credentials.withPassword("Pa$$w0rd");

		IssuerBundle certificateIssuer = new IssuerBundle();
		certificateIssuer.withProvider(ISSUER_TEST);
		certificateIssuer.withCredentials(credentials);
		certificateIssuer.withOrganizationDetails(organizationDetails);

		String certificateIssuerName = "createCertificateJavaPkcs12Issuer01";
		IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
				new SetCertificateIssuerRequest
					.Builder(getVaultUri(),certificateIssuerName)
					.withIssuer(certificateIssuer)
					.build()).getBody();
		
		Assert.assertNotNull(createdCertificateIssuer);
		Assert.assertNotNull(createdCertificateIssuer.issuerIdentifier());
		Assert.assertNotNull(createdCertificateIssuer.issuerIdentifier().name());
		Assert.assertTrue(createdCertificateIssuer.issuerIdentifier().name().equalsIgnoreCase(certificateIssuerName));

		// Set content type to indicate the certificate is PKCS12 format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PKCS12);

		X509CertificateProperties x509Properties = new X509CertificateProperties();
		String subjectName = "CN=TestJavaPkcs12";
		x509Properties.withSubject(subjectName);
		x509Properties.withValidityInMonths(12);

		// Set issuer reference to the created issuer
		IssuerReference issuerReference = new IssuerReference();
		issuerReference.withName(createdCertificateIssuer.issuerIdentifier().name());

		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);
		certificatePolicy.withIssuerReference(issuerReference);
		certificatePolicy.withX509CertificateProperties(x509Properties);

		String vaultUri = getVaultUri();
		String certificateName = "createTestJavaPkcs12";
		CertificateOperation certificateOperation = keyVaultClient.createCertificate(
				new CreateCertificateRequest
					.Builder(vaultUri, certificateName)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		Assert.assertNotNull(certificateOperation);
		Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

		CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.keyIdentifier());
		Assert.assertNotNull(certificateBundle.secretIdentifier());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());
		Assert.assertNotNull(certificateBundle.policy());
		Assert.assertNotNull(certificateBundle.policy().issuerReference());
		Assert.assertNotNull(certificateBundle.policy().issuerReference().name());
		Assert.assertTrue(
				certificateBundle.policy().issuerReference().name().equalsIgnoreCase(certificateIssuerName));

		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();

		// Load the secret into a KeyStore
		ByteArrayInputStream secretStream = new ByteArrayInputStream(_base64.decode(secret.value()));
		String secretPassword = "";
		KeyStore keyStore = KeyStore.getInstance(PKCS12);
		keyStore.load(secretStream, secretPassword.toCharArray());
		secretStream.close();

		// Validate the certificate in the KeyStore
		String defaultAlias = Collections.list(keyStore.aliases()).get(0);
		X509Certificate secretCertificate = (X509Certificate) keyStore.getCertificate(defaultAlias);
		Assert.assertNotNull(secretCertificate);
		Assert.assertTrue(secretCertificate.getPublicKey().equals(x509Certificate.getPublicKey()));
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * Create a test-issuer certificate in PEM format (which includes the
	 * private key) certificate.
	 * 
	 * @throws Exception
	 */
	@Test
	public void createCertificatePem() throws Exception {
		// Construct organization administrator details
		AdministratorDetails administratorDetails = new AdministratorDetails();
		administratorDetails.withFirstName("John");
		administratorDetails.withLastName("Doe");
		administratorDetails.withEmailAddress("john.doe@contoso.com");
		administratorDetails.withPhone("1234567890");

		// Construct organization details
		OrganizationDetails organizationDetails = new OrganizationDetails();
		List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
		administratorsDetails.add(administratorDetails);
		organizationDetails.withAdminDetails(administratorsDetails);

		// Construct certificate issuer credentials
		IssuerCredentials credentials = new IssuerCredentials();
		credentials.withAccountId("account1");
		credentials.withPassword("Pa$$w0rd");

		IssuerBundle certificateIssuer = new IssuerBundle();
		certificateIssuer.withProvider(ISSUER_TEST);
		certificateIssuer.withCredentials(credentials);
		certificateIssuer.withOrganizationDetails(organizationDetails);

		String certificateIssuerName = "createCertificateJavaPemIssuer01";
		IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
				new SetCertificateIssuerRequest
					.Builder(getVaultUri(),	certificateIssuerName)
					.withIssuer(certificateIssuer)
					.build()).getBody();
		Assert.assertNotNull(createdCertificateIssuer);
		Assert.assertNotNull(createdCertificateIssuer.issuerIdentifier());
		Assert.assertNotNull(createdCertificateIssuer.issuerIdentifier().name());
		Assert.assertTrue(createdCertificateIssuer.issuerIdentifier().name().equalsIgnoreCase(certificateIssuerName));

		// Set content type to indicate the certificate is PEM format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PEM);

		X509CertificateProperties x509Properties = new X509CertificateProperties();
		String subjectName = "CN=TestJavaPem";
		x509Properties.withSubject(subjectName);
		x509Properties.withValidityInMonths(12);

		// Set issuer reference to the created issuer
		IssuerReference issuerReference = new IssuerReference();
		issuerReference.withName(createdCertificateIssuer.issuerIdentifier().name());

		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);
		certificatePolicy.withIssuerReference(issuerReference);
		certificatePolicy.withX509CertificateProperties(x509Properties);

		String vaultUri = getVaultUri();
		String certificateName = "createTestJavaPem";
		CertificateOperation certificateOperation = keyVaultClient.createCertificate(
				new CreateCertificateRequest
					.Builder(vaultUri, certificateName)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		Assert.assertNotNull(certificateOperation);
		Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));

		CertificateBundle certificateBundle = pollOnCertificateOperation(certificateOperation);
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.kid());
		Assert.assertNotNull(certificateBundle.sid());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());
		Assert.assertNotNull(certificateBundle.policy());
		Assert.assertNotNull(certificateBundle.policy().issuerReference());
		Assert.assertNotNull(certificateBundle.policy().issuerReference().name());
		Assert.assertTrue(
				certificateBundle.policy().issuerReference().name().equalsIgnoreCase(certificateIssuerName));

		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals(subjectName));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals(subjectName));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		}
		catch(KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * Create a certificate signing request with key in Key Vault.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws KeyVaultErrorException 
	 * 
	 * @throws Exception
	 */	
	@Test
	public void createCsr() throws InterruptedException, ExecutionException, KeyVaultErrorException, IllegalArgumentException, IOException {
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PKCS12);

		X509CertificateProperties x509Properties = new X509CertificateProperties();
		String subjectName = "CN=ManualEnrollmentJava";
		x509Properties.withSubject(subjectName);
		x509Properties.withValidityInMonths(12);

		// Set issuer to "Unknown"
		IssuerReference issuerReference = new IssuerReference();
		issuerReference.withName(ISSUER_UNKNOWN);

		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);
		certificatePolicy.withIssuerReference(issuerReference);
		certificatePolicy.withX509CertificateProperties(x509Properties);

		String vaultUri = getVaultUri();
		String certificateName = "createManualEnrollmentJava";
		CertificateOperation certificateOperation = keyVaultClient.createCertificate(
				new CreateCertificateRequest
					.Builder(vaultUri, certificateName)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		Assert.assertNotNull(certificateOperation);
		Assert.assertTrue(certificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS));
		Assert.assertNotNull(certificateOperation.csr());
		
		String csr = keyVaultClient.getPendingCertificateSigningRequest(vaultUri, certificateName).getBody();	
		Assert.assertNotNull(csr);
		
		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}
	/**
	 * Import a PKCS12 format (which includes the private key) certificate.
	 */
	@Test
	public void importCertificatePkcs12() throws Exception {
		String certificateContent = "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";
		String certificatePassword = "123";

		// Set content type to indicate the certificate is PKCS12 format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PKCS12);
		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);

		String vaultUri = getVaultUri();
		String certificateName = "importCertPkcs";
		CertificateBundle certificateBundle = keyVaultClient.importCertificate(
				new ImportCertificateRequest
					.Builder(vaultUri, certificateName, certificateContent)
					.withPassword(certificatePassword)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		// Validate the certificate bundle created
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.kid());
		Assert.assertNotNull(certificateBundle.sid());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());
		
		Assert.assertTrue(toHexString(certificateBundle.x509Thumbprint()).equalsIgnoreCase("7cb8b7539d87ba7215357b9b9049dff2d3fa59ba"));
		
		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName().equals("CN=KeyVaultTest"));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName().equals("CN=Root Agency"));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();

		// Load the secret into a KeyStore
		ByteArrayInputStream secretStream = new ByteArrayInputStream(_base64.decode(secret.value()));
		String secretPassword = "";
		KeyStore keyStore = KeyStore.getInstance(PKCS12);
		keyStore.load(secretStream, secretPassword.toCharArray());
		secretStream.close();

		// Validate the certificate in the KeyStore
		String defaultAlias = Collections.list(keyStore.aliases()).get(0);
		X509Certificate secretCertificate = (X509Certificate) keyStore.getCertificate(defaultAlias);
		Assert.assertNotNull(secretCertificate);
		Assert.assertTrue(secretCertificate.getPublicKey().equals(x509Certificate.getPublicKey()));
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * Import a PEM format (which includes a encrypted private key) certificate.
	 * TODO: Server can't handle this yet.
	 */
	// @Test
	public void importCertificatePem() throws Exception {
		String certificateContent = "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
				+ "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\n" 
				+ "JHA=\n"
				+ "-----END ENCRYPTED PRIVATE KEY-----\n" 
				+ "-----BEGIN CERTIFICATE-----\n"
				+ "MIIDozCCAougAwIBAgIJAIvNGnmenqgjMA0GCSqGSIb3DQEBCwUAMGgxCzAJBgNV\n"
				+ "BAYTAlVTMQswCQYDVQQIDAJXQTEQMA4GA1UEBwwHUmVkbW9uZDEXMBUGA1UECgwO\n"
				+ "TWljcm9zb2Z0IENvcnAxDjAMBgNVBAsMBUF6dXJlMREwDwYDVQQDDAhLZXlWYXVs\n"
				+ "dDAeFw0xNjA0MDEyMzAwMjlaFw0xNjA1MzEyMzAwMjlaMGgxCzAJBgNVBAYTAlVT\n"
				+ "MQswCQYDVQQIDAJXQTEQMA4GA1UEBwwHUmVkbW9uZDEXMBUGA1UECgwOTWljcm9z\n"
				+ "b2Z0IENvcnAxDjAMBgNVBAsMBUF6dXJlMREwDwYDVQQDDAhLZXlWYXVsdDCCASIw\n"
				+ "DQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN1udkhsWIwmua3SFJWxS9AJoKK5\n"
				+ "o7RAHwsQUWWNkSsPiWrzDYXarfUEs1HBEsAjOJDabK1L0ahw4Manx0NXDOmw8kuD\n"
				+ "lNMs4yTZNxvECvKpq37Z6Q3D9ts4sVSeFbXtOYr81P+8DOOH3Ibk3sldoJBMXJ5h\n"
				+ "pw4R72988m9CZ9KjcdaKFk3L1baCehpwkJLZD2XD7MzV9YBKNnd15DPCkVZHul1t\n"
				+ "bW0E7kf7vUOPIfRuNZeN6QvqsKTA+RoGh3CVu9QV+XG/AsHDoHUwGUlJPvOCm3U5\n"
				+ "tDrrbXAP+Wa/kE/fGAJkZQLPcbappUI4Swtt9u7+CpyQ96H7BY1yHvmBzBUCAwEA\n"
				+ "AaNQME4wHQYDVR0OBBYEFJ9DSIY/4ODmWY7oIQkGDD7KlQnAMB8GA1UdIwQYMBaA\n"
				+ "FJ9DSIY/4ODmWY7oIQkGDD7KlQnAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEL\n"
				+ "BQADggEBAGV3fTAVWd1tdgcaogBirn1LF8d3H45bdDtjD98933dsOCYlXHKNdB62\n"
				+ "6Qwg6XF9a+p1vuHI1I8MKBu//q+pLJce+bi2jmge64zlz/iO3sLSOFo/q1EWzhal\n"
				+ "TRglNkvqWr7OvJXdUznQI3AzjB8tbFB2YerSbmD6FxAAihEq8ZoJ1BsMq5vknpPB\n"
				+ "iETENaNSjdgPEsiapYNALgY4AVxtSS5GJDZ9zpc5Q6HCPmUozLbQheNZf3+D75cy\n"
				+ "gB2odtfwhKCuIfuMan51UqjupK0JVJuNV4MXRXH0mFPEBxI4pYolFuV8960jGXqE\n"
				+ "m/26LtCJLW5QaedtCCKpn9fat5VHgso=\n" 
				+ "-----END CERTIFICATE-----\n";

		String certificatePassword = "1234";

		// Set content type to indicate the certificate is PEM format.
		SecretProperties secretProperties = new SecretProperties();
		secretProperties.withContentType(MIME_PEM);
		CertificatePolicy certificatePolicy = new CertificatePolicy();
		certificatePolicy.withSecretProperties(secretProperties);

		String vaultUri = getVaultUri();
		String certificateName = "importCertPem";
		CertificateBundle certificateBundle = keyVaultClient.importCertificate(
				new ImportCertificateRequest
					.Builder(vaultUri, certificateName, certificateContent)
					.withPassword(certificatePassword)
					.withPolicy(certificatePolicy)
					.build()).getBody();

		// Validate the certificate bundle created
		Assert.assertNotNull(certificateBundle);
		Assert.assertNotNull(certificateBundle.id());
		Assert.assertNotNull(certificateBundle.kid());
		Assert.assertNotNull(certificateBundle.sid());
		Assert.assertNotNull(certificateBundle.x509Thumbprint());
		Assert.assertTrue(toHexString(certificateBundle.x509Thumbprint()).equalsIgnoreCase("d86db6736c335f08ef39aa27ef83836e8eba95b9"));

		// Load the CER part into X509Certificate object
		Assert.assertNotNull(certificateBundle.cer());
		ByteArrayInputStream cerStream = new ByteArrayInputStream(certificateBundle.cer());
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(cerStream);
		cerStream.close();

		Assert.assertTrue(x509Certificate.getSubjectX500Principal().getName()
				.equals("CN=KeyVault,OU=Azure,O=Microsoft Corp,L=Redmond,ST=WA,C=US"));
		Assert.assertTrue(x509Certificate.getIssuerX500Principal().getName()
				.equals("CN=KeyVault,OU=Azure,O=Microsoft Corp,L=Redmond,ST=WA,C=US"));

		// Retrieve the secret backing the certificate
		SecretIdentifier secretIdentifier = certificateBundle.secretIdentifier();
		SecretBundle secret = keyVaultClient.getSecret(secretIdentifier.baseIdentifier()).getBody();

		// Load the secret into a KeyStore
		ByteArrayInputStream secretStream = new ByteArrayInputStream(_base64.decode(secret.value()));
		String secretPassword = "";
		KeyStore keyStore = KeyStore.getInstance(PKCS12);
		keyStore.load(secretStream, secretPassword.toCharArray());
		secretStream.close();

		// Validate the certificate in the KeyStore
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

		CertificateBundle deletedCertificateBundle = keyVaultClient.deleteCertificate(getVaultUri(), certificateName).getBody();
		Assert.assertNotNull(deletedCertificateBundle);

		try {
			keyVaultClient.getCertificate(deletedCertificateBundle.certificateIdentifier().baseIdentifier());
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateNotFound", e.getBody().error().code());
		}
	}

	/**
	 * List certificates in a vault.
	 */
	@Test
	public void listCertificates() throws Exception {
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
								.build()).getBody();
					CertificateIdentifier id = certificateBundle.certificateIdentifier();
					certificates.add(id.baseIdentifier());
					break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.getBody().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        Thread.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
			}
		}

		PagedList<CertificateItem> listResult = keyVaultClient.getCertificates(getVaultUri(), PAGELIST_MAX_CERTS).getBody();
		Assert.assertTrue(PAGELIST_MAX_CERTS >= listResult.currentPage().getItems().size());

		HashSet<String> toDelete = new HashSet<String>();

        for (CertificateItem item : listResult) {
            CertificateIdentifier id = new CertificateIdentifier(item.id());
            toDelete.add(id.name());
            certificates.remove(item.id());
        }

		Assert.assertEquals(0, certificates.size());

		for (String toDeleteCertificateName : toDelete) {
			keyVaultClient.deleteCertificate(getVaultUri(), toDeleteCertificateName);
		}
	}

	/**
	 * List versions of a certificate in a vault.
	 */
	@Test
	public void listCertificateVersions() throws Exception {
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
								.build()).getBody();
					CertificateIdentifier id = certificateBundle.certificateIdentifier();
					certificates.add(id.identifier());
					break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.getBody().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        Thread.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
			}
		}

        PagedList<CertificateItem> listResult = keyVaultClient.getCertificateVersions(getVaultUri(), certificateName, PAGELIST_MAX_CERTS).getBody();
        Assert.assertTrue(PAGELIST_MAX_CERTS >= listResult.currentPage().getItems().size());

        listResult = keyVaultClient.getCertificateVersions(getVaultUri(), certificateName).getBody();
        for (;;) {
        	for (CertificateItem item : listResult) {
                certificates.remove(item.id());
            }
            String nextLink = listResult.nextPageLink();
            if (nextLink == null) {
                break;
            }
            keyVaultClient.getCertificateVersionsNext(nextLink).getBody();
        }

        Assert.assertEquals(0, certificates.size());

        keyVaultClient.deleteCertificate(getVaultUri(), certificateName);
	}

	/**
	 * CRUD for Certificate issuers
	 */
	@Test
	public void issuerCrudOperations() throws Exception {
		// Construct organization administrator details
		AdministratorDetails administratorDetails = new AdministratorDetails();
		administratorDetails.withFirstName("John");
		administratorDetails.withLastName("Doe");
		administratorDetails.withEmailAddress("john.doe@contoso.com");
		administratorDetails.withPhone("1234567890");

		// Construct organization details
		OrganizationDetails organizationDetails = new OrganizationDetails();
		List<AdministratorDetails> administratorsDetails = new ArrayList<AdministratorDetails>();
		administratorsDetails.add(administratorDetails);
		organizationDetails.withAdminDetails(administratorsDetails);

		// Construct certificate issuer credentials
		IssuerCredentials credentials = new IssuerCredentials();
		credentials.withAccountId("account1");
		credentials.withPassword("Pa$$w0rd");

		IssuerBundle certificateIssuer = new IssuerBundle();
		certificateIssuer.withProvider(ISSUER_TEST);
		certificateIssuer.withCredentials(credentials);
		certificateIssuer.withOrganizationDetails(organizationDetails);

		IssuerBundle createdCertificateIssuer = keyVaultClient.setCertificateIssuer(
				new SetCertificateIssuerRequest
					.Builder(getVaultUri(), "issuer1")
					.withIssuer(certificateIssuer)
					.build()).getBody();

		Assert.assertNotNull(createdCertificateIssuer);
		Assert.assertNotNull(createdCertificateIssuer.provider());
		Assert.assertTrue(createdCertificateIssuer.provider().equals("Test"));

		Assert.assertNotNull(createdCertificateIssuer.credentials());
		Assert.assertNotNull(createdCertificateIssuer.credentials().accountId());
		Assert.assertTrue(createdCertificateIssuer.credentials().accountId().equals("account1"));
		Assert.assertNull(createdCertificateIssuer.credentials().password());

		Assert.assertNotNull(createdCertificateIssuer.organizationDetails());

		String certificateIssuerName = createdCertificateIssuer.issuerIdentifier().name();
		IssuerBundle retrievedCertificateIssuer = keyVaultClient.getCertificateIssuer(getVaultUri(),
				certificateIssuerName).getBody();

		Assert.assertNotNull(retrievedCertificateIssuer);
		Assert.assertNotNull(retrievedCertificateIssuer.provider());
		Assert.assertTrue(retrievedCertificateIssuer.provider().equals(ISSUER_TEST));

		Assert.assertNotNull(retrievedCertificateIssuer.credentials());
		Assert.assertNotNull(retrievedCertificateIssuer.credentials().accountId());
		Assert.assertTrue(retrievedCertificateIssuer.credentials().accountId().equals("account1"));
		Assert.assertNull(retrievedCertificateIssuer.credentials().password());

		Assert.assertNotNull(retrievedCertificateIssuer.organizationDetails());

		IssuerCredentials updatedCredentials = new IssuerCredentials();
		updatedCredentials.withAccountId("account2");
		updatedCredentials.withPassword("Secur!Ty");
		retrievedCertificateIssuer.withCredentials(updatedCredentials);
		IssuerBundle updatedCertificateIssuer = keyVaultClient.updateCertificateIssuer(
				new UpdateCertificateIssuerRequest
					.Builder(getVaultUri(), certificateIssuerName)
					.withIssuer(retrievedCertificateIssuer)
					.build()).getBody();

		Assert.assertNotNull(updatedCertificateIssuer);
		Assert.assertNotNull(updatedCertificateIssuer.provider());
		Assert.assertTrue(updatedCertificateIssuer.provider().equals(ISSUER_TEST));

		Assert.assertNotNull(updatedCertificateIssuer.credentials());
		Assert.assertNotNull(updatedCertificateIssuer.credentials().accountId());
		Assert.assertTrue(updatedCertificateIssuer.credentials().accountId().equals("account2"));
		Assert.assertNull(updatedCertificateIssuer.credentials().password());

		Assert.assertNotNull(updatedCertificateIssuer.organizationDetails());

		IssuerBundle deletedCertificateIssuer = keyVaultClient.deleteCertificateIssuer(getVaultUri(), certificateIssuerName).getBody();

		Assert.assertNotNull(deletedCertificateIssuer);
		Assert.assertNotNull(deletedCertificateIssuer.provider());
		Assert.assertTrue(deletedCertificateIssuer.provider().equals(ISSUER_TEST));

		Assert.assertNotNull(deletedCertificateIssuer.credentials());
		Assert.assertNotNull(deletedCertificateIssuer.credentials().accountId());
		Assert.assertTrue(deletedCertificateIssuer.credentials().accountId().equals("account2"));
		Assert.assertNull(deletedCertificateIssuer.credentials().password());

		Assert.assertNotNull(deletedCertificateIssuer.organizationDetails());

		try {
			keyVaultClient.getCertificateIssuer(getVaultUri(), certificateIssuerName);
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("CertificateIssuerNotFound", e.getBody().error().code());
		}
	}

	/**
	 * CRUD for Certificate contacts
	 * @throws Exception 
	 */
	@Test
	public void contactsCrudOperations() throws Exception {
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
		Contacts createdCertificateContacts = keyVaultClient.setCertificateContacts(getVaultUri(), certificateContacts).getBody();
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
		Contacts retrievedCertificateContacts = keyVaultClient.getCertificateContacts(getVaultUri()).getBody();
		Assert.assertNotNull(retrievedCertificateContacts);
		Assert.assertNotNull(retrievedCertificateContacts.contactList());
		Assert.assertTrue(retrievedCertificateContacts.contactList().size() == 2);		
		
		// Delete
		Contacts deletedCertificateContacts = keyVaultClient.deleteCertificateContacts(getVaultUri()).getBody();
		Assert.assertNotNull(deletedCertificateContacts);
		Assert.assertNotNull(deletedCertificateContacts.contactList());
		Assert.assertTrue(deletedCertificateContacts.contactList().size() == 2);		
		
		// Get after delete		
		try {
			keyVaultClient.getCertificateContacts(getVaultUri()).getBody();
		} catch (KeyVaultErrorException e) {
			Assert.assertNotNull(e.getBody().error());
			Assert.assertEquals("ContactsNotFound", e.getBody().error().code());
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
					.getCertificateOperation(getVaultUri(), certificateName).getBody();
			if (pendingCertificateOperation.status().equalsIgnoreCase(STATUS_IN_PROGRESS)) {
				Thread.sleep(10000);
				pendingPollCount += 1;
				continue;
			}

			if (pendingCertificateOperation.status().equalsIgnoreCase(STATUS_COMPLETED)) {
				return keyVaultClient.getCertificate(pendingCertificateOperation.target()).getBody();
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
		Matcher matcher = _privateKey.matcher(pemContents);
		if (!matcher.find()) {
			throw new IllegalArgumentException("No private key found in PEM contents.");
		}

		byte[] privateKeyBytes = _base64.decode(matcher.group(1));
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
		Matcher matcher = _certificate.matcher(pemContents);
		if (!matcher.find()) {
			throw new IllegalArgumentException("No certificate found in PEM contents.");
		}

		List<X509Certificate> result = new ArrayList<X509Certificate>();
		int offset = 0;
		while (true) {
			if (!matcher.find(offset)) {
				break;
			}
			byte[] certBytes = _base64.decode(matcher.group(1));
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

		if(x5t == null)
			return "";
		
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
}
