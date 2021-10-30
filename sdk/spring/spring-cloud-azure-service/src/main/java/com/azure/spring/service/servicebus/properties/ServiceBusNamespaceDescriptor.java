// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

/**
 * Azure Service Bus related properties.
 */
public interface ServiceBusNamespaceDescriptor extends ServiceBusCommonDescriptor {

    Boolean getCrossEntityTransactions();

}
