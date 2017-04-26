/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.SupportsGettingByIdImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Observable;
import rx.functions.Func1;

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
    public PagedList<Subscription> list() {
        PagedListConverter<SubscriptionInner, Subscription> converter = new PagedListConverter<SubscriptionInner, Subscription>() {
            @Override
            public Subscription typeConvert(SubscriptionInner subscriptionInner) {
                return wrapModel(subscriptionInner);
            }
        };
        return converter.convert(client.list());
    }


    @Override
    public Observable<Subscription> getByIdAsync(String id) {
        return client.getAsync(id).map(new Func1<SubscriptionInner, Subscription>() {
            @Override
            public Subscription call(SubscriptionInner subscriptionInner) {
                return wrapModel(subscriptionInner);
            }
        });
    }

    @Override
    public Observable<Subscription> listAsync() {
        return ReadableWrappersImpl.convertPageToInnerAsync(client.listAsync()).map(new Func1<SubscriptionInner, Subscription>() {
            @Override
            public Subscription call(SubscriptionInner subscriptionInner) {
                return wrapModel(subscriptionInner);
            }
        });
    }

    private SubscriptionImpl wrapModel(SubscriptionInner subscriptionInner) {
        if (subscriptionInner == null) {
            return null;
        }
        return new SubscriptionImpl(subscriptionInner, client);
    }
}
