// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.azure.resourcemanager.servicebus.models.AuthorizationRule;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.springframework.lang.NonNull;

/**
 * Get connection string for Event Hub namespace.
 */
public class ServiceBusConnectionStringProvider {

    private final String connectionString;

    public ServiceBusConnectionStringProvider(@NonNull ServiceBusNamespace serviceBusNamespace) {
        this(buildConnectionString(serviceBusNamespace));
    }

    public ServiceBusConnectionStringProvider(@NonNull String connectionString) {
        this.connectionString = connectionString;
    }

    public String getConnectionString() {
        return this.connectionString;
    }

    @SuppressWarnings("rawtypes")
    private static String buildConnectionString(ServiceBusNamespace serviceBusNamespace) {
        return serviceBusNamespace.authorizationRules()
                                  .list()
                                  .stream()
                                  .findFirst()
                                  .map(AuthorizationRule::getKeys)
                                  .map(AuthorizationKeys::primaryConnectionString)
                                  .map(s -> new ConnectionStringBuilder(s, serviceBusNamespace.name()).toString())
                                  .orElseThrow(
                                      () -> new RuntimeException(
                                          String.format("Service bus namespace '%s' key is empty",
                                              serviceBusNamespace.name()), null));
    }

}
