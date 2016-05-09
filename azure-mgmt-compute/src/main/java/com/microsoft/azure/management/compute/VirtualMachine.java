package com.microsoft.azure.management.compute;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

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

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithRegion> {
    }

    interface DefinitionWithRegion extends GroupableResource.DefinitionWithGroup<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup {
        DefinitionWithImage withImage(String publisher, String offer, String sku, String version);
        DefinitionWithImage withImage(VirtualMachineImage image);
        DefinitionWithImage withLatestImage(String publisher, String offer, String sku);
        DefinitionWithImage withKnownImage(KnownVirtualMachineImage image);
    }

    interface DefinitionWithImage {
    }
}
