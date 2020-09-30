// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS;
import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.SUB_STATUS;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

public final class BatchResponseParser {

    private final static Logger logger = LoggerFactory.getLogger(BatchResponseParser.class);
    private final static char HYBRID_V1 = 129;

    /** Creates a transactional batch response} from a exception
     *
     * @param throwable the {@link Throwable error}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     *
     * @return a Mono that provides the {@link TransactionalBatchResponse transactional batch response} created
     * from {@link TransactionalBatchResponse message} when the asynchronous operation completes.
     */
    public static Mono<TransactionalBatchResponse> fromErrorResponseAsync(
        final Throwable throwable,
        final ServerBatchRequest request) {

        if (throwable instanceof CosmosException) {
            final CosmosException cosmosException = (CosmosException) throwable;
            final TransactionalBatchResponse response = BridgeInternal.createTransactionBatchResponse(
                cosmosException.getStatusCode(),
                cosmosException.getSubStatusCode(),
                cosmosException.toString(),
                cosmosException.getResponseHeaders(),
                cosmosException.getDiagnostics(),
                request.getOperations());

            BatchResponseParser.createAndPopulateResults(response, request.getOperations(), cosmosException.getRetryAfterDuration());
            return Mono.just(response);
        } else {
            return Mono.error(throwable);
        }
    }

    /** Creates a transactional batch response} from a response message
     *
     * @param documentServiceResponse the {@link RxDocumentServiceResponse response message}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     * @param shouldPromoteOperationStatus indicates whether the operation status should be promoted.
     *
     * @return a Mono that provides the {@link TransactionalBatchResponse transactional batch response} created
     * from {@link RxDocumentServiceResponse message} when the asynchronous operation completes.
     */
    public static Mono<TransactionalBatchResponse> fromDocumentServiceResponseAsync(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        TransactionalBatchResponse response = null;
        final byte[] responseContent = documentServiceResponse.getResponseBodyAsByteArray();

        if (responseContent != null && responseContent.length > 0) {
            response = BatchResponseParser.populateFromResponseContentAsync(documentServiceResponse, request, shouldPromoteOperationStatus);

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = BridgeInternal.createTransactionBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    0,
                    "ServerResponseDeserializationFailure",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics(),
                    request.getOperations());
            }
        }

        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = Integer.parseInt(
            documentServiceResponse.getResponseHeaders().getOrDefault(SUB_STATUS, String.valueOf(0)));

        if (response == null) {
            response = BridgeInternal.createTransactionBatchResponse(
                responseStatusCode,
                responseSubStatusCode,
                null,
                documentServiceResponse.getResponseHeaders(),
                documentServiceResponse.getCosmosDiagnostics(),
                request.getOperations());
        }

        if (response.size() != request.getOperations().size()) {
            if (responseStatusCode >= 200 && responseStatusCode <= 299)  {
                // Server should be guaranteeing number of results equal to operations when
                // batch request is successful - so fail as InternalServerError if this is not the case.
                response = BridgeInternal.createTransactionBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    0,
                    "Invalid server response",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics(),
                    request.getOperations());
            }

            // When the overall response status code is TooManyRequests, propagate the RetryAfter into the individual operations.
            int retryAfterMilliseconds = 0;

            if (responseStatusCode == HttpResponseStatus.TOO_MANY_REQUESTS.code()) {
                String retryResponseValue = documentServiceResponse.getResponseHeaders().getOrDefault(RETRY_AFTER_IN_MILLISECONDS, null);
                if (StringUtils.isNotEmpty(retryResponseValue)) {
                    try {
                        retryAfterMilliseconds = Integer.parseInt(retryResponseValue);
                    } catch (NumberFormatException ex) {
                        // Do nothing. It's number format exception
                    }
                }
            }

            BatchResponseParser.createAndPopulateResults(response, request.getOperations(), Duration.ofMillis(retryAfterMilliseconds));
        }

        checkState(response.size() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        return Mono.just(response);
    }

    private static TransactionalBatchResponse populateFromResponseContentAsync(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        final ArrayList<TransactionalBatchOperationResult<?>> results = new ArrayList<>(request.getOperations().size());
        final byte[] responseContent = documentServiceResponse.getResponseBodyAsByteArray();

        if (responseContent[0] != (byte)HYBRID_V1) {
            // Read from a json response body. To enable hybrid row just complete the else part
            final ObjectMapper mapper = Utils.getSimpleObjectMapper();

            try {
                final ObjectNode[] objectNodes = mapper.readValue(responseContent, ObjectNode[].class);
                for (ObjectNode objectInArray : objectNodes) {
                    final TransactionalBatchOperationResult<?> batchOperationResult = BatchResponseParser.createBatchOperationResultFromJson(objectInArray);
                    results.add(batchOperationResult);
                }
            } catch (IOException ex) {
                logger.error("Exception in parsing response", ex);
            }

        } else {
            // TODO(rakkuma): Implement hybrid row response parsing logic here. Parse the response hybrid row buffer
            //  into array list of TransactionalBatchOperationResult. Remaining part is taken care from the caller function.
            logger.error("Hybrid row is not implemented right now");
            return null;
        }

        int responseStatusCode = documentServiceResponse.getStatusCode();
        Integer responseSubStatusCode = Integer.parseInt(
            documentServiceResponse.getResponseHeaders().getOrDefault(SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.UNKNOWN)));

        // Status code of the exact operation which failed.
        if (responseStatusCode ==  HttpResponseStatus.MULTI_STATUS.code()
            && shouldPromoteOperationStatus) {
            for (TransactionalBatchOperationResult<?> result : results) {
                if (result.getResponseStatus()!=  HttpResponseStatus.FAILED_DEPENDENCY.code()) {
                    responseStatusCode = result.getResponseStatus();
                    responseSubStatusCode = result.getSubStatusCode();
                    break;
                }
            }
        }

        final TransactionalBatchResponse response = BridgeInternal.createTransactionBatchResponse(
            responseStatusCode,
            responseSubStatusCode,
            null,
            documentServiceResponse.getResponseHeaders(),
            documentServiceResponse.getCosmosDiagnostics(),
            request.getOperations());

        BridgeInternal.addTransactionBatchResultInResponse(response, results);
        return response;
    }

    /**
     * Read batch operation result result.
     *
     *  TODO(rakkuma): Similarly hybrid row result needs to be parsed.
     *
     * @param objectNode having response for a single operation.
     *
     * @return the result
     */
    private static TransactionalBatchOperationResult<?> createBatchOperationResultFromJson(ObjectNode objectNode) {
        final JsonSerializable jsonSerializable = new JsonSerializable(objectNode);

        final int responseStatusCode = jsonSerializable.getInt(BatchRequestResponseConstant.FIELD_STATUS_CODE);
        final Integer subStatusCode = jsonSerializable.getInt(BatchRequestResponseConstant.FIELD_SUBSTATUS_CODE);
        final Double requestCharge = jsonSerializable.getDouble(BatchRequestResponseConstant.FIELD_REQUEST_CHARGE);
        final String eTag = jsonSerializable.getString(BatchRequestResponseConstant.FIELD_ETAG);
        final ObjectNode resourceBody = jsonSerializable.getObject(BatchRequestResponseConstant.FIELD_RESOURCE_BODY);
        final Integer retryAfterMilliseconds = jsonSerializable.getInt(BatchRequestResponseConstant.FIELD_RETRY_AFTER_MILLISECONDS);

        return BridgeInternal.createTransactionBatchResult(
            eTag,
            requestCharge,
            resourceBody,
            responseStatusCode,
            retryAfterMilliseconds != null ? Duration.ofMillis(retryAfterMilliseconds) : null,
            subStatusCode);
    }

    /**
     * Populate results to match number of operations to number of results in case of any error.
     *
     * @param response The transactionalBatchResponse in which to add the results
     * @param operations List of operations for which the wrapper TransactionalBatchResponse is returned.
     * @param retryAfterDuration retryAfterDuration.
     * */
    private static void createAndPopulateResults(final TransactionalBatchResponse response,
                                                 final List<ItemBatchOperation<?>> operations,
                                                 final Duration retryAfterDuration) {
        final ArrayList<TransactionalBatchOperationResult<?>> results = new ArrayList<>(operations.size());
        for (int i = 0; i < operations.size(); i++) {
            results.add(
                BridgeInternal.createTransactionBatchResult(
                    null,
                    response.getRequestCharge(),
                    null,
                    response.getResponseStatus(),
                    retryAfterDuration,
                    response.getSubStatusCode()
                ));
        }

        BridgeInternal.addTransactionBatchResultInResponse(response, results);
    }
}
