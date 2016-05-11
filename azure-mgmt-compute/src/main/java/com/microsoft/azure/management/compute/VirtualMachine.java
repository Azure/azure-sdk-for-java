package com.microsoft.azure.management.compute;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.List;

public interface VirtualMachine extends
        GroupableResource,
        Refreshable<VirtualMachine>,
        Wrapper<VirtualMachineInner> {
    /**
     * Get the plan value.
     *
     * @return the plan value
     */
    Plan plan();

    /**
     * Get the hardwareProfile value.
     *
     * @return the hardwareProfile value
     */
    HardwareProfile hardwareProfile();

    /**
     * Get the storageProfile value.
     *
     * @return the storageProfile value
     */
    StorageProfile storageProfile();

    /**
     * Get the osProfile value.
     *
     * @return the osProfile value
     */
    OSProfile osProfile();

    /**
     * Get the networkProfile value.
     *
     * @return the networkProfile value
     */
    NetworkProfile networkProfile();

    /**
     * Get the diagnosticsProfile value.
     *
     * @return the diagnosticsProfile value
     */
    DiagnosticsProfile diagnosticsProfile();

    /**
     * Get the availabilitySet value.
     *
     * @return the availabilitySet value
     */
    SubResource availabilitySet();

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    String provisioningState();

    /**
     * Get the instanceView value.
     *
     * @return the instanceView value
     */
    VirtualMachineInstanceView instanceView();

    /**
     * Get the licenseType value.
     *
     * @return the licenseType value
     */
    String licenseType();

    /**
     * Get the resources value.
     *
     * @return the resources value
     */
    List<VirtualMachineExtensionInner> resources();

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithStorageAccount> {
    }

    interface DefinitionWithStorageAccount {
        DefinitionWithNewOSDisk withNewStorageAccount(String name);
        DefinitionWithOSDisk withExistingStorageAccount(String name);
        DefinitionWithOSDisk withExistingStorageAccount(StorageAccount.DefinitionProvisionable provisionable);
    }

    interface DefinitionWithVMImage {
        DefinitionWithDataDisk withImage(ImageReference imageReference);
        DefinitionWithDataDisk withLatestImage(String publisher, String offer, String sku);
        DefinitionWithDataDisk withKnownImage(KnownVirtualMachineImage knownImage);
    }

    interface DefinitionWithNewOSDisk extends DefinitionWithVMImage {
        NewOSDiskFromImage  defineOSDisk(String name);
    }

    interface DefinitionWithOSDisk extends DefinitionWithVMImage {
        OSDiskFromImage defineOSDisk(String name);
        DefinitionWithDataDisk withUserImage(String containerName, String vhdName, OperatingSystemTypes osType);
        DefinitionWithDataDisk withExistingOSDisk(String containerName, String vhdName, OperatingSystemTypes osType);
    }

    interface NewOSDiskFromImage {
        DefinitionWithOSDiskConfiguration fromImage(ImageReference imageReference);
        DefinitionWithOSDiskConfiguration fromLatestImage(String publisher, String offer, String sku);
        DefinitionWithOSDiskConfiguration fromKnownImage(KnownVirtualMachineImage knownImage);
    }

    interface OSDiskFromImage extends NewOSDiskFromImage {
        DefinitionWithOSDiskConfiguration fromUserImage(String containerName, String vhdName, OperatingSystemTypes osType);
    }

    interface DefinitionWithOSDiskConfiguration {
        DefinitionWithOSDiskConfiguration withReadOnlyCaching();
        DefinitionWithOSDiskConfiguration withReadWriteCaching();
        DefinitionWithOSDiskConfiguration withNoCaching();
        DefinitionWithOSDiskConfiguration withSize(Integer sizeInGB);
        DefinitionWithOSDiskConfiguration storeVHDAt(String containerName, String vhdName);
        VirtualMachine.DefinitionWithDataDisk attach();
    }

    interface DefinitionWithDataDisk {
    }
}
