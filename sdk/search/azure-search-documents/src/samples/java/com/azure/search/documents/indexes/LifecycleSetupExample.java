// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.IndexingSchedule;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchSuggester;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This scenario assumes an existing search solution and uses a pre-population data source with sample data set For more
 * information visit Azure Cognitive Search Sample Data: https://docs.microsoft.com/en-us/samples/azure-samples/azure-search-sample-data/azure-search-sample-data/
 */
public class LifecycleSetupExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key, and set the values of these
     * environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    // Using hard coded connection string to pre-populated Cosmos DB database with sample data set
    // For more information visit https://github.com/Azure-Samples/azure-search-sample-data
    private static final String COSMOS_CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("AZURE_COSMOS_CONNECTION_STRING");
    private static final String COSMOS_COLLECTION_NAME = "hotels";

    private static final String INDEX_NAME = "hotels-sample-index1";
    private static final String DATASOURCE_NAME = "hotels-sample-datasource1";
    private static final String SKILLSET_NAME = "hotels-sample-skillset1";
    private static final String INDEXER_NAME = "hotels-sample-indexer1";
    private static final String SUGGESTER_NAME = "sg";

    public static void main(String[] args) {
        SearchIndexClient indexClient = createIndexClient();
        SearchIndexerClient indexerClient = createIndexerClient();
        // Create a data source for a Cosmos DB database
        SearchIndexerDataSourceConnection dataSource = createCosmosDataSource(indexerClient);

        System.out.println("Created DataSource " + dataSource.getName());

        // Create an index
        SearchIndex index = createIndex(indexClient);
        System.out.println("Created Index " + index.getName());

        // Create a skillset for Cognitive Services
        SearchIndexerSkillset skillset = createSkillset(indexerClient);
        System.out.println("Created Skillset " + skillset.getName());

        // Create an indexer that uses the skillset and data source and loads the index
        SearchIndexer indexer = createIndexer(indexerClient, dataSource, skillset, index);
        System.out.println("Created Indexer " + indexer.getName());

        // Update indexer schedule
        updateIndexerSchedule(indexerClient, indexer);
        System.out.println("Updated Indexer Schedule " + indexer.getName());

        // Clean up resources.
        indexClient.deleteIndex(INDEX_NAME);
        indexerClient.deleteIndexer(INDEXER_NAME);
    }

    private static SearchIndexClient createIndexClient() {
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    private static SearchIndexerClient createIndexerClient() {
        return new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    private static void updateIndexerSchedule(SearchIndexerClient client, SearchIndexer indexer) {
        IndexingSchedule indexingSchedule = new IndexingSchedule(Duration.ofMinutes(10));
        indexer.setSchedule(indexingSchedule);

        client.createOrUpdateIndexer(indexer);
    }

    private static SearchIndexer createIndexer(SearchIndexerClient client, SearchIndexerDataSourceConnection dataSource,
        SearchIndexerSkillset skillset, SearchIndex index) {
        SearchIndexer indexer = new SearchIndexer(INDEXER_NAME, dataSource.getName(), index.getName())
            .setSkillsetName(skillset.getName());

        return client.createOrUpdateIndexer(indexer);
    }

    private static SearchIndexerSkillset createSkillset(SearchIndexerClient client) {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("text")
                .setSource("/document/Description")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry("locations")
                .setTargetName("locations")
        );


        SearchIndexerSkill skill = new EntityRecognitionSkill(inputs, outputs)
            .setName("#1")
            .setDescription("Entity Recognition Skill")
            .setContext("/document/Description");

        SearchIndexerSkillset skillset = new SearchIndexerSkillset(SKILLSET_NAME, Collections.singletonList(skill))
            .setDescription("Skillset for testing default configuration");


        return client.createOrUpdateSkillset(skillset);
    }

    private static SearchIndex createIndex(SearchIndexClient client) {

        List<SearchField> fields = Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING)
                .setKey(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setHidden(Boolean.FALSE)
                .setSearchable(Boolean.FALSE)
                .setSortable(Boolean.FALSE),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setFacetable(Boolean.FALSE)
                .setFilterable(Boolean.FALSE)
                .setHidden(Boolean.FALSE)
                .setKey(Boolean.FALSE)
                .setSearchable(Boolean.TRUE)
                .setSortable(Boolean.FALSE)
                .setAnalyzerName(LexicalAnalyzerName.EN_MICROSOFT),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.FALSE)
                .setHidden(Boolean.FALSE)
                .setSortable(Boolean.FALSE)
                .setFacetable(Boolean.FALSE)
                .setAnalyzerName(LexicalAnalyzerName.EN_MICROSOFT),
            new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setFacetable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setHidden(Boolean.FALSE)
                .setSearchable(Boolean.TRUE)
                .setAnalyzerName(LexicalAnalyzerName.EN_MICROSOFT));
        // Index definition
        SearchIndex index = new SearchIndex(INDEX_NAME, fields);

        // Set Suggester
        index.setSuggesters(new SearchSuggester(SUGGESTER_NAME, Collections.singletonList("Tags")));

        return client.createOrUpdateIndex(index);
    }

    private static SearchIndexerDataSourceConnection createCosmosDataSource(SearchIndexerClient client) {

        SearchIndexerDataContainer dataContainer = new SearchIndexerDataContainer(COSMOS_COLLECTION_NAME);
        HighWaterMarkChangeDetectionPolicy highWaterMarkChangeDetectionPolicy =
            new HighWaterMarkChangeDetectionPolicy("_ts");

        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection(DATASOURCE_NAME,
            SearchIndexerDataSourceType.COSMOS_DB, COSMOS_CONNECTION_STRING, dataContainer)
            .setDataChangeDetectionPolicy(highWaterMarkChangeDetectionPolicy);

        return client.createOrUpdateDataSourceConnection(dataSource);
    }


}
