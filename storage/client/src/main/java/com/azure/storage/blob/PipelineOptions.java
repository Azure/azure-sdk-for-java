// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;

/**
 * This type encapsulates all the possible configuration for the default pipeline. It may be passed to the
 * createPipeline method on {@link StorageURL}. All the options fields have default values if nothing is passed, and
 * no logger will be used if it is not set. An HttpClient must be set, however.
 */
public final class PipelineOptions {
    /*
     PipelineOptions is mutable, but its fields refer to immutable objects. The createPipeline method can pass the
     fields to other methods, but the PipelineOptions object itself can only be used for the duration of this call; it
     must not be passed to anything with a longer lifetime.
     */

    private HttpClient client;

    private RequestRetryOptions requestRetryOptions = new RequestRetryOptions();

    private LoggingOptions loggingOptions = new LoggingOptions();

    private TelemetryOptions telemetryOptions = new TelemetryOptions();


    /**
     * Specifies which HttpClient to use to send the requests.
     */
    public HttpClient client() {
        return client;
    }

    /**
     * Specifies which HttpClient to use to send the requests.
     */
    public PipelineOptions withClient(HttpClient client) {
        this.client = client;
        return this;
    }

    /**
     * Configures the retry policy's behavior.
     */
    public RequestRetryOptions requestRetryOptions() {
        return requestRetryOptions;
    }

    /**
     * Configures the retry policy's behavior.
     */
    public PipelineOptions withRequestRetryOptions(RequestRetryOptions requestRetryOptions) {
        this.requestRetryOptions = requestRetryOptions;
        return this;
    }

    /**
     * Configures the built-in request logging policy.
     */
    public LoggingOptions loggingOptions() {
        return loggingOptions;
    }

    /**
     * Configures the built-in request logging policy.
     */
    public PipelineOptions withLoggingOptions(LoggingOptions loggingOptions) {
        this.loggingOptions = loggingOptions;
        return this;
    }

    /**
     * Configures the built-in telemetry policy behavior.
     */
    public TelemetryOptions telemetryOptions() {
        return telemetryOptions;
    }

    /**
     * Configures the built-in telemetry policy behavior.
     */
    public PipelineOptions withTelemetryOptions(TelemetryOptions telemetryOptions) {
        this.telemetryOptions = telemetryOptions;
        return this;
    }
}
