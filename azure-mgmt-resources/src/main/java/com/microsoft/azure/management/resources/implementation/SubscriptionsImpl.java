/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;

/**
 * The implementation of {@link Subscriptions}.
 */
final class SubscriptionsImpl
        implements Subscriptions {
    private final SubscriptionsInner client;

    SubscriptionsImpl(final SubscriptionsInner client) {
        this.client = client;
    }

    @Override
    public PagedList<Subscription> list() {
        PagedListConverter<SubscriptionInner, Subscription> converter = new PagedListConverter<SubscriptionInner, Subscription>() {
            @Override
            public Subscription typeConvert(SubscriptionInner subscriptionInner) {
                return new SubscriptionImpl(subscriptionInner, client);
            }
        };
        return converter.convert(client.list());
    }

    @Override
    // Gets a specific resource group
    public SubscriptionImpl getByName(String name) {
        SubscriptionInner subscription = client.get(name);
        if (subscription == null) {
            return null;
        }
        return new SubscriptionImpl(subscription, client);
    }
}
