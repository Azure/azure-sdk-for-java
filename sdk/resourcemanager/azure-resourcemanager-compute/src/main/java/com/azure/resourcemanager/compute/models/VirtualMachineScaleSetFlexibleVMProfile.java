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
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.util.List;

/** Describes a virtual machine scale set virtual machine profile. */
public interface VirtualMachineScaleSetFlexibleVMProfile {

    interface UpdateAttachStages {

        interface Blank<ParentT> extends WithNetworkSubnet<ParentT> {}

        /**
         * The stage of a virtual machine scale set definition allowing to specify the virtual network subnet for the
         * primary network configuration.
         */
        interface WithNetworkSubnet<ParentT> {
            /**
             * Associate an existing virtual network subnet with the primary network interface of the virtual machines
             * in the scale set.
             *
             * @param network an existing virtual network
             * @param subnetName the subnet name
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancer<ParentT> withExistingPrimaryNetworkSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an Internet-facing load balancer for
         * the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancer<ParentT> {
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
            WithPrimaryInternetFacingLoadBalancerBackendOrNatPool<ParentT> withExistingPrimaryInternetFacingLoadBalancer(
                LoadBalancer loadBalancer);

            /**
             * Specifies that no public load balancer should be associated with the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer<ParentT> withoutPrimaryInternetFacingLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate a backend pool and/or an inbound
         * NAT pool of the selected Internet-facing load balancer with the primary network interface of the virtual
         * machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerBackendOrNatPool<ParentT>
            extends WithPrimaryInternetFacingLoadBalancerNatPool<ParentT> {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames the names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternetFacingLoadBalancerNatPool<ParentT> withPrimaryInternetFacingLoadBalancerBackends(
                String... backendNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate an inbound NAT pool of the selected
         * Internet-facing load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternetFacingLoadBalancerNatPool<ParentT> extends WithPrimaryInternalLoadBalancer<ParentT> {
            /**
             * Associates the specified inbound NAT pools of the selected internal load balancer with the primary
             * network interface of the virtual machines in the scale set.
             *
             * @param natPoolNames inbound NAT pools names existing on the selected load balancer
             * @return the next stage of the definition
             */
            WithPrimaryInternalLoadBalancer<ParentT> withPrimaryInternetFacingLoadBalancerInboundNatPools(
                String... natPoolNames);
        }

        /**
         * The stage of a virtual machine scale set definition allowing to specify an internal load balancer for the
         * primary network interface of the virtual machines in the scale set.
         */
        interface WithPrimaryInternalLoadBalancer<ParentT> {
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
            WithInternalLoadBalancerBackendOrNatPool<ParentT> withExistingPrimaryInternalLoadBalancer(LoadBalancer loadBalancer);

            /**
             * Specifies that no internal load balancer should be associated with the primary network interfaces of the
             * virtual machines in the scale set.
             *
             * @return the next stage of the definition
             */
            WithOS<ParentT> withoutPrimaryInternalLoadBalancer();
        }

        /**
         * The stage of a virtual machine scale set definition allowing to associate backend pools and/or inbound NAT
         * pools of the selected internal load balancer with the primary network interface of the virtual machines in
         * the scale set.
         */
        interface WithInternalLoadBalancerBackendOrNatPool<ParentT> extends WithInternalInternalLoadBalancerNatPool<ParentT> {
            /**
             * Associates the specified backends of the selected load balancer with the primary network interface of the
             * virtual machines in the scale set.
             *
             * @param backendNames names of existing backends in the selected load balancer
             * @return the next stage of the definition
             */
            WithInternalInternalLoadBalancerNatPool<ParentT> withPrimaryInternalLoadBalancerBackends(String... backendNames);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to associate inbound NAT pools of the selected
         * internal load balancer with the primary network interface of the virtual machines in the scale set.
         */
        interface WithInternalInternalLoadBalancerNatPool<ParentT> extends WithOS<ParentT> {
            /**
             * Associate internal load balancer inbound NAT pools with the the primary network interface of the scale
             * set virtual machine.
             *
             * @param natPoolNames inbound NAT pool names
             * @return the next stage of the definition
             */
            WithOS<ParentT> withPrimaryInternalLoadBalancerInboundNatPools(String... natPoolNames);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the operating system image. */
        interface WithOS<ParentT> {
            /**
             * Specifies a known marketplace Windows image used as the operating system for the virtual machines in the
             * scale set.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged<ParentT> withPopularWindowsImage(
                KnownWindowsVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of the specified marketplace Windows image should be used.
             *
             * @param publisher specifies the publisher of the image
             * @param offer specifies the offer of the image
             * @param sku specifies the SKU of the image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged<ParentT> withLatestWindowsImage(
                String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a marketplace Windows image needs to be used.
             *
             * @param imageReference describes publisher, offer, SKU and version of the marketplace image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManagedOrUnmanaged<ParentT> withSpecificWindowsImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a generalized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameManaged<ParentT> withGeneralizedWindowsCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Windows custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withSpecializedWindowsCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Windows image to be used as the operating system for the virtual machines in
             * the scale set.
             *
             * @param imageUrl the URL of the VHD
             * @return the next stage of the definition
             */
            WithWindowsAdminUsernameUnmanaged<ParentT> withStoredWindowsImage(String imageUrl);

            /**
             * Specifies a known marketplace Linux image used as the virtual machine's operating system.
             *
             * @param knownImage a known market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged<ParentT> withPopularLinuxImage(KnownLinuxVirtualMachineImage knownImage);

            /**
             * Specifies that the latest version of a marketplace Linux image should be used.
             *
             * @param publisher the publisher of the image
             * @param offer the offer of the image
             * @param sku the SKU of the image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged<ParentT> withLatestLinuxImage(String publisher, String offer, String sku);

            /**
             * Specifies the specific version of a market-place Linux image that should be used.
             *
             * @param imageReference describes the publisher, offer, SKU and version of the market-place image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManagedOrUnmanaged<ParentT> withSpecificLinuxImageVersion(ImageReference imageReference);

            /**
             * Specifies the ID of a generalized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameManaged<ParentT> withGeneralizedLinuxCustomImage(String customImageId);

            /**
             * Specifies the ID of a specialized Linux custom image to be used.
             *
             * @param customImageId the resource ID of the custom image
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged<ParentT> withSpecializedLinuxCustomImage(String customImageId);

            /**
             * Specifies the user (custom) Linux image used as the virtual machine's operating system.
             *
             * @param imageUrl the URL the the VHD
             * @return the next stage of the definition
             */
            WithLinuxRootUsernameUnmanaged<ParentT> withStoredLinuxImage(String imageUrl);
        }



        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManagedOrUnmanaged<ParentT> {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged<ParentT> withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameManaged<ParentT> {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming conventions for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyManaged<ParentT> withRootUsername(String rootUserName);
        }

        /** The stage of the Linux virtual machine scale set definition allowing to specify SSH root user name. */
        interface WithLinuxRootUsernameUnmanaged<ParentT> {
            /**
             * Specifies the SSH root user name for the Linux virtual machine.
             *
             * @param rootUserName a root user name following the required naming convention for Linux user names
             * @return the next stage of the definition
             */
            WithLinuxRootPasswordOrPublicKeyUnmanaged<ParentT> withRootUsername(String rootUserName);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged<ParentT> {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged<ParentT> withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged<ParentT> withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyManaged<ParentT> {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged<ParentT> withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged<ParentT> withSsh(String publicKey);
        }

        /**
         * The stage of the Linux virtual machine scale set definition allowing to specify SSH root password or public
         * key.
         */
        interface WithLinuxRootPasswordOrPublicKeyUnmanaged<ParentT> {
            /**
             * Specifies the SSH root password for the Linux virtual machine.
             *
             * @param rootPassword a password following the complexity criteria for Azure Linux VM passwords
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged<ParentT> withRootPassword(String rootPassword);

            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey the SSH public key in PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged<ParentT> withSsh(String publicKey);
        }


        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachManaged<ParentT> extends WithManagedAttach<ParentT> {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManaged<ParentT> withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachUnmanaged<ParentT> extends WithUnmanagedAttach<ParentT> {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachUnmanaged<ParentT> withSsh(String publicKey);
        }

        /**
         * The stage of a Linux virtual machine scale set definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithLinuxAttachManagedOrUnmanaged<ParentT> extends WithManagedAttach<ParentT> {
            /**
             * Specifies the SSH public key.
             *
             * <p>Each call to this method adds the given public key to the list of VM's public keys.
             *
             * @param publicKey an SSH public key in the PEM format.
             * @return the next stage of the definition
             */
            WithLinuxAttachManagedOrUnmanaged<ParentT> withSsh(String publicKey);

            /** @return the next stage of a unmanaged disk based virtual machine scale set definition */
            WithUnmanagedAttach<ParentT> withUnmanagedDisks();
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameUnmanaged<ParentT> {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordUnmanaged<ParentT> withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordUnmanaged<ParentT> {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachUnmanaged<ParentT> withAdminPassword(String adminPassword);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManagedOrUnmanaged<ParentT> {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManagedOrUnmanaged<ParentT> withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManagedOrUnmanaged<ParentT> {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachManagedOrUnmanaged<ParentT> withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachManagedOrUnmanaged<ParentT> extends WithWindowsAttachManaged<ParentT> {
            WithWindowsAttachUnmanaged<ParentT> withUnmanagedDisks();
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminUsernameManaged<ParentT> {
            /**
             * Specifies the administrator user name for the Windows virtual machine.
             *
             * @param adminUserName the Windows administrator user name. This must follow the required naming convention
             *     for Windows user name.
             * @return the next stage of the definition
             */
            WithWindowsAdminPasswordManaged<ParentT> withAdminUsername(String adminUserName);
        }

        /**
         * The stage of the Windows virtual machine scale set definition allowing to specify administrator user name.
         */
        interface WithWindowsAdminPasswordManaged<ParentT> {
            /**
             * Specifies the administrator password for the Windows virtual machine.
             *
             * @param adminPassword the administrator password. This must follow the criteria for Azure Windows VM
             *     password.
             * @return the stage representing creatable Windows VM definition
             */
            WithWindowsAttachManaged<ParentT> withAdminPassword(String adminPassword);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachUnmanaged<ParentT> extends WithUnmanagedAttach<ParentT> {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsAttachUnmanaged<ParentT> withWinRM(WinRMListener listener);
        }

        /**
         * The stage of a Windows virtual machine scale set definition which contains all the minimum required inputs
         * for the resource to be created, but also allows for any other optional settings to be specified.
         */
        interface WithWindowsAttachManaged<ParentT> extends WithManagedAttach<ParentT> {
            /**
             * Enables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withVMAgent();

            /**
             * Disables the VM agent.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withoutVMAgent();

            /**
             * Enables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withAutoUpdate();

            /**
             * Disables automatic updates.
             *
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withoutAutoUpdate();

            /**
             * Specifies the time zone for the virtual machines to use.
             *
             * @param timeZone a time zone
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withTimeZone(String timeZone);

            /**
             * Specifies the WinRM listener.
             *
             * <p>Each call to this method adds the given listener to the list of VM's WinRM listeners.
             *
             * @param listener a WinRM listener
             * @return the next stage of the definition
             */
            WithWindowsAttachManaged<ParentT> withWinRM(WinRMListener listener);
        }


        /** The stage of a virtual machine scale set definition allowing to specify managed data disks. */
        interface WithManagedDataDisk<ParentT> {
            /**
             * Specifies that a managed disk needs to be created implicitly with the given size.
             *
             * @param sizeInGB the size of the managed disk
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDisk(int sizeInGB);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDisk(int sizeInGB, int lun, CachingTypes cachingType);

            /**
             * Specifies that a managed disk needs to be created implicitly with the given settings.
             *
             * @param sizeInGB the size of the managed disk
             * @param lun the disk LUN
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDisk(
                int sizeInGB, int lun, CachingTypes cachingType, StorageAccountTypes storageAccountType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDiskFromImage(int imageLun);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDiskFromImage(int imageLun, int newSizeInGB, CachingTypes cachingType);

            /**
             * Specifies the data disk to be created from the data disk image in the virtual machine image.
             *
             * @param imageLun the LUN of the source data disk image
             * @param newSizeInGB the new size that overrides the default size specified in the data disk image
             * @param cachingType the caching type
             * @param storageAccountType the storage account type
             * @return the next stage of virtual machine definition
             */
            WithManagedAttach<ParentT> withNewDataDiskFromImage(
                int imageLun, int newSizeInGB, CachingTypes cachingType, StorageAccountTypes storageAccountType);
        }

        /** The optionals applicable only for managed disks. */
        interface WithManagedDiskOptionals<ParentT> {
            /**
             * Specifies the storage account type for managed OS disk.
             *
             * @param accountType the storage account type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach<ParentT> withOSDiskStorageAccountType(StorageAccountTypes accountType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param cachingType the caching type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach<ParentT> withDataDiskDefaultCachingType(CachingTypes cachingType);

            /**
             * Specifies the default caching type for the managed data disks.
             *
             * @param storageAccountType the storage account type
             * @return the stage representing creatable VM definition
             */
            WithManagedAttach<ParentT> withDataDiskDefaultStorageAccountType(StorageAccountTypes storageAccountType);
        }

        /** The stage of the virtual machine scale set definition allowing to specify availability zone. */
        interface WithAvailabilityZone<ParentT> {
            /**
             * Specifies the availability zone for the virtual machine scale set.
             *
             * @param zoneId the zone identifier.
             * @return the next stage of the definition
             */
            WithManagedAttach<ParentT> withAvailabilityZone(AvailabilityZoneId zoneId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow managed data disks specific settings to be specified.
         */
        interface WithManagedAttach<ParentT>
            extends WithManagedDataDisk<ParentT>, WithManagedDiskOptionals<ParentT>, WithAvailabilityZone<ParentT>, WithAttach<ParentT> {
        }

        /** The stage of the virtual machine scale set definition allowing to specify unmanaged data disk. */
        interface WithUnmanagedDataDisk {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the VM scale set to be created
         * and optionally allow unmanaged data disks specific settings to be specified.
         */
        interface WithUnmanagedAttach<ParentT> extends WithUnmanagedDataDisk, WithAttach<ParentT> {
        }

        /** The stage of a virtual machine scale set definition allowing to specify the computer name prefix. */
        interface WithComputerNamePrefix<ParentT> {
            /**
             * Specifies the name prefix to use for auto-generating the names for the virtual machines in the scale set.
             *
             * @param namePrefix the prefix for the auto-generated names of the virtual machines in the scale set
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withComputerNamePrefix(String namePrefix);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify number of virtual machines in the
         * scale set.
         */
        interface WithCapacity<ParentT> {
            /**
             * Specifies the maximum number of virtual machines in the scale set.
             *
             * @param capacity number of virtual machines
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withCapacity(long capacity);
        }

        /** The stage of a virtual machine scale set definition allowing to specify OS disk configurations. */
        interface WithOSDiskSettings<ParentT> {
            /**
             * Specifies the caching type for the operating system disk.
             *
             * @param cachingType the caching type
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSDiskCaching(CachingTypes cachingType);

            /**
             * Specifies the name for the OS disk.
             *
             * @param name the OS disk name
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSDiskName(String name);
        }

        /** The stage of a virtual machine scale set definition allowing to specify the storage account. */
        interface WithStorageAccount<ParentT> {
            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param name the name of the storage account
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withNewStorageAccount(String name);

            /**
             * Specifies a new storage account for the OS and data disk VHDs of the virtual machines in the scale set.
             *
             * @param creatable the storage account definition in a creatable stage
             * @return the next stage in the definition
             */
            WithAttach<ParentT> withNewStorageAccount(Creatable<StorageAccount> creatable);

            /**
             * Specifies an existing storage account for the OS and data disk VHDs of the virtual machines in the scale
             * set.
             *
             * @param storageAccount an existing storage account
             * @return the next stage in the definition
             */
            WithAttach<ParentT> withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the custom data. */
        interface WithCustomData<ParentT> {
            /**
             * Specifies the custom data for the virtual machine scale set.
             *
             * @param base64EncodedCustomData the base64 encoded custom data
             * @return the next stage in the definition
             */
            WithAttach<ParentT> withCustomData(String base64EncodedCustomData);
        }

        /** The stage of the virtual machine scale set definition allowing to specify the secrets. */
        interface WithSecrets<ParentT> {
            /**
             * Specifies set of certificates that should be installed onto the virtual machine.
             *
             * @param secrets the secrets value to set
             * @return the next stage in the definition he secrets value to set
             */
            WithAttach<ParentT> withSecrets(List<VaultSecretGroup> secrets);
        }

//        /** The stage of a virtual machine scale set definition allowing to specify extensions. */
//        interface WithExtension<ParentT> {
//            /**
//             * Begins the definition of an extension reference to be attached to the virtual machines in the scale set.
//             *
//             * @param name the reference name for the extension
//             * @return the first stage of the extension reference definition
//             */
//            VirtualMachineScaleSetExtension.DefinitionStages.Blank<WithAttach<ParentT>> defineNewExtension(String name);
//        }

        /**
         * The stage of the virtual machine scale set definition allowing to enable System Assigned (Local) Managed
         * Service Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity<ParentT> {
            /**
             * Specifies that System Assigned (Local) Managed Service Identity needs to be enabled in the virtual
             * machine scale set.
             *
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> withSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled virtual machine scale set allowing
         * to set access for the identity.
         */
        interface WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> extends WithAttach<ParentT> {
            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the given access
             * (described by the role) on an ARM resource identified by the resource ID. Applications running on the
             * scale set VM instance will have the same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies that virtual machine scale set's local identity should have the given access (described by the
             * role) on the resource group that virtual machine resides. Applications running on the scale set VM
             * instance will have the same permission (role) on the resource group.
             *
             * @param role access role to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
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
            WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies that virtual machine scale set's system assigned (local) identity should have the access
             * (described by the role definition) on the resource group that virtual machine resides. Applications
             * running on the scale set VM instance will have the same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the scale set local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrAttach<ParentT> withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                String roleDefinitionId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify User Assigned (External) Managed
         * Service Identities.
         */
        interface WithUserAssignedManagedServiceIdentity<ParentT> {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the virtual
             * machine scale set.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the virtual machine scale set definition
             */
            WithAttach<ParentT> withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the virtual machine scale set.
             *
             * @param identity the identity
             * @return the next stage of the virtual machine scale set definition
             */
            WithAttach<ParentT> withExistingUserAssignedManagedServiceIdentity(Identity identity);
        }

        /** The stage of the virtual machine scale set definition allowing to enable boot diagnostics. */
        interface WithBootDiagnostics<ParentT> {
            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine.
             * Managed storage account is used.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBootDiagnosticsOnManagedStorageAccount();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBootDiagnostics();

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param creatable the storage account to be created and used for store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBootDiagnostics(Creatable<StorageAccount> creatable);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccount an existing storage account to be uses to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBootDiagnostics(StorageAccount storageAccount);

            /**
             * Specifies that boot diagnostics needs to be enabled in the virtual machine scale set.
             *
             * @param storageAccountBlobEndpointUri a storage account blob endpoint to store the boot diagnostics
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBootDiagnostics(String storageAccountBlobEndpointUri);
        }

        /** The stage of the virtual machine definition allowing to specify billing profile. */
        interface WithBillingProfile<ParentT> {

            /**
             * Set the billing related details of the low priority virtual machines in the scale set. This price is in
             * US Dollars.
             *
             * @param maxPrice the maxPrice value to set
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMaxPrice(Double maxPrice);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to specify priority for vms in the scale-set.
         */
        interface WithVMPriority<ParentT> {
            /**
             * Specifies the priority of the virtual machines in the scale set.
             *
             * @param priority the priority
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachinePriority(VirtualMachinePriorityTypes priority);

            /**
             * Specify that virtual machines in the scale set should be low priority VMs.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLowPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be low priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLowPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSpotPriorityVirtualMachine();

            /**
             * Specify that virtual machines in the scale set should be spot priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines in the scale set.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSpotPriorityVirtualMachine(VirtualMachineEvictionPolicyTypes policy);
        }

        /** The stage of the virtual machine scale set definition allowing to enable public ip for vm instances. */
        interface WithVirtualMachinePublicIp<ParentT> {
            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachinePublicIp();

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param leafDomainLabel the domain name label
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachinePublicIp(String leafDomainLabel);

            /**
             * Specify that virtual machines in the scale set should have public ip address.
             *
             * @param ipConfig the public ip address configuration
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachinePublicIp(VirtualMachineScaleSetPublicIpAddressConfiguration ipConfig);
        }

        /** The stage of the virtual machine scale set definition allowing to configure accelerated networking. */
        interface WithAcceleratedNetworking<ParentT> {
            /**
             * Specify that accelerated networking should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAcceleratedNetworking();

            /**
             * Specify that accelerated networking should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutAcceleratedNetworking();
        }

        /** The stage of the virtual machine scale set definition allowing to configure ip forwarding. */
        interface WithIpForwarding<ParentT> {
            /**
             * Specify that ip forwarding should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIpForwarding();

            /**
             * Specify that ip forwarding should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutIpForwarding();
        }

        /** The stage of the virtual machine scale set definition allowing to configure network security group. */
        interface WithNetworkSecurityGroup<ParentT> {
            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroup the network security group to associate
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);

            /**
             * Specifies the network security group for the virtual machine scale set.
             *
             * @param networkSecurityGroupId the network security group to associate
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroupId(String networkSecurityGroupId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure single placement group. */
        interface WithSinglePlacementGroup<ParentT> {
            /**
             * Specify that single placement group should be enabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSinglePlacementGroup();

            /**
             * Specify that single placement group should be disabled for the virtual machine scale set.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutSinglePlacementGroup();
        }

        /** The stage of the virtual machine scale set definition allowing to configure application gateway. */
        interface WithApplicationGateway<ParentT> {
            /**
             * Specify that an application gateway backend pool should be associated with virtual machine scale set.
             *
             * @param backendPoolId an existing backend pool id of the gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingApplicationGatewayBackendPool(String backendPoolId);
        }

        /** The stage of the virtual machine scale set definition allowing to configure application security group. */
        interface WithApplicationSecurityGroup<ParentT> {
            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroup the application security group
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingApplicationSecurityGroup(ApplicationSecurityGroup applicationSecurityGroup);

            /**
             * Specifies that provided application security group should be associated with the virtual machine scale
             * set.
             *
             * @param applicationSecurityGroupId the application security group id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingApplicationSecurityGroupId(String applicationSecurityGroupId);
        }

        /**
         * The stage of the virtual machine scale set definition allowing to configure a purchase plan.
         */
        interface WithPlan<ParentT> {
            /**
             * Specifies the purchase plan for the virtual machine scale set.
             *
             * @param plan a purchase plan
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPlan(PurchasePlan plan);
        }

        /**
         * The virtual machine scale set stages shared between managed and unmanaged based virtual machine scale set
         * definitions.
         */
        interface DefinitionShared<ParentT>
            extends UpdateAttachStages.Blank<ParentT>,
//            DefinitionStages.Blank4UpdateStage<ParentT>,
            UpdateAttachStages.WithNetworkSubnet<ParentT>,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancer<ParentT>,
            UpdateAttachStages.WithPrimaryInternalLoadBalancer<ParentT>,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancerBackendOrNatPool<ParentT>,
            UpdateAttachStages.WithInternalLoadBalancerBackendOrNatPool<ParentT>,
            UpdateAttachStages.WithPrimaryInternetFacingLoadBalancerNatPool<ParentT>,
            UpdateAttachStages.WithInternalInternalLoadBalancerNatPool<ParentT>,
            UpdateAttachStages.WithOS<ParentT>,
            UpdateAttachStages.WithAttach<ParentT>{
        }

        /** The entirety of the virtual machine scale set definition. */
        interface DefinitionManagedOrUnmanaged<ParentT>
            extends DefinitionShared<ParentT>,
            UpdateAttachStages.WithLinuxRootUsernameManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminUsernameManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminPasswordManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithLinuxAttachManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAttachManagedOrUnmanaged<ParentT>,
            UpdateAttachStages.WithManagedAttach<ParentT>,
            UpdateAttachStages.WithUnmanagedAttach<ParentT> {
        }

        /** The entirety of the managed disk based virtual machine scale set definition. */
        interface DefinitionManaged<ParentT>
            extends DefinitionShared<ParentT>,
            UpdateAttachStages.WithLinuxRootUsernameManaged<ParentT>,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyManaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminUsernameManaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminPasswordManaged<ParentT>,
            UpdateAttachStages.WithLinuxAttachManaged<ParentT>,
            UpdateAttachStages.WithWindowsAttachManaged<ParentT>,
            UpdateAttachStages.WithManagedAttach<ParentT> {
        }

        /** The entirety of the unmanaged disk based virtual machine scale set definition. */
        interface DefinitionUnmanaged<ParentT>
            extends DefinitionShared<ParentT>,
            UpdateAttachStages.WithLinuxRootUsernameUnmanaged<ParentT>,
            UpdateAttachStages.WithLinuxRootPasswordOrPublicKeyUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminUsernameUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAdminPasswordUnmanaged<ParentT>,
            UpdateAttachStages.WithLinuxAttachUnmanaged<ParentT>,
            UpdateAttachStages.WithWindowsAttachUnmanaged<ParentT>,
            UpdateAttachStages.WithUnmanagedAttach<ParentT> {
        }

        /**
         * The stage of a virtual machine scale set definition containing all the required inputs for the resource to be
         * created, but also allowing for any other optional settings to be specified.
         */
        interface WithAttach<ParentT>
            extends Attachable<ParentT>,
            WithOSDiskSettings<ParentT>,
            WithComputerNamePrefix<ParentT>,
            WithCapacity<ParentT>,
            WithStorageAccount<ParentT>,
            WithCustomData<ParentT>,
//            WithExtension<ParentT>,
            WithSystemAssignedManagedServiceIdentity<ParentT>,
            WithUserAssignedManagedServiceIdentity<ParentT>,
            WithBootDiagnostics<ParentT>,
            WithBillingProfile<ParentT>,
            WithVMPriority<ParentT>,
            WithVirtualMachinePublicIp<ParentT>,
            WithAcceleratedNetworking<ParentT>,
            WithIpForwarding<ParentT>,
            WithNetworkSecurityGroup<ParentT>,
            WithSinglePlacementGroup<ParentT>,
            WithApplicationGateway<ParentT>,
            WithApplicationSecurityGroup<ParentT>,
            WithSecrets<ParentT>,
            WithPlan<ParentT>,
            Resource.DefinitionWithTags<WithAttach<ParentT>> {
        }

    }

}
