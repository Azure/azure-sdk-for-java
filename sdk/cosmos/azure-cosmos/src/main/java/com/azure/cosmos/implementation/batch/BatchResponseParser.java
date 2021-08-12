// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

public final class BatchResponseParser {

    private final static Logger logger = LoggerFactory.getLogger(BatchResponseParser.class);
    private final static char HYBRID_V1 = 129;

    /** Creates a transactional batch response from a documentServiceResponse.
     *
     * @param documentServiceResponse the {@link RxDocumentServiceResponse response message}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     * @param shouldPromoteOperationStatus indicates whether the operation status should be promoted.
     *
     * @return the {@link TransactionalBatchResponse transactional batch response} created
     * from {@link RxDocumentServiceResponse message} when the batch operation completes.
     */
    public static TransactionalBatchResponse fromDocumentServiceResponse(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        TransactionalBatchResponse response = null;
        final byte[] responseContent = documentServiceResponse.getResponseBodyAsByteArray();

        if (responseContent != null && responseContent.length > 0) {
            response = BatchResponseParser.populateFromResponseContent(documentServiceResponse, request, shouldPromoteOperationStatus);

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = BridgeInternal.createTransactionBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    "ServerResponseDeserializationFailure",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics());
            }
        }

        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = BatchExecUtils.getSubStatusCode(documentServiceResponse.getResponseHeaders());

        if (response == null) {
            response = BridgeInternal.createTransactionBatchResponse(
                responseStatusCode,
                responseSubStatusCode,
                null,
                documentServiceResponse.getResponseHeaders(),
                documentServiceResponse.getCosmosDiagnostics());
        }

        if (response.getSize() != request.getOperations().size()) {
            if (responseStatusCode >= 200 && responseStatusCode <= 299)  {
                // Server should be guaranteeing number of results equal to operations when
                // batch request is successful - so fail as InternalServerError if this is not the case.
                response = BridgeInternal.createTransactionBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    "Invalid server response",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics());
            }

            // When the overall response status code is TooManyRequests, propagate the RetryAfter into the individual operations.
            Duration retryAfterDuration = Duration.ZERO;
            if (responseStatusCode == HttpResponseStatus.TOO_MANY_REQUESTS.code()) {
                retryAfterDuration = BatchExecUtils.getRetryAfterDuration(documentServiceResponse.getResponseHeaders());
            }

            BatchResponseParser.createAndPopulateResults(response, request.getOperations(), retryAfterDuration);
        }

        checkState(response.getSize() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        return response;
    }

    private static TransactionalBatchResponse populateFromResponseContent(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        final List<TransactionalBatchOperationResult> results = new ArrayList<>(request.getOperations().size());
        final byte[] responseContent = documentServiceResponse.getResponseBodyAsByteArray();

        if (responseContent[0] != (byte)HYBRID_V1) {
            // Read from a json response body. To enable hybrid row just complete the else part
            final ObjectMapper mapper = Utils.getSimpleObjectMapper();

            try {
                final List<CosmosItemOperation> cosmosItemOperations = request.getOperations();
                final ObjectNode[] objectNodes = mapper.readValue(responseContent, ObjectNode[].class);

                for (int index = 0; index < objectNodes.length; index++) {
                    ObjectNode objectInArray = objectNodes[index];

                    results.add(
                        BatchResponseParser.createBatchOperationResultFromJson(objectInArray, cosmosItemOperations.get(index)));
                }
            } catch (IOException ex) {
                logger.error("Exception in parsing response", ex);
            }

        } else {
            // TODO(rakkuma): Implement hybrid row response parsing logic here.
            // Issue: https://github.com/Azure/azure-sdk-for-java/issues/15856
            logger.error("Hybrid row is not implemented right now");
            return null;
        }

        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = BatchExecUtils.getSubStatusCode(documentServiceResponse.getResponseHeaders());

        // Status code of the exact operation which failed.
        if (responseStatusCode == HttpResponseStatus.MULTI_STATUS.code() && shouldPromoteOperationStatus) {
            for (TransactionalBatchOperationResult result : results) {
                if (result.getStatusCode() !=  HttpResponseStatus.FAILED_DEPENDENCY.code() &&
                    result.getStatusCode() >= 400) {
                    responseStatusCode = result.getStatusCode();
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
            documentServiceResponse.getCosmosDiagnostics());

        BridgeInternal.addTransactionBatchResultInResponse(response, results);

        assert (response.getResults().size() == request.getOperations().size());

        return response;
    }

    /**
     * Read batch operation result result.
     *
     *  TODO(rakkuma): Similarly hybrid row result needs to be parsed.
     *  Issue: https://github.com/Azure/azure-sdk-for-java/issues/15856
     *
     * @param objectNode having response for a single operation.
     *
     * @return the result
     */
    private static TransactionalBatchOperationResult createBatchOperationResultFromJson(
        ObjectNode objectNode,
        CosmosItemOperation cosmosItemOperation) {

        final JsonSerializable jsonSerializable = new JsonSerializable(objectNode);

        final int statusCode = jsonSerializable.getInt(BatchRequestResponseConstants.FIELD_STATUS_CODE);
        Integer subStatusCode = jsonSerializable.getInt(BatchRequestResponseConstants.FIELD_SUBSTATUS_CODE);
        if (subStatusCode == null) {
            subStatusCode = HttpConstants.SubStatusCodes.UNKNOWN;
        }

        Double requestCharge = jsonSerializable.getDouble(BatchRequestResponseConstants.FIELD_REQUEST_CHARGE);
        if (requestCharge == null) {
            requestCharge = (double) 0;
        }

        final String eTag = jsonSerializable.getString(BatchRequestResponseConstants.FIELD_ETAG);
        final ObjectNode resourceBody = jsonSerializable.getObject(BatchRequestResponseConstants.FIELD_RESOURCE_BODY);
        final Integer retryAfterMilliseconds = jsonSerializable.getInt(BatchRequestResponseConstants.FIELD_RETRY_AFTER_MILLISECONDS);

        return BridgeInternal.createTransactionBatchResult(
            eTag,
            requestCharge,
            resourceBody,
            statusCode,
            retryAfterMilliseconds != null ? Duration.ofMillis(retryAfterMilliseconds) : Duration.ZERO,
            subStatusCode,
            cosmosItemOperation);
    }

    /**
     * Populate results to match number of operations to number of results in case of any error.
     *
     * @param response The transactionalBatchResponse in which to add the results
     * @param operations List of operations for which the wrapper TransactionalBatchResponse is returned.
     * @param retryAfterDuration retryAfterDuration.
     * */
    private static void createAndPopulateResults(final TransactionalBatchResponse response,
                                                 final List<CosmosItemOperation> operations,
                                                 final Duration retryAfterDuration) {
        final List<TransactionalBatchOperationResult> results = new ArrayList<>(operations.size());
        for (CosmosItemOperation cosmosItemOperation : operations) {
            results.add(
                BridgeInternal.createTransactionBatchResult(
                    null,
                    response.getRequestCharge(),
                    null,
                    response.getStatusCode(),
                    retryAfterDuration,
                    response.getSubStatusCode(),
                    cosmosItemOperation
                ));
        }

        BridgeInternal.addTransactionBatchResultInResponse(response, results);
    }
}
