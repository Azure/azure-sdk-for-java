/**
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility;

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.models.*;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VMHelper {


    public static void createOrUpdateResourceGroup(
            ResourceManagementClient resourceManagementClient, ResourceContext context)
            throws ServiceException, IOException, URISyntaxException {
        //make sure the rg exist
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(context.getResourceGroupName(),
                new ResourceGroup(context.getLocation()));
    }

    public static StorageAccount createStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context)
            throws Exception {
        //create storage account
        StorageAccountCreateParameters stoInput = new StorageAccountCreateParameters(AccountType.STANDARDGRS,
                context.getLocation());
        return createStorageAccount(storageManagementClient, context, stoInput);
    }

    public static StorageAccount createStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context,
            StorageAccountCreateParameters stoInput) throws Exception {
        String storageAccountName = context.getStorageAccountName();
        StorageAccount storageAccount = storageManagementClient.getStorageAccountsOperations()
                .create(context.getResourceGroupName(), storageAccountName, stoInput)
                .getStorageAccount();

        //wait for the creation of storage account
        boolean created = false;
        while(!created) {
            waitSeconds(3);
            List<StorageAccount> storageAccountList = storageManagementClient.getStorageAccountsOperations()
                    .listByResourceGroup(context.getResourceGroupName()).getStorageAccounts();
            for (StorageAccount account : storageAccountList) {
                if (account.getName().equalsIgnoreCase(storageAccountName)) {
                    created = true;
                    break;
                }
            }

            //follow overwrite from .net tests
            storageAccount.setName(storageAccountName);
        }

        context.setStorageAccount(storageAccount);
        return storageAccount;
    }

    public static PublicIpAddress createPublicIpAddress(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context)
            throws Exception {
        PublicIpAddress publicIpParams = new PublicIpAddress(context.getLocation(), IpAllocationMethod.DYNAMIC);
        String publicIpName = context.getPublicIpName();

        AzureAsyncOperationResponse response = networkResourceProviderClient.getPublicIpAddressesOperations()
                .createOrUpdate(context.getResourceGroupName(), publicIpName, publicIpParams);

        PublicIpAddress ip = networkResourceProviderClient.getPublicIpAddressesOperations()
                .get(context.getResourceGroupName(), publicIpName).getPublicIpAddress();
        context.setPublicIpAddress(ip);
        return ip;
    }

    public static VirtualNetwork createVirtualNetwork(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context)
            throws Exception {
        VirtualNetwork vnet = new VirtualNetwork(context.getLocation());
        String subnetName = context.getSubnetName();
        String vnetName = context.getVirtualNetworkName();

        // set AddressSpace
        AddressSpace asp = new AddressSpace();
        ArrayList<String> addrPrefixes = new ArrayList<String>(1);
        addrPrefixes.add("10.0.0.0/16");
        asp.setAddressPrefixes(addrPrefixes);
        vnet.setAddressSpace(asp);

        // set DhcpOptions
        DhcpOptions dop = new DhcpOptions();
        ArrayList<String> dnsServers = new ArrayList<String>(2);
        dnsServers.add("10.1.1.1");
        dop.setDnsServers(dnsServers);
        vnet.setDhcpOptions(dop);

        // set subNet
        Subnet subnet = new Subnet("10.0.0.0/24");
        subnet.setName(subnetName);
        ArrayList<Subnet> subNets = new ArrayList<Subnet>(1);
        subNets.add(subnet);
        vnet.setSubnets(subNets);

        // send request
        AzureAsyncOperationResponse response = networkResourceProviderClient.getVirtualNetworksOperations()
                .createOrUpdate(context.getResourceGroupName(), vnetName, vnet);

        VirtualNetwork createdVnet = networkResourceProviderClient.getVirtualNetworksOperations()
                .get(context.getResourceGroupName(), vnetName)
                .getVirtualNetwork();

        context.setVirtualNetwork(createdVnet);
        return createdVnet;
    }

    public static NetworkInterface createNIC(
            NetworkResourceProviderClient networkResourceProviderClient, ResourceContext context, Subnet subNet)
            throws Exception {
        NetworkInterface nic = new NetworkInterface(context.getLocation());
        String nicName = context.getNetworkInterfaceName();
        String ipConfigName = context.getIpConfigName();
        nic.setName(nicName);

        //set tags
        if (context.getTags() != null) {
            nic.setTags(context.getTags());
        }

        //set ipconfiguration
        NetworkInterfaceIpConfiguration nicConfig = new NetworkInterfaceIpConfiguration();
        nicConfig.setName(ipConfigName);
        nicConfig.setPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        nicConfig.setSubnet(subNet);
        ArrayList<NetworkInterfaceIpConfiguration> ipConfigs = new ArrayList<NetworkInterfaceIpConfiguration>(1);
        ipConfigs.add(nicConfig);
        nic.setIpConfigurations(ipConfigs);

        if (context.getPublicIpAddress() != null) {
            ResourceId publicIpAddressId = new ResourceId();
            publicIpAddressId.setId(context.getPublicIpAddress().getId());
            nic.getIpConfigurations().get(0).setPublicIpAddress(publicIpAddressId);
        }

        // send request
        AzureAsyncOperationResponse response = networkResourceProviderClient.getNetworkInterfacesOperations()
                .createOrUpdate(context.getResourceGroupName(), nicName, nic);

        NetworkInterface createdNic = networkResourceProviderClient.getNetworkInterfacesOperations()
                .get(context.getResourceGroupName(), nicName)
                .getNetworkInterface();
        context.setNetworkInterface(createdNic);
        return createdNic;
    }

    public static String createAvailabilitySet(
            ComputeManagementClient computeManagementClient, ResourceContext context)
            throws Exception {
        String asName = context.getAvailabilitySetName();
        AvailabilitySet as = new AvailabilitySet(context.getLocation());
        as.setName(asName);

        if (context.getTags() != null)
            as.setTags(context.getTags());

        // create availability set
        AvailabilitySetCreateOrUpdateResponse response = computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), as);
        assertTrue(response.getStatusCode() == HttpStatus.SC_OK);
        String asetId = ComputeHelper.getAvailabilitySetRef(
                context.getSubscriptionId(), context.getResourceGroupName(), asName);

        context.setAvailabilitySetId(asetId);
        return asetId;
    }

    public static VirtualMachine createDefaultVMInput(
            ResourceContext context, String vmName, String adminName, String adminPassword) {

        String vhdContainer = "https://" + context.getStorageAccount().getName() + ".blob.core.windows.net/"
                + context.getContainerName();
        // String vhdUri = vhdContainer + String.format("/%s.vhd", "datavhd");
        String osVhduri = vhdContainer + String.format("/os%s.vhd", "osvhd");

        VirtualMachine vm = new VirtualMachine(context.getLocation());
        vm.setName(vmName);

        //set tags
        if (context.getTags() != null)
        vm.setTags(context.getTags());

        vm.setType("Microsoft.Compute/virtualMachines");

        //set availability set
        AvailabilitySetReference asRef = new AvailabilitySetReference();
        asRef.setReferenceUri(context.getAvailabilitySetId());
        vm.setAvailabilitySetReference(asRef);

        //set hardware profile
        HardwareProfile hwProfile = new HardwareProfile();
        if (context.getVirtualMachineSizeType() != null && !context.getVirtualMachineSizeType().isEmpty()) {
            hwProfile.setVirtualMachineSize(context.getVirtualMachineSizeType());
        } else {
            hwProfile.setVirtualMachineSize(VirtualMachineSizeTypes.STANDARD_A0);
        }

        vm.setHardwareProfile(hwProfile);

        //set storage profile
        StorageProfile sto = new StorageProfile();
        //setSourceImage api gone
//        if (context.getSourceImageReferenceUri() != null && !context.getSourceImageReferenceUri().isEmpty()) {
//            SourceImageReference sir = new SourceImageReference();
//            sir.setReferenceUri(context.getSourceImageReferenceUri());
//            sto.getImageReference().setSourceImage(sir);
//        } else {
//            ImageReference ir = new ImageReference();
//            ir.setPublisher("MicrosoftWindowsServer");
//            ir.setOffer("WindowsServer");
//            ir.setSku("2008-R2-SP1");
//            ir.setVersion("latest");
//            sto.setImageReference(ir);
//        }

        ImageReference ir = new ImageReference();
        ir.setPublisher("MicrosoftWindowsServer");
        ir.setOffer("WindowsServer");
        ir.setSku("2008-R2-SP1");
        ir.setVersion("latest");
        sto.setImageReference(ir);


        VirtualHardDisk vhardDisk = new VirtualHardDisk();
        vhardDisk.setUri(osVhduri);
        OSDisk osDisk = new OSDisk("osdisk", vhardDisk, DiskCreateOptionTypes.FROMIMAGE);
        osDisk.setCaching(CachingTypes.NONE);
        sto.setOSDisk(osDisk);
        vm.setStorageProfile(sto);

        //set network profile
        NetworkProfile networkProfile = new NetworkProfile();
        NetworkInterfaceReference nir = new NetworkInterfaceReference();
        nir.setReferenceUri(context.getNetworkInterface().getId());
        ArrayList<NetworkInterfaceReference> nirs = new ArrayList<NetworkInterfaceReference>(1);
        nirs.add(nir);
        networkProfile.setNetworkInterfaces(nirs);
        vm.setNetworkProfile(networkProfile);

        //set os profile
        OSProfile osProfile = new OSProfile();
        osProfile.setAdminPassword(adminPassword);
        osProfile.setAdminUsername(adminName);
        osProfile.setComputerName(vmName);
        vm.setOSProfile(osProfile);

        context.setVMInput(vm);
        return vm;
    }

    public static VirtualMachineCreateOrUpdateResponse createVM(
            ResourceManagementClient resourceManagementClient, ComputeManagementClient computeManagementClient,
            NetworkResourceProviderClient networkResourceProviderClient,
            StorageManagementClient storageManagementClient,
            ResourceContext context, String vmName, String adminName, String adminPassword)
            throws Exception {
            return createVM(
                    resourceManagementClient, computeManagementClient, networkResourceProviderClient,
                    storageManagementClient, context, vmName, adminName, adminPassword, null);
    }

    /**
     * This helper method will help you quickly create a VM.
     * For customization please set name or existing component like virtual network in the ResourceContext.
     *
     * @param resourceManagementClient
     * @param computeManagementClient
     * @param networkResourceProviderClient
     * @param storageManagementClient
     * @param context
     * @param vmName
     * @param adminName
     * @param adminPassword
     * @param vmInputModifier
     * @return
     * @throws Exception
     */
    public static VirtualMachineCreateOrUpdateResponse createVM(
            ResourceManagementClient resourceManagementClient, ComputeManagementClient computeManagementClient,
            NetworkResourceProviderClient networkResourceProviderClient,
            StorageManagementClient storageManagementClient,
            ResourceContext context, String vmName, String adminName, String adminPassword,
            Consumer<VirtualMachine> vmInputModifier)
            throws Exception {
        //ensure resource group exists
        createOrUpdateResourceGroup(resourceManagementClient, context);

        if (context.getStorageAccount() == null) {
            createStorageAccount(storageManagementClient, context);
        }

        if (context.getNetworkInterface() == null) {

            if (context.isCreatePublicIpAddress() && context.getPublicIpAddress() == null) {
                createPublicIpAddress(networkResourceProviderClient, context);
            }

            if (context.getVirtualNetwork() == null) {
                createVirtualNetwork(
                        networkResourceProviderClient, context);
            }

            createNIC(
                    networkResourceProviderClient, context, context.getVirtualNetwork().getSubnets().get(0));
        }

        if (context.getAvailabilitySetId() == null || context.getAvailabilitySetId().isEmpty()) {
            createAvailabilitySet(
                    computeManagementClient, context);
        }

        if (context.getVMInput() == null) {
            createDefaultVMInput(
                    context, vmName, adminName, adminPassword);
        }

        if (vmInputModifier != null) {
            vmInputModifier.accept(context.getVMInput());
        }

        VirtualMachineCreateOrUpdateResponse vmCreationResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginCreatingOrUpdating(context.getResourceGroupName(), context.getVMInput());

        assertTrue(HttpStatus.SC_CREATED == vmCreationResponse.getStatusCode());

        return vmCreationResponse;
    }

    public static ComputeLongRunningOperationResponse waitForVMCreation(
            ComputeManagementClient computeManagementClient, VirtualMachineCreateOrUpdateResponse vmCreationResponse)
            throws IOException, ServiceException {
        //wait for the vm creation
        return computeManagementClient.getLongRunningOperationStatus(
                vmCreationResponse.getAzureAsyncOperation());
    }

    protected static void waitSeconds(double seconds) throws InterruptedException{
        Thread.sleep((long)seconds * 100);
    }

    public static void assertTrue(boolean condition) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }
}
