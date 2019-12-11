package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Type represents a provider that when called return a Function to retrieve pages.
 *
 * @param <P> the Page type.
 */
@FunctionalInterface
public interface PageRetrieverProvider<P> extends Supplier<Function<String, Flux<P>>> {
}
