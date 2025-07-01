// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Helper class for tests involving the HTTP pipeline.
 */
public final class PipelineTestHelpers {
    public static final HttpHeaderName TRACESTATE = HttpHeaderName.fromString("tracestate");
    public static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    /**
     * Sends a request using the provided HTTP pipeline.
     * <p>
     * Sends a GET request to {@code "http://localhost/"} using the specified HTTP pipeline.
     *
     * @param pipeline the HTTP pipeline to use for sending the request
     * @param isAsync whether to send the request asynchronously or synchronously
     * @return the response from the HTTP request
     */
    public static Response<BinaryData> sendRequest(HttpPipeline pipeline, boolean isAsync) {
        return sendRequest(pipeline, new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost/"), isAsync);
    }

    /**
     * Sends a request using the provided HTTP pipeline.
     *
     * @param pipeline the HTTP pipeline to use for sending the request
     * @param request the HTTP request to send
     * @param isAsync whether to send the request asynchronously or synchronously
     * @return the response from the HTTP request
     */
    public static Response<BinaryData> sendRequest(HttpPipeline pipeline, HttpRequest request, boolean isAsync) {
        if (isAsync) {
            try {
                return pipeline.sendAsync(request).get();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        } else {
            return pipeline.send(request);
        }
    }

    /**
     * Creates a basic HTTP client that uses the provided function to send requests.
     * <p>
     * This only implements the {@link HttpClient#send(HttpRequest)} method and leaves
     * {@link HttpClient#sendAsync(HttpRequest)} to use the default implementation.
     *
     * @param sendFunction The function that sends the HTTP request and returns a response.
     * @return A new instance of {@link HttpClient} that uses the provided function to send requests.
     */
    public static HttpClient createBasicHttpClient(Function<HttpRequest, Response<BinaryData>> sendFunction) {
        return new BasicHttpClient(sendFunction);
    }

    private static final class BasicHttpClient implements HttpClient {
        private final Function<HttpRequest, Response<BinaryData>> sendFunction;

        BasicHttpClient(Function<HttpRequest, Response<BinaryData>> sendFunction) {
            this.sendFunction = sendFunction;
        }

        @Override
        public Response<BinaryData> send(HttpRequest request) {
            return sendFunction.apply(request);
        }
    }

    private PipelineTestHelpers() {
        // Prevent instantiation
    }
}
