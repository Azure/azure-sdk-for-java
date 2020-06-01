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
import com.azure.search.documents.indexes.models.ServiceStatistics;
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
import java.util.Map;

/**
 * In this sample we will demonstrate how to get statistics for and Index and a service.
 */
public class IndexAndServiceStatisticsExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service name and API key and
     * populate ADMIN_KEY and SEARCH_SERVICE_NAME.
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchIndexClient client = createClient();
        getIndexStatistics(client);
        getServiceStatistics(client);
    }

    private static void getServiceStatistics(SearchIndexClient client) {
        ServiceStatistics serviceStatistics = client.getServiceStatistics();

        System.out.println(":" + serviceStatistics);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(serviceStatistics);
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
        return new SearchIndex()
            .setName("hotels")
            .setFields(Arrays.asList(
                new SearchField()
                    .setName("HotelId")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("HotelName")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.FALSE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Description")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(Boolean.FALSE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("DescriptionFr")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Description_Custom")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setSearchAnalyzerName(LexicalAnalyzerName.STOP)
                    .setIndexAnalyzerName(LexicalAnalyzerName.STOP)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Category")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Tags")
                    .setType(SearchFieldDataType.collection(SearchFieldDataType.STRING))
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("ParkingIncluded")
                    .setType(SearchFieldDataType.BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("SmokingAllowed")
                    .setType(SearchFieldDataType.BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("LastRenovationDate")
                    .setType(SearchFieldDataType.DATE_TIME_OFFSET)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Rating")
                    .setType(SearchFieldDataType.INT32)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Address")
                    .setType(SearchFieldDataType.COMPLEX)
                    .setFields(Arrays.asList(
                        new SearchField()
                            .setName("StreetAddress")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("City")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("StateProvince")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("Country")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("PostalCode")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                        )
                    ),
                new SearchField()
                    .setName("Location")
                    .setType(SearchFieldDataType.GEOGRAPHY_POINT)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.FALSE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Rooms")
                    .setType(SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                    .setFields(Arrays.asList(
                        new SearchField()
                            .setName("Description")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                            .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                        new SearchField()
                            .setName("DescriptionFr")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                            .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
                        new SearchField()
                            .setName("Type")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("BaseRate")
                            .setType(SearchFieldDataType.DOUBLE)
                            .setKey(Boolean.FALSE)
                            .setSearchable(Boolean.FALSE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("BedOptions")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("SleepsCount")
                            .setType(SearchFieldDataType.INT32)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("SmokingAllowed")
                            .setType(SearchFieldDataType.BOOLEAN)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new SearchField()
                            .setName("Tags")
                            .setType(SearchFieldDataType.collection(SearchFieldDataType.STRING))
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.FALSE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                        )
                    ),
                new SearchField()
                    .setName("TotalGuests")
                    .setType(SearchFieldDataType.INT64)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.TRUE),
                new SearchField()
                    .setName("ProfitMargin")
                    .setType(SearchFieldDataType.DOUBLE)
                )
            )
            .setScoringProfiles(Arrays.asList(
                new ScoringProfile()
                    .setName("MyProfile")
                    .setFunctionAggregation(ScoringFunctionAggregation.AVERAGE)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(1)
                                .setBoostingRangeEnd(4)
                                .setShouldBoostBeyondRangeByConstant(true))
                            .setFieldName("Rating")
                            .setBoost(2.0)
                            .setInterpolation(ScoringFunctionInterpolation.CONSTANT),
                        new DistanceScoringFunction()
                            .setParameters(new DistanceScoringParameters()
                                .setBoostingDistance(5)
                                .setReferencePointParameter("Loc"))
                            .setFieldName("Location")
                            .setBoost(1.5)
                            .setInterpolation(ScoringFunctionInterpolation.LINEAR),
                        new FreshnessScoringFunction()
                            .setParameters(new FreshnessScoringParameters()
                                .setBoostingDuration(Duration.ofDays(365)))
                            .setFieldName("LastRenovationDate")
                            .setBoost(1.1)
                            .setInterpolation(ScoringFunctionInterpolation.LOGARITHMIC)
                    ))
                    .setTextWeights(new TextWeights()
                        .setWeights(weights)),
                new ScoringProfile()
                    .setName("ProfileTwo")
                    .setFunctionAggregation(ScoringFunctionAggregation.MAXIMUM)
                    .setFunctions(Collections.singletonList(
                        new TagScoringFunction()
                            .setParameters(new TagScoringParameters().setTagsParameter("MyTags"))
                            .setFieldName("Tags")
                            .setBoost(1.5)
                            .setInterpolation(ScoringFunctionInterpolation.LINEAR)
                    )),
                new ScoringProfile()
                    .setName("ProfileThree")
                    .setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                    .setFunctions(Collections.singletonList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(0)
                                .setBoostingRangeEnd(10)
                                .setShouldBoostBeyondRangeByConstant(false))
                            .setFieldName("Rating")
                            .setBoost(3.0)
                            .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)
                    )),
                new ScoringProfile()
                    .setName("ProfileFour")
                    .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                    .setFunctions(Collections.singletonList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(1)
                                .setBoostingRangeEnd(5)
                                .setShouldBoostBeyondRangeByConstant(false))
                            .setFieldName("Rating")
                            .setBoost(3.14)
                            .setInterpolation(ScoringFunctionInterpolation.CONSTANT)
                    ))
            ))
            .setDefaultScoringProfile("MyProfile")
            .setCorsOptions(new CorsOptions()
                .setAllowedOrigins("http://tempuri.org", "http://localhost:80")
                .setMaxAgeInSeconds(60L))
            .setSearchSuggesters(Collections.singletonList(new SearchSuggester()
                .setName("FancySuggester")
                .setSourceFields(Collections.singletonList("HotelName"))));
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
