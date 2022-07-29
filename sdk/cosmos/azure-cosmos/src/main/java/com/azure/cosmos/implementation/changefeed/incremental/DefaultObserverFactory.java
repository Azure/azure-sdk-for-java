// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

class DefaultObserverFactory implements ChangeFeedObserverFactory {
    private final Logger log = LoggerFactory.getLogger(DefaultObserverFactory.class);

    private Consumer<List<JsonNode>> consumer;

    public DefaultObserverFactory(Consumer<List<JsonNode>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ChangeFeedObserver createObserver() {
        return new DefaultObserver(consumer);
    }
}
