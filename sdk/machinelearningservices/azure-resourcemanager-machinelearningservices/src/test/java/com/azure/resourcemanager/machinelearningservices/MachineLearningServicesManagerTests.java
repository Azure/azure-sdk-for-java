// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.machinelearningservices;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.applicationinsights.ApplicationInsightsManager;
import com.azure.resourcemanager.applicationinsights.models.ApplicationInsightsComponent;
import com.azure.resourcemanager.applicationinsights.models.ApplicationType;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.machinelearningservices.models.Identity;
import com.azure.resourcemanager.machinelearningservices.models.ResourceIdentityType;
import com.azure.resourcemanager.machinelearningservices.models.Sku;
import com.azure.resourcemanager.machinelearningservices.models.Workspace;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class MachineLearningServicesManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private MachineLearningServicesManager machineLearningServicesManager;
    private StorageManager storageManager;
    private KeyVaultManager keyVaultManager;
    private ApplicationInsightsManager applicationInsightsManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        machineLearningServicesManager = MachineLearningServicesManager
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
    @DoNotRecord(skipInPlayback = true)
    public void testCreateWorkSpace() {
        Workspace workspace = null;
        String randomPadding = randomPadding();
        try {
            String workspaceName = "workspace" + randomPadding;
            String storageName = "storage" + randomPadding;
            String keyVaultName = "vault" + randomPadding;
            String insightName = "insight" + randomPadding;
            // @embedmeStart
            StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(StorageAccountSkuType.STANDARD_LRS)
                .withMinimumTlsVersion(MinimumTlsVersion.TLS1_0)
                .withHnsEnabled(false)
                .withAccessFromAzureServices()
                .withOnlyHttpsTraffic()
                .withBlobStorageAccountKind().withAccessTier(AccessTier.HOT)
                .create();

            Vault vault = keyVaultManager.vaults()
                .define(keyVaultName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withEmptyAccessPolicy()
                .withSku(SkuName.STANDARD)
                .withDeploymentDisabled()
                .withAccessFromAllNetworks()
                .create();

            ApplicationInsightsComponent insightsComponent =
                applicationInsightsManager.components()
                    .define(insightName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(resourceGroupName)
                    .withKind("web")
                    .withApplicationType(ApplicationType.WEB)
                    .create();

            workspace = machineLearningServicesManager.workspaces()
                .define(workspaceName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new Sku().withName("Basic").withTier("Basic"))
                .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
                .withFriendlyName(workspaceName)
                .withStorageAccount(storageAccount.id())
                .withKeyVault(vault.id())
                .withApplicationInsights(insightsComponent.id())
                .create();
            // @embedmeEnd
            workspace.refresh();
            Assertions.assertEquals(workspace.name(), workspaceName);
            Assertions.assertEquals(workspace.name(), machineLearningServicesManager.workspaces().getById(workspace.id()).name());
            Assertions.assertTrue(machineLearningServicesManager.workspaces().list().stream().count() > 0);
        } finally {
            if (workspace != null) {
                machineLearningServicesManager.workspaces().deleteById(workspace.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
