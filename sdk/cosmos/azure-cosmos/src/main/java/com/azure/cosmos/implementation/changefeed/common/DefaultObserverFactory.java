// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.azure.cosmos.ChangeFeedProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultObserverFactory<T> implements ChangeFeedObserverFactory<T> {
    private final Logger log = LoggerFactory.getLogger(DefaultObserverFactory.class);
    private final Consumer<List<T>> consumer;
    private final BiConsumer<List<T>, ChangeFeedProcessorContext> biConsumer;

    public DefaultObserverFactory(Consumer<List<T>> consumer) {
        this.consumer = consumer;
        this.biConsumer = null;
    }

    public DefaultObserverFactory(BiConsumer<List<T>, ChangeFeedProcessorContext> biConsumer) {
        this.biConsumer = biConsumer;
        this.consumer = null;
    }

    @Override
    public ChangeFeedObserver<T> createObserver() {

        if (consumer != null) {
            return new DefaultObserver<>(consumer);
        } else if (biConsumer != null) {
            return new DefaultObserver<>(biConsumer);
        }

        throw new IllegalStateException("Both consumer and biConsumer cannot be null");
    }
}
