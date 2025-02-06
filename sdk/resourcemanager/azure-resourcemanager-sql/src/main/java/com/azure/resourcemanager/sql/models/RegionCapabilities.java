// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.sql.fluent.models.LocationCapabilitiesInner;
import java.util.Map;

/** An immutable client-side representation of an Azure SQL server capabilities for a given region. */
@Fluent
public interface RegionCapabilities extends HasInnerModel<LocationCapabilitiesInner> {
    /**
     * Gets the location name.
     *
     * @return the location name
     */
    Region region();

    /**
     * Gets the Azure SQL Database's status for the location.
     *
     * @return the Azure SQL Database's status for the location
     */
    CapabilityStatus status();

    /**
     * Gets the list of supported server versions.
     *
     * @return the list of supported server versions
     */
    Map<String, ServerVersionCapability> supportedCapabilitiesByServerVersion();
}
