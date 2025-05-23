// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.elasticsan.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.elasticsan.fluent.models.SnapshotInner;
import com.azure.resourcemanager.elasticsan.models.SnapshotCreationData;
import com.azure.resourcemanager.elasticsan.models.SnapshotList;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class SnapshotListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SnapshotList model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"creationData\":{\"sourceId\":\"yaw\"},\"provisioningState\":\"Deleting\",\"sourceVolumeSizeGiB\":2698678982079233320,\"volumeName\":\"lyjpk\"},\"id\":\"dzyexznelixh\",\"name\":\"rzt\",\"type\":\"o\"},{\"properties\":{\"creationData\":{\"sourceId\":\"hb\"},\"provisioningState\":\"Restoring\",\"sourceVolumeSizeGiB\":8618140120589918297,\"volumeName\":\"ulppggdtpnapnyir\"},\"id\":\"uhpigvp\",\"name\":\"ylgqgitxmedjvcsl\",\"type\":\"n\"}],\"nextLink\":\"wncwzzhxgktrmg\"}")
            .toObject(SnapshotList.class);
        Assertions.assertEquals("yaw", model.value().get(0).creationData().sourceId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SnapshotList model = new SnapshotList().withValue(
            Arrays.asList(new SnapshotInner().withCreationData(new SnapshotCreationData().withSourceId("yaw")),
                new SnapshotInner().withCreationData(new SnapshotCreationData().withSourceId("hb"))));
        model = BinaryData.fromObject(model).toObject(SnapshotList.class);
        Assertions.assertEquals("yaw", model.value().get(0).creationData().sourceId());
    }
}
