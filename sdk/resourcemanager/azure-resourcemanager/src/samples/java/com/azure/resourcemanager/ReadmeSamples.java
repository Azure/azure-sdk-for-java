// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private final String rgName = "rg-test";

    public void authenticate() {
        // BEGIN: readme-sample-authenticate
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        AzureResourceManager azure = AzureResourceManager
            .authenticate(credential, profile)
            .withDefaultSubscription();
        // END: readme-sample-authenticate
    }

    public void configureWithLogging() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        // BEGIN: readme-sample-configureWithLogging
        AzureResourceManager azure = AzureResourceManager
            .configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();
        // END: readme-sample-configureWithLogging
    }

    public void authenticateComputeManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        // BEGIN: readme-sample-authenticateComputeManager
        ComputeManager manager = ComputeManager.authenticate(credential, profile);
        manager.virtualMachines().list();
        // END: readme-sample-authenticateComputeManager
    }

    public void createAndUpdateVirtualMachine() {
        AzureResourceManager azure = newAzureResourceManager();

        // BEGIN: readme-sample-createVirtualMachine
        VirtualMachine linuxVM = azure.virtualMachines().define("myLinuxVM")
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("<username>")
            .withSsh("<ssh-key>")
            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
            .create();
        // END: readme-sample-createVirtualMachine

        // BEGIN: readme-sample-updateVirtualMachine
        linuxVM.update()
            .withNewDataDisk(10, 0, CachingTypes.READ_WRITE)
            .apply();
        // END: readme-sample-updateVirtualMachine
    }

    public void createFunctionApp() {
        AzureResourceManager azure = newAzureResourceManager();

        // BEGIN: readme-sample-createFunctionApp
        Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts()
            .define("<storage-account-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withGeneralPurposeAccountKindV2()
            .withSku(StorageAccountSkuType.STANDARD_LRS);
        Creatable<AppServicePlan> creatableAppServicePlan = azure.appServicePlans()
            .define("<app-service-plan-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withPricingTier(PricingTier.STANDARD_S1)
            .withOperatingSystem(OperatingSystem.LINUX);
        FunctionApp linuxFunctionApp = azure.functionApps().define("<function-app-name>")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withNewLinuxAppServicePlan(creatableAppServicePlan)
            .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
            .withNewStorageAccount(creatableStorageAccount)
            .withHttpsOnly(true)
            .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", "<function-app-package-url>")
            .create();
        // END: readme-sample-createFunctionApp
    }

    public void batchCreateAndDeleteDisk() {
        AzureResourceManager azure = newAzureResourceManager();

        // BEGIN: readme-sample-batchCreateAndDeleteDisk
        List<String> diskNames = Arrays.asList("datadisk1", "datadisk2");
        List<Creatable<Disk>> creatableDisks = diskNames.stream()
            .map(diskName -> azure.disks()
                .define(diskName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(10)
                .withSku(DiskSkuTypes.STANDARD_LRS))
            .collect(Collectors.toList());
        Collection<Disk> disks = azure.disks().create(creatableDisks).values();
        azure.disks().deleteByIds(disks.stream().map(Disk::id).collect(Collectors.toList()));
        // END: readme-sample-batchCreateAndDeleteDisk
    }

    public void createRoleAssignment() {
        AzureResourceManager azure = newAzureResourceManager();
        ResourceGroup resource = azure.resourceGroups().getByName(rgName);
        ServicePrincipal servicePrincipal = azure.accessManagement().servicePrincipals().getById("<service-principal-id>");

        // BEGIN: readme-sample-createRoleAssignment
        String raName = UUID.randomUUID().toString();
        RoleAssignment roleAssignment = azure.accessManagement().roleAssignments()
            .define(raName)
            .forServicePrincipal(servicePrincipal)
            .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
            .withScope(resource.id())
            .create();
        // END: readme-sample-createRoleAssignment
    }

    public void createStorageAccountAndBlobContainerAsync() {
        AzureResourceManager azure = newAzureResourceManager();

        // BEGIN: readme-sample-createStorageAccountAndBlobContainerAsync
        azure.storageAccounts().define("<storage-account-name>")
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku(StorageAccountSkuType.STANDARD_LRS)
            .withGeneralPurposeAccountKindV2()
            .withOnlyHttpsTraffic()
            .createAsync()
            .flatMap(storageAccount -> azure.storageBlobContainers()
                .defineContainer("container")
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .createAsync()
            )
            //...
        // END: readme-sample-createStorageAccountAndBlobContainerAsync
            .block();
    }

    public void restartVirtualMachineAsync() {
        AzureResourceManager azure = newAzureResourceManager();

        // BEGIN: readme-sample-restartVirtualMachineAsync
        azure.virtualMachines().listByResourceGroupAsync(rgName)
            .flatMap(VirtualMachine::restartAsync)
            //...
        // END: readme-sample-restartVirtualMachineAsync
            .blockLast();
    }

    public void configureClientAndPipeline() {
        HttpClient customizedHttpClient = HttpClient.createDefault();
        HttpPipelinePolicy additionalPolicy = new TimeoutPolicy(Duration.ofMinutes(2));

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        // BEGIN: readme-sample-configureClientAndPipeline
        AzureResourceManager azure = AzureResourceManager
            .configure()
            .withHttpClient(customizedHttpClient)
            .withPolicy(additionalPolicy)
            //...
        // END: readme-sample-configureClientAndPipeline
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    private AzureResourceManager newAzureResourceManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        return AzureResourceManager
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }
}
