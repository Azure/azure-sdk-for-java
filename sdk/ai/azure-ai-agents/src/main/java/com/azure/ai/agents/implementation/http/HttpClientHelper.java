// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.RequestOptions;
import com.openai.core.Timeout;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpClient;
import com.openai.core.http.HttpRequest;
import com.openai.core.http.HttpRequestBody;
import com.openai.core.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Utility entry point that adapts an Azure {@link com.azure.core.http.HttpClient} so it can be consumed by
 * the OpenAI SDK generated clients. The helper performs request/response translation so that existing Azure
 * pipelines, diagnostics, and retry policies can be reused without exposing the Azure HTTP primitives to
 * callers that only understand the OpenAI surface area.
 */
public final class HttpClientHelper {

    private static final ClientLogger LOGGER = new ClientLogger(HttpClientHelper.class);

    private HttpClientHelper() {
    }

    /**
     * Implements the OpenAI {@link HttpClient} interface that sends the HTTP request through the Azure HTTP pipeline.
     * All requests and responses are converted on the fly.
     *
     * @param httpPipeline The Azure HTTP pipeline that will execute HTTP requests.
     * @return A bridge client that honors the OpenAI interface but delegates execution to the Azure pipeline.
     */
    public static HttpClient mapToOpenAIHttpClient(HttpPipeline httpPipeline) {
        return new HttpClientWrapper(httpPipeline);
    }

    private static final class HttpClientWrapper implements HttpClient {

        private final HttpPipeline httpPipeline;

        private HttpClientWrapper(HttpPipeline httpPipeline) {
            this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null.");
        }

        @Override
        public void close() {
            // no-op
        }

        @Override
        public HttpResponse execute(HttpRequest request) {
            return execute(request, RequestOptions.none());
        }

        @Override
        public HttpResponse execute(HttpRequest request, RequestOptions requestOptions) {
            Objects.requireNonNull(request, "request");
            Objects.requireNonNull(requestOptions, "requestOptions");

            com.azure.core.http.HttpRequest azureRequest = buildAzureRequest(request);

            Context requestContext = Context.NONE;
            Timeout timeout = requestOptions.getTimeout();
            if (timeout != null && !timeout.read().isZero() && !timeout.read().isNegative()) {
                requestContext = requestContext.addData("azure-response-timeout", timeout.read());
            }

            com.azure.core.http.HttpResponse azureResponse = this.httpPipeline.sendSync(azureRequest, requestContext);
            return new AzureHttpResponseAdapter(azureResponse);
        }

        @Override
        public CompletableFuture<HttpResponse> executeAsync(HttpRequest request) {
            return executeAsync(request, RequestOptions.none());
        }

        @Override
        public CompletableFuture<HttpResponse> executeAsync(HttpRequest request, RequestOptions requestOptions) {
            Objects.requireNonNull(request, "request");
            Objects.requireNonNull(requestOptions, "requestOptions");

            final com.azure.core.http.HttpRequest azureRequest = buildAzureRequest(request);

            Context requestContext = new Context("azure-eagerly-read-response", true);
            Timeout timeout = requestOptions.getTimeout();
            if (timeout != null && !timeout.read().isZero() && !timeout.read().isNegative()) {
                requestContext = requestContext.addData("azure-response-timeout", timeout.read());
            }

            return this.httpPipeline.send(azureRequest, requestContext)
                .map(response -> (HttpResponse) new AzureHttpResponseAdapter(response))
                //                    .onErrorMap(t -> {
                //                        // 2 or 3 from Azure Errors, should be mapped to Stainless Error.
                //                        // - Auth
                //                        // - Resource not found
                //                        // - HttpResponse Ex
                //                        //
                //                        // new StainlessException(t.getCause())
                //                    })
                .toFuture();
        }

        /**
         * Converts the OpenAI request metadata and body into an Azure {@link com.azure.core.http.HttpRequest}.
         */
        private static com.azure.core.http.HttpRequest buildAzureRequest(HttpRequest request) {
            HttpRequestBody requestBody = request.body();
            String contentType = requestBody != null ? requestBody.contentType() : null;
            BinaryData bodyData = null;

            if (requestBody != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                requestBody.writeTo(outputStream);
                bodyData = BinaryData.fromBytes(outputStream.toByteArray());
            }

            HttpHeaders headers = toAzureHeaders(request.headers());
            if (!CoreUtils.isNullOrEmpty(contentType) && headers.getValue(HttpHeaderName.CONTENT_TYPE) == null) {
                headers.set(HttpHeaderName.CONTENT_TYPE, contentType);
            }

            com.azure.core.http.HttpRequest azureRequest = new com.azure.core.http.HttpRequest(
                HttpMethod.valueOf(request.method().name()), OpenAiRequestUrlBuilder.buildUrl(request), headers);

            if (bodyData != null) {
                azureRequest.setBody(bodyData);
            }

            return azureRequest;
        }

        /**
         * Copies OpenAI headers into an {@link HttpHeaders} instance so the Azure pipeline can process them.
         */
        private static HttpHeaders toAzureHeaders(Headers sourceHeaders) {
            HttpHeaders target = new HttpHeaders();
            sourceHeaders.names().forEach(name -> {
                List<String> values = sourceHeaders.values(name);
                HttpHeaderName headerName = HttpHeaderName.fromString(name);
                if (values.isEmpty()) {
                    target.set(headerName, "");
                } else {
                    target.set(headerName, values);
                }
            });
            return target;
        }

    }
}
