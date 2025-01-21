// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluent.models.SubscriptionInner;

/**
 * An immutable client-side representation of an Azure subscription.
 */
@Fluent
public interface Subscription extends Indexable, HasInnerModel<SubscriptionInner> {

    /**
     * Gets the UUID of the subscription.
     *
     * @return the UUID of the subscription
     */
    String subscriptionId();

    /**
     * Gets the name of the subscription for humans to read.
     *
     * @return the name of the subscription for humans to read
     */
    String displayName();

    /**
     * Gets the state of the subscription.
     *
     * @return the state of the subscription.
     */
    SubscriptionState state();

    /**
     * Gets the policies defined in the subscription.
     *
     * @return the policies defined in the subscription
     */
    SubscriptionPolicies subscriptionPolicies();

    /**
     * List the locations the subscription has access to.
     *
     * @return the lazy list of locations
     */

    PagedIterable<Location> listLocations();

    /**
     * Gets the data center location for the specified region, if the selected subscription has access to it.
     *
     * @param region an Azure region
     * @return an Azure data center location, or null if the location is not accessible to this subscription
     */
    Location getLocationByRegion(Region region);
}
