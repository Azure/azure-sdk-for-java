package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionPolicies;

import java.io.IOException;

public class SubscriptionImpl extends
        IndexableWrapperImpl<SubscriptionInner>
        implements
        Subscription  {

    private final SubscriptionsInner client;
    private final SubscriptionClientImpl serviceClient;

    public SubscriptionImpl(SubscriptionInner subscription, SubscriptionClientImpl serviceClient) {
        super(subscription.id(), subscription);
        this.serviceClient = serviceClient;
        this.client = serviceClient.subscriptions();
    }

    /***********************************************************
     * Getters
     ***********************************************************/

    @Override
    public String subscriptionId() {
        return this.inner().subscriptionId();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public String state() {
        return this.inner().state();
    }

    @Override
    public SubscriptionPolicies subscriptionPolicies() {
        return this.inner().subscriptionPolicies();
    }

    /***********************************************************
     * Other operations
     ***********************************************************/

    @Override
    public ResourceGroups resourceGroups() throws IOException, CloudException {
        return new ResourceGroupsImpl(new ResourceManagementClientImpl(serviceClient.getCredentials()));
    }

    @Override
    public GenericResources genericResources() {
        return null;
    }
}
