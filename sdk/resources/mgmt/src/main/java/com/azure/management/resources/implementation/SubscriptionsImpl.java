/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Subscription;
import com.azure.management.resources.Subscriptions;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.fluentcore.arm.collection.implementation.SupportsGettingByIdImpl;
import com.azure.management.resources.models.SubscriptionInner;
import com.azure.management.resources.models.SubscriptionsInner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The implementation of Subscriptions.
 */
final class SubscriptionsImpl
        extends SupportsGettingByIdImpl<Subscription>
        implements Subscriptions {
    private final SubscriptionsInner client;

    SubscriptionsImpl(final SubscriptionsInner client) {
        this.client = client;
    }

    @Override
    public PagedIterable<Subscription> list() {
        return client.list().mapPage(inner -> wrapModel(inner));
    }


    @Override
    public Mono<Subscription> getByIdAsync(String id) {
        return client.getAsync(id)
                .map(inner -> wrapModel(inner));
    }

    @Override
    public PagedFlux<Subscription> listAsync() {
        return client.listAsync().mapPage(inner -> wrapModel(inner));
    }

    private SubscriptionImpl wrapModel(SubscriptionInner subscriptionInner) {
        if (subscriptionInner == null) {
            return null;
        }
        return new SubscriptionImpl(subscriptionInner, client);
    }
}
