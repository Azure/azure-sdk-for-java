// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.Sku;
import com.azure.resourcemanager.compute.models.UpgradeMode;
import com.azure.resourcemanager.compute.models.VaultCertificate;
import com.azure.resourcemanager.compute.models.VaultSecretGroup;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetPublicIpAddressConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
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
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNicIpConfiguration;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
            resourceManager.resourceGroups().deleteByName(rgName);
        }
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

        final String storageConnectionString =
            String
                .format(
                    "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    storageAccount.name(), storageAccountKey);
        // Get the script to upload
        //
        InputStream scriptFileAsStream =
            VirtualMachineScaleSetOperationsTests.class.getResourceAsStream("/install_apache.sh");
        // Get the size of the stream
        //
        int fileSize;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int bytesRead;
        while ((bytesRead = scriptFileAsStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        fileSize = outputStream.size();
        outputStream.close();
        // Upload the script file as block blob
        //
        URI fileUri;
        if (isPlaybackMode()) {
            fileUri = new URI("http://nonexisting.blob.core.windows.net/scripts/install_apache.sh");
        } else {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            CloudBlobContainer container = cloudBlobClient.getContainerReference("scripts");
            container.createIfNotExists();
            CloudBlockBlob blob = container.getBlockBlobReference("install_apache.sh");
            blob.upload(scriptFileAsStream, fileSize);
            fileUri = blob.getUri();
        }
        List<String> fileUris = new ArrayList<>();
        fileUris.add(fileUri.toString());

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
                .withRootPassword(password)
                .withUnmanagedDisks()
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withExistingStorageAccount(storageAccount)
                .defineNewExtension("CustomScriptForLinux")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("CustomScriptForLinux")
                .withVersion("1.4")
                .withMinorVersionAutoUpgrade()
                .withPublicSetting("fileUris", fileUris)
                .withProtectedSetting("commandToExecute", "bash install_apache.sh")
                .withProtectedSetting("storageAccountName", storageAccount.name())
                .withProtectedSetting("storageAccountKey", storageAccountKey)
                .attach()
                .create();
        // Validate extensions after create
        //
        Map<String, VirtualMachineScaleSetExtension> extensions = virtualMachineScaleSet.extensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertEquals(1, extensions.size());
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
        extensions = virtualMachineScaleSet.extensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertEquals(1, extensions.size());
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
        Assertions.assertEquals(1, extensions.size());
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
        final String apacheInstallScript =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/compute/mgmt/src/test/resources/install_apache.sh";
        final String installCommand = "bash install_apache.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(apacheInstallScript);

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
                .withRootPassword(password)
                .withUnmanagedDisks()
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .defineNewExtension("CustomScriptForLinux")
                .withPublisher("Microsoft.OSTCExtensions")
                .withType("CustomScriptForLinux")
                .withVersion("1.4")
                .withMinorVersionAutoUpgrade()
                .withPublicSetting("fileUris", fileUris)
                .withPublicSetting("commandToExecute", installCommand)
                .attach()
                .withUpgradeMode(UpgradeMode.MANUAL)
                .create();

        checkVMInstances(virtualMachineScaleSet);

        List<String> publicIPAddressIds = virtualMachineScaleSet.primaryPublicIpAddressIds();
        PublicIpAddress publicIPAddress = this.networkManager.publicIpAddresses().getById(publicIPAddressIds.get(0));

        String fqdn = publicIPAddress.fqdn();
        // Assert public load balancing connection
        if (!isPlaybackMode()) {
            HttpClient client = new NettyAsyncHttpClientBuilder().build();
            HttpRequest request = new HttpRequest(HttpMethod.GET, "http://" + fqdn);
            HttpResponse response = client.send(request).block();
            Assertions.assertEquals(response.getStatusCode(), 200);
        }

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

            this.sleep(1000 * 60); // Wait some time for VM to be available
            this.ensureCanDoSsh(fqdn, sshFrontendPort, uname, password);
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
                .withRootPassword(password())
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
        String secretValue = IOUtils.toString(embeddedJsonConfig);
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
                .withRootPassword(password())
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
                .withRootPassword("123OData!@#123")
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

    @Test
    public void
        canCreateTwoRegionalVirtualMachineScaleSetsAndAssociateEachWithDifferentBackendPoolOfZoneResilientLoadBalancer()
            throws Exception {
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
                .withRootPassword("123OData!@#123")
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
                .withRootPassword("123OData!@#123")
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
                .withRootPassword("123OData!@#123")
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
                .withRootPassword("123OData!@#123")
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
                .withRootPassword("123OData!@#123")
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
            .withRootPassword("123OData!@#123")
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
                .withRootPassword("123OData!@#123")
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
            .withRootPassword("123OData!@#123")
            .withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
            .create();

        PagedIterable<VirtualMachineScaleSetVM> vmInstances = vmss.virtualMachines().list();
        for (VirtualMachineScaleSetVM instance: vmInstances) {
            Assertions.assertTrue(instance.osDiskSizeInGB() > 0);
            // call simulate eviction
            vmss.virtualMachines().simulateEviction(instance.instanceId());
        }

        SdkContext.sleep(30 * 60 * 1000);

        for (VirtualMachineScaleSetVM instance: vmInstances) {
            instance.refresh();
            Assertions.assertTrue(instance.osDiskSizeInGB() == 0);
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
        Assertions
            .assertEquals(original.isLinuxPasswordAuthenticationEnabled(), fetched.isLatestScaleSetUpdateApplied());
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
            Assertions.assertTrue(vm.isLinuxPasswordAuthenticationEnabled());
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
            Assertions.assertNotNull(nicB);
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
        sku1.withCapacity(new Long(1));
        Assertions.assertEquals(sku1.capacity().longValue(), 1);
        // Ensure the original is not affected
        Assertions.assertNull(skuType.sku().capacity());
        // second copy of sku
        Sku sku2 = skuType.sku();
        Assertions.assertNull(sku2.capacity());
        sku2.withCapacity(new Long(2));
        Assertions.assertEquals(sku2.capacity().longValue(), 2);
        // Ensure the original is not affected
        Assertions.assertNull(skuType.sku().capacity());
        // Ensure previous copy is not affected due to change in first copy
        Assertions.assertEquals(sku1.capacity().longValue(), 1);
    }
}
