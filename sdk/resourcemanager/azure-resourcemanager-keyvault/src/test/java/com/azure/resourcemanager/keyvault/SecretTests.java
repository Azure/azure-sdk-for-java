// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
        Assertions.assertEquals("Some secret value", secret.value());

        secret = secret.update().withValue("Some updated value").apply();

        Assertions.assertEquals("Some updated value", secret.value());

        Iterable<Secret> versions = secret.listVersions();

        int count = 2;
        for (Secret version : versions) {
            if ("Some secret value".equals(version.value())) {
                count--;
            }
            if ("Some updated value".equals(version.value())) {
                count--;
            }
        }
        Assertions.assertEquals(0, count);
    }
}
