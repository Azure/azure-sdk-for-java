// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;

public class AzureResourceManagerJavaDocSamples {
    public void authenticateUsingTokenCredentialAndAzureProfile() {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        AzureResourceManager azure = AzureResourceManager
            .authenticate(credential, profile)
            .withDefaultSubscription();
        // END: com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile
    }

    public void configure(HttpPipelinePolicy customPolicy,
                          RetryPolicy customRetryPolicy,
                          HttpClient httpClient,
                          TokenCredential credential,
                          AzureProfile profile) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.configure
        AzureResourceManager azure = AzureResourceManager
            .configure()
            .withLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .withPolicy(customPolicy)
            .withRetryPolicy(customRetryPolicy)
            .withHttpClient(httpClient)
            //...
            // END: com.azure.resourcemanager.azureResourceManager.configure
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    public void createVirtualMachine(AzureResourceManager azure,
                                     Region region,
                                     String resourceGroupName,
                                     Disk dataDisk,
                                     String windowsVMName,
                                     String userName,
                                     String password) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.virtualMachines.createVirtualMachine
        VirtualMachine windowsVM = azure.virtualMachines()
            .define(windowsVMName)
            .withRegion(region)
            .withNewResourceGroup(resourceGroupName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
            .withAdminUsername(userName)
            .withAdminPassword(password)
            .withNewDataDisk(10)
            .withExistingDataDisk(dataDisk)
            .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
            .create();
        // END: com.azure.resourcemanager.azureResourceManager.virtualMachines.createVirtualMachine
    }

    public void createStorageAccountAsync(AzureResourceManager azure,
                                          String resourceGroupName) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.storageAccounts.createStorageAccountAsync
        azure.storageAccounts().define("<storage-account-name>")
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(resourceGroupName)
            .withSku(StorageAccountSkuType.STANDARD_LRS)
            .withGeneralPurposeAccountKindV2()
            .withOnlyHttpsTraffic()
            .createAsync()
            //...
            // END: com.azure.resourcemanager.azureResourceManager.storageAccounts.createStorageAccountAsync
            .block();
    }

    public void createBlobContainerAsync(AzureResourceManager azure,
                                         StorageAccount storageAccount) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.storageBlobContainers.createBlobContainerAsync
        azure.storageBlobContainers()
            .defineContainer("container")
            .withExistingStorageAccount(storageAccount)
            .withPublicAccess(PublicAccess.NONE)
            .createAsync()
            //...
            // END: com.azure.resourcemanager.azureResourceManager.storageBlobContainers.createBlobContainerAsync
            .block();
    }

    public void restartVirtualMachineAsync(AzureResourceManager azure,
                                           String resourceGroupName) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.virtualMachines.restartVirtualMachineAsync
        azure.virtualMachines().listByResourceGroupAsync(resourceGroupName)
            .flatMap(VirtualMachine::restartAsync)
            //...
            // END: com.azure.resourcemanager.azureResourceManager.virtualMachines.restartVirtualMachineAsync
            .blockLast();
    }

    public void createFunctionApp(AzureResourceManager azure,
                                  String resourceGroupName) {
        // BEGIN: com.azure.resourcemanager.azureResourceManager.functionApps.createFunctionApp
        Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts()
            .define("<storage-account-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(resourceGroupName)
            .withGeneralPurposeAccountKindV2()
            .withSku(StorageAccountSkuType.STANDARD_LRS);
        Creatable<AppServicePlan> creatableAppServicePlan = azure.appServicePlans()
            .define("<app-service-plan-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(resourceGroupName)
            .withPricingTier(PricingTier.STANDARD_S1)
            .withOperatingSystem(OperatingSystem.LINUX);
        FunctionApp linuxFunctionApp = azure.functionApps().define("<function-app-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(resourceGroupName)
            .withNewLinuxAppServicePlan(creatableAppServicePlan)
            .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
            .withNewStorageAccount(creatableStorageAccount)
            .withHttpsOnly(true)
            .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", "<function-app-package-url>")
            .create();
        // END: com.azure.resourcemanager.azureResourceManager.functionApps.createFunctionApp
    }
}
