package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionPolicies;

import java.io.IOException;

public interface Subscription extends
        Indexable,
        Wrapper<SubscriptionInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String subscriptionId();
    String displayName();
    String state();
    SubscriptionPolicies subscriptionPolicies();

    /***********************************************************
     * Other operations
     ***********************************************************/
    PagedList<Location> listLocations() throws IOException, CloudException;
}
