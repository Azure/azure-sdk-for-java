package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionPolicies;

public interface Location extends
        Indexable,
        Wrapper<LocationInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String subscriptionId();
    String name();
    String displayName();
    String latitude();
    String longitude();
}
