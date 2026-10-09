// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatasetVersionSerializationTests {

    @Test
    void deserializesFileDatasetVersionFromBaseType() throws IOException {
        DatasetVersion dataset = deserialize("uri_file");

        assertInstanceOf(FileDatasetVersion.class, dataset);
        assertDataset(dataset, DatasetType.URI_FILE);
    }

    @Test
    void deserializesFolderDatasetVersionFromBaseType() throws IOException {
        DatasetVersion dataset = deserialize("uri_folder");

        assertInstanceOf(FolderDatasetVersion.class, dataset);
        assertDataset(dataset, DatasetType.URI_FOLDER);
    }

    private static DatasetVersion deserialize(String type) throws IOException {
        String json = "{\"name\":\"dataset\",\"version\":\"1\",\"type\":\"" + type
            + "\",\"dataUri\":\"azureml://data\",\"isReference\":true,\"connectionName\":\"storage\","
            + "\"id\":\"dataset-id\",\"description\":\"description\",\"tags\":{\"environment\":\"test\"}}";
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return DatasetVersion.fromJson(reader);
        }
    }

    private static void assertDataset(DatasetVersion dataset, DatasetType type) {
        assertEquals("dataset", dataset.getName());
        assertEquals("1", dataset.getVersion());
        assertEquals(type, dataset.getType());
        assertEquals("azureml://data", dataset.getDataUrl());
        assertEquals(true, dataset.isReference());
        assertEquals("storage", dataset.getConnectionName());
        assertEquals("dataset-id", dataset.getId());
        assertEquals("description", dataset.getDescription());
        assertEquals("test", dataset.getTags().get("environment"));
    }
}
