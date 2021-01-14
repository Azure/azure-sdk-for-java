// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

class BatchResponsePayloadWriter {

    private List<TransactionalBatchOperationResult> results;

    BatchResponsePayloadWriter(List<TransactionalBatchOperationResult> results) {
        this.results = results;
    }

    String generatePayload() {
        return writeOperationResult().toString();
    }

    private ArrayNode writeOperationResult() {
        ArrayNode arrayNode =  Utils.getSimpleObjectMapper().createArrayNode();

        for(TransactionalBatchOperationResult result : results) {
            JsonSerializable operationJsonSerializable = writeResult(result);

            arrayNode.add(operationJsonSerializable.getPropertyBag());
        }
        return arrayNode;
    }

    private JsonSerializable writeResult(TransactionalBatchOperationResult result) {

        JsonSerializable jsonSerializable = new JsonSerializable();
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_STATUS_CODE, result.getStatusCode());
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_SUBSTATUS_CODE, result.getSubStatusCode());
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_ETAG, result.getETag());
        jsonSerializable.set(BatchRequestResponseConstants.FIELD_REQUEST_CHARGE, result.getRequestCharge());

        if(result.getRetryAfterDuration() != null) {
            jsonSerializable.set(BatchRequestResponseConstants.FIELD_RETRY_AFTER_MILLISECONDS, result.getRetryAfterDuration().toMillis());
        }

        return jsonSerializable;
    }
}
