// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

/**
 * Represents a messaging client to an Azure Service Bus entity.
 *
 * @since 1.0
 */
public interface IMessageEntityClient extends ICloseable {
    /**
     * Gets the path of the entity this client is sending messages to or receiving messages from.
     *
     * @return path of the entity this client is connecting to
     */
    String getEntityPath();
}
