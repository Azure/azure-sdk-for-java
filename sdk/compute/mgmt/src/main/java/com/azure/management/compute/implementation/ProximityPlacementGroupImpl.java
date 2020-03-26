/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.compute.models.ProximityPlacementGroupInner;
import com.azure.management.compute.ComputeSku;
import com.azure.management.compute.ProximityPlacementGroup;
import com.azure.management.compute.ProximityPlacementGroupType;
import com.azure.management.resources.fluentcore.arm.ResourceId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The implementation for {@link ComputeSku}.
 */
final class ProximityPlacementGroupImpl implements ProximityPlacementGroup {
    private final ProximityPlacementGroupInner inner;

    ProximityPlacementGroupImpl(ProximityPlacementGroupInner inner) {
        this.inner = inner;
    }

    @Override
    public ProximityPlacementGroupType proximityPlacementGroupType() {
        return this.inner().proximityPlacementGroupType();
    }

    @Override
    public List<String> virtualMachineIds() {
        return getStringListFromSubResourceList(this.inner().virtualMachines());
    }

    @Override
    public List<String> virtualMachineScaleSetIds() {
        return getStringListFromSubResourceList(this.inner().virtualMachineScaleSets());
    }

    @Override
    public List<String> availabilitySetIds() {
        return getStringListFromSubResourceList(this.inner().availabilitySets());
    }

    @Override
    public String location() {
        return this.inner().getLocation();
    }

    @Override
    public String resourceGroupName() {
        return ResourceId.fromString(this.id()).resourceGroupName();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public ProximityPlacementGroupInner inner() {
        return inner;
    }


    private List<String> getStringListFromSubResourceList(List<SubResource> subList) {
        List<String> stringList = null;

        if (subList != null && !subList.isEmpty()) {
            stringList = new ArrayList<>();
            Iterator<SubResource> iter = subList.iterator();
            while (iter.hasNext()) {
                stringList.add(iter.next().getId());
            }
        }

        return stringList;
    }
}
