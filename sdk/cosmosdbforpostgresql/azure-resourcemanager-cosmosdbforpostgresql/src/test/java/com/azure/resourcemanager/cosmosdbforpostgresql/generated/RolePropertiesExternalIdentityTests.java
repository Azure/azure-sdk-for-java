// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmosdbforpostgresql.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.cosmosdbforpostgresql.fluent.models.RolePropertiesExternalIdentity;
import com.azure.resourcemanager.cosmosdbforpostgresql.models.PrincipalType;
import org.junit.jupiter.api.Assertions;

public final class RolePropertiesExternalIdentityTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RolePropertiesExternalIdentity model = BinaryData
            .fromString("{\"objectId\":\"vvnchrkcc\",\"principalType\":\"user\",\"tenantId\":\"zjuqkhrsaj\"}")
            .toObject(RolePropertiesExternalIdentity.class);
        Assertions.assertEquals("vvnchrkcc", model.objectId());
        Assertions.assertEquals(PrincipalType.USER, model.principalType());
        Assertions.assertEquals("zjuqkhrsaj", model.tenantId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RolePropertiesExternalIdentity model = new RolePropertiesExternalIdentity().withObjectId("vvnchrkcc")
            .withPrincipalType(PrincipalType.USER)
            .withTenantId("zjuqkhrsaj");
        model = BinaryData.fromObject(model).toObject(RolePropertiesExternalIdentity.class);
        Assertions.assertEquals("vvnchrkcc", model.objectId());
        Assertions.assertEquals(PrincipalType.USER, model.principalType());
        Assertions.assertEquals("zjuqkhrsaj", model.tenantId());
    }
}
