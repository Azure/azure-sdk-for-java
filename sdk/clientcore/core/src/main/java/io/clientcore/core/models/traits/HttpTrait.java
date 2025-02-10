// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.traits;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpRedirectOptions;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;

/**
 * A {@link io.clientcore.core.models.traits trait} providing a consistent interface for configuration of HTTP-specific
 * settings. Refer to the
 * <a href="https://aka.ms/azsdk/java/docs/http-client-pipeline">HTTP clients and pipelines</a> documentation for more
 * details on proper usage and configuration of HTTP clients.
 *
 * <p>It is important to understand the precedence order of the {@link HttpTrait} APIs. In particular, if an
 * {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and they will be ignored.
 * If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally based on the settings
 * provided to this trait. Additionally, there may be other APIs in types that implement this trait that are also
 * ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the documentation of types that
 * implement this trait to understand the full set of implications.</p>
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 * return the concrete type, rather than the trait type.
 *
 * @see io.clientcore.core.models.traits
 * @see HttpClient
 * @see HttpPipeline
 * @see HttpPipelinePolicy
 * @see HttpInstrumentationOptions
 * @see HttpRetryOptions
 * @see HttpRedirectOptions
 */
public interface HttpTrait<T extends HttpTrait<T>> {
    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param client The {@link HttpClient} to use for requests.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T httpClient(HttpClient client);

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T httpPipeline(HttpPipeline pipeline);

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     *
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    T addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy);

    /**
     * Sets the {@link HttpRetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param retryOptions The {@link HttpRetryOptions} to use for all the requests made through the client.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T httpRetryOptions(HttpRetryOptions retryOptions);

    /**
     * Sets the {@link HttpInstrumentationOptions instrumentation configuration} to use when recording telemetry about HTTP
     * requests sent to the service and responses received from it.
     * <p>
     * By default, when instrumentation options are not provided (explicitly or via environment variables), the following
     * defaults are used:
     * <ul>
     *     <li>Detailed HTTP logging about requests and responses is disabled</li>
     *     <li>Distributed tracing is enabled. If OpenTelemetry is found on the classpath, HTTP requests are
     *     captured as OpenTelemetry spans.
     *     If OpenTelemetry is not found on the classpath, the same information is captured in logs.
     *     HTTP request spans contain basic information about the request, such as the HTTP method, URL, status code and
     *     duration.
     *     See {@link io.clientcore.core.http.pipeline.HttpInstrumentationPolicy} for
     *     the details.</li>
     * </ul>
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param instrumentationOptions The {@link HttpInstrumentationOptions configuration} to use when recording telemetry about HTTP
     * requests sent to the service and responses received from it.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T httpInstrumentationOptions(HttpInstrumentationOptions instrumentationOptions);

    /**
     * Sets the {@link HttpRedirectOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the {@link HttpTrait} APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param redirectOptions The {@link HttpRedirectOptions} to use for all the requests made through the client.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T httpRedirectOptions(HttpRedirectOptions redirectOptions);
}
