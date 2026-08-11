// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.IngestionDefinition;
import com.azure.analytics.planetarycomputer.models.IngestionRun;
import com.azure.analytics.planetarycomputer.models.IngestionSource;
import com.azure.analytics.planetarycomputer.models.IngestionType;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityConnection;
import com.azure.analytics.planetarycomputer.models.ManagedIdentityIngestionSource;
import com.azure.analytics.planetarycomputer.models.RenderOption;
import com.azure.analytics.planetarycomputer.models.RenderOptionType;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureToken;
import com.azure.analytics.planetarycomputer.models.StacCatalogCollections;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.analytics.planetarycomputer.models.StacItem;
import com.azure.analytics.planetarycomputer.models.StacItemCollection;
import com.azure.analytics.planetarycomputer.models.StacSearchParameters;
import com.azure.analytics.planetarycomputer.models.TilerMosaicSearchRegistrationResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;

public final class ReadmeSamples {
    public void createStacClient() {
        // BEGIN: readme-sample-createStacClient
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();
        // END: readme-sample-createStacClient
    }

    public void createStacAsyncClient() {
        // BEGIN: readme-sample-createStacAsyncClient
        StacAsyncClient stacAsyncClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacAsyncClient();
        // END: readme-sample-createStacAsyncClient
    }

    public void getCollection() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-getCollection
        StacCollection collection = stacClient.getCollection("naip-atl", null, null);
        System.out.printf("Collection ID: %s, Description: %s%n",
            collection.getId(), collection.getDescription());
        // END: readme-sample-getCollection
    }

    public void searchItems() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-searchItems
        StacItemCollection results = stacClient.search(
            new StacSearchParameters()
                .setCollections(Arrays.asList("naip-atl"))
                .setDatetime("2021-01-01T00:00:00Z/2022-12-31T00:00:00Z")
                .setLimit(10),
            null, null);
        System.out.printf("Found %d items%n", results.getFeatures().size());
        // END: readme-sample-searchItems
    }

    public void getToken() {
        // BEGIN: readme-sample-getToken
        SharedAccessSignatureClient sasClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildSharedAccessSignatureClient();
        SharedAccessSignatureToken token = sasClient.getToken("naip-atl", null);
        // END: readme-sample-getToken
    }

    public void listCollections() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-listCollections
        StacCatalogCollections collections = stacClient.getCollections();
        for (StacCollection col : collections.getCollections()) {
            System.out.printf("Collection: %s - %s%n", col.getId(), col.getTitle());
        }
        // END: readme-sample-listCollections
    }

    public void getItem() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-getItem
        StacItem item = stacClient.getItem("naip-atl", "ga_m_3308421_se_16_060_20211114");
        System.out.printf("Item ID: %s, Collection: %s%n", item.getId(), item.getCollection());
        System.out.printf("Assets: %s%n", item.getAssets().keySet());
        // END: readme-sample-getItem
    }

    public void createCollection() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-createCollection
        String collectionJson = "{"
            + "\"id\": \"my-collection\","
            + "\"type\": \"Collection\","
            + "\"stac_version\": \"1.0.0\","
            + "\"description\": \"My geospatial dataset\","
            + "\"license\": \"proprietary\","
            + "\"extent\": {\"spatial\": {\"bbox\": [[-180, -90, 180, 90]]},"
            + "\"temporal\": {\"interval\": [[\"2020-01-01T00:00:00Z\", null]]}},"
            + "\"links\": []}";

        SyncPoller<BinaryData, BinaryData> poller = stacClient.beginCreateCollection(
            BinaryData.fromString(collectionJson), new RequestOptions());
        poller.getFinalResult();
        // END: readme-sample-createCollection
    }

    public void configureVisualization() {
        StacClient stacClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildStacClient();

        // BEGIN: readme-sample-configureVisualization
        RenderOption renderOption = new RenderOption("natural-color", "Natural Color")
            .setType(RenderOptionType.RASTER_TILE)
            .setOptions("assets=image&asset_bidx=image|1,2,3")
            .setMinZoom(6);
        RenderOption created = stacClient.createRenderOption("naip-atl", renderOption);
        System.out.printf("Created render option: %s%n", created.getId());
        // END: readme-sample-configureVisualization
    }

    public void mosaicTiles() {
        DataClient dataClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildDataClient();

        // BEGIN: readme-sample-mosaicTiles
        String searchBody = "{\"filter\":{\"op\":\"and\",\"args\":["
            + "{\"op\":\"=\",\"args\":[{\"property\":\"collection\"},\"naip-atl\"]},"
            + "{\"op\":\">=\",\"args\":[{\"property\":\"datetime\"},\"2021-01-01T00:00:00Z\"]}"
            + "]},\"filter-lang\":\"cql2-json\"}";

        Response<BinaryData> searchResponse = dataClient.registerMosaicsSearchWithResponse(
            BinaryData.fromString(searchBody), new RequestOptions());
        String searchId = searchResponse.getValue()
            .toObject(TilerMosaicSearchRegistrationResponse.class).getSearchId();
        System.out.printf("Registered mosaic search: %s%n", searchId);
        // END: readme-sample-mosaicTiles
    }

    public void pointValues() {
        DataClient dataClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildDataClient();

        // BEGIN: readme-sample-pointValues
        RequestOptions options = new RequestOptions();
        options.addQueryParam("assets", "image", false);
        Response<BinaryData> response = dataClient.getItemPointWithResponse(
            "naip-atl", "ga_m_3308421_se_16_060_20211114", -84.386, 33.676, options);
        System.out.printf("Point data: %s%n", response.getValue().toString());
        // END: readme-sample-pointValues
    }

    public void mapTile() {
        DataClient dataClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildDataClient();

        // BEGIN: readme-sample-mapTile
        RequestOptions options = new RequestOptions();
        options.addQueryParam("assets", "image", false);
        options.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData tile = dataClient.getTileWithTmsByFormatWithResponse(
            "naip-atl", "ga_m_3308421_se_16_060_20211114",
            "WebMercatorQuad", 14, 4349, 6564, "png", options).getValue();
        System.out.printf("Tile size: %d bytes%n", tile.toBytes().length);
        // END: readme-sample-mapTile
    }

    public void ingestionSource() {
        IngestionClient ingestionClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildIngestionClient();

        // BEGIN: readme-sample-ingestionSource
        IngestionSource source = ingestionClient.createSource(
            new ManagedIdentityIngestionSource("source-id",
                new ManagedIdentityConnection("https://storage.blob.core.windows.net/container",
                    "managed-identity-object-id")));
        System.out.printf("Created ingestion source: %s%n", source.getId());
        // END: readme-sample-ingestionSource
    }

    public void ingestion() {
        IngestionClient ingestionClient = new PlanetaryComputerProClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-endpoint>")
            .buildIngestionClient();

        // BEGIN: readme-sample-ingestion
        IngestionDefinition ingestion = new IngestionDefinition();
        ingestion.setImportType(IngestionType.STATIC_CATALOG);
        ingestion.setDisplayName("My Dataset Ingestion");
        ingestion.setSourceCatalogUrl("https://example.com/catalog.json");

        IngestionDefinition created = ingestionClient.create("my-collection", ingestion);
        IngestionRun run = ingestionClient.createRun("my-collection", created.getId());
        System.out.printf("Ingestion run started: %s%n", run.getId());
        // END: readme-sample-ingestion
    }
}
