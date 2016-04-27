package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
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
import com.microsoft.azure.management.network.implementation.api.*;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
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
        group.setLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);
        StorageAccountCreateParametersInner parameters = new StorageAccountCreateParametersInner();
        parameters.setLocation(location);
        parameters.setAccountType(AccountType.STANDARD_LRS);
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
        request.setLocation(location);
        request.setOsProfile(new OSProfile());
        request.osProfile().setComputerName("javatest");
        request.osProfile().setAdminUsername("Foo12");
        request.osProfile().setAdminPassword("BaR@123" + rgName);
        request.setHardwareProfile(new HardwareProfile());
        request.hardwareProfile().setVmSize("Basic_A0");
        request.setStorageProfile(new StorageProfile());
        request.storageProfile().setImageReference(getVMImage("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"));
        request.storageProfile().setDataDisks(null);
        request.storageProfile().setOsDisk(new OSDisk());
        request.storageProfile().osDisk().setCaching(CachingTypes.NONE);
        request.storageProfile().osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        request.storageProfile().osDisk().setName("javatest");
        request.storageProfile().osDisk().setVhd(new VirtualHardDisk());
        request.storageProfile().osDisk().vhd().setUri("https://" + accountName + ".blob.core.windows.net/javacontainer/osjavawindows.vhd");
        request.setNetworkProfile(new NetworkProfile());
        request.networkProfile().setNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
        NetworkInterfaceReference nir = new NetworkInterfaceReference();
        nir.setPrimary(true);
        PublicIPAddressInner address = createPublicIP();
        nir.setId(createNIC(createVNET(), address != null ? address.ipAddress() : null).id());
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
        imageReference.setOffer(offer);
        imageReference.setPublisher(publisher);
        imageReference.setSku(sku);
        imageReference.setVersion(name);
        return imageReference;
    }

    private NetworkInterfaceInner createNIC(SubnetInner subnet, String publicIP) throws Exception {
        NetworkInterfaceInner nic = new NetworkInterfaceInner();
        nic.setLocation(location);
        nic.setIpConfigurations(new ArrayList<NetworkInterfaceIPConfiguration>());
        NetworkInterfaceIPConfiguration configuration = new NetworkInterfaceIPConfiguration();
        configuration.setName("javacrpip");
        configuration.setPrivateIPAllocationMethod("Dynamic");
        configuration.setSubnet(subnet);
        if (publicIP != null) {
            configuration.setPublicIPAddress(networkManagementClient.publicIPAddresses().get(rgName, publicIP, null).getBody());
        }
        nic.ipConfigurations().add(configuration);
        networkManagementClient.networkInterfaces().createOrUpdate(rgName, "javanic", nic);
        return networkManagementClient.networkInterfaces().get(rgName, "javanic", null).getBody();
    }

    private SubnetInner createVNET() throws Exception {
        VirtualNetworkInner vnet = new VirtualNetworkInner();
        vnet.setLocation(location);
        vnet.setAddressSpace(new AddressSpace());
        vnet.addressSpace().setAddressPrefixes(new ArrayList<String>());
        vnet.addressSpace().addressPrefixes().add("10.0.0.0/16");
        vnet.setDhcpOptions(new DhcpOptions());
        vnet.dhcpOptions().setDnsServers(new ArrayList<String>());
        vnet.dhcpOptions().dnsServers().add("10.1.1.1");
        vnet.dhcpOptions().dnsServers().add("10.1.2.4");
        vnet.setSubnets(new ArrayList<SubnetInner>());
        SubnetInner subnet = new SubnetInner();
        subnet.setName("javasn");
        subnet.setAddressPrefix("10.0.0.0/24");
        vnet.subnets().add(subnet);
        networkManagementClient.virtualNetworks().createOrUpdate(rgName, "javavn", vnet);
        return networkManagementClient.subnets().get(rgName, "javavn", "javasn", null).getBody();
    }

    private PublicIPAddressInner createPublicIP() throws Exception {
        PublicIPAddressInner publicIPAddress = new PublicIPAddressInner();
        publicIPAddress.setLocation(location);
        publicIPAddress.setPublicIPAllocationMethod("Dynamic");
        publicIPAddress.setDnsSettings(new PublicIPAddressDnsSettings());
        publicIPAddress.dnsSettings().setDomainNameLabel("javadn");

        networkManagementClient.publicIPAddresses().createOrUpdate(rgName, "javapip", publicIPAddress);
        return networkManagementClient.publicIPAddresses().get(rgName, "javapip", null).getBody();
    }
}
