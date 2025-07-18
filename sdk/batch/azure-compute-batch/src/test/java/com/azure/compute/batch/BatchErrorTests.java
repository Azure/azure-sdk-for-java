// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.polling.SyncPoller;

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
}
