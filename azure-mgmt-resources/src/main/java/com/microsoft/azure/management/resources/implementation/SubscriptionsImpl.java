package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.models.Location;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.SubscriptionImpl;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionsImpl
        extends PagedList<Subscription>
        implements Subscriptions {
    private SubscriptionClientImpl client;
    private PagedList<SubscriptionInner> innerList;
    private Map<String, Subscription> indexable;

    public SubscriptionsImpl(SubscriptionClientImpl client) throws IOException, CloudException {
        this.client = client;
        this.innerList = client.subscriptions().list().getBody();
    }

    @Override
    public Map<String, Subscription> asMap() throws Exception {
        if (indexable == null) {
            indexable = new HashMap<>();
            for(Subscription item : this) {
                indexable.put(item.id(), item);
            }
        }
        return Collections.unmodifiableMap(indexable);
    }

    @Override
    // Gets a specific resource group
    public SubscriptionImpl get(String name) throws Exception {
        SubscriptionInner subscription = client.subscriptions().get(name).getBody();
        return new SubscriptionImpl(subscription, client);
    }

    /***************************************************
     * Helpers
     ***************************************************/

    @Override
    public Page<Subscription> nextPage(String nextPageLink) throws RestException, IOException {
        PageImpl<Subscription> page = new PageImpl<>();
        List<Subscription> items = new ArrayList<>();
        if (currentPage() == null) {
            for (SubscriptionInner inner : innerList) {
                items.add((new SubscriptionImpl(inner, client)));
            }
            page.setNextPageLink(innerList.nextpageLink());
        } else {
            Page<SubscriptionInner> innerPage = innerList.nextPage(nextPageLink);
            page.setNextPageLink(innerPage.getNextPageLink());
            for (SubscriptionInner inner : innerList) {
                items.add((new SubscriptionImpl(inner, client)));
            }
        }
        page.setItems(items);
        return page;
    }

    @Override
    public PagedList<Location> listLocations() {
        return null;
    }
}
