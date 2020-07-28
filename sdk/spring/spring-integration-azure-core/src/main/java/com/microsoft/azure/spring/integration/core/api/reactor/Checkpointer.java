/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

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
     */
    Mono<Void> success();

    /**
     * Fail current message. Please check result to detect failure
     */
    Mono<Void> failure();
}
