// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.v2.util.BinaryData;
import com.azure.core.v2.util.polling.PollOperationDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PollOperationDetailsTests {

    @Test
    public void testDeserialization() {
        final String expectedOperationId = "Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs";

        String successResponseBody
            = "{\"id\":\"Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs\",\"status\":\"Succeeded\",\"error\":null}";
        PollOperationDetails pollOperationDetails
            = BinaryData.fromString(successResponseBody).toObject(PollOperationDetails.class);
        Assertions.assertEquals(expectedOperationId, pollOperationDetails.getOperationId());
        Assertions.assertNull(pollOperationDetails.getError());

        // dummy error response
        String failureResponseBody
            = "{\"id\":\"Uw_N1zoT53-rhHeaB5UJ_BaWKv8QVanU_Rjmo5FVdRs\",\"status\":\"Failed\",\"error\":{\"code\":\"CODE\",\"message\":\"MESSAGE\"}}";
        pollOperationDetails = BinaryData.fromString(failureResponseBody).toObject(PollOperationDetails.class);
        Assertions.assertEquals(expectedOperationId, pollOperationDetails.getOperationId());
        Assertions.assertNotNull(pollOperationDetails.getError());
        Assertions.assertEquals("CODE", pollOperationDetails.getError().getCode());
        Assertions.assertEquals("MESSAGE", pollOperationDetails.getError().getMessage());
    }
}
