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

import com.microsoft.azure.management.compute.models.ImageReference;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.network.models.*;
import com.microsoft.azure.management.storage.models.StorageAccount;

import java.util.HashMap;
import java.util.Random;

public class ResourceContext {
    private String Location;
    private String ResourceGroupName;

    private String SubscriptionId;
    private StorageAccount StorageAccount;
    private VirtualNetwork VirtualNetwork;
    private PublicIpAddress PublicIpAddress;
    private NetworkInterface NetworkInterface;
    private VirtualMachine VMInput;
    private String AvailabilitySetId;
    private String VirtualMachineSizeType;
    private ImageReference ImageReference;
    private HashMap<String, String> Tags;

    private boolean CreatePublicIpAddress;

    //resource names
    private String StorageAccountName;
    private String PublicIpName;
    private String SubnetName;
    private String NetworkInterfaceName;
    private String IpConfigName;
    private String ContainerName;
    private String VirtualNetworkName;
    private String AvailabilitySetName;

    public String getLocation() {
        return Location;
    }

    public String getResourceGroupName() {
        return ResourceGroupName;
    }

    public String getSubscriptionId() {
        return SubscriptionId;
    }

    public ResourceContext(
            String location, String resourceGroupName, String subscriptionId, boolean createPublicIpAddress) {
        this.Location = location;
        this.ResourceGroupName = resourceGroupName;
        this.SubscriptionId = subscriptionId;
        this.CreatePublicIpAddress = createPublicIpAddress;
    }

    public StorageAccount getStorageAccount() {
        return StorageAccount;
    }

    public void setStorageAccount(StorageAccount storageAccount) {
        StorageAccount = storageAccount;
    }

    public PublicIpAddress getPublicIpAddress() {
        return PublicIpAddress;
    }

    public void setPublicIpAddress(PublicIpAddress publicIpAddress) {
        PublicIpAddress = publicIpAddress;
    }

    public boolean isCreatePublicIpAddress() {
        return CreatePublicIpAddress;
    }

    public void setCreatePublicIpAddress(boolean createPublicIpAddress) {
        CreatePublicIpAddress = createPublicIpAddress;
    }

    public VirtualNetwork getVirtualNetwork() {
        return VirtualNetwork;
    }

    public void setVirtualNetwork(VirtualNetwork virtualNetwork) {
        VirtualNetwork = virtualNetwork;
    }

    public NetworkInterface getNetworkInterface() {
        return NetworkInterface;
    }

    public void setNetworkInterface(NetworkInterface networkInterface) {
        NetworkInterface = networkInterface;
    }

    public String getAvailabilitySetId() {
        return AvailabilitySetId;
    }

    public void setAvailabilitySetId(String availabilitySetId) {
        AvailabilitySetId = availabilitySetId;
    }

    public HashMap<String, String> getTags() {
        return Tags;
    }

    public void setTags(HashMap<String, String> tags) {
        Tags = tags;
    }

    public String getVirtualMachineSizeType() {
        return VirtualMachineSizeType;
    }

    public void setVirtualMachineSizeType(String virtualMachineSizeType) {
        VirtualMachineSizeType = virtualMachineSizeType;
    }

    public ImageReference getImageReference() {
        return ImageReference;
    }

    public void setImageReference(ImageReference imageReference) {
        ImageReference = imageReference;
    }

    public VirtualMachine getVMInput() {
        return VMInput;
    }

    public void setVMInput(VirtualMachine VMInput) {
        this.VMInput = VMInput;
    }

    public String getStorageAccountName() {
        if (StorageAccountName == null || StorageAccountName.isEmpty()) {
            StorageAccountName = generateName("javasto").toLowerCase();
        }
        return StorageAccountName;
    }

    public void setStorageAccountName(String storageAccountName) {
        StorageAccountName = storageAccountName;
    }

    public static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }

    public static String generateName(String prefix) {
        String name = prefix + randomString(5);
        return name;
    }

    public String getPublicIpName() {
        if (PublicIpName == null || PublicIpName.isEmpty()) {
            PublicIpName = generateName(ResourceGroupName + "publicip");
        }
        return PublicIpName;
    }

    public void setPublicIpName(String publicIpName) {
        PublicIpName = publicIpName;
    }

    public String getSubnetName() {
        if (SubnetName == null || SubnetName.isEmpty()) {
            SubnetName = generateName(ResourceGroupName + "subnet");
        }
        return SubnetName;
    }

    public void setSubnetName(String subnetName) {
        SubnetName = subnetName;
    }

    public String getNetworkInterfaceName() {
        if (NetworkInterfaceName == null || NetworkInterfaceName.isEmpty()) {
            NetworkInterfaceName = generateName(ResourceGroupName + "nic");
        }
        return NetworkInterfaceName;
    }

    public void setNetworkInterfaceName(String networkInterfaceName) {
        NetworkInterfaceName = networkInterfaceName;
    }

    public String getIpConfigName() {
        if (IpConfigName == null || IpConfigName.isEmpty()) {
            IpConfigName = generateName(ResourceGroupName + "ipconfig");
        }
        return IpConfigName;
    }

    public void setIpConfigName(String ipConfigName) {
        IpConfigName = ipConfigName;
    }

    public String getContainerName() {
        if (ContainerName == null || ContainerName.isEmpty()) {
            ContainerName = generateName(ResourceGroupName.toLowerCase() + "container");
        }
        return ContainerName;
    }

    public void setContainerName(String containerName) {
        ContainerName = containerName;
    }

    public String getVirtualNetworkName() {
        if (VirtualNetworkName == null || VirtualNetworkName.isEmpty()) {
            VirtualNetworkName = generateName(ResourceGroupName + "vnet");
        }
        return VirtualNetworkName;
    }

    public void setVirtualNetworkName(String virtualNetworkName) {
        VirtualNetworkName = virtualNetworkName;
    }

    public String getAvailabilitySetName() {
        if (AvailabilitySetName == null || AvailabilitySetName.isEmpty()) {
            AvailabilitySetName = generateName(ResourceGroupName + "availabilityset");
        }
        return AvailabilitySetName;
    }

    public void setAvailabilitySetName(String availabilitySetName) {
        AvailabilitySetName = availabilitySetName;
    }
}
