// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.agents.SearchKnowledgeAgentClientBuilder;
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
import com.azure.search.documents.indexes.models.TagScoringFunction;
import com.azure.search.documents.indexes.models.TagScoringParameters;
import com.azure.search.documents.indexes.models.TextWeights;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiConsumer;

import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.ISO8601_FORMAT;
import static com.azure.search.documents.TestHelpers.SQL_DATASOURCE_NAME;
import static com.azure.search.documents.indexes.DataSourceTests.FAKE_AZURE_SQL_CONNECTION_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract base class for all Search API tests
 */
public abstract class SearchTestBase extends TestProxyTestBase {
    protected static final String HOTELS_TESTS_INDEX_DATA_JSON = "HotelsTestsIndexData.json";
    private boolean sanitizersRemoved = false;

    protected static final String SEARCH_ENDPOINT
        = Configuration.getGlobalConfiguration().get("SEARCH_SERVICE_ENDPOINT", "https://playback.search.windows.net");

    protected static final String OPENAI_ENDPOINT;
    static {
        String openAiEndpoint = Configuration.getGlobalConfiguration()
            .get("SEARCH_OPENAI_ENDPOINT", "https://your-endpoint.openai.azure.com");
        // Service trims trailing '/', need to do that here so tests don't fail.
        if (openAiEndpoint.endsWith("/")) {
            openAiEndpoint = openAiEndpoint.substring(0, openAiEndpoint.length() - 1);
        }

        OPENAI_ENDPOINT = openAiEndpoint;
    }

    protected static final String OPENAI_DEPLOYMENT_NAME
        = Configuration.getGlobalConfiguration().get("SEARCH_OPENAI_DEPLOYMENT_NAME", "search-knowledge-agent-model");
    protected static final String OPENAI_MODEL_NAME
        = Configuration.getGlobalConfiguration().get("SEARCH_OPENAI_MODEL_NAME", "gpt-4.1-nano");

    protected static final String STORAGE_ACCOUNT_NAME
        = Configuration.getGlobalConfiguration().get("SEARCH_STORAGE_ACCOUNT_NAME", "storageaccount");
    protected static final String BLOB_CONTAINER_NAME
        = Configuration.getGlobalConfiguration().get("SEARCH_STORAGE_CONTAINER_NAME", "searchcontainer");

    protected static final String USER_ASSIGNED_IDENTITY = Configuration.getGlobalConfiguration()
        .get("SEARCH_USER_ASSIGNED_IDENTITY", "/subscriptions/subscription-id/resourceGroups/resource-group-name/"
            + "providers/Microsoft.ManagedIdentity/userAssignedIdentities/user-assigned-managed-identity-name");
    protected static final String SUBSCRIPTION_ID
        = Configuration.getGlobalConfiguration().get("SEARCH_SUBSCRIPTION_ID", "subscription-id");
    protected static final String RESOURCE_GROUP
        = Configuration.getGlobalConfiguration().get("SEARCH_RESOURCE_GROUP", "resource-group");

    protected static final TestMode TEST_MODE = initializeTestMode();

    private static final String FAKE_DESCRIPTION = "Some data source";

    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    // This has to be used in all test modes as this is more retry counts than the standard policy.
    // Change the delay based on the mode.
    static final RetryPolicy SERVICE_THROTTLE_SAFE_RETRY_POLICY = new RetryPolicy(
        new FixedDelay(4, TEST_MODE == TestMode.PLAYBACK ? Duration.ofMillis(1) : Duration.ofSeconds(60)));
    static final RetryOptions SERVICE_THROTTLE_SAFE_RETRY_OPTIONS = new RetryOptions(
        new FixedDelayOptions(4, TEST_MODE == TestMode.PLAYBACK ? Duration.ofMillis(1) : Duration.ofSeconds(60)));

    protected String createHotelIndex() {
        return setupIndexFromJsonFile();
    }

    protected String setupIndexFromJsonFile() {
        try (JsonReader jsonReader
            = JsonProviders.createReader(TestHelpers.loadResource(SearchTestBase.HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);
            String testIndexName = testResourceNamer.randomName(baseIndex.getName(), 64);

            return setupIndex(TestHelpers.createTestIndex(testIndexName, baseIndex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected String setupIndex(SearchIndex index) {
        getSearchIndexClientBuilder(true).buildClient().createOrUpdateIndex(index);

        return index.getName();
    }

    protected SearchIndexClientBuilder getSearchIndexClientBuilder(boolean isSync) {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(getTestTokenCredential())
            .httpClient(getHttpClient(interceptorManager, isSync))
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY);

        // Disable `("$..token")` and `name` sanitizer
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers("AZSDK3431", "AZSDK3493", "AZSDK3430");
            sanitizersRemoved = true;
        }

        if (interceptorManager.isPlaybackMode()) {
            addPolicies(builder);
            return builder;
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SearchIndexerClientBuilder getSearchIndexerClientBuilder(boolean isSync, HttpPipelinePolicy... policies) {
        SearchIndexerClientBuilder builder = new SearchIndexerClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(getTestTokenCredential())
            .httpClient(getHttpClient(interceptorManager, isSync))
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY);

        addPolicies(builder, policies);
        // Disable `("$..token")` and `name` sanitizer
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers("AZSDK3431", "AZSDK3493", "AZSDK3430");
            sanitizersRemoved = true;
        }

        if (interceptorManager.isPlaybackMode()) {
            return builder;
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SearchKnowledgeAgentClientBuilder getSearchKnowledgeAgentClientBuilder(boolean isSync) {
        SearchKnowledgeAgentClientBuilder builder = new SearchKnowledgeAgentClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(getTestTokenCredential())
            .httpClient(getHttpClient(interceptorManager, isSync))
            .retryOptions(SERVICE_THROTTLE_SAFE_RETRY_OPTIONS);

        // Disable `("$..token")` and `name` sanitizer
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers("AZSDK3431", "AZSDK3493", "AZSDK3430");
            sanitizersRemoved = true;
        }

        if (interceptorManager.isPlaybackMode()) {
            addPolicies(builder);
            return builder;
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    private static void addPolicies(HttpTrait<?> builder, HttpPipelinePolicy... policies) {
        if (policies == null) {
            return;
        }

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }
    }

    protected SearchClientBuilder getSearchClientBuilder(String indexName, boolean isSync) {
        return getSearchClientBuilderHelper(indexName, isSync);

    }

    /**
     * Retrieve the appropriate TokenCredential based on the test mode.
     *
     * @return The appropriate token credential
     */
    public static TokenCredential getTestTokenCredential() {
        return TestHelpers.getTestTokenCredential();
    }

    private SearchClientBuilder getSearchClientBuilderHelper(String indexName, boolean isSync) {
        SearchClientBuilder builder = new SearchClientBuilder().endpoint(SEARCH_ENDPOINT)
            .indexName(indexName)
            .credential(getTestTokenCredential())
            .httpClient(getHttpClient(interceptorManager, isSync))
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY);

        // Disable `("$..token")` and `name` sanitizer
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers("AZSDK3431", "AZSDK3493", "AZSDK3430");
            sanitizersRemoved = true;
        }

        if (interceptorManager.isPlaybackMode()) {
            return builder;
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    private static HttpClient getHttpClient(InterceptorManager interceptorManager, boolean isSync) {
        HttpClient httpClient
            = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : HttpClient.createDefault();

        if (!isSync) {
            return new AssertingHttpClientBuilder(httpClient).assertAsync()
                .skipRequest((ignored1, ignored2) -> false)
                .build();
        } else {
            return new AssertingHttpClientBuilder(httpClient).assertSync()
                .skipRequest((ignored1, ignored2) -> false)
                .build();
        }
    }

    protected SearchIndex createTestIndex(String indexName) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        String searchIndexName = indexName == null ? randomIndexName(HOTEL_INDEX_NAME) : indexName;
        return new SearchIndex(searchIndexName,
            Arrays.asList(
                new SearchField("HotelId", SearchFieldDataType.STRING).setKey(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Description", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                    .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField("DescriptionFr", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                    .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Description_Custom", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                    .setSearchAnalyzerName(LexicalAnalyzerName.STOP)
                    .setIndexAnalyzerName(LexicalAnalyzerName.STOP)
                    .setHidden(Boolean.FALSE),
                new SearchField("Category", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("ParkingIncluded", SearchFieldDataType.BOOLEAN).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("LastRenovationDate", SearchFieldDataType.DATE_TIME_OFFSET).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Rating", SearchFieldDataType.INT32).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Address", SearchFieldDataType.COMPLEX).setFields(
                    new SearchField("StreetAddress", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("City", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("StateProvince", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Country", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("PostalCode", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setSortable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)),
                new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setHidden(Boolean.FALSE),
                new SearchField("Rooms", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX)).setFields(
                    new SearchField("Description", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                    new SearchField("DescriptionFr", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Type", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("BaseRate", SearchFieldDataType.DOUBLE).setKey(Boolean.FALSE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("BedOptions", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("SleepsCount", SearchFieldDataType.INT32).setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN).setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE),
                    new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                        .setSearchable(Boolean.TRUE)
                        .setFilterable(Boolean.TRUE)
                        .setFacetable(Boolean.TRUE)
                        .setHidden(Boolean.FALSE)),
                new SearchField("TotalGuests", SearchFieldDataType.INT64).setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE),
                new SearchField("ProfitMargin", SearchFieldDataType.DOUBLE)))
                    .setScoringProfiles(
                        new ScoringProfile("MyProfile").setFunctionAggregation(ScoringFunctionAggregation.AVERAGE)
                            .setFunctions(new MagnitudeScoringFunction("Rating", 2.0,
                                new MagnitudeScoringParameters(1, 4).setShouldBoostBeyondRangeByConstant(true))
                                    .setInterpolation(ScoringFunctionInterpolation.CONSTANT),
                                new DistanceScoringFunction("Location", 1.5, new DistanceScoringParameters("Loc", 5))
                                    .setInterpolation(ScoringFunctionInterpolation.LINEAR),
                                new FreshnessScoringFunction("LastRenovationDate", 1.1,
                                    new FreshnessScoringParameters(Duration.ofDays(365)))
                                        .setInterpolation(ScoringFunctionInterpolation.LOGARITHMIC))
                            .setTextWeights(new TextWeights(weights)),
                        new ScoringProfile("ProfileTwo").setFunctionAggregation(ScoringFunctionAggregation.MAXIMUM)
                            .setFunctions(new TagScoringFunction("Tags", 1.5, new TagScoringParameters("MyTags"))
                                .setInterpolation(ScoringFunctionInterpolation.LINEAR)),
                        new ScoringProfile("ProfileThree").setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                            .setFunctions(new MagnitudeScoringFunction("Rating", 3.0,
                                new MagnitudeScoringParameters(0, 10).setShouldBoostBeyondRangeByConstant(false))
                                    .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)),
                        new ScoringProfile("ProfileFour")
                            .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                            .setFunctions(new MagnitudeScoringFunction("Rating", 3.25,
                                new MagnitudeScoringParameters(1, 5).setShouldBoostBeyondRangeByConstant(false))
                                    .setInterpolation(ScoringFunctionInterpolation.CONSTANT)))
                    .setDefaultScoringProfile("MyProfile")
                    .setCorsOptions(new CorsOptions(Arrays.asList("http://tempuri.org", "http://localhost:80"))
                        .setMaxAgeInSeconds(60L))
                    .setSuggesters(new SearchSuggester("FancySuggester", Collections.singletonList("HotelName")));
    }

    protected SearchIndexerDataSourceConnection createTestSqlDataSourceObject() {
        return createTestSqlDataSourceObject(null, null, null);
    }

    protected SearchIndexerDataSourceConnection createTestSqlDataSourceObject(
        DataDeletionDetectionPolicy dataDeletionDetectionPolicy, DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        return createTestSqlDataSourceObject(testResourceNamer.randomName(SQL_DATASOURCE_NAME, 32),
            dataDeletionDetectionPolicy, dataChangeDetectionPolicy);
    }

    protected SearchIndexerDataSourceConnection createTestSqlDataSourceObject(String name,
        DataDeletionDetectionPolicy dataDeletionDetectionPolicy, DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        if (name == null) {
            name = testResourceNamer.randomName(SQL_DATASOURCE_NAME, 32);
        }

        return SearchIndexerDataSources.createFromAzureSql(name, FAKE_AZURE_SQL_CONNECTION_STRING, "GeoNamesRI",
            FAKE_DESCRIPTION, dataChangeDetectionPolicy, dataDeletionDetectionPolicy);
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

    protected void validateETagUpdate(String original, String updated) {
        assertNotNull(original);
        assertNotNull(updated);
        assertNotEquals(original, updated);
    }

    protected <T> void compareMaps(Map<String, T> expectedMap, Map<String, T> actualMap,
        BiConsumer<T, T> comparisonFunction) {
        compareMaps(expectedMap, actualMap, comparisonFunction, true);
    }

    protected <T> void compareMaps(Map<String, T> expectedMap, Map<String, T> actualMap,
        BiConsumer<T, T> comparisonFunction, boolean checkSize) {
        if (checkSize) {
            assertEquals(expectedMap.size(), actualMap.size());
        }

        actualMap.forEach((key, actual) -> {
            T expected = expectedMap.get(key);
            assertNotNull(expected, "Actual map contained an entry that doesn't exist in the expected map: " + key);

            comparisonFunction.accept(expected, actual);
        });
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    protected static Date parseDate(String dateString) {
        DateFormat dateFormat = new SimpleDateFormat(ISO8601_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
}
