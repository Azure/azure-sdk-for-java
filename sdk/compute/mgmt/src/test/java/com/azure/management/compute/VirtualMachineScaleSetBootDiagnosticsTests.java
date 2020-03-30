/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.management.RestClient;
import com.azure.management.compute.models.VirtualMachineScaleSetInner;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerSkuType;
import com.azure.management.network.Network;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineScaleSetBootDiagnosticsTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private final Region REGION = Region.US_SOUTH_CENTRAL;
    private final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String storageName = sdkContext.randomResourceName("st", 14);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withBootDiagnostics(creatableStorageAccount)
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMSSCreation() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String storageName = sdkContext.randomResourceName("st", 14);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withBootDiagnostics(storageAccount)
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canDisableVMSSBootDiagnostics() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());

        virtualMachineScaleSet.update()
                .withoutBootDiagnostics()
                .apply();

        Assertions.assertFalse(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        // Disabling boot diagnostics will not remove the storage uri from the vm payload.
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
    }

    @Test
    public void bootDiagnosticsShouldUsesVMSSOSUnManagedDiskImplicitStorage() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withUnmanagedDisks()
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());

        VirtualMachineScaleSetInner inner = virtualMachineScaleSet.inner();
        Assertions.assertNotNull(inner);
        Assertions.assertNotNull(inner.virtualMachineProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile().osDisk());
        List<String> containers = inner.virtualMachineProfile().storageProfile().osDisk().vhdContainers();
        Assertions.assertFalse(containers.isEmpty());
        // Boot diagnostics should share storage used for os/disk containers
        boolean found = false;
        for (String containerStorageUri : containers) {
            if (containerStorageUri.toLowerCase().startsWith(virtualMachineScaleSet.bootDiagnosticsStorageUri().toLowerCase())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
    }

    @Test
    public void bootDiagnosticsShouldUseVMSSUnManagedDisksExplicitStorage() throws Exception {
        final String storageName = sdkContext.randomResourceName("st", 14);
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .create();

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withUnmanagedDisks()
                .withBootDiagnostics()
                .withExistingStorageAccount(storageAccount) // This storage account must be shared by disk and boot diagnostics
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));

        VirtualMachineScaleSetInner inner = virtualMachineScaleSet.inner();
        Assertions.assertNotNull(inner);
        Assertions.assertNotNull(inner.virtualMachineProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile().osDisk());
        List<String> containers = inner.virtualMachineProfile().storageProfile().osDisk().vhdContainers();
        Assertions.assertFalse(containers.isEmpty());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnUnManagedVMSSCreation() throws Exception {
        final String storageName = sdkContext.randomResourceName("st", 14);
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(REGION,
                resourceGroup,
                "1",
                LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);


        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withUnmanagedDisks()
                .withBootDiagnostics(creatableStorageAccount) // This storage account should be used for BDiagnostics not OS disk storage account
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet);
        Assertions.assertTrue(virtualMachineScaleSet.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachineScaleSet.bootDiagnosticsStorageUri().contains(storageName));
        // There should be a different storage account created for VMSS OS Disk

        VirtualMachineScaleSetInner inner = virtualMachineScaleSet.inner();
        Assertions.assertNotNull(inner);
        Assertions.assertNotNull(inner.virtualMachineProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile());
        Assertions.assertNotNull(inner.virtualMachineProfile().storageProfile().osDisk());
        List<String> containers = inner.virtualMachineProfile().storageProfile().osDisk().vhdContainers();
        Assertions.assertFalse(containers.isEmpty());
        boolean notFound = true;
        for (String containerStorageUri : containers) {
            if (containerStorageUri.toLowerCase().startsWith(virtualMachineScaleSet.bootDiagnosticsStorageUri().toLowerCase())) {
                notFound = false;
                break;
            }
        }
        Assertions.assertTrue(notFound);
    }
}
