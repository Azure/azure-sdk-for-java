package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.implementation.api.HardwareProfile;
import com.microsoft.azure.management.compute.implementation.api.ImageReference;
import com.microsoft.azure.management.compute.implementation.api.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.implementation.api.NetworkProfile;
import com.microsoft.azure.management.compute.implementation.api.OSDisk;
import com.microsoft.azure.management.compute.implementation.api.OSProfile;
import com.microsoft.azure.management.compute.implementation.api.StorageProfile;
import com.microsoft.azure.management.compute.implementation.api.VirtualHardDisk;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.network.implementation.api.AddressSpace;
import com.microsoft.azure.management.network.implementation.api.DhcpOptions;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceInner;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.network.implementation.api.VirtualNetworkInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import com.microsoft.azure.management.storage.implementation.api.Sku;
import com.microsoft.azure.management.storage.implementation.api.SkuName;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountCreateParametersInner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualMachineOperationsTests extends ComputeManagementTestBase {
    private static String rgName = "javacsmrg";
    private static String location = "southcentralus";
    private static String accountName = "javasto";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
        ResourceGroupInner group = new ResourceGroupInner();
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        StorageAccountCreateParametersInner parameters = new StorageAccountCreateParametersInner();
        parameters.withLocation(location);
        parameters.withSku(new Sku().withName(SkuName.STANDARD_LRS));
        storageManagementClient.storageAccounts().create(rgName, accountName, parameters).getBody();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManagementClient.resourceGroups().delete(rgName);
    }

    @Test
    public void canCreateVirtualMachine() throws Exception {
        // Create
        String vmName = "javavm";
        VirtualMachineInner request = new VirtualMachineInner();
        request.withLocation(location);
        request.withOsProfile(new OSProfile());
        request.osProfile().withComputerName("javatest");
        request.osProfile().withAdminUsername("Foo12");
        request.osProfile().withAdminPassword("BaR@123" + rgName);
        request.withHardwareProfile(new HardwareProfile());
        request.hardwareProfile().withVmSize("Basic_A0");
        request.withStorageProfile(new StorageProfile());
        request.storageProfile().withImageReference(getVMImage("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"));
        request.storageProfile().withDataDisks(null);
        request.storageProfile().withOsDisk(new OSDisk());
        request.storageProfile().osDisk().withCaching(CachingTypes.NONE);
        request.storageProfile().osDisk().withCreateOption(DiskCreateOptionTypes.FROM_IMAGE);
        request.storageProfile().osDisk().withName("javatest");
        request.storageProfile().osDisk().withVhd(new VirtualHardDisk());
        request.storageProfile().osDisk().vhd().withUri("https://" + accountName + ".blob.core.windows.net/javacontainer/osjavawindows.vhd");
        request.withNetworkProfile(new NetworkProfile());
        request.networkProfile().withNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
        NetworkInterfaceReference nir = new NetworkInterfaceReference();
        nir.withPrimary(true);
        PublicIPAddressInner address = createPublicIP();
        nir.withId(createNIC(createVNET(), address != null ? address.ipAddress() : null).id());
        request.networkProfile().networkInterfaces().add(nir);
        VirtualMachineInner result = computeManagementClient.virtualMachines().createOrUpdate(rgName, vmName, request).getBody();
        Assert.assertNotNull(result);
        Assert.assertEquals(location, result.location());
        // List
        List<VirtualMachineInner> listResult = computeManagementClient.virtualMachines().list(rgName).getBody();
        VirtualMachineInner listVM = null;
        for (VirtualMachineInner vm : listResult) {
            if (vm.name().equals(vmName)) {
                listVM = vm;
                break;
            }
        }
        Assert.assertNotNull(listVM);
        Assert.assertEquals(location, listVM.location());
        // Get
        VirtualMachineInner getResult = computeManagementClient.virtualMachines().get(rgName, vmName, null).getBody();
        Assert.assertNotNull(getResult);
        Assert.assertEquals(location, getResult.location());
        // Delete
        computeManagementClient.virtualMachines().delete(rgName, vmName);
    }

    private ImageReference getVMImage(String publisher, String offer, String sku) throws CloudException, IOException {
        String name = computeManagementClient.virtualMachineImages().list(location, publisher, offer, sku, null, 1, null).getBody().get(0).name();
        ImageReference imageReference = new ImageReference();
        imageReference.withOffer(offer);
        imageReference.withPublisher(publisher);
        imageReference.withSku(sku);
        imageReference.withVersion(name);
        return imageReference;
    }

    private NetworkInterfaceInner createNIC(SubnetInner subnet, String publicIP) throws Exception {
        NetworkInterfaceInner nic = new NetworkInterfaceInner();
        nic.withLocation(location);
        nic.withIpConfigurations(new ArrayList<NetworkInterfaceIPConfiguration>());
        NetworkInterfaceIPConfiguration configuration = new NetworkInterfaceIPConfiguration();
        configuration.withName("javacrpip");
        configuration.withPrivateIPAllocationMethod("Dynamic");
        configuration.withSubnet(subnet);
        if (publicIP != null) {
            SubResource subResource = new SubResource();
            subResource.withId(networkManagementClient.publicIPAddresses().get(rgName, publicIP, null).getBody().id());
            configuration.withPublicIPAddress(subResource);
        }
        nic.ipConfigurations().add(configuration);
        networkManagementClient.networkInterfaces().createOrUpdate(rgName, "javanic", nic);
        return networkManagementClient.networkInterfaces().get(rgName, "javanic", null).getBody();
    }

    private SubnetInner createVNET() throws Exception {
        VirtualNetworkInner vnet = new VirtualNetworkInner();
        vnet.withLocation(location);
        vnet.withAddressSpace(new AddressSpace());
        vnet.addressSpace().withAddressPrefixes(new ArrayList<String>());
        vnet.addressSpace().addressPrefixes().add("10.0.0.0/16");
        vnet.withDhcpOptions(new DhcpOptions());
        vnet.dhcpOptions().withDnsServers(new ArrayList<String>());
        vnet.dhcpOptions().dnsServers().add("10.1.1.1");
        vnet.dhcpOptions().dnsServers().add("10.1.2.4");
        vnet.withSubnets(new ArrayList<SubnetInner>());
        SubnetInner subnet = new SubnetInner();
        subnet.withName("javasn");
        subnet.withAddressPrefix("10.0.0.0/24");
        vnet.subnets().add(subnet);
        networkManagementClient.virtualNetworks().createOrUpdate(rgName, "javavn", vnet);
        return networkManagementClient.subnets().get(rgName, "javavn", "javasn", null).getBody();
    }

    private PublicIPAddressInner createPublicIP() throws Exception {
        PublicIPAddressInner publicIPAddress = new PublicIPAddressInner();
        publicIPAddress.withLocation(location);
        publicIPAddress.withPublicIPAllocationMethod("Dynamic");
        publicIPAddress.withDnsSettings(new PublicIPAddressDnsSettings());
        publicIPAddress.dnsSettings().withDomainNameLabel("javadn");

        networkManagementClient.publicIPAddresses().createOrUpdate(rgName, "javapip", publicIPAddress);
        return networkManagementClient.publicIPAddresses().get(rgName, "javapip", null).getBody();
    }
}
