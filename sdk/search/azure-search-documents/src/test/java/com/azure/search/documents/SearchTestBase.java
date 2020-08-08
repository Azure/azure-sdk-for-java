// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
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
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;
import com.azure.search.documents.indexes.models.TextWeights;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.Exceptions;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_NAME;
import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.SQL_DATASOURCE_NAME;

/**
 * Abstract base class for all Search API tests
 */
public abstract class SearchTestBase extends TestBase {
    protected static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("SEARCH_SERVICE_ENDPOINT", "https://playback.search.windows.net");

    protected static final String API_KEY = Configuration.getGlobalConfiguration()
        .get("SEARCH_SERVICE_API_KEY", "apiKey");

    protected static final TestMode TEST_MODE = initializeTestMode();

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
        try {
            return setupIndexFromJsonFile(HOTELS_TESTS_INDEX_DATA_JSON);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    protected String setupIndexFromJsonFile(String jsonFile) {
        Reader indexData = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
            .getResourceAsStream(jsonFile)));
        try {
            return setupIndex(new ObjectMapper().readValue(indexData, SearchIndex.class));
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    protected String setupIndex(SearchIndex index) {
        try {
            Field searchIndexName = index.getClass().getDeclaredField("name");
            searchIndexName.setAccessible(true);

            searchIndexName.set(index, testResourceNamer.randomName(index.getName(), 64));
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
        getSearchIndexClientBuilder().buildClient().createOrUpdateIndex(index);

        return index.getName();
    }

    protected SearchIndexClientBuilder getSearchIndexClientBuilder() {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT);
        builder.credential(new AzureKeyCredential(API_KEY));
        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            addPolicies(builder);
            return builder;
        }

        //builder.httpClient(new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).build());

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
        //builder.httpClient(new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).build());
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

    protected SearchClientBuilder getSearchClientBuilder(String indexName) {
        SearchClientBuilder builder = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .indexName(indexName);

        builder.credential(new AzureKeyCredential(API_KEY));
        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(interceptorManager.getPlaybackClient());
        }
        //builder.httpClient(new NettyAsyncHttpClientBuilder().proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))).build());
        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SearchIndex createTestIndex(String indexName) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        String searchIndexName = indexName == null ? randomIndexName(HOTEL_INDEX_NAME) : indexName;
        return new SearchIndex(searchIndexName, Arrays.asList(
            new SearchField("HotelId", SearchFieldDataType.STRING)
                .setKey(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("HotelName", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("Description", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                .setHidden(Boolean.FALSE),
            new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
                .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                .setHidden(Boolean.FALSE),
            new SearchField("Description_Custom", SearchFieldDataType.STRING)
                .setSearchable(Boolean.TRUE)
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
                .setFields(new SearchField("StreetAddress", SearchFieldDataType.STRING)
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
                        .setHidden(Boolean.FALSE)),
            new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setHidden(Boolean.FALSE),
            new SearchField("Rooms", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                .setFields(new SearchField("Description", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                    new SearchField("DescriptionFr", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Type", SearchFieldDataType.STRING)
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("BaseRate", SearchFieldDataType.DOUBLE)
                        .setKey(Boolean.FALSE)
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
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)),
            new SearchField("TotalGuests", SearchFieldDataType.INT64)
                .setFilterable(Boolean.TRUE)
                .setSortable(Boolean.TRUE)
                .setFacetable(Boolean.TRUE),
            new SearchField("ProfitMargin", SearchFieldDataType.DOUBLE)
        )).setScoringProfiles(new ScoringProfile("MyProfile")
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
                .setFunctions(new TagScoringFunction("Tags", 1.5,
                    new TagScoringParameters("MyTags"))
                    .setInterpolation(ScoringFunctionInterpolation.LINEAR)),
            new ScoringProfile("ProfileThree")
                .setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                .setFunctions(new MagnitudeScoringFunction("Rating", 3.0,
                    new MagnitudeScoringParameters(0, 10)
                        .setShouldBoostBeyondRangeByConstant(false))
                    .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)),
            new ScoringProfile("ProfileFour")
                .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                .setFunctions(new MagnitudeScoringFunction("Rating", 3.14,
                    new MagnitudeScoringParameters(1, 5)
                        .setShouldBoostBeyondRangeByConstant(false))
                    .setInterpolation(ScoringFunctionInterpolation.CONSTANT))
        ).setDefaultScoringProfile("MyProfile")
            .setCorsOptions(new CorsOptions(Arrays.asList("http://tempuri.org", "http://localhost:80"))
                .setMaxAgeInSeconds(60L))
            .setSuggesters(new SearchSuggester("FancySuggester", Collections.singletonList("HotelName")));
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

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(SearchTestBase.class);
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }
}
