// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.DrillSource;

public final class DrillSourceTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DrillSource model = BinaryData.fromString(
            "{\"type\":\"DrillSource\",\"query\":\"datafn\",\"queryTimeout\":\"dataeyavldovpwrq\",\"additionalColumns\":\"datazokplzliizb\",\"sourceRetryCount\":\"datajumulhfq\",\"sourceRetryWait\":\"datanchah\",\"maxConcurrentConnections\":\"datanrptrqcap\",\"disableMetricsCollection\":\"datafvowzbk\",\"\":{\"qzzkplqmca\":\"datapzdpujywjmo\",\"jgfpqwwugfwpvj\":\"dataseiauveeng\"}}")
            .toObject(DrillSource.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DrillSource model = new DrillSource().withSourceRetryCount("datajumulhfq")
            .withSourceRetryWait("datanchah")
            .withMaxConcurrentConnections("datanrptrqcap")
            .withDisableMetricsCollection("datafvowzbk")
            .withQueryTimeout("dataeyavldovpwrq")
            .withAdditionalColumns("datazokplzliizb")
            .withQuery("datafn");
        model = BinaryData.fromObject(model).toObject(DrillSource.class);
    }
}
