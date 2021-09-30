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
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link EventHubOperation}. From version 4.0.0, the reactor API support will be moved to
 * com.azure.spring.eventhubs.core.EventHubOperation.
 */
@Deprecated
public interface EventHubRxOperation extends RxSendOperation, RxSubscribeByGroupOperation {

    @Deprecated
    void setStartPosition(StartPosition startPosition);
}
