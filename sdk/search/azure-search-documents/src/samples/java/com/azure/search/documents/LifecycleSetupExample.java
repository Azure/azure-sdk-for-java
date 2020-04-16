// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.DataContainer;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.DataSourceType;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.EntityRecognitionSkill;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.Indexer;
import com.azure.search.documents.models.IndexingSchedule;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.Skill;
import com.azure.search.documents.models.Skillset;
import com.azure.search.documents.models.Suggester;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This scenario assumes an existing search solution and uses a pre-population data source with sample data set
 * For more information visit Azure Search Sample Data:
 * https://docs.microsoft.com/en-us/samples/azure-samples/azure-search-sample-data/azure-search-sample-data/
 */
public class LifecycleSetupExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    // Using hard coded connection string to pre-populated Cosmos DB database with sample data set
    // For more information visit https://docs.microsoft.com/en-us/samples/azure-samples/azure-search-sample-data/azure-search-sample-data/
    private static final String COSMOS_CONNECTION_STRING = "AccountEndpoint=https://hotels-docbb.documents.azure.com:443/;AccountKey=4UPsNZyFAjgZ1tzHPGZaxS09XcwLrIawbXBWk6IixcxJoSePTcjBn0mi53XiKWu8MaUgowUhIovOv7kjksqAug==;Database=SampleData";
    private static final String COSMOS_COLLECTION_NAME = "hotels";

    private static final String INDEX_NAME = "hotels-sample-index1";
    private static final String DATASOURCE_NAME = "hotels-sample-datasource1";
    private static final String SKILLSET_NAME = "hotels-sample-skillset1";
    private static final String INDEXER_NAME = "hotels-sample-indexer1";
    private static final String SUGGESTER_NAME = "sg";

    public static void main(String[] args) {
        SearchServiceClient client = createServiceClient();
        // Create a data source for a Cosmos DB database
        DataSource dataSource = createCosmosDataSource(client);
        System.out.println("Created DataSource " + dataSource.getName());

        // Create an index
        Index index = createIndex(client);
        System.out.println("Created Index " + index.getName());

        // Create a skillset for Cognitive Services
        Skillset skillset = createSkillset(client);
        System.out.println("Created Skillset " + skillset.getName());

        // Create an indexer that uses the skillset and data source and loads the index
        Indexer indexer = createIndexer(client, dataSource, skillset, index);
        System.out.println("Created Indexer " + indexer.getName());

        // Update indexer schedule
        updateIndexerSchedule(client, indexer);
        System.out.println("Updated Indexer Schedule " + indexer.getName());

        // Clean up resources.
        client.deleteIndex(INDEX_NAME);
        client.deleteIndexer(INDEXER_NAME);
    }

    private static SearchServiceClient createServiceClient() {
        return new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    private static void updateIndexerSchedule(SearchServiceClient client, Indexer indexer) {
        IndexingSchedule indexingSchedule = new IndexingSchedule()
            .setInterval(Duration.ofMinutes(10));
        indexer.setSchedule(indexingSchedule);

        client.createOrUpdateIndexer(indexer);
    }

    private static Indexer createIndexer(SearchServiceClient client, DataSource dataSource, Skillset skillset, Index index) {
        Indexer indexer = new Indexer()
            .setName(INDEXER_NAME)
            .setDataSourceName(dataSource.getName())
            .setSkillsetName(skillset.getName())
            .setTargetIndexName(index.getName());

        return client.createOrUpdateIndexer(indexer);
    }

    private static Skillset createSkillset(SearchServiceClient client) {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/Description")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("locations")
                .setTargetName("locations")
        );


        Skill skill = new EntityRecognitionSkill()
            .setName("#1")
            .setDescription("Entity Recognition Skill")
            .setContext("/document/Description")
            .setInputs(inputs)
            .setOutputs(outputs);

        Skillset skillset = new Skillset()
            .setName(SKILLSET_NAME)
            .setDescription("Skillset for testing default configuration")
            .setSkills(Collections.singletonList(skill));


        return client.createOrUpdateSkillset(skillset);
    }

    private static Index createIndex(SearchServiceClient client) {

        // Index definition
        Index index = new Index()
            .setName(INDEX_NAME)
            .setFields(
                Arrays.asList(new Field()
                        .setName("HotelId")
                        .setType(DataType.EDM_STRING)
                        .setKey(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                        .setSearchable(Boolean.FALSE)
                        .setSortable(Boolean.FALSE),
                    new Field()
                        .setName("HotelName")
                        .setType(DataType.EDM_STRING)
                        .setFacetable(Boolean.FALSE)
                        .setFilterable(Boolean.FALSE)
                        .setHidden(Boolean.FALSE)
                        .setKey(Boolean.FALSE)
                        .setSearchable(Boolean.TRUE)
                        .setSortable(Boolean.FALSE)
                        .setAnalyzer(AnalyzerName.EN_MICROSOFT),
                    new Field()
                        .setName("Description")
                        .setType(DataType.EDM_STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.FALSE)
                        .setHidden(Boolean.FALSE)
                        .setSortable(Boolean.FALSE)
                        .setFacetable(Boolean.FALSE)
                        .setAnalyzer(AnalyzerName.EN_MICROSOFT),
                    new Field()
                        .setName("Tags")
                        .setType(DataType.collection(DataType.EDM_STRING))
                        .setFacetable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                        .setSearchable(Boolean.TRUE)
                        .setAnalyzer(AnalyzerName.EN_MICROSOFT)));

        // Set Suggester
        index.setSuggesters(Collections.singletonList(new Suggester()
            .setName(SUGGESTER_NAME)
            .setSourceFields(Collections.singletonList("Tags"))));

        return client.createOrUpdateIndex(index);
    }

    private static DataSource createCosmosDataSource(SearchServiceClient client) {

        DataContainer dataContainer = new DataContainer().setName(COSMOS_COLLECTION_NAME);
        HighWaterMarkChangeDetectionPolicy highWaterMarkChangeDetectionPolicy = new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts");

        DataSource dataSource = new DataSource()
            .setName(DATASOURCE_NAME)
            .setType(DataSourceType.COSMOS)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString(COSMOS_CONNECTION_STRING))
            .setContainer(dataContainer)
            .setDataChangeDetectionPolicy(highWaterMarkChangeDetectionPolicy);

        return client.createOrUpdateDataSource(dataSource);
    }


}
