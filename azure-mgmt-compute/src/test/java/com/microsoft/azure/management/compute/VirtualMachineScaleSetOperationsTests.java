package com.microsoft.azure.management.compute;

import com.jcraft.jsch.JSch;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNicIpConfiguration;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String LOCATION = "eastasia";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachineScaleSetWithCustomScriptExtension() throws Exception {
        final String vmssName = ResourceNamer.randomResourceName("vmss", 10);
        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/azure-mgmt-compute/src/test/assets/install_apache.sh";
        final String installCommand = "bash install_apache.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(apacheInstallScript);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(LOCATION)
                .create();

        Network network = this.networkManager
                .networks()
                .define(ResourceNamer.randomResourceName("vmssvnet", 15))
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(resourceGroup, "1");
        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
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
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://" + fqdn)
                .build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.code(), 200);

        // Check SSH to VM instances via Nat rule
        //
        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            PagedList<VirtualMachineScaleSetNetworkInterface> networkInterfaces = vm.listNetworkInterfaces();
            Assert.assertEquals(networkInterfaces.size(), 1);
            VirtualMachineScaleSetNetworkInterface networkInterface = networkInterfaces.get(0);
            VirtualMachineScaleSetNicIpConfiguration primaryIpConfig = null;
            Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigs =  networkInterface.ipConfigurations();
            for (Map.Entry<String, VirtualMachineScaleSetNicIpConfiguration> entry :ipConfigs.entrySet()) {
                VirtualMachineScaleSetNicIpConfiguration ipConfig = entry.getValue();
                if(ipConfig.isPrimary()) {
                    primaryIpConfig = ipConfig;
                    break;
                }
            }
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
            // Assert public ssh connection to vm
            JSch jsch = new JSch();
            com.jcraft.jsch.Session session = null;
            try {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session = jsch.getSession("jvuser", fqdn, sshFrontendPort);
                session.setPassword("123OData!@#123");
                session.setConfig(config);
                session.connect();
            } catch (Exception e) {
                Assert.fail("SSH connection failed" + e.getMessage());
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        }
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        final String vmss_name = ResourceNamer.randomResourceName("vmss", 10);
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(LOCATION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(resourceGroup, "1");
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assert.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmss_name)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
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

        LoadBalancer internalLoadBalancer = createInternalLoadBalancer(resourceGroup,
                primaryNetwork,
                "1");

        virtualMachineScaleSet
                .update()
                .withExistingPrimaryInternalLoadBalancer(internalLoadBalancer)
                .withoutPrimaryInternalLoadBalancerNatPools(inboundNatPoolToRemove) // Remove one NatPool
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
                // backend pools so there will be 4 backends in ip-config
                Assert.assertEquals(lbBackends.size(), 4);
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
                // nat pools so there will be 3 nat rule in ip-config
                Assert.assertEquals(lbNatRules.size(), 3);
                for (LoadBalancerInboundNatRule lbNatRule : lbNatRules) {
                    Assert.assertTrue((lbNatRule.frontendPort() >= 5000 && lbNatRule.frontendPort()<= 5099)
                            || (lbNatRule.frontendPort() >= 8000 && lbNatRule.frontendPort()<= 8099)
                            || (lbNatRule.frontendPort() >= 9000 && lbNatRule.frontendPort()<= 9099));
                    Assert.assertTrue(lbNatRule.backendPort() == 22
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
            Assert.assertTrue(vm.isOsBasedOnPlatformImage());
            Assert.assertNull(vm.customImageVhdUri());
            Assert.assertFalse(vm.isWindowsAutoUpdateEnabled());
            Assert.assertFalse(vm.isWindowsVmAgentProvisioned());
            Assert.assertTrue(vm.administratorUserName().equalsIgnoreCase("jvuser"));
            VirtualMachineImage vmImage = vm.getPlatformImage();
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

    private LoadBalancer createHttpLoadBalancers(ResourceGroup resourceGroup,
                                                 String id) throws Exception {
        final String loadBalancerName = ResourceNamer.randomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName = loadBalancerName + "-BAP1";
        final String natPoolName = loadBalancerName + "-INP1";

        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses()
                .define(publicIpName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .definePublicFrontend(frontendName)
                .withExistingPublicIpAddress(publicIpAddress)
                .attach()
                .defineBackend(backendPoolName)
                .attach()
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(frontendName)
                .withFrontendPort(80)
                .withProbe("httpProbe")
                .withBackend(backendPoolName)
                .attach()
                .defineInboundNatPool(natPoolName)
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(frontendName)
                .withFrontendPortRange(5000, 5099)
                .withBackendPort(22)
                .attach()
                .create();
        return loadBalancer;

    }

    private LoadBalancer createInternetFacingLoadBalancer(ResourceGroup resourceGroup, String id) throws Exception {
        final String loadBalancerName = ResourceNamer.randomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";

        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses()
                .define(publicIpName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .definePublicFrontend(frontendName)
                .withExistingPublicIpAddress(publicIpAddress)
                .attach()
                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                .attach()
                .defineBackend(backendPoolName2)
                .attach()
                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .defineHttpProbe("httpsProbe")
                .withRequestPath("/")
                .attach()
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(80)
                    .withProbe("httpProbe")
                    .withBackend(backendPoolName1)
                .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(443)
                    .withProbe("httpsProbe")
                    .withBackend(backendPoolName2)
                .attach()
                // Add two nat pools to enable direct VM connectivity to port SSH and 23
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(5000, 5099)
                    .withBackendPort(22)
                .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(6000, 6099)
                    .withBackendPort(23)
                .attach()
                .create();
        return loadBalancer;
    }

    private LoadBalancer createInternalLoadBalancer(ResourceGroup resourceGroup,
                                                    Network network, String id) throws Exception {
        final String loadBalancerName = ResourceNamer.randomResourceName("InternalLb" + id + "-", 18);
        final String privateFrontEndName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String subnetName = "subnet1";

        LoadBalancer loadBalancer = this.networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, subnetName)
                .attach()
                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                .attach()
                .defineBackend(backendPoolName2)
                .attach()
                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .defineHttpProbe("httpsProbe")
                .withRequestPath("/")
                .attach()
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPort(1000)
                .withProbe("httpProbe")
                .withBackend(backendPoolName1)
                .attach()
                .defineLoadBalancingRule("httpsRule")
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPort(1001)
                .withProbe("httpsProbe")
                .withBackend(backendPoolName2)
                .attach()
                // Add two nat pools to enable direct VM connectivity to port 44 and 45
                .defineInboundNatPool(natPoolName1)
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPortRange(8000, 8099)
                .withBackendPort(44)
                .attach()
                .defineInboundNatPool(natPoolName2)
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPortRange(9000, 9099)
                .withBackendPort(45)
                .attach()
                .create();
        return loadBalancer;
    }
}
