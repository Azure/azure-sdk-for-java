// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SecretTests extends KeyVaultManagementTest {

    @Test
    @DoNotRecord
    public void canCRUDSecret() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        String vaultName = generateRandomResourceName("vault", 20);
        String secretName = generateRandomResourceName("secret", 20);

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .defineAccessPolicy()
                .forServicePrincipal(clientIdFromFile())
                .allowSecretAllPermissions()
                .attach()
                .create();

        Assertions.assertNotNull(vault);

        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        Secret secret = vault.secrets().define(secretName).withValue("Some secret value").create();

        Assertions.assertNotNull(secret);
        Assertions.assertNotNull(secret.id());
        Assertions.assertEquals("Some secret value", secret.getValue());

        secret = secret.update().withValue("Some updated value").apply();

        Assertions.assertEquals("Some updated value", secret.getValue());

        Iterable<Secret> versions = secret.listVersions();

        int count = 2;
        for (Secret version : versions) {
            if ("Some secret value".equals(version.getValue())) {
                count--;
            }
            if ("Some updated value".equals(version.getValue())) {
                count--;
            }
        }
        Assertions.assertEquals(0, count);
    }

    @Test
    @DoNotRecord
    public void canDisableSecret() throws Exception {
        if (skipInPlayback()) {
            return;
        }

        String vaultName = generateRandomResourceName("vault", 20);
        String secretName = generateRandomResourceName("secret", 20);

        Vault vault =
            keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .defineAccessPolicy()
                .forServicePrincipal(clientIdFromFile())
                .allowSecretAllPermissions()
                .attach()
                .create();

        Assertions.assertNotNull(vault);

        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        final String value1 = "Some secret value";
        final String value2 = "Other secret value";
        final String type2 = "Other type";

        // version
        Secret secret = vault.secrets().define(secretName)
            .withValue(value1)
            .create();
        String version1 = secret.attributes().getVersion();

        Assertions.assertNotNull(secret);
        Assertions.assertNotNull(secret.id());

        // new version
        Secret secret2 = vault.secrets().define(secretName)
            .withValue(value2)
            .withContentType(type2)
            .create();
        String version2 = secret2.attributes().getVersion();

        // disable secret
        vault.secrets().disableByNameAndVersion(secretName, version2);

        List<Secret> secrets = vault.secrets().list().stream().collect(Collectors.toList());
        Assertions.assertEquals(1, secrets.size());

        // list by version
        secrets = secrets.iterator().next().listVersions().stream().collect(Collectors.toList());
        Assertions.assertEquals(2, secrets.size());

        // find secrets
        secret = secrets.stream().filter(s -> s.attributes().getVersion().equals(version1)).findFirst().get();
        secret2 = secrets.stream().filter(s -> s.attributes().getVersion().equals(version2)).findFirst().get();

        // verify enabled/disabled
        Assertions.assertTrue(secret.enabled());
        Assertions.assertFalse(secret2.enabled());

        Assertions.assertEquals(value1, secret.getValue());

        // verify that disable secret does not support GET
        Assertions.assertThrows(ResourceModifiedException.class, secret2::getValue);

        // enable secret
        secret2 = vault.secrets().enableByNameAndVersion(secretName, version2);

        // can get secret value after enabling
        Assertions.assertEquals(value2, secret2.getValue());
        Assertions.assertEquals(type2, secret2.contentType());

        secret2 = vault.secrets().getById(secret2.id());
        Assertions.assertEquals(value2, secret2.getValue());
        Assertions.assertEquals(type2, secret2.contentType());
    }
}
