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
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;
import com.azure.search.documents.indexes.models.TextWeights;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In this sample we will demonstrate how to get statistics for and Index and a service.
 */
public class IndexAndServiceStatisticsExample {

    /**
     * From the Azure portal, get your Azure AI Search service name and API key and populate ADMIN_KEY and
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
        try {
            System.out.println(searchServiceStatistics.toJsonString());
        } catch (IOException ex) {
            ex.printStackTrace();
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
        GetIndexStatisticsResult result = client.getIndexStatistics(index.getName());
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
                .setKey(true)
                .setSearchable(false)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(false)
                .setRetrievable(true),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setKey(false)
                .setSearchable(true)
                .setFilterable(false)
                .setSortable(false)
                .setFacetable(false)
                .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                .setRetrievable(true),
            new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(false)
                .setSortable(false)
                .setFacetable(false)
                .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                .setRetrievable(true),
            new SearchField("Description_Custom", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(false)
                .setSortable(false)
                .setFacetable(false)
                .setSearchAnalyzerName(LexicalAnalyzerName.STOP)
                .setIndexAnalyzerName(LexicalAnalyzerName.STOP)
                .setRetrievable(true),
            new SearchField("Category", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(false)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("ParkingIncluded", SearchFieldDataType.BOOLEAN)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("LastRenovationDate", SearchFieldDataType.DATE_TIME_OFFSET)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("Rating", SearchFieldDataType.INT32)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(true),
            new SearchField("Address", SearchFieldDataType.COMPLEX)
                .setFields(
                    new SearchField("StreetAddress", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setRetrievable(true),
                    new SearchField("City", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("StateProvince", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("Country", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("PostalCode", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true)
                        .setRetrievable(true)),
            new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(false)
                .setRetrievable(true),
            new SearchField("Rooms", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                .setFields(
                    new SearchField("Description", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setRetrievable(true)
                        .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                    new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setRetrievable(true)
                        .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
                    new SearchField("Type", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("BaseRate", SearchFieldDataType.DOUBLE)
                        .setKey(false)
                        .setSearchable(false)
                        .setFilterable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("BedOptions", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("SleepsCount", SearchFieldDataType.INT32)
                        .setFilterable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                        .setFilterable(true)
                        .setFacetable(true)
                        .setRetrievable(true),
                    new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(false)
                        .setFacetable(true)
                        .setRetrievable(true)),
            new SearchField("TotalGuests", SearchFieldDataType.INT64)
                .setFilterable(true)
                .setSortable(true)
                .setFacetable(true)
                .setRetrievable(false),
            new SearchField("ProfitMargin", SearchFieldDataType.DOUBLE)
        );
        return new SearchIndex("hotels", fieldList)
            .setScoringProfiles(
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
                            .setInterpolation(ScoringFunctionInterpolation.LOGARITHMIC))
                    .setTextWeights(new TextWeights(weights)),
                new ScoringProfile("ProfileTwo")
                    .setFunctionAggregation(ScoringFunctionAggregation.MAXIMUM)
                    .setFunctions(new TagScoringFunction("Tags", 1.5, new TagScoringParameters("MyTags"))
                        .setInterpolation(ScoringFunctionInterpolation.LINEAR)),
                new ScoringProfile("ProfileThree")
                    .setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                    .setFunctions(new MagnitudeScoringFunction("Rating", 3.0,
                        new MagnitudeScoringParameters(0, 10)
                            .setShouldBoostBeyondRangeByConstant(false))
                        .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)),
                new ScoringProfile("ProfileFour")
                    .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                    .setFunctions(new MagnitudeScoringFunction("Rating", 3.5,
                        new MagnitudeScoringParameters(1, 5)
                            .setShouldBoostBeyondRangeByConstant(false))
                        .setInterpolation(ScoringFunctionInterpolation.CONSTANT)))
            .setDefaultScoringProfile("MyProfile")
            .setCorsOptions(new CorsOptions("http://tempuri.org", "http://localhost:80").setMaxAgeInSeconds(60L))
            .setSuggesters(new SearchSuggester("FancySuggester", "HotelName"));
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
