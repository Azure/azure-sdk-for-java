// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.CorsOptions;
import com.azure.search.documents.models.DataChangeDetectionPolicy;
import com.azure.search.documents.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.DistanceScoringFunction;
import com.azure.search.documents.models.DistanceScoringParameters;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.FreshnessScoringFunction;
import com.azure.search.documents.models.FreshnessScoringParameters;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.MagnitudeScoringFunction;
import com.azure.search.documents.models.MagnitudeScoringParameters;
import com.azure.search.documents.models.ScoringFunctionAggregation;
import com.azure.search.documents.models.ScoringFunctionInterpolation;
import com.azure.search.documents.models.ScoringProfile;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.models.Suggester;
import com.azure.search.documents.models.TagScoringFunction;
import com.azure.search.documents.models.TagScoringParameters;
import com.azure.search.documents.models.TextWeights;
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

            return setupIndex(new ObjectMapper().readValue(indexData, Index.class));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected String setupIndex(Index index) {
        index.setName(testResourceNamer.randomName(index.getName(), 64));
        getSearchServiceClientBuilder().buildClient().createOrUpdateIndex(index);

        return index.getName();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder(HttpPipelinePolicy... policies) {
        SearchServiceClientBuilder builder = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            addPolicies(builder, policies);
            return builder;
        }

        addPolicies(builder, policies);
        builder.credential(new AzureKeyCredential(API_KEY))
            .retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;

    }

    private static void addPolicies(SearchServiceClientBuilder builder, HttpPipelinePolicy... policies) {
        if (policies == null) {
            return;
        }

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
    }

    protected SearchIndexClientBuilder getSearchIndexClientBuilder(String indexName) {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .indexName(indexName);

        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(interceptorManager.getPlaybackClient());
        }

        builder.credential(new AzureKeyCredential(API_KEY))
            .retryPolicy(new RetryPolicy(new ExponentialBackoff(3, Duration.ofSeconds(10), Duration.ofSeconds(30))));

        if (!interceptorManager.isLiveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected Index createTestIndex() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        return new Index()
            .setName(randomIndexName(HOTEL_INDEX_NAME))
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Description")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzer(AnalyzerName.EN_LUCENE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("DescriptionFr")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzer(AnalyzerName.FR_LUCENE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Description_Custom")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setSearchAnalyzer(AnalyzerName.STOP)
                    .setIndexAnalyzer(AnalyzerName.STOP)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Category")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Tags")
                    .setType(DataType.collection(DataType.EDM_STRING))
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("ParkingIncluded")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("SmokingAllowed")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("LastRenovationDate")
                    .setType(DataType.EDM_DATE_TIME_OFFSET)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Rating")
                    .setType(DataType.EDM_INT32)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Address")
                    .setType(DataType.EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("StreetAddress")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("City")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("StateProvince")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("Country")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("PostalCode")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                        )
                    ),
                new Field()
                    .setName("Location")
                    .setType(DataType.EDM_GEOGRAPHY_POINT)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new Field()
                    .setName("Rooms")
                    .setType(DataType.collection(DataType.EDM_COMPLEX_TYPE))
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("Description")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.EN_LUCENE),
                        new Field()
                            .setName("DescriptionFr")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.FR_LUCENE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("Type")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("BaseRate")
                            .setType(DataType.EDM_DOUBLE)
                            .setKey(Boolean.FALSE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("BedOptions")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("SleepsCount")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("SmokingAllowed")
                            .setType(DataType.EDM_BOOLEAN)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE),
                        new Field()
                            .setName("Tags")
                            .setType(DataType.collection(DataType.EDM_STRING))
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setHidden(Boolean.FALSE)
                        )
                    ),
                new Field()
                    .setName("TotalGuests")
                    .setType(DataType.EDM_INT64)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE),
                new Field()
                    .setName("ProfitMargin")
                    .setType(DataType.EDM_DOUBLE)
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
            .setSuggesters(Collections.singletonList(new Suggester()
                .setName("FancySuggester")
                .setSourceFields(Collections.singletonList("HotelName"))));
    }

    protected DataSource createTestSqlDataSourceObject() {
        return createTestSqlDataSourceObject(null, null);
    }

    protected DataSource createTestSqlDataSourceObject(DataDeletionDetectionPolicy dataDeletionDetectionPolicy,
        DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        return DataSources.createFromAzureSql(testResourceNamer.randomName(SQL_DATASOURCE_NAME, 32),
            AZURE_SQL_CONN_STRING_READONLY_PLAYGROUND, "GeoNamesRI", FAKE_DESCRIPTION, dataChangeDetectionPolicy,
            dataDeletionDetectionPolicy);
    }

    protected DataSource createBlobDataSource() {
        String storageConnectionString = Configuration.getGlobalConfiguration()
            .get("SEARCH_STORAGE_CONNECTION_STRING", "connectionString");
        String blobContainerName = Configuration.getGlobalConfiguration()
            .get("SEARCH_STORAGE_CONTAINER_NAME", "container");

        // create the new data source object for this storage account and container
        return DataSources.createFromAzureBlobStorage(testResourceNamer.randomName(BLOB_DATASOURCE_NAME, 32),
            storageConnectionString, blobContainerName, "/", "real live blob",
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue"));
    }

    protected String randomIndexName(String indexNameBase) {
        return testResourceNamer.randomName(indexNameBase, 64);
    }
}
