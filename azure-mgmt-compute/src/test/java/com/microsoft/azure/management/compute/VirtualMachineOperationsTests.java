package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.models.HardwareProfile;
import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.NetworkProfile;
import com.microsoft.azure.management.compute.models.OSDisk;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.StorageProfile;
import com.microsoft.azure.management.compute.models.VirtualHardDisk;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.network.models.AddressSpace;
import com.microsoft.azure.management.network.models.DhcpOptions;
import com.microsoft.azure.management.network.models.NetworkInterface;
import com.microsoft.azure.management.network.models.NetworkInterfaceIPConfiguration;
import com.microsoft.azure.management.network.models.PublicIPAddress;
import com.microsoft.azure.management.network.models.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.models.Subnet;
import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
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
        ResourceGroup group = new ResourceGroup();
        group.setLocation(location);
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(rgName, group);
        StorageAccountCreateParameters parameters = new StorageAccountCreateParameters();
        parameters.setLocation(location);
        parameters.setAccountType(AccountType.STANDARD_LRS);
        storageManagementClient.getStorageAccountsOperations().create(rgName, accountName, parameters).getBody();

    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManagementClient.getResourceGroupsOperations().delete(rgName);
    }

    @Test
    public void canCreateVirtualMachine() throws Exception {
        // Create
        String vmName = "javavm";
        VirtualMachine request = new VirtualMachine();
        request.setLocation(location);
        request.setOsProfile(new OSProfile());
        request.getOsProfile().setComputerName("javatest");
        request.getOsProfile().setAdminUsername("Foo12");
        request.getOsProfile().setAdminPassword("BaR@123" + rgName);
        request.setHardwareProfile(new HardwareProfile());
        request.getHardwareProfile().setVmSize("Basic_A0");
        request.setStorageProfile(new StorageProfile());
        request.getStorageProfile().setImageReference(getVMImage("MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter"));
        request.getStorageProfile().setDataDisks(null);
        request.getStorageProfile().setOsDisk(new OSDisk());
        request.getStorageProfile().getOsDisk().setCaching("None");
        request.getStorageProfile().getOsDisk().setCreateOption("fromImage");
        request.getStorageProfile().getOsDisk().setName("javatest");
        request.getStorageProfile().getOsDisk().setVhd(new VirtualHardDisk());
        request.getStorageProfile().getOsDisk().getVhd().setUri("https://" + accountName + ".blob.core.windows.net/javacontainer/osjavawindows.vhd");
        request.setNetworkProfile(new NetworkProfile());
        request.getNetworkProfile().setNetworkInterfaces(new ArrayList<NetworkInterfaceReference>());
        NetworkInterfaceReference nir = new NetworkInterfaceReference();
        nir.setPrimary(true);
        PublicIPAddress address = createPublicIP();
        nir.setId(createNIC(createVNET(), address != null ? address.getIpAddress() : null).getId());
        request.getNetworkProfile().getNetworkInterfaces().add(nir);
        VirtualMachine result = computeManagementClient.getVirtualMachinesOperations().createOrUpdate(rgName, vmName, request).getBody();
        Assert.assertNotNull(result);
        Assert.assertEquals(location, result.getLocation());
        // List
        List<VirtualMachine> listResult = computeManagementClient.getVirtualMachinesOperations().list(rgName).getBody();
        VirtualMachine listVM = null;
        for (VirtualMachine vm : listResult) {
            if (vm.getName().equals(vmName)) {
                listVM = vm;
                break;
            }
        }
        Assert.assertNotNull(listVM);
        Assert.assertEquals(location, listVM.getLocation());
        // Get
        VirtualMachine getResult = computeManagementClient.getVirtualMachinesOperations().get(rgName, vmName, null).getBody();
        Assert.assertNotNull(getResult);
        Assert.assertEquals(location, getResult.getLocation());
        // Delete
        computeManagementClient.getVirtualMachinesOperations().delete(rgName, vmName);
    }

    private ImageReference getVMImage(String publisher, String offer, String sku) throws CloudException, IOException {
        String name = computeManagementClient.getVirtualMachineImagesOperations().list(location, publisher, offer, sku, null, 1, null).getBody().get(0).getName();
        ImageReference imageReference = new ImageReference();
        imageReference.setOffer(offer);
        imageReference.setPublisher(publisher);
        imageReference.setSku(sku);
        imageReference.setVersion(name);
        return imageReference;
    }

    private NetworkInterface createNIC(Subnet subnet, String publicIP) throws Exception {
        NetworkInterface nic = new NetworkInterface();
        nic.setLocation(location);
        nic.setIpConfigurations(new ArrayList<NetworkInterfaceIPConfiguration>());
        NetworkInterfaceIPConfiguration configuration = new NetworkInterfaceIPConfiguration();
        configuration.setName("javacrpip");
        configuration.setPrivateIPAllocationMethod("Dynamic");
        configuration.setSubnet(subnet);
        if (publicIP != null) {
            configuration.setPublicIPAddress(new PublicIPAddress());
            configuration.getPublicIPAddress().setId(publicIP);
        }
        nic.getIpConfigurations().add(configuration);
        networkManagementClient.getNetworkInterfacesOperations().createOrUpdate(rgName, "javanic", nic);
        return networkManagementClient.getNetworkInterfacesOperations().get(rgName, "javanic", null).getBody();
    }

    private Subnet createVNET() throws Exception {
        VirtualNetwork vnet = new VirtualNetwork();
        vnet.setLocation(location);
        vnet.setAddressSpace(new AddressSpace());
        vnet.getAddressSpace().setAddressPrefixes(new ArrayList<String>());
        vnet.getAddressSpace().getAddressPrefixes().add("10.0.0.0/16");
        vnet.setDhcpOptions(new DhcpOptions());
        vnet.getDhcpOptions().setDnsServers(new ArrayList<String>());
        vnet.getDhcpOptions().getDnsServers().add("10.1.1.1");
        vnet.getDhcpOptions().getDnsServers().add("10.1.2.4");
        vnet.setSubnets(new ArrayList<Subnet>());
        Subnet subnet = new Subnet();
        subnet.setName("javasn");
        subnet.setAddressPrefix("10.0.0.0/24");
        vnet.getSubnets().add(subnet);
        networkManagementClient.getVirtualNetworksOperations().createOrUpdate(rgName, "javavn", vnet);
        return networkManagementClient.getSubnetsOperations().get(rgName, "javavn", "javasn", null).getBody();
    }

    private PublicIPAddress createPublicIP() throws Exception {
        PublicIPAddress publicIPAddress = new PublicIPAddress();
        publicIPAddress.setLocation(location);
        publicIPAddress.setPublicIPAllocationMethod("Dynamic");
        publicIPAddress.setDnsSettings(new PublicIPAddressDnsSettings());
        publicIPAddress.getDnsSettings().setDomainNameLabel("javadn");

        networkManagementClient.getPublicIPAddressesOperations().createOrUpdate(rgName, "javapip", publicIPAddress);
        return networkManagementClient.getPublicIPAddressesOperations().get(rgName, "javapip", null).getBody();
    }
}
