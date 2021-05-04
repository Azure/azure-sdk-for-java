// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Supplier;

/**
 *
 * @param <U> The type of the final result of long running operation.
 */
public final class PollerFactory<U> {
    private static final String OPERATION_LOCATION = "Operation-Location";
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";

    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;

    private Duration pollInterval;

    public PollerFactory(SerializerAdapter serializerAdapter,
                         HttpPipeline httpPipeline) {
        this.serializerAdapter = serializerAdapter;
        this.httpPipeline = httpPipeline;
        pollInterval = Duration.ofSeconds(30);
    }

    public PollerFactory<U> setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }

    public PollerFlux<PollResult, U> createOperationResourcePoller(
            Supplier<Mono<? extends Response<?>>> activation, Type resultType) {
        return new PollerFlux<>(
                pollInterval,
                ctx -> activation.get()
                    .flatMap(r -> {
                        if (r.getStatusCode() / 100 != 2) {
                            return Mono.<PollResult>error(new RuntimeException("Operation cancelled or failed."));
                        }
                        HttpHeader operationLocationHeader = r.getHeaders().get(OPERATION_LOCATION);
                        if (operationLocationHeader == null) {
                            return Mono.<PollResult>error(new RuntimeException("Operation-Location header not found."));
                        } else {
                            ctx.setData(OPERATION_LOCATION, operationLocationHeader.getValue());
                            ctx.setData(REQUEST_URL, r.getRequest().getUrl().toString());
                            ctx.setData(HTTP_METHOD, r.getRequest().getHttpMethod().name());
                            HttpHeader locationHeader = r.getHeaders().get(LOCATION);
                            if (locationHeader != null) {
                                ctx.setData(LOCATION, locationHeader.getValue());
                            }
                            return Mono.just(new PollResult());
                        }
                    }),
                ctx -> {
                    HttpRequest request = new HttpRequest(HttpMethod.GET, ctx.getData(OPERATION_LOCATION));
                    return httpPipeline.send(request/*TODO: context? */)
                            .flatMap(r -> {
                                HttpHeader operationLocationHeader = r.getHeaders().get(OPERATION_LOCATION);
                                HttpHeader locationHeader = r.getHeaders().get(LOCATION);
                                if (operationLocationHeader != null) {
                                    ctx.setData(OPERATION_LOCATION, operationLocationHeader.getValue());
                                }
                                if (locationHeader != null) {
                                    ctx.setData(LOCATION, locationHeader.getValue());
                                }
                                return r.getBodyAsString();
                            })
                            .flatMap(body -> Mono.fromCallable(() -> serializerAdapter.<PollResult>deserialize(body,
                                    PollResult.class, SerializerEncoding.JSON)))
                            .map(result -> {
                                LongRunningOperationStatus lroStatus;
                                if ("NotStarted".equalsIgnoreCase(result.getStatus())) {
                                    lroStatus = LongRunningOperationStatus.NOT_STARTED;
                                } else if ("InProgress".equalsIgnoreCase(result.getStatus())
                                        || "Running".equalsIgnoreCase(result.getStatus())) {
                                    lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                                } else if ("Succeeded".equalsIgnoreCase(result.getStatus())) {
                                    lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                                } else if ("Failed".equalsIgnoreCase(result.getStatus())) {
                                    lroStatus = LongRunningOperationStatus.FAILED;
                                } else {
                                    throw new RuntimeException("Unknown 'status' property in the polling response: "
                                            + result.getStatus());
                                }
                                return new PollResponse<>(lroStatus, result);
                            });
                },
                (ctx, pr) -> Mono.error(new RuntimeException("Cancellation is not supported.")),
                ctx -> {
                    PollResponse<PollResult> lastResponse = ctx.getLatestResponse();
                    String finalGetUrl = lastResponse.getValue().getResourceLocation();
                    if (finalGetUrl == null) {
                        String httpMethod = ctx.getData(HTTP_METHOD);
                        if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
                            finalGetUrl = ctx.getData(REQUEST_URL);
                        } else if ("POST".equalsIgnoreCase(httpMethod) && ctx.getData(LOCATION) != null) {
                            finalGetUrl = ctx.getData(LOCATION);
                        } else {
                            return Mono.error(new RuntimeException("Cannot get final result"));
                        }
                    }

                    HttpRequest request = new HttpRequest(HttpMethod.GET,
                            finalGetUrl);
                    return httpPipeline.send(request/*TODO: context? */)
                            .flatMap(HttpResponse::getBodyAsString)
                            .flatMap(body -> Mono.fromCallable(() -> serializerAdapter.deserialize(body,
                                    resultType, SerializerEncoding.JSON)));
                });
    }
}
