// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Factory to create PollerFlux for Azure resource manager (ARM) long-running-operation (LRO).
 */
public final class PollerFactory {
    private static final ClientLogger LOGGER = new ClientLogger(PollerFactory.class);
    private static final LongRunningOperationStatus LRO_CANCELLED = LongRunningOperationStatus.fromString("Cancelled",
        true);

    /**
     * Creates a PollerFlux with default ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitMono the Mono on subscribe send the service request to initiate the long-running-operation
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Mono<Response<Flux<ByteBuffer>>> lroInitMono) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        Objects.requireNonNull(pollResultType, "'pollResultType' cannot be null.");
        Objects.requireNonNull(finalResultType, "'finalResultType' cannot be null.");
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        Objects.requireNonNull(lroInitMono, "'lroInitMono' cannot be null.");
        Function<PollingContext<PollResult<T>>, Mono<PollResponse<PollResult<T>>>> defaultLroInitOperation =
            context -> lroInitMono.flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue())
                .map(contentBytes -> {
                    String content = new String(contentBytes, StandardCharsets.UTF_8);
                    PollingState state = PollingState.create(serializerAdapter,
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        content);
                    state.store(context);
                    T result = deserialize(serializerAdapter, content, pollResultType);
                    return new PollResponse<>(state.getOperationStatus(), new PollResult<>(result));
                }));
        return PollerFlux.create(defaultPollInterval,
            defaultLroInitOperation,
            pollFunction(serializerAdapter, pipeline, pollResultType),
            cancelFunction(),
            fetchResultFunction(serializerAdapter, pipeline, finalResultType));
    }

    /**
     * Creates a PollerFlux.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitOperation the function upon invoking should initiate the long-running-operation
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Function<PollingContext<PollResult<T>>, Mono<PollResult<T>>> lroInitOperation) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        Objects.requireNonNull(pollResultType, "'pollResultType' cannot be null.");
        Objects.requireNonNull(finalResultType, "'finalResultType' cannot be null.");
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        Objects.requireNonNull(lroInitOperation, "'lroInitOperation' cannot be null.");

        return new PollerFlux<>(defaultPollInterval,
            lroInitOperation,
            pollFunction(serializerAdapter, pipeline, pollResultType),
            cancelFunction(),
            fetchResultFunction(serializerAdapter, pipeline, finalResultType));
    }

    /**
     * Dehydrate a PollerFlux from a string.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param pollingStateStr the string to dehydrate PollerFlux from
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
                                                             HttpPipeline pipeline,
                                                             Type pollResultType,
                                                             Type finalResultType,
                                                             Duration defaultPollInterval,
                                                             String pollingStateStr) {
        return create(serializerAdapter, pipeline, pollResultType, finalResultType, defaultPollInterval,
            context -> {
                PollingState.from(serializerAdapter, pollingStateStr).store(context);
                return Mono.empty();
            });
    }

    /**
     * Get a Function that polls provisioning state of ARM resource.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making poll request
     * @param pollResultType the type of the poll result
     * @param <T> the type of poll result type
     * @return the ARM poll function
     */
    private static <T> Function<PollingContext<PollResult<T>>, Mono<PollResponse<PollResult<T>>>> pollFunction(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType) {
        return context -> {
            PollingState pollingState = PollingState.from(serializerAdapter, context);
            if (pollingState.getOperationStatus().isComplete()) {
                if (pollingState.getOperationStatus() == LongRunningOperationStatus.FAILED
                    || pollingState.getOperationStatus() == LRO_CANCELLED) {
                    // Failed|Cancelled
                    Error lroInitError = pollingState.getSynchronouslyFailedLroError();
                    if (lroInitError != null) {
                        return errorPollResponseMono(pollingState.getOperationStatus(), lroInitError);
                    }
                    Error pollError = pollingState.getPollError();
                    if (pollError != null) {
                        return errorPollResponseMono(pollingState.getOperationStatus(), pollError);
                    }
                    throw new IllegalStateException("Either LroError or PollError must"
                        + "be set when OperationStatus is in Failed|Cancelled State.");
                } else {
                    // Succeeded
                    return pollResponseMono(serializerAdapter,
                        pollingState.getOperationStatus(),
                        pollingState.getLastResponseBody(),
                        pollResultType);
                }
            } else {
                // InProgress|NonTerminal-Status
                Mono<PollResponse<PollResult<T>>> pollResponse = pollResponseMono(serializerAdapter,
                    pollingState.getOperationStatus(),
                    pollingState.getLastResponseBody(),
                    pollResultType);
                return doSinglePoll(pipeline, pollingState)
                    .doOnNext(updatedState -> updatedState.store(context))
                    .then(pollResponse);
            }
        };
    }

    /**
     * Currently there is no option to cancel an ARM LRO in generic way, this is NOP.
     *
     * @param <T> the type of poll result type
     * @return cancel Function
     */
    private static <T>
        BiFunction<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>, Mono<PollResult<T>>> cancelFunction() {
        return new BiFunction<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>, Mono<PollResult<T>>>() {
            @Override
            public Mono<PollResult<T>> apply(PollingContext<PollResult<T>> context,
                                             PollResponse<PollResult<T>> response) {
                return Mono.empty();
            }
        };
    }

    /**
     * Get a Function that retrieves final result of a LRO.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for fetching final result
     * @param finalResultType the final result type
     * @param <T> the final result type
     * @param <U> the poll result type
     * @return retrieve final LRO result Function
     */
    private static <T, U> Function<PollingContext<PollResult<T>>, Mono<U>> fetchResultFunction(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type finalResultType) {
        return context -> {
            PollingState pollingState = PollingState.from(serializerAdapter, context);
            FinalResult finalResult = pollingState.getFinalResult();
            if (finalResult == null) {
                return Mono.empty();
            } else {
                String value = finalResult.getResult();
                if (value != null) {
                    U result = deserialize(serializerAdapter, value, finalResultType);
                    return result != null ? Mono.just(result) : Mono.empty();
                } else {
                    return pipeline.send(new HttpRequest(HttpMethod.GET, finalResult.getResultUri()))
                        .flatMap((Function<HttpResponse, Mono<String>>) response -> response.getBodyAsString())
                        .flatMap(body -> {
                            U result = deserialize(serializerAdapter, body, finalResultType);
                            return result != null ? Mono.just(result) : Mono.empty();
                        });
                }
            }
        };
    }

    /**
     * Create a PollResponse indicating service error.
     *
     * @param opStatus the long-running-operation errored status
     * @param error the error description
     * @param <T> the poll result type
     * @return PollResponse
     */
    private static <T> Mono<PollResponse<PollResult<T>>> errorPollResponseMono(LongRunningOperationStatus opStatus,
                                                                               Error error) {
        PollResult<T> pollResult = new PollResult<>(new PollResult.Error(error.getMessage(),
            error.getResponseStatusCode(),
            error.getResponseBody()));
        return Mono.just(new PollResponse<>(opStatus, pollResult));
    }

    /**
     * Create a PollResponse indicating succeeded or in-progress LRO.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param opStatus the long-running-operation succeeded or in-progress status
     * @param pollResponseBody the poll response body
     * @param pollResultType the poll result type
     * @param <T> the poll result type
     * @return PollResponse
     */
    private static <T> Mono<PollResponse<PollResult<T>>> pollResponseMono(SerializerAdapter serializerAdapter,
                                                                          LongRunningOperationStatus opStatus,
                                                                          String pollResponseBody,
                                                                          Type pollResultType) {
        T result = deserialize(serializerAdapter, pollResponseBody, pollResultType);
        return Mono.just(new PollResponse<>(opStatus, new PollResult<T>(result)));
    }

    /**
     * Do a poll to retrieve the LRO status.
     *
     * @param pipeline the HttpPipeline for making poll request
     * @param pollingState the current PollingState
     * @return a Mono emitting PollingState updated from the poll operation response
     */
    private static Mono<PollingState> doSinglePoll(HttpPipeline pipeline, PollingState pollingState) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, pollingState.getPollUrl()))
            .flatMap((Function<HttpResponse, Mono<PollingState>>) response -> response.getBodyAsString()
                .map(body -> pollingState.update(response.getStatusCode(), response.getHeaders(), body))
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(pollingState.update(response.getStatusCode(),
                        response.getHeaders(),
                        null));
                })));
    }

    /**
     * Decode a string value.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param value the value to decode
     * @param type the type to decode to
     * @param <U> the type to decode to
     * @return decoded value
     */
    @SuppressWarnings("unchecked")
    private static <U> U deserialize(SerializerAdapter serializerAdapter, String value, Type type) {
        if (value == null || value.equalsIgnoreCase("")) {
            LOGGER.info("Ignoring decoding of null or empty value to:" + type.getTypeName());
            return null;
        } else {
            try {
                return (U) serializerAdapter.deserialize(value, type, SerializerEncoding.JSON);
            } catch (IOException ioe) {
                LOGGER.logExceptionAsWarning(new IllegalArgumentException("Unable to decode '" + value + "' to: "
                    + type.getTypeName(), ioe));
                return null;
            }
        }
    }
}
