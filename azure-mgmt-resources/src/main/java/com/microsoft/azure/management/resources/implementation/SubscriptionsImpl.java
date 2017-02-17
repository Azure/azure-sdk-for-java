/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation of Subscriptions.
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
                return wrapModel(subscriptionInner);
            }
        };
        return converter.convert(client.list());
    }

    @Override
    public Observable<Subscription> listAsync() {
        return client.listAsync().map(new Func1<Page<SubscriptionInner>, List<SubscriptionInner>>() {
            @Override
            public List<SubscriptionInner> call(Page<SubscriptionInner> subscriptionInnerPage) {
                return subscriptionInnerPage.items();
            }
        }).flatMap(new Func1<List<SubscriptionInner>, Observable<SubscriptionInner>>() {
            @Override
            public Observable<SubscriptionInner> call(List<SubscriptionInner> subscriptionInners) {
                return Observable.from(subscriptionInners);
            }
        }).map(new Func1<SubscriptionInner, Subscription>() {
            @Override
            public Subscription call(SubscriptionInner subscriptionInner) {
                return wrapModel(subscriptionInner);
            }
        });
    }

    @Override
    // Gets a specific resource group
    public SubscriptionImpl getById(String id) {
        SubscriptionInner subscription = client.get(id);
        if (subscription == null) {
            return null;
        }
        return wrapModel(subscription);
    }

    private SubscriptionImpl wrapModel(SubscriptionInner subscriptionInner) {
        return new SubscriptionImpl(subscriptionInner, client);
    }
}
