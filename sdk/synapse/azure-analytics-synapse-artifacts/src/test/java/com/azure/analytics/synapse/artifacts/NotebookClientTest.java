// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.artifacts;

import com.azure.analytics.synapse.artifacts.models.NotebookResource;
import org.junit.jupiter.api.Test;

public class NotebookClientTest extends ArtifactsClientTestBase {

    private NotebookClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new ArtifactsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildNotebookClient());
    }

    /**
     * Tests that notebook can be listed in the workspace.
     */
    @Test
    public void getNoteBook() {
        for (NotebookResource expectedNotebook : client.getNotebooksByWorkspace()) {
            NotebookResource actualNotebook = client.getNotebook(expectedNotebook.getName());
            validateNotebook(expectedNotebook, actualNotebook);
        }
    }
}
