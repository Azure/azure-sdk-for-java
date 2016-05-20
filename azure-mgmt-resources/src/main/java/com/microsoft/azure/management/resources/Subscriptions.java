package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Defines an interface for accessing subscriptions in Azure.
 */
public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGetting<Subscription> {
}
