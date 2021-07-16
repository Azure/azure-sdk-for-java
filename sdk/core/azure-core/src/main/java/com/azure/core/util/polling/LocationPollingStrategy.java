// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implements a Location polling strategy.
 */
public class LocationPollingStrategy implements PollingStrategy {
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String RETRY_AFTER = "Retry-After";

    private final JacksonAdapter serializer = new JacksonAdapter();

    private final HttpPipeline httpPipeline;
    private final Context context;

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public LocationPollingStrategy(
            HttpPipeline httpPipeline,
            Context context) {
        this.httpPipeline = httpPipeline;
        this.context = context;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(LOCATION);
        return Mono.just(locationHeader != null);
    }

    @Override
    public Mono<LongRunningOperationStatus> onInitialResponse(Response<?> response, PollingContext<BinaryData> pollingContext) {
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (locationHeader != null) {
            pollingContext.setData(LOCATION, locationHeader.getValue());
        }
        pollingContext.setData(HTTP_METHOD, response.getRequest().getHttpMethod().name());
        pollingContext.setData(REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            return Mono.just(LongRunningOperationStatus.IN_PROGRESS);
        } else {
            throw new RuntimeException("Operation failed or cancelled: " + response.getStatusCode());
        }
    }

    @Override
    public Mono<PollResponse<BinaryData>> poll(PollingContext<BinaryData> pollingContext) {
        HttpRequest request = new HttpRequest(HttpMethod.GET,
            pollingContext.getData(LOCATION).replace("http://", "https://"));
        Mono<HttpResponse> responseMono;
        if (context == null) {
            responseMono = httpPipeline.send(request);
        } else {
            responseMono = httpPipeline.send(request, context);
        }
        return responseMono.flatMap(res -> {
            HttpHeader locationHeader = res.getHeaders().get(LOCATION);
            if (locationHeader != null) {
                pollingContext.setData(LOCATION, locationHeader.getValue());
            }

            LongRunningOperationStatus status;
            if (res.getStatusCode() == 202) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (res.getStatusCode() >= 200 && res.getStatusCode() <= 204) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else {
                status = LongRunningOperationStatus.FAILED;
            }

            return BinaryData.fromFlux(res.getBody()).map(binaryData -> {
                String retryAfter = res.getHeaderValue(RETRY_AFTER);
                if (retryAfter != null) {
                    return new PollResponse<>(status, binaryData, Duration.ofSeconds(Long.parseLong(retryAfter)));
                } else {
                    return new PollResponse<>(status, binaryData);
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Mono<U> getResult(PollingContext<BinaryData> pollingContext, TypeReference<U> resultType) {
        String finalGetUrl;
        String httpMethod = pollingContext.getData(HTTP_METHOD);
        if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(REQUEST_URL);
        } else if ("POST".equalsIgnoreCase(httpMethod) && pollingContext.getData(LOCATION) != null) {
            finalGetUrl = pollingContext.getData(LOCATION);
        } else {
            throw new RuntimeException("Cannot get final result");
        }

        if (finalGetUrl == null) {
            BinaryData latestResponse = pollingContext.getLatestResponse().getValue();
            if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                return (Mono<U>) Mono.just(latestResponse);
            } else {
                return latestResponse.toObjectAsync(resultType);
            }
        } else {
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            Mono<HttpResponse> responseMono;
            if (context == null) {
                responseMono = httpPipeline.send(request);
            } else {
                responseMono = httpPipeline.send(request, context);
            }
            return responseMono.flatMap(res -> {
                if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                    return (Mono<U>) BinaryData.fromFlux(res.getBody());
                } else {
                    return res.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                        serializer.deserialize(body, resultType.getJavaType(), SerializerEncoding.JSON)));
                }
            });
        }
    }

    @Override
    public Mono<BinaryData> cancel(PollingContext<BinaryData> pollingContext, PollResponse<BinaryData> initialResponse) {
        return Mono.error(new IllegalStateException("Cancellation is not supported."));
    }
}
