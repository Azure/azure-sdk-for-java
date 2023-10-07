// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PollResultTests {

    @Test
    public void testDeserialization() {
        final String expectedOperationId = "Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs";

        String successResponseBody = "{\"id\":\"Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs\",\"status\":\"Succeeded\",\"error\":null}";
        PollResult pollResult = BinaryData.fromString(successResponseBody).toObject(PollResult.class);
        Assertions.assertEquals(expectedOperationId, pollResult.getOperationId());
        Assertions.assertNull(pollResult.getError());

        // dummy error response
        String failureResponseBody = "{\"id\":\"Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs\",\"status\":\"Failed\",\"error\":{\"code\":\"CODE\",\"message\":\"MESSAGE\"}}";
        pollResult = BinaryData.fromString(failureResponseBody).toObject(PollResult.class);
        Assertions.assertEquals(expectedOperationId, pollResult.getOperationId());
        Assertions.assertNotNull(pollResult.getError());
        Assertions.assertEquals("CODE", pollResult.getError().getCode());
        Assertions.assertEquals("MESSAGE", pollResult.getError().getMessage());
    }
}
