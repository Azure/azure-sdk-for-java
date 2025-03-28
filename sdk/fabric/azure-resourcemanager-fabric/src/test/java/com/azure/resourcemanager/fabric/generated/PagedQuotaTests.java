// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.fabric.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.fabric.implementation.models.PagedQuota;
import org.junit.jupiter.api.Assertions;

public final class PagedQuotaTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        PagedQuota model = BinaryData.fromString(
            "{\"value\":[{\"name\":{\"value\":\"ta\",\"localizedValue\":\"pwgcuertu\"},\"unit\":\"kdosvqw\",\"currentValue\":4117897998779209947,\"limit\":3494827514980222479},{\"name\":{\"value\":\"jfddgmbmbe\",\"localizedValue\":\"pbhtqqrolfpfpsa\"},\"unit\":\"gbquxigj\",\"currentValue\":3940628181525155930,\"limit\":3662129632751771998}],\"nextLink\":\"o\"}")
            .toObject(PagedQuota.class);
        Assertions.assertEquals("kdosvqw", model.value().get(0).unit());
        Assertions.assertEquals(4117897998779209947L, model.value().get(0).currentValue());
        Assertions.assertEquals(3494827514980222479L, model.value().get(0).limit());
        Assertions.assertEquals("o", model.nextLink());
    }
}
