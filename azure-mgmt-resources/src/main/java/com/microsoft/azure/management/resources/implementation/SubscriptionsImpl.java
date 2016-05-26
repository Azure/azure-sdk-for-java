/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;

import java.io.IOException;

/**
 * The implementation of Subscriptions and its nested interfaces.
 */
final class SubscriptionsImpl
        implements Subscriptions {
    private final SubscriptionsInner client;

    SubscriptionsImpl(final SubscriptionsInner client) {
        this.client = client;
    }

    @Override
    public PagedList<Subscription> list() throws CloudException, IOException {
        PagedListConverter<SubscriptionInner, Subscription> converter = new PagedListConverter<SubscriptionInner, Subscription>() {
            @Override
            public Subscription typeConvert(SubscriptionInner subscriptionInner) {
                return new SubscriptionImpl(subscriptionInner, client);
            }
        };
        return converter.convert(client.list().getBody());
    }

    @Override
    // Gets a specific resource group
    public SubscriptionImpl get(String name) throws CloudException, IOException {
        SubscriptionInner subscription = client.get(name).getBody();
        return new SubscriptionImpl(subscription, client);
    }
}
