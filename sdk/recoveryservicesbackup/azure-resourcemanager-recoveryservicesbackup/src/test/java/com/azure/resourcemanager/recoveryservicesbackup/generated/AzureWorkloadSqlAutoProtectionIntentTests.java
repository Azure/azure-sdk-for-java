// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicesbackup.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureWorkloadSqlAutoProtectionIntent;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupManagementType;
import com.azure.resourcemanager.recoveryservicesbackup.models.ProtectionStatus;
import com.azure.resourcemanager.recoveryservicesbackup.models.WorkloadItemType;
import org.junit.jupiter.api.Assertions;

public final class AzureWorkloadSqlAutoProtectionIntentTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureWorkloadSqlAutoProtectionIntent model = BinaryData.fromString(
            "{\"protectionIntentItemType\":\"AzureWorkloadSQLAutoProtectionIntent\",\"workloadItemType\":\"SAPHanaDBInstance\",\"backupManagementType\":\"AzureSql\",\"sourceResourceId\":\"ymzvla\",\"itemId\":\"pbh\",\"policyId\":\"vqs\",\"protectionState\":\"Protected\"}")
            .toObject(AzureWorkloadSqlAutoProtectionIntent.class);
        Assertions.assertEquals(BackupManagementType.AZURE_SQL, model.backupManagementType());
        Assertions.assertEquals("ymzvla", model.sourceResourceId());
        Assertions.assertEquals("pbh", model.itemId());
        Assertions.assertEquals("vqs", model.policyId());
        Assertions.assertEquals(ProtectionStatus.PROTECTED, model.protectionState());
        Assertions.assertEquals(WorkloadItemType.SAPHANA_DBINSTANCE, model.workloadItemType());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureWorkloadSqlAutoProtectionIntent model = new AzureWorkloadSqlAutoProtectionIntent()
            .withBackupManagementType(BackupManagementType.AZURE_SQL).withSourceResourceId("ymzvla").withItemId("pbh")
            .withPolicyId("vqs").withProtectionState(ProtectionStatus.PROTECTED)
            .withWorkloadItemType(WorkloadItemType.SAPHANA_DBINSTANCE);
        model = BinaryData.fromObject(model).toObject(AzureWorkloadSqlAutoProtectionIntent.class);
        Assertions.assertEquals(BackupManagementType.AZURE_SQL, model.backupManagementType());
        Assertions.assertEquals("ymzvla", model.sourceResourceId());
        Assertions.assertEquals("pbh", model.itemId());
        Assertions.assertEquals("vqs", model.policyId());
        Assertions.assertEquals(ProtectionStatus.PROTECTED, model.protectionState());
        Assertions.assertEquals(WorkloadItemType.SAPHANA_DBINSTANCE, model.workloadItemType());
    }
}
