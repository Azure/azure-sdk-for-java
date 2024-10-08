// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.machinelearning.models.JobTier;
import com.azure.resourcemanager.machinelearning.models.QueueSettings;
import org.junit.jupiter.api.Assertions;

public final class QueueSettingsTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        QueueSettings model = BinaryData.fromString("{\"jobTier\":\"Spot\"}").toObject(QueueSettings.class);
        Assertions.assertEquals(JobTier.SPOT, model.jobTier());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        QueueSettings model = new QueueSettings().withJobTier(JobTier.SPOT);
        model = BinaryData.fromObject(model).toObject(QueueSettings.class);
        Assertions.assertEquals(JobTier.SPOT, model.jobTier());
    }
}
