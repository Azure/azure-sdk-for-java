// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.properties;

/**
 * Azure Event Hub related properties.
 */
public interface EventHubNamespaceDescriptor extends EventHubCommonDescriptor {

    Boolean getSharedConnection();

}
