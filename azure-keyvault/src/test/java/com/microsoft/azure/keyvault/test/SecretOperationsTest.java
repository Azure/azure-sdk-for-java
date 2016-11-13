/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.KeyVaultError;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.SecretAttributes;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;

public class SecretOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String SECRET_NAME  = "javaSecret";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";
    private static final int MAX_SECRETS = 4;
    private static final int PAGELIST_MAX_SECRETS = 3;

    @Test
    public void transparentAuthentication() throws Exception {

        // Create a secret on a vault.
        {
            Attributes attributes = new SecretAttributes()
                    .withEnabled(true)
                    .withExpires(new DateTime().withYear(2050).withMonthOfYear(1))
                    .withNotBefore(new DateTime().withYear(2000).withMonthOfYear(1));
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("foo", "baz");
            String contentType = "contentType";
            
            SecretBundle secret = keyVaultClient.setSecret(
                    new SetSecretRequest
                        .Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE)
                        .withAttributes(attributes)
                        .withContentType(contentType)
                        .withTags(tags)
                        .build());
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, contentType, attributes);
        }

        // Create a secret on a different vault. Secret Vault Data Plane returns
        // 401, which must be transparently handled by KeyVaultCredentials.
        {
            SecretBundle secret = keyVaultClient.setSecret(
                    new SetSecretRequest.Builder(getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE).build());
            validateSecret(secret, getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE, null, null);
        }

    }

    @Test
    public void deserializeWithExtraFieldTest() throws Exception {
        AzureJacksonMapperAdapter mapperAdapter = new AzureJacksonMapperAdapter();
        ObjectMapper mapper = mapperAdapter.getObjectMapper();
        KeyVaultError error = mapper.readValue("{\"error\":{\"code\":\"SecretNotFound\",\"message\":\"Secret not found: javaSecret\",\"noneexisting\":true}}", KeyVaultError.class);
        Assert.assertEquals(error.error().message(), "Secret not found: javaSecret");
        Assert.assertEquals(error.error().code(), "SecretNotFound");
    }
    
    @Test
    // verifies the inner error on disabled secret
    public void disabledSecretGet() throws Exception {

        String secretName = "disabledsecret";
        SecretBundle secret = keyVaultClient.setSecret(
                new SetSecretRequest
                    .Builder(getVaultUri(), secretName, SECRET_VALUE)
                    .withAttributes(new SecretAttributes().withEnabled(false))
                    .build());
        try {
            keyVaultClient.getSecret(secret.id());
            Assert.fail("Should throw exception for disabled secret.");
        }
        catch (KeyVaultErrorException e) {
            Assert.assertEquals(e.getBody().error().code(), "Forbidden");
            Assert.assertNotNull(e.getBody().error().message());
            Assert.assertNotNull(e.getBody().error().innerError());
            Assert.assertEquals(e.getBody().error().innerError().code(), "SecretDisabled");
        }
        catch (Exception e) {
            Assert.fail("Should throw KeyVaultErrorException for disabled secret.");
        }
        keyVaultClient.deleteSecret(getVaultUri(), secretName);
    }
    
    @Test
    public void crudOperations() throws Exception {

        SecretBundle secret;
        {
            // Create secret
            secret = keyVaultClient.setSecret(
                    new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE).build());
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, null, null);
        }

        // Secret identifier.
        SecretIdentifier secretId = new SecretIdentifier(secret.id());

        {
            // Get secret using kid WO version
            SecretBundle readBundle = keyVaultClient.getSecret(secretId.baseIdentifier());
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using full kid as defined in the bundle
            SecretBundle readBundle = keyVaultClient.getSecret(secret.id());
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using vault and secret name.
            SecretBundle readBundle = keyVaultClient.getSecret(getVaultUri(), SECRET_NAME);
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using vault, secret name and version.
            SecretBundle readBundle = keyVaultClient.getSecret(getVaultUri(), SECRET_NAME, secretId.version());
            compareSecrets(secret, readBundle);
        }

        {
            secret.attributes().withExpires(new DateTime()
                                                  .withMonthOfYear(2)
                                                  .withDayOfMonth(1)
                                                  .withYear(2050));
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("foo", "baz");
            secret.withTags(tags)
                  .withContentType("application/html")
                  .withValue(null);    // The value doesn't get updated
            
            // Update secret using the kid as defined in the bundle
            SecretBundle updatedSecret = keyVaultClient.updateSecret(
                    new UpdateSecretRequest
                        .Builder(secret.id())
                            .withContentType(secret.contentType())
                            .withAttributes(secret.attributes())
                            .withTags(secret.tags())
                            .build());
            compareSecrets(secret, updatedSecret);

            // Subsequent operations must use the updated bundle for comparison.
            secret = updatedSecret;
        }

        {
            // Update secret using vault and secret name.

            secret.attributes().withNotBefore(new DateTime()
                                                    .withMonthOfYear(2)
                                                    .withDayOfMonth(1)
                                                    .withYear(2000));   
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("rex", "woof");
            secret.withTags(tags)
                  .withContentType("application/html");

            // Perform the operation.
            SecretBundle updatedSecret = keyVaultClient.updateSecret(
                    new UpdateSecretRequest
                        .Builder(getVaultUri(), SECRET_NAME)
                            .withVersion(secret.secretIdentifier().version())
                            .withContentType(secret.contentType())
                            .withAttributes(secret.attributes())
                            .withTags(secret.tags())
                            .build());

            compareSecrets(secret, updatedSecret);
            validateSecret(updatedSecret, 
                    secret.secretIdentifier().vault(), 
                    secret.secretIdentifier().name(), 
                    null, secret.contentType(), secret.attributes());
        }

        {
            // Delete secret
            SecretBundle deleteBundle = keyVaultClient.deleteSecret(getVaultUri(), SECRET_NAME);
            compareSecrets(secret, deleteBundle);
        }

        {
            // Expects a secret not found
            try {
                keyVaultClient.getSecret(secretId.baseIdentifier());
            } catch (KeyVaultErrorException e) {
                Assert.assertNotNull(e.getBody().error().code());
                Assert.assertEquals("SecretNotFound", e.getBody().error().code());
            }
        }

    }

    @Test
    public void listSecrets() throws Exception {        
        HashSet<String> secrets = new HashSet<String>();
        for (int i = 0; i < MAX_SECRETS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    SecretBundle secret = keyVaultClient.setSecret(
                            new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME + i, SECRET_VALUE).build());
                    SecretIdentifier id = new SecretIdentifier(secret.id());
                    secrets.add(id.baseIdentifier());
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

        PagedList<SecretItem> listResult = keyVaultClient.listSecrets(getVaultUri(), PAGELIST_MAX_SECRETS);
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().getItems().size());

        HashSet<String> toDelete = new HashSet<String>();

        for (SecretItem item : listResult) {
            if(item != null) {
                SecretIdentifier id = new SecretIdentifier(item.id());
                toDelete.add(id.name());
                secrets.remove(item.id());
            }
        }

        Assert.assertEquals(0, secrets.size());

        for (String secretName : toDelete) {
            try{
                keyVaultClient.deleteSecret(getVaultUri(), secretName);
            }
            catch(KeyVaultErrorException e){
                // Ignore forbidden exception for certificate secrets that cannot be deleted
                if(!e.getBody().error().code().equals("Forbidden"))
                    throw e;
            }
        }
    }

    @Test
    public void listSecretVersions() throws Exception {

        HashSet<String> secrets = new HashSet<String>();
        for (int i = 0; i < MAX_SECRETS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    SecretBundle secret = keyVaultClient.setSecret(
                            new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE).build());
                    secrets.add(secret.id());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.getBody().error().code().equals("Throttled")) {
                        System.out.println("Throttled!");
                        Thread.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<SecretItem> listResult = keyVaultClient.listSecretVersions(getVaultUri(), SECRET_NAME, PAGELIST_MAX_SECRETS);
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().getItems().size());

        listResult = keyVaultClient.listSecretVersions(getVaultUri(), SECRET_NAME);
        for (SecretItem item : listResult) {
            if(item != null) {
                secrets.remove(item.id());
            }
        }

        Assert.assertEquals(0, secrets.size());

        keyVaultClient.deleteSecret(getVaultUri(), SECRET_NAME);
    }

    private static void validateSecret(SecretBundle secret, String vault, String name, String value, String contentType, Attributes attributes) throws Exception {
        String prefix = vault + "/secrets/" + name + "/";
        String id = secret.id();
        Assert.assertTrue( //
                String.format("\"id\" should start with \"%s\", but instead the value is \"%s\".", prefix, id), //
                id.startsWith(prefix));
        Assert.assertEquals(value, secret.value());
        if (contentType != null) {
            Assert.assertEquals(contentType, secret.contentType());
        }
        Assert.assertNotNull("\"created\" should not be null.", secret.attributes().created());
        Assert.assertNotNull("\"updated\" should not be null.", secret.attributes().updated());
        
        compareAttributes(attributes, secret.attributes());

        Assert.assertTrue(secret.managed() == null || secret.managed() == false);
    }

    private void compareSecrets(SecretBundle expected, SecretBundle actual) {
        Assert.assertEquals(expected.contentType(), actual.contentType());
        Assert.assertEquals(expected.id(), actual.id());
        Assert.assertEquals(expected.value(), actual.value());
        Assert.assertEquals(expected.attributes().enabled(), actual.attributes().enabled());
        Assert.assertEquals(expected.attributes().expires(), actual.attributes().expires());
        Assert.assertEquals(expected.attributes().notBefore(), actual.attributes().notBefore());
        if(expected.tags() != null || actual.tags() != null)
            Assert.assertTrue(expected.tags().equals(actual.tags()));
        
    }

}
