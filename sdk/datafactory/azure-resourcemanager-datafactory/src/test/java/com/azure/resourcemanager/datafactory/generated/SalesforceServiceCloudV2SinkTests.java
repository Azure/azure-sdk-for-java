// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.SalesforceServiceCloudV2Sink;
import com.azure.resourcemanager.datafactory.models.SalesforceV2SinkWriteBehavior;
import org.junit.jupiter.api.Assertions;

public final class SalesforceServiceCloudV2SinkTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SalesforceServiceCloudV2Sink model = BinaryData.fromString(
            "{\"type\":\"SalesforceServiceCloudV2Sink\",\"writeBehavior\":\"Upsert\",\"externalIdFieldName\":\"datao\",\"ignoreNullValues\":\"datavgrloshkqthuijvi\",\"writeBatchSize\":\"datawswpwbgoetu\",\"writeBatchTimeout\":\"datasfsfuzqpigirnmd\",\"sinkRetryCount\":\"datamagmwyfxeu\",\"sinkRetryWait\":\"datavtkllbfnn\",\"maxConcurrentConnections\":\"datar\",\"disableMetricsCollection\":\"dataqqjcyhvyr\",\"\":{\"wldkjayiexpcxylq\":\"datauvuj\"}}")
            .toObject(SalesforceServiceCloudV2Sink.class);
        Assertions.assertEquals(SalesforceV2SinkWriteBehavior.UPSERT, model.writeBehavior());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SalesforceServiceCloudV2Sink model = new SalesforceServiceCloudV2Sink().withWriteBatchSize("datawswpwbgoetu")
            .withWriteBatchTimeout("datasfsfuzqpigirnmd")
            .withSinkRetryCount("datamagmwyfxeu")
            .withSinkRetryWait("datavtkllbfnn")
            .withMaxConcurrentConnections("datar")
            .withDisableMetricsCollection("dataqqjcyhvyr")
            .withWriteBehavior(SalesforceV2SinkWriteBehavior.UPSERT)
            .withExternalIdFieldName("datao")
            .withIgnoreNullValues("datavgrloshkqthuijvi");
        model = BinaryData.fromObject(model).toObject(SalesforceServiceCloudV2Sink.class);
        Assertions.assertEquals(SalesforceV2SinkWriteBehavior.UPSERT, model.writeBehavior());
    }
}
