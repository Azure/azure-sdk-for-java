// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.spring.cloud.context.core.util.Tuple;

import java.util.Map;
import java.util.stream.Collectors;

public class DefaultEventHubClientFactoryInspector {

    private DefaultEventHubClientFactory clientFactory;

    public DefaultEventHubClientFactoryInspector(DefaultEventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public Map<Tuple<String, String>, Boolean> getProcessorClientStatus() {
        return clientFactory.getProcessorClientMap()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().isRunning()));
    }

}
