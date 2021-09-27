// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.spring.eventhubs.support.StartPosition;
import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.messaging.core.BatchSendOperation;
import com.azure.spring.messaging.core.SendOperation;

/**
 * Azure event hub operation to support send data asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface EventHubOperation extends SendOperation, BatchSendOperation, SubscribeByGroupOperation {

    void setStartPosition(StartPosition startPosition);
}
