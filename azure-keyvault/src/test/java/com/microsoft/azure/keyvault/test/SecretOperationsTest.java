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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;

public class SecretOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String SECRET_NAME  = "javaSecret";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";
    private static final int MAX_SECRETS = 4;
    private static final int PAGELIST_MAX_SECRETS = 3;

    @Test
    public void transparentAuthentication() throws Exception {

        // Create a secret on a vault.
        {
        	SecretBundle secret = keyVaultClient.setSecret(
        			new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE).build()).getBody();
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

        // Create a secret on a different vault. Secret Vault Data Plane returns
        // 401, which must be transparently handled by KeyVaultCredentials.
        {
        	SecretBundle secret = keyVaultClient.setSecret(
        			new SetSecretRequest.Builder(getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE).build()).getBody();
            validateSecret(secret, getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

    }

    @Test
    public void crudOperations() throws Exception {

    	SecretBundle secret;
        {
            // Create secret
        	secret = keyVaultClient.setSecret(
        			new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE).build()).getBody();
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

        // Secret identifier.
        SecretIdentifier secretId = new SecretIdentifier(secret.id());

        {
            // Get secret using kid WO version
        	SecretBundle readBundle = keyVaultClient.getSecret(secretId.baseIdentifier()).getBody();
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using full kid as defined in the bundle
        	SecretBundle readBundle = keyVaultClient.getSecret(secret.id()).getBody();
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using vault and secret name.
        	SecretBundle readBundle = keyVaultClient.getSecret(getVaultUri(), SECRET_NAME).getBody();
            compareSecrets(secret, readBundle);
        }

        {
            // Get secret using vault, secret name and version.
        	SecretBundle readBundle = keyVaultClient.getSecret(getVaultUri(), SECRET_NAME, secretId.version()).getBody();
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
        		  .withValue(null);	// The value doesn't get updated
        	
            // Update secret using the kid as defined in the bundle
            SecretBundle updatedSecret = keyVaultClient.updateSecret(
            		new UpdateSecretRequest
            			.Builder(secret.id())
	            			.withContentType(secret.contentType())
	            			.withAttributes(secret.attributes())
	            			.withTags(secret.tags())
	            			.build()).getBody();
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
	            			.withContentType(secret.contentType())
	            			.withAttributes(secret.attributes())
	            			.withTags(secret.tags())
	            			.build()).getBody();

            compareSecrets(secret, updatedSecret);
        }

        {
            // Delete secret
        	SecretBundle deleteBundle = keyVaultClient.deleteSecret(getVaultUri(), SECRET_NAME).getBody();
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
                    		new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME + i, SECRET_VALUE).build()).getBody();
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

        PagedList<SecretItem> listResult = keyVaultClient.getSecrets(getVaultUri(), PAGELIST_MAX_SECRETS).getBody();
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().getItems().size());

        HashSet<String> toDelete = new HashSet<String>();

        for (SecretItem item : listResult) {
            SecretIdentifier id = new SecretIdentifier(item.id());
            toDelete.add(id.name());
            secrets.remove(item.id());
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
                    		new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE).build()).getBody();
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

        PagedList<SecretItem> listResult = keyVaultClient.getSecretVersions(getVaultUri(), SECRET_NAME, PAGELIST_MAX_SECRETS).getBody();
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().getItems().size());

        listResult = keyVaultClient.getSecretVersions(getVaultUri(), SECRET_NAME).getBody();
        for (;;) {
        	for (SecretItem item : listResult) {
                secrets.remove(item.id());
            }
            String nextLink = listResult.nextPageLink();
            if (nextLink == null) {
                break;
            }
            keyVaultClient.getSecretVersionsNext(nextLink).getBody();
        }

        Assert.assertEquals(0, secrets.size());

        keyVaultClient.deleteSecret(getVaultUri(), SECRET_NAME);
    }

    private static void validateSecret(SecretBundle secret, String vault, String name, String value, String contentType) throws Exception {
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
