// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.api;

import com.azure.spring.integration.core.api.RxSendOperation;
import com.azure.spring.integration.core.api.RxSubscribeByGroupOperation;
import com.azure.spring.integration.core.api.StartPosition;

/**
 * Azure event hub operation to support send and subscribe in a reactive way
 *
 * @author Warren Zhu
 *
 * @deprecated {@link rx} API will be dropped in version 4.x, please migrate to reactor API in
 * {@link EventHubOperation}. From version 4.0.0, {@link EventHubOperation} will be dropped
 * and use com.azure.spring.messaging.core.SendOperation instead.
 */
@Deprecated
public interface EventHubRxOperation extends RxSendOperation, RxSubscribeByGroupOperation {

    @Deprecated
    void setStartPosition(StartPosition startPosition);
}
