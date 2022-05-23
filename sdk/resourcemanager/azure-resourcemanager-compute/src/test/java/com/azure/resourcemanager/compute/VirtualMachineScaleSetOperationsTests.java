// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.SubResource;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetInner;
import com.azure.resourcemanager.compute.models.DeleteOptions;
import com.azure.resourcemanager.compute.models.DiffDiskPlacement;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.OrchestrationMode;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.PurchasePlan;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.Sku;
import com.azure.resourcemanager.compute.models.UpgradeMode;
import com.azure.resourcemanager.compute.models.VaultCertificate;
import com.azure.resourcemanager.compute.models.VaultSecretGroup;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetPublicIpAddressConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMExpandType;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNicIpConfiguration;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_WEST;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canCreateVMSSWithPlan() {
        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();
        final String uname = "jvuser";

        Network network =
            this
                .networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        PurchasePlan plan = new PurchasePlan()
            .withName("access_server_byol")
            .withPublisher("openvpn")
            .withProduct("openvpnas");

        ImageReference imageReference = new ImageReference()
            .withPublisher("openvpn")
            .withOffer("openvpnas")
            .withSku("access_server_byol")
            .withVersion("latest");

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withSpecificLinuxImageVersion(imageReference)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withNewDataDisk(1)
                .withPlan(plan)
                .create();

        VirtualMachineScaleSet currentVirtualMachineScaleSet = this.computeManager.virtualMachineScaleSets().getByResourceGroup(rgName, vmssName);
        // assertion for purchase plan
        Assertions.assertEquals("access_server_byol", currentVirtualMachineScaleSet.plan().name());
        Assertions.assertEquals("openvpn", currentVirtualMachineScaleSet.plan().publisher());
        Assertions.assertEquals("openvpnas", currentVirtualMachineScaleSet.plan().product());

    }

    @Test
    public void canUpdateVirtualMachineScaleSetWithExtensionProtectedSettings() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String uname = "jvuser";
        final String password = password();

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        StorageAccount storageAccount =
            this
                .storageManager
                .storageAccounts()
                .define(generateRandomResourceName("stg", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        List<StorageAccountKey> keys = storageAccount.getKeys();
        Assertions.assertNotNull(keys);
        Assertions.assertTrue(keys.size() > 0);
        String storageAccountKey = keys.get(0).value();

        Network network =
            this
                .networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withExistingStorageAccount(storageAccount)
                .defineNewExtension("CustomScriptForLinux")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("CustomScriptForLinux")
                .withVersion("1.4")
                .withMinorVersionAutoUpgrade()
                .withPublicSetting("commandToExecute", "ls")
                .withProtectedSetting("storageAccountName", storageAccount.name())
                .withProtectedSetting("storageAccountKey", storageAccountKey)
                .attach()
                .create();
        // Validate extensions after create
        //
        Map<String, VirtualMachineScaleSetExtension> extensions = virtualMachineScaleSet.extensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertTrue(extensions.size() > 0);
        Assertions.assertTrue(extensions.containsKey("CustomScriptForLinux"));
        VirtualMachineScaleSetExtension extension = extensions.get("CustomScriptForLinux");
        Assertions.assertNotNull(extension.publicSettings());
        Assertions.assertEquals(1, extension.publicSettings().size());
        Assertions.assertNotNull(extension.publicSettingsAsJsonString());
        // Retrieve scale set
        VirtualMachineScaleSet scaleSet =
            this.computeManager.virtualMachineScaleSets().getById(virtualMachineScaleSet.id());
        // Validate extensions after get
        //
        extensions = scaleSet.extensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertTrue(extensions.size() > 0);
        Assertions.assertTrue(extensions.containsKey("CustomScriptForLinux"));
        extension = extensions.get("CustomScriptForLinux");
        Assertions.assertNotNull(extension.publicSettings());
        Assertions.assertEquals(1, extension.publicSettings().size());
        Assertions.assertNotNull(extension.publicSettingsAsJsonString());
        // Update VMSS capacity
        //
        int newCapacity = scaleSet.capacity() + 1;
        virtualMachineScaleSet.update().withCapacity(newCapacity).apply();
        // Validate extensions after update
        //
        extensions = virtualMachineScaleSet.extensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertTrue(extensions.size() > 0);
        Assertions.assertTrue(extensions.containsKey("CustomScriptForLinux"));
        extension = extensions.get("CustomScriptForLinux");
        Assertions.assertNotNull(extension.publicSettings());
        Assertions.assertEquals(1, extension.publicSettings().size());
        Assertions.assertNotNull(extension.publicSettingsAsJsonString());
    }

    @Test
    public void canCreateVirtualMachineScaleSetWithCustomScriptExtension() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String uname = "jvuser";
        final String password = password();

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1");
        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .defineNewExtension("CustomScriptForLinux")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("CustomScriptForLinux")
                .withVersion("1.4")
                .withMinorVersionAutoUpgrade()
                .withPublicSetting("commandToExecute", "ls")
                .attach()
                .withUpgradeMode(UpgradeMode.MANUAL)
                .create();

        checkVMInstances(virtualMachineScaleSet);

        List<String> publicIPAddressIds = virtualMachineScaleSet.primaryPublicIpAddressIds();
        PublicIpAddress publicIPAddress = this.networkManager.publicIpAddresses().getById(publicIPAddressIds.get(0));

        String fqdn = publicIPAddress.fqdn();
        Assertions.assertNotNull(fqdn);
//        // Assert public load balancing connection
//        if (!isPlaybackMode()) {
//            HttpClient client = HttpClient.createDefault();
//            HttpRequest request = new HttpRequest(HttpMethod.GET, "http://" + fqdn);
//            HttpResponse response = client.send(request).block();
//            Assertions.assertEquals(response.getStatusCode(), 200);
//        }

        // Check SSH to VM instances via Nat rule
        //
        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            PagedIterable<VirtualMachineScaleSetNetworkInterface> networkInterfaces = vm.listNetworkInterfaces();
            Assertions.assertEquals(TestUtilities.getSize(networkInterfaces), 1);
            VirtualMachineScaleSetNetworkInterface networkInterface = networkInterfaces.iterator().next();
            VirtualMachineScaleSetNicIpConfiguration primaryIpConfig = null;
            primaryIpConfig = networkInterface.primaryIPConfiguration();
            Assertions.assertNotNull(primaryIpConfig);
            Integer sshFrontendPort = null;
            List<LoadBalancerInboundNatRule> natRules = primaryIpConfig.listAssociatedLoadBalancerInboundNatRules();
            for (LoadBalancerInboundNatRule natRule : natRules) {
                if (natRule.backendPort() == 22) {
                    sshFrontendPort = natRule.frontendPort();
                    break;
                }
            }
            Assertions.assertNotNull(sshFrontendPort);

//            this.sleep(1000 * 60); // Wait some time for VM to be available
//            this.ensureCanDoSsh(fqdn, sshFrontendPort, uname, password);
        }
    }

    @Test
    public void canCreateVirtualMachineScaleSetWithOptionalNetworkSettings() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String vmssVmDnsLabel = generateRandomResourceName("pip", 10);
        final String nsgName = generateRandomResourceName("nsg", 10);
        final String asgName = generateRandomResourceName("asg", 8);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        ApplicationSecurityGroup asg =
            this
                .networkManager
                .applicationSecurityGroups()
                .define(asgName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        // Create VMSS with instance public ip
        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withVirtualMachinePublicIp(vmssVmDnsLabel)
                .withExistingApplicationSecurityGroup(asg)
                .create();

        VirtualMachineScaleSetPublicIpAddressConfiguration currentIpConfig =
            virtualMachineScaleSet.virtualMachinePublicIpConfig();

        Assertions.assertNotNull(currentIpConfig);
        Assertions.assertNotNull(currentIpConfig.dnsSettings());
        Assertions.assertNotNull(currentIpConfig.dnsSettings().domainNameLabel());

        currentIpConfig.withIdleTimeoutInMinutes(20);

        virtualMachineScaleSet.update().withVirtualMachinePublicIp(currentIpConfig).apply();

        currentIpConfig = virtualMachineScaleSet.virtualMachinePublicIpConfig();
        Assertions.assertNotNull(currentIpConfig);
        Assertions.assertNotNull(currentIpConfig.idleTimeoutInMinutes());
        Assertions.assertEquals((long) 20, (long) currentIpConfig.idleTimeoutInMinutes());

        virtualMachineScaleSet.refresh();
        currentIpConfig = virtualMachineScaleSet.virtualMachinePublicIpConfig();
        Assertions.assertNotNull(currentIpConfig);
        Assertions.assertNotNull(currentIpConfig.idleTimeoutInMinutes());
        Assertions.assertEquals((long) 20, (long) currentIpConfig.idleTimeoutInMinutes());

        List<String> asgIds = virtualMachineScaleSet.applicationSecurityGroupIds();
        Assertions.assertNotNull(asgIds);
        Assertions.assertEquals(1, asgIds.size());

        NetworkSecurityGroup nsg =
            networkManager
                .networkSecurityGroups()
                .define(nsgName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .defineRule("rule1")
                .allowOutbound()
                .fromAnyAddress()
                .fromPort(80)
                .toAnyAddress()
                .toPort(80)
                .withProtocol(SecurityRuleProtocol.TCP)
                .attach()
                .create();
        virtualMachineScaleSet.deallocate();
        virtualMachineScaleSet
            .update()
            .withIpForwarding()
            .withAcceleratedNetworking()
            .withExistingNetworkSecurityGroup(nsg)
            .apply();

        Assertions.assertTrue(virtualMachineScaleSet.isIpForwardingEnabled());
        Assertions.assertTrue(virtualMachineScaleSet.isAcceleratedNetworkingEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.networkSecurityGroupId());
        //
        virtualMachineScaleSet.refresh();
        //
        Assertions.assertTrue(virtualMachineScaleSet.isIpForwardingEnabled());
        Assertions.assertTrue(virtualMachineScaleSet.isAcceleratedNetworkingEnabled());
        Assertions.assertNotNull(virtualMachineScaleSet.networkSecurityGroupId());

        virtualMachineScaleSet
            .update()
            .withoutIpForwarding()
            .withoutAcceleratedNetworking()
            .withoutNetworkSecurityGroup()
            .apply();
        Assertions.assertFalse(virtualMachineScaleSet.isIpForwardingEnabled());
        Assertions.assertFalse(virtualMachineScaleSet.isAcceleratedNetworkingEnabled());
        Assertions.assertNull(virtualMachineScaleSet.networkSecurityGroupId());
    }

    @Test
    @Disabled("Mock framework doesn't support data plane")
    public void canCreateVirtualMachineScaleSetWithSecret() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String vaultName = generateRandomResourceName("vlt", 10);
        final String secretName = generateRandomResourceName("srt", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        Vault vault =
            this
                .keyVaultManager
                .vaults()
                .define(vaultName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .defineAccessPolicy()
                .forServicePrincipal(clientIdFromFile())
                .allowSecretAllPermissions()
                .attach()
                .withDeploymentEnabled()
                .create();
        final InputStream embeddedJsonConfig =
            VirtualMachineExtensionOperationsTests.class.getResourceAsStream("/myTest.txt");
        String secretValue = IOUtils.toString(embeddedJsonConfig, StandardCharsets.UTF_8);
        Secret secret = vault.secrets().define(secretName).withValue(secretValue).create();
        List<VaultCertificate> certs = new ArrayList<>();
        certs.add(new VaultCertificate().withCertificateUrl(secret.id()));
        List<VaultSecretGroup> group = new ArrayList<>();
        group
            .add(
                new VaultSecretGroup()
                    .withSourceVault(new SubResource().withId(vault.id()))
                    .withVaultCertificates(certs));

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSecrets(group)
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg3", 15))
                .withUpgradeMode(UpgradeMode.MANUAL)
                .create();

        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            Assertions.assertTrue(vm.osProfile().secrets().size() > 0);
        }

        virtualMachineScaleSet.update().withoutSecrets().apply();

        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            Assertions.assertTrue(vm.osProfile().secrets().size() == 0);
        }
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .create();

        // Validate Network specific properties (LB, VNet, NIC, IPConfig etc..)
        //
        Assertions.assertNull(virtualMachineScaleSet.getPrimaryInternalLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerBackends().size() == 0);
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assertions.assertNotNull(virtualMachineScaleSet.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerBackends().size() == 2);
        Assertions
            .assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().size() == 2);

        Network primaryNetwork = virtualMachineScaleSet.getPrimaryNetwork();
        Assertions.assertNotNull(primaryNetwork.id());

        PagedIterable<VirtualMachineScaleSetNetworkInterface> nics = virtualMachineScaleSet.listNetworkInterfaces();
        int nicCount = 0;
        for (VirtualMachineScaleSetNetworkInterface nic : nics) {
            nicCount++;
            Assertions.assertNotNull(nic.id());
            Assertions
                .assertTrue(nic.virtualMachineId().toLowerCase().startsWith(virtualMachineScaleSet.id().toLowerCase()));
            Assertions.assertNotNull(nic.macAddress());
            Assertions.assertNotNull(nic.dnsServers());
            Assertions.assertNotNull(nic.appliedDnsServers());
            Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigs = nic.ipConfigurations();
            Assertions.assertEquals(ipConfigs.size(), 1);
            for (Map.Entry<String, VirtualMachineScaleSetNicIpConfiguration> entry : ipConfigs.entrySet()) {
                VirtualMachineScaleSetNicIpConfiguration ipConfig = entry.getValue();
                Assertions.assertNotNull(ipConfig);
                Assertions.assertTrue(ipConfig.isPrimary());
                Assertions.assertNotNull(ipConfig.subnetName());
                Assertions.assertTrue(primaryNetwork.id().toLowerCase().equalsIgnoreCase(ipConfig.networkId()));
                Assertions.assertNotNull(ipConfig.privateIpAddress());
                Assertions.assertNotNull(ipConfig.privateIpAddressVersion());
                Assertions.assertNotNull(ipConfig.privateIpAllocationMethod());
                List<LoadBalancerBackend> lbBackends = ipConfig.listAssociatedLoadBalancerBackends();
                // VMSS is created with a internet facing LB with two Backend pools so there will be two
                // backends in ip-config as well
                Assertions.assertEquals(lbBackends.size(), 2);
                for (LoadBalancerBackend lbBackend : lbBackends) {
                    Map<String, LoadBalancingRule> lbRules = lbBackend.loadBalancingRules();
                    Assertions.assertEquals(lbRules.size(), 1);
                    for (Map.Entry<String, LoadBalancingRule> ruleEntry : lbRules.entrySet()) {
                        LoadBalancingRule rule = ruleEntry.getValue();
                        Assertions.assertNotNull(rule);
                        Assertions
                            .assertTrue(
                                (rule.frontendPort() == 80 && rule.backendPort() == 80)
                                    || (rule.frontendPort() == 443 && rule.backendPort() == 443));
                    }
                }
                List<LoadBalancerInboundNatRule> lbNatRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
                // VMSS is created with a internet facing LB with two nat pools so there will be two
                //  nat rules in ip-config as well
                Assertions.assertEquals(lbNatRules.size(), 2);
                for (LoadBalancerInboundNatRule lbNatRule : lbNatRules) {
                    Assertions
                        .assertTrue(
                            (lbNatRule.frontendPort() >= 5000 && lbNatRule.frontendPort() <= 5099)
                                || (lbNatRule.frontendPort() >= 6000 && lbNatRule.frontendPort() <= 6099));
                    Assertions.assertTrue(lbNatRule.backendPort() == 22 || lbNatRule.backendPort() == 23);
                }
            }
        }
        Assertions.assertTrue(nicCount > 0);

        // Validate other properties
        //
        Assertions.assertEquals(virtualMachineScaleSet.vhdContainers().size(), 2);
        Assertions.assertEquals(virtualMachineScaleSet.sku(), VirtualMachineScaleSetSkuTypes.STANDARD_A0);
        // Check defaults
        Assertions.assertTrue(virtualMachineScaleSet.upgradeModel() == UpgradeMode.AUTOMATIC);
        Assertions.assertEquals(virtualMachineScaleSet.capacity(), 2);
        // Fetch the primary Virtual network
        primaryNetwork = virtualMachineScaleSet.getPrimaryNetwork();

        String inboundNatPoolToRemove = null;
        for (String inboundNatPoolName
            : virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().keySet()) {
            inboundNatPoolToRemove = inboundNatPoolName;
            break;
        }

        LoadBalancer internalLoadBalancer = createInternalLoadBalancer(region, resourceGroup, primaryNetwork, "1");

        virtualMachineScaleSet
            .update()
            .withExistingPrimaryInternalLoadBalancer(internalLoadBalancer)
            .withoutPrimaryInternetFacingLoadBalancerNatPools(inboundNatPoolToRemove) // Remove one NatPool
            .apply();

        virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets().getByResourceGroup(rgName, vmssName);

        // Check LB after update
        //
        Assertions.assertNotNull(virtualMachineScaleSet.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerBackends().size() == 2);
        Assertions
            .assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().size() == 1);

        Assertions.assertNotNull(virtualMachineScaleSet.getPrimaryInternalLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerBackends().size() == 2);
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerInboundNatPools().size() == 2);

        // Check NIC + IpConfig after update
        //
        nics = virtualMachineScaleSet.listNetworkInterfaces();
        nicCount = 0;
        for (VirtualMachineScaleSetNetworkInterface nic : nics) {
            nicCount++;
            Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigs = nic.ipConfigurations();
            Assertions.assertEquals(ipConfigs.size(), 1);
            for (Map.Entry<String, VirtualMachineScaleSetNicIpConfiguration> entry : ipConfigs.entrySet()) {
                VirtualMachineScaleSetNicIpConfiguration ipConfig = entry.getValue();
                Assertions.assertNotNull(ipConfig);
                List<LoadBalancerBackend> lbBackends = ipConfig.listAssociatedLoadBalancerBackends();
                Assertions.assertNotNull(lbBackends);
                // Updated VMSS has a internet facing LB with two backend pools and a internal LB with two
                // backend pools so there should be 4 backends in ip-config
                // #1: But this is not always happening, it seems update is really happening only
                // for subset of vms [TODO: Report this to network team]
                // Assertions.True(lbBackends.Count == 4);
                // Assertions.assertEquals(lbBackends.size(), 4);
                for (LoadBalancerBackend lbBackend : lbBackends) {
                    Map<String, LoadBalancingRule> lbRules = lbBackend.loadBalancingRules();
                    Assertions.assertEquals(lbRules.size(), 1);
                    for (Map.Entry<String, LoadBalancingRule> ruleEntry : lbRules.entrySet()) {
                        LoadBalancingRule rule = ruleEntry.getValue();
                        Assertions.assertNotNull(rule);
                        Assertions
                            .assertTrue(
                                (rule.frontendPort() == 80 && rule.backendPort() == 80)
                                    || (rule.frontendPort() == 443 && rule.backendPort() == 443)
                                    || (rule.frontendPort() == 1000 && rule.backendPort() == 1000)
                                    || (rule.frontendPort() == 1001 && rule.backendPort() == 1001));
                    }
                }
                List<LoadBalancerInboundNatRule> lbNatRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
                // Updated VMSS has a internet facing LB with one nat pool and a internal LB with two
                // nat pools so there should be 3 nat rule in ip-config
                // Same issue as above #1
                // But this is not always happening, it seems update is really happening only
                // for subset of vms [TODO: Report this to network team]
                // Assertions.assertEquals(lbNatRules.size(), 3);
                for (LoadBalancerInboundNatRule lbNatRule : lbNatRules) {
                    // As mentioned above some chnages are not propgating to all VM instances 6000+ should be there
                    Assertions
                        .assertTrue(
                            (lbNatRule.frontendPort() >= 6000 && lbNatRule.frontendPort() <= 6099)
                                || (lbNatRule.frontendPort() >= 5000 && lbNatRule.frontendPort() <= 5099)
                                || (lbNatRule.frontendPort() >= 8000 && lbNatRule.frontendPort() <= 8099)
                                || (lbNatRule.frontendPort() >= 9000 && lbNatRule.frontendPort() <= 9099));
                    // Same as above
                    Assertions
                        .assertTrue(
                            lbNatRule.backendPort() == 23
                                || lbNatRule.backendPort() == 22
                                || lbNatRule.backendPort() == 44
                                || lbNatRule.backendPort() == 45);
                }
            }
        }
        Assertions.assertTrue(nicCount > 0);
    }

    /*
     * Previously name
     * canCreateTwoRegionalVirtualMachineScaleSetsAndAssociateEachWithDifferentBackendPoolOfZoneResilientLoadBalancer
     * but this was too long for some OSes and would cause git checkout to fail.
     */
    @Test
    public void canCreateTwoRegionalVMScaleSetsWithDifferentPoolOfZoneResilientLoadBalancer() throws Exception {
        // Zone resilient resource -> resources deployed in all zones by the service and it will be served by all AZs
        // all the time.
        // ZoneResilientLoadBalancer -> STANDARD LB -> [Since service deploy them to all zones, user don't have to set
        // zone explicitly, even if he does its a constrain as user can set only one zone at this time]

        Region region2 = Region.US_EAST2;

        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().define(rgName).withRegion(region2).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region2)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        // Creates a STANDARD LB with one public frontend ip configuration with two backend pools
        // Each address pool of STANDARD LB can hold different VMSS resource.
        //
        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region2, resourceGroup, "1", LoadBalancerSkuType.STANDARD);

        // With default LB SKU BASIC, an attempt to associate two different VMSS to different
        // backend pool will cause below error (more accurately, while trying to put second VMSS)
        // {
        //        "startTime": "2017-09-06T14:27:22.1849435+00:00",
        //        "endTime": "2017-09-06T14:27:45.8885142+00:00",
        //        "status": "Failed",
        //        "error": {
        //            "code": "VmIsNotInSameAvailabilitySetAsLb",
        //            "message": "Virtual Machine
        // /subscriptions/<sub-id>/resourceGroups/<rg-name>/providers/Microsoft.Compute/virtualMachines/|providers|Microsoft.Compute|virtualMachineScaleSets|<vm-ss-name>|virtualMachines|<instance-id> is using different Availability Set than other Virtual Machines connected to the Load Balancer(s) <lb-name>."
        //         },
        //        "name": "97531d64-db37-4d21-a1cb-9c53aad7c342"
        // }

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        List<String> natpools = new ArrayList<>();
        for (String natPool : publicLoadBalancer.inboundNatPools().keySet()) {
            natpools.add(natPool);
        }
        Assertions.assertTrue(natpools.size() == 2);

        final String vmssName1 = generateRandomResourceName("vmss1", 10);
        // HTTP goes to this virtual machine scale set
        //
        VirtualMachineScaleSet virtualMachineScaleSet1 =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName1)
                .withRegion(region2)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0)) // This VMSS in the first backend pool
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(0))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .create();

        final String vmssName2 = generateRandomResourceName("vmss2", 10);
        // HTTPS goes to this virtual machine scale set
        //
        VirtualMachineScaleSet virtualMachineScaleSet2 =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName2)
                .withRegion(region2)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(1)) // This VMSS in the second backend pool
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .create();

        // Validate Network specific properties (LB, VNet, NIC, IPConfig etc..)
        //
        Assertions.assertNull(virtualMachineScaleSet1.getPrimaryInternalLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet1.listPrimaryInternalLoadBalancerBackends().size() == 0);
        Assertions.assertTrue(virtualMachineScaleSet1.listPrimaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assertions.assertNotNull(virtualMachineScaleSet1.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet1.listPrimaryInternetFacingLoadBalancerBackends().size() == 1);

        Assertions.assertNull(virtualMachineScaleSet2.getPrimaryInternalLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet2.listPrimaryInternalLoadBalancerBackends().size() == 0);
        Assertions.assertTrue(virtualMachineScaleSet2.listPrimaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assertions.assertNotNull(virtualMachineScaleSet2.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet2.listPrimaryInternetFacingLoadBalancerBackends().size() == 1);
    }

    @Test
    public void canCreateZoneRedundantVirtualMachineScaleSetWithZoneResilientLoadBalancer() throws Exception {
        // Zone redundant VMSS is the one with multiple zones
        //

        Region region2 = Region.US_EAST2;

        ResourceGroup resourceGroup =
            this.resourceManager.resourceGroups().define(rgName).withRegion(region2).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region2)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        // Zone redundant VMSS requires STANDARD LB
        //
        // Creates a STANDARD LB with one public frontend ip configuration with two backend pools
        // Each address pool of STANDARD LB can hold different VMSS resource.
        //
        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region2, resourceGroup, "1", LoadBalancerSkuType.STANDARD);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        final String vmssName = generateRandomResourceName("vmss", 10);
        // HTTP & HTTPS traffic on port 80, 443 of Internet-facing LB goes to corresponding port in virtual machine
        // scale set
        //
        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region2)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1) // Zone redundant - zone 1 + zone 2
                .withAvailabilityZone(AvailabilityZoneId.ZONE_2)
                .create();

        // Check zones
        //
        Assertions.assertNotNull(virtualMachineScaleSet.availabilityZones());
        Assertions.assertEquals(2, virtualMachineScaleSet.availabilityZones().size());

        // Validate Network specific properties (LB, VNet, NIC, IPConfig etc..)
        //
        Assertions.assertNull(virtualMachineScaleSet.getPrimaryInternalLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerBackends().size() == 0);
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assertions.assertNotNull(virtualMachineScaleSet.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerBackends().size() == 2);
        Assertions
            .assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().size() == 2);

        Network primaryNetwork = virtualMachineScaleSet.getPrimaryNetwork();
        Assertions.assertNotNull(primaryNetwork.id());
    }

    @Test
    public void canEnableMSIOnVirtualMachineScaleSetWithoutRoleAssignment() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSystemAssignedManagedServiceIdentity()
                .create();

        // Validate service created service principal
        //
        // TODO: Renable the below code snippet: https://github.com/Azure/azure-libraries-for-net/issues/739

        //        ServicePrincipal servicePrincipal = authorizationManager
        //                .servicePrincipals()
        //                .getById(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        //
        //        Assertions.assertNotNull(servicePrincipal);
        //        Assertions.assertNotNull(servicePrincipal.inner());

        // Ensure role assigned for resource group
        //
        PagedIterable<RoleAssignment> rgRoleAssignments = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments);
        boolean found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                .principalId()
                .equalsIgnoreCase(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertFalse(
                found, "Resource group should not have a role assignment with virtual machine scale set MSI principal");
    }

    @Test
    public void canEnableMSIOnVirtualMachineScaleSetWithMultipleRoleAssignment() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        StorageAccount storageAccount =
            this
                .storageManager
                .storageAccounts()
                .define(generateRandomResourceName("jvcsrg", 10))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .create();

        VirtualMachineScaleSet virtualMachineScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .withSystemAssignedIdentityBasedAccessTo(storageAccount.id(), BuiltInRole.CONTRIBUTOR)
                .create();

        Assertions.assertNotNull(virtualMachineScaleSet.managedServiceIdentityType());
        Assertions
            .assertTrue(
                virtualMachineScaleSet.managedServiceIdentityType().equals(ResourceIdentityType.SYSTEM_ASSIGNED));

        // Validate service created service principal
        //
        // TODO: Renable the below code snippet: https://github.com/Azure/azure-libraries-for-net/issues/739

        //        ServicePrincipal servicePrincipal = authorizationManager
        //                .servicePrincipals()
        //                .getById(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId());
        //
        //        Assertions.assertNotNull(servicePrincipal);
        //        Assertions.assertNotNull(servicePrincipal.inner());

        // Ensure role assigned for resource group
        //
        PagedIterable<RoleAssignment> rgRoleAssignments = authorizationManager.roleAssignments().listByScope(resourceGroup.id());
        Assertions.assertNotNull(rgRoleAssignments);
        boolean found = false;
        for (RoleAssignment roleAssignment : rgRoleAssignments) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                .principalId()
                .equalsIgnoreCase(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found, "Resource group should have a role assignment with virtual machine scale set MSI principal");

        // Ensure role assigned for storage account
        //
        PagedIterable<RoleAssignment> stgRoleAssignments =
            authorizationManager.roleAssignments().listByScope(storageAccount.id());
        Assertions.assertNotNull(stgRoleAssignments);
        found = false;
        for (RoleAssignment roleAssignment : stgRoleAssignments) {
            if (roleAssignment.principalId() != null
                && roleAssignment
                .principalId()
                .equalsIgnoreCase(virtualMachineScaleSet.systemAssignedManagedServiceIdentityPrincipalId())) {
                found = true;
                break;
            }
        }
        Assertions
            .assertTrue(
                found, "Storage account should have a role assignment with virtual machine scale set MSI principal");
    }

    @Test
    public void canGetSingleVMSSInstance() throws Exception {

        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.BASIC);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withNewStorageAccount(generateRandomResourceName("stg", 15))
            .withNewStorageAccount(generateRandomResourceName("stg3", 15))
            .withUpgradeMode(UpgradeMode.MANUAL)
            .create();

        VirtualMachineScaleSet virtualMachineScaleSet =
            this.computeManager.virtualMachineScaleSets().getByResourceGroup(rgName, vmssName);
        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = virtualMachineScaleSet.virtualMachines();
        VirtualMachineScaleSetVM firstVm = virtualMachineScaleSetVMs.list().iterator().next();
        VirtualMachineScaleSetVM fetchedVm = virtualMachineScaleSetVMs.getInstance(firstVm.instanceId());
        this.checkVmsEqual(firstVm, fetchedVm);
        VirtualMachineScaleSetVM fetchedAsyncVm =
            virtualMachineScaleSetVMs.getInstanceAsync(firstVm.instanceId()).block();
        this.checkVmsEqual(firstVm, fetchedAsyncVm);
    }

    @Test
    public void canCreateLowPriorityVMSSInstance() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer =
            createInternetFacingLoadBalancer(region, resourceGroup, "1", LoadBalancerSkuType.STANDARD);

        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assertions.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet vmss =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg3", 15))
                .withUpgradeMode(UpgradeMode.MANUAL)
                .withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
                .withMaxPrice(-1.0)
                .create();

        Assertions.assertEquals(vmss.virtualMachinePriority(), VirtualMachinePriorityTypes.LOW);
        Assertions.assertEquals(vmss.virtualMachineEvictionPolicy(), VirtualMachineEvictionPolicyTypes.DEALLOCATE);
        Assertions.assertEquals(vmss.billingProfile().maxPrice(), (Double) (-1.0));

        vmss.update().withMaxPrice(2000.0).apply();
        Assertions.assertEquals(vmss.billingProfile().maxPrice(), (Double) 2000.0);
    }

    @Test
    public void canListInstancesIncludingInstanceView() {
        // however, it is hard to verify in automation that we do not send redundant REST call after received the instance view (i.e., no REST call to virtualMachines/{instanceId}/instanceView)
        // currently this is verified manually

        final String vmssName = generateRandomResourceName("vmss", 10);

        Network network = this.networkManager
            .networks()
            .define("vmssvnet")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withUpgradeMode(UpgradeMode.MANUAL)
            .withCapacity(3)
            .create();

        // list with VirtualMachineScaleSetVMExpandType.INSTANCE_VIEW
        VirtualMachineScaleSet vmss = computeManager.virtualMachineScaleSets().getByResourceGroup(rgName, vmssName);
        List<VirtualMachineScaleSetVM> vmInstances = vmss.virtualMachines().list(null, VirtualMachineScaleSetVMExpandType.INSTANCE_VIEW).stream().collect(Collectors.toList());
        Assertions.assertEquals(3, vmInstances.size());
        List<PowerState> powerStates = vmInstances.stream().map(VirtualMachineScaleSetVM::powerState).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(PowerState.RUNNING, PowerState.RUNNING, PowerState.RUNNING), powerStates);

        // update status of VM and check again
        String firstInstanceId = vmInstances.get(0).instanceId();
        computeManager.serviceClient().getVirtualMachineScaleSetVMs().deallocate(rgName, vmssName, firstInstanceId);
        vmInstances.get(0).refresh();
        powerStates = vmInstances.stream().map(VirtualMachineScaleSetVM::powerState).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(PowerState.DEALLOCATED, PowerState.RUNNING, PowerState.RUNNING), powerStates);

        // check single VM
        VirtualMachineScaleSetVM vmInstance0 = vmss.virtualMachines().getInstance(firstInstanceId);
        Assertions.assertEquals(PowerState.DEALLOCATED, vmInstance0.powerState());
    }

    @Test
    public void canPerformSimulateEvictionOnSpotVMSSInstance() {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
            .define(rgName)
            .withRegion(region)
            .create();

        Network network = this.networkManager
            .networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        VirtualMachineScaleSet vmss = computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
            .create();

        PagedIterable<VirtualMachineScaleSetVM> vmInstances = vmss.virtualMachines().list();
        for (VirtualMachineScaleSetVM instance : vmInstances) {
            Assertions.assertTrue(instance.osDiskSizeInGB() > 0);
            // call simulate eviction
            vmss.virtualMachines().simulateEviction(instance.instanceId());
        }

        boolean deallocated = false;
        int pollIntervalInMinutes = 5;
        for (int i = 0; i < 30; i += pollIntervalInMinutes) {
            ResourceManagerUtils.sleep(Duration.ofMinutes(pollIntervalInMinutes));

            deallocated = true;
            for (VirtualMachineScaleSetVM instance : vmInstances) {
                instance.refresh();

                if (instance.powerState() != PowerState.DEALLOCATED) {
                    deallocated = false;
                }
            }

            if (deallocated) {
                break;
            }
        }
        Assertions.assertTrue(deallocated);

        for (VirtualMachineScaleSetVM instance : vmInstances) {
            instance.refresh();
            Assertions.assertEquals(0, instance.osDiskSizeInGB());
        }
    }

    private void checkVmsEqual(VirtualMachineScaleSetVM original, VirtualMachineScaleSetVM fetched) {
        Assertions.assertEquals(original.administratorUserName(), fetched.administratorUserName());
        Assertions.assertEquals(original.availabilitySetId(), fetched.availabilitySetId());
        Assertions.assertEquals(original.bootDiagnosticEnabled(), fetched.bootDiagnosticEnabled());
        Assertions.assertEquals(original.bootDiagnosticStorageAccountUri(), fetched.bootDiagnosticStorageAccountUri());
        Assertions.assertEquals(original.computerName(), fetched.computerName());
        Assertions.assertEquals(original.dataDisks().size(), fetched.dataDisks().size());
        Assertions.assertEquals(original.extensions().size(), fetched.extensions().size());
        Assertions.assertEquals(original.instanceId(), fetched.instanceId());
        Assertions.assertEquals(original.isLatestScaleSetUpdateApplied(), fetched.isLatestScaleSetUpdateApplied());
        Assertions.assertEquals(original.isLinuxPasswordAuthenticationEnabled(), fetched.isLinuxPasswordAuthenticationEnabled());
        Assertions.assertEquals(original.isManagedDiskEnabled(), fetched.isManagedDiskEnabled());
        Assertions.assertEquals(original.isOSBasedOnCustomImage(), fetched.isOSBasedOnCustomImage());
        Assertions.assertEquals(original.isOSBasedOnPlatformImage(), fetched.isOSBasedOnPlatformImage());
        Assertions.assertEquals(original.isOSBasedOnStoredImage(), fetched.isOSBasedOnStoredImage());
        Assertions.assertEquals(original.isWindowsAutoUpdateEnabled(), fetched.isWindowsAutoUpdateEnabled());
        Assertions.assertEquals(original.isWindowsVMAgentProvisioned(), original.isWindowsVMAgentProvisioned());
        Assertions.assertEquals(original.networkInterfaceIds().size(), fetched.networkInterfaceIds().size());
        Assertions.assertEquals(original.osDiskCachingType(), fetched.osDiskCachingType());
        Assertions.assertEquals(original.osDiskId(), fetched.osDiskId());
        Assertions.assertEquals(original.osDiskName(), fetched.osDiskName());
        Assertions.assertEquals(original.osDiskSizeInGB(), fetched.osDiskSizeInGB());
        Assertions.assertEquals(original.osType(), fetched.osType());
        Assertions.assertEquals(original.osUnmanagedDiskVhdUri(), fetched.osUnmanagedDiskVhdUri());
        Assertions.assertEquals(original.powerState(), fetched.powerState());
        Assertions.assertEquals(original.primaryNetworkInterfaceId(), fetched.primaryNetworkInterfaceId());
        Assertions.assertEquals(original.size(), fetched.size());
        Assertions.assertEquals(original.sku().name(), fetched.sku().name());
        Assertions.assertEquals(original.storedImageUnmanagedVhdUri(), fetched.storedImageUnmanagedVhdUri());
        Assertions.assertEquals(original.unmanagedDataDisks().size(), fetched.unmanagedDataDisks().size());
        Assertions.assertEquals(original.windowsTimeZone(), fetched.windowsTimeZone());
    }

    private void checkVMInstances(VirtualMachineScaleSet vmScaleSet) {
        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedIterable<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();

        Assertions.assertEquals(TestUtilities.getSize(virtualMachines), vmScaleSet.capacity());
        Assertions.assertTrue(TestUtilities.getSize(virtualMachines) > 0);
        virtualMachineScaleSetVMs.updateInstances(virtualMachines.iterator().next().instanceId());

        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assertions.assertNotNull(vm.size());
            Assertions.assertEquals(vm.osType(), OperatingSystemTypes.LINUX);
            Assertions.assertNotNull(vm.computerName().startsWith(vmScaleSet.computerNamePrefix()));
            Assertions.assertTrue(vm.isOSBasedOnPlatformImage());
            Assertions.assertNull(vm.osDiskId()); // VMSS is un-managed, so osDiskId must be null
            Assertions.assertNotNull(vm.osUnmanagedDiskVhdUri()); // VMSS is un-managed, so osVhd should not be null
            Assertions.assertNull(vm.storedImageUnmanagedVhdUri());
            Assertions.assertFalse(vm.isWindowsAutoUpdateEnabled());
            Assertions.assertFalse(vm.isWindowsVMAgentProvisioned());
            Assertions.assertTrue(vm.administratorUserName().equalsIgnoreCase("jvuser"));
            VirtualMachineImage vmImage = vm.getOSPlatformImage();
            Assertions.assertNotNull(vmImage);
            Assertions.assertEquals(vm.extensions().size(), vmScaleSet.extensions().size());
            Assertions.assertNotNull(vm.powerState());
            vm.refreshInstanceView();
        }

        // Check actions
        VirtualMachineScaleSetVM virtualMachineScaleSetVM = virtualMachines.iterator().next();
        Assertions.assertNotNull(virtualMachineScaleSetVM);
        virtualMachineScaleSetVM.restart();
        virtualMachineScaleSetVM.powerOff();
        virtualMachineScaleSetVM.refreshInstanceView();
        Assertions.assertEquals(virtualMachineScaleSetVM.powerState(), PowerState.STOPPED);
        virtualMachineScaleSetVM.start();

        // Check Instance NICs
        //
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            PagedIterable<VirtualMachineScaleSetNetworkInterface> nics =
                vmScaleSet.listNetworkInterfacesByInstanceId(vm.instanceId());
            Assertions.assertNotNull(nics);
            Assertions.assertEquals(TestUtilities.getSize(nics), 1);
            VirtualMachineScaleSetNetworkInterface nic = nics.iterator().next();
            Assertions.assertNotNull(nic.virtualMachineId());
            Assertions.assertTrue(nic.virtualMachineId().toLowerCase().equalsIgnoreCase(vm.id()));
            Assertions.assertNotNull(vm.listNetworkInterfaces());
            VirtualMachineScaleSetNetworkInterface nicA =
                vmScaleSet.getNetworkInterfaceByInstanceId(vm.instanceId(), nic.name());
            Assertions.assertNotNull(nicA);
            VirtualMachineScaleSetNetworkInterface nicB = vm.getNetworkInterface(nic.name());
            String nicIdB = vm.getNetworkInterfaceAsync(nic.name()).map(n -> nic.primaryIPConfiguration().networkId()).block();
            Assertions.assertNotNull(nicB);
            Assertions.assertNotNull(nicIdB);
        }
    }

    @Test
    public void testVirtualMachineScaleSetSkuTypes() {
        rgName = null;
        VirtualMachineScaleSetSkuTypes skuType = VirtualMachineScaleSetSkuTypes.STANDARD_A0;
        Assertions.assertNull(skuType.sku().capacity());
        // first copy of sku
        Sku sku1 = skuType.sku();
        Assertions.assertNull(sku1.capacity());
        sku1.withCapacity(1L);
        Assertions.assertEquals(sku1.capacity().longValue(), 1);
        // Ensure the original is not affected
        Assertions.assertNull(skuType.sku().capacity());
        // second copy of sku
        Sku sku2 = skuType.sku();
        Assertions.assertNull(sku2.capacity());
        sku2.withCapacity(2L);
        Assertions.assertEquals(sku2.capacity().longValue(), 2);
        // Ensure the original is not affected
        Assertions.assertNull(skuType.sku().capacity());
        // Ensure previous copy is not affected due to change in first copy
        Assertions.assertEquals(sku1.capacity().longValue(), 1);
    }

    @Test
    public void canDeleteVMSSInstance() throws Exception {
        String euapRegion = "eastus2euap";

        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName)
            .withRegion(euapRegion)
            .create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(euapRegion)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        VirtualMachineScaleSet vmss = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(euapRegion)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withCapacity(4)    // 4 instances
            .create();

        Assertions.assertEquals(4, vmss.virtualMachines().list().stream().count());

        // force delete first 2 instances
        List<String> firstTwoIds = vmss.virtualMachines().list().stream()
            .limit(2)
            .map(VirtualMachineScaleSetVM::instanceId)
            .collect(Collectors.toList());
        vmss.virtualMachines().deleteInstances(firstTwoIds, true);

        Assertions.assertEquals(2, vmss.virtualMachines().list().stream().count());

        // delete next 1 instance
        vmss.virtualMachines().deleteInstances(Collections.singleton(vmss.virtualMachines().list().stream().findFirst().get().instanceId()), false);

        Assertions.assertEquals(1, vmss.virtualMachines().list().stream().count());

        // force delete next 1 instance
        computeManager.virtualMachineScaleSets().deleteInstances(rgName, vmssName, Collections.singleton(vmss.virtualMachines().list().stream().findFirst().get().instanceId()), false);
    }

    @Test
    public void canCreateFlexibleVMSS() throws Exception {
        // create vmss with flexible orchestration type
        VirtualMachineScaleSetInner options = new VirtualMachineScaleSetInner();
        options.withOrchestrationMode(OrchestrationMode.FLEXIBLE)
            .withPlatformFaultDomainCount(1)
            .withLocation(region.name());

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName)
            .withRegion(region.name())
            .create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region.name())
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();
        final String vmssName = generateRandomResourceName("vmss", 10);
        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);

        VirtualMachineScaleSet vmss = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region.name())
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.CENTOS_8_3)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .create();

        Assertions.assertNotNull(vmss.innerModel().virtualMachineProfile());
        Assertions.assertNotNull(vmss.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertNotNull(vmss.getPrimaryNetwork());
        Assertions.assertEquals(vmss.orchestrationMode(), OrchestrationMode.FLEXIBLE);

    }

    @Test
    public void canUpdateVMSSInCreateOrUpdateMode() throws Exception {
        // create vmss with empty profile
        //create vmss with uniform orchestration type
        String euapRegion = "eastus2euap";

        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName)
            .withRegion(euapRegion)
            .create();

        VirtualMachineScaleSet vmss = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(euapRegion)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();

        Assertions.assertEquals(vmss.orchestrationMode(), OrchestrationMode.FLEXIBLE);
        Assertions.assertNull(vmss.innerModel().virtualMachineProfile());

        Assertions.assertNull(vmss.getPrimaryNetwork());
        Assertions.assertNull(vmss.storageProfile());
        Assertions.assertNull(vmss.networkProfile());
        Assertions.assertNull(vmss.virtualMachinePublicIpConfig());
        Assertions.assertEquals(vmss.applicationGatewayBackendAddressPoolsIds().size(), 0);
        Assertions.assertEquals(vmss.applicationSecurityGroupIds().size(), 0);
        Assertions.assertNull(vmss.billingProfile());
        Assertions.assertNull(vmss.bootDiagnosticsStorageUri());
        Assertions.assertNull(vmss.getPrimaryInternalLoadBalancer());
        Assertions.assertEquals(vmss.vhdContainers().size(), 0);

        Assertions.assertEquals(vmss.listPrimaryInternalLoadBalancerBackends().size(), 0);
        Assertions.assertEquals(vmss.listPrimaryInternalLoadBalancerInboundNatPools().size(), 0);
        Assertions.assertEquals(vmss.listPrimaryInternetFacingLoadBalancerBackends().size(), 0);
        Assertions.assertEquals(vmss.listPrimaryInternetFacingLoadBalancerInboundNatPools().size(), 0);
        Assertions.assertEquals(vmss.primaryPublicIpAddressIds().size(), 0);

        Assertions.assertFalse(vmss.isAcceleratedNetworkingEnabled());
        Assertions.assertFalse(vmss.isBootDiagnosticsEnabled());
        Assertions.assertFalse(vmss.isIpForwardingEnabled());
        Assertions.assertNull(vmss.networkSecurityGroupId());
        Assertions.assertFalse(vmss.isManagedDiskEnabled());

        // update tag on vmss flex with no profile
        vmss.update()
            .withTag("tag1", "value1")
            .apply();

        Assertions.assertNotNull(vmss.tags());
        Assertions.assertEquals(vmss.tags().get("tag1"), "value1");

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(euapRegion)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();
        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(Region.fromName(euapRegion), resourceGroup, "1", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);
        final String vmssVmDnsLabel = generateRandomResourceName("pip", 10);

        // update vmss, attach profile
        vmss = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(euapRegion)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withCapacity(1)
            .withVirtualMachinePublicIp(vmssVmDnsLabel)
            .create();
        Assertions.assertNotNull(vmss.innerModel().virtualMachineProfile());
        Assertions.assertEquals(vmss.orchestrationMode(), OrchestrationMode.FLEXIBLE);
        Assertions.assertNotNull(vmss.getPrimaryInternetFacingLoadBalancer());
        Assertions.assertNotNull(vmss.getPrimaryNetwork());

        // update tag on vmss flex with profile
        vmss = this.computeManager
            .virtualMachineScaleSets()
            .getById(vmss.id());
        Assertions.assertNotNull(vmss);
        vmss.update()
            .withTag("tag1", "value2")
            .apply();
        Assertions.assertNotNull(vmss.innerModel().virtualMachineProfile());
        Assertions.assertNotNull(vmss.tags());
        Assertions.assertEquals(vmss.tags().get("tag1"), "value2");

        Assertions.assertNotNull(vmss.getPrimaryNetwork());
        Assertions.assertNotNull(vmss.storageProfile());
        Assertions.assertNotNull(vmss.networkProfile());
        Assertions.assertNotNull(vmss.virtualMachinePublicIpConfig());

        Assertions.assertNotEquals(vmss.listPrimaryInternetFacingLoadBalancerBackends().size(), 0);
        Assertions.assertEquals(vmss.listPrimaryInternetFacingLoadBalancerInboundNatPools().size(), 0);
        Assertions.assertNotEquals(vmss.primaryPublicIpAddressIds().size(), 0);

    }

    @Test
    public void canGetOrchestrationType() {

        //create vmss with uniform orchestration type
        String euapRegion = "eastus2euap";

        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName)
            .withRegion(euapRegion)
            .create();

        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(euapRegion)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        VirtualMachineScaleSet vmss = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(euapRegion)
            .withExistingResourceGroup(resourceGroup)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withCapacity(1)    // 1 instances
            .create();

        Assertions.assertEquals(vmss.orchestrationMode(), OrchestrationMode.UNIFORM);

        // create vmss with flexible orchestration type
        final String vmssName2 = generateRandomResourceName("vmss", 10);
        VirtualMachineScaleSet vmss2 = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName2)
            .withRegion(euapRegion)
            .withExistingResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .create();

        Assertions.assertNotNull(vmss2);
        Assertions.assertEquals(vmss2.orchestrationMode(), OrchestrationMode.FLEXIBLE);
    }

    @Test
    public void npeProtectionTest() throws Exception {
        String euapRegion = "eastus2euap";

        final String vmssName = generateRandomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName)
            .withRegion(euapRegion)
            .create();

        VirtualMachineScaleSet vmss = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(euapRegion)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();

        String excludeMethodsString = "start,startAsync,reimage,reimageAsync,deallocate,deallocateAsync,powerOff,powerOffAsync,restart,restartAsync";
        Set<String> excludeMethods = new HashSet<>(Arrays.asList(excludeMethodsString.split(",")));
        Set<String> invoked = new HashSet<>();
        // invoke all the methods with 0 parameters except those from the exclusion set
        for (Method method : VirtualMachineScaleSet.class.getDeclaredMethods()) {
            if (!excludeMethods.contains(method.getName()) && method.getParameterCount() == 0) {
                method.invoke(vmss);
                invoked.add(method.getName());
            }
        }
        Assertions.assertTrue(invoked.contains("isEphemeralOSDisk"));
        Assertions.assertFalse(invoked.contains("start"));
    }

    @Test
    public void canBatchOperateVMSSInstance() {
        final String vmssName = generateRandomResourceName("vmss", 10);

        Network network = this.networkManager
            .networks()
            .define("vmssvnet")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        VirtualMachineScaleSet vmss = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withUpgradeMode(UpgradeMode.AUTOMATIC)
            .withCapacity(3)
            .create();

        List<VirtualMachineScaleSetVM> instances = vmss.virtualMachines().list().stream().collect(Collectors.toList());
        Assertions.assertEquals(3, instances.size());

        // batch operation on instance 0 and 2
        // leave instance 1 untouched
        Collection<String> instanceIds = Arrays.asList(instances.get(0).instanceId(), instances.get(2).instanceId());

        VirtualMachineScaleSetVMs vmInstances = vmss.virtualMachines();
        VirtualMachineScaleSetVM vmInstance2 = vmss.virtualMachines().getInstance(instances.get(2).instanceId());

//        vmInstances.reimageInstances(instanceIds);

        vmInstances.redeployInstances(instanceIds);

        vmInstances.powerOffInstances(instanceIds, true);
        vmInstance2.refreshInstanceView();
        Assertions.assertEquals(PowerState.STOPPED, vmInstance2.powerState());

        vmInstances.startInstances(instanceIds);
        vmInstance2.refreshInstanceView();
        Assertions.assertEquals(PowerState.RUNNING, vmInstance2.powerState());

        vmInstances.restartInstances(instanceIds);
        vmInstance2.refreshInstanceView();
        Assertions.assertEquals(PowerState.RUNNING, vmInstance2.powerState());

        vmInstances.deallocateInstances(instanceIds);
        vmInstance2.refreshInstanceView();
        Assertions.assertEquals(PowerState.DEALLOCATED, vmInstance2.powerState());

        // instance 1 is not affected
        VirtualMachineScaleSetVM vmInstance1 = vmss.virtualMachines().getInstance(instances.get(1).instanceId());
        Assertions.assertEquals(PowerState.RUNNING, vmInstance1.powerState());
    }

    @Test
    public void canCreateVMSSWithEphemeralOSDisk() throws Exception {
        // uniform vmss with ephemeral os disk
        final String vmssName = generateRandomResourceName("vmss", 10);

        Network network = this.networkManager
            .networks()
            .define("vmssvnet")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        VirtualMachineScaleSet uniformVMSS = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_DS1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withoutPrimaryInternetFacingLoadBalancer()
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withUpgradeMode(UpgradeMode.AUTOMATIC)
            .withEphemeralOSDisk()
            .withPlacement(DiffDiskPlacement.CACHE_DISK)
            .withCapacity(2)
            .create();
        Assertions.assertTrue(uniformVMSS.isEphemeralOSDisk());

        // flex vmss with ephemeral os disk
        Network network2 =
            this
                .networkManager
                .networks()
                .define("vmssvnet2")
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withAddressSpace("10.1.0.0/16")
                .withSubnet("subnet1", "10.1.0.0/16")
                .create();
        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, this.resourceManager.resourceGroups().getByName(rgName), "1", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);

        final String vmssName1 = generateRandomResourceName("vmss", 10);
        VirtualMachineScaleSet flexVMSS = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName1)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_DS1_V2)
            .withExistingPrimaryNetworkSubnet(network2, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withEphemeralOSDisk()
            .withPlacement(DiffDiskPlacement.CACHE_DISK)
            .create();
        Assertions.assertTrue(flexVMSS.isEphemeralOSDisk());
        VirtualMachine instance1 = this.computeManager
            .virtualMachines()
            .getById(flexVMSS.virtualMachines().list().stream().iterator().next().id());
        Assertions.assertTrue(instance1.isOSDiskEphemeral());

        // can add vm with non-ephemeral os disk to flex vmss with ephemeral os disk vm profile
        final String vmName = generateRandomResourceName("vm", 10);
        VirtualMachine vm = this.computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network2)
            .withSubnet("subnet1")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withExistingVirtualMachineScaleSet(flexVMSS)
            .create();
        Assertions.assertEquals(vm.virtualMachineScaleSetId(), flexVMSS.id());
        // flex vmss can have a mixed set of VMs with ephemeral and non-ephemeral os disk
        // which contradicts the FAQ: https://docs.microsoft.com/en-us/azure/virtual-machines/ephemeral-os-disks#frequently-asked-questions
        Assertions.assertFalse(vm.isOSDiskEphemeral());
    }
}
