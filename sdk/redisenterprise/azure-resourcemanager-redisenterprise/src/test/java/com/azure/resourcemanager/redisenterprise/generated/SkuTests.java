// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.redisenterprise.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.redisenterprise.models.Sku;
import com.azure.resourcemanager.redisenterprise.models.SkuName;
import org.junit.jupiter.api.Assertions;

public final class SkuTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        Sku model
            = BinaryData.fromString("{\"name\":\"EnterpriseFlash_F300\",\"capacity\":186394728}").toObject(Sku.class);
        Assertions.assertEquals(SkuName.ENTERPRISE_FLASH_F300, model.name());
        Assertions.assertEquals(186394728, model.capacity());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        Sku model = new Sku().withName(SkuName.ENTERPRISE_FLASH_F300).withCapacity(186394728);
        model = BinaryData.fromObject(model).toObject(Sku.class);
        Assertions.assertEquals(SkuName.ENTERPRISE_FLASH_F300, model.name());
        Assertions.assertEquals(186394728, model.capacity());
    }
}
