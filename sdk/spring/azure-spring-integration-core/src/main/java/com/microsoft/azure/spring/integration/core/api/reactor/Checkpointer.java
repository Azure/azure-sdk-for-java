// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api.reactor;

import reactor.core.publisher.Mono;

/**
 * A callback to perform checkpoint.
 *
 * @author Xiaolu Dai
 */
public interface Checkpointer {

    /**
     * Acknowledge success of current message. Please check result to detect failure
     * @return Mono Void
     */
    Mono<Void> success();

    /**
     * Fail current message. Please check result to detect failure
     * @return Mono Void
     */
    Mono<Void> failure();
}
