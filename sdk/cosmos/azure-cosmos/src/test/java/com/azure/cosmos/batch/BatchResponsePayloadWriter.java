// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private JSONArray writeOperationResult() {
        JSONArray arr = new JSONArray();

        for(TransactionalBatchOperationResult<?> result : results) {
            JSONObject operationJson = writeResult(result);

            arr.put(operationJson);
        }

        return arr;
    }

    private JSONObject writeResult(TransactionalBatchOperationResult<?> result) {

        JSONObject obj = new JSONObject();
        obj.accumulate(FIELD_STATUS_CODE, result.getStatus().code());
        obj.accumulate(FIELD_SUBSTATUS_CODE, result.getSubStatusCode());
        obj.accumulate(FIELD_ETAG, result.getETag());
        obj.accumulate(FIELD_RESOURCE_BODY, result.getResourceObject());

        return obj;
    }
}
