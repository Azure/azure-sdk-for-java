// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.exceptions.ObserverException;

/**
 * DEFAULT implementation for {@link ChangeFeedObserverFactory}.
 */
public class ChangeFeedObserverFactoryImpl<T> implements ChangeFeedObserverFactory<T> {
    private final Class<? extends ChangeFeedObserver<T>> observerType;

    public ChangeFeedObserverFactoryImpl(Class<? extends ChangeFeedObserver<T>> observerType) {
        this.observerType = observerType;
    }

    @Override
    public ChangeFeedObserver<T> createObserver() {
        try {
            return observerType.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ObserverException(ex);
        }
    }
}
