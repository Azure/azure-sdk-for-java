package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionPolicies;

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
    ResourceGroups resourceGroups() throws IOException, CloudException;
    GenericResources genericResources();
}
