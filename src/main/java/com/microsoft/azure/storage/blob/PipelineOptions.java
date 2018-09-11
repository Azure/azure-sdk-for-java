/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpPipelineLogger;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private HttpPipelineLogger logger;

    private RequestRetryOptions requestRetryOptions = RequestRetryOptions.DEFAULT;

    private LoggingOptions loggingOptions = LoggingOptions.DEFAULT;

    private TelemetryOptions telemetryOptions = TelemetryOptions.DEFAULT;

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
     * Specifies the logger for the pipeline.
     */
    public HttpPipelineLogger logger() {
        return logger;
    }

    /**
     * Specifies the logger for the pipeline.
     */
    public PipelineOptions withLogger(HttpPipelineLogger logger) {
        this.logger = logger;
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

    /**
     * Returns a {@code PipelineOptions} object with default values for each of the options fields. An
     * {@link HttpClient} must still be set explicitly, however.
     *
     * @apiNote
     * ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=pipeline_options "Sample code for PipelineOptions constructor")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/New-Storage-SDK-V10-Preview/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public PipelineOptions() {
        this.logger = new HttpPipelineLogger() {
            @Override
            public HttpPipelineLogLevel minimumLogLevel() {
                return HttpPipelineLogLevel.OFF;
            }

            // TODO: Revisit
            @Override
            public void log(HttpPipelineLogLevel logLevel, String s, Object... objects) {
                if (logLevel == HttpPipelineLogLevel.INFO) {
                    Logger.getGlobal().info(String.format(Locale.ROOT, s, objects));
                } else if (logLevel == HttpPipelineLogLevel.WARNING) {
                    Logger.getGlobal().warning(String.format(Locale.ROOT, s, objects));
                } else if (logLevel == HttpPipelineLogLevel.ERROR) {
                    Logger.getGlobal().severe(String.format(Locale.ROOT, s, objects));
                }
            }
        };
    }
}
