/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.LocationCapabilitiesInner;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure SQL server capabilities for a given region.
 */
@Fluent
public interface RegionCapabilities extends
        HasInner<LocationCapabilitiesInner> {
    /**
     * @return the location name
     */
    Region region();

    /**
     * @return the Azure SQL Database's status for the location
     */
    CapabilityStatus status();

    /**
     * @return the list of supported server versions
     */
    Map<String, ServerVersionCapability> supportedCapabilitiesByServerVersion();
}
