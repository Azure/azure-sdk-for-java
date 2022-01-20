// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import java.util.concurrent.CompletableFuture;

/**
 * A callback to perform checkpoint.
 *
 * @author Warren Zhu
 * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link com.azure.spring.integration.core.api.reactor.Checkpointer}. From version 4.0.0, the reactor PAI support will
 * be move to com.azure.spring.messaging.core.checkpoint.Checkpointer.
 */
@Deprecated
public interface Checkpointer {

    /**
     * Acknowledge success of current message. Please check result to detect failure
     * @return completable future instance
     *
     * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.Checkpointer}. From version 4.0.0, the reactor PAI support will
     * be move to com.azure.spring.messaging.core.checkpoint.Checkpointer.
     */
    @Deprecated
    CompletableFuture<Void> success();

    /**
     * Fail current message. Please check result to detect failure
     * @return completable future instance
     *
     * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.Checkpointer}. From version 4.0.0, the reactor PAI support will
     * be move to com.azure.spring.messaging.core.checkpoint.Checkpointer.
     */
    @Deprecated
    CompletableFuture<Void> failure();



}
