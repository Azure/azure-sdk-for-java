// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.BackupKeyResult;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.CertificateIssuerItem;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.keyvault.models.CertificateOperation;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
import com.microsoft.azure.keyvault.models.Contact;
import com.microsoft.azure.keyvault.models.Contacts;
import com.microsoft.azure.keyvault.models.IssuerBundle;
import com.microsoft.azure.keyvault.models.IssuerParameters;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyItem;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.KeyVerifyResult;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.keyvault.models.SecretProperties;
import com.microsoft.azure.keyvault.models.X509CertificateProperties;
import com.microsoft.azure.keyvault.requests.CreateCertificateRequest;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.requests.SetCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateOperationRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificatePolicyRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateRequest;
import com.microsoft.azure.keyvault.requests.UpdateKeyRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.rest.ServiceCallback;

public class AsyncOperationsTest extends KeyVaultClientIntegrationTestBase {

    @Test
    public void keyAsyncForAsyncOperationsTest() throws Exception {

        String vault = getVaultUri();
        String keyname = "mykey";

        CreateKeyRequest createKeyRequest = new CreateKeyRequest.Builder(vault, keyname, JsonWebKeyType.RSA).build();
        KeyBundle keyBundle = keyVaultClient.createKeyAsync(createKeyRequest, null).get();
        Assert.assertNotNull(keyBundle);

        UpdateKeyRequest updateKeyRequest = new UpdateKeyRequest.Builder(keyBundle.key().kid()).build();
        keyBundle = keyVaultClient.updateKeyAsync(updateKeyRequest, null).get();
        Assert.assertNotNull(keyBundle);

        keyBundle = keyVaultClient.getKeyAsync(keyBundle.key().kid(), null).get();
        Assert.assertNotNull(keyBundle);

        List<KeyItem> keyItems = keyVaultClient.listKeysAsync(vault, 2, null).get();
        Assert.assertNotNull(keyItems);

        List<KeyItem> keyVersionItems = keyVaultClient.listKeyVersionsAsync(vault, keyname, 2, null).get();
        Assert.assertNotNull(keyVersionItems);

        BackupKeyResult backupResult = keyVaultClient.backupKeyAsync(vault, keyname, null).get();
        Assert.assertNotNull(backupResult);

        keyVaultClient.deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null).get();
        pollOnKeyDeletion(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name());
        keyVaultClient.purgeDeletedKey(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name());
        SdkContext.sleep(40000);

        KeyBundle restoreResult = keyVaultClient.restoreKeyAsync(vault, backupResult.value(), null).get();
        Assert.assertNotNull(restoreResult);

        KeyOperationResult encryptResult = keyVaultClient
                .encryptAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, new byte[100], null).get();
        Assert.assertNotNull(encryptResult);

        KeyOperationResult decryptResult = keyVaultClient.decryptAsync(keyBundle.key().kid(),
                JsonWebKeyEncryptionAlgorithm.RSA_OAEP, encryptResult.result(), null).get();
        Assert.assertNotNull(decryptResult);

        KeyOperationResult wrapResult = keyVaultClient
                .wrapKeyAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, new byte[100], null).get();
        Assert.assertNotNull(wrapResult);

        KeyOperationResult unwrapResult = keyVaultClient.unwrapKeyAsync(keyBundle.key().kid(),
                JsonWebKeyEncryptionAlgorithm.RSA_OAEP, wrapResult.result(), null).get();
        Assert.assertNotNull(unwrapResult);

        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plainText);
        byte[] digest = md.digest();
        KeyOperationResult signResult = keyVaultClient
                .signAsync(keyBundle.key().kid(), JsonWebKeySignatureAlgorithm.RS256, digest, null).get();
        Assert.assertNotNull(signResult);

        KeyVerifyResult verifypResult = keyVaultClient.verifyAsync(keyBundle.key().kid(),
                JsonWebKeySignatureAlgorithm.RS256, digest, signResult.result(), null).get();
        Assert.assertTrue(verifypResult.value());

        keyBundle = keyVaultClient
                .deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null).get();
        Assert.assertNotNull(keyBundle);
        pollOnKeyDeletion(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name());
        keyVaultClient.purgeDeletedKey(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name());
        SdkContext.sleep(20000);
        // Get the unavailable key to throw exception -> it gets stuck

        try {
            keyVaultClient.deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null)
                    .get();
        } catch (ExecutionException ex) {

            Throwable t = ex.getCause();
            if (t instanceof KeyVaultErrorException) {
                Assert.assertEquals("KeyNotFound", ((KeyVaultErrorException) t).body().error().code());
            } else {
                throw ex;
            }
        }

    }

    @Test
    public void secretAsyncForAsyncOperationsTest() throws Exception {

        String vault = getVaultUri();
        String secretname = "mySecret2";
        String password = "password";

        SetSecretRequest setSecretRequest = new SetSecretRequest.Builder(vault, secretname, password).build();
        SecretBundle secretBundle = keyVaultClient.setSecretAsync(setSecretRequest, null).get();
        Assert.assertNotNull(secretBundle);

        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest.Builder(secretBundle.id()).build();
        secretBundle = keyVaultClient.updateSecretAsync(updateSecretRequest, null).get();
        Assert.assertNotNull(secretBundle);

        secretBundle = keyVaultClient.getSecretAsync(secretBundle.id(), null).get();
        Assert.assertNotNull(secretBundle);

        List<SecretItem> secretItems = keyVaultClient.listSecretsAsync(vault, 2, null).get();
        Assert.assertNotNull(secretItems);

        List<SecretItem> secretVersionItems = keyVaultClient.listSecretVersionsAsync(vault, secretname, 2, null).get();
        Assert.assertNotNull(secretVersionItems);

        secretBundle = keyVaultClient.deleteSecretAsync(vault, secretname, null).get();
        Assert.assertNotNull(secretBundle);

        try {
            keyVaultClient.deleteSecretAsync(vault, secretname, null).get();

        } catch (ExecutionException ex) {

            Throwable t = ex.getCause();
            if (t instanceof KeyVaultErrorException) {
                Assert.assertEquals("SecretNotFound", ((KeyVaultErrorException) t).body().error().code());
            } else {
                throw ex;
            }
        }
        pollOnSecretDeletion(vault, secretname);
        keyVaultClient.purgeDeletedSecretAsync(vault, secretname, null).get();
        SdkContext.sleep(20000);
    }

    @Test
    public void certificateAsyncForAsyncOperationsTest() throws Exception {

        String vault = getVaultUri();
        String certificateName = "tempCertificate2";

        CreateCertificateRequest createCertificateRequest = new CreateCertificateRequest.Builder(vault, certificateName)
                .withPolicy(new CertificatePolicy()
                        .withSecretProperties(new SecretProperties().withContentType("application/x-pkcs12"))
                        .withIssuerParameters(new IssuerParameters().withName("Self"))
                        .withX509CertificateProperties(new X509CertificateProperties()
                                .withSubject("CN=SelfSignedJavaPkcs12").withValidityInMonths(12)))
                .build();
        CertificateOperation certificateOperation = keyVaultClient
                .createCertificateAsync(createCertificateRequest, null).get();
        Assert.assertNotNull(certificateOperation);

        UpdateCertificateOperationRequest updateCertificateOperationRequest = new UpdateCertificateOperationRequest.Builder(
                vault, certificateName, false).build();
        certificateOperation = keyVaultClient.updateCertificateOperationAsync(updateCertificateOperationRequest, null)
                .get();
        Assert.assertNotNull(certificateOperation);

        Map<String, String> tags = new HashMap<String, String>();
        tags.put("tag1", "foo");
        UpdateCertificateRequest updateCertificateRequest = new UpdateCertificateRequest.Builder(vault, certificateName)
                .withTags(tags).build();
        CertificateBundle certificateBundle = keyVaultClient.updateCertificateAsync(updateCertificateRequest, null)
                .get();
        Assert.assertNotNull(certificateBundle);

        UpdateCertificatePolicyRequest updateCertificatePolicyRequest = new UpdateCertificatePolicyRequest.Builder(
                vault, certificateName).build();
        CertificatePolicy certificatePolicy = keyVaultClient
                .updateCertificatePolicyAsync(updateCertificatePolicyRequest, null).get();
        Assert.assertNotNull(certificatePolicy);

        certificatePolicy = keyVaultClient.getCertificatePolicyAsync(vault, certificateName, null).get();
        Assert.assertNotNull(certificatePolicy);

        certificateOperation = keyVaultClient.getCertificateOperationAsync(vault, certificateName, null).get();
        Assert.assertNotNull(certificateOperation);

        certificateBundle = keyVaultClient
                .getCertificateAsync(vault, certificateName, (ServiceCallback<CertificateBundle>) null).get();
        Assert.assertNotNull(certificateBundle);

        String cert = keyVaultClient.getPendingCertificateSigningRequestAsync(vault, certificateName, null).get();
        Assert.assertTrue(!cert.isEmpty());

        List<CertificateItem> certificateItem = keyVaultClient.listCertificatesAsync(vault, null).get();
        Assert.assertNotNull(certificateItem);

        List<CertificateItem> certificateVersionItem = keyVaultClient
                .listCertificateVersionsAsync(vault, certificateName, null).get();
        Assert.assertNotNull(certificateVersionItem);

        keyVaultClient.deleteCertificateOperationAsync(vault, certificateName, null).get();
        keyVaultClient.deleteCertificateAsync(vault, certificateName, null).get();
        pollOnCertificateDeletion(vault, certificateName);

        try {
            keyVaultClient.deleteCertificateAsync(vault, certificateName, null).get();
        } catch (ExecutionException ex) {

            Throwable t = ex.getCause();
            if (t instanceof KeyVaultErrorException) {
                Assert.assertEquals("CertificateNotFound", ((KeyVaultErrorException) t).body().error().code());
            } else {
                throw ex;
            }
        }

        keyVaultClient.purgeDeletedCertificate(vault, certificateName);
        SdkContext.sleep(20000);
    }

    @Test
    public void issuerAsyncForAsyncOperationsTest() throws Exception {

        String vault = getVaultUri();
        String issuerName = "myIssuer";

        SetCertificateIssuerRequest setCertificateIssuerRequest = new SetCertificateIssuerRequest.Builder(vault,
                issuerName, "Test").build();
        IssuerBundle certificateIssuer = keyVaultClient.setCertificateIssuerAsync(setCertificateIssuerRequest, null)
                .get();
        Assert.assertNotNull(certificateIssuer);

        UpdateCertificateIssuerRequest updateCertificateIssuerRequest = new UpdateCertificateIssuerRequest.Builder(
                vault, issuerName).withProvider("SslAdmin").build();
        certificateIssuer = keyVaultClient.updateCertificateIssuerAsync(updateCertificateIssuerRequest, null).get();
        Assert.assertNotNull(certificateIssuer);

        certificateIssuer = keyVaultClient.getCertificateIssuerAsync(vault, issuerName, null).get();
        Assert.assertNotNull(certificateIssuer);

        List<CertificateIssuerItem> issuers = keyVaultClient.listCertificateIssuersAsync(vault, null).get();
        Assert.assertNotNull(issuers);

        keyVaultClient.deleteCertificateIssuerAsync(vault, issuerName, null).get();
    }

    @Test
    public void certificateContactsAsyncForAsyncOperationsTest() throws Exception {
        String vault = getVaultUri();

        Contact contact1 = new Contact();
        contact1.withName("James");
        contact1.withEmailAddress("james@contoso.com");
        contact1.withPhone("7777777777");

        Contact contact2 = new Contact();
        contact2.withName("Ethan");
        contact2.withEmailAddress("ethan@contoso.com");
        contact2.withPhone("8888888888");

        List<Contact> contactList = new ArrayList<Contact>();
        contactList.add(contact1);
        contactList.add(contact2);

        Contacts certificateContacts = new Contacts();
        certificateContacts.withContactList(contactList);

        Contacts contacts = keyVaultClient.setCertificateContactsAsync(vault, certificateContacts, (ServiceCallback<Contacts>) null).get();
        Assert.assertNotNull(contacts);

        contacts = keyVaultClient.getCertificateContactsAsync(vault, null).get();
        Assert.assertNotNull(contacts);

        keyVaultClient.deleteCertificateContactsAsync(vault, null).get();
    }
}
