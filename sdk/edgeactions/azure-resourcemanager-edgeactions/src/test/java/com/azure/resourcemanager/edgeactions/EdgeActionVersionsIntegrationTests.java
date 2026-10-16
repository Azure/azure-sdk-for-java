// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.resourcemanager.edgeactions.models.EdgeActionIsDefaultVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionUpdateProperties;
import com.azure.resourcemanager.edgeactions.models.VersionCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EdgeActionVersionsIntegrationTests extends EdgeActionsTestBase {
    @Test
    public void deploysAndRetrievesFileCode() throws IOException {
        verifyCodeDeployment(EdgeActionVersionDeploymentType.FILE);
    }

    @Test
    public void deploysAndRetrievesZipCode() throws IOException {
        verifyCodeDeployment(EdgeActionVersionDeploymentType.ZIP);
    }

    private void verifyCodeDeployment(EdgeActionVersionDeploymentType deploymentType) throws IOException {
        createEdgeAction();
        EdgeActionVersion version = deployVersion("v1", deploymentType);
        Assertions.assertEquals("v1", version.name());
        Assertions.assertEquals("global", version.location());
        Assertions.assertEquals(deploymentType, version.properties().deploymentType());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.TRUE, version.properties().isDefaultVersion());
        VersionCode downloaded = version.getVersionCode();
        Assertions.assertNotNull(downloaded.content());
        Assertions.assertEquals(EdgeActionsTestCode.source(),
            EdgeActionsTestCode.downloadedSource(downloaded.content()));
        manager.edgeActionVersions().delete(resourceGroupName, edgeActionName, version.name());
        assertNotFound(() -> manager.edgeActionVersions().get(resourceGroupName, edgeActionName, version.name()));
    }

    @Test
    public void updatesVersionsAndSwapsDefault() throws IOException {
        createEdgeAction();
        EdgeActionVersion first = deployVersion("v1", EdgeActionVersionDeploymentType.FILE);
        EdgeActionVersion second = deployVersion("v2", EdgeActionVersionDeploymentType.FILE);
        Assertions.assertEquals(EdgeActionIsDefaultVersion.TRUE, first.properties().isDefaultVersion());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.FALSE, second.properties().isDefaultVersion());
        List<String> names = manager.edgeActionVersions()
            .listByEdgeAction(resourceGroupName, edgeActionName)
            .stream()
            .map(EdgeActionVersion::name)
            .collect(Collectors.toList());
        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.contains("v1"));
        Assertions.assertTrue(names.contains("v2"));

        second.update()
            .withProperties(
                new EdgeActionVersionUpdateProperties().withDeploymentType(EdgeActionVersionDeploymentType.FILE)
                    .withIsDefaultVersion(EdgeActionIsDefaultVersion.FALSE))
            .withTags(Collections.singletonMap("purpose", "updated"))
            .apply();
        second.refresh();
        Assertions.assertEquals(Collections.singletonMap("purpose", "updated"), second.tags());
        Assertions.assertEquals(EdgeActionVersionDeploymentType.FILE, second.properties().deploymentType());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.FALSE, second.properties().isDefaultVersion());

        manager.edgeActionVersions().swapDefault(resourceGroupName, edgeActionName, second.name());
        EdgeActionVersion firstAfter
            = manager.edgeActionVersions().get(resourceGroupName, edgeActionName, first.name());
        EdgeActionVersion secondAfter
            = manager.edgeActionVersions().get(resourceGroupName, edgeActionName, second.name());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.FALSE, firstAfter.properties().isDefaultVersion());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.TRUE, secondAfter.properties().isDefaultVersion());
        Assertions.assertEquals(1,
            manager.edgeActionVersions()
                .listByEdgeAction(resourceGroupName, edgeActionName)
                .stream()
                .filter(version -> EdgeActionIsDefaultVersion.TRUE.equals(version.properties().isDefaultVersion()))
                .count());
    }
}
