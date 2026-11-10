// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EdgeActionsTestCodeTests {
    @Test
    public void createsDeterministicDeploymentContent() throws IOException {
        Assertions.assertEquals(EdgeActionsTestCode.source(),
            new String(
                Base64.getDecoder().decode(EdgeActionsTestCode.deploymentContent(EdgeActionVersionDeploymentType.FILE)),
                StandardCharsets.UTF_8));
        String archive = EdgeActionsTestCode.deploymentContent(EdgeActionVersionDeploymentType.ZIP);
        Assertions.assertEquals(archive, EdgeActionsTestCode.deploymentContent(EdgeActionVersionDeploymentType.ZIP));
        Assertions.assertEquals(EdgeActionsTestCode.source(), EdgeActionsTestCode.downloadedSource(archive));
    }

    @Test
    public void rejectsMissingCodeInDownloadedArchive() {
        Assertions.assertThrows(IOException.class,
            () -> EdgeActionsTestCode.downloadedSource(Base64.getEncoder().encodeToString(new byte[0])));
    }
}
