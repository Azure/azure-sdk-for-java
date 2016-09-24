/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.test;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.BackupKeyResult;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.CertificateIssuerItem;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.keyvault.models.CertificateOperation;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
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


public class AsyncOperationsTest extends KeyVaultClientIntegrationTestBase {
    
    @Test
    public void keyAsync() throws Exception {

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
        
        List<KeyItem> keyVersionItems = keyVaultClient.listKeyVersionsAsync(getVaultUri(), keyname, 2, null).get();
        Assert.assertNotNull(keyVersionItems);

        BackupKeyResult backupResult = keyVaultClient.backupKeyAsync(vault, keyname, null).get();
        Assert.assertNotNull(backupResult);        

        keyVaultClient.deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null).get();
        
        KeyBundle restoreResult = keyVaultClient.restoreKeyAsync(vault, backupResult.value(), null).get();
        Assert.assertNotNull(restoreResult);
        
        KeyOperationResult encryptResult = keyVaultClient.encryptAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, new byte[100], null).get();
        Assert.assertNotNull(encryptResult);
        
        KeyOperationResult decryptResult = keyVaultClient.decryptAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, encryptResult.result(), null).get();
        Assert.assertNotNull(decryptResult);
        
        KeyOperationResult wrapResult = keyVaultClient.wrapKeyAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, new byte[100], null).get();
        Assert.assertNotNull(wrapResult);
        
        KeyOperationResult unwrapResult = keyVaultClient.unwrapKeyAsync(keyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA_OAEP, wrapResult.result(), null).get();
        Assert.assertNotNull(unwrapResult);
        
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plainText);
        byte[] digest = md.digest();
        KeyOperationResult signResult = keyVaultClient.signAsync(keyBundle.key().kid(), JsonWebKeySignatureAlgorithm.RS256, digest, null).get();
        Assert.assertNotNull(signResult);
        
        KeyVerifyResult verifypResult = keyVaultClient.verifyAsync(keyBundle.key().kid(), JsonWebKeySignatureAlgorithm.RS256, digest, signResult.result(), null).get();
        Assert.assertTrue(verifypResult.value());

        keyBundle = keyVaultClient.deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null).get();
        Assert.assertNotNull(keyBundle);
        
        //Get the unavailable key to throw exception -> it gets stuck

        try {
            keyVaultClient.deleteKeyAsync(keyBundle.keyIdentifier().vault(), keyBundle.keyIdentifier().name(), null).get();
        } catch (ExecutionException ex) {

            Throwable t = ex.getCause();
            if(t instanceof KeyVaultErrorException)
            {
                Assert.assertEquals("KeyNotFound", ((KeyVaultErrorException) t).getBody().error().code());
            }
            else throw ex;
        }
    }
    
    @Test
    public void secretAsync() throws Exception {

        String vault = getVaultUri();
        String secretname = "mySecret";
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
            if(t instanceof KeyVaultErrorException)
            {
                Assert.assertEquals("SecretNotFound", ((KeyVaultErrorException) t).getBody().error().code());
            }
            else throw ex;
        }
    }
    
    @Test
    public void certificateAsync() throws Exception {

        String vault = getVaultUri();
        String certificateName = "myCertificate";

        CreateCertificateRequest createCertificateRequest = 
            new CreateCertificateRequest
                .Builder(vault, certificateName)
                .withPolicy(new CertificatePolicy()
                .withSecretProperties(new SecretProperties().withContentType("application/x-pkcs12"))
                .withIssuerParameters(new IssuerParameters().withName("Self"))
                .withX509CertificateProperties(new X509CertificateProperties()
                .withSubject("CN=SelfSignedJavaPkcs12")
                .withValidityInMonths(12)))
                .build();
        CertificateOperation certificateOperation = keyVaultClient.createCertificateAsync(createCertificateRequest, null).get();
        Assert.assertNotNull(certificateOperation);
        
        UpdateCertificateOperationRequest updateCertificateOperationRequest = new UpdateCertificateOperationRequest.Builder(vault, certificateName, false).build();
        certificateOperation = keyVaultClient.updateCertificateOperationAsync(updateCertificateOperationRequest, null).get();
        Assert.assertNotNull(certificateOperation);
        
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("tag1", "foo");
        UpdateCertificateRequest updateCertificateRequest = new UpdateCertificateRequest.Builder(vault, certificateName).withTags(tags).build();
        CertificateBundle certificateBundle = keyVaultClient.updateCertificateAsync(updateCertificateRequest, null).get();
        Assert.assertNotNull(certificateBundle);
        
        UpdateCertificatePolicyRequest updateCertificatePolicyRequest = new UpdateCertificatePolicyRequest.Builder(vault, certificateName).build();
        CertificatePolicy certificatePolicy = keyVaultClient.updateCertificatePolicyAsync(updateCertificatePolicyRequest, null).get();
        Assert.assertNotNull(certificatePolicy);
        
        certificatePolicy = keyVaultClient.getCertificatePolicyAsync(vault, certificateName, null).get();
        Assert.assertNotNull(certificatePolicy);
        
        certificateOperation = keyVaultClient.getCertificateOperationAsync(vault, certificateName, null).get(); 
        Assert.assertNotNull(certificateOperation);

        certificateBundle = keyVaultClient.getCertificateAsync(vault, certificateName, null).get(); 
        Assert.assertNotNull(certificateBundle);
        
        String cert = keyVaultClient.getPendingCertificateSigningRequestAsync(vault, certificateName, null).get();
        Assert.assertTrue(!cert.isEmpty());
        
        List<CertificateItem> certificateItem = keyVaultClient.listCertificatesAsync(vault, null).get();
        Assert.assertNotNull(certificateItem);
        
        List<CertificateItem> certificateVersionItem = keyVaultClient.listCertificateVersionsAsync(vault, certificateName, null).get();
        Assert.assertNotNull(certificateVersionItem);

        
        keyVaultClient.deleteCertificateOperationAsync(vault, certificateName, null).get();        
        keyVaultClient.deleteCertificateAsync(vault, certificateName, null).get();
        
        try {
            keyVaultClient.deleteCertificateAsync(vault, certificateName, null).get();
        } catch (ExecutionException ex) {

            Throwable t = ex.getCause();
            if(t instanceof KeyVaultErrorException)
            {
                Assert.assertEquals("CertificateNotFound", ((KeyVaultErrorException) t).getBody().error().code());
            }
            else throw ex;
        }
    }
    
    @Test
    public void issuerAsync() throws Exception {

        String vault = getVaultUri();
        String issuerName = "myIssuer";
        
        SetCertificateIssuerRequest setCertificateIssuerRequest = new SetCertificateIssuerRequest.Builder(vault, issuerName, "Test").build();
        IssuerBundle certificateIssuer = keyVaultClient.setCertificateIssuerAsync(setCertificateIssuerRequest, null).get();
        Assert.assertNotNull(certificateIssuer);        

        UpdateCertificateIssuerRequest updateCertificateIssuerRequest = new UpdateCertificateIssuerRequest.Builder(vault, issuerName).withProvider("SslAdmin").build();
        certificateIssuer = keyVaultClient.updateCertificateIssuerAsync(updateCertificateIssuerRequest, null).get();
        Assert.assertNotNull(certificateIssuer);
        
        certificateIssuer = keyVaultClient.getCertificateIssuerAsync(vault, issuerName, null).get();
        Assert.assertNotNull(certificateIssuer);

        List<CertificateIssuerItem> issuers = keyVaultClient.listCertificateIssuersAsync(vault, null).get();
        Assert.assertNotNull(issuers);
        
        keyVaultClient.deleteCertificateIssuerAsync(vault, issuerName, null).get();
    }
    

    @Test
    public void certificateContactsAsync() throws Exception {

        String vault = getVaultUri();
        
        Contacts contacts = keyVaultClient.setCertificateContactsAsync(vault, new Contacts(), null).get();
        Assert.assertNotNull(contacts);
        
        contacts = keyVaultClient.getCertificateContactsAsync(vault, null).get();
        Assert.assertNotNull(contacts);
        
        keyVaultClient.deleteCertificateContactsAsync(vault, null).get();
    }
}
