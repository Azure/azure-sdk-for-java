// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.*;

public class BatchResponsePayloadWriter {

    private List<TransactionalBatchOperationResult<?>> results;

    public BatchResponsePayloadWriter(List<TransactionalBatchOperationResult<?>> results) {
        this.results = results;
    }

    public String generatePayload() {
        return writeOperationResult().toString();
    }

    private ArrayNode writeOperationResult() {
        ArrayNode arrayNode =  Utils.getSimpleObjectMapper().createArrayNode();

        for(TransactionalBatchOperationResult<?> result : results) {
            JsonSerializable operationJsonSerializable = writeResult(result);

            arrayNode.add(operationJsonSerializable.getPropertyBag());
        }
        return arrayNode;
    }

    private JsonSerializable writeResult(TransactionalBatchOperationResult<?> result) {

        JsonSerializable jsonSerializable = new JsonSerializable();
        jsonSerializable.set(FIELD_STATUS_CODE, result.getStatus().code());
        jsonSerializable.set(FIELD_SUBSTATUS_CODE, result.getSubStatusCode());
        jsonSerializable.set(FIELD_ETAG, result.getETag());
        jsonSerializable.set(FIELD_RESOURCE_BODY, result.getResourceObject());

        return jsonSerializable;
    }
}
