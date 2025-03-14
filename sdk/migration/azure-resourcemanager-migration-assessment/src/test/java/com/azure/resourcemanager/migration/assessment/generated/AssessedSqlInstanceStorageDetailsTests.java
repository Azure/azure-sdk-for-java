// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.migration.assessment.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.migration.assessment.models.AssessedSqlInstanceStorageDetails;
import org.junit.jupiter.api.Assertions;

public final class AssessedSqlInstanceStorageDetailsTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AssessedSqlInstanceStorageDetails model = BinaryData.fromString(
            "{\"storageType\":\"rkyui\",\"diskSizeInMB\":46.088665,\"megabytesPerSecondOfRead\":27.76271,\"megabytesPerSecondOfWrite\":36.77259,\"numberOfReadOperationsPerSecond\":60.807236,\"numberOfWriteOperationsPerSecond\":35.29606}")
            .toObject(AssessedSqlInstanceStorageDetails.class);
        Assertions.assertEquals("rkyui", model.storageType());
        Assertions.assertEquals(46.088665F, model.diskSizeInMB());
        Assertions.assertEquals(27.76271F, model.megabytesPerSecondOfRead());
        Assertions.assertEquals(36.77259F, model.megabytesPerSecondOfWrite());
        Assertions.assertEquals(60.807236F, model.numberOfReadOperationsPerSecond());
        Assertions.assertEquals(35.29606F, model.numberOfWriteOperationsPerSecond());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AssessedSqlInstanceStorageDetails model = new AssessedSqlInstanceStorageDetails().withStorageType("rkyui")
            .withDiskSizeInMB(46.088665F)
            .withMegabytesPerSecondOfRead(27.76271F)
            .withMegabytesPerSecondOfWrite(36.77259F)
            .withNumberOfReadOperationsPerSecond(60.807236F)
            .withNumberOfWriteOperationsPerSecond(35.29606F);
        model = BinaryData.fromObject(model).toObject(AssessedSqlInstanceStorageDetails.class);
        Assertions.assertEquals("rkyui", model.storageType());
        Assertions.assertEquals(46.088665F, model.diskSizeInMB());
        Assertions.assertEquals(27.76271F, model.megabytesPerSecondOfRead());
        Assertions.assertEquals(36.77259F, model.megabytesPerSecondOfWrite());
        Assertions.assertEquals(60.807236F, model.numberOfReadOperationsPerSecond());
        Assertions.assertEquals(35.29606F, model.numberOfWriteOperationsPerSecond());
    }
}
