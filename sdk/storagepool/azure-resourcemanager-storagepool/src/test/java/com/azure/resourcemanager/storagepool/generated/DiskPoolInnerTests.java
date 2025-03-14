// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.storagepool.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.storagepool.fluent.models.DiskPoolInner;
import com.azure.resourcemanager.storagepool.models.Disk;
import com.azure.resourcemanager.storagepool.models.OperationalStatus;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class DiskPoolInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DiskPoolInner model = BinaryData.fromString(
            "{\"sku\":{\"name\":\"rfidfvzwdz\",\"tier\":\"tymw\"},\"properties\":{\"provisioningState\":\"Succeeded\",\"availabilityZones\":[\"kfthwxmntei\",\"aop\",\"km\"],\"status\":\"Stopped\",\"disks\":[{\"id\":\"mxdcufufsrp\"},{\"id\":\"mzidnsezcxtb\"}],\"subnetId\":\"sgfyccsnew\",\"additionalCapabilities\":[\"z\",\"eiachboosflnr\"]},\"managedBy\":\"fqpte\",\"managedByExtended\":[\"zvypyqrimzinp\",\"swjdkirso\",\"dqxhcrmnohjtckwh\",\"soifiyipjxsqw\"],\"systemData\":{\"createdBy\":\"jbznorc\",\"createdByType\":\"User\",\"createdAt\":\"2021-01-21T21:41:42Z\",\"lastModifiedBy\":\"yxqabnmocpcyshur\",\"lastModifiedByType\":\"User\",\"lastModifiedAt\":\"2021-02-03T18:35:41Z\"},\"location\":\"jjgpb\",\"tags\":{\"mkljavb\":\"c\"},\"id\":\"idtqajzyu\",\"name\":\"pku\",\"type\":\"jkrlkhbzhfepg\"}")
            .toObject(DiskPoolInner.class);
        Assertions.assertEquals("jjgpb", model.location());
        Assertions.assertEquals("c", model.tags().get("mkljavb"));
        Assertions.assertEquals("rfidfvzwdz", model.nameSkuName());
        Assertions.assertEquals("tymw", model.tier());
        Assertions.assertEquals("kfthwxmntei", model.availabilityZones().get(0));
        Assertions.assertEquals(OperationalStatus.STOPPED, model.status());
        Assertions.assertEquals("mxdcufufsrp", model.disks().get(0).id());
        Assertions.assertEquals("sgfyccsnew", model.subnetId());
        Assertions.assertEquals("z", model.additionalCapabilities().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DiskPoolInner model = new DiskPoolInner().withLocation("jjgpb")
            .withTags(mapOf("mkljavb", "c"))
            .withNameSkuName("rfidfvzwdz")
            .withTier("tymw")
            .withAvailabilityZones(Arrays.asList("kfthwxmntei", "aop", "km"))
            .withStatus(OperationalStatus.STOPPED)
            .withDisks(Arrays.asList(new Disk().withId("mxdcufufsrp"), new Disk().withId("mzidnsezcxtb")))
            .withSubnetId("sgfyccsnew")
            .withAdditionalCapabilities(Arrays.asList("z", "eiachboosflnr"));
        model = BinaryData.fromObject(model).toObject(DiskPoolInner.class);
        Assertions.assertEquals("jjgpb", model.location());
        Assertions.assertEquals("c", model.tags().get("mkljavb"));
        Assertions.assertEquals("rfidfvzwdz", model.nameSkuName());
        Assertions.assertEquals("tymw", model.tier());
        Assertions.assertEquals("kfthwxmntei", model.availabilityZones().get(0));
        Assertions.assertEquals(OperationalStatus.STOPPED, model.status());
        Assertions.assertEquals("mxdcufufsrp", model.disks().get(0).id());
        Assertions.assertEquals("sgfyccsnew", model.subnetId());
        Assertions.assertEquals("z", model.additionalCapabilities().get(0));
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
