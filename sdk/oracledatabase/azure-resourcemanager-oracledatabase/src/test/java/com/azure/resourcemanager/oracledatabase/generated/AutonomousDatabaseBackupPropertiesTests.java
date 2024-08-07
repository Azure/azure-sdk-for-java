// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.oracledatabase.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabaseBackupProperties;
import org.junit.jupiter.api.Assertions;

public final class AutonomousDatabaseBackupPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AutonomousDatabaseBackupProperties model = BinaryData.fromString(
            "{\"autonomousDatabaseOcid\":\"rmnjijpx\",\"databaseSizeInTbs\":5.180892672849591,\"dbVersion\":\"dfnbyxbaaabjyv\",\"displayName\":\"ffimrzrtuzqogsex\",\"ocid\":\"vfdnwnwmewzsyyce\",\"isAutomatic\":true,\"isRestorable\":false,\"lifecycleDetails\":\"judpfrxt\",\"lifecycleState\":\"Deleting\",\"retentionPeriodInDays\":729293630,\"sizeInTbs\":24.597232718966964,\"timeAvailableTil\":\"2021-02-25T22:09:17Z\",\"timeStarted\":\"qbrqubpaxhexiili\",\"timeEnded\":\"dtiirqt\",\"backupType\":\"LongTerm\",\"provisioningState\":\"Provisioning\"}")
            .toObject(AutonomousDatabaseBackupProperties.class);
        Assertions.assertEquals("ffimrzrtuzqogsex", model.displayName());
        Assertions.assertEquals(729293630, model.retentionPeriodInDays());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AutonomousDatabaseBackupProperties model
            = new AutonomousDatabaseBackupProperties().withDisplayName("ffimrzrtuzqogsex")
                .withRetentionPeriodInDays(729293630);
        model = BinaryData.fromObject(model).toObject(AutonomousDatabaseBackupProperties.class);
        Assertions.assertEquals("ffimrzrtuzqogsex", model.displayName());
        Assertions.assertEquals(729293630, model.retentionPeriodInDays());
    }
}
