// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>This package contains classes and interfaces that provide RESTful HTTP functionality for Azure SDKs.</p>
 *
 * <p>The classes in this package allow you to send HTTP requests to Azure services and handle the responses. They also
 * provide functionality for handling paged responses from Azure services, which is useful when dealing with large
 * amounts of data.</p>
 *
 * <p>Here are some of the key classes included in this package:</p>
 *
 * <ul>
 *     <li>{@link com.azure.core.http.rest.ResponseBase}: The base class for all responses of a REST request.</li>
 *     <li>{@link com.azure.core.http.rest.PagedIterable}: Provides utility to iterate over {@link PagedResponse} using
 *     {@link Stream} and {@link Iterable} interfaces.</li>
 *     <li>{@link com.azure.core.http.rest.PagedFlux}: Provides utility to iterate over {@link PagedResponse} using
 *     {@link Flux} and {@link Iterable} interfaces.</li>
 *     <li>{@link com.azure.core.http.rest.PagedFluxBase}: The base class for {@link PagedFlux}.</li>
 *     <li>{@link com.azure.core.http.rest.PagedIterableBase}: The base class for {@link PagedIterable}.</li>
 *     <li>{@link com.azure.core.http.rest.PagedResponseBase}: The base class for {@link PagedResponse}.</li>
 *     <li>{@link com.azure.core.http.rest.SimpleResponse}: Represents a REST response with a strongly-typed content
 *     deserialized from the response body.</li>
 * </ul>
 *
 * <p>Each class provides useful methods and functionality for dealing with HTTP requests and responses. For example,
 * the {@link com.azure.core.http.rest.PagedIterable} class provides methods for iterating over paged responses from
 * Azure services.</p>
 */
package com.azure.core.http.rest;
