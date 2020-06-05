// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClientBuilder;

/**
 *
 */
@ServiceClientBuilder(serviceClients = {ServiceBusManagementClient.class, ServiceBusManagementAsyncClient.class})
public class ServiceBusManagementClientBuilder {
}
