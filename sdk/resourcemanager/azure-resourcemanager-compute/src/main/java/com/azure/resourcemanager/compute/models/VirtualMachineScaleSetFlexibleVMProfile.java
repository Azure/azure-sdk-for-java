// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.util.List;

/** Describes a virtual machine scale set virtual machine profile. */
public interface VirtualMachineScaleSetFlexibleVMProfile {

    interface UpdateAttachStages {

        interface Blank extends WithNetworkSubnet {}

        /**
         * The stage of a virtual machine scale set definition allowing to specify the virtual network subnet for the
         * primary network configuration.
         */
        interface WithNetworkSubnet {
            /**
             * Associate an existing virtual network subnet with the primary network interface of the virtual machines
             * in the scale set.
             *
             * @param network an existing virtual network
             * @param subnetName the subnet name
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancer withExistingPrimaryNetworkSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an Internet-facing load balancer for
         * the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancer {
            /**
             * Specifies an Internet-facing load balancer whose backends and/or NAT pools can be assigned to the primary
             * network interfaces of the virtual machines in the scale set.
             *
             * <p>By default, all the backends and inbound NAT pools of the load balancer will be associated with the
             * primary network interface of the scale set virtual machines.
             *
             * <p>
             *
             * @param loadBalancer an existing Internet-facing load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool withExistingPrimaryInternetFacingLoadBalancer(
                LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer should be associated with the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate a backend pool and/or an inbound
         * NAT pool of the selected Internet-facing load balancer with the primary network interface of the virtual
         * machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool
            extends WithPrimaryInternetFacingLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool withPrimaryInternetFacingLoadBalancerBackends(
                String... backendNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool extends WithPrimaryInternalLoadBalancer {
            /**
             * Associates the specified inbound NAT pools of the selected internal load balancer with the primary
             * network interface of the virtual machines in the scale set.
             *
             * @param natPoolNames inbound NAT pools names existing on the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer withPrimaryInternetFacingLoadBalancerInboundNatPools(
                String... natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an internal load balancer for the
         * primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancer {
            /**
             * Specifies the internal load balancer whose backends and/or NAT pools can be assigned to the primary
             * network interface of the virtual machines in the scale set.
             *
             * <p>By default all the backends and inbound NAT pools of the load balancer will be associated with the
             * primary network interface of the virtual machines in the scale set, unless subset of them is selected in
             * the next stages.
             *
             * <p>
             *
             * @param loadBalancer an existing internal load balancer
             * @return the next stage of the definition
             */
            WithInternalLoadBalancerBackendOrNatPool withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no internal load balancer should be associated with the primary network interfaces of the
             * virtual machines in the scale set.
             *
             * @return the next stage of the definition
             */
            WithOS withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate backend pools and/or inbound NAT
         * pools of the selected internal load balancer with the primary network interface of the virtual machines in
         * the scale set.
         */
        interface WithInternalLoadBalancerBackendOrNatPool extends WithInternalInternalLoadBalancerNatPool {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithInternalInternalLoadBalancerNatPool withPrimaryInternalLoadBalancerBackends(String... backendNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pools of the selected
         * internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithInternalInternalLoadBalancerNatPool extends WithOS {
            /**
             * Associate internal load balancer inbound NAT pools with the the primary network interface of the scale
             * set virtual machine.
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the definition
             */
            WithOS withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the operating system image. */
        interface WithOS {
            /**
             * Specifies a known marketplace Windows image used as the operating system for the virtual machines in the
             * scale set.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withPopularWindowsImage(
                KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of the specified marketplace Windows image should be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withLatestWindowsImage(
                String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, SKU and version of the marketplace image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a generalized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManaged withGeneralizedWindowsCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withSpecializedWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Windows image to be used as the operating system for the virtual machines in
             * the scale set.
             *
             * @param imageUrl the URL of the VHD
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameUnmanaged withStoredWindowsImage(String imageUrl);

            /**
             * Specifies a known marketplace Linux image used as the virtual machine's operating system.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image should be used.
             *
             * @param publisher the publisher of the image
             * @param offer the offer of the image
             * @param sku the SKU of the image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a market-place Linux image that should be used.
             *
             * @param imageReference describes the publisher, offer, SKU and version of the market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a generalized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManaged withGeneralizedLinuxCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged withSpecializedLinuxCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Linux image used as the virtual machine's operating system.
             *
             * @param imageUrl the URL the the VHD
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameUnmanaged withStoredLinuxImage(String imageUrl);
        }



        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManagedOrUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming conventions for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameUnmanaged {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyUnmanaged withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyUnmanaged {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged withSsh(String publicKey);
        }


        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachManaged extends WithManagedAttach {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachUnmanaged extends WithUnmanagedAttach {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachManagedOrUnmanaged extends WithManagedAttach {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged withSsh(String publicKey);

            /** @return the next stage of a unmanaged disk based virtual machine scale set definition */
            WithUnmanagedAttach withUnmanagedDisks();
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManagedOrUnmanaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManagedOrUnmanaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManagedOrUnmanaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachManagedOrUnmanaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachManagedOrUnmanaged extends WithWindowsAttachManaged {
            WithWindowsAttachUnmanaged withUnmanagedDisks();
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManaged {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManaged withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManaged {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachManaged withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachUnmanaged extends WithUnmanagedAttach {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachManaged extends WithManagedAttach {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged withWinRM(WinRMListener listener);
        }


        /** The stage of a virtual machine scale set definition allowing to specify managed data disks. */
        interface WithManagedDataDisk {
            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDisk(
                int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDiskFromImage(int imageLun);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach withNewDataDiskFromImage(
                int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType);
        }

        /** The optionals applicable only for managed disks. */
        interface WithManagedDiskOptionals {
            /**
             * Specifies the storage account type for managed OS disk.
             *
             * @param accountType the storage account type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach withOSDiskStorageAccountType(StorageAccountTypes accountType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param cachingType the caching type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach withDataDiskDefaultCachingType(CachingTypes cachingType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param storageAccountType the storage account type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);
        }

        /** The stage of the virtual machine scale set definition allowing to specify availability zone. */
        interface WithAvailabilityZone {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            WithManagedAttach withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow managed data disks specific settings to be specified.
         */
        interface WithManagedAttach
            extends WithManagedDataDisk, WithManagedDiskOptionals, WithAvailabilityZone, WithAttach {
        }

        /** The stage of the virtual machine scale set definition allowing to specify unmanaged data disk. */
        interface WithUnmanagedDataDisk {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow unmanaged data disks specific settings to be specified.
         */
        interface WithUnmanagedAttach extends WithUnmanagedDataDisk, WithAttach {
        }

        /** The stage of a virtual machine scale set definition allowing to specify the computer name prefix. */
        interface WithComputerNamePrefix {
            /**
             * Specifies the name prefix to use for auto-generating the names for the virtual machines in the scale set.
             *
             * @param namePrefix the prefix for the auto-generated names of the virtual machines in the scale set
             * @return the next stage of the definition
             */
            WithAttach withComputerNamePrefix(String namePrefix);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify number of virtual machines in the
         * scale set.
         */
        interface WithCapacity {
            /**
             * Specifies the maximum number of virtual machines in the scale set.
             *
             * @param capacity number of virtual machines
             * @return the next stage of the definition
             */
            WithAttach withCapacity(long capacity);
        }

        /** The stage of a virtual machine scale set definition allowing to specify OS disk configurations. */
        interface WithOSDiskSettings {
            /**
             * Specifies the caching type for the operating system disk.
             *
             * @param cachingType the caching type
             * @return the next stage of the definition
             */
            WithAttach withOSDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the name for the OS disk.
             *
             * @param name the OS disk name
             * @return the next stage of the definition
             */
            WithAttach withOSDiskName(String name);
        }

        /** The stage of a virtual machine scale set definition allowing to specify the storage account. */
        interface WithStorageAccount {
            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param name the name of the storage account
             * @return the next stage of the definition
             */
            WithAttach withNewStorageAccount(String name);

            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param creatable the storage account definition in a creatable stage
             * @return the next stage in the definition
             */
            WithAttach withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing storage account for the OS and data disk VHDs of the virtual machines in the scale
             * set.
             *
             * @param storageAccount an existing storage account
             * @return the next stage in the definition
             */
            WithAttach withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the custom data. */
        interface WithCustomData {
            /**
             * Specifies the custom data for the virtual machine scale set.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage in the definition
             */
            WithAttach withCustomData(String base64EncodedCustomData);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the secrets. */
        interface WithSecrets {
            /**
             * Specifies set of certificates that should be installed onto the virtual machine.
             *
             * @param secrets the secrets value to set
             * @return the next stage in the definition he secrets value to set
             */
            WithAttach withSecrets(List<VaultSecretGroup> secrets);
        }

//        /** The stage of a virtual machine scale set definition allowing to specify extensions. */
//        interface WithExtension {
//            /**
//             * Begins the definition of an extension reference to be attached to the virtual machines in the scale set.
//             *
//             * @param name the reference name for the extension
//             * @return the first stage of the extension reference definition
//             */
//            VirtualMachineScaleSetExtension.DefinitionStages.Blank<WithAttach> defineNewExtension(String name);
//        }

        /**
         * The stage of the virtual machine scale set definition allowing to enable System Assigned (Local) Managed
         * Service Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies that System Assigned (Local) Managed Service Identity needs to be enabled in the virtual
             * machine scale set.
             *
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach withSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled virtual machine scale set allowing
         * to set access for the identity.
         */
        interface WithSystemAssignedIdentityBasedAccessOrAttach extends WithAttach {
            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the given access
             * (described by the role) on an ARM resource identified by the resource ID. Applications running on the
             * scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's local identity should have the given access (described by the
             * role) on the resource group that virtual machine resides. Applications running on the scale set VM
             * instance will have the same permission (role) on the resource group.
             *
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on an ARM resource identified by the resource ID. Applications running
             * on the scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on the resource group that virtual machine resides. Applications
             * running on the scale set VM instance will have the same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                String roleDefinitionId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify User Assigned (External) Managed
         * Service Identities.
         */
        interface WithUserAssignedManagedServiceIdentity {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the virtual
             * machine scale set.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the virtual machine scale set definition
             */
            WithAttach withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the virtual machine scale set.
             *
             * @param identity the identity
             * @return the next stage of the virtual machine scale set definition
             */
            WithAttach withExistingUserAssignedManagedServiceIdentity(Identity identity);
        }

        /** The stage of the virtual machine scale set definition allowing to enable boot diagnostics. */
        interface WithBootDiagnostics {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             * Managed storage account is used.
             *
             * @return the next stage of the definition
             */
            WithAttach withBootDiagnosticsOnManagedStorageAccount();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach withBootDiagnostics(String storageAccountBlobEndpointUri);
        }

        /** The stage of the virtual machine definition allowing to specify billing profile. */
        interface WithBillingProfile {

            /**
             * Set the billing related details of the low priority virtual machines in the scale set. This price is in
             * US Dollars.
             *
             * @param maxPrice the maxPrice value to set
             * @return the next stage of the definition
             */
            WithAttach withMaxPrice(Double maxPrice);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify priority for vms in the scale-set.
         */
        interface WithVMPriority {
            /**
             * Specifies the priority of the virtual machines in the scale set.
             *
             * @param priority the priority
             * @return the next stage of the definition
             */
            WithAttach withVirtualMachinePriority(VirtualMachinePriorityTypes priority);

            /**
             * Specify that virtual machines in the scale set should be low priority VMs.
             *
             * @return the next stage of the definition
             */
            WithAttach withLowPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be low priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithAttach withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs.
             *
             * @return the next stage of the definition
             */
            WithAttach withSpotPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithAttach withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);
        }

        /** The stage of the virtual machine scale set definition allowing to enable public ip for vm instances. */
        interface WithVirtualMachinePublicIp {
            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @return the next stage of the definition
             */
            WithAttach withVirtualMachinePublicIp();

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param leafDomainLabel the domain name label
             * @return the next stage of the definition
             */
            WithAttach withVirtualMachinePublicIp(String leafDomainLabel);

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param ipConfig the public ip address configuration
             * @return the next stage of the definition
             */
            WithAttach withVirtualMachinePublicIp(VirtualMachineScaleSetPublicIpAddressConfiguration ipConfig);
        }

        /** The stage of the virtual machine scale set definition allowing to configure accelerated networking. */
        interface WithAcceleratedNetworking {
            /**
             * Specify that accelerated networking should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withAcceleratedNetworking();

            /**
             * Specify that accelerated networking should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withoutAcceleratedNetworking();
        }

        /** The stage of the virtual machine scale set definition allowing to configure ip forwarding. */
        interface WithIpForwarding {
            /**
             * Specify that ip forwarding should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withIpForwarding();

            /**
             * Specify that ip forwarding should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withoutIpForwarding();
        }

        /** The stage of the virtual machine scale set definition allowing to configure network security group. */
        interface WithNetworkSecurityGroup {
            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroup the network security group to associate
             * @return the next stage of the definition
             */
            WithAttach withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);

            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroupId the network security group to associate
             * @return the next stage of the definition
             */
            WithAttach withExistingNetworkSecurityGroupId(String networkSecurityGroupId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure single placement group. */
        interface WithSinglePlacementGroup {
            /**
             * Specify that single placement group should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withSinglePlacementGroup();

            /**
             * Specify that single placement group should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach withoutSinglePlacementGroup();
        }

        /** The stage of the virtual machine scale set definition allowing to configure application gateway. */
        interface WithApplicationGateway {
            /**
             * Specify that an application gateway backend pool should be associated with virtual machine scale set.
             *
             * @param backendPoolId an existing backend pool id of the gateway
             * @return the next stage of the definition
             */
            WithAttach withExistingApplicationGatewayBackendPool(String backendPoolId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure application security group. */
        interface WithApplicationSecurityGroup {
            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroup the application security group
             * @return the next stage of the definition
             */
            WithAttach withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup);

            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroupId the application security group id
             * @return the next stage of the definition
             */
            WithAttach withExistingApplicationSecurityGroupId(String applicationSecurityGroupId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to configure a purchase plan.
         */
        interface WithPlan {
            /**
             * Specifies the purchase plan for the virtual machine scale set.
             *
             * @param plan a purchase plan
             * @return the next stage of the definition
             */
            WithAttach withPlan(PurchasePlan plan);
        }

        /**
         * The virtual machine scale set stages shared between managed and unmanaged based virtual machine scale set
         * definitions.
         */
        interface DefinitionShared
            extends UpdateAttachStages.Blank,
            UpdateAttachStages.WithNetworkSubnet,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancer,
            UpdateAttachStages.WithPrimaryInternalLoadBalancer,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool,
            UpdateAttachStages.WithInternalLoadBalancerBackendOrNatPool,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancerNatPool,
            UpdateAttachStages.WithInternalInternalLoadBalancerNatPool,
            UpdateAttachStages.WithOS,
            UpdateAttachStages.WithAttach{
        }

        /** The entirety of the virtual machine scale set definition. */
        interface DefinitionManagedOrUnmanaged
            extends DefinitionShared,
            UpdateAttachStages.WithLinuxRootUsernameManagedOrUnmanaged,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged,
            UpdateAttachStages.WithWindowsAdminUsernameManagedOrUnmanaged,
            UpdateAttachStages.WithWindowsAdminPasswordManagedOrUnmanaged,
            UpdateAttachStages.WithLinuxAttachManagedOrUnmanaged,
            UpdateAttachStages.WithWindowsAttachManagedOrUnmanaged,
            UpdateAttachStages.WithManagedAttach,
            UpdateAttachStages.WithUnmanagedAttach {
        }

        /** The entirety of the managed disk based virtual machine scale set definition. */
        interface DefinitionManaged
            extends DefinitionShared,
            UpdateAttachStages.WithLinuxRootUsernameManaged,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyManaged,
            UpdateAttachStages.WithWindowsAdminUsernameManaged,
            UpdateAttachStages.WithWindowsAdminPasswordManaged,
            UpdateAttachStages.WithLinuxAttachManaged,
            UpdateAttachStages.WithWindowsAttachManaged,
            UpdateAttachStages.WithManagedAttach {
        }

        /** The entirety of the unmanaged disk based virtual machine scale set definition. */
        interface DefinitionUnmanaged
            extends DefinitionShared,
            UpdateAttachStages.WithLinuxRootUsernameUnmanaged,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyUnmanaged,
            UpdateAttachStages.WithWindowsAdminUsernameUnmanaged,
            UpdateAttachStages.WithWindowsAdminPasswordUnmanaged,
            UpdateAttachStages.WithLinuxAttachUnmanaged,
            UpdateAttachStages.WithWindowsAttachUnmanaged,
            UpdateAttachStages.WithUnmanagedAttach {
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource to be
         * created, but also allowing for any other optional settings to be specified.
         */
        interface WithAttach
            extends Attachable<VirtualMachineScaleSet.UpdateStages.WithApply>,
            WithOSDiskSettings,
            WithComputerNamePrefix,
            WithCapacity,
            WithStorageAccount,
            WithCustomData,
//            WithExtension,
            WithSystemAssignedManagedServiceIdentity,
            WithUserAssignedManagedServiceIdentity,
            WithBootDiagnostics,
            WithBillingProfile,
            WithVMPriority,
            WithVirtualMachinePublicIp,
            WithAcceleratedNetworking,
            WithIpForwarding,
            WithNetworkSecurityGroup,
            WithSinglePlacementGroup,
            WithApplicationGateway,
            WithApplicationSecurityGroup,
            WithSecrets,
            WithPlan,
            Resource.DefinitionWithTags<WithAttach> {
        }

    }

}
