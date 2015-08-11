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
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ComputeHelper {
    public final static String Subscriptions = "subscriptions";
    public final static String ResourceGroups = "resourceGroups";
    public final static String Providers = "providers";
    public final static String AvailabilitySets = "availabilitySets";
    public final static String ResourceProviderNamespace = "Microsoft.Compute";
    public final static String VirtualMachines = "virtualMachines";

    private static String getEntityReferenceId(
            String subId, String resourceGrpName, String controllerName, String entityName)
    {
        return String.format("/%s/%s/%s/%s/%s/%s/%s/%s",
                Subscriptions, subId, ResourceGroups, resourceGrpName,
                Providers, ResourceProviderNamespace, controllerName,
                entityName);
    }

    public static String getAvailabilitySetRef(String subId, String resourceGrpName, String availabilitySetName) {
        return getEntityReferenceId(subId, resourceGrpName, AvailabilitySets, availabilitySetName);
    }

    public static String getVMReferenceId(String subId, String rgName, String vmName) {
        return getEntityReferenceId(subId, rgName, VirtualMachines, vmName);
    }

    public static void createOrUpdateResourceGroup(
            ResourceManagementClient resourceManagementClient, ResourceContext context)
            throws ServiceException, IOException, URISyntaxException {
        //make sure the rg exist
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(context.getResourceGroupName(),
                new ResourceGroup(context.getLocation()));
    }

    /***
     * Create a new availability set from given resource context.
     *
     * @param computeManagementClient
     * @param context
     * @return created availabilitySet Id
     * @throws Exception
     */
    public static String createAvailabilitySet(
            ComputeManagementClient computeManagementClient, ResourceContext context)
            throws Exception {
        String asName = context.getAvailabilitySetName();
        AvailabilitySet as = new AvailabilitySet(context.getLocation());
        as.setName(asName);

        if (context.getTags() != null)
            as.setTags(context.getTags());

        // create availability set
        return createAvailabilitySet(computeManagementClient, as, context);
    }

    /***
     * Create a new availability set from given resource context and availability Set.
     *
     * @param computeManagementClient
     * @param context
     * @return created availabilitySet Id
     * @throws Exception
     */
    public static String createAvailabilitySet(
            ComputeManagementClient computeManagementClient, AvailabilitySet avSet, ResourceContext context)
            throws Exception {
        computeManagementClient.getAvailabilitySetsOperations()
                .createOrUpdate(context.getResourceGroupName(), avSet);

        String assetId = getAvailabilitySetRef(
                context.getSubscriptionId(), context.getResourceGroupName(), avSet.getName());

        context.setAvailabilitySetId(assetId);
        return assetId;
    }

    /***
     * Create the model for a VirtualMachine with required parameters.
     * Default OS: Latest WindowsServer 2008-R2-SP1
     * Use context to specify custom settings
     *
     * @param context
     * @param vmName
     * @param adminName
     * @param adminPassword
     * @return VirtualMachine model to be sent to computeManagementClient
     */
    public static VirtualMachine createDefaultVMInput(
            ResourceContext context, String vmName, String adminName, String adminPassword) {

        String vhdContainer = getVhdContainerUrl(context);
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

        if (context.getImageReference() != null) {
            sto.setImageReference(context.getImageReference());
        } else {
            //TODO replace with getWindowsServerDefaultImage()
            ImageReference ir = new ImageReference();
            ir.setPublisher("MicrosoftWindowsServer");
            ir.setOffer("WindowsServer");
            ir.setSku("2008-R2-SP1");
            ir.setVersion("latest");
            sto.setImageReference(ir);
        }

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
     * @return
     * @throws Exception
     */
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
     * Use vmInputModifier to specify custom settings.
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
            ConsumerWrapper<VirtualMachine> vmInputModifier)
            throws Exception {
        //ensure resource group exists
        createOrUpdateResourceGroup(resourceManagementClient, context);

        if (context.getStorageAccount() == null) {
            StorageHelper.createStorageAccount(storageManagementClient, context);
        }

        if (context.getNetworkInterface() == null) {

            if (context.isCreatePublicIpAddress() && context.getPublicIpAddress() == null) {
                NetworkHelper.createPublicIpAddress(networkResourceProviderClient, context);
            }

            if (context.getVirtualNetwork() == null) {
                NetworkHelper.createVirtualNetwork(
                        networkResourceProviderClient, context);
            }

            NetworkHelper.createNIC(
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

    public static ArrayList<VirtualMachineImageResource> queryVMImage(
            ComputeManagementClient computeManagementClient, String location, String publisher, String offer,
            String sku, String filterExpression)
            throws ServiceException, IOException, URISyntaxException {
        VirtualMachineImageListParameters param = new VirtualMachineImageListParameters();
        param.setLocation(location);
        param.setPublisherName(publisher);
        param.setOffer(offer);
        param.setSkus(sku);
        param.setFilterExpression(filterExpression);
        VirtualMachineImageResourceList images = computeManagementClient.getVirtualMachineImagesOperations()
                .list(param);
        return images.getResources();
    }

    public static ImageReference getDefaultVMImage(
            ComputeManagementClient computeManagementClient, String location, String publisher,
            String offer, String sku)
            throws IOException, ServiceException, URISyntaxException {
        ArrayList<VirtualMachineImageResource> queryResult = queryVMImage(
                computeManagementClient, location, publisher, offer, sku, "$top=1");
        if (queryResult.size() < 1) {
            throw new IllegalArgumentException(
                    String.format("no image found for %s, %s, %s, %s", location, publisher, offer, sku));
        }

        VirtualMachineImageResource image = queryResult.get(0);
        ImageReference defaultImage = new ImageReference();
        defaultImage.setOffer(offer);
        defaultImage.setPublisher(publisher);
        defaultImage.setSku(sku);
        defaultImage.setVersion(image.getName());

        return defaultImage;
    }

    public static ImageReference getWindowsServerDefaultImage(
            ComputeManagementClient computeManagementClient, String location)
            throws ServiceException, IOException, URISyntaxException {
        return getDefaultVMImage(
                computeManagementClient, location, "MicrosoftWindowsServer", "WindowsServer", "2012-R2-Datacenter");
    }

    public static ImageReference getUbuntuServerDefaultImage(
            ComputeManagementClient computeManagementClient, String location)
            throws ServiceException, IOException, URISyntaxException {
        // GET https://management.azure.com/subscriptions/<subId>/providers/Microsoft.Compute/locations/SoutheastAsia/publishers/Canonical/artifacttypes/vmimage/offers/UbuntuServer/skus?api-version=2015-06-15
        return getDefaultVMImage(
                computeManagementClient, location, "Canonical", "UbuntuServer", "15.04");
    }

    public static VirtualMachineImage getMarketplaceVMImage(
            ComputeManagementClient computeManagementClient, String location, String publisher,
            String offer, String sku)
            throws ServiceException, IOException, URISyntaxException {
        ImageReference imageRef = getDefaultVMImage(computeManagementClient, location, publisher, offer, sku);
        VirtualMachineImageGetParameters param = new VirtualMachineImageGetParameters();
        param.setLocation(location);
        param.setPublisherName(publisher);
        param.setOffer(offer);
        param.setSkus(sku);
        param.setVersion(imageRef.getVersion());

        return computeManagementClient.getVirtualMachineImagesOperations().get(param).getVirtualMachineImage();
    }

    public static String getVhdContainerUrl(ResourceContext context) {
        return String.format("https://%s.blob.core.windows.net/%s",
                context.getStorageAccount().getName(), context.getContainerName());
    }

    public static ComputeLongRunningOperationResponse waitForVMCreation(
            ComputeManagementClient computeManagementClient, VirtualMachineCreateOrUpdateResponse vmCreationResponse)
            throws IOException, ServiceException {
        //wait for the vm creation
        return computeManagementClient.getLongRunningOperationStatus(
                vmCreationResponse.getAzureAsyncOperation());
    }

    private static void assertTrue(boolean condition) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }
}
