// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.properties;

/**
 * Azure Service Bus related properties.
 */
public interface ServiceBusNamespaceProperties extends ServiceBusClientCommonProperties {

    Boolean getCrossEntityTransactions();

}
