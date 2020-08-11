// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
