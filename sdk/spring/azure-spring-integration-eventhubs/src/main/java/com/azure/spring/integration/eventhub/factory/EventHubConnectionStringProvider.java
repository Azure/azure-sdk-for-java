// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.resourcemanager.eventhubs.models.AuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.spring.integration.eventhub.impl.EventHubRuntimeException;
import org.springframework.lang.NonNull;

/**
 * Get connection string for Event Hub namespace.
 */
public class EventHubConnectionStringProvider {

    private final String connectionString;

    public EventHubConnectionStringProvider(@NonNull EventHubNamespace eventHubNamespace) {
        this(toConnectionString(eventHubNamespace));
    }

    public EventHubConnectionStringProvider(@NonNull String connectionString) {
        this.connectionString = connectionString;
    }

    @SuppressWarnings("rawtypes")
    private static String toConnectionString(EventHubNamespace eventHubNamespace) {
        return eventHubNamespace.listAuthorizationRules()
                                .stream()
                                .findFirst()
                                .map(AuthorizationRule::getKeys)
                                .map(EventHubAuthorizationKey::primaryConnectionString)
                                .orElseThrow(() -> new EventHubRuntimeException(
                                    String.format("Failed to fetch connection string of namespace '%s'",
                                        eventHubNamespace.name()), null));
    }

    public String getConnectionString() {
        return this.connectionString;
    }

}
