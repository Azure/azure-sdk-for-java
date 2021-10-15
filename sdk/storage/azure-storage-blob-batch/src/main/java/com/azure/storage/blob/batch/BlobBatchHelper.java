// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class contains helper methods for dealing with batch requests.
 */
class BlobBatchHelper {
    /*
     * Newline characters used in HTTP
     */
    static final String HTTP_NEWLINE = "\r\n";

    /*
     * This pattern matches finding the "Content-Id" of the batch response.
     */
    private static final Pattern CONTENT_ID_PATTERN = Pattern
        .compile("Content-ID:\\s?(\\d+)", Pattern.CASE_INSENSITIVE);

    /*
     * This pattern matches finding the status code of the batch response.
     */
    private static final Pattern STATUS_CODE_PATTERN = Pattern
        .compile("HTTP\\/\\d\\.\\d\\s?(\\d+)\\s?\\w+", Pattern.CASE_INSENSITIVE);

    /*
     * This pattern matches finding the 'application/http' portion of the body.
     */
    private static final Pattern APPLICATION_HTTP_PATTERN = Pattern
        .compile("application\\/http", Pattern.CASE_INSENSITIVE);

    /*
     * The following patterns were previously used in 'String.split' calls. 'String.split' internally compiles the
     * String pattern into a Pattern regex if it is larger than one character, or two if-and-only-if it is an escaped
     * single character. Compiling these patterns here will help reduce the number of regex compilations, greatly
     * improving performance.
     */
    private static final Pattern HTTP_NEWLINE_PATTERN = Pattern.compile(HTTP_NEWLINE);
    private static final Pattern HTTP_DOUBLE_NEWLINE_PATTERN = Pattern.compile(HTTP_NEWLINE + HTTP_NEWLINE_PATTERN);
    private static final Pattern HTTP_HEADER_SPLIT_PATTERN = Pattern.compile(":\\s*");

    // This method connects the batch response values to the individual batch operations based on their Content-Id
    static Mono<SimpleResponse<Void>> mapBatchResponse(BlobBatchOperationInfo batchOperationInfo,
        Response<Flux<ByteBuffer>> rawResponse, boolean throwOnAnyFailure, ClientLogger logger) {
        /*
         * Content-Type will contain the boundary for each batch response. The expected format is:
         * "Content-Type: multipart/mixed; boundary=batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"
         */
        String contentType = rawResponse.getHeaders().getValue(Constants.HeaderConstants.CONTENT_TYPE);

        // Split on the boundary [ "multipart/mixed; boundary", "batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"]
        String[] boundaryPieces = contentType.split("=", 2);
        if (boundaryPieces.length == 1) {
            return Mono.error(logger
                .logExceptionAsError(new IllegalStateException("Response doesn't contain a boundary.")));
        }

        String boundary = boundaryPieces[1];

        return FluxUtil.collectBytesInByteBufferStream(rawResponse.getValue())
            /*
             * This has been changed from using 'Mono.fromRunnable' to 'Mono.create' to resolve an issue where iterating
             * the responses resulted in 0 responses being returned. The reason that this occurred is that
             * 'Mono.fromRunnable' only returns an 'onComplete' trigger with no response value, resulting on all
             * downstream operators being skipped as there is no 'onNext' trigger. Effectively, the request and response
             * worked correctly but emitting the responses failed.
             *
             * This change has an additional benefit in that the MonoSink used in 'Mono.create' now allows for 'onError'
             * emissions to occur instead of escaping the reactive stream boundaries when throwing an exception.
             */
            .flatMap(byteArrayBody -> Mono.create(sink -> {
                String body = new String(byteArrayBody, StandardCharsets.UTF_8);
                List<BlobStorageException> exceptions = new ArrayList<>();

                String[] subResponses = body.split("--" + boundary);
                if (subResponses.length == 3 && batchOperationInfo.getOperationCount() != 1) {
                    String[] exceptionSections = HTTP_DOUBLE_NEWLINE_PATTERN.split(subResponses[1]);
                    int statusCode = getStatusCode(exceptionSections[1], logger);
                    HttpHeaders headers = getHttpHeaders(exceptionSections[1]);

                    sink.error(logger.logExceptionAsError(new BlobStorageException(
                        headers.getValue(Constants.HeaderConstants.ERROR_CODE),
                        createHttpResponse(rawResponse.getRequest(), statusCode, headers, body), body)));
                }

                // Split the batch response body into batch operation responses.
                for (String subResponse : subResponses) {
                    // This is a split value that isn't a response.
                    if (!APPLICATION_HTTP_PATTERN.matcher(subResponse).find()) {
                        continue;
                    }

                    // The batch operation response will be delimited by two new lines.
                    String[] subResponseSections = HTTP_DOUBLE_NEWLINE_PATTERN.split(subResponse);

                    // The first section will contain batching metadata.
                    BlobBatchOperationResponse<?> batchOperationResponse =
                        getBatchOperation(batchOperationInfo, subResponseSections[0], logger);

                    // The second section will contain status code and header information.
                    batchOperationResponse.setStatusCode(getStatusCode(subResponseSections[1], logger));
                    batchOperationResponse.setHeaders(getHttpHeaders(subResponseSections[1]));

                    // The third section will contain the body.
                    if (subResponseSections.length > 2) {
                        // The body is optional and may not exist.
                        setBodyOrAddException(batchOperationResponse, subResponseSections[2], exceptions, logger);
                    }
                }

                if (throwOnAnyFailure && exceptions.size() != 0) {
                    sink.error(logger.logExceptionAsError(new BlobBatchStorageException("Batch had operation failures.",
                        createHttpResponse(rawResponse), exceptions)));
                }

                sink.success(new SimpleResponse<>(rawResponse, null));
            }));
    }

    private static BlobBatchOperationResponse<?> getBatchOperation(BlobBatchOperationInfo batchOperationInfo,
        String responseBatchInfo, ClientLogger logger) {
        Matcher contentIdMatcher = CONTENT_ID_PATTERN.matcher(responseBatchInfo);

        int contentId;
        if (contentIdMatcher.find() && contentIdMatcher.groupCount() >= 1) {
            contentId = Integer.parseInt(contentIdMatcher.group(1));
        } else {
            throw logger.logExceptionAsError(
                new IllegalStateException("Batch operation response doesn't contain a 'Content-Id' header."));
        }

        return batchOperationInfo.getBatchRequest(contentId).setResponseReceived();
    }

    private static int getStatusCode(String responseMetadata, ClientLogger logger) {
        Matcher statusCodeMatcher = STATUS_CODE_PATTERN.matcher(responseMetadata);
        if (statusCodeMatcher.find()) {
            return Integer.parseInt(statusCodeMatcher.group(1));
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("Unable to parse response status code."));
        }
    }

    private static HttpHeaders getHttpHeaders(String responseMetadata) {
        HttpHeaders headers = new HttpHeaders();

        for (String line : HTTP_NEWLINE_PATTERN.split(responseMetadata)) {
            if (CoreUtils.isNullOrEmpty(line) || (line.startsWith("HTTP") && !line.contains(":"))) {
                continue;
            }

            String[] headerPieces = HTTP_HEADER_SPLIT_PATTERN.split(line, 2);
            if (headerPieces.length == 1) {
                headers.set(headerPieces[0], (String) null);
            } else {
                headers.set(headerPieces[0], headerPieces[1]);
            }
        }

        return headers;
    }

    private static void setBodyOrAddException(BlobBatchOperationResponse<?> batchOperationResponse,
        String responseBody, List<BlobStorageException> exceptions, ClientLogger logger) {
        /*
         * Currently, no batching operations will return a success body, they will only return a body on an exception.
         * For now this will only construct the exception and throw if it should throw on an error.
         */
        BlobStorageException exception = new BlobStorageException(responseBody,
            batchOperationResponse.asHttpResponse(responseBody), responseBody);
        logger.logExceptionAsError(exception);
        batchOperationResponse.setException(exception);
        exceptions.add(exception);
    }

    static HttpResponse createHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, String body) {
        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                return headers.getValue(name);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(body.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(body);
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return getBodyAsByteArray().map(body -> new String(body, charset));
            }
        };
    }

    private static HttpResponse createHttpResponse(Response<Flux<ByteBuffer>> response) {
        return new HttpResponse(response.getRequest()) {
            @Override
            public int getStatusCode() {
                return response.getStatusCode();
            }

            @Override
            public String getHeaderValue(String name) {
                return response.getHeaders().getValue(name);
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return response.getValue();
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return FluxUtil.collectBytesInByteBufferStream(getBody());
            }

            @Override
            public Mono<String> getBodyAsString() {
                return getBodyAsByteArray().map(body -> new String(body, StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return getBodyAsByteArray().map(body -> new String(body, charset));
            }
        };
    }
}
