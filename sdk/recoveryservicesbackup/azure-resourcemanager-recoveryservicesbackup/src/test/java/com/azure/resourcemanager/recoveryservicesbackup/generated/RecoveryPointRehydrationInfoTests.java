// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryPointRehydrationInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.RehydrationPriority;
import org.junit.jupiter.api.Assertions;

public final class RecoveryPointRehydrationInfoTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RecoveryPointRehydrationInfo model
            = BinaryData.fromString("{\"rehydrationRetentionDuration\":\"cdp\",\"rehydrationPriority\":\"Standard\"}")
                .toObject(RecoveryPointRehydrationInfo.class);
        Assertions.assertEquals("cdp", model.rehydrationRetentionDuration());
        Assertions.assertEquals(RehydrationPriority.STANDARD, model.rehydrationPriority());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RecoveryPointRehydrationInfo model = new RecoveryPointRehydrationInfo().withRehydrationRetentionDuration("cdp")
            .withRehydrationPriority(RehydrationPriority.STANDARD);
        model = BinaryData.fromObject(model).toObject(RecoveryPointRehydrationInfo.class);
        Assertions.assertEquals("cdp", model.rehydrationRetentionDuration());
        Assertions.assertEquals(RehydrationPriority.STANDARD, model.rehydrationPriority());
    }
}
