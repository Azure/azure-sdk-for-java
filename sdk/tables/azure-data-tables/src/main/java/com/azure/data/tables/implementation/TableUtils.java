// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.implementation.models.TableServiceErrorOdataError;
import com.azure.data.tables.implementation.models.TableServiceErrorOdataErrorMessage;
import com.azure.data.tables.models.TableServiceError;
import com.azure.data.tables.models.TableServiceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A class containing utility methods for the Azure Data Tables library.
 */
public final class TableUtils {
    private TableUtils() {
        throw new UnsupportedOperationException("Cannot instantiate TablesUtils");
    }

    /**
     * Convert an implementation {@link com.azure.data.tables.implementation.models.TableServiceError} to a public
     * {@link TableServiceError}. This function maps the service returned
     * {@link com.azure.data.tables.implementation.models.TableServiceErrorOdataError inner OData error} and its
     * contents to the top level {@link TableServiceError error}.
     *
     * @param tableServiceError The {@link com.azure.data.tables.implementation.models.TableServiceError} returned by
     * the service.
     *
     * @return The {@link TableServiceError} returned by the SDK.
     */
    public static TableServiceError toTableServiceError(
        com.azure.data.tables.implementation.models.TableServiceError tableServiceError) {

        String errorCode = null;
        String errorMessage = null;

        if (tableServiceError != null) {
            final TableServiceErrorOdataError odataError = tableServiceError.getOdataError();

            if (odataError != null) {
                errorCode = odataError.getCode();
                TableServiceErrorOdataErrorMessage odataErrorMessage = odataError.getMessage();

                if (odataErrorMessage != null) {
                    errorMessage = odataErrorMessage.getValue();
                }
            }
        }

        return new TableServiceError(errorCode, errorMessage);
    }

    /**
     * Convert an implementation {@link TableServiceErrorException} to a public {@link TableServiceException}.
     *
     * @param exception The {@link TableServiceErrorException}.
     *
     * @return The {@link TableServiceException} to be thrown.
     */
    public static TableServiceException toTableServiceException(TableServiceErrorException exception) {
        return new TableServiceException(exception.getMessage(), exception.getResponse(),
            toTableServiceError(exception.getValue()));
    }

    /**
     * Map a {@link Throwable} to {@link TableServiceException} if it's an instance of
     * {@link TableServiceErrorException}, else it returns the original throwable.
     *
     * @param throwable A throwable.
     *
     * @return A Throwable that is either an instance of {@link TableServiceException} or the original throwable.
     */
    public static Throwable mapThrowableToTableServiceException(Throwable throwable) {
        if (throwable instanceof TableServiceErrorException) {
            return toTableServiceException((TableServiceErrorException) throwable);
        } else {
            return throwable;
        }
    }

    /**
     * Applies a timeout to a {@link Mono publisher} if the given timeout is not null.
     *
     * @param publisher {@link Mono} to apply optional timeout to.
     * @param timeout Optional timeout.
     * @param <T> Return type of the {@link Mono}.
     * @return {@link Mono} with an applied timeout, if any.
     */
    public static <T> Mono<T> applyOptionalTimeout(Mono<T> publisher, Duration timeout) {
        return timeout == null ? publisher : publisher.timeout(timeout);
    }

    /**
     * Applies a timeout to a {@link Flux publisher} if the given timeout is not null.
     *
     * @param publisher {@link Flux} to apply optional timeout to.
     * @param timeout Optional timeout.
     * @param <T> Return type of the {@link Flux}.
     * @return {@link Flux} with an applied timeout, if any.
     */
    public static <T> Flux<T> applyOptionalTimeout(Flux<T> publisher, Duration timeout) {
        return timeout == null ? publisher : publisher.timeout(timeout);
    }

    /**
     * Applies a timeout to a {@link PagedFlux publisher} if the given timeout is not null.
     *
     * @param publisher {@link PagedFlux} to apply optional timeout to.
     * @param timeout Optional timeout.
     * @param <T> Return type of the {@link PagedFlux}.
     * @return {@link PagedFlux} with an applied timeout, if any.
     */
    public static <T> PagedFlux<T> applyOptionalTimeout(PagedFlux<T> publisher, Duration timeout) {
        return timeout == null ? publisher : (PagedFlux<T>) publisher.timeout(timeout);
    }

    /**
     * Blocks an asynchronous response with an optional timeout.
     *
     * @param response Asynchronous response to block.
     * @param timeout Optional timeout.
     * @param <T> Return type of the asynchronous response.
     * @return The value of the asynchronous response.
     * @throws RuntimeException If the asynchronous response doesn't complete before the timeout expires.
     */
    public static <T> T blockWithOptionalTimeout(Mono<T> response, Duration timeout) {
        if (timeout == null) {
            return response.block();
        } else {
            return response.block(timeout);
        }
    }

    /**
     * Deserializes a given {@link Response HTTP response} including headers to a given class.
     *
     * @param statusCode The status code which will trigger exception swallowing.
     * @param httpResponseException The {@link HttpResponseException} to be swallowed.
     * @param logger {@link ClientLogger} that will be used to record the exception.
     * @param <E> The class of the exception to swallow.
     *
     * @return A {@link Mono} that contains the deserialized response.
     */
    public static <E extends HttpResponseException> Mono<Response<Void>> swallowExceptionForStatusCode(int statusCode, E httpResponseException, ClientLogger logger) {
        HttpResponse httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            return Mono.just(new SimpleResponse<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                httpResponse.getHeaders(), null));
        }

        return monoError(logger, httpResponseException);
    }
}
