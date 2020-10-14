// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.CapabilityStatus;
import com.azure.resourcemanager.sql.models.RegionCapabilities;
import com.azure.resourcemanager.sql.models.ServerVersionCapability;
import com.azure.resourcemanager.sql.fluent.models.LocationCapabilitiesInner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Implementation for RegionCapabilities. */
public class RegionCapabilitiesImpl extends WrapperImpl<LocationCapabilitiesInner> implements RegionCapabilities {

    private Map<String, ServerVersionCapability> supportedCapabilitiesMap;

    /**
     * Creates an instance of the region capabilities object.
     *
     * @param innerObject the inner object
     */
    public RegionCapabilitiesImpl(LocationCapabilitiesInner innerObject) {
        super(innerObject);
        supportedCapabilitiesMap = new HashMap<>();
        if (this.innerModel().supportedServerVersions() != null) {
            for (ServerVersionCapability serverVersionCapability : this.innerModel().supportedServerVersions()) {
                supportedCapabilitiesMap.put(serverVersionCapability.name(), serverVersionCapability);
            }
        }
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().name());
    }

    @Override
    public CapabilityStatus status() {
        return this.innerModel().status();
    }

    @Override
    public Map<String, ServerVersionCapability> supportedCapabilitiesByServerVersion() {
        return Collections.unmodifiableMap(this.supportedCapabilitiesMap);
    }
}
