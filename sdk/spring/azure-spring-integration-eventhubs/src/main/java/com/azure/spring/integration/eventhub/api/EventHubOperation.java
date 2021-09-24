// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.api;

import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.azure.spring.integration.core.api.reactor.BatchSendOperation;
import com.azure.spring.integration.core.api.reactor.SendOperation;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * Azure event hub operation to support send data asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface EventHubOperation extends SendOperation, BatchSendOperation, SubscribeByGroupOperation {

    void setStartPosition(StartPosition startPosition);

    void addListener(Listener listener);

    public List<Listener> getListeners();

    interface Listener {

        default void eventHubTemplateCreated(String namespace,EventHubOperation eventHubOperation) {
        }

    }
}
