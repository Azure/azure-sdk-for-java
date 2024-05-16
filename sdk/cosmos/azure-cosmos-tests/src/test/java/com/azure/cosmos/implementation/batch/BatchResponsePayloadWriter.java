// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

class BatchResponsePayloadWriter {

    private List<CosmosBatchOperationResult> results;

    BatchResponsePayloadWriter(List<CosmosBatchOperationResult> results) {
        this.results = results;
    }

    String generatePayload() {
        return writeOperationResult().toString();
    }

    private ArrayNode writeOperationResult() {
        ArrayNode arrayNode =  Utils.getSimpleObjectMapper().createArrayNode();

        for(CosmosBatchOperationResult result : results) {
            JsonSerializable operationJsonSerializable = writeResult(result);

            arrayNode.add(operationJsonSerializable.getPropertyBag());
        }
        return arrayNode;
    }

    private JsonSerializable writeResult(CosmosBatchOperationResult result) {

        JsonSerializable jsonSerializable = new JsonSerializable();
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_STATUS_CODE, result.getStatusCode(), CosmosItemSerializer.DEFAULT_SERIALIZER);
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_SUBSTATUS_CODE, result.getSubStatusCode(), CosmosItemSerializer.DEFAULT_SERIALIZER);
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_ETAG, result.getETag(), CosmosItemSerializer.DEFAULT_SERIALIZER);
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_REQUEST_CHARGE, result.getRequestCharge(), CosmosItemSerializer.DEFAULT_SERIALIZER);

        if(result.getRetryAfterDuration() != null) {
            jsonSerializable.set(BatchRequestResponseConstants.FIELD_RETRY_AFTER_MILLISECONDS, result.getRetryAfterDuration().toMillis(), CosmosItemSerializer.DEFAULT_SERIALIZER);
        }

        return jsonSerializable;
    }
}
