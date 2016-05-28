package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGettingByName<Subscription> {
}
