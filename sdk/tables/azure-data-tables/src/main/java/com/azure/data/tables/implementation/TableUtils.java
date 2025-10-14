// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableServiceErrorException;
import com.azure.data.tables.implementation.models.TableServiceJsonError;
import com.azure.data.tables.implementation.models.TableServiceJsonErrorException;
import com.azure.data.tables.implementation.models.TableServiceOdataError;
import com.azure.data.tables.implementation.models.TableServiceOdataErrorMessage;
import com.azure.data.tables.models.TableServiceError;
import com.azure.data.tables.models.TableServiceException;
import com.azure.data.tables.models.TableTransactionFailedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.util.CoreUtils.getResultWithTimeout;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * A class containing utility methods for the Azure Tables library.
 */
public final class TableUtils {
    private static final String UTF8_CHARSET = "UTF-8";
    private static final String DELIMITER_CONTINUATION_TOKEN = ";";

    private TableUtils() {
        throw new UnsupportedOperationException("Cannot instantiate TablesUtils");
    }

    /**
     * Convert an implementation {@link TableServiceJsonError} to a public {@link TableServiceError}. This function maps
     * the service returned {@link TableServiceJsonError inner OData error} and its contents to the top level
     * {@link TableServiceError error}.
     *
     * @param tableServiceJsonError The {@link TableServiceJsonError} returned by the service.
     * @return The {@link TableServiceError} returned by the SDK.
     */
    public static TableServiceError toTableServiceError(TableServiceJsonError tableServiceJsonError) {
        String errorCode = null;
        String errorMessage = null;

        if (tableServiceJsonError != null) {
            final TableServiceOdataError odataError = tableServiceJsonError.getOdataError();

            if (odataError != null) {
                errorCode = odataError.getCode();
                TableServiceOdataErrorMessage odataErrorMessage = odataError.getMessage();

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
     * @return The {@link TableServiceException} to be thrown.
     */
    public static TableServiceException toTableServiceException(TableServiceJsonErrorException exception) {
        return new TableServiceException(exception.getMessage(), exception.getResponse(),
            toTableServiceError(exception.getValue()));
    }

    /**
     * Map a {@link Throwable} to {@link TableServiceException} if it's an instance of
     * {@link TableServiceJsonErrorException}, else it returns the original throwable.
     *
     * @param throwable A throwable.
     * @return A Throwable that is either an instance of {@link TableServiceException} or the original throwable.
     */
    public static Throwable mapThrowableToTableServiceException(Throwable throwable) {
        if (throwable instanceof TableServiceJsonErrorException) {
            return toTableServiceException((TableServiceJsonErrorException) throwable);
        } else if (throwable.getCause() instanceof Exception) {
            Throwable cause = throwable.getCause();
            if (cause instanceof TableServiceJsonErrorException) {
                return toTableServiceException((TableServiceJsonErrorException) cause);
            }
        }
        return throwable;
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
     * Deserializes a given {@link Response HTTP response} including headers to a given class.
     *
     * @param statusCode The status code which will trigger exception swallowing.
     * @param httpResponseException The {@link HttpResponseException} to be swallowed.
     * @param logger {@link ClientLogger} that will be used to record the exception.
     * @param <E> The class of the exception to swallow.
     * @return A {@link Mono} that contains the deserialized response.
     */
    public static <E extends HttpResponseException> Mono<Response<Void>> swallowExceptionForStatusCode(int statusCode,
        E httpResponseException, ClientLogger logger) {
        HttpResponse httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            return Mono.just(new SimpleResponse<>(httpResponse.getRequest(), httpResponse.getStatusCode(),
                httpResponse.getHeaders(), null));
        }

        return monoError(logger, httpResponseException);
    }

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order. The value is
     * stored as a parsed array (ex. key=[val1, val2, val3] instead of key=val1,val2,val3).
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    public static Map<String, String[]> parseQueryStringSplitValues(final String queryString) {
        // We need to first split by comma and then decode each piece since we don't want to confuse legitimate separate
        // query values from query values that container a comma.
        // Example 1: prefix=a%2cb => prefix={decode(a%2cb)} => prefix={"a,b"}
        // Example 2: prefix=a,b => prefix={decode(a),decode(b)} => prefix={"a", "b"}
        return parseQueryStringHelper(queryString, value -> {
            String[] v = value.split(",");
            String[] ret = new String[v.length];

            for (int i = 0; i < v.length; i++) {
                ret[i] = urlDecode(v[i]);
            }

            return ret;
        });
    }

    private static <T> Map<String, T> parseQueryStringHelper(final String queryString,
        Function<String, T> valueParser) {
        TreeMap<String, T> pieces = new TreeMap<>();

        if (CoreUtils.isNullOrEmpty(queryString)) {
            return pieces;
        }

        for (String kvp : queryString.split("&")) {
            int equalIndex = kvp.indexOf("=");
            String key = urlDecode(kvp.substring(0, equalIndex).toLowerCase(Locale.ROOT));
            T value = valueParser.apply(kvp.substring(equalIndex + 1));

            pieces.putIfAbsent(key, value);
        }

        return pieces;
    }

    /**
     * Performs a safe decoding of the passed string, taking care to preserve each {@code +} character rather than
     * replacing it with a space character.
     *
     * @param stringToDecode String value to decode
     * @return the decoded string value
     * @throws RuntimeException If the UTF-8 charset isn't supported
     */
    public static String urlDecode(final String stringToDecode) {
        if (CoreUtils.isNullOrEmpty(stringToDecode)) {
            return "";
        }

        if (stringToDecode.contains("+")) {
            StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;

            for (int m = 0; m < stringToDecode.length(); m++) {
                if (stringToDecode.charAt(m) == '+') {
                    if (m > startDex) {
                        outBuilder.append(decode(stringToDecode.substring(startDex, m)));
                    }

                    outBuilder.append("+");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToDecode.length()) {
                outBuilder.append(decode(stringToDecode.substring(startDex)));
            }

            return outBuilder.toString();
        } else {
            return decode(stringToDecode);
        }
    }

    /*
     * Helper method to reduce duplicate calls of URLDecoder.decode
     */
    private static String decode(final String stringToDecode) {
        try {
            return URLDecoder.decode(stringToDecode, UTF8_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Performs a safe encoding of the specified string, taking care to insert %20 for each space character instead of
     * inserting the {@code +} character.
     *
     * @param stringToEncode String value to encode
     * @return the encoded string value
     * @throws RuntimeException If the UTF-8 charset isn't supported
     */
    public static String urlEncode(final String stringToEncode) {
        if (stringToEncode == null) {
            return null;
        }

        if (stringToEncode.isEmpty()) {
            return "";
        }

        if (stringToEncode.contains(" ")) {
            StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;
            for (int m = 0; m < stringToEncode.length(); m++) {
                if (stringToEncode.charAt(m) == ' ') {
                    if (m > startDex) {
                        outBuilder.append(encode(stringToEncode.substring(startDex, m)));
                    }

                    outBuilder.append("%20");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToEncode.length()) {
                outBuilder.append(encode(stringToEncode.substring(startDex)));
            }

            return outBuilder.toString();
        } else {
            return encode(stringToEncode);
        }
    }

    /*
     * Helper method to reduce duplicate calls of URLEncoder.encode
     */
    private static String encode(final String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, UTF8_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Single quotes in OData queries should be escaped by using two consecutive single quotes characters.
    // Source: http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#sec_URLSyntax.
    public static String escapeSingleQuotes(String input) {
        if (input == null) {
            return null;
        }

        return input.replace("'", "''");
    }

    public static Exception interpretException(Exception ex) {
        Throwable exception = ex;
        if (exception instanceof ExecutionException) {
            exception = exception.getCause();
        }
        Throwable cause = exception.getCause();
        if (cause instanceof TableTransactionFailedException) {
            return (TableTransactionFailedException) cause;
        } else {
            return (Exception) mapThrowableToTableServiceException(exception);
        }
    }

    public static String[] getKeysFromToken(String token) {
        String[] split = token.split(DELIMITER_CONTINUATION_TOKEN, 2);
        String[] keys = new String[2];
        if (split.length == 0) {
            throw new RuntimeException("Split done incorrectly, must have partition key. Token: " + token);
        } else if (split.length == 2) {
            keys[0] = split[0];
            keys[1] = split[1];
        } else {
            keys[0] = split[0];
            keys[1] = null;
        }
        return keys;
    }

    public static <T> Response<T> callWithOptionalTimeout(Supplier<Response<T>> callable, Duration timeout,
        ClientLogger logger) {
        return callWithOptionalTimeout(callable, timeout, logger, false);
    }

    public static <T> Response<T> callWithOptionalTimeout(Supplier<Response<T>> callable, Duration timeout,
        ClientLogger logger, boolean skip409Logging) {
        try {
            return callHandler(callable, timeout, logger);
        } catch (Exception thrown) {
            Throwable exception = mapThrowableToTableServiceException(thrown);
            if (exception instanceof TableServiceException) {
                TableServiceException e = (TableServiceException) exception;
                if (skip409Logging && e.getResponse() != null && e.getResponse().getStatusCode() == 409) {
                    // return empty response
                    HttpResponse resp = ((TableServiceException) exception).getResponse();
                    return new SimpleResponse<>(resp.getRequest(), resp.getStatusCode(), resp.getHeaders(), null);
                }
            }

            throw logger.logExceptionAsError((RuntimeException) exception);
        }
    }

    public static <T> PagedIterable<T> callIterableWithOptionalTimeout(Supplier<PagedIterable<T>> callable,
        Duration timeout, ClientLogger logger) {
        try {
            return callHandler(callable, timeout, logger);
        } catch (Exception thrown) {
            Throwable exception = mapThrowableToTableServiceException(thrown);
            throw logger.logExceptionAsError((RuntimeException) exception);
        }
    }

    public static <T> T requestWithOptionalTimeout(Supplier<T> request, Duration timeout)
        throws ExecutionException, InterruptedException, TimeoutException {
        return hasTimeout(timeout)
            ? getResultWithTimeout(SharedExecutorService.getInstance().submit(request::get), timeout)
            : request.get();
    }

    /**
     * Checks whether the timeout exists (is not null and has a positive duration).
     *
     * @param timeout The timeout to check.
     * @return Whether the timeout exists (is not null and has a positive duration).
     */
    private static boolean hasTimeout(Duration timeout) {
        return timeout != null && (timeout.getSeconds() | timeout.getNano()) > 0;
    }

    private static <T> T callHandler(Supplier<T> callable, Duration timeout, ClientLogger logger) throws Exception {
        try {
            return hasTimeout(timeout)
                ? getResultWithTimeout(SharedExecutorService.getInstance().submit(callable::get), timeout)
                : callable.get();
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {

            if (ex instanceof ExecutionException) {
                Throwable cause = ex.getCause();
                throw (Exception) mapThrowableToTableServiceException(cause);
            } else {
                throw logger.logExceptionAsError(new RuntimeException(ex));
            }
        }
    }

    public static boolean isCosmosEndpoint(String endpoint) {
        try {
            URI endpointUrl = new URI(endpoint);
            String host = endpointUrl.getHost();
            return host.contains(".cosmos.") || host.contains(".cosmosdb.");
        } catch (Exception e) {
            return false;
        }
    }
}
