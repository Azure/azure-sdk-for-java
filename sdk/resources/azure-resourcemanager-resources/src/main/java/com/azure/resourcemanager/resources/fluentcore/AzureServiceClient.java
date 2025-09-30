// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class AzureServiceClient {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final String sdkVersion;

    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;

    private final String sdkName;

    /**
     * Creates a new instance of {@link AzureServiceClient}.
     *
     * @param httpPipeline The HttpPipline used by the client.
     * @param serializerAdapter The SerializerAdapter used by the client.
     * @param environment The AzureEnvironment used by the client.
     */
    protected AzureServiceClient(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
        AzureEnvironment environment) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;

        String packageName = this.getClass().getPackage().getName();
        String implementationSegment = ".implementation";
        if (packageName.endsWith(implementationSegment)) {
            packageName = packageName.substring(0, packageName.length() - implementationSegment.length());
        }
        this.sdkName = packageName;
        if (packageName.startsWith("com.")) {
            // com.azure.resourcemanager.compute -> azure-resourcemanager-compute.properties
            String artifactId = packageName.substring("com.".length()).replace(".", "-");
            Map<String, String> properties = CoreUtils.getProperties(artifactId + ".properties");
            sdkVersion = properties.getOrDefault("version", "UnknownVersion");
        } else {
            sdkVersion = "UnknownVersion";
        }
    }

    /**
     * Gets serializer adapter for JSON serialization/de-serialization.
     *
     * @return the serializer adapter.
     */
    private SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Gets The default poll interval for long-running operation.
     *
     * @return the defaultPollInterval value.
     */
    public abstract Duration getDefaultPollInterval();

    /**
     * Gets default client context.
     *
     * @return the default client context.
     */
    public Context getContext() {
        return new Context("Sdk-Name", sdkName).addData("Sdk-Version", sdkVersion);
    }

    /**
     * Merges default client context with provided context.
     *
     * @param context the context to be merged with default client context.
     * @return the merged context.
     */
    public Context mergeContext(Context context) {
        // data from context would override data from client
        return CoreUtils.mergeContexts(this.getContext(), context);
    }

    /**
     * Gets long running operation result.
     *
     * @param lroInit the raw response of init operation.
     * @param httpPipeline the http pipeline.
     * @param pollResultType type of poll result.
     * @param finalResultType type of final result.
     * @param context the context shared by all requests.
     * @param <T> type of poll result.
     * @param <U> type of final result.
     * @return poller flux for poll result and final result.
     */
    public <T, U> PollerFlux<PollResult<T>, U> getLroResult(Mono<Response<Flux<ByteBuffer>>> lroInit,
        HttpPipeline httpPipeline, Type pollResultType, Type finalResultType, Context context) {
        return PollerFactory.create(getSerializerAdapter(), httpPipeline, pollResultType, finalResultType,
            ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(this.getDefaultPollInterval()), lroInit,
            context);
    }

    /**
     * Gets the final result, or an error, based on last async poll response.
     *
     * @param response the last async poll response.
     * @param <T> type of poll result.
     * @param <U> type of final result.
     * @return the final result, or an error.
     */
    public <T, U> Mono<U> getLroFinalResultOrError(AsyncPollResponse<PollResult<T>, U> response) {
        if (response.getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String errorMessage;
            ManagementError managementError = null;
            HttpResponse errorResponse = null;
            PollResult.Error lroError = response.getValue().getError();
            if (lroError != null) {
                errorResponse = new HttpResponseImpl(lroError.getResponseStatusCode(), lroError.getResponseHeaders(),
                    lroError.getResponseBody());

                errorMessage = response.getValue().getError().getMessage();
                String errorBody = response.getValue().getError().getResponseBody();
                if (errorBody != null) {
                    // try to deserialize error body to ManagementError
                    try {
                        managementError = this.getSerializerAdapter()
                            .deserialize(errorBody, ManagementError.class, SerializerEncoding.JSON);
                        if (managementError.getCode() == null || managementError.getMessage() == null) {
                            managementError = null;
                        }
                    } catch (IOException | RuntimeException ioe) {
                        logger.logThrowableAsWarning(ioe);
                    }
                }
            } else {
                // fallback to default error message
                errorMessage = "Long running operation failed.";
            }
            if (managementError == null) {
                // fallback to default ManagementError
                managementError = new ManagementError(response.getStatus().toString(), errorMessage);
            }
            return Mono.error(new ManagementException(errorMessage, errorResponse, managementError));
        } else {
            return response.getFinalResult();
        }
    }

    private static class HttpResponseImpl extends HttpResponse {
        private final int statusCode;
        private final byte[] responseBody;
        private final HttpHeaders httpHeaders;

        HttpResponseImpl(int statusCode, HttpHeaders httpHeaders, String responseBody) {
            super(null);
            this.statusCode = statusCode;
            this.httpHeaders = httpHeaders;
            this.responseBody = responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String s) {
            return httpHeaders.getValue(s);
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.just(ByteBuffer.wrap(responseBody));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(responseBody);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(new String(responseBody, StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(responseBody, charset));
        }
    }
}
