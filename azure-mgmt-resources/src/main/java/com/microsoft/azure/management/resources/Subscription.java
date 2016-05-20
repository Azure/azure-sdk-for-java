package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionPolicies;

import java.io.IOException;

/**
 * Defines an interface for accessing a subscription in Azure.
 */
public interface Subscription extends
        Indexable,
        Wrapper<SubscriptionInner> {

    /**
     * Get the subscription Id.
     *
     * @return the subscription Id.
     */
    String subscriptionId();

    /**
     * Get the subscription display name.
     *
     * @return the subscription display name.
     */
    String displayName();

    /**
     * Get the subscription state.
     *
     * @return the subscription state.
     */
    String state();

    /**
     * Get the subscription policies.
     * @return the subscription policies.
     */
    SubscriptionPolicies subscriptionPolicies();

    /**
     * Gets a list of the subscription locations.
     *
     * @throws CloudException exception thrown from REST call.
     * @throws IOException exception thrown from serialization/deserialization.
     * @return the List of locations.
     */

    PagedList<Location> listLocations() throws IOException, CloudException;
}
