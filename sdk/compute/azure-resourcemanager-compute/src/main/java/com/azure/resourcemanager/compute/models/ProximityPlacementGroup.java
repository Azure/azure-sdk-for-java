// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.ProximityPlacementGroupInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** Type representing Proximity Placement Group for an Azure compute resource. */
@Fluent
public interface ProximityPlacementGroup extends HasInnerModel<ProximityPlacementGroupInner> {
    /**
     * Get specifies the type of the proximity placement group. &lt;br&gt;&lt;br&gt; Possible values are:
     * &lt;br&gt;&lt;br&gt; **Standard** &lt;br&gt;&lt;br&gt; **Ultra**. Possible values include: 'Standard', 'Ultra'.
     *
     * @return the proximityPlacementGroupType value
     */
    ProximityPlacementGroupType proximityPlacementGroupType();

    /**
     * Get a list of references to all virtual machines in the proximity placement group.
     *
     * @return the virtualMachines value
     */
    List<String> virtualMachineIds();

    /**
     * Get a list of references to all virtual machine scale sets in the proximity placement group.
     *
     * @return the virtualMachineScaleSets value
     */
    List<String> virtualMachineScaleSetIds();

    /**
     * Get a list of references to all availability sets in the proximity placement group.
     *
     * @return the availabilitySets value
     */
    List<String> availabilitySetIds();

    /**
     * Gets the location of the proximity placement group.
     *
     * @return the location
     */
    String location();

    /**
     * Gets the name of the resource group for the proximity placement group.
     *
     * @return the resource group name
     */
    String resourceGroupName();

    /**
     * The rsource ID of the placement group.
     *
     * @return the resource Id.
     */
    String id();
}
