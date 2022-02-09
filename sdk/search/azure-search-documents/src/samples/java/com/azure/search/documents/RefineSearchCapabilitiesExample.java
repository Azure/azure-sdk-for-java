// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchServiceCounters;
import com.azure.search.documents.indexes.models.SearchServiceLimits;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.indexes.models.WebApiSkill;
import com.azure.search.documents.models.Hotel;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This scenario assumes an existing search solution, with index and an indexer setup (see LifecycleSetupExample) For
 * more information visit <a href="https://github.com/Azure-Samples/azure-search-sample-data">Azure Cognitive Search
 * Sample Data</a>.
 */
public class RefineSearchCapabilitiesExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key, and set the values of these
     * environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";
    private static final String INDEXER_NAME = "hotels-sample-indexer";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = createIndexClient();
        SearchIndexerClient searchIndexerClient = createIndexerClient();
        SearchClient indexClient = createSearchClient();

        // Add a synonym map to an index field
        addSynonymMapToIndex(searchIndexClient);

        // Add a custom web based skillset to the indexer
        addCustomWebSkillset(searchIndexerClient);

        // Manually add a set of documents to the index
        uploadDocumentsToIndex(indexClient);

        // Retrieve service statistics
        getServiceStatistics(searchIndexClient);
    }

    private static void addCustomWebSkillset(SearchIndexerClient client) {
        String skillsetName = "custom-web-skillset";
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("text")
                .setSource("/document/Description")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry("textItems")
                .setTargetName("TextItems")
        );

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "Bing entity search API key");

        SearchIndexerSkill webApiSkill = new WebApiSkill(inputs, outputs,
            "https://api.cognitive.microsoft.com/bing/v7.0/entities/")
            .setHttpMethod("POST") // Supports only "POST" and "PUT" HTTP methods
            .setHttpHeaders(headers)
            .setName("webapi-skill")
            .setDescription("A WebApi skill that can be used as a custom skillset");

        SearchIndexerSkillset skillset = new SearchIndexerSkillset(skillsetName, Collections.singletonList(webApiSkill))
            .setDescription("Skillset for testing custom skillsets");

        client.createOrUpdateSkillset(skillset);
        System.out.printf("Created Skillset %s%n", skillsetName);

        SearchIndexer indexer = client.getIndexer(INDEXER_NAME).setSkillsetName(skillsetName);
        client.createOrUpdateIndexer(indexer);
        System.out.printf("Updated Indexer %s with  Skillset %s%n", INDEXER_NAME, skillsetName);
    }

    private static void getServiceStatistics(SearchIndexClient client) {
        SearchServiceStatistics statistics = client.getServiceStatistics();
        SearchServiceCounters counters = statistics.getCounters();
        SearchServiceLimits limits = statistics.getLimits();

        System.out.println("Service Statistics:");
        System.out.printf("     Documents quota: %d, documents usage: %d%n", counters.getDocumentCounter().getQuota(), counters.getDocumentCounter().getUsage());
        System.out.printf("     Max fields per index limit: %d%n", limits.getMaxFieldsPerIndex());

    }

    private static void uploadDocumentsToIndex(SearchClient client) {

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));

        // Perform index operations on a list of documents
        IndexDocumentsResult result = client.mergeOrUploadDocuments(hotels);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }

    private static void addSynonymMapToIndex(SearchIndexClient client) {
        String synonymMapName = "hotel-synonym-sample";
        SynonymMap synonymMap = new SynonymMap(synonymMapName,
            "hotel, motel\ninternet,wifi\nfive star=>luxury\neconomy,inexpensive=>budget");

        client.createOrUpdateSynonymMap(synonymMap);

        SearchIndex index = client.getIndex(INDEX_NAME);
        List<SearchField> fields = index.getFields();
        fields.get(1).setSynonymMapNames(synonymMapName);
        index.setFields(fields);

        client.createOrUpdateIndex(index);
        System.out.printf("Updated index %s with synonym map %s on field %s%n", INDEX_NAME, synonymMapName, "HotelName");
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

    private static SearchClient createSearchClient() {
        return new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
    }
}
