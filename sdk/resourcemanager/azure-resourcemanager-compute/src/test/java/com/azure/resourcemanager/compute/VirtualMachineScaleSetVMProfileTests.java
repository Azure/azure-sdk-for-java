package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.SubResource;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OrchestrationMode;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VaultCertificate;
import com.azure.resourcemanager.compute.models.VaultSecretGroup;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetPublicIpAddressConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaofeicao
 * @createdAt 2021-11-29 13:01
 */
public class VirtualMachineScaleSetVMProfileTests extends ComputeManagementTest {
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
    public void canUpdateVMSSInUpdateMode() throws Exception {
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

        vmss.update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withCapacity(1)
                .attach()
        .apply()
        ;
        Assertions.assertNotNull(vmss.innerModel().virtualMachineProfile());
        Assertions.assertEquals(vmss.orchestrationMode(), OrchestrationMode.FLEXIBLE);

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

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withVirtualMachinePublicIp(vmssVmDnsLabel)
                .withExistingApplicationSecurityGroup(asg)
                .attach()
            .create();

        // Create VMSS with instance public ip
        virtualMachineScaleSet.update()
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .defineFlexibleVirtualMachineProfile()
                    .withExistingPrimaryNetworkSubnet(network, "subnet1")
                    .withoutPrimaryInternetFacingLoadBalancer()
                    .withoutPrimaryInternalLoadBalancer()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("jvuser")
                    .withSsh(sshPublicKey())
                    .withVirtualMachinePublicIp(vmssVmDnsLabel)
                    .withExistingApplicationSecurityGroup(asg)
                    .attach()
            .apply();

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

        VirtualMachineScaleSet virtualMachineScaleSet = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
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

        virtualMachineScaleSet
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
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
                .attach()
            .apply();

        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            Assertions.assertTrue(vm.osProfile().secrets().size() > 0);
        }

        virtualMachineScaleSet.update().withoutSecrets().apply();

        for (VirtualMachineScaleSetVM vm : virtualMachineScaleSet.virtualMachines().list()) {
            Assertions.assertTrue(vm.osProfile().secrets().size() == 0);
        }
    }

    /*
     * Previously name
     * canCreateTwoRegionalVirtualMachineScaleSetsAndAssociateEachWithDifferentBackendPoolOfZoneResilientLoadBalancer
     * but this was too long for some OSes and would cause git checkout to fail.
     */
    @Test
    public void
    canCreateTwoRegionalVMScaleSetsWithDifferentPoolOfZoneResilientLoadBalancer()
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
        VirtualMachineScaleSet virtualMachineScaleSet1 = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName1)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();
        // HTTP goes to this virtual machine scale set
        //
        virtualMachineScaleSet1
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0)) // This VMSS in the first backend pool
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(0))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .attach()
            .apply();

        final String vmssName2 = generateRandomResourceName("vmss2", 10);
        VirtualMachineScaleSet virtualMachineScaleSet2 = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName2)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();
        // HTTPS goes to this virtual machine scale set
        //
        virtualMachineScaleSet2
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(1)) // This VMSS in the second backend pool
                .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .attach()
            .apply()
        ;

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
        VirtualMachineScaleSet virtualMachineScaleSet = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();
        // HTTP & HTTPS traffic on port 80, 443 of Internet-facing LB goes to corresponding port in virtual machine
        // scale set
        //
        virtualMachineScaleSet
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withAvailabilityZone(AvailabilityZoneId.ZONE_1) // Zone redundant - zone 1 + zone 2
                .withAvailabilityZone(AvailabilityZoneId.ZONE_2)
                .attach()
            .apply();

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

        VirtualMachineScaleSet virtualMachineScaleSet = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();

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

        virtualMachineScaleSet
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSystemAssignedManagedServiceIdentity()
                .attach()

            .apply()
        ;

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

        VirtualMachineScaleSet virtualMachineScaleSet = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();

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

        virtualMachineScaleSet
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
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
                .attach()
            .apply()
        ;

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
        VirtualMachineScaleSet virtualMachineScaleSet = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();
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

        virtualMachineScaleSet
            .update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg3", 15))
            .attach()
            .apply();

        virtualMachineScaleSet =
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
        VirtualMachineScaleSet vmss = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();
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

        vmss.update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withNewStorageAccount(generateRandomResourceName("stg", 15))
                .withNewStorageAccount(generateRandomResourceName("stg3", 15))
                .withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
                .withMaxPrice(-1.0)
                .attach()
            .apply()
        ;

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

        VirtualMachineScaleSet vmss = this
            .computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFlexibleOrchestrationMode()
            .create();

        Network network = this.networkManager
            .networks()
            .define("vmssvnet")
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withAddressSpace("10.0.0.0/28")
            .withSubnet("subnet1", "10.0.0.0/28")
            .create();

        vmss.update()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
            .defineFlexibleVirtualMachineProfile()
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
                .attach()
            .apply()
        ;

        PagedIterable<VirtualMachineScaleSetVM> vmInstances = vmss.virtualMachines().list();
        for (VirtualMachineScaleSetVM instance: vmInstances) {
            Assertions.assertTrue(instance.osDiskSizeInGB() > 0);
            // call simulate eviction
            vmss.virtualMachines().simulateEviction(instance.instanceId());
        }

        boolean deallocated = false;
        int pollIntervalInMinutes = 5;
        for (int i = 0; i < 30; i += pollIntervalInMinutes) {
            ResourceManagerUtils.sleep(Duration.ofMinutes(pollIntervalInMinutes));

            deallocated = true;
            for (VirtualMachineScaleSetVM instance: vmInstances) {
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

        for (VirtualMachineScaleSetVM instance: vmInstances) {
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

}
