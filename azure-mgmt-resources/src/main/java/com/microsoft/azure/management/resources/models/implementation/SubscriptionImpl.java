package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.models.Location;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionPolicies;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;

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
    public PagedList<Location> listLocations() throws IOException, CloudException {
        final PagedList<LocationInner> inners = client.listLocations(this.subscriptionId()).getBody();
        PageImpl<Location> locationPage = new PageImpl<>();
        locationPage.setNextPageLink(inners.nextpageLink());
        locationPage.setItems(new ArrayList<Location>());
        for (LocationInner inner : inners) {
            locationPage.getItems().add(new LocationImpl(inner, client));
        }
        return new PagedList<Location>() {
            @Override
            public Page<Location> nextPage(String nextPageLink) throws RestException, IOException {
                Page<LocationInner> innerPage = inners.nextPage(nextPageLink);
                PageImpl<Location> locationPage = new PageImpl<>();
                locationPage.setNextPageLink(innerPage.getNextPageLink());
                locationPage.setItems(new ArrayList<Location>());
                for (LocationInner inner : innerPage.getItems()) {
                    locationPage.getItems().add(new LocationImpl(inner, client));
                }
                return locationPage;
            }
        };
    }

    @Override
    public ResourceGroups resourceGroups() throws IOException, CloudException {
        return new ResourceGroupsImpl(new ResourceManagementClientImpl(serviceClient.getCredentials()));
    }

    @Override
    public GenericResources genericResources() {
        return null;
    }
}
