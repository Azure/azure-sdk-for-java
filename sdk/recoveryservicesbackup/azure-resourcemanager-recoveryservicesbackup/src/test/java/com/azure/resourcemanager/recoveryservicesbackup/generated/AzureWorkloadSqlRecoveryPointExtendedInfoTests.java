// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureWorkloadSqlRecoveryPointExtendedInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.SqlDataDirectory;
import com.azure.resourcemanager.recoveryservicesbackup.models.SqlDataDirectoryType;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class AzureWorkloadSqlRecoveryPointExtendedInfoTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureWorkloadSqlRecoveryPointExtendedInfo model = BinaryData.fromString(
            "{\"dataDirectoryTimeInUTC\":\"2021-11-01T10:21:35Z\",\"dataDirectoryPaths\":[{\"type\":\"Data\",\"path\":\"vigorqjbttzhragl\",\"logicalName\":\"fhonqjujeickpzvc\"},{\"type\":\"Log\",\"path\":\"xelnwc\",\"logicalName\":\"yjede\"},{\"type\":\"Log\",\"path\":\"f\",\"logicalName\":\"qscazuawxtz\"}]}")
            .toObject(AzureWorkloadSqlRecoveryPointExtendedInfo.class);
        Assertions.assertEquals(OffsetDateTime.parse("2021-11-01T10:21:35Z"), model.dataDirectoryTimeInUtc());
        Assertions.assertEquals(SqlDataDirectoryType.DATA, model.dataDirectoryPaths().get(0).type());
        Assertions.assertEquals("vigorqjbttzhragl", model.dataDirectoryPaths().get(0).path());
        Assertions.assertEquals("fhonqjujeickpzvc", model.dataDirectoryPaths().get(0).logicalName());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureWorkloadSqlRecoveryPointExtendedInfo model = new AzureWorkloadSqlRecoveryPointExtendedInfo()
            .withDataDirectoryTimeInUtc(OffsetDateTime.parse("2021-11-01T10:21:35Z"))
            .withDataDirectoryPaths(Arrays.asList(
                new SqlDataDirectory().withType(SqlDataDirectoryType.DATA)
                    .withPath("vigorqjbttzhragl")
                    .withLogicalName("fhonqjujeickpzvc"),
                new SqlDataDirectory().withType(SqlDataDirectoryType.LOG).withPath("xelnwc").withLogicalName("yjede"),
                new SqlDataDirectory().withType(SqlDataDirectoryType.LOG)
                    .withPath("f")
                    .withLogicalName("qscazuawxtz")));
        model = BinaryData.fromObject(model).toObject(AzureWorkloadSqlRecoveryPointExtendedInfo.class);
        Assertions.assertEquals(OffsetDateTime.parse("2021-11-01T10:21:35Z"), model.dataDirectoryTimeInUtc());
        Assertions.assertEquals(SqlDataDirectoryType.DATA, model.dataDirectoryPaths().get(0).type());
        Assertions.assertEquals("vigorqjbttzhragl", model.dataDirectoryPaths().get(0).path());
        Assertions.assertEquals("fhonqjujeickpzvc", model.dataDirectoryPaths().get(0).logicalName());
    }
}
