package com.azure.data.tables.implementation;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpResponseLogger;
import com.azure.core.http.policy.HttpResponseLoggingContext;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public class TablesHttpResponseLogger implements HttpResponseLogger {
    private static final ClientLogger LOGGER = new ClientLogger(TablesHttpResponseLogger.class);
    private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
    private static final ObjectMapperShim PRETTY_PRINTER = ObjectMapperShim.createPrettyPrintMapper();
    private final HttpLogOptions httpLogOptions;

    public TablesHttpResponseLogger(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
    }

    @Override
    public Mono<HttpResponse> logResponse(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
        final LogLevel logLevel = getLogLevel(loggingOptions);
        final HttpResponse response = loggingOptions.getHttpResponse();

        if (!logger.canLogAtLevel(logLevel)) {
            return Mono.just(response);
        }

        LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

        logContentLength(response, logBuilder);

        logUrl(httpLogOptions, loggingOptions, response, logBuilder);

        logHeaders(httpLogOptions, logger, response, logBuilder);

        if (httpLogOptions.getLogLevel().shouldLogBody()) {
            String contentTypeHeader = response.getHeaderValue("Content-Type");
            long contentLength = getContentLength(logger, response.getHeaders());
            if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                return Mono.just(new LoggingHttpResponse(response, logBuilder, logger,
                    (int) contentLength, contentTypeHeader, httpLogOptions.isPrettyPrintBody()));
            }
        }

        logBuilder.log("HTTP response");
        return Mono.just(response);
    }

    @Override
    public HttpResponse logResponseSync(ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
        final LogLevel logLevel = getLogLevel(loggingOptions);
        final HttpResponse response = loggingOptions.getHttpResponse();

        if (!logger.canLogAtLevel(logLevel)) {
            return response;
        }
        LoggingEventBuilder logBuilder = getLogBuilder(logLevel, logger);

        logContentLength(response, logBuilder);

        logUrl(httpLogOptions, loggingOptions, response, logBuilder);

        logHeaders(httpLogOptions, logger, response, logBuilder);

        if (httpLogOptions.getLogLevel().shouldLogBody()) {
            String contentTypeHeader = response.getHeaderValue("Content-Type");
            long contentLength = getContentLength(logger, response.getHeaders());
            if (shouldBodyBeLogged(contentTypeHeader, contentLength)) {
                return new LoggingHttpResponse(response, logBuilder, logger,
                    (int) contentLength, contentTypeHeader, httpLogOptions.isPrettyPrintBody());
            }
        }

        logBuilder.log("Http response");

        return response;
    }

    private static LoggingEventBuilder getLogBuilder(LogLevel logLevel, ClientLogger logger) {
        switch (logLevel) {
            case ERROR:
                return logger.atError();
            case WARNING:
                return logger.atWarning();
            case INFORMATIONAL:
                return logger.atInfo();
            case VERBOSE:
            default:
                return logger.atVerbose();
        }
    }

    private void logContentLength(HttpResponse response, LoggingEventBuilder logBuilder) {
        String contentLengthString = response.getHeaderValue("Content-Length");
        if (!CoreUtils.isNullOrEmpty(contentLengthString)) {
            logBuilder.addKeyValue(TablesLoggingKeys.CONTENT_LENGTH_KEY, contentLengthString);
        }
    }

    private void logUrl(HttpLogOptions httpLogOptions, HttpResponseLoggingContext loggingOptions, HttpResponse response,
                        LoggingEventBuilder logBuilder) {
        if (httpLogOptions.getLogLevel().shouldLogUrl()) {
            logBuilder
                .addKeyValue(TablesLoggingKeys.STATUS_CODE_KEY, response.getStatusCode())
                .addKeyValue(TablesLoggingKeys.URL_KEY, getRedactedUrl(response.getRequest().getUrl(),
                    httpLogOptions.getAllowedQueryParamNames()))
                .addKeyValue(TablesLoggingKeys.DURATION_MS_KEY, loggingOptions.getResponseDuration().toMillis());
        }
    }

    private void logHeaders(HttpLogOptions httpLogOptions, ClientLogger logger, HttpResponse response,
                            LoggingEventBuilder logBuilder) {
        if (httpLogOptions.getLogLevel().shouldLogHeaders() && logger.canLogAtLevel(LogLevel.VERBOSE)) {
            addHeadersToLogMessage(httpLogOptions.getAllowedHeaderNames(), response.getHeaders(), logBuilder);
        }
    }

    private static void addHeadersToLogMessage(Set<String> allowedHeaderNames, HttpHeaders headers,
                                               LoggingEventBuilder logBuilder) {
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            logBuilder.addKeyValue(headerName, allowedHeaderNames.contains(headerName.toLowerCase(Locale.ROOT))
                ? header.getValue() : "REDACTED");
        }
    }

    private static String getRedactedUrl(URL url, Set<String> allowedQueryParameterNames) {
        String query = url.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            // URL doesn't have a query string, just return the URL as-is.
            return url.toString();
        }

        // URL does have a query string that may need redactions.
        // Use UrlBuilder to break apart the URL, clear the query string, and add the redacted query string.
        UrlBuilder urlBuilder = TablesLoggingUtils.parseUrl(url, false);

        TablesLoggingUtils.parseQueryParameters(query).forEachRemaining(queryParam -> {
            if (allowedQueryParameterNames.contains(queryParam.getKey().toLowerCase(Locale.ROOT))) {
                urlBuilder.addQueryParameter(queryParam.getKey(), queryParam.getValue());
            } else {
                urlBuilder.addQueryParameter(queryParam.getKey(), "REDACTED");
            }
        });

        return urlBuilder.toString();
    }

    private static long getContentLength(ClientLogger logger, HttpHeaders headers) {
        long contentLength = 0;

        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            return contentLength;
        }

        try {
            contentLength = Long.parseLong(contentLengthString);
        } catch (NumberFormatException | NullPointerException e) {
            logger.warning("Could not parse the HTTP header content-length: '{}'.", contentLengthString, e);
        }

        return contentLength;
    }

    private static boolean shouldBodyBeLogged(String contentTypeHeader, long contentLength) {
        return !ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
            && contentLength < MAX_BODY_LOG_SIZE;
    }

    private static final class LoggingHttpResponse extends HttpResponse {
        private final HttpResponse actualResponse;
        private final LoggingEventBuilder logBuilder;
        private final int contentLength;
        private final ClientLogger logger;
        private final boolean prettyPrintBody;
        private final String contentTypeHeader;

        private LoggingHttpResponse(HttpResponse actualResponse, LoggingEventBuilder logBuilder,
                                    ClientLogger logger, int contentLength, String contentTypeHeader,
                                    boolean prettyPrintBody) {
            super(actualResponse.getRequest());
            this.actualResponse = actualResponse;
            this.logBuilder = logBuilder;
            this.logger = logger;
            this.contentLength = contentLength;
            this.contentTypeHeader = contentTypeHeader;
            this.prettyPrintBody = prettyPrintBody;
        }

        @Override
        public int getStatusCode() {
            return actualResponse.getStatusCode();
        }

        @Override
        public String getHeaderValue(String name) {
            return actualResponse.getHeaderValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return actualResponse.getHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            TablesAccessibleByteArrayOutputStream stream = new TablesAccessibleByteArrayOutputStream(contentLength);

            return actualResponse.getBody()
                .doOnNext(byteBuffer -> {
                    try {
                        TablesLoggingUtils.writeByteBufferToStream(byteBuffer.duplicate(), stream);
                    } catch (IOException ex) {
                        throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
                    }
                })
                .doFinally(ignored -> logBuilder.addKeyValue(TablesLoggingKeys.BODY_KEY,
                        prettyPrintIfNeeded(logger, prettyPrintBody, contentTypeHeader,
                            stream.toString(StandardCharsets.UTF_8)))
                    .log("HTTP response"));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesFromNetworkResponse(getBody(), actualResponse.getHeaders());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray().map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }
    }

    private static String prettyPrintIfNeeded(ClientLogger logger, boolean prettyPrintBody, String contentType,
                                              String body) {
        String result = body;
        if (prettyPrintBody && contentType != null
            && (contentType.startsWith(ContentType.APPLICATION_JSON) || contentType.startsWith("text/json"))) {
            try {
                final Object deserialized = PRETTY_PRINTER.readTree(body);
                result = PRETTY_PRINTER.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warning("Failed to pretty print JSON", e);
            }
        }
        return result;
    }
}
