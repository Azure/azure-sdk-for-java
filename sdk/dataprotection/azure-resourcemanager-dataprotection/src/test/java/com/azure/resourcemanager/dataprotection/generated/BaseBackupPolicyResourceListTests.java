// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dataprotection.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.dataprotection.fluent.models.BaseBackupPolicyResourceInner;
import com.azure.resourcemanager.dataprotection.models.BaseBackupPolicy;
import com.azure.resourcemanager.dataprotection.models.BaseBackupPolicyResourceList;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class BaseBackupPolicyResourceListTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        BaseBackupPolicyResourceList model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"objectType\":\"BaseBackupPolicy\",\"datasourceTypes\":[\"jsllrmv\",\"d\",\"watkpnpulexxb\"]},\"id\":\"wtruwiqzbqjvsovm\",\"name\":\"okacspk\",\"type\":\"lhzdobp\"},{\"properties\":{\"objectType\":\"BaseBackupPolicy\",\"datasourceTypes\":[\"flbvvnchrkcciwwz\",\"uqkhrsajiwku\"]},\"id\":\"oskg\",\"name\":\"sauuimj\",\"type\":\"vxieduugidyj\"}],\"nextLink\":\"f\"}")
            .toObject(BaseBackupPolicyResourceList.class);
        Assertions.assertEquals("f", model.nextLink());
        Assertions.assertEquals("jsllrmv", model.value().get(0).properties().datasourceTypes().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        BaseBackupPolicyResourceList model = new BaseBackupPolicyResourceList().withNextLink("f")
            .withValue(Arrays.asList(
                new BaseBackupPolicyResourceInner().withProperties(
                    new BaseBackupPolicy().withDatasourceTypes(Arrays.asList("jsllrmv", "d", "watkpnpulexxb"))),
                new BaseBackupPolicyResourceInner().withProperties(
                    new BaseBackupPolicy().withDatasourceTypes(Arrays.asList("flbvvnchrkcciwwz", "uqkhrsajiwku")))));
        model = BinaryData.fromObject(model).toObject(BaseBackupPolicyResourceList.class);
        Assertions.assertEquals("f", model.nextLink());
        Assertions.assertEquals("jsllrmv", model.value().get(0).properties().datasourceTypes().get(0));
    }
}
