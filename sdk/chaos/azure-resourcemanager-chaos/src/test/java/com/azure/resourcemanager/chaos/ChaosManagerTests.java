// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.chaos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.chaos.fluent.models.TargetInner;
import com.azure.resourcemanager.chaos.models.Target;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class ChaosManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private ChaosManager chaosManager;
    private KeyVaultManager keyVaultManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        chaosManager = ChaosManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .authenticate(credential, profile);

        keyVaultManager = KeyVaultManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void tesCreateChaosTarget() {
        Target target = null;
        String kvName = "kv" + randomPadding();
        try {
            // @embedStart
            keyVaultManager.vaults()
                .define(kvName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withRoleBasedAccessControl()
                .withSku(SkuName.STANDARD)
                .create();

            target = chaosManager.targets()
                .createOrUpdate(
                    resourceGroupName,
                    "microsoft.keyvault",
                    "vaults",
                    kvName,
                    "microsoft-keyvault",
                    new TargetInner()
                        .withLocation(REGION.name())
                        .withProperties(Collections.emptyMap())
                );
            // @embedEnd
            Assertions.assertEquals(target.name(), "microsoft-keyvault");
            Assertions.assertTrue(Objects.nonNull(chaosManager.targets().get(
                resourceGroupName, "microsoft.keyvault",
                "vaults", kvName, "microsoft-keyvault")));
            Assertions.assertTrue(chaosManager.targets().list(
                    resourceGroupName, "microsoft.keyvault",
                    "vaults", kvName)
                .stream().findAny().isPresent());
        } finally {
            if (target != null) {
                chaosManager.targets().delete(resourceGroupName,
                    "microsoft.keyvault",
                    "vaults",
                    kvName,
                    "microsoft-keyvault");
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
