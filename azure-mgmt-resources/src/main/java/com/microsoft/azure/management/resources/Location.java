/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.LocationInner;

/**
 * An immutable client-side representation of an Azure location.
 */
public interface Location extends
        Indexable,
        Wrapper<LocationInner> {
    /**
     * @return the subscription UUID
     */
    String subscriptionId();

    /**
     * @return the name of the location
     */
    String name();

    /**
     * @return the display name of the location readable by humans
     */
    String displayName();

    /**
     * @return the latitude of the location
     */
    String latitude();

    /**
     * @return the longitude of the location
     */
    String longitude();
}
