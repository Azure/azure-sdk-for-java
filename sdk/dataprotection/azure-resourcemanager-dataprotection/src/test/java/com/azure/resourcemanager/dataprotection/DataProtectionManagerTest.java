// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.dataprotection;

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
import com.azure.resourcemanager.dataprotection.models.AlertsState;
import com.azure.resourcemanager.dataprotection.models.AzureMonitorAlertSettings;
import com.azure.resourcemanager.dataprotection.models.BackupVault;
import com.azure.resourcemanager.dataprotection.models.BackupVaultResource;
import com.azure.resourcemanager.dataprotection.models.CrossSubscriptionRestoreSettings;
import com.azure.resourcemanager.dataprotection.models.CrossSubscriptionRestoreState;
import com.azure.resourcemanager.dataprotection.models.DppIdentityDetails;
import com.azure.resourcemanager.dataprotection.models.FeatureSettings;
import com.azure.resourcemanager.dataprotection.models.ImmutabilitySettings;
import com.azure.resourcemanager.dataprotection.models.ImmutabilityState;
import com.azure.resourcemanager.dataprotection.models.MonitoringSettings;
import com.azure.resourcemanager.dataprotection.models.SecuritySettings;
import com.azure.resourcemanager.dataprotection.models.SoftDeleteSettings;
import com.azure.resourcemanager.dataprotection.models.SoftDeleteState;
import com.azure.resourcemanager.dataprotection.models.StorageSetting;
import com.azure.resourcemanager.dataprotection.models.StorageSettingStoreTypes;
import com.azure.resourcemanager.dataprotection.models.StorageSettingTypes;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

public class DataProtectionManagerTest extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private DataProtectionManager dataProtectionManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        dataProtectionManager = DataProtectionManager
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
    public void testCreateBackupVault() {
        BackupVaultResource resource = null;
        try {
            String vaultName = "vault" + randomPadding();
            // @embedmeStart
            resource = dataProtectionManager
                    .backupVaults()
                    .define(vaultName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(resourceGroupName)
                    .withProperties(
                            new BackupVault()
                                    .withMonitoringSettings(
                                            new MonitoringSettings()
                                                    .withAzureMonitorAlertSettings(
                                                            new AzureMonitorAlertSettings()
                                                                    .withAlertsForAllJobFailures(AlertsState.ENABLED)))
                                    .withSecuritySettings(
                                            new SecuritySettings()
                                                    .withSoftDeleteSettings(
                                                            new SoftDeleteSettings()
                                                                    .withState(SoftDeleteState.ALWAYS_ON)
                                                                    .withRetentionDurationInDays(14.0D))
                                                    .withImmutabilitySettings(
                                                            new ImmutabilitySettings()
                                                                    .withState(ImmutabilityState.LOCKED)))
                                    .withStorageSettings(
                                            Collections.singletonList(
                                                    new StorageSetting()
                                                            .withDatastoreType(StorageSettingStoreTypes.VAULT_STORE)
                                                            .withType(StorageSettingTypes.LOCALLY_REDUNDANT)))
                                    .withFeatureSettings(
                                            new FeatureSettings()
                                                    .withCrossSubscriptionRestoreSettings(
                                                            new CrossSubscriptionRestoreSettings()
                                                                    .withState(CrossSubscriptionRestoreState.ENABLED))))
                    .withIdentity(new DppIdentityDetails().withType("systemAssigned"))
                    .create();
            // @embedmeEnd
            resource.refresh();
            Assertions.assertEquals(resource.name(), vaultName);
            Assertions.assertEquals(resource.name(), dataProtectionManager.backupVaults().getById(resource.id()).name());
            Assertions.assertTrue(dataProtectionManager.backupVaults().list().stream().count() > 0);
        } finally {
            if (resource != null) {
                dataProtectionManager.backupVaults().deleteById(resource.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

}
