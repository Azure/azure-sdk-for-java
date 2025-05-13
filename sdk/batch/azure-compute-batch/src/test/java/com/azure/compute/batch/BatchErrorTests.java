// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.azure.core.exception.HttpResponseException;

public class BatchErrorTests extends BatchClientTestBase {

    @Test
    public void testResizeErrorCases() {
        try {

            BatchPoolResizeParameters resizeParameters = new BatchPoolResizeParameters();
            batchClient.resizePool("fakepool", resizeParameters);
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

            batchClient.resizePool("fakepool",
                new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1));
        } catch (HttpResponseException err) {

            BatchError error = BatchError.fromException(err);
            Assertions.assertNotNull(error);
            Assertions.assertEquals("PoolNotFound", error.getCode());
            Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
            Assertions.assertNull(error.getValues());
        }
    }
}
