package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.models.Subscription;

public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGetting<Subscription> {
}
