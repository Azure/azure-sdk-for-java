// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.DistanceScoringFunction;
import com.azure.search.documents.indexes.models.DistanceScoringParameters;
import com.azure.search.documents.indexes.models.FreshnessScoringFunction;
import com.azure.search.documents.indexes.models.FreshnessScoringParameters;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;
import com.azure.search.documents.indexes.models.TextWeights;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In this sample we will demonstrate how to get statistics for and Index and a service.
 */
public class IndexAndServiceStatisticsExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service name and API key and populate ADMIN_KEY and
     * SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchIndexClient client = createClient();
        getIndexStatistics(client);
        getServiceStatistics(client);
    }

    private static void getServiceStatistics(SearchIndexClient client) {
        SearchServiceStatistics searchServiceStatistics = client.getServiceStatistics();

        System.out.println(":" + searchServiceStatistics);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(searchServiceStatistics);
            System.out.println(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        /* Output:
          {
             "counters":{
                "documentCount":{
                   "usage":5009,
                   "quota":null
                },
                "indexesCount":{
                   "usage":4,
                   "quota":50
                },
                "indexersCount":{
                   "usage":2,
                   "quota":50
                },
                "dataSourcesCount":{
                   "usage":2,
                   "quota":50
                },
                "storageSize":{
                   "usage":19551042,
                   "quota":26843545600
                },
                "synonymMaps":{
                   "usage":0,
                   "quota":5
                }
             },
             "limits":{
                "maxFieldsPerIndex":1000,
                "maxFieldNestingDepthPerIndex":10,
                "maxComplexCollectionFieldsPerIndex":40,
                "maxComplexObjectsInCollectionsPerDocument":3000
             }
          }
         */
    }

    private static void getIndexStatistics(SearchIndexClient client) {
        SearchIndex testIndex = createTestIndex();
        SearchIndex index = client.createOrUpdateIndex(testIndex);
        SearchIndexStatistics result = client.getIndexStatistics(index.getName());
        long documentCount = result.getDocumentCount();
        long storageSize = result.getStorageSize();

        System.out.println("document Count:" + documentCount);
        System.out.println("storage Size:" + storageSize);

        // Output:
        // document Count:0
        // storage Size:0
    }

    /**
     * A helper method to create an index for testing
     *
     * @return an Index
     */
    private static SearchIndex createTestIndex() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        List<SearchField> fieldList = Arrays.asList(
            new SearchField("HotelId", SearchFieldDataType.STRING)
                .setKey(Boolean.TRUE)
                .setSearchable(Boolean.FALSE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.FALSE)
                .setHidden(Boolean.FALSE),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setKey(Boolean.FALSE)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.FALSE)
                .setSortable(Boolean.FALSE)
                .setFacetable(Boolean.FALSE)
                .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                .setHidden(Boolean.FALSE),
            new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.FALSE)
                .setSortable(Boolean.FALSE)
                .setFacetable(Boolean.FALSE)
                .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                .setHidden(Boolean.FALSE),
            new SearchField("Description_Custom", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.FALSE)
                .setSortable(Boolean.FALSE)
                .setFacetable(Boolean.FALSE)
                .setSearchAnalyzerName(LexicalAnalyzerName.STOP)
                .setIndexAnalyzerName(LexicalAnalyzerName.STOP)
                .setHidden(Boolean.FALSE),
            new SearchField("Category", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.FALSE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("ParkingIncluded", SearchFieldDataType.BOOLEAN)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("LastRenovationDate", SearchFieldDataType.DATE_TIME_OFFSET)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("Rating", SearchFieldDataType.INT32)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("Address", SearchFieldDataType.COMPLEX)
                .setFields(Arrays.asList(
                    new SearchField("StreetAddress", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("City", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("StateProvince", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Country", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("PostalCode", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                    )
                ),
            new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.FALSE)
                .setHidden(Boolean.FALSE),
            new SearchField("Rooms", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                .setFields(Arrays.asList(
                    new SearchField("Description", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                        .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                    new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                        .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
                    new SearchField("Type", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("BaseRate", SearchFieldDataType.DOUBLE)
                        .setKey(Boolean.FALSE)
                        .setSearchable(Boolean.FALSE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("BedOptions", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("SleepsCount", SearchFieldDataType.INT32)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.FALSE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)
                    )
                ),
            new SearchField("TotalGuests", SearchFieldDataType.INT64)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.TRUE),
            new SearchField("ProfitMargin", SearchFieldDataType.DOUBLE)
        );
        return new SearchIndex("hotels", fieldList)
            .setScoringProfiles(Arrays.asList(
                new ScoringProfile("MyProfile")
                    .setFunctionAggregation(ScoringFunctionAggregation.AVERAGE)
                    .setFunctions(new MagnitudeScoringFunction("Rating", 2.0,
                            new MagnitudeScoringParameters(1, 4)
                                .setShouldBoostBeyondRangeByConstant(true))
                            .setInterpolation(ScoringFunctionInterpolation.CONSTANT),
                        new DistanceScoringFunction("Location", 1.5,
                            new DistanceScoringParameters("Loc", 5))
                            .setInterpolation(ScoringFunctionInterpolation.LINEAR),
                        new FreshnessScoringFunction("LastRenovationDate", 1.1,
                            new FreshnessScoringParameters(Duration.ofDays(365)))
                            .setInterpolation(ScoringFunctionInterpolation.LOGARITHMIC)
                    )
                    .setTextWeights(new TextWeights(weights)),
                new ScoringProfile("ProfileTwo")
                    .setFunctionAggregation(ScoringFunctionAggregation.MAXIMUM)
                    .setFunctions(new TagScoringFunction("Tags", 1.5, new TagScoringParameters("MyTags"))
                        .setInterpolation(ScoringFunctionInterpolation.LINEAR)
                    ),
                new ScoringProfile("ProfileThree")
                    .setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                    .setFunctions(new MagnitudeScoringFunction("Rating", 3.0,
                        new MagnitudeScoringParameters(0, 10)
                            .setShouldBoostBeyondRangeByConstant(false))
                        .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)
                    ),
                new ScoringProfile("ProfileFour")
                    .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                    .setFunctions(new MagnitudeScoringFunction("Rating", 3.14,
                        new MagnitudeScoringParameters(1, 5)
                            .setShouldBoostBeyondRangeByConstant(false))
                        .setInterpolation(ScoringFunctionInterpolation.CONSTANT))))
            .setDefaultScoringProfile("MyProfile")
            .setCorsOptions(new CorsOptions(Arrays.asList("http://tempuri.org", "http://localhost:80"))
                .setMaxAgeInSeconds(60L))
            .setSuggesters(new SearchSuggester("FancySuggester", Collections.singletonList("HotelName")));
    }

    /**
     * Builds a {@link SearchIndexClient}
     *
     * @return async service client
     */
    private static SearchIndexClient createClient() {
        AzureKeyCredential searchApiKeyCredential = new AzureKeyCredential(ADMIN_KEY);
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(searchApiKeyCredential)
            .buildClient();
    }
}
