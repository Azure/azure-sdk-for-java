// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerDataSources;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.DataChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;
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
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;
import com.azure.search.documents.indexes.models.TextWeights;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_NAME;
import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.SQL_DATASOURCE_NAME;

/**
 * Abstract base class for all Search API tests
 */
public abstract class SearchTestBase extends TestBase {
    private static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("SEARCH_SERVICE_ENDPOINT", "https://playback.search.windows.net");

    protected static final String API_KEY = Configuration.getGlobalConfiguration()
        .get("SEARCH_SERVICE_API_KEY", "apiKey");

    // The connection string we use here, as well as table name and target index schema, use the USGS database
    // that we set up to support our code samples.
    //
    // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
    // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
    private static final String AZURE_SQL_CONN_STRING_READONLY_PLAYGROUND =
        "Server=tcp:azs-playground.database.windows.net,1433;Database=usgs;User ID=reader;Password=EdrERBt3j6mZDP;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;"; // [SuppressMessage("Microsoft.Security", "CS001:SecretInline")]

    private static final String FAKE_DESCRIPTION = "Some data source";

    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";
    static final String HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION = "HotelsDataArrayWithoutFr.json";

    protected String createHotelIndex() {
        return setupIndexFromJsonFile(HOTELS_TESTS_INDEX_DATA_JSON);
    }

    protected String setupIndexFromJsonFile(String jsonFile) {
        try {
            Reader indexData = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream(jsonFile)));

            return setupIndex(new ObjectMapper().readValue(indexData, SearchIndex.class));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected String setupIndex(SearchIndex index) {
        index.setName(testResourceNamer.randomName(index.getName(), 64));
        getSearchIndexClientBuilder().buildClient().createOrUpdateIndex(index);

        return index.getName();
    }

    protected SearchIndexClientBuilder getSearchIndexClientBuilder(HttpPipelinePolicy... policies) {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT);
        builder.credential(new AzureKeyCredential(API_KEY));
        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            addPolicies(builder, policies);
            return builder;
        }

        addPolicies(builder, policies);

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;

    }

    protected SearchIndexerClientBuilder getSearchIndexerClientBuilder(HttpPipelinePolicy... policies) {
        SearchIndexerClientBuilder builder = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT);
        builder.credential(new AzureKeyCredential(API_KEY));
        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            addPolicies(builder, policies);
            return builder;
        }

        addPolicies(builder, policies);

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;

    }

    private static void addPolicies(SearchIndexClientBuilder builder, HttpPipelinePolicy... policies) {
        if (policies == null) {
            return;
        }

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
    }

    private static void addPolicies(SearchIndexerClientBuilder builder, HttpPipelinePolicy... policies) {
        if (policies == null) {
            return;
        }

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
    }

    protected SearchClientBuilder getSearchIndexClientBuilder(String indexName) {
        SearchClientBuilder builder = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .indexName(indexName);

        builder.credential(new AzureKeyCredential(API_KEY));
        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(interceptorManager.getPlaybackClient());
        }

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SearchIndex createTestIndex() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        return new SearchIndex()
            .setName(randomIndexName(HOTEL_INDEX_NAME))
            .setFields(Arrays.asList(
                new SearchField()
                    .setName("HotelId")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(Boolean.TRUE)
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
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Description")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("DescriptionFr")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Description_Custom")
                    .setType(SearchFieldDataType.STRING)
                    .setSearchable(Boolean.TRUE)
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
                    .setHidden(Boolean.FALSE),
                new SearchField()
                    .setName("Rooms")
                    .setType(SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                    .setFields(Arrays.asList(
                        new SearchField()
                            .setName("Description")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                        new SearchField()
                            .setName("DescriptionFr")
                            .setType(SearchFieldDataType.STRING)
                            .setSearchable(Boolean.TRUE)
                            .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                            .setHidden(Boolean.FALSE),
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
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                        )
                    ),
                new SearchField()
                    .setName("TotalGuests")
                    .setType(SearchFieldDataType.INT64)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE),
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
            .setSuggesters(Collections.singletonList(new SearchSuggester()
                .setName("FancySuggester")
                .setSourceFields(Collections.singletonList("HotelName"))));
    }

    protected SearchIndexerDataSourceConnection createTestSqlDataSourceObject() {
        return createTestSqlDataSourceObject(null, null);
    }

    protected SearchIndexerDataSourceConnection createTestSqlDataSourceObject(
        DataDeletionDetectionPolicy dataDeletionDetectionPolicy, DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        return SearchIndexerDataSources.createFromAzureSql(testResourceNamer.randomName(SQL_DATASOURCE_NAME, 32),
            AZURE_SQL_CONN_STRING_READONLY_PLAYGROUND, "GeoNamesRI", FAKE_DESCRIPTION, dataChangeDetectionPolicy,
            dataDeletionDetectionPolicy);
    }

    protected SearchIndexerDataSourceConnection createBlobDataSource() {
        String storageConnectionString = Configuration.getGlobalConfiguration()
            .get("SEARCH_STORAGE_CONNECTION_STRING", "connectionString");
        String blobContainerName = Configuration.getGlobalConfiguration()
            .get("SEARCH_STORAGE_CONTAINER_NAME", "container");

        // create the new data source object for this storage account and container
        return SearchIndexerDataSources.createFromAzureBlobStorage(testResourceNamer.randomName(BLOB_DATASOURCE_NAME, 32),
            storageConnectionString, blobContainerName, "/", "real live blob",
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue"));
    }

    protected String randomIndexName(String indexNameBase) {
        return testResourceNamer.randomName(indexNameBase, 64);
    }
}
