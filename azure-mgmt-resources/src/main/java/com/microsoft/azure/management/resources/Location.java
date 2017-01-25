/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.LocationInner;

/**
 * An immutable client-side representation of an Azure location.
 */
@Fluent
public interface Location extends
        Indexable,
        Wrapper<LocationInner>,
        HasName {
    /**
     * @return the subscription UUID
     */
    String subscriptionId();

    /**
     * @return the display name of the location readable by humans
     */
    String displayName();

    /**
     * @return the region of the data center location
     */
    Region region();

    /**
     * @return the latitude of the location
     */
    String latitude();

    /**
     * @return the longitude of the location
     */
    String longitude();
}
