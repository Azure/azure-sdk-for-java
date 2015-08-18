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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.ListSecretsResponseMessage;
import com.microsoft.azure.keyvault.models.Secret;
import com.microsoft.azure.keyvault.models.SecretIdentifier;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.windowsazure.exception.ServiceException;

public class SecretOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String SECRET_NAME  = "javaSecret";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

    @Test
    public void transparentAuthentication() throws Exception {

        if (!handling401) {
            // TODO: Is there a way to report "not tested" without generating a
            // failure?
            return;
        }

        // Create a secret on a vault.
        {
            Future<Secret> result = keyVaultClient.setSecretAsync(getVaultUri(), SECRET_NAME, SECRET_VALUE, null, null, null);
            Secret secret = result.get();
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

        // Create a secret on a different vault. Secret Vault Data Plane returns
        // 401, which must be transparently handled by KeyVaultCredentials.
        {
            Future<Secret> result = keyVaultClient.setSecretAsync(getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE, null, null, null);
            Secret bundle = result.get();
            validateSecret(bundle, getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

    }

    @SuppressWarnings("serial")
    @Test
    public void crudOperations() throws Exception {

        Secret secret;
        {
            // Create secret
            Future<Secret> result = keyVaultClient.setSecretAsync(getVaultUri(), SECRET_NAME, SECRET_VALUE, null, null, null);
            secret = result.get();
            validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, null);
        }

        // Secret identifier.
        SecretIdentifier secretId = new SecretIdentifier(secret.getId());

        {
            // Get secret using kid WO version
            Future<Secret> result = keyVaultClient.getSecretAsync(secretId.getBaseIdentifier());
            Secret readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get secret using full kid as defined in the bundle
            Future<Secret> result = keyVaultClient.getSecretAsync(secret.getId());
            Secret readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get secret using vault and secret name.
            Future<Secret> result = keyVaultClient.getSecretAsync(getVaultUri(), SECRET_NAME, null);
            Secret readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get secret using vault, secret name and version.
            Future<Secret> result = keyVaultClient.getSecretAsync(getVaultUri(), SECRET_NAME, secretId.getVersion());
            Secret readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get secret using vault, secret name and a null version.
            Future<Secret> result = keyVaultClient.getSecretAsync(getVaultUri(), SECRET_NAME, null);
            Secret readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Update secret using the kid as defined in the bundle

            // First we create a bundle with the modified attributes.
            Secret updatingSecret = cloneWithJson(secret); // Start with a copy
                                                           // of original
                                                           // bundle.
            updatingSecret.setValue(null); // Updating does not return the
                                           // value, so we don't compare.
            // updatingSecret.setContentType("application/html"); possible
            // service bug
            updatingSecret.getAttributes().setExpires(newDate(2050, 1, 2));
            updatingSecret.setTags(new HashMap<String, String>() {
                {
                    put("foo", "baz");
                }
            });

            // Perform the operation.
            Future<Secret> result = keyVaultClient.updateSecretAsync(secret.getId(), updatingSecret.getContentType(), updatingSecret.getAttributes(), updatingSecret.getTags());
            Secret updatedSecret = result.get();

            // Compare the JSON value.
            updatingSecret.getAttributes().setUpdatedUnixTime(updatedSecret.getAttributes().getUpdatedUnixTime()); // Nonsense
                                                                                                                   // comparing
                                                                                                                   // this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingSecret), jsonWriter.writeValueAsString(updatedSecret));

            // Subsequent operations must use the updated bundle for comparison.
            secret = updatedSecret;
        }

        {
            // Update secret using vault and secret name.

            // First we create a bundle with the modified attributes.
            Secret updatingSecret = cloneWithJson(secret); // Start with a copy
                                                           // of original
                                                           // bundle.
            updatingSecret.getAttributes().setNotBefore(newDate(2000, 1, 2));
            updatingSecret.setTags(new HashMap<String, String>() {
                {
                    put("rex", "woof");
                }
            });

            // Perform the operation.
            Future<Secret> result = keyVaultClient.updateSecretAsync(getVaultUri(), SECRET_NAME, updatingSecret.getContentType(), updatingSecret.getAttributes(), updatingSecret.getTags());
            Secret updatedSecret = result.get();

            // Compare the JSON value.
            updatingSecret.getAttributes().setUpdatedUnixTime(updatedSecret.getAttributes().getUpdatedUnixTime()); // Nonsense
                                                                                                                   // comparing
                                                                                                                   // this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingSecret), jsonWriter.writeValueAsString(updatedSecret));

            // Subsequent operations must use the updated bundle for comparison.
            secret = updatedSecret;
        }

        {
            // Delete secret
            Future<Secret> result = keyVaultClient.deleteSecretAsync(getVaultUri(), SECRET_NAME);
            Secret deleteBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(secret), jsonWriter.writeValueAsString(deleteBundle));
        }

        {
            // Expects a secret not found
            Future<Secret> result = keyVaultClient.getSecretAsync(secretId.getBaseIdentifier());
            try {
                result.get();
            } catch (ExecutionException e) {
                ServiceException cause = (ServiceException) e.getCause();
                Assert.assertNotNull(cause.getError());
                Assert.assertEquals("SecretNotFound", cause.getError().getCode());
            }
        }

    }

    @Test
    public void listSecrets() throws Exception {

        HashSet<String> secrets = new HashSet<String>();
        for (int i = 0; i < 50; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    Future<Secret> result = keyVaultClient.setSecretAsync(getVaultUri(), SECRET_NAME + i, SECRET_VALUE, null, null, null);
                    Secret secret = result.get();
                    SecretIdentifier id = new SecretIdentifier(secret.getId());
                    secrets.add(id.getBaseIdentifier());
                    break;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ServiceException) {
                        ++failureCount;
                        ServiceException se = (ServiceException) e.getCause();
                        if (se.getError().getCode().equals("Throttled")) {
                            System.out.println("Waiting to avoid throttling");
                            if (!IS_MOCKED) {
                                Thread.sleep(failureCount * 1500);
                            }
                            continue;
                        }
                    }
                    throw e;
                }
            }
        }

        Future<ListSecretsResponseMessage> promise = keyVaultClient.getSecretsAsync(getVaultUri(), 11);
        ListSecretsResponseMessage listResult = promise.get();
        Assert.assertEquals(11, listResult.getValue().length);

        HashSet<String> toDelete = new HashSet<String>();

        promise = keyVaultClient.getSecretsAsync(getVaultUri(), null);
        listResult = promise.get();
        for (;;) {
            for (SecretItem item : listResult.getValue()) {
                SecretIdentifier id = new SecretIdentifier(item.getId());
                toDelete.add(id.getName());
                secrets.remove(item.getId());
            }
            String nextLink = listResult.getNextLink();
            if (nextLink == null) {
                break;
            }
            promise = keyVaultClient.getSecretsNextAsync(nextLink);
            listResult = promise.get();
        }

        Assert.assertEquals(0, secrets.size());

        for (String secretName : toDelete) {
            Future<Secret> delPromise = keyVaultClient.deleteSecretAsync(getVaultUri(), secretName);
            delPromise.get();
        }
    }

    @Test
    public void listSecretVersions() throws Exception {

        HashSet<String> secrets = new HashSet<String>();
        for (int i = 0; i < 50; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    Future<Secret> result = keyVaultClient.setSecretAsync(getVaultUri(), SECRET_NAME, SECRET_VALUE, null, null, null);
                    Secret secret = result.get();
                    secrets.add(secret.getId());
                    break;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ServiceException) {
                        ++failureCount;
                        ServiceException se = (ServiceException) e.getCause();
                        if (se.getError().getCode().equals("Throttled")) {
                            System.out.println("Throttled!");
                            if (!IS_MOCKED) {
                                Thread.sleep(failureCount * 1500);
                            }
                            continue;
                        }
                    }
                    throw e;
                }
            }
        }

        Future<ListSecretsResponseMessage> promise = keyVaultClient.getSecretVersionsAsync(getVaultUri(), SECRET_NAME, 11);
        ListSecretsResponseMessage listResult = promise.get();
        Assert.assertEquals(11, listResult.getValue().length);

        promise = keyVaultClient.getSecretVersionsAsync(getVaultUri(), SECRET_NAME, null);
        listResult = promise.get();
        for (;;) {
            for (SecretItem item : listResult.getValue()) {
                secrets.remove(item.getId());
            }
            String nextLink = listResult.getNextLink();
            if (nextLink == null) {
                break;
            }
            promise = keyVaultClient.getSecretVersionsNextAsync(nextLink);
            listResult = promise.get();
        }

        Assert.assertEquals(0, secrets.size());

        Future<Secret> delPromise = keyVaultClient.deleteSecretAsync(getVaultUri(), SECRET_NAME);
        delPromise.get();
    }

    private static Secret cloneWithJson(Secret template) throws Exception {
        String json = jsonWriter.writeValueAsString(template);
        return jsonReader.withType(Secret.class).readValue(json);
    }

    private static Date newDate(int y, int m, int d) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        calendar.clear();
        calendar.set(y, m, d);
        return calendar.getTime();
    }

    private static void validateSecret(Secret secret, String vault, String name, String value, String contentType) throws Exception {
        String prefix = vault + "/secrets/" + name + "/";
        String id = secret.getId();
        Assert.assertTrue( //
                String.format("\"id\" should start with \"%s\", but instead the value is \"%s\".", prefix, id), //
                id.startsWith(prefix));
        Assert.assertEquals(value, secret.getValue());
        if (contentType != null) {
            Assert.assertEquals(contentType, secret.getContentType());
        }
        Assert.assertNotNull("\"created\" should not be null.", secret.getAttributes().getCreated());
        Assert.assertNotNull("\"updated\" should not be null.", secret.getAttributes().getUpdated());
    }

}
