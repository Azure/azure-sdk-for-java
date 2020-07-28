/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import java.util.concurrent.CompletableFuture;

/**
 * A callback to perform checkpoint.
 *
 * @author Warren Zhu
 */
public interface Checkpointer {

    /**
     * Acknowledge success of current message. Please check result to detect failure
     */
    CompletableFuture<Void> success();

    /**
     * Fail current message. Please check result to detect failure
     */
    CompletableFuture<Void> failure();
}
