// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.spring.eventhubs.support.StartPosition;
import com.azure.spring.messaging.core.RxSendOperation;
import com.azure.spring.messaging.core.RxSubscribeByGroupOperation;

/**
 * Azure event hub operation to support send and subscribe in a reactive way
 *
 * @author Warren Zhu
 */
public interface EventHubRxOperation extends RxSendOperation, RxSubscribeByGroupOperation {

    void setStartPosition(StartPosition startPosition);
}
