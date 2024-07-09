// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.machinelearning;

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
import com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager;
import com.azure.resourcemanager.applicationinsights.models.ApplicationType;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import com.azure.resourcemanager.machinelearning.models.Workspace;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class MachineLearningManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private MachineLearningManager machineLearningManager;
    private StorageManager storageManager;
    private KeyVaultManager keyVaultManager;
    private ApplicationInsightsManager applicationInsightsManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        machineLearningManager = MachineLearningManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        storageManager = StorageManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        keyVaultManager = KeyVaultManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        applicationInsightsManager = ApplicationInsightsManager
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
    public void testCreateWorkSpace() {
        Workspace workspace = null;
        String randomPadding = randomPadding();
        try {
            String workspaceName = "workspace" + randomPadding;
            String storageName = "storage" + randomPadding;
            String keyVaultName = "vault" + randomPadding;
            String insightName = "insight" + randomPadding;
            // @embedmeStart
            workspace = machineLearningManager.workspaces()
                .define(workspaceName)
                .withExistingResourceGroup(resourceGroupName)
                .withRegion(REGION)
                .withSku(new Sku().withName("Basic").withTier(SkuTier.BASIC))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
                .withFriendlyName(workspaceName)
                .withStorageAccount(
                    storageManager.storageAccounts()
                        .define(storageName)
                        .withRegion(REGION)
                        .withExistingResourceGroup(resourceGroupName)
                        .withSku(StorageAccountSkuType.STANDARD_LRS)
                        .withMinimumTlsVersion(MinimumTlsVersion.TLS1_0)
                        .withHnsEnabled(false)
                        .withAccessFromAzureServices()
                        .withOnlyHttpsTraffic()
                        .withBlobStorageAccountKind().withAccessTier(AccessTier.HOT)
                        .create()
                        .id())
                .withKeyVault(
                    keyVaultManager.vaults()
                        .define(keyVaultName)
                        .withRegion(REGION)
                        .withExistingResourceGroup(resourceGroupName)
                        .withEmptyAccessPolicy()
                        .withSku(SkuName.STANDARD)
                        .withDeploymentDisabled()
                        .withAccessFromAllNetworks()
                        .create()
                        .id())
                .withApplicationInsights(
                    applicationInsightsManager.components()
                        .define(insightName)
                        .withRegion(REGION)
                        .withExistingResourceGroup(resourceGroupName)
                        .withKind("web")
                        .withApplicationType(ApplicationType.WEB)
                        .create()
                        .id())
                .create();
            // @embedmeEnd
            workspace.refresh();
            Assertions.assertEquals(workspace.name(), workspaceName);
            Assertions.assertEquals(workspace.name(), machineLearningManager.workspaces().getById(workspace.id()).name());
            Assertions.assertTrue(machineLearningManager.workspaces().list().stream().count() > 0);
        } finally {
            if (workspace != null) {
                machineLearningManager.workspaces().deleteById(workspace.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
