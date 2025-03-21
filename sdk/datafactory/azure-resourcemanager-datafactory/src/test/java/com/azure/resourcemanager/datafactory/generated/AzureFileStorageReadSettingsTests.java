// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.AzureFileStorageReadSettings;

public final class AzureFileStorageReadSettingsTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureFileStorageReadSettings model = BinaryData.fromString(
            "{\"type\":\"AzureFileStorageReadSettings\",\"recursive\":\"datadqgy\",\"wildcardFolderPath\":\"dataulzguvckpdp\",\"wildcardFileName\":\"datanrjqskikqd\",\"prefix\":\"dataybqtlvofjjsetiz\",\"fileListPath\":\"datanadn\",\"enablePartitionDiscovery\":\"datasbpxlserqgxnh\",\"partitionRootPath\":\"dataccd\",\"deleteFilesAfterCompletion\":\"dataxybn\",\"modifiedDatetimeStart\":\"datahmpmeglolpot\",\"modifiedDatetimeEnd\":\"datamb\",\"maxConcurrentConnections\":\"dataqjrytymfnojjh\",\"disableMetricsCollection\":\"datanthjqgovviv\",\"\":{\"rafet\":\"datay\",\"vpiilgy\":\"datawyt\",\"vpbuk\":\"dataluolgspyqsapnh\",\"oujtcp\":\"dataurqviyfksegwezgf\"}}")
            .toObject(AzureFileStorageReadSettings.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureFileStorageReadSettings model
            = new AzureFileStorageReadSettings().withMaxConcurrentConnections("dataqjrytymfnojjh")
                .withDisableMetricsCollection("datanthjqgovviv")
                .withRecursive("datadqgy")
                .withWildcardFolderPath("dataulzguvckpdp")
                .withWildcardFileName("datanrjqskikqd")
                .withPrefix("dataybqtlvofjjsetiz")
                .withFileListPath("datanadn")
                .withEnablePartitionDiscovery("datasbpxlserqgxnh")
                .withPartitionRootPath("dataccd")
                .withDeleteFilesAfterCompletion("dataxybn")
                .withModifiedDatetimeStart("datahmpmeglolpot")
                .withModifiedDatetimeEnd("datamb");
        model = BinaryData.fromObject(model).toObject(AzureFileStorageReadSettings.class);
    }
}
