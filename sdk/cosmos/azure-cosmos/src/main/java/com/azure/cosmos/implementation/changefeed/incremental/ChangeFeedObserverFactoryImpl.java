// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.implementation.changefeed.exceptions.ObserverException;

/**
 * DEFAULT implementation for {@link ChangeFeedObserverFactory}.
 */
public class ChangeFeedObserverFactoryImpl implements ChangeFeedObserverFactory {
    private final Class<? extends ChangeFeedObserver> observerType;

    public ChangeFeedObserverFactoryImpl(Class<? extends ChangeFeedObserver> observerType) {
        this.observerType = observerType;
    }

    @Override
    public ChangeFeedObserver createObserver() {
        try {
            return observerType.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ObserverException(ex);
        }
    }
}
