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
    /** @return the location name */
    Region region();

    /** @return the Azure SQL Database's status for the location */
    CapabilityStatus status();

    /** @return the list of supported server versions */
    Map<String, ServerVersionCapability> supportedCapabilitiesByServerVersion();
}
