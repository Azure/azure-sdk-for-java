// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Type represents a Provider that when called return a Function to retrieve pages.
 *
 * @param <P> the Page type.
 */
@FunctionalInterface
public interface PageRetrieverProvider<P> extends Supplier<Function<String, Flux<P>>> {
}
