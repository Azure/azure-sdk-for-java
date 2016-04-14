package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.models.Location;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionPolicies;

import java.io.IOException;

public class SubscriptionImpl extends
        IndexableWrapperImpl<SubscriptionInner>
        implements
        Subscription  {

    private final SubscriptionsInner subscriptions;
    private final SubscriptionClientImpl client;

    public SubscriptionImpl(SubscriptionInner subscription, SubscriptionClientImpl client) {
        super(subscription.id(), subscription);
        this.subscriptions = client.subscriptions();
        this.client = client;
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
    public PagedList<Location> listLocations() throws IOException, CloudException {
        PagedListConverter<LocationInner, Location> converter = new PagedListConverter<LocationInner, Location>() {
            @Override
            public Location typeConvert(LocationInner locationInner) {
                return new LocationImpl(locationInner, subscriptions);
            }
        };
        return converter.convert(subscriptions.listLocations(this.subscriptionId()).getBody());
    }

    @Override
    public ResourceGroups resourceGroups() throws IOException, CloudException {
        ResourceManagementClientImpl resourceManagementClient = new ResourceManagementClientImpl(client.getCredentials());
        resourceManagementClient.setSubscriptionId(this.subscriptionId());
        return new ResourceGroupsImpl(resourceManagementClient);
    }

    @Override
    public GenericResources genericResources() {
        return null;
    }
}
