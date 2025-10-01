// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.computefleet;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.computefleet.models.FleetUpdate;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.computefleet.models.ApiEntityReference;
import com.azure.resourcemanager.computefleet.models.BaseVirtualMachineProfile;
import com.azure.resourcemanager.computefleet.models.CachingTypes;
import com.azure.resourcemanager.computefleet.models.ComputeProfile;
import com.azure.resourcemanager.computefleet.models.DeleteOptions;
import com.azure.resourcemanager.computefleet.models.DiskControllerTypes;
import com.azure.resourcemanager.computefleet.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.computefleet.models.DiskDeleteOptionTypes;
import com.azure.resourcemanager.computefleet.models.EvictionPolicy;
import com.azure.resourcemanager.computefleet.models.Fleet;
import com.azure.resourcemanager.computefleet.models.FleetProperties;
import com.azure.resourcemanager.computefleet.models.ImageReference;
import com.azure.resourcemanager.computefleet.models.LinuxConfiguration;
import com.azure.resourcemanager.computefleet.models.NetworkApiVersion;
import com.azure.resourcemanager.computefleet.models.OperatingSystemTypes;
import com.azure.resourcemanager.computefleet.models.RegularPriorityAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.RegularPriorityProfile;
import com.azure.resourcemanager.computefleet.models.SpotAllocationStrategy;
import com.azure.resourcemanager.computefleet.models.SpotPriorityProfile;
import com.azure.resourcemanager.computefleet.models.StorageAccountTypes;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetIPConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetManagedDiskParameters;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkConfigurationProperties;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetNetworkProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSDisk;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetOSProfile;
import com.azure.resourcemanager.computefleet.models.VirtualMachineScaleSetStorageProfile;
import com.azure.resourcemanager.computefleet.models.VmSizeProfile;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class ComputeFleetManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private ResourceManager resourceManager = null;
    private ComputeFleetManager computeFleetManager = null;
    private NetworkManager networkManager = null;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        networkManager = NetworkManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        computeFleetManager = ComputeFleetManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .withPolicy(new ProviderRegistrationPolicy(networkManager.resourceManager()))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateComputeFleet() {
        Fleet fleet = null;
        try {
            String fleetName = "fleet" + randomPadding();
            String vmName = "vm" + randomPadding();
            String vnetName = "vnet" + randomPadding();
            String loadBalancerName = "loadBalancer" + randomPadding();
            String adminUser = "adminUser" + randomPadding();
            String adminPwd = UUID.randomUUID().toString().replace("-", "@").substring(0, 13);
            // @embedmeStart
            Network network = networkManager.networks()
                .define(vnetName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAddressSpace("172.16.0.0/16")
                .defineSubnet("default")
                .withAddressPrefix("172.16.0.0/24")
                .attach()
                .create();

            LoadBalancer loadBalancer = networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .defineLoadBalancingRule(loadBalancerName + "-lbrule")
                .withProtocol(TransportProtocol.TCP)
                .fromExistingSubnet(network, "default")
                .fromFrontendPort(80)
                .toBackend(loadBalancerName + "-backend")
                .toBackendPort(80)
                .attach()
                .withSku(LoadBalancerSkuType.STANDARD)
                .create();

            fleet = computeFleetManager.fleets()
                .define(fleetName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withProperties(new FleetProperties()
                    .withSpotPriorityProfile(new SpotPriorityProfile().withMaintain(false)
                        .withCapacity(1)
                        .withEvictionPolicy(EvictionPolicy.DELETE)
                        .withAllocationStrategy(SpotAllocationStrategy.LOWEST_PRICE))
                    .withVmSizesProfile(Arrays.asList(new VmSizeProfile().withName("Standard_D4s_v3")))
                    .withComputeProfile(new ComputeProfile()
                        .withBaseVirtualMachineProfile(new BaseVirtualMachineProfile()
                            .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                                .withImageReference(new ImageReference().withPublisher("canonical")
                                    .withOffer("ubuntu-24_04-lts")
                                    .withSku("server")
                                    .withVersion("latest"))
                                .withOsDisk(new VirtualMachineScaleSetOSDisk()
                                    .withManagedDisk(new VirtualMachineScaleSetManagedDiskParameters()
                                        .withStorageAccountType(StorageAccountTypes.PREMIUM_LRS))
                                    .withOsType(OperatingSystemTypes.LINUX)
                                    .withDiskSizeGB(30)
                                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                                    .withDeleteOption(DiskDeleteOptionTypes.DELETE)
                                    .withCaching(CachingTypes.READ_WRITE))
                                .withDiskControllerType(DiskControllerTypes.SCSI))
                            .withOsProfile(new VirtualMachineScaleSetOSProfile().withComputerNamePrefix(randomPadding())
                                .withAdminUsername(adminUser)
                                .withAdminPassword(adminPwd)
                                .withLinuxConfiguration(
                                    new LinuxConfiguration().withDisablePasswordAuthentication(false)))
                            .withNetworkProfile(
                                new VirtualMachineScaleSetNetworkProfile()
                                    .withNetworkInterfaceConfigurations(
                                        Arrays.asList(new VirtualMachineScaleSetNetworkConfiguration().withName(vmName)
                                            .withProperties(new VirtualMachineScaleSetNetworkConfigurationProperties()
                                                .withPrimary(true)
                                                .withEnableAcceleratedNetworking(false)
                                                .withDeleteOption(DeleteOptions.DELETE)
                                                .withIpConfigurations(Arrays
                                                    .asList(new VirtualMachineScaleSetIPConfiguration().withName(vmName)
                                                        .withProperties(
                                                            new VirtualMachineScaleSetIPConfigurationProperties()
                                                                .withPrimary(true)
                                                                .withSubnet(new ApiEntityReference()
                                                                    .withId(network.subnets().get("default").id()))
                                                                .withLoadBalancerBackendAddressPools(
                                                                    loadBalancer.loadBalancingRules()
                                                                        .get(loadBalancerName + "-lbrule")
                                                                        .innerModel()
                                                                        .backendAddressPools())))))))
                                    .withNetworkApiVersion(NetworkApiVersion.fromString("2024-03-01"))))
                        .withComputeApiVersion("2024-03-01")
                        .withPlatformFaultDomainCount(1))
                    .withRegularPriorityProfile(new RegularPriorityProfile()
                        .withAllocationStrategy(RegularPriorityAllocationStrategy.LOWEST_PRICE)
                        .withMinCapacity(1)
                        .withCapacity(2)))
                .create();
            // @embedmeEnd
            fleet.refresh();
            Assertions.assertEquals(fleetName, fleet.name());
            Assertions.assertEquals(fleetName, computeFleetManager.fleets().getById(fleet.id()).name());
            Assertions.assertTrue(
                computeFleetManager.fleets().listByResourceGroup(resourceGroupName).stream().findAny().isPresent());

            // test update, use serviceClient API to do a minimal PATCH
            computeFleetManager.serviceClient()
                .getFleets()
                .update(resourceGroupName, fleetName,
                    new FleetUpdate()
                        .withProperties(new FleetProperties().withRegularPriorityProfile(new RegularPriorityProfile()
                            .withAllocationStrategy(RegularPriorityAllocationStrategy.LOWEST_PRICE)
                            .withMinCapacity(1)
                            .withCapacity(3))));
        } finally {
            if (fleet != null) {
                computeFleetManager.fleets().deleteById(fleet.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
