// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.ComputeResourceType;
import com.azure.resourcemanager.compute.models.ComputeSku;
import com.azure.resourcemanager.compute.models.ComputeSkuName;
import com.azure.resourcemanager.compute.models.ComputeSkuTier;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.DiskStorageAccountTypes;
import com.azure.resourcemanager.compute.models.ResourceSkuCapabilities;
import com.azure.resourcemanager.compute.models.ResourceSkuCapacity;
import com.azure.resourcemanager.compute.models.ResourceSkuCosts;
import com.azure.resourcemanager.compute.models.ResourceSkuLocationInfo;
import com.azure.resourcemanager.compute.models.ResourceSkuRestrictions;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.fluent.models.ResourceSkuInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.core.management.Region;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The implementation for {@link ComputeSku}. */
final class ComputeSkuImpl implements ComputeSku {
    private final ResourceSkuInner inner;

    ComputeSkuImpl(ResourceSkuInner inner) {
        this.inner = inner;
    }

    @Override
    public ComputeSkuName name() {
        if (this.inner.name() != null) {
            return ComputeSkuName.fromString(this.inner.name());
        } else {
            return null;
        }
    }

    @Override
    public ComputeSkuTier tier() {
        if (this.inner.tier() != null) {
            return ComputeSkuTier.fromString(this.inner.tier());
        } else {
            return null;
        }
    }

    @Override
    public ComputeResourceType resourceType() {
        if (this.inner.resourceType() != null) {
            return ComputeResourceType.fromString(this.inner.resourceType());
        } else {
            return null;
        }
    }

    @Override
    public VirtualMachineSizeTypes virtualMachineSizeType() {
        if (this.inner.resourceType() != null
            && this.inner.resourceType().equalsIgnoreCase("virtualMachines")
            && this.inner.name() != null) {
            return VirtualMachineSizeTypes.fromString(this.inner.name());
        } else {
            return null;
        }
    }

    @Override
    public DiskSkuTypes diskSkuType() {
        if (this.inner.resourceType() != null
            && (this.inner.resourceType().equalsIgnoreCase("disks")
                || this.inner.resourceType().equalsIgnoreCase("snapshots"))
            && this.inner.name() != null) {
            return DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(this.inner.name()));
        } else {
            return null;
        }
    }

    @Override
    public AvailabilitySetSkuTypes availabilitySetSkuType() {
        if (this.inner.resourceType() != null
            && (this.inner.resourceType().equalsIgnoreCase("availabilitySets"))
            && this.inner.name() != null) {
            return AvailabilitySetSkuTypes.fromString(this.inner.name());
        } else {
            return null;
        }
    }

    @Override
    public List<Region> regions() {
        List<Region> regions = new ArrayList<>();
        if (this.inner.locations() != null) {
            for (String location : this.inner.locations()) {
                regions.add(Region.fromName(location));
            }
        }
        return regions;
    }

    @Override
    public Map<Region, Set<AvailabilityZoneId>> zones() {
        Map<Region, Set<AvailabilityZoneId>> regionToZones = new HashMap<>();
        if (this.inner.locationInfo() != null) {
            for (ResourceSkuLocationInfo info : this.inner.locationInfo()) {
                if (info.location() != null) {
                    Region region = Region.fromName(info.location());
                    if (!regionToZones.containsKey(region)) {
                        regionToZones.put(region, new HashSet<>());
                    }
                    Set<AvailabilityZoneId> availabilityZoneIds = new HashSet<>();
                    if (info.zones() != null) {
                        for (String zone : info.zones()) {
                            availabilityZoneIds.add(AvailabilityZoneId.fromString(zone));
                        }
                    }
                    regionToZones.get(region).addAll(availabilityZoneIds);
                }
            }
        }
        return regionToZones;
    }

    @Override
    public ResourceSkuCapacity capacity() {
        return this.inner.capacity();
    }

    @Override
    public List<String> apiVersions() {
        if (this.inner.apiVersions() != null) {
            return Collections.unmodifiableList(this.inner.apiVersions());
        } else {
            return Collections.unmodifiableList(new ArrayList<>());
        }
    }

    @Override
    public List<ResourceSkuCosts> costs() {
        if (this.inner.costs() != null) {
            return Collections.unmodifiableList(this.inner.costs());
        } else {
            return Collections.unmodifiableList(new ArrayList<>());
        }
    }

    @Override
    public List<ResourceSkuCapabilities> capabilities() {
        if (this.inner.capabilities() != null) {
            return Collections.unmodifiableList(this.inner.capabilities());
        } else {
            return Collections.unmodifiableList(new ArrayList<>());
        }
    }

    @Override
    public List<ResourceSkuRestrictions> restrictions() {
        if (this.inner.capabilities() != null) {
            return Collections.unmodifiableList(this.inner.restrictions());
        } else {
            return Collections.unmodifiableList(new ArrayList<>());
        }
    }

    @Override
    public ResourceSkuInner innerModel() {
        return this.inner;
    }
}
