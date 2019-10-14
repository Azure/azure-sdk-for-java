// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.ServicesSubmitBatchResponse;
import com.azure.storage.blob.models.StorageErrorException;
import com.azure.storage.blob.models.StorageException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
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

    // This method connects the batch response values to the individual batch operations based on their Content-Id
    static Mono<SimpleResponse<Void>> mapBatchResponse(BlobBatch batch, ServicesSubmitBatchResponse batchResponse,
        boolean throwOnAnyFailure, ClientLogger logger) {
        /*
         * Content-Type will contain the boundary for each batch response. The expected format is:
         * "Content-Type: multipart/mixed; boundary=batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"
         */
        String contentType = batchResponse.getDeserializedHeaders().getContentType();

        // Split on the boundary [ "multipart/mixed; boundary", "batchresponse_66925647-d0cb-4109-b6d3-28efe3e1e5ed"]
        String boundary = contentType.split("=", 2)[1];

        return FluxUtil.collectBytesInByteBufferStream(batchResponse.getValue())
            .flatMap(byteArrayBody -> {
                String body = new String(byteArrayBody, StandardCharsets.UTF_8);

                // Split the batch response body into batch operation responses.
                for (String subResponse : body.split("--" + boundary)) {
                    // This is a split value that isn't a response.
                    if (!subResponse.contains("application/http")) {
                        continue;
                    }

                    // The batch operation response will be delimited by two new lines.
                    String[] subResponseSections = subResponse.split(HTTP_NEWLINE + HTTP_NEWLINE);

                    // The first section will contain batching metadata.
                    BlobBatchOperationResponse<?> batchOperationResponse =
                        getBatchOperation(batch, subResponseSections[0], logger);

                    // The second section will contain status code and header information.
                    setStatusCodeAndHeaders(batchOperationResponse, subResponseSections[1]);

                    // The third section will contain the body.
                    if (subResponseSections.length > 2) {
                        // The body is optional and may not exist.
                        setBodyOrPotentiallyThrow(batchOperationResponse, subResponseSections[2], throwOnAnyFailure,
                            logger);
                    }
                }

                return Mono.just(new SimpleResponse<>(batchResponse, null));
            });
    }

    private static BlobBatchOperationResponse<?> getBatchOperation(BlobBatch batch, String responseBatchInfo,
        ClientLogger logger) {
        Matcher contentIdMatcher = CONTENT_ID_PATTERN.matcher(responseBatchInfo);

        int contentId;
        if (contentIdMatcher.find()) {
            contentId = Integer.parseInt(contentIdMatcher.group(1));
        } else {
            throw logger.logExceptionAsError(
                new IllegalStateException("Batch operation response doesn't contain a 'Content-Id' header."));
        }

        return batch.getBatchRequest(contentId).setResponseReceived();
    }

    private static void setStatusCodeAndHeaders(BlobBatchOperationResponse<?> batchOperationResponse,
        String responseHeaders) {
        HttpHeaders headers = new HttpHeaders();
        for (String line : responseHeaders.split(HTTP_NEWLINE)) {
            if (ImplUtils.isNullOrEmpty(line)) {
                continue;
            }

            if (line.startsWith("HTTP")) {
                Matcher statusCodeMatcher = STATUS_CODE_PATTERN.matcher(line);
                if (statusCodeMatcher.find()) {
                    batchOperationResponse.setStatusCode(Integer.parseInt(statusCodeMatcher.group(1)));
                }
            } else {
                String[] headerPieces = line.split(":\\s*", 2);
                headers.put(headerPieces[0], headerPieces[1]);
            }
        }

        batchOperationResponse.setHeaders(headers);
    }

    private static void setBodyOrPotentiallyThrow(BlobBatchOperationResponse<?> batchOperationResponse,
        String responseBody, boolean throwOnError, ClientLogger logger) {
        /*
         * Currently no batching operations will return a success body, they will only return a body on an exception.
         * For now this will only construct the exception and throw if it should throw on an error.
         */
        StorageException exception = new StorageException(responseBody,
            batchOperationResponse.asHttpResponse(responseBody), responseBody);
        batchOperationResponse.setException(exception);

        if (throwOnError) {
            throw logger.logExceptionAsError(exception);
        }
    }
}
