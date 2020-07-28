/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.microsoft.azure.spring.integration.core.api.reactor.ReceiveOperation;
import com.microsoft.azure.spring.integration.core.api.reactor.SendOperation;

/**
 * Azure storage queue operation to support send and receive
 * {@link org.springframework.messaging.Message} asynchronously
 *
 * You should checkpoint if message has been processed successfully, otherwise it will be visible again after certain
 * time specified by {@link #setVisibilityTimeoutInSeconds(int)}.
 *
 * @author Miao Cao
 * @author Warren Zhu
 */
public interface StorageQueueOperation extends SendOperation, ReceiveOperation {

    /**
     * Set visibility timeout. Default is 30
     */
    void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds);
}
