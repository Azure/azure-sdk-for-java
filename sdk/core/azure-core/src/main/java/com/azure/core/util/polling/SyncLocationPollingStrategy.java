// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollingConstants;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;
import static com.azure.core.util.polling.implementation.PollingUtils.serializeResponseSync;

/**
 * Implements a synchronous Location polling strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public class SyncLocationPollingStrategy<T, U> implements SyncPollingStrategy<T, U> {
    private static final ObjectSerializer DEFAULT_SERIALIZER = new DefaultJsonSerializer();

    private static final ClientLogger LOGGER = new ClientLogger(SyncLocationPollingStrategy.class);

    private final String endpoint;
    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final Context context;

    /**
     * Creates an instance of the location polling strategy using a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public SyncLocationPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, DEFAULT_SERIALIZER, Context.NONE);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public SyncLocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer) {
        this(httpPipeline, serializer, Context.NONE);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param context an instance of {@link Context}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public SyncLocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, Context context) {
        this(httpPipeline, null, serializer, context);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param context an instance of {@link Context}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public SyncLocationPollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
        Context context) {
        this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
        this.endpoint = endpoint;
        this.serializer = (serializer == null) ? DEFAULT_SERIALIZER : serializer;
        this.context = context == null ? Context.NONE : context;
    }

    @Override
    public boolean canPoll(Response<?> initialResponse) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(HttpHeaderName.LOCATION);
        if (locationHeader != null) {
            try {
                new URL(getAbsolutePath(locationHeader.getValue(), endpoint, LOGGER));
                return true;
            } catch (MalformedURLException e) {
                LOGGER.info("Failed to parse Location header into a URL.", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);
        if (locationHeader != null) {
            pollingContext.setData(PollingConstants.LOCATION,
                getAbsolutePath(locationHeader.getValue(), endpoint, LOGGER));
        }
        pollingContext.setData(PollingConstants.HTTP_METHOD, response.getRequest().getHttpMethod().name());
        pollingContext.setData(PollingConstants.REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201
            || response.getStatusCode() == 202 || response.getStatusCode() == 204) {
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                PollingUtils.convertResponseSync(response.getValue(), serializer, pollResponseType), retryAfter);
        }

        throw LOGGER.logExceptionAsError(new AzureException(String.format(
            "Operation failed or cancelled with status code %d, 'Location' header: %s, and response body: %s",
            response.getStatusCode(), locationHeader, serializeResponseSync(response.getValue(), serializer))));
    }

    @Override
    public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(PollingConstants.LOCATION));


        try (HttpResponse response = httpPipeline.sendSync(request, context)) {
            HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);

            if (locationHeader != null) {
                pollingContext.setData(PollingConstants.LOCATION, locationHeader.getValue());
            }

            LongRunningOperationStatus status;
            if (response.getStatusCode() == 202) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (response.getStatusCode() >= 200 && response.getStatusCode() <= 204) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else {
                status = LongRunningOperationStatus.FAILED;
            }

            BinaryData responseBody = response.getBodyAsBinaryData();
            pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, responseBody.toString());
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);

            return new PollResponse<>(status,
                PollingUtils.deserializeResponseSync(responseBody, serializer, pollResponseType), retryAfter);
        }
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long-running operation failed."));
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long-running operation cancelled."));
        }

        String finalGetUrl;
        String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
        if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
            || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(PollingConstants.REQUEST_URL);
        } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
        } else {
            throw LOGGER.logExceptionAsError(new AzureException("Cannot get final result"));
        }

        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY);
            return PollingUtils.deserializeResponseSync(BinaryData.fromString(latestResponseBody), serializer,
                resultType);
        }

        HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
        try (HttpResponse response = httpPipeline.sendSync(request, context)) {
            BinaryData responseBody = response.getBodyAsBinaryData();
            return PollingUtils.deserializeResponseSync(responseBody, serializer, resultType);
        }
    }
}
