// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BatchErrorTests extends BatchClientTestBase {

    @Test
    public void testResizeErrorCases() {
        try {

            BatchPoolResizeContent resizeContent = new BatchPoolResizeContent();
            batchClient.resizePool("fakepool", resizeContent);
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

            batchClient.resizePool("fakepool",
                new BatchPoolResizeContent().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1));
        } catch (BatchErrorException err) {

            BatchError error = err.getValue();
            Assertions.assertNotNull(error);
            Assertions.assertEquals("PoolNotFound", error.getCode());
            Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
            Assertions.assertNull(error.getValues());
        }
    }
}
