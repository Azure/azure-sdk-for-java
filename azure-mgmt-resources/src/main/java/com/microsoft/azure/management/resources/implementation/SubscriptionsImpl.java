package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.SubscriptionImpl;
import com.microsoft.azure.management.resources.models.implementation.api.SubscriptionInner;

import java.io.IOException;
import java.util.List;

final class SubscriptionsImpl
        implements Subscriptions {
    private SubscriptionsInner subscriptions;
    private SubscriptionClientImpl client;

    public SubscriptionsImpl(SubscriptionClientImpl client) throws IOException, CloudException {
        this.client = client;
        this.subscriptions = client.subscriptions();
    }

    @Override
    public List<Subscription> list() throws CloudException, IOException {
        PagedListConverter<SubscriptionInner, Subscription> converter = new PagedListConverter<SubscriptionInner, Subscription>() {
            @Override
            public Subscription typeConvert(SubscriptionInner subscriptionInner) {
                return new SubscriptionImpl(subscriptionInner, subscriptions, client);
            }
        };
        return converter.convert(subscriptions.list().getBody());
    }

    @Override
    // Gets a specific resource group
    public SubscriptionImpl get(String name) throws Exception {
        SubscriptionInner subscription = subscriptions.get(name).getBody();
        return new SubscriptionImpl(subscription, subscriptions, client);
    }

    /***************************************************
     * Helpers
     ***************************************************/
}
