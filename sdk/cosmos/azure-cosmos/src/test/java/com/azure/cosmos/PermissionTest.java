// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.Permission;
import com.azure.cosmos.models.PermissionMode;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PermissionTest {

    @Test(groups = {"unit"})
    public void deserialize() {
        String json = "{" +
                "    'id': 'a98eb026-b66b-4cec-8fb9-9b0e10ddab76'," +
                "    'permissionMode': 'read'," +
                "    'resource': 'dbs/AQAAAA==/colls/AQAAAJ0fgTc='," +
                "    'resourcePartitionKey': ['/id']" +
                "}";
        Permission p = new Permission(json);
        assertThat(p.getResourcePartitionKey()).isEqualToComparingFieldByField(new PartitionKey("/id"));
        assertThat(p.getPermissionMode()).isEqualTo(PermissionMode.READ);
    }
}
