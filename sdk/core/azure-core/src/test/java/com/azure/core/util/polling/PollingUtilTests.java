// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.implementation.PollResult;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class PollingUtilTests {

    private static final TypeReference<PollResult> POLL_RESULT_TYPE_REFERENCE = TypeReference.createInstance(PollResult.class);

    @Test
    public void testUnknownStatus() {
        PollingContext<BinaryData> context = new PollingContext<>();
        PollResponse<BinaryData> intermediatePollResponse = PollingUtil.pollingLoop(context, null, null, context1 -> {
            // see SyncOperationResourcePollingStrategy.poll
            PollResult pollResult = PollingUtils.deserializeResponseSync(
                BinaryData.fromString("{\"status\": \"notRunning\"}"), JsonSerializerProviders.createInstance(true), POLL_RESULT_TYPE_REFERENCE);
            return new PollResponse<>(pollResult.getStatus(), null, null);
        }, Duration.ofMillis(1));

        // PollingUtil.pollingLoop should not stop in such polling condition
        Assertions.assertEquals("notRunning", intermediatePollResponse.getStatus().toString());
    }
}
