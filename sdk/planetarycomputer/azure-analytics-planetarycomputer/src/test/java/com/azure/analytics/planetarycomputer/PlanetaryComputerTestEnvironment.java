// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.core.util.Configuration;

/**
 * Test environment configuration for Planetary Computer tests.
 * Reads configuration from environment variables.
 */
public class PlanetaryComputerTestEnvironment {
    private final Configuration configuration;

    public PlanetaryComputerTestEnvironment() {
        this.configuration = Configuration.getGlobalConfiguration();
    }

    /**
     * Get the Planetary Computer endpoint.
     */
    public String getEndpoint() {
        return configuration.get("PLANETARYCOMPUTER_ENDPOINT",
            "https://test-accessibility.h5d5a9crhnc8deaz.uksouth.geocatalog.spatio.azure.com");
    }

    /**
     * Get the test collection ID.
     */
    public String getCollectionId() {
        return configuration.get("PLANETARYCOMPUTER_COLLECTION_ID", "naip-atl");
    }

    /**
     * Get the test item ID.
     */
    public String getItemId() {
        return configuration.get("PLANETARYCOMPUTER_ITEM_ID", "ga_m_3308421_se_16_060_20211114");
    }

    /**
     * Get the ingestion container URI.
     */
    public String getIngestionContainerUri() {
        return configuration.get("PLANETARYCOMPUTER_INGESTION_CONTAINER_URI",
            "https://datazoo.blob.core.windows.net/sentinel2static");
    }

    /**
     * Get the ingestion catalog URL.
     */
    public String getIngestionCatalogUrl() {
        return configuration.get("PLANETARYCOMPUTER_INGESTION_CATALOG_URL",
            "https://raw.githubusercontent.com/chahibi/mpcpro-sample-datasets/refs/heads/main/datasets/planetary_computer/naip/atl/catalog.json");
    }

    /**
     * Get the managed identity object ID.
     */
    public String getManagedIdentityObjectId() {
        return configuration.get("PLANETARYCOMPUTER_MANAGED_IDENTITY_OBJECT_ID",
            "ebad594e-84af-49da-89db-7bffc9c39f3a");
    }

    /**
     * Get the SAS token ingestion container URI.
     */
    public String getIngestionSasContainerUri() {
        return configuration.get("PLANETARYCOMPUTER_INGESTION_SAS_CONTAINER_URI",
            "https://examplestorage.blob.core.windows.net/sample-container");
    }

    /**
     * Get the SAS token for ingestion.
     */
    public String getIngestionSasToken() {
        return configuration.get("PLANETARYCOMPUTER_INGESTION_SAS_TOKEN",
            "sv=2021-01-01&st=2020-01-01T00:00:00Z&se=2099-12-31T23:59:59Z&sr=c&sp=rl&sig=faketoken");
    }

    /**
     * Get test mode from environment.
     */
    public String getTestMode() {
        return configuration.get("AZURE_TEST_MODE", "playback");
    }
}
