/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.SubscriptionInner;

/**
 * An immutable client-side representation of an Azure subscription.
 */
@Fluent
public interface Subscription extends
        Indexable,
        Wrapper<SubscriptionInner> {

    /**
     * @return the UUID of the subscription
     */
    String subscriptionId();

    /**
     * @return the name of the subscription for humans to read
     */
    String displayName();

    /**
     * @return the state of the subscription.
     */
    SubscriptionState state();

    /**
     * @return the policies defined in the subscription
     */
    SubscriptionPolicies subscriptionPolicies();

    /**
     * List the locations the subscription has access to.
     *
     * @return the lazy list of locations
     */

    PagedList<Location> listLocations();
}
