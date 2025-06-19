// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;

public class BatchErrorTests extends BatchClientTestBase {

    @SyncAsyncTest
    public void testResizeErrorCases() {
        try {
            BatchPoolResizeParameters emptyResizeParams = new BatchPoolResizeParameters();
            SyncAsyncExtension.execute(() -> batchClient.resizePool("fakepool-sync", emptyResizeParams),
                () -> batchAsyncClient.resizePool("fakepool-async", emptyResizeParams));
        } catch (HttpResponseException err) {
            BatchError error = BatchError.fromException(err);
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
            SyncAsyncExtension.execute(() -> batchClient.resizePool("fakepool-sync", resizeParams),
                () -> batchAsyncClient.resizePool("fakepool-async", resizeParams));
        } catch (HttpResponseException err) {
            BatchError error = BatchError.fromException(err);
            Assertions.assertNotNull(error);
            Assertions.assertEquals("PoolNotFound", error.getCode());
            Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
            Assertions.assertNull(error.getValues());
        }
    }
}
