// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.implementation.batch.hybridrow.HybridRowBatchCodec;
import com.azure.cosmos.implementation.json.CosmosBinaryJacksonCodec;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

public final class BatchResponseParser {

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
        byte[] rawResponse = documentServiceResponse.getResponseBodyAsBytes();

        if (rawResponse != null && rawResponse.length > 0 && (rawResponse[0] & 0xFF) == 0x81) {
            try {
                response = populateFromHybridRow(
                    documentServiceResponse, request, shouldPromoteOperationStatus, rawResponse);
            } catch (RuntimeException decodingFailure) {
                response = deserializationFailure(documentServiceResponse, decodingFailure);
            }
        } else if (responseContentAsJson != null) {
            response = BatchResponseParser.populateFromResponseContent(documentServiceResponse, request, shouldPromoteOperationStatus);

            if (response == null) {
                // Convert any payload read failures as InternalServerError
                response = deserializationFailure(documentServiceResponse, null);
            }
        }

        int responseStatusCode = documentServiceResponse.getStatusCode();
        int responseSubStatusCode = BatchExecUtils.getSubStatusCode(documentServiceResponse.getResponseHeaders());

        if (response == null) {
            response = ModelBridgeInternal.createCosmosBatchResponse(
                responseStatusCode,
                responseSubStatusCode,
                null,
                documentServiceResponse.getResponseHeaders(),
                documentServiceResponse.getCosmosDiagnostics());
        }

        if (response.size() != request.getOperations().size()) {
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
        }

        checkState(response.size() == request.getOperations().size(),
            "Number of responses should be equal to number of operations in request.");

        return response;
    }

    private static CosmosBatchResponse deserializationFailure(
        RxDocumentServiceResponse serviceResponse, RuntimeException cause) {
        String message = cause == null
            ? "ServerResponseDeserializationFailure"
            : "ServerResponseDeserializationFailure: " + cause.getMessage();
        return ModelBridgeInternal.createCosmosBatchResponse(
            HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
            HttpConstants.SubStatusCodes.UNKNOWN,
            message,
            serviceResponse.getResponseHeaders(),
            serviceResponse.getCosmosDiagnostics());
    }

    private static CosmosBatchResponse populateFromHybridRow(
        RxDocumentServiceResponse serviceResponse,
        ServerBatchRequest request,
        boolean shouldPromoteOperationStatus,
        byte[] payload) {

        List<CosmosItemOperation> operations = request.getOperations();
        List<HybridRowBatchCodec.Result> wireResults = HybridRowBatchCodec.decodeResponse(
            payload, operations.size());
        if (wireResults.size() != operations.size()) {
            return null;
        }
        List<CosmosBatchOperationResult> results = new ArrayList<>(wireResults.size());
        for (int index = 0; index < wireResults.size(); index++) {
            HybridRowBatchCodec.Result wireResult = wireResults.get(index);
            results.add(ModelBridgeInternal.createCosmosBatchResult(
                wireResult.getETag(),
                wireResult.getRequestCharge(),
                decodeResourceBody(wireResult.getResourceBody()),
                wireResult.getStatusCode(),
                Duration.ofMillis(wireResult.getRetryAfterMilliseconds()),
                wireResult.getSubStatusCode(),
                operations.get(index)));
        }
        int statusCode = promotedStatus(serviceResponse.getStatusCode(), results, shouldPromoteOperationStatus);
        int subStatusCode = statusCode == serviceResponse.getStatusCode()
            ? BatchExecUtils.getSubStatusCode(serviceResponse.getResponseHeaders())
            : results.stream().filter(result -> result.getStatusCode() == statusCode)
                .map(CosmosBatchOperationResult::getSubStatusCode).findFirst().orElse(HttpConstants.SubStatusCodes.UNKNOWN);
        CosmosBatchResponse response = ModelBridgeInternal.createCosmosBatchResponse(
            statusCode, subStatusCode, null, serviceResponse.getResponseHeaders(), serviceResponse.getCosmosDiagnostics());
        ModelBridgeInternal.addCosmosBatchResultInResponse(response, results);
        return response;
    }

    private static ObjectNode decodeResourceBody(byte[] resourceBody) {
        if (resourceBody == null || resourceBody.length == 0) {
            return null;
        }
        JsonNode value;
        try {
            value = CosmosBinaryJacksonCodec.isBinaryFormat(resourceBody)
                ? CosmosBinaryJacksonCodec.decode(resourceBody)
                : Utils.getSimpleObjectMapper().readTree(resourceBody);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to decode batch resource body", error);
        }
        if (!(value instanceof ObjectNode)) {
            throw new IllegalStateException("Batch resource body is not an object");
        }
        return (ObjectNode) value;
    }

    private static int promotedStatus(
        int responseStatus, List<CosmosBatchOperationResult> results, boolean shouldPromoteOperationStatus) {
        if (responseStatus == HttpResponseStatus.MULTI_STATUS.code() && shouldPromoteOperationStatus) {
            for (CosmosBatchOperationResult result : results) {
                if (result.getStatusCode() != HttpResponseStatus.FAILED_DEPENDENCY.code()
                    && result.getStatusCode() >= 400) {
                    return result.getStatusCode();
                }
            }
        }
        return responseStatus;
    }

    private static CosmosBatchResponse populateFromResponseContent(
        final RxDocumentServiceResponse documentServiceResponse,
        final ServerBatchRequest request,
        final boolean shouldPromoteOperationStatus) {

        final List<CosmosBatchOperationResult> results = new ArrayList<>(request.getOperations().size());
        final ArrayNode responseContent = (ArrayNode)documentServiceResponse.getResponseBody();
        final List<CosmosItemOperation> cosmosItemOperations = request.getOperations();
        final ObjectNode[] objectNodes = new ObjectNode[responseContent.size()];
        int i = 0;
        for (Iterator<JsonNode> it = responseContent.iterator(); it.hasNext(); ) {
            JsonNode arrayItemNode = it.next();
            objectNodes[i] = (ObjectNode)arrayItemNode;
            i++;
        }

        for (int index = 0; index < objectNodes.length; index++) {
            ObjectNode objectInArray = objectNodes[index];

            results.add(
                BatchResponseParser.createBatchOperationResultFromJson(objectInArray, cosmosItemOperations.get(index)));
        }

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
     * Read a JSON batch operation result.
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
