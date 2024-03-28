// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.devcenter.custom;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.devcenter.models.DevBox;
import com.azure.developer.devcenter.models.DevCenterOperationDetails;
import com.azure.developer.devcenter.DevCenterClientTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public final class CreatesADevBoxTests extends DevCenterClientTestBase {
    @Test
    @Disabled
    public void testCreatesADevBoxTests() {
        // method invocation
        SyncPoller<DevCenterOperationDetails, DevBox> response
            = devBoxesClient.beginCreateDevBox("myProject", "me", new DevBox("MyDevBox", "LargeDevWorkStationPool"));

        // response assertion
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
            response.waitForCompletion().getStatus());
    }
}
