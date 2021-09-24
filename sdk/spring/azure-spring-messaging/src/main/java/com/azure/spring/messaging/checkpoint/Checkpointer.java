// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

import java.util.concurrent.CompletableFuture;

/**
 * A callback to perform checkpoint.
 *
 * @author Warren Zhu
 */
public interface Checkpointer {

    /**
     * Acknowledge success of current message. Please check result to detect failure
     * @return completable future instance
     */
    CompletableFuture<Void> success();

    /**
     * Fail current message. Please check result to detect failure
     * @return completable future instance
     */
    CompletableFuture<Void> failure();



}
