// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.spring.messaging.core.reactor.ReceiveOperation;
import com.azure.spring.messaging.core.reactor.SendOperation;

/**
 * Azure storage queue operation to support send and receive
 * {@link org.springframework.messaging.Message} asynchronously
 * <p>
 * You should checkpoint if message has been processed successfully, otherwise it will be visible again after certain
 * time specified by {@link #setVisibilityTimeoutInSeconds(int)}.
 *
 * @author Miao Cao
 * @author Warren Zhu
 */
public interface StorageQueueOperation extends SendOperation, ReceiveOperation {

    /**
     * Set visibility timeout. Default is 30
     *
     * @param visibilityTimeoutInSeconds visibility timeout
     */
    void setVisibilityTimeoutInSeconds(int visibilityTimeoutInSeconds);
}
