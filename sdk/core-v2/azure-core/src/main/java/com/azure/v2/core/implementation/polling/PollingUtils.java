// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation.polling;

import com.azure.v2.core.http.polling.LocationPollingStrategy;
import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.core.http.polling.OperationResourcePollingStrategy;
import com.azure.v2.core.implementation.ImplUtils;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.SharedExecutorService;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Utility class for handling polling responses.
 */
public final class PollingUtils {
    private static final String FORWARD_SLASH = "/";
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtils.class);

    /**
     * Serialize a response to a {@link BinaryData}. If the response is already a {@link BinaryData}, return as is.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @return a {@link BinaryData} response
     */
    public static BinaryData serializeResponse(Object response, ObjectSerializer serializer) {
        if (response instanceof BinaryData) {
            return (BinaryData) response;
        } else {
            return BinaryData.fromObject(response, serializer);
        }
    }

    /**
     * Deserialize a {@link BinaryData} into a poll response type. If the poll response type is also a
     * {@link BinaryData}, return as is.
     *
     * @param binaryData the binary data to deserialize
     * @param serializer the object serializer to use
     * @param typeReference the {@link Type} of the poll response type
     * @param <T> the generic parameter of the poll response type
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeResponse(BinaryData binaryData, ObjectSerializer serializer, Type typeReference) {
        if (typeReference instanceof Class<?> && BinaryData.class.isAssignableFrom((Class<?>) typeReference)) {
            return (T) binaryData.toReplayableBinaryData();
        } else {
            return binaryData.toObject(typeReference, serializer);
        }
    }

    /**
     * Converts an object received from an activation or a polling call to another type requested by the user. If the
     * object type is identical to the type requested by the user, it's returned as is. If the response is null, null
     * is returned.
     * <p>
     * This is useful when an activation response needs to be converted to a polling response type, or a final result
     * type, if the long-running operation completes upon activation.
     *
     * @param response the response from an activation or polling call
     * @param serializer the object serializer to use
     * @param typeReference the {@link Type} of the user requested type
     * @param <T> the generic parameter of the user requested type
     * @return the converted object
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertResponse(Object response, ObjectSerializer serializer, Type typeReference) {
        if (response == null) {
            return null;
        } else if (typeReference instanceof Class<?>
            && response.getClass().isAssignableFrom((Class<?>) typeReference)) {
            return (T) response;
        } else {
            return deserializeResponse(serializeResponse(response, serializer), serializer, typeReference);
        }
    }

    public static <T> PollResponse<T> pollingLoop(PollingContext<T> pollingContext, Duration timeout,
        LongRunningOperationStatus statusToWaitFor, Function<PollingContext<T>, PollResponse<T>> pollOperation,
        Duration pollInterval, boolean isWaitForStatus) {
        boolean timeBound = timeout != null;
        long timeoutInMillis = timeBound ? timeout.toMillis() : -1;
        long startTime = System.currentTimeMillis();
        PollResponse<T> intermediatePollResponse = pollingContext.getLatestResponse();

        boolean firstPoll = true;
        while (!intermediatePollResponse.getStatus().isComplete()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (timeBound && elapsedTime >= timeoutInMillis) {
                if (intermediatePollResponse.getStatus().equals(statusToWaitFor) || isWaitForStatus) {
                    return intermediatePollResponse;
                } else {
                    throw LOGGER.throwableAtError()
                        .log("Polling didn't complete before the timeout period.", CoreException::from);
                }
            }

            if (intermediatePollResponse.getStatus().equals(statusToWaitFor)) {
                return intermediatePollResponse;
            }

            final Future<PollResponse<T>> pollOp;
            if (firstPoll) {
                firstPoll = false;
                pollOp = SharedExecutorService.getInstance().submit(() -> pollOperation.apply(pollingContext));
            } else {
                Duration delay = getDelay(intermediatePollResponse, pollInterval);
                pollOp = SharedExecutorService.getInstance()
                    .schedule(() -> pollOperation.apply(pollingContext), delay.toMillis(), TimeUnit.MILLISECONDS);
            }

            try {
                long pollTimeout = timeBound ? timeoutInMillis - elapsedTime : -1;
                intermediatePollResponse = ImplUtils.getResultWithTimeout(pollOp, pollTimeout);
                pollingContext.setLatestResponse(intermediatePollResponse);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // waitUntil should not throw when timeout is reached.
                if (isWaitForStatus) {
                    return intermediatePollResponse;
                }
                throw LOGGER.throwableAtError().log(e, CoreException::from);
            }
        }

        return intermediatePollResponse;
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private static <T> Duration getDelay(PollResponse<T> pollResponse, Duration pollInterval) {
        Duration retryAfter = pollResponse.getRetryAfter();
        if (retryAfter == null) {
            return pollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0 ? retryAfter : pollInterval;
        }
    }

    /**
     * Determines if the location can poll.
     * <p>
     * Shared functionality for {@link LocationPollingStrategy}.
     *
     * @param initialResponse The initial response.
     * @param endpoint The endpoint.
     * @param logger The logger.
     * @return Whether the location can poll.
     */
    public static boolean locationCanPoll(Response<?> initialResponse, String endpoint, ClientLogger logger) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(HttpHeaderName.LOCATION);

        if (locationHeader != null) {
            try {
                ImplUtils.createUrl(getAbsolutePath(locationHeader.getValue(), endpoint, logger));
                return true;
            } catch (MalformedURLException e) {
                logger.atInfo().setThrowable(e).log("Failed to parse Location header into a URL.");
                return false;
            }
        }

        return false;
    }

    /**
     * Determines if the operation resource can poll.
     * <p>
     * Shared functionality for {@link OperationResourcePollingStrategy}.
     *
     * @param initialResponse The initial response.
     * @param operationLocationHeader The operation location header.
     * @param endpoint The endpoint.
     * @param logger The logger.
     * @return Whether the operation resource can poll.
     */
    public static boolean operationResourceCanPoll(Response<?> initialResponse, HttpHeaderName operationLocationHeader,
        String endpoint, ClientLogger logger) {
        HttpHeader header = initialResponse.getHeaders().get(operationLocationHeader);

        if (header != null) {
            try {
                ImplUtils.createUrl(getAbsolutePath(header.getValue(), endpoint, logger));
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Validates the timeout.
     *
     * @param timeout The timeout.
     * @param logger The logger.
     * @throws NullPointerException if {@code timeout} is null.
     * @throws IllegalArgumentException if {@code timeout} is negative or zero.
     */
    public static void validateTimeout(Duration timeout, ClientLogger logger) {
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        if (timeout.isNegative() || timeout.isZero()) {
            throw logger.throwableAtWarning()
                .log("Negative or zero value for timeout is not allowed.", IllegalArgumentException::new);
        }
    }

    /**
     * Validates the poll interval.
     *
     * @param pollInterval The poll interval.
     * @param logger The logger.
     * @return The poll interval.
     * @throws NullPointerException if {@code pollInterval} is null.
     * @throws IllegalArgumentException if {@code pollInterval} is negative or zero.
     */
    public static Duration validatePollInterval(Duration pollInterval, ClientLogger logger) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw logger.throwableAtWarning()
                .log("Negative or zero value for pollInterval is not allowed.", IllegalArgumentException::new);
        }
        return pollInterval;
    }

    /**
     * Create an absolute path from the endpoint if the 'path' is relative. Otherwise, return the 'path' as absolute
     * path.
     *
     * @param path a relative path or absolute path.
     * @param endpoint an endpoint to create the absolute path if the path is relative.
     * @param logger a {@link ClientLogger} to log the exception.
     * @return an absolute path.
     */
    public static String getAbsolutePath(String path, String endpoint, ClientLogger logger) {
        try {
            URI uri = new URI(path);
            if (!uri.isAbsolute()) {
                if (CoreUtils.isNullOrEmpty(endpoint)) {
                    throw logger.throwableAtWarning()
                        .log("Relative path requires endpoint to be non-null and non-empty to create an absolute path.",
                            IllegalArgumentException::new);
                }

                if (endpoint.endsWith(FORWARD_SLASH) && path.startsWith(FORWARD_SLASH)) {
                    return endpoint + path.substring(1);
                } else if (!endpoint.endsWith(FORWARD_SLASH) && !path.startsWith(FORWARD_SLASH)) {
                    return endpoint + FORWARD_SLASH + path;
                } else {
                    return endpoint + path;
                }
            }
        } catch (URISyntaxException ex) {
            throw logger.throwableAtWarning().log("'path' must be a valid URI.", ex, IllegalArgumentException::new);
        }
        return path;
    }

    private PollingUtils() {
    }
}
