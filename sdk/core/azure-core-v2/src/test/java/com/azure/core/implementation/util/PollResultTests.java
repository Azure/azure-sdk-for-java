// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.v2.util.polling.LongRunningOperationStatus;
import com.azure.core.v2.util.polling.implementation.PollResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PollResultTests {

    @Test
    public void testPollResultStatus() {
        Assertions.assertEquals(LongRunningOperationStatus.NOT_STARTED,
            new PollResult().setStatus("notStarted").getStatus());

        Assertions.assertEquals(LongRunningOperationStatus.IN_PROGRESS,
            new PollResult().setStatus("inProgress").getStatus());

        Assertions.assertEquals(LongRunningOperationStatus.IN_PROGRESS,
            new PollResult().setStatus("running").getStatus());

        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            new PollResult().setStatus("succeeded").getStatus());

        Assertions.assertEquals(LongRunningOperationStatus.FAILED, new PollResult().setStatus("failed").getStatus());

        Assertions.assertEquals(LongRunningOperationStatus.USER_CANCELLED,
            new PollResult().setStatus("canceled").getStatus());

        final String unknownStatusString = "idontknow";
        LongRunningOperationStatus unknownStatus = new PollResult().setStatus(unknownStatusString).getStatus();
        Assertions.assertEquals(unknownStatusString, unknownStatus.toString());
        Assertions.assertFalse(unknownStatus.isComplete());
    }
}
