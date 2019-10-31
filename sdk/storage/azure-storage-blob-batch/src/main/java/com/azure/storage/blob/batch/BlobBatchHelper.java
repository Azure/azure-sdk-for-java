// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.ServicesSubmitBatchResponse;
import com.azure.storage.blob.models.BlobStorageException;
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

    // This method connects the batch response values to the individual batch operations based on their Content-Id
    static Mono<SimpleResponse<Void>> mapBatchResponse(BlobBatch batch, ServicesSubmitBatchResponse batchResponse,
        boolean throwOnAnyFailure, ClientLogger logger) {
        /*
         * Content-Type will contain the boundary for each batch response. The expected format is:
         * "Content-Type: multipart/mixed; boundary=batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"
         */
        String contentType = batchResponse.getDeserializedHeaders().getContentType();

        // Split on the boundary [ "multipart/mixed; boundary", "batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"]
        String[] boundaryPieces = contentType.split("=", 2);
        if (boundaryPieces.length == 1) {
            return Mono.error(logger
                .logExceptionAsError(new IllegalStateException("Response doesn't contain a boundary.")));
        }

        String boundary = boundaryPieces[1];

        return FluxUtil.collectBytesInByteBufferStream(batchResponse.getValue())
            .flatMap(byteArrayBody -> Mono.fromRunnable(() -> {
                String body = new String(byteArrayBody, StandardCharsets.UTF_8);
                List<BlobStorageException> exceptions = new ArrayList<>();

                String[] subResponses = body.split("--" + boundary);
                if (subResponses.length == 3 && batch.getOperationCount() != 1) {
                    String[] exceptionSections = subResponses[1].split(HTTP_NEWLINE + HTTP_NEWLINE);
                    int statusCode = getStatusCode(exceptionSections[1], logger);
                    HttpHeaders headers = getHttpHeaders(exceptionSections[1]);

                    throw logger.logExceptionAsError(new BlobStorageException(headers.getValue("x-ms-error-code"),
                        createHttpResponse(batchResponse.getRequest(), statusCode, headers, body), body));
                }

                // Split the batch response body into batch operation responses.
                for (String subResponse : subResponses) {
                    // This is a split value that isn't a response.
                    if (!APPLICATION_HTTP_PATTERN.matcher(subResponse).find()) {
                        continue;
                    }

                    // The batch operation response will be delimited by two new lines.
                    String[] subResponseSections = subResponse.split(HTTP_NEWLINE + HTTP_NEWLINE);

                    // The first section will contain batching metadata.
                    BlobBatchOperationResponse<?> batchOperationResponse =
                        getBatchOperation(batch, subResponseSections[0], logger);

                    // The second section will contain status code and header information.
                    batchOperationResponse.setStatusCode(getStatusCode(subResponseSections[1], logger));
                    batchOperationResponse.setHeaders(getHttpHeaders(subResponseSections[1]));

                    // The third section will contain the body.
                    if (subResponseSections.length > 2) {
                        // The body is optional and may not exist.
                        setBodyOrAddException(batchOperationResponse, subResponseSections[2], exceptions);
                    }
                }

                if (throwOnAnyFailure && exceptions.size() != 0) {
                    throw logger.logExceptionAsError(new BlobBatchStorageException("Batch had operation failures.",
                        createHttpResponse(batchResponse), exceptions));
                }

                new SimpleResponse<>(batchResponse, null);
            }));
    }

    private static BlobBatchOperationResponse<?> getBatchOperation(BlobBatch batch, String responseBatchInfo,
        ClientLogger logger) {
        Matcher contentIdMatcher = CONTENT_ID_PATTERN.matcher(responseBatchInfo);

        int contentId;
        if (contentIdMatcher.find() && contentIdMatcher.groupCount() >= 1) {
            contentId = Integer.parseInt(contentIdMatcher.group(1));
        } else {
            throw logger.logExceptionAsError(
                new IllegalStateException("Batch operation response doesn't contain a 'Content-Id' header."));
        }

        return batch.getBatchRequest(contentId).setResponseReceived();
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

        for (String line : responseMetadata.split(HTTP_NEWLINE)) {
            if (CoreUtils.isNullOrEmpty(line) || (line.startsWith("HTTP") && !line.contains(":"))) {
                continue;
            }

            String[] headerPieces = line.split(":\\s*", 2);
            if (headerPieces.length == 1) {
                headers.put(headerPieces[0], null);
            } else {
                headers.put(headerPieces[0], headerPieces[1]);
            }
        }

        return headers;
    }

    private static void setBodyOrAddException(BlobBatchOperationResponse<?> batchOperationResponse,
        String responseBody, List<BlobStorageException> exceptions) {
        /*
         * Currently no batching operations will return a success body, they will only return a body on an exception.
         * For now this will only construct the exception and throw if it should throw on an error.
         */
        BlobStorageException exception = new BlobStorageException(responseBody,
            batchOperationResponse.asHttpResponse(responseBody), responseBody);
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

    private static HttpResponse createHttpResponse(ServicesSubmitBatchResponse response) {
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
