// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

/**
 * Azure Event Hubs related properties.
 */
public interface EventHubsNamespaceDescriptor extends EventHubsCommonDescriptor {

    Boolean getSharedConnection();

}
