// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DatasetsClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private DatasetsClient datasetsClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        datasetsClient = clientBuilder.buildDatasetsClient();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDataset(HttpClient httpClient) throws FileNotFoundException, URISyntaxException {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        Path filePath = getPath("product_info.md");

        FileDatasetVersion createdDatasetVersion
            = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);
    }
}
