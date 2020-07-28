/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.api;

import com.microsoft.azure.spring.integration.core.api.RxSendOperation;
import com.microsoft.azure.spring.integration.core.api.RxSubscribeByGroupOperation;
import com.microsoft.azure.spring.integration.core.api.StartPosition;

/**
 * Azure event hub operation to support send and subscribe in a reactive way
 *
 * @author Warren Zhu
 */
public interface EventHubRxOperation extends RxSendOperation, RxSubscribeByGroupOperation {

    void setStartPosition(StartPosition startPosition);
}
