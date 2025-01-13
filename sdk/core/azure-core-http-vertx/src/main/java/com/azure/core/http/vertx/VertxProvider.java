// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import io.vertx.core.Vertx;

/**
 * Service provider interface providing platforms and applications the means to have their own managed {@link Vertx} be
 * resolved by the {@link VertxHttpClientBuilder}.
 */
public interface VertxProvider {

    /**
     * Creates a {@link Vertx}. Could either be the result of returning {@code Vertx.vertx()}, or returning a
     * {@link Vertx} that was resolved from a dependency injection framework like Spring or CDI.
     *
     * @return The created {@link Vertx}.
     */
    Vertx createVertx();
}
