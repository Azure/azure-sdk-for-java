// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

class DefaultObserverFactory implements ChangeFeedObserverFactory {
    private final Logger log = LoggerFactory.getLogger(DefaultObserverFactory.class);

    private Consumer<List<CosmosItemProperties>> consumer;

    public DefaultObserverFactory(Consumer<List<CosmosItemProperties>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ChangeFeedObserver createObserver() {
        return new DefaultObserver(consumer);
    }
}
