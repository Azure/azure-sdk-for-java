// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.DynamicsCrmSource;

public final class DynamicsCrmSourceTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DynamicsCrmSource model = BinaryData.fromString(
            "{\"type\":\"DynamicsCrmSource\",\"query\":\"databomjby\",\"additionalColumns\":\"dataprkbzraljwfnc\",\"sourceRetryCount\":\"dataaylcpgzmx\",\"sourceRetryWait\":\"datappqajdm\",\"maxConcurrentConnections\":\"datanntqqgu\",\"disableMetricsCollection\":\"datanwrzimin\",\"\":{\"dlclxxquyff\":\"datazfwfuxdtpjcsqk\",\"b\":\"dataqombdsgqxacidu\"}}")
            .toObject(DynamicsCrmSource.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DynamicsCrmSource model = new DynamicsCrmSource().withSourceRetryCount("dataaylcpgzmx")
            .withSourceRetryWait("datappqajdm")
            .withMaxConcurrentConnections("datanntqqgu")
            .withDisableMetricsCollection("datanwrzimin")
            .withQuery("databomjby")
            .withAdditionalColumns("dataprkbzraljwfnc");
        model = BinaryData.fromObject(model).toObject(DynamicsCrmSource.class);
    }
}
