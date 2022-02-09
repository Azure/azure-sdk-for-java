// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.quantum.jobs.models.BlobDetails;
import com.azure.quantum.jobs.models.SasUriResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageClientTest extends QuantumClientTestBase {

    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private StorageClient client;

    private void initializeClient(HttpClient httpClient) {
        client = getClientBuilder(httpClient).buildStorageClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void sasUriTest(HttpClient httpClient) {
        initializeClient(httpClient);

        String containerName = "testcontainer";
        SasUriResponse response = client.sasUri(new BlobDetails().setContainerName(containerName));

        assertNotEquals(null, response);
        assertTrue(response.getSasUri().matches(String.format("https://[a-zA-Z0-9-]{3,63}.blob.core.windows.net/%s\\?sv=.*", containerName)));
    }
}
