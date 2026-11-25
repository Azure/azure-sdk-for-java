// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilter;
import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilterProperties;
import com.azure.resourcemanager.edgeactions.models.EdgeActionExecutionFilterUpdateProperties;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;
import com.azure.resourcemanager.edgeactions.models.ProvisioningState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class EdgeActionExecutionFiltersIntegrationTests extends EdgeActionsTestBase {
    @Test
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    public void createsExecutionFilterWithoutCodeDeployment() {
        createEdgeAction();
        EdgeActionVersion version = createVersion("v1", EdgeActionVersionDeploymentType.FILE);
        String filterName = "undeployedfilter";
        EdgeActionExecutionFilter filter = manager.edgeActionExecutionFilters()
            .define(filterName)
            .withRegion("global")
            .withExistingEdgeAction(resourceGroupName, edgeActionName)
            .withProperties(new EdgeActionExecutionFilterProperties().withVersionId(version.id())
                .withExecutionFilterIdentifierHeaderName("X-Java-Undeployed")
                .withExecutionFilterIdentifierHeaderValue("no-code"))
            .create();
        Assertions.assertEquals(filterName, filter.name());
        assertResourceIdEquals(version.id(), filter.properties().versionId());
        Assertions.assertEquals("X-Java-Undeployed", filter.properties().executionFilterIdentifierHeaderName());
        Assertions.assertEquals("no-code", filter.properties().executionFilterIdentifierHeaderValue());

        EdgeActionExecutionFilter retrieved
            = manager.edgeActionExecutionFilters().get(resourceGroupName, edgeActionName, filterName);
        assertResourceIdEquals(filter.id(), retrieved.id());
        assertResourceIdEquals(version.id(), retrieved.properties().versionId());
        Assertions.assertEquals("X-Java-Undeployed", retrieved.properties().executionFilterIdentifierHeaderName());
        Assertions.assertEquals("no-code", retrieved.properties().executionFilterIdentifierHeaderValue());

        manager.edgeActionExecutionFilters().delete(resourceGroupName, edgeActionName, filterName);
        assertNotFound(() -> manager.edgeActionExecutionFilters().get(resourceGroupName, edgeActionName, filterName));
    }

    @Test
    @Timeout(value = 90, unit = TimeUnit.MINUTES)
    public void createsUpdatesListsAndDeletesExecutionFilter() throws IOException {
        createEdgeAction();
        EdgeActionVersion first = deployVersion("v1", EdgeActionVersionDeploymentType.FILE);
        EdgeActionVersion second = deployVersion("v2", EdgeActionVersionDeploymentType.FILE);
        String filterName = "testfilter";
        EdgeActionExecutionFilter filter = manager.edgeActionExecutionFilters()
            .define(filterName)
            .withRegion("global")
            .withExistingEdgeAction(resourceGroupName, edgeActionName)
            .withProperties(new EdgeActionExecutionFilterProperties().withVersionId(first.id())
                .withExecutionFilterIdentifierHeaderName("X-Java-Test")
                .withExecutionFilterIdentifierHeaderValue("initial"))
            .withTags(Collections.singletonMap("purpose", "initial"))
            .create();
        Assertions.assertEquals(filterName, filter.name());
        Assertions.assertEquals("global", filter.location());
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, filter.properties().provisioningState());
        filter.refresh();
        assertResourceIdEquals(first.id(), filter.properties().versionId());
        Assertions.assertEquals("X-Java-Test", filter.properties().executionFilterIdentifierHeaderName());
        Assertions.assertEquals("initial", filter.properties().executionFilterIdentifierHeaderValue());
        String filterId = filter.id();
        Assertions.assertTrue(manager.edgeActionExecutionFilters()
            .listByEdgeAction(resourceGroupName, edgeActionName)
            .stream()
            .anyMatch(item -> filterId.equalsIgnoreCase(item.id())));

        filter.update()
            .withProperties(new EdgeActionExecutionFilterUpdateProperties().withVersionId(second.id())
                .withExecutionFilterIdentifierHeaderName("X-Java-Updated")
                .withExecutionFilterIdentifierHeaderValue("updated"))
            .withTags(Collections.singletonMap("purpose", "updated"))
            .apply();
        filter = manager.edgeActionExecutionFilters().get(resourceGroupName, edgeActionName, filterName);
        assertResourceIdEquals(second.id(), filter.properties().versionId());
        Assertions.assertEquals("X-Java-Updated", filter.properties().executionFilterIdentifierHeaderName());
        Assertions.assertEquals("updated", filter.properties().executionFilterIdentifierHeaderValue());
        Assertions.assertEquals(Collections.singletonMap("purpose", "updated"), filter.tags());

        filter.update()
            .withProperties(new EdgeActionExecutionFilterUpdateProperties()
                .withExecutionFilterIdentifierHeaderValue("preserved-tags"))
            .apply();
        filter.refresh();
        assertResourceIdEquals(second.id(), filter.properties().versionId());
        Assertions.assertEquals("X-Java-Updated", filter.properties().executionFilterIdentifierHeaderName());
        Assertions.assertEquals("preserved-tags", filter.properties().executionFilterIdentifierHeaderValue());
        Assertions.assertEquals(Collections.singletonMap("purpose", "updated"), filter.tags());
        manager.edgeActionExecutionFilters().delete(resourceGroupName, edgeActionName, filterName);
        assertNotFound(() -> manager.edgeActionExecutionFilters().get(resourceGroupName, edgeActionName, filterName));
    }
}
