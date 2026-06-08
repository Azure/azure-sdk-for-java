// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

public final class BatchResponseParser {
    private static final Logger logger = LoggerFactory.getLogger(BatchResponseParser.class);
    private static final int MAX_LOGGED_OPERATION_LENGTH = 256;

    /** Creates a transactional batch response from a documentServiceResponse.
     *
     * @param documentServiceResponse the {@link RxDocumentServiceResponse response message}.
     * @param request the {@link ServerBatchRequest batch request} that produced {@code message}.
     * @param shouldPromoteOperationStatus indicates whether the operation status should be promoted.
     *
     * @return the {@link CosmosBatchResponse cosmos batch response} created
     * from {@link RxDocumentServiceResponse message} when the batch operation completes.
     */
    public static CosmosBatchResponse fromDocumentServiceResponse(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        CosmosBatchResponse response = null;
        final JsonNode responseContentAsJson = documentServiceResponse.getResponseBody();
        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = BatchExecUtils.getSubStatusCode(documentServiceResponse.getResponseHeaders());

        logger.info(
            "BatchResponseParser - received batch response: requestOperationCount={}, outerStatusCode={}, "
                + "outerSubStatusCode={}, payloadLength={}, payloadPresent={}, isAtomicBatch={}, shouldContinueOnError={}",
            request.getOperations().size(),
            responseStatusCode,
            responseSubStatusCode,
            documentServiceResponse.getResponsePayloadLength(),
            responseContentAsJson != null,
            request.isAtomicBatch(),
            request.isShouldContinueOnError());

        if (responseContentAsJson != null) {
            logger.info("BatchResponseParser - full raw batch response payload: {}", responseContentAsJson);

            try {
                response = BatchResponseParser.populateFromResponseContent(documentServiceResponse, request, shouldPromoteOperationStatus);
            } catch (RuntimeException exception) {
                logger.error(
                    "BatchResponseParser - failed to parse batch response payload. outerStatusCode={}, "
                        + "outerSubStatusCode={}, payload={}",
                    responseStatusCode,
                    responseSubStatusCode,
                    responseContentAsJson,
                    exception);
                throw exception;
            }

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = ModelBridgeInternal.createCosmosBatchResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    "ServerResponseDeserializationFailure",
                    documentServiceResponse.getResponseHeaders(),
                    documentServiceResponse.getCosmosDiagnostics());
            }
        }

        if (response == null) {
            logger.info(
                "BatchResponseParser - no response payload was parsed; creating batch response from outer status. "
                    + "outerStatusCode={}, outerSubStatusCode={}",
                responseStatusCode,
                responseSubStatusCode);

            response = ModelBridgeInternal.createCosmosBatchResponse(
                responseStatusCode,
                responseSubStatusCode,
                null,
                documentServiceResponse.getResponseHeaders(),
                documentServiceResponse.getCosmosDiagnostics());
        }

        if (response.size() != request.getOperations().size()) {
            logger.warn(
                "BatchResponseParser - parsed result count mismatch. parsedResultCount={}, requestOperationCount={}, "
                    + "outerStatusCode={}, parsedBatchStatusCode={}, parsedBatchSubStatusCode={}",
                response.size(),
                request.getOperations().size(),
                responseStatusCode,
                response.getStatusCode(),
                response.getSubStatusCode());

            if (responseStatusCode >= 200 && responseStatusCode <= 299)  {
                // Server should be guaranteeing number of results equal to operations when
                // batch request is successful - so fail as InternalServerError if this is not the case.
                response = ModelBridgeInternal.createCosmosBatchResponse(
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

            logParsedResults("BatchResponseParser - synthesized result", response.getResults());
        }

        checkState(response.size() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        logger.info(
            "BatchResponseParser - final batch response: statusCode={}, subStatusCode={}, resultCount={}",
            response.getStatusCode(),
            response.getSubStatusCode(),
            response.size());

        return response;
    }

    private static CosmosBatchResponse populateFromResponseContent(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        final List<CosmosBatchOperationResult> results = new ArrayList<>(request.getOperations().size());
        final ArrayNode responseContent = (ArrayNode)documentServiceResponse.getResponseBody();
        final List<CosmosItemOperation> cosmosItemOperations = request.getOperations();
        final ObjectNode[] objectNodes = new ObjectNode[responseContent.size()];

        logger.info(
            "BatchResponseParser - parsing batch response content: responseArraySize={}, requestOperationCount={}",
            responseContent.size(),
            request.getOperations().size());

        int i = 0;
        for (Iterator<JsonNode> it = responseContent.iterator(); it.hasNext(); ) {
            JsonNode arrayItemNode = it.next();
            objectNodes[i] = (ObjectNode)arrayItemNode;
            i++;
        }

        for (int index = 0; index < objectNodes.length; index++) {
            ObjectNode objectInArray = objectNodes[index];
            CosmosBatchOperationResult result = BatchResponseParser.createBatchOperationResultFromJson(
                objectInArray,
                cosmosItemOperations.get(index));

            if (!result.isSuccessStatusCode()) {
                logger.info("BatchResponseParser - raw failed operation result[{}]: {}", index, objectInArray);
            }

            results.add(result);
        }

        logger.info("BatchResponseParser - parsed result status summary: {}", summarizeStatuses(results));
        logParsedResults("BatchResponseParser - parsed result", results);

        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = BatchExecUtils.getSubStatusCode(documentServiceResponse.getResponseHeaders());

        // Status code of the exact operation which failed.
        if (responseStatusCode == HttpResponseStatus.MULTI_STATUS.code() && shouldPromoteOperationStatus) {
            for (CosmosBatchOperationResult result : results) {
                if (result.getStatusCode() !=  HttpResponseStatus.FAILED_DEPENDENCY.code() &&
                    result.getStatusCode() >= 400) {
                    responseStatusCode = result.getStatusCode();
                    responseSubStatusCode = result.getSubStatusCode();
                    break;
                }
            }
        }

        final CosmosBatchResponse response = ModelBridgeInternal.createCosmosBatchResponse(
            responseStatusCode,
            responseSubStatusCode,
            null,
            documentServiceResponse.getResponseHeaders(),
            documentServiceResponse.getCosmosDiagnostics());

        ModelBridgeInternal.addCosmosBatchResultInResponse(response, results);

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
    private static CosmosBatchOperationResult createBatchOperationResultFromJson(
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

        return ModelBridgeInternal.createCosmosBatchResult(
            eTag,
            requestCharge,
            resourceBody,
            statusCode,
            retryAfterMilliseconds != null ? Duration.ofMillis(retryAfterMilliseconds) : Duration.ZERO,
            subStatusCode,
            cosmosItemOperation);
    }

    private static void logParsedResults(String messagePrefix, List<CosmosBatchOperationResult> results) {
        for (int index = 0; index < results.size(); index++) {
            CosmosBatchOperationResult result = results.get(index);
            if (result.isSuccessStatusCode()) {
                continue;
            }

            CosmosItemOperation operation = result.getOperation();

            logger.info(
                "{}[{}]: operationType={}, operationId={}, partitionKey={}, statusCode={}, "
                    + "subStatusCode={}, requestCharge={}, retryAfter={}",
                messagePrefix,
                index,
                operation != null ? operation.getOperationType() : null,
                operation != null ? summarize(operation.getId()) : null,
                operation != null && operation.getPartitionKeyValue() != null
                    ? summarize(operation.getPartitionKeyValue().toString())
                    : null,
                result.getStatusCode(),
                result.getSubStatusCode(),
                result.getRequestCharge(),
                result.getRetryAfterDuration());
        }
    }

    private static String summarizeStatuses(List<CosmosBatchOperationResult> results) {
        StringBuilder builder = new StringBuilder("[");

        for (int index = 0; index < results.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }

            CosmosBatchOperationResult result = results.get(index);
            builder.append(index)
                .append(":")
                .append(result.getStatusCode())
                .append("/")
                .append(result.getSubStatusCode());
        }

        builder.append("]");
        return builder.toString();
    }

    private static String summarize(String value) {
        if (value == null || value.length() <= MAX_LOGGED_OPERATION_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_LOGGED_OPERATION_LENGTH) + "...(len=" + value.length() + ")";
    }

    /**
     * Populate results to match number of operations to number of results in case of any error.
     *
     * @param response The transactionalBatchResponse in which to add the results
     * @param operations List of operations for which the wrapper TransactionalBatchResponse is returned.
     * @param retryAfterDuration retryAfterDuration.
     * */
    private static void createAndPopulateResults(final CosmosBatchResponse response,
                                                 final List<CosmosItemOperation> operations,
                                                 final Duration retryAfterDuration) {
        final List<CosmosBatchOperationResult> results = new ArrayList<>(operations.size());
        for (CosmosItemOperation cosmosItemOperation : operations) {
            results.add(
                ModelBridgeInternal.createCosmosBatchResult(
                    null,
                    response.getRequestCharge(),
                    null,
                    response.getStatusCode(),
                    retryAfterDuration,
                    response.getSubStatusCode(),
                    cosmosItemOperation
                ));
        }

        ModelBridgeInternal.addCosmosBatchResultInResponse(response, results);
    }
}
