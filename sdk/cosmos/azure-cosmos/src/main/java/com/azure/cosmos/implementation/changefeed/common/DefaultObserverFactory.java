// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class DefaultObserverFactory<T> implements ChangeFeedObserverFactory<T> {
    private final Logger log = LoggerFactory.getLogger(DefaultObserverFactory.class);

    private final Consumer<List<T>> consumer;

    public DefaultObserverFactory(Consumer<List<T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ChangeFeedObserver<T> createObserver() {
        return new DefaultObserver<>(consumer);
    }
}
