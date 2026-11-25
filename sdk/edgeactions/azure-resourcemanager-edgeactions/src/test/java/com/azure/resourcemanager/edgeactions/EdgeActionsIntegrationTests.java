// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.edgeactions.models.EdgeAction;
import com.azure.resourcemanager.edgeactions.models.SkuTypeUpdate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EdgeActionsIntegrationTests extends EdgeActionsTestBase {
    @Test
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    public void createsUpdatesListsAndDeletesEdgeAction() {
        Map<String, String> initialTags = new HashMap<>();
        initialTags.put("purpose", "initial");
        initialTags.put("removeMe", "initial");
        EdgeAction action = createEdgeAction(initialTags);
        Assertions.assertEquals(edgeActionName, action.name());
        Assertions.assertEquals("global", action.location());
        Assertions.assertEquals("Standard", action.sku().name());
        Assertions.assertEquals("Standard", action.sku().tier());
        Assertions.assertEquals(initialTags, action.tags());
        assertResourceIdEquals(action.id(), manager.edgeActions().getById(action.id()).id());
        assertResourceIdEquals(action.id(),
            manager.edgeActions().getByResourceGroup(resourceGroupName, edgeActionName).id());
        Assertions.assertTrue(manager.edgeActions()
            .listByResourceGroup(resourceGroupName)
            .stream()
            .anyMatch(item -> action.id().equalsIgnoreCase(item.id())));
        Assertions.assertTrue(
            manager.edgeActions().list().stream().anyMatch(item -> action.id().equalsIgnoreCase(item.id())));

        Map<String, String> replacement = Collections.singletonMap("purpose", "updated");
        action.update().withTags(replacement).apply();
        Assertions.assertEquals(replacement, action.refresh().tags());
        action.update().withTags(Collections.emptyMap()).apply();
        Assertions.assertTrue(action.refresh().tags().isEmpty());
        Assertions.assertEquals("global", action.location());
        Assertions.assertEquals("Standard", action.sku().name());
        Assertions.assertEquals("Standard", action.sku().tier());

        manager.edgeActions().deleteById(action.id());
        assertNotFound(() -> manager.edgeActions().getById(action.id()));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    public void rejectsSkuInPatchEvenWhenUnchanged() {
        EdgeAction action = createEdgeAction();
        ManagementException exception = Assertions.assertThrows(ManagementException.class,
            () -> action.update()
                .withSku(new SkuTypeUpdate().withName(action.sku().name()).withTier(action.sku().tier()))
                .apply());
        Assertions.assertNotNull(exception.getResponse());
        Assertions.assertEquals(400, exception.getResponse().getStatusCode());
        Assertions.assertEquals("Standard", action.refresh().sku().name());
    }
}
