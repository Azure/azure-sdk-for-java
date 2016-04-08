package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.models.Location;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.api.LocationInner;

public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGetting<Subscription> {
    PagedList<Location> listLocations();
}
