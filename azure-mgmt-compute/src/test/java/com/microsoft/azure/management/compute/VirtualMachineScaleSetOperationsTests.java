package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String LOCATION = "eastus";
    private static final String VMSCALESETNAME = ResourceNamer.randomResourceName("vmss", 10);

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(LOCATION)
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(resourceGroup, "1");
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assert.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(VMSCALESETNAME)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUserName("jvuser")
                .withPassword("123OData!@#123")
                .defineNewExtension("CustomScriptForLinux")
                    .withPublisher("Microsoft.OSTCExtensions")
                    .withType("CustomScriptForLinux")
                    .withVersion("1.4")
                    .withAutoUpgradeMinorVersionEnabled()
                    .withPublicSetting("fileUris",fileUris)
                    .withPublicSetting("commandToExecute", installCommand)
                    .attach()
                .create();
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


}
