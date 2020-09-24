// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.batch.TransactionalBatchOperationResult;
import com.azure.cosmos.batch.TransactionalBatchResponse;
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

import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_ETAG;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_REQUEST_CHARGE;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_RESOURCE_BODY;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_RETRY_AFTER_MILLISECONDS;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_STATUS_CODE;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.FIELD_SUBSTATUS_CODE;
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
            final TransactionalBatchResponse response =  new TransactionalBatchResponse(
                cosmosException.getStatusCode(),
                cosmosException.getSubStatusCode(),
                cosmosException.getMessage(),
                cosmosException.getResponseHeaders(),
                cosmosException.getDiagnostics(),
                request.getOperations());

            response.createAndPopulateResults(request.getOperations(), 0);
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
        final String responseContent = documentServiceResponse.getResponseBodyAsString();

        if (StringUtils.isNotEmpty(responseContent)) {
            response = BatchResponseParser.populateFromResponseContentAsync(documentServiceResponse, request, shouldPromoteOperationStatus);

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = new TransactionalBatchResponse(
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
            response = new TransactionalBatchResponse(
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
                response = new TransactionalBatchResponse(
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

            response.createAndPopulateResults(request.getOperations(), retryAfterMilliseconds);
        }

        checkState(response.size() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        return Mono.just(response);
    }

    private static TransactionalBatchResponse populateFromResponseContentAsync(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        final ArrayList<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        final String responseContent = documentServiceResponse.getResponseBodyAsString();

        if (responseContent.charAt(0) != HYBRID_V1) {
            // Read from a json response body. To enable hybrid row just complete the else part
            final ObjectMapper mapper = Utils.getSimpleObjectMapper();

            try{
                final ObjectNode[] objectNodes = mapper.readValue(documentServiceResponse.getResponseBodyAsString(), ObjectNode[].class);
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
        int responseSubStatusCode = Integer.parseInt(
            documentServiceResponse.getResponseHeaders().getOrDefault(SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.UNKNOWN)));

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

        final TransactionalBatchResponse response = new TransactionalBatchResponse(
            responseStatusCode,
            responseSubStatusCode,
            null,
            documentServiceResponse.getResponseHeaders(),
            documentServiceResponse.getCosmosDiagnostics(),
            request.getOperations());

        response.addAll(results);

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
        final TransactionalBatchOperationResult<?> transactionalBatchOperationResult = new TransactionalBatchOperationResult<>();

        final JsonSerializable jsonSerializable = new JsonSerializable(objectNode);
        transactionalBatchOperationResult.setResponseStatus(jsonSerializable.getInt(FIELD_STATUS_CODE));

        final Integer subStatusCode = jsonSerializable.getInt(FIELD_SUBSTATUS_CODE);
        if(subStatusCode != null) {
            transactionalBatchOperationResult.setSubStatusCode(subStatusCode);
        }

        final Double requestCharge = jsonSerializable.getDouble(FIELD_REQUEST_CHARGE);
        if(requestCharge != null) {
            transactionalBatchOperationResult.setRequestCharge(requestCharge);
        }

        final Integer retryAfterMilliseconds = jsonSerializable.getInt(FIELD_RETRY_AFTER_MILLISECONDS);
        if(retryAfterMilliseconds != null) {
            transactionalBatchOperationResult.setRetryAfter(Duration.ofMillis(retryAfterMilliseconds));
        }

        final String etag = jsonSerializable.getString(FIELD_ETAG);
        if(etag != null) {
            transactionalBatchOperationResult.setETag(etag);
        }

        final ObjectNode resourceBody = jsonSerializable.getObject(FIELD_RESOURCE_BODY);
        if(resourceBody != null) {
            transactionalBatchOperationResult.setResourceObject(resourceBody);
        }

        return transactionalBatchOperationResult;
    }
}
