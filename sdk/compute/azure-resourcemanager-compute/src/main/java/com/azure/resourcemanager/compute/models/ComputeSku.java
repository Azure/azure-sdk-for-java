// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.ResourceSkuInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Type representing sku for an Azure compute resource. */
@Fluent
public interface ComputeSku extends HasInnerModel<ResourceSkuInner> {
    /**
     * Gets the SKU name.
     *
     * @return the SKU name
     */
    ComputeSkuName name();

    /**
     * Gets the SKU tier.
     *
     * @return the SKU tier
     */
    ComputeSkuTier tier();

    /**
     * Gets the compute resource type that the SKU describes.
     *
     * @return the compute resource type that the SKU describes
     */
    ComputeResourceType resourceType();

    /**
     * The virtual machine size type if the sku describes sku for virtual machine resource type.
     *
     * <p>The size can be used for {@link VirtualMachine.DefinitionStages.WithVMSize#withSize(VirtualMachineSizeTypes)}
     * and {@link VirtualMachine.Update#withSize(VirtualMachineSizeTypes)}.
     *
     * @return the virtual machine size type
     */
    VirtualMachineSizeTypes virtualMachineSizeType();

    /**
     * The managed disk or snapshot sku type if the sku describes sku for disk or snapshot resource type.
     *
     * <p>The sku type can be used for {@link Disk.DefinitionStages.WithSku#withSku(DiskSkuTypes)}, {@link
     * Disk.UpdateStages.WithSku#withSku(DiskSkuTypes)}.
     *
     * @return the managed disk or snapshot sku type
     */
    DiskSkuTypes diskSkuType();

    /**
     * The availability set sku type if the sku describes sku for availability set resource type.
     *
     * <p>The sku type can be used for {@link AvailabilitySet.DefinitionStages.WithSku#withSku(AvailabilitySetSkuTypes)}
     * and {@link AvailabilitySet.UpdateStages.WithSku#withSku(AvailabilitySetSkuTypes)}.
     *
     * @return the availability set sku type
     */
    AvailabilitySetSkuTypes availabilitySetSkuType();

    /**
     * Gets the regions that the SKU is available.
     *
     * @return the regions that the sku is available
     */
    List<Region> regions();

    /**
     * Gets the availability zones supported for this SKU.
     *
     * @return the availability zones supported for this SKU, index by region
     */
    Map<Region, Set<AvailabilityZoneId>> zones();

    /**
     * Gets the scaling information of the SKU.
     *
     * @return the scaling information of the SKU
     */
    ResourceSkuCapacity capacity();

    /**
     * Gets the api versions that this SKU supports.
     *
     * @return the api versions that this SKU supports
     */
    List<String> apiVersions();

    /**
     * Gets the metadata for querying the SKU pricing information.
     *
     * @return the metadata for querying the SKU pricing information
     */
    List<ResourceSkuCosts> costs();

    /**
     * Gets the capabilities of the SKU.
     *
     * @return the capabilities of the SKU
     */
    List<ResourceSkuCapabilities> capabilities();

    /**
     * Gets the restrictions because of which SKU cannot be used.
     *
     * @return the restrictions because of which SKU cannot be used
     */
    List<ResourceSkuRestrictions> restrictions();
}
