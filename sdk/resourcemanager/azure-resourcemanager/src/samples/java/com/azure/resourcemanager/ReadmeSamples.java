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
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private final String rgName = "rg-test";

    // extra empty lines to compensate import lines





    // THIS LINE MUST BE AT LINE NO. 60
    public void authenticate() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        AzureResourceManager azure = AzureResourceManager
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    public void configureWithLogging() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        AzureResourceManager azure = AzureResourceManager
            .configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    public void authenticateComputeManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        ComputeManager manager = ComputeManager.authenticate(credential, profile);
        manager.virtualMachines().list();
    }

    public void createAndUpdateVirtualMachine() {
        AzureResourceManager azure = newAzureResourceManager();

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

        linuxVM.update()
            .withNewDataDisk(10, 0, CachingTypes.READ_WRITE)
            .apply();
    }

    public void createFunctionApp() {
        AzureResourceManager azure = newAzureResourceManager();

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
    }

    public void batchCreateAndDeleteDisk() {
        AzureResourceManager azure = newAzureResourceManager();

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
    }

    public void createRoleAssignment() {
        AzureResourceManager azure = newAzureResourceManager();
        ResourceGroup resource = azure.resourceGroups().getByName(rgName);
        ServicePrincipal servicePrincipal = azure.accessManagement().servicePrincipals().getById("<service-principal-id>");

        String raName = UUID.randomUUID().toString();
        RoleAssignment roleAssignment = azure.accessManagement().roleAssignments()
            .define(raName)
            .forServicePrincipal(servicePrincipal)
            .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
            .withScope(resource.id())
            .create();
    }

    public void createStorageAccountAndBlobContainerAsync() {
        AzureResourceManager azure = newAzureResourceManager();

        azure.storageAccounts().define("<storage-account-name>")
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku(StorageAccountSkuType.STANDARD_LRS)
            .withGeneralPurposeAccountKindV2()
            .withOnlyHttpsTraffic()
            .createAsync()
            .flatMap(storageAccount -> azure.storageBlobContainers()
                .defineContainer("container")
                .withExistingBlobService(rgName, storageAccount.name())
                .withPublicAccess(PublicAccess.NONE)
                .createAsync()
            )
            //...
            .block();
    }

    public void restartVirtualMachineAsync() {
        AzureResourceManager azure = newAzureResourceManager();

        azure.virtualMachines().listByResourceGroupAsync(rgName)
            .flatMap(VirtualMachine::restartAsync)
            //...
            .blockLast();
    }

    public void configureClientAndPipeline() {
        HttpClient customizedHttpClient = HttpClient.createDefault();
        HttpPipelinePolicy additionalPolicy = new TimeoutPolicy(Duration.ofMinutes(2));

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        AzureResourceManager azure = AzureResourceManager
            .configure()
            .withHttpClient(customizedHttpClient)
            .withPolicy(additionalPolicy)
            //...
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
