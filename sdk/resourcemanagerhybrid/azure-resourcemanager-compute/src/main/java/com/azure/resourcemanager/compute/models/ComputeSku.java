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
    /** @return the sku name */
    ComputeSkuName name();
    /** @return the sku tier */
    ComputeSkuTier tier();
    /** @return the compute resource type that the sku describes */
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
    /** @return the regions that the sku is available */
    List<Region> regions();
    /** @return the availability zones supported for this sku, index by region */
    Map<Region, Set<AvailabilityZoneId>> zones();
    /** @return the scaling information of the sku */
    ResourceSkuCapacity capacity();
    /** @return the api versions that this sku supports */
    List<String> apiVersions();
    /** @return the metadata for querying the sku pricing information */
    List<ResourceSkuCosts> costs();
    /** @return the capabilities of the sku */
    List<ResourceSkuCapabilities> capabilities();
    /** @return the restrictions because of which SKU cannot be used */
    List<ResourceSkuRestrictions> restrictions();
}
