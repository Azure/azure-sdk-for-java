// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.DeletedSecretBundle;
import com.microsoft.azure.keyvault.models.DeletionRecoveryLevel;
import com.microsoft.azure.keyvault.models.KeyVaultError;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.SecretAttributes;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;

public class SecretOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String CRUD_SECRET_NAME = "crudSecret";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";
    private static final int MAX_SECRETS = 4;
    private static final int PAGELIST_MAX_SECRETS = 3;

    @Test
    public void transparentAuthenticationForSecretOperationsTest() throws Exception {
        // Create a secret on a vault.
        Attributes attributes = new SecretAttributes().withEnabled(true)
                .withExpires(new DateTime().withYear(2050).withMonthOfYear(1))
                .withNotBefore(new DateTime().withYear(2000).withMonthOfYear(1));
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("foo", "baz");
        String contentType = "contentType";

        SecretBundle secret = keyVaultClient
                .setSecret(new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, SECRET_VALUE)
                        .withAttributes(attributes).withContentType(contentType).withTags(tags).build());
        validateSecret(secret, getVaultUri(), SECRET_NAME, SECRET_VALUE, contentType, attributes);

        // Create a secret on a different vault. Secret Vault Data Plane returns
        // 401, which must be transparently handled by KeyVaultCredentials.
        SecretBundle secret2 = alternativeKeyVaultClient
                .setSecret(new SetSecretRequest.Builder(getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE).build());
        validateSecret(secret2, getSecondaryVaultUri(), SECRET_NAME, SECRET_VALUE, null, null);
    }

    @Test
    public void deserializeWithExtraFieldTestForSecretOperationsTest() throws Exception {
        String content = "{\"error\":{\"code\":\"SecretNotFound\",\"message\":\"Secret not found: javaSecretTemp\",\"noneexisting\":true}}";
        KeyVaultError error = keyVaultClient.serializerAdapter().deserialize(content, KeyVaultError.class);
        Assert.assertEquals(error.error().message(), "Secret not found: javaSecretTemp");
        Assert.assertEquals(error.error().code(), "SecretNotFound");
    }

    @Test
    // verifies the inner error on disabled secret
    public void disabledSecretGetForSecretOperationsTest() throws Exception {

        String secretName = "disabledsecret";
        SecretBundle secret = keyVaultClient
                .setSecret(new SetSecretRequest.Builder(getVaultUri(), secretName, SECRET_VALUE)
                        .withAttributes(new SecretAttributes().withEnabled(false)).build());
        try {
            keyVaultClient.getSecret(secret.id());

            Assert.fail("Should throw exception for disabled secret.");
        } catch (KeyVaultErrorException e) {
            Assert.assertEquals(e.body().error().code(), "Forbidden");
            Assert.assertNotNull(e.body().error().message());
            Assert.assertNotNull(e.body().error().innerError());
            Assert.assertEquals(e.body().error().innerError().code(), "SecretDisabled");
        } catch (Exception e) {
            Assert.fail("Should throw KeyVaultErrorException for disabled secret.");
        }

        keyVaultClient.deleteSecret(getVaultUri(), secretName);
        // Polling on secret is disabled.
        SdkContext.sleep(40000);
        keyVaultClient.purgeDeletedSecret(getVaultUri(), secretName);
    }

    @Test
    public void crudOperationsForSecretOperationsTest() throws Exception {
        // Create secret
        SecretBundle secret = keyVaultClient
                .setSecret(new SetSecretRequest.Builder(getVaultUri(), CRUD_SECRET_NAME, SECRET_VALUE).build());
        validateSecret(secret, getVaultUri(), CRUD_SECRET_NAME, SECRET_VALUE, null, null);

        // Secret identifier.
        SecretIdentifier secretId = new SecretIdentifier(secret.id());

        // Get secret using kid WO version
        SecretBundle readBundle = keyVaultClient.getSecret(secretId.baseIdentifier());
        compareSecrets(secret, readBundle);

        // Get secret using full kid as defined in the bundle
        SecretBundle secretBundle = keyVaultClient.getSecret(secret.id());
        compareSecrets(secret, secretBundle);

        // Get secret using vault and secret name.
        SecretBundle secretNameBundle = keyVaultClient.getSecret(getVaultUri(), CRUD_SECRET_NAME);
        compareSecrets(secret, secretNameBundle);

        // Get secret using vault, secret name and version.
        SecretBundle secretNameVersion = keyVaultClient.getSecret(getVaultUri(), CRUD_SECRET_NAME, secretId.version());
        compareSecrets(secret, secretNameVersion);

        secret.attributes().withExpires(new DateTime().withMonthOfYear(2).withDayOfMonth(1).withYear(2050));
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "baz");
        secret.withTags(tags).withContentType("application/html").withValue(null); // The value doesn't get updated

        // Update secret using the kid as defined in the bundle
        SecretBundle updatedSecret = keyVaultClient
                .updateSecret(new UpdateSecretRequest.Builder(secret.id()).withContentType(secret.contentType())
                        .withAttributes(secret.attributes()).withTags(secret.tags()).build());
        compareSecrets(secret, updatedSecret);

        // Subsequent operations must use the updated bundle for comparison.
        secret = updatedSecret;

        // Update secret using vault and secret name.
        secret.attributes().withNotBefore(new DateTime().withMonthOfYear(2).withDayOfMonth(1).withYear(2000));
        Map<String, String> tags2 = new HashMap<String, String>();
        tags2.put("rex", "woof");
        secret.withTags(tags2).withContentType("application/html");

        // Perform the operation.
        SecretBundle updatedSecret2 = keyVaultClient
                .updateSecret(new UpdateSecretRequest.Builder(getVaultUri(), CRUD_SECRET_NAME)
                        .withVersion(secret.secretIdentifier().version()).withContentType(secret.contentType())
                        .withAttributes(secret.attributes()).withTags(secret.tags()).build());

        compareSecrets(secret, updatedSecret2);
        validateSecret(updatedSecret2, secret.secretIdentifier().vault(), secret.secretIdentifier().name(), null,
                secret.contentType(), secret.attributes());

        // Delete secret
        SecretBundle deleteBundle = keyVaultClient.deleteSecret(getVaultUri(), CRUD_SECRET_NAME);
        pollOnSecretDeletion(getVaultUri(), CRUD_SECRET_NAME);
        compareSecrets(secret, deleteBundle);

        // Expects a secret not found
        try {
            keyVaultClient.getSecret(secretId.baseIdentifier());
        } catch (KeyVaultErrorException e) {
            Assert.assertNotNull(e.body().error().code());
            Assert.assertEquals("SecretNotFound", e.body().error().code());
        }

        // Purge secret
        keyVaultClient.purgeDeletedSecret(getVaultUri(), CRUD_SECRET_NAME);
    }

    @Test
    public void listSecretsForSecretOperationsTest() throws Exception {
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
                    if (e.body().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        SdkContext.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<SecretItem> listResult = keyVaultClient.listSecrets(getVaultUri(), PAGELIST_MAX_SECRETS);
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().items().size());

        HashSet<String> toDelete = new HashSet<String>();

        for (SecretItem item : listResult) {
            if (item != null) {
                SecretIdentifier id = new SecretIdentifier(item.id());
                toDelete.add(id.name());
                secrets.remove(item.id());
            }
        }

        Assert.assertEquals(0, secrets.size());

        for (String secretName : toDelete) {
            try {
                System.out.println("Deleting next secret:" + secretName);

                keyVaultClient.deleteSecret(getVaultUri(), secretName);
                DeletedSecretBundle deletedSecretBundle = pollOnSecretDeletion(getVaultUri(), secretName);
                Assert.assertNotNull(deletedSecretBundle);
                keyVaultClient.purgeDeletedSecret(getVaultUri(), secretName);
                SdkContext.sleep(20000);
            } catch (KeyVaultErrorException e) {
                // Ignore forbidden exception for certificate secrets that cannot be deleted
                if (!e.body().error().code().equals("Forbidden")) {
                    throw e;
                }
            }
        }
    }

    @Test
    public void listSecretVersionsForSecretOperationsTest() throws Exception {
        final String listVersionSecretName = "javaSecretVersions";
        HashSet<String> secrets = new HashSet<String>();
        for (int i = 0; i < MAX_SECRETS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    SecretBundle secret = keyVaultClient
                            .setSecret(new SetSecretRequest.Builder(getVaultUri(), listVersionSecretName, SECRET_VALUE).build());
                    secrets.add(secret.id());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.body().error().code().equals("Throttled")) {
                        System.out.println("Throttled!");
                        SdkContext.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<SecretItem> listResult = keyVaultClient.listSecretVersions(getVaultUri(), listVersionSecretName,
                PAGELIST_MAX_SECRETS);
        Assert.assertTrue(PAGELIST_MAX_SECRETS >= listResult.currentPage().items().size());

        listResult = keyVaultClient.listSecretVersions(getVaultUri(), listVersionSecretName);
        for (SecretItem item : listResult) {
            if (item != null) {
                secrets.remove(item.id());
            }
        }

        Assert.assertEquals(0, secrets.size());

        keyVaultClient.deleteSecret(getVaultUri(), listVersionSecretName);
        pollOnSecretDeletion(getVaultUri(), listVersionSecretName);
        keyVaultClient.purgeDeletedSecret(getVaultUri(), listVersionSecretName);

    }

    private static void validateSecret(SecretBundle secret, String vault, String name, String value, String contentType,
            Attributes attributes) throws Exception {
        String prefix = vault + "/secrets/" + name + "/";
        String id = secret.id();
        Assert.assertTrue(
                String.format("\"id\" should start with \"%s\", but instead the value is \"%s\".", prefix, id), //
                id.startsWith(prefix));
        Assert.assertEquals(value, secret.value());
        if (contentType != null) {
            Assert.assertEquals(contentType, secret.contentType());
        }
        Assert.assertNotNull("\"created\" should not be null.", secret.attributes().created());
        Assert.assertNotNull("\"updated\" should not be null.", secret.attributes().updated());
        DeletionRecoveryLevel deletionRecoveryLevel = secret.attributes().recoveryLevel();
        Assert.assertNotNull(deletionRecoveryLevel);

        Assert.assertTrue(secret.managed() == null || !secret.managed());
    }

    private void compareSecrets(SecretBundle expected, SecretBundle actual) {
        Assert.assertEquals(expected.contentType(), actual.contentType());
        Assert.assertEquals(expected.id(), actual.id());
        Assert.assertEquals(expected.value(), actual.value());
        Assert.assertEquals(expected.attributes().enabled(), actual.attributes().enabled());
        if (expected.tags() != null || actual.tags() != null) {
            Assert.assertEquals(expected.tags(), actual.tags());
        }
    }
}
