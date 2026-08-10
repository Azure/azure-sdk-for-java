// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import reactor.core.publisher.Mono;

public class BatchErrorTests extends BatchClientTestBase {

    @SyncAsyncTest
    public void testResizeErrorCases() {
        try {
            BatchPoolResizeParameters emptyResizeParams = new BatchPoolResizeParameters();

            setPlaybackSyncPollerPollInterval(SyncAsyncExtension
                .execute(() -> batchClient.beginResizePool("fakepool-sync", emptyResizeParams), () -> Mono.fromCallable(
                    () -> batchAsyncClient.beginResizePool("fakepool-async", emptyResizeParams).getSyncPoller())));

        } catch (BatchErrorException err) {
            BatchError error = err.getValue();
            Assertions.assertNotNull(error);
            Assertions.assertEquals("MissingRequiredProperty", error.getCode());
            Assertions.assertTrue(
                error.getMessage().getValue().contains("A required property was not specified in the request body."));
            Assertions.assertEquals("targetDedicatedNodes and/or targetLowPriorityNodes",
                error.getValues().get(0).getValue());
        }

        try {
            BatchPoolResizeParameters resizeParams
                = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);
            setPlaybackSyncPollerPollInterval(SyncAsyncExtension
                .execute(() -> batchClient.beginResizePool("fakepool-sync", resizeParams), () -> Mono.fromCallable(
                    () -> batchAsyncClient.beginResizePool("fakepool-async", resizeParams).getSyncPoller())));
        } catch (BatchErrorException err) {
            BatchError error = err.getValue();
            Assertions.assertNotNull(error);
            Assertions.assertEquals("PoolNotFound", error.getCode());
            Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
            Assertions.assertNull(error.getValues());
        }
    }

    @Test
    public void testDeserializationOfBatchErrorWithMultipleValues() {
        String errorJson = "{" + "\"code\": \"InvalidQueryParameterValue\","
            + "\"message\": {\"lang\": \"en-us\", \"value\": \"Value for one of the query parameters specified in the request URI is invalid\"},"
            + "\"values\": [" + "  {\"key\": \"QueryParameterName\", \"value\": \"state\"},"
            + "  {\"key\": \"QueryParameterValue\", \"value\": \"deleted\"},"
            + "  {\"key\": \"Reason\", \"value\": \"invalid state\"}" + "]" + "}";

        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(errorJson))) {
            BatchError error = BatchError.fromJson(jsonReader);

            Assertions.assertNotNull(error);
            Assertions.assertEquals("InvalidQueryParameterValue", error.getCode());
            Assertions.assertNotNull(error.getMessage());
            Assertions.assertEquals("Value for one of the query parameters specified in the request URI is invalid",
                error.getMessage().getValue());

            List<BatchErrorDetail> values = error.getValues();
            Assertions.assertNotNull(values);
            Assertions.assertEquals(3, values.size());

            Assertions.assertEquals("QueryParameterName", values.get(0).getKey());
            Assertions.assertEquals("state", values.get(0).getValue());

            Assertions.assertEquals("QueryParameterValue", values.get(1).getKey());
            Assertions.assertEquals("deleted", values.get(1).getValue());

            Assertions.assertEquals("Reason", values.get(2).getKey());
            Assertions.assertEquals("invalid state", values.get(2).getValue());
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize BatchError", e);
        }
    }
}
