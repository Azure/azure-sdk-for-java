// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.ComputeSku;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroup;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.fluent.models.ProximityPlacementGroupInner;
import com.azure.resourcemanager.compute.models.SubResourceWithColocationStatus;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** The implementation for {@link ComputeSku}. */
final class ProximityPlacementGroupImpl implements ProximityPlacementGroup {
    private final ProximityPlacementGroupInner inner;

    ProximityPlacementGroupImpl(ProximityPlacementGroupInner inner) {
        this.inner = inner;
    }

    @Override
    public ProximityPlacementGroupType proximityPlacementGroupType() {
        return this.innerModel().proximityPlacementGroupType();
    }

    @Override
    public List<String> virtualMachineIds() {
        return getStringListFromSubResourceList(this.innerModel().virtualMachines());
    }

    @Override
    public List<String> virtualMachineScaleSetIds() {
        return getStringListFromSubResourceList(this.innerModel().virtualMachineScaleSets());
    }

    @Override
    public List<String> availabilitySetIds() {
        return getStringListFromSubResourceList(this.innerModel().availabilitySets());
    }

    @Override
    public String location() {
        return this.innerModel().location();
    }

    @Override
    public String resourceGroupName() {
        return ResourceId.fromString(this.id()).resourceGroupName();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public ProximityPlacementGroupInner innerModel() {
        return inner;
    }

    private List<String> getStringListFromSubResourceList(List<SubResourceWithColocationStatus> subList) {
        List<String> stringList = null;

        if (subList != null && !subList.isEmpty()) {
            stringList = new ArrayList<>();
            Iterator<SubResourceWithColocationStatus> iter = subList.iterator();
            while (iter.hasNext()) {
                stringList.add(iter.next().id());
            }
        }

        return stringList;
    }
}
