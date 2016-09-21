package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class NetworkInterfaceOperationsTests extends NetworkManagementTestBase {
    private static String RG_NAME = "javanwmrg";
    private static String resourceGroupId = null;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (resourceGroupId != null) {
            resourceManager.resourceGroups().delete(resourceGroupId);
        }
    }

    @Test
    public void canCreateBatchOfNetworkInterfaces() throws Exception {
        ResourceGroups resourceGroups = resourceManager.resourceGroups();
        Networks networks = networkManager.networks();
        NetworkInterfaces networkInterfaces = networkManager.networkInterfaces();

        Creatable<ResourceGroup> resourceGroupCreatable = resourceGroups
                .define(RG_NAME)
                .withRegion(Region.US_EAST);

        final String vnetName = "vnet1212";
        Creatable<Network> networkCreatable = networks
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare a batch of nics
        //
        final String nic1Name = "nic1";
        Creatable<NetworkInterface> networkInterface1Creatable = networkInterfaces
                .define(nic1Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.5");

        final String nic2Name = "nic2";
        Creatable<NetworkInterface> networkInterface2Creatable = networkInterfaces
                .define(nic2Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.6");

        final String nic3Name = "nic3";
        Creatable<NetworkInterface> networkInterface3Creatable = networkInterfaces
                .define(nic3Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.7");

        final String nic4Name = "nic4";
        Creatable<NetworkInterface> networkInterface4Creatable = networkInterfaces
                .define(nic4Name)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.8");

        CreatedResources<NetworkInterface> batchNics = networkInterfaces.create(networkInterface1Creatable,
                networkInterface2Creatable,
                networkInterface3Creatable,
                networkInterface4Creatable);

        Assert.assertTrue(batchNics.size() == 4);
        HashMap<String, Boolean> found = new LinkedHashMap<>();
        for (NetworkInterface nic : batchNics) {
            if (nic.name().equalsIgnoreCase(nic1Name)) {
                found.put(nic1Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic2Name)) {
                found.put(nic2Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic3Name)) {
                found.put(nic3Name, true);
            }
            if (nic.name().equalsIgnoreCase(nic4Name)) {
                found.put(nic4Name, true);
            }
        }
        Assert.assertTrue(found.size() == 4);
    }
}
