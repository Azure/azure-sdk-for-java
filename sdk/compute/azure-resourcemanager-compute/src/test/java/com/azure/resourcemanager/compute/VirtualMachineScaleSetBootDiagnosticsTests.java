// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineScaleSetBootDiagnosticsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_WEST3;
    private final String vmName = "javavm";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network = this.networkManager.networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        LoadBalancer publicLoadBalancer
            = createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .disableSharedKeyAccess()
            .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withBootDiagnostics()
            .withExistingStorageAccount(storageAccount)
            .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String storageName = generateRandomResourceName("st", 14);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network = this.networkManager.networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        LoadBalancer publicLoadBalancer
            = createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
            .define(storageName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .disableSharedKeyAccess();

        StorageAccount storageAccount = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .disableSharedKeyAccess()
            .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withBootDiagnostics(creatableStorageAccount)
            .withExistingStorageAccount(storageAccount)
            .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String storageName = generateRandomResourceName("st", 14);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network = this.networkManager.networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        LoadBalancer publicLoadBalancer
            = createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(storageName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .disableSharedKeyAccess()
            .create();

        Creatable<StorageAccount> storageAccountCreatable = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .disableSharedKeyAccess();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withBootDiagnostics(storageAccount)
            .withNewStorageAccount(storageAccountCreatable)
            .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canDisableVMSSBootDiagnostics() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network = this.networkManager.networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        LoadBalancer publicLoadBalancer
            = createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .disableSharedKeyAccess()
            .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withBootDiagnostics()
            .withExistingStorageAccount(storageAccount)
            .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());

        virtualMachineScaleSet.update().withoutBootDiagnostics().apply();

        Assertions.assertFalse(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        // Disabling boot diagnostics will not remove the storage uri from the vm payload.
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsOnManagedStorageAccount() {
        Network network = this.networkManager.networks()
            .define("vmssvnet")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
            .define(vmName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withBootDiagnosticsOnManagedStorageAccount()
            .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
    }
}
