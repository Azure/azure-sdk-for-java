package com.azure.analytics.synapse.development;

import com.azure.analytics.synapse.development.implementation.models.NotebookResource;
import org.junit.jupiter.api.Test;

public class NotebookClientTest extends DevelopmentClientBase {

    private NotebookClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new DevelopmentClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildNotebookClient());
    }

    /**
     * Tests that notebook can be listed in the workspace.
     */
    @Test
    public void getNoteBook() {
        for (NotebookResource expectedNotebook : client.getNotebooksByWorkspace())
        {
            NotebookResource actualNotebook = client.getNotebook(expectedNotebook.getName());
            validateNotebook(expectedNotebook, actualNotebook);
        }
    }
}
