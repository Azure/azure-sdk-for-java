package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_EAST;

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
    public void canCreateVirtualMachineScaleSetWithCustomScriptExtension() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);
        final String uname = "jvuser";
        final String password = "123OData!@#123";
        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/azure-mgmt-compute/src/test/assets/install_apache.sh";
        final String installCommand = "bash install_apache.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(apacheInstallScript);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(REGION)
                .create();

        Network network = this.networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(REGION, resourceGroup, "1");
        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets().define(vmssName)
                .withRegion(REGION)
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
                    .withPublicSetting("fileUris",fileUris)
                    .withPublicSetting("commandToExecute", installCommand)
                .attach()
                .create();

        checkVMInstances(virtualMachineScaleSet);

        List<String> publicIpAddressIds = virtualMachineScaleSet.primaryPublicIpAddressIds();
        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses()
                .getById(publicIpAddressIds.get(0));

        String fqdn = publicIpAddress.fqdn();
        // Assert public load balancing connection
        if (!IS_MOCKED) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://" + fqdn)
                    .build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(response.code(), 200);
        }

        // Check SSH to VM instances via Nat rule
        //
        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            PagedList<VirtualMachineScaleSetNetworkInterface> networkInterfaces = vm.listNetworkInterfaces();
            Assert.assertEquals(networkInterfaces.size(), 1);
            VirtualMachineScaleSetNetworkInterface networkInterface = networkInterfaces.get(0);
            VirtualMachineScaleSetNicIpConfiguration primaryIpConfig = null;
            primaryIpConfig = networkInterface.primaryIpConfiguration();
            Assert.assertNotNull(primaryIpConfig);
            Integer sshFrontendPort = null;
            List<LoadBalancerInboundNatRule> natRules = primaryIpConfig.listAssociatedLoadBalancerInboundNatRules();
            for (LoadBalancerInboundNatRule natRule : natRules) {
                if (natRule.backendPort() == 22) {
                    sshFrontendPort = natRule.frontendPort();
                    break;
                }
            }
            Assert.assertNotNull(sshFrontendPort);

            this.sleep(1000 * 60); // Wait some time for VM to be available
            this.ensureCanDoSsh(fqdn, sshFrontendPort, uname, password);
        }
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        final String vmss_name = generateRandomResourceName("vmss", 10);
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
                "1");
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assert.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmss_name)
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
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .create();

        // Validate Network specific properties (LB, VNet, NIC, IPConfig etc..)
        //
        Assert.assertNull(virtualMachineScaleSet.getPrimaryInternalLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerBackends().size() == 0);
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assert.assertNotNull(virtualMachineScaleSet.getPrimaryInternetFacingLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerBackends().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().size() == 2);

        Network primaryNetwork = virtualMachineScaleSet.getPrimaryNetwork();
        Assert.assertNotNull(primaryNetwork.id());

        List<VirtualMachineScaleSetNetworkInterface> nics = virtualMachineScaleSet.listNetworkInterfaces();
        int nicCount = 0;
        for (VirtualMachineScaleSetNetworkInterface nic : nics) {
            nicCount++;
            Assert.assertNotNull(nic.id());
            Assert.assertTrue(nic.virtualMachineId().toLowerCase().startsWith(virtualMachineScaleSet.id().toLowerCase()));
            Assert.assertNotNull(nic.macAddress());
            Assert.assertNotNull(nic.dnsServers());
            Assert.assertNotNull(nic.appliedDnsServers());
            Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigs =  nic.ipConfigurations();
            Assert.assertEquals(ipConfigs.size(), 1);
            for (Map.Entry<String, VirtualMachineScaleSetNicIpConfiguration> entry :ipConfigs.entrySet()) {
                VirtualMachineScaleSetNicIpConfiguration ipConfig = entry.getValue();
                Assert.assertNotNull(ipConfig);
                Assert.assertTrue(ipConfig.isPrimary());
                Assert.assertNotNull(ipConfig.subnetName());
                Assert.assertTrue(primaryNetwork.id().toLowerCase().equalsIgnoreCase(ipConfig.networkId()));
                Assert.assertNotNull(ipConfig.privateIpAddress());
                Assert.assertNotNull(ipConfig.privateIpAddressVersion());
                Assert.assertNotNull(ipConfig.privateIpAllocationMethod());
                List<LoadBalancerBackend> lbBackends = ipConfig.listAssociatedLoadBalancerBackends();
                // VMSS is created with a internet facing LB with two Backend pools so there will be two
                // backends in ip-config as well
                Assert.assertEquals(lbBackends.size(), 2);
                for (LoadBalancerBackend lbBackend : lbBackends) {
                    Map<String, LoadBalancingRule> lbRules = lbBackend.loadBalancingRules();
                    Assert.assertEquals(lbRules.size(), 1);
                    for (Map.Entry<String, LoadBalancingRule> ruleEntry : lbRules.entrySet()) {
                        LoadBalancingRule rule = ruleEntry.getValue();
                        Assert.assertNotNull(rule);
                        Assert.assertTrue((rule.frontendPort() == 80 && rule.backendPort() == 80)
                                || (rule.frontendPort() == 443 && rule.backendPort() == 443));
                    }
                }
                List<LoadBalancerInboundNatRule> lbNatRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
                // VMSS is created with a internet facing LB with two nat pools so there will be two
                //  nat rules in ip-config as well
                Assert.assertEquals(lbNatRules.size(), 2);
                for (LoadBalancerInboundNatRule lbNatRule : lbNatRules) {
                    Assert.assertTrue((lbNatRule.frontendPort() >= 5000 && lbNatRule.frontendPort()<= 5099)
                            || (lbNatRule.frontendPort() >= 6000 && lbNatRule.frontendPort()<= 6099));
                    Assert.assertTrue(lbNatRule.backendPort() == 22 || lbNatRule.backendPort() == 23);
                }
            }
        }
        Assert.assertTrue(nicCount > 0);

        // Validate other properties
        //
        Assert.assertEquals(virtualMachineScaleSet.vhdContainers().size(), 2);
        Assert.assertEquals(virtualMachineScaleSet.sku(), VirtualMachineScaleSetSkuTypes.STANDARD_A0);
        // Check defaults
        Assert.assertTrue(virtualMachineScaleSet.upgradeModel() == UpgradeMode.AUTOMATIC);
        Assert.assertEquals(virtualMachineScaleSet.capacity(), 2);
        // Fetch the primary Virtual network
        primaryNetwork = virtualMachineScaleSet.getPrimaryNetwork();

        String inboundNatPoolToRemove = null;
        for (String inboundNatPoolName :
                virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().keySet()) {
            inboundNatPoolToRemove = inboundNatPoolName;
            break;
        }

        LoadBalancer internalLoadBalancer = createInternalLoadBalancer(REGION,
                resourceGroup,
                primaryNetwork,
                "1");

        virtualMachineScaleSet
                .update()
                .withExistingPrimaryInternalLoadBalancer(internalLoadBalancer)
                .withoutPrimaryInternetFacingLoadBalancerNatPools(inboundNatPoolToRemove) // Remove one NatPool
                .apply();

        virtualMachineScaleSet = this.computeManager
                .virtualMachineScaleSets()
                .getByGroup(RG_NAME, vmss_name);

        // Check LB after update
        //
        Assert.assertNotNull(virtualMachineScaleSet.getPrimaryInternetFacingLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerBackends().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternetFacingLoadBalancerInboundNatPools().size() == 1);

        Assert.assertNotNull(virtualMachineScaleSet.getPrimaryInternalLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerBackends().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.listPrimaryInternalLoadBalancerInboundNatPools().size() == 2);

        // Check NIC + IpConfig after update
        //
        nics = virtualMachineScaleSet.listNetworkInterfaces();
        nicCount = 0;
        for (VirtualMachineScaleSetNetworkInterface nic : nics) {
            nicCount++;
            Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigs =  nic.ipConfigurations();
            Assert.assertEquals(ipConfigs.size(), 1);
            for (Map.Entry<String, VirtualMachineScaleSetNicIpConfiguration> entry :ipConfigs.entrySet()) {
                VirtualMachineScaleSetNicIpConfiguration ipConfig = entry.getValue();
                Assert.assertNotNull(ipConfig);
                List<LoadBalancerBackend> lbBackends = ipConfig.listAssociatedLoadBalancerBackends();
                Assert.assertNotNull(lbBackends);
                // Updated VMSS has a internet facing LB with two backend pools and a internal LB with two
                // backend pools so there should be 4 backends in ip-config
                // #1: But this is not always happening, it seems update is really happening only
                // for subset of vms [TODO: Report this to network team]
                // Assert.True(lbBackends.Count == 4);
                // Assert.assertEquals(lbBackends.size(), 4);
                for (LoadBalancerBackend lbBackend : lbBackends) {
                    Map<String, LoadBalancingRule> lbRules = lbBackend.loadBalancingRules();
                    Assert.assertEquals(lbRules.size(), 1);
                    for (Map.Entry<String, LoadBalancingRule> ruleEntry : lbRules.entrySet()) {
                        LoadBalancingRule rule = ruleEntry.getValue();
                        Assert.assertNotNull(rule);
                        Assert.assertTrue((rule.frontendPort() == 80 && rule.backendPort() == 80)
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
                // Assert.assertEquals(lbNatRules.size(), 3);
                for (LoadBalancerInboundNatRule lbNatRule : lbNatRules) {
                    // As mentioned above some chnages are not propgating to all VM instances 6000+ should be there
                    Assert.assertTrue((lbNatRule.frontendPort() >= 6000 && lbNatRule.frontendPort()<= 6099)
                            || (lbNatRule.frontendPort() >= 5000 && lbNatRule.frontendPort()<= 5099)
                            || (lbNatRule.frontendPort() >= 8000 && lbNatRule.frontendPort()<= 8099)
                            || (lbNatRule.frontendPort() >= 9000 && lbNatRule.frontendPort()<= 9099));
                    // Same as above
                    Assert.assertTrue(lbNatRule.backendPort() == 23
                            || lbNatRule.backendPort() == 22
                            || lbNatRule.backendPort() == 44
                            || lbNatRule.backendPort() == 45);
                }
            }
        }
        Assert.assertTrue(nicCount > 0);
    }

    private void checkVMInstances(VirtualMachineScaleSet vmScaleSet) {
        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedList<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();
        Assert.assertEquals(virtualMachines.size(), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assert.assertNotNull(vm.size());
            Assert.assertEquals(vm.osType(), OperatingSystemTypes.LINUX);
            Assert.assertNotNull(vm.computerName().startsWith(vmScaleSet.computerNamePrefix()));
            Assert.assertTrue(vm.isLinuxPasswordAuthenticationEnabled());
            Assert.assertTrue(vm.isOSBasedOnPlatformImage());
            Assert.assertNull(vm.osDiskId());                   // VMSS is un-managed, so osDiskId must be null
            Assert.assertNotNull(vm.osUnmanagedDiskVhdUri());   // VMSS is un-managed, so osVhd should not be null
            Assert.assertNull(vm.storedImageUnmanagedVhdUri());
            Assert.assertFalse(vm.isWindowsAutoUpdateEnabled());
            Assert.assertFalse(vm.isWindowsVmAgentProvisioned());
            Assert.assertTrue(vm.administratorUserName().equalsIgnoreCase("jvuser"));
            VirtualMachineImage vmImage = vm.getOSPlatformImage();
            Assert.assertNotNull(vmImage);
            Assert.assertEquals(vm.extensions().size(), vmScaleSet.extensions().size());
            Assert.assertNotNull(vm.powerState());
            vm.refreshInstanceView();
        }

        // Check actions
        VirtualMachineScaleSetVM virtualMachineScaleSetVM = virtualMachines.get(0);
        Assert.assertNotNull(virtualMachineScaleSetVM);
        virtualMachineScaleSetVM.restart();
        virtualMachineScaleSetVM.powerOff();
        virtualMachineScaleSetVM.refreshInstanceView();
        Assert.assertEquals(virtualMachineScaleSetVM.powerState(), PowerState.STOPPED);
        virtualMachineScaleSetVM.start();

        // Check Instance NICs
        //
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            PagedList<VirtualMachineScaleSetNetworkInterface> nics
                    = vmScaleSet.listNetworkInterfacesByInstanceId(vm.instanceId());
            Assert.assertNotNull(nics);
            Assert.assertEquals(nics.size(), 1);
            VirtualMachineScaleSetNetworkInterface nic = nics.get(0);
            Assert.assertNotNull(nic.virtualMachineId());
            Assert.assertTrue(nic.virtualMachineId().toLowerCase().equalsIgnoreCase(vm.id()));
            Assert.assertNotNull(vm.listNetworkInterfaces());
            VirtualMachineScaleSetNetworkInterface nicA = vmScaleSet.getNetworkInterfaceByInstanceId(vm.instanceId(), nic.name());
            Assert.assertNotNull(nicA);
            VirtualMachineScaleSetNetworkInterface nicB = vm.getNetworkInterface(nic.name());
            Assert.assertNotNull(nicB);
        }
    }
}
