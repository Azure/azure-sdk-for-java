/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.api;

import com.microsoft.azure.spring.integration.core.api.StartPosition;
import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.core.api.reactor.BatchSendOperation;
import com.microsoft.azure.spring.integration.core.api.reactor.SendOperation;

/**
 * Azure event hub operation to support send data asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface EventHubOperation extends SendOperation, BatchSendOperation, SubscribeByGroupOperation {

    void setStartPosition(StartPosition startPosition);
}
