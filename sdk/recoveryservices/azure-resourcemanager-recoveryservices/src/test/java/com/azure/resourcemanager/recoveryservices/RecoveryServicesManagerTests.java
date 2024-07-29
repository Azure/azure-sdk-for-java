// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.recoveryservices;

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
import com.azure.resourcemanager.recoveryservices.models.CrossSubscriptionRestoreSettings;
import com.azure.resourcemanager.recoveryservices.models.CrossSubscriptionRestoreState;
import com.azure.resourcemanager.recoveryservices.models.ImmutabilitySettings;
import com.azure.resourcemanager.recoveryservices.models.ImmutabilityState;
import com.azure.resourcemanager.recoveryservices.models.PublicNetworkAccess;
import com.azure.resourcemanager.recoveryservices.models.RestoreSettings;
import com.azure.resourcemanager.recoveryservices.models.SecuritySettings;
import com.azure.resourcemanager.recoveryservices.models.Sku;
import com.azure.resourcemanager.recoveryservices.models.SkuName;
import com.azure.resourcemanager.recoveryservices.models.Vault;
import com.azure.resourcemanager.recoveryservices.models.VaultProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class RecoveryServicesManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private RecoveryServicesManager recoveryServicesManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        recoveryServicesManager = RecoveryServicesManager
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
    public void testCreateVault() {
        Vault vault = null;
        try {
            String vaultName = "vault" + randomPadding();
            // @embedmeStart
            vault = recoveryServicesManager.vaults()
                .define(vaultName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new Sku().withName(SkuName.RS0).withTier("Standard"))
                .withProperties(new VaultProperties()
                    .withSecuritySettings(new SecuritySettings()
                        .withImmutabilitySettings(
                            new ImmutabilitySettings()
                                .withState(ImmutabilityState.UNLOCKED)))
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withRestoreSettings(new RestoreSettings()
                        .withCrossSubscriptionRestoreSettings(
                            new CrossSubscriptionRestoreSettings()
                                .withCrossSubscriptionRestoreState(CrossSubscriptionRestoreState.ENABLED)))
                )
                .create();
            // @embedmeEnd
            vault.refresh();
            Assertions.assertEquals(vault.name(), vaultName);
            Assertions.assertEquals(vault.name(), recoveryServicesManager.vaults().getById(vault.id()).name());
            Assertions.assertTrue(recoveryServicesManager.vaults().list().stream().count() > 0);
        } finally {
            if (vault != null) {
                recoveryServicesManager.vaults().deleteById(vault.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
