// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.Configuration;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.IndexDocumentsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.InputFieldMappingEntry;
import com.azure.search.models.OutputFieldMappingEntry;
import com.azure.search.models.ServiceCounters;
import com.azure.search.models.ServiceLimits;
import com.azure.search.models.ServiceStatistics;
import com.azure.search.models.Skill;
import com.azure.search.models.Skillset;
import com.azure.search.models.SynonymMap;
import com.azure.search.models.WebApiSkill;
import com.azure.search.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This scenario assumes an existing search solution, with index and an indexer setup (see LifecycleSetupExample)
 * For more information visit Azure Search Sample Data
 * https://docs.microsoft.com/en-us/samples/azure-samples/azure-search-sample-data/azure-search-sample-data/
 */
public class RefineSearchCapabilitiesExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";
    private static final String INDEXER_NAME = "hotels-sample-indexer";

    public static void main(String[] args) {
        SearchServiceClient serviceClient = createServiceClient();
        SearchIndexClient indexClient = createIndexClient();

        // Add a synonym map to an index field
        addSynonymMapToIndex(serviceClient);

        // Add a custom web based skillset to the indexer
        addCustomWebSkillset(serviceClient);

        // Manually add a set of documents to the index
        uploadDocumentsToIndex(indexClient);

        // Retrieve service statistics
        getServiceStatistics(serviceClient);
    }

    private static void addCustomWebSkillset(SearchServiceClient client) {
        String skillsetName = "custom-web-skillset";
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/Description")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("textItems")
                .setTargetName("TextItems")
        );

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "Bing entity search API key");

        Skill webApiSkill = new WebApiSkill()
            .setUri("https://api.cognitive.microsoft.com/bing/v7.0/entities/")
            .setHttpMethod("POST") // Supports only "POST" and "PUT" HTTP methods
            .setHttpHeaders(headers)
            .setInputs(inputs)
            .setOutputs(outputs)
            .setName("webapi-skill")
            .setDescription("A WebApi skill that can be used as a custom skillset");

        Skillset skillset = new Skillset()
            .setName(skillsetName)
            .setDescription("Skillset for testing custom skillsets")
            .setSkills(Collections.singletonList(webApiSkill));

        client.createOrUpdateSkillset(skillset);
        System.out.printf("Created Skillset %s%n", skillsetName);

        Indexer indexer = client.getIndexer(INDEXER_NAME).setSkillsetName(skillsetName);
        client.createOrUpdateIndexer(indexer);
        System.out.printf("Updated Indexer %s with  Skillset %s%n", INDEXER_NAME, skillsetName);
    }

    private static void getServiceStatistics(SearchServiceClient client) {
        ServiceStatistics statistics = client.getServiceStatistics();
        ServiceCounters counters = statistics.getCounters();
        ServiceLimits limits = statistics.getLimits();

        System.out.println("Service Statistics:");
        System.out.printf("     Documents quota: %d, documents usage: %d%n", counters.getDocumentCounter().getQuota(), counters.getDocumentCounter().getUsage());
        System.out.printf("     Max fields per index limit: %d%n", limits.getMaxFieldsPerIndex());

    }

    private static void uploadDocumentsToIndex(SearchIndexClient client) {

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel().setHotelId("100"));
        hotels.add(new Hotel().setHotelId("200"));
        hotels.add(new Hotel().setHotelId("300"));

        // Perform index operations on a list of documents
        IndexDocumentsResult result = client.mergeOrUploadDocuments(hotels);
        System.out.printf("Indexed %s documents%n", result.getResults().size());
    }

    private static void addSynonymMapToIndex(SearchServiceClient client) {
        String synonymMapName = "hotel-synonym-sample";
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel, motel\ninternet,wifi\nfive star=>luxury\neconomy,inexpensive=>budget");

        client.createOrUpdateSynonymMap(synonymMap);

        Index index = client.getIndex(INDEX_NAME);
        List<Field> fields = index.getFields();
        fields.get(1).setSynonymMaps(Collections.singletonList(synonymMapName));
        index.setFields(fields);

        client.createOrUpdateIndex(index);
        System.out.printf("Updated index %s with synonym map %s on field %s%n", INDEX_NAME, synonymMapName, "HotelName");
    }

    private static SearchServiceClient createServiceClient() {
        return new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    private static SearchIndexClient createIndexClient() {
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
    }
}
