// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devcenter.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.devcenter.fluent.models.ProjectInner;
import com.azure.resourcemanager.devcenter.models.CatalogItemType;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.devcenter.models.ProjectCatalogSettings;
import com.azure.resourcemanager.devcenter.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class ProjectInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ProjectInner model = BinaryData.fromString(
            "{\"properties\":{\"provisioningState\":\"Failed\",\"devCenterUri\":\"jdous\",\"devCenterId\":\"qvkoc\",\"description\":\"jdkwtnhxbnjb\",\"maxDevBoxesPerUser\":703766696,\"displayName\":\"rglssainqpj\",\"catalogSettings\":{\"catalogItemSyncTypes\":[\"EnvironmentDefinition\",\"EnvironmentDefinition\",\"EnvironmentDefinition\"]}},\"identity\":{\"principalId\":\"57787e3b-2b33-4331-99dd-cbe45020da13\",\"tenantId\":\"ae03694d-71a9-4886-b7be-fda36b629931\",\"type\":\"UserAssigned\",\"userAssignedIdentities\":{\"gxsabkyq\":{\"principalId\":\"a76d32de-83b1-465c-a635-74d7981e548c\",\"clientId\":\"24253647-10e4-4aad-ab26-6798901fef21\"},\"jitcjczdzevn\":{\"principalId\":\"a2cfa346-6a0c-4106-b511-188fdbb329ab\",\"clientId\":\"82a295d3-59fc-4158-8090-a8893e620357\"},\"rwpdappdsbdkvwrw\":{\"principalId\":\"3a4ea746-661d-4e99-8aaa-8b53ba699197\",\"clientId\":\"feb69166-b5bd-41b5-a58b-97ce842407ce\"},\"usnhutje\":{\"principalId\":\"a0dd647e-fae9-465b-a310-d3655ef6cc99\",\"clientId\":\"4041d98e-e490-4974-b97a-2326f29c2cef\"}}},\"location\":\"mrldhu\",\"tags\":{\"ablgphuticndvk\":\"zdatqxhocdg\"},\"id\":\"ozwyiftyhxhuro\",\"name\":\"ftyxolniw\",\"type\":\"wcukjfkgiawxk\"}")
            .toObject(ProjectInner.class);
        Assertions.assertEquals("mrldhu", model.location());
        Assertions.assertEquals("zdatqxhocdg", model.tags().get("ablgphuticndvk"));
        Assertions.assertEquals(ManagedServiceIdentityType.USER_ASSIGNED, model.identity().type());
        Assertions.assertEquals("qvkoc", model.devCenterId());
        Assertions.assertEquals("jdkwtnhxbnjb", model.description());
        Assertions.assertEquals(703766696, model.maxDevBoxesPerUser());
        Assertions.assertEquals("rglssainqpj", model.displayName());
        Assertions.assertEquals(CatalogItemType.ENVIRONMENT_DEFINITION,
            model.catalogSettings().catalogItemSyncTypes().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ProjectInner model
            = new ProjectInner().withLocation("mrldhu")
                .withTags(mapOf("ablgphuticndvk", "zdatqxhocdg"))
                .withIdentity(
                    new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentities(mapOf("gxsabkyq", new UserAssignedIdentity(), "jitcjczdzevn",
                            new UserAssignedIdentity(), "rwpdappdsbdkvwrw", new UserAssignedIdentity(), "usnhutje",
                            new UserAssignedIdentity())))
                .withDevCenterId("qvkoc")
                .withDescription("jdkwtnhxbnjb")
                .withMaxDevBoxesPerUser(703766696)
                .withDisplayName("rglssainqpj")
                .withCatalogSettings(new ProjectCatalogSettings()
                    .withCatalogItemSyncTypes(Arrays.asList(CatalogItemType.ENVIRONMENT_DEFINITION,
                        CatalogItemType.ENVIRONMENT_DEFINITION, CatalogItemType.ENVIRONMENT_DEFINITION)));
        model = BinaryData.fromObject(model).toObject(ProjectInner.class);
        Assertions.assertEquals("mrldhu", model.location());
        Assertions.assertEquals("zdatqxhocdg", model.tags().get("ablgphuticndvk"));
        Assertions.assertEquals(ManagedServiceIdentityType.USER_ASSIGNED, model.identity().type());
        Assertions.assertEquals("qvkoc", model.devCenterId());
        Assertions.assertEquals("jdkwtnhxbnjb", model.description());
        Assertions.assertEquals(703766696, model.maxDevBoxesPerUser());
        Assertions.assertEquals("rglssainqpj", model.displayName());
        Assertions.assertEquals(CatalogItemType.ENVIRONMENT_DEFINITION,
            model.catalogSettings().catalogItemSyncTypes().get(0));
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
