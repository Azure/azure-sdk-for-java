// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
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
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.ResourceCounter;
import com.azure.search.documents.models.ScoringFunctionAggregation;
import com.azure.search.documents.models.ScoringFunctionInterpolation;
import com.azure.search.documents.models.ScoringProfile;
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.models.ServiceCounters;
import com.azure.search.documents.models.ServiceLimits;
import com.azure.search.documents.models.ServiceStatistics;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.models.Suggester;
import com.azure.search.documents.models.TagScoringFunction;
import com.azure.search.documents.models.TagScoringParameters;
import com.azure.search.documents.models.TextWeights;
import com.azure.search.documents.test.environment.setup.AzureSearchResources;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SearchServiceTestBase extends TestBase {

    private static final String DEFAULT_DNS_SUFFIX = "search.windows.net";
    private static final String DOGFOOD_DNS_SUFFIX = "search-dogfood.windows-int.net";

    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";

    // The connection string we use here, as well as table name and target index schema, use the USGS database
    // that we set up to support our code samples.
    //
    // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
    // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
    private static final String AZURE_SQL_CONN_STRING_READONLY_PLAYGROUND =
        "Server=tcp:azs-playground.database.windows.net,1433;Database=usgs;User ID=reader;Password=EdrERBt3j6mZDP;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;"; // [SuppressMessage("Microsoft.Security", "CS001:SecretInline")]

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        OBJECT_MAPPER.setDateFormat(df);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static final String HOTEL_INDEX_NAME = "hotels";

    static final String BLOB_DATASOURCE_NAME = "azs-java-live-blob";
    static final String BLOB_DATASOURCE_TEST_NAME = "azs-java-test-blob";
    static final String SQL_DATASOURCE_NAME = "azs-java-test-sql";

    private String searchServiceName;
    private String searchDnsSuffix;
    protected String endpoint;
    AzureKeyCredential searchApiKeyCredential;
    private static final boolean IS_DEBUG = false;

    private static String testEnvironment;
    private static AzureSearchResources azureSearchResources;

    @BeforeAll
    public static void beforeAll() {
        initializeAzureResources();
        if (!playbackMode()) {
            azureSearchResources.initialize();
            azureSearchResources.createResourceGroup();
        }
    }

    @AfterAll
    public static void afterAll() {
        if (IS_DEBUG) {
            azureSearchResources.deleteResourceGroup();
        }
    }

    @Override
    protected void beforeTest() {
        searchDnsSuffix = testEnvironment.equals("DOGFOOD") ? DOGFOOD_DNS_SUFFIX : DEFAULT_DNS_SUFFIX;

        if (!interceptorManager.isPlaybackMode()) {
            azureSearchResources.createService(testResourceNamer);
            searchApiKeyCredential = new AzureKeyCredential(azureSearchResources.getSearchAdminKey());
        }
        searchServiceName = azureSearchResources.getSearchServiceName();
        endpoint = String.format("https://%s.%s", searchServiceName, searchDnsSuffix);
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        azureSearchResources.deleteService();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        return getSearchServiceClientBuilderWithHttpPipelinePolicies(null);
    }

    /**
     * Provides a way to inject custom HTTP pipeline policies before the client is instantiated
     *
     * @param policies the additional HTTP pipeline policies
     * @return {@link SearchServiceClientBuilder}
     */
    SearchServiceClientBuilder getSearchServiceClientBuilderWithHttpPipelinePolicies(
        List<HttpPipelinePolicy> policies) {
        SearchServiceClientBuilder builder = new SearchServiceClientBuilder()
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
            addPolicies(builder, policies);
            return builder;
        }

        addPolicies(builder, policies);
        builder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
            .credential(searchApiKeyCredential);

        if (!liveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;

    }

    private void addPolicies(SearchServiceClientBuilder builder, List<HttpPipelinePolicy> policies) {
        if (policies != null && policies.size() > 0) {
            for (HttpPipelinePolicy policy : policies) {
                builder.addPolicy(policy);
            }
        }
    }

    Index createTestIndex() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        return new Index()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzer(AnalyzerName.EN_LUCENE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("DescriptionFr")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setAnalyzer(AnalyzerName.FR_LUCENE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description_Custom")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setSearchAnalyzer(AnalyzerName.STOP)
                    .setIndexAnalyzer(AnalyzerName.STOP)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Category")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Tags")
                    .setType(DataType.collection(DataType.EDM_STRING))
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("ParkingIncluded")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("SmokingAllowed")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("LastRenovationDate")
                    .setType(DataType.EDM_DATE_TIME_OFFSET)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Rating")
                    .setType(DataType.EDM_INT32)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Address")
                    .setType(DataType.EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("StreetAddress")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("City")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("StateProvince")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("Country")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("PostalCode")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                        )
                    ),
                new Field()
                    .setName("Location")
                    .setType(DataType.EDM_GEOGRAPHY_POINT)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Rooms")
                    .setType(DataType.collection(DataType.EDM_COMPLEX_TYPE))
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("Description")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.EN_LUCENE),
                        new Field()
                            .setName("DescriptionFr")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.FR_LUCENE),
                        new Field()
                            .setName("Type")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("BaseRate")
                            .setType(DataType.EDM_DOUBLE)
                            .setKey(Boolean.FALSE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("BedOptions")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("SleepsCount")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("SmokingAllowed")
                            .setType(DataType.EDM_BOOLEAN)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("Tags")
                            .setType(DataType.collection(DataType.EDM_STRING))
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
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

    DataSource createTestSqlDataSourceObject(DataDeletionDetectionPolicy deletionDetectionPolicy,
        DataChangeDetectionPolicy changeDetectionPolicy) {
        return DataSources.createFromAzureSql(
            SearchServiceTestBase.SQL_DATASOURCE_NAME,
            AZURE_SQL_CONN_STRING_READONLY_PLAYGROUND,
            "GeoNamesRI",
            FAKE_DESCRIPTION,
            changeDetectionPolicy,
            deletionDetectionPolicy
        );
    }

    DataSource createTestSqlDataSourceObject() {
        return createTestSqlDataSourceObject(null, null);
    }

    /**
     * create a new blob data source object
     * @return the created data source
     */
    DataSource createBlobDataSource() {
        String storageConnString = "connectionString";
        String blobContainerDatasourceName = "container";
        if (!interceptorManager.isPlaybackMode()) {

            // First, we create a storage account
            storageConnString = azureSearchResources.createStorageAccount(testResourceNamer);
            // Next, we create the blobs container
            blobContainerDatasourceName =
                azureSearchResources.createBlobContainer(storageConnString, testResourceNamer);
        }

        // create the new data source object for this storage account and container
        return DataSources.createFromAzureBlobStorage(
            BLOB_DATASOURCE_NAME,
            storageConnString,
            blobContainerDatasourceName,
            "/",
            "real live blob",
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue")
        );
    }

    private static void initializeAzureResources() {
        String appId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String azureDomainId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String secret = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        testEnvironment = Configuration.getGlobalConfiguration().get("AZURE_TEST_ENVIRONMENT");
        testEnvironment = (testEnvironment == null) ? "AZURE" : testEnvironment.toUpperCase(Locale.US);

        AzureEnvironment environment = testEnvironment.equals("DOGFOOD") ? getDogfoodEnvironment() : AzureEnvironment.AZURE;

        ApplicationTokenCredentials applicationTokenCredentials =
            new ApplicationTokenCredentials(appId, azureDomainId, secret, environment);

        azureSearchResources = new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_WEST2);
    }

    private static AzureEnvironment getDogfoodEnvironment() {
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("portalUrl", "http://df.onecloud.azure-test.net");
        configuration.put("managementEndpointUrl", "https://management.core.windows.net/");
        configuration.put("resourceManagerEndpointUrl", "https://api-dogfood.resources.windows-int.net/");
        configuration.put("activeDirectoryEndpointUrl", "https://login.windows-ppe.net/");
        configuration.put("activeDirectoryResourceId", "https://management.core.windows.net/");
        configuration.put("activeDirectoryGraphResourceId", "https://graph.ppe.windows.net/");
        configuration.put("activeDirectoryGraphApiVersion", "2013-04-05");
        return new AzureEnvironment(configuration);
    }

    protected SearchIndexClientBuilder getSearchIndexClientBuilder(String indexName) {
        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(String.format("https://%s.%s", searchServiceName, searchDnsSuffix))
            .indexName(indexName);

        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(interceptorManager.getPlaybackClient());
        }

        builder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
            .credential(searchApiKeyCredential);

        if (!liveMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected void waitForIndexing() {
        // Wait 2 seconds to allow index request to finish.
        sleepIfRunningAgainstService(2000);
    }

    /**
     * If the document schema is known, user can convert the properties to a specific object type
     *
     * @param cls Class type of the document object to convert to
     * @param <T> type
     * @return an object of the request type
     */
    static <T> T convertToType(Object document, Class<T> cls) {
        return OBJECT_MAPPER.convertValue(document, cls);
    }

    void addFieldToIndex(Index index, Field field) {
        List<Field> fields = new ArrayList<>(index.getFields());
        fields.add(field);

        index.setFields(fields);
    }

    /**
     * Constructs a request options object with client request Id.
     * @return a RequestOptions object with ClientRequestId.
     */
    protected RequestOptions generateRequestOptions() {
        return new RequestOptions().setXMsClientRequestId(UUID.randomUUID());
    }

    void assertHttpResponseException(Runnable exceptionThrower, HttpResponseStatus expectedResponseStatus,
        String expectedMessage) {
        try {
            exceptionThrower.run();
            fail();

        } catch (Throwable ex) {
            verifyHttpResponseError(ex, expectedResponseStatus, expectedMessage);
        }
    }

    void assertHttpResponseExceptionAsync(Publisher<?> exceptionThrower) {
        StepVerifier.create(exceptionThrower)
            .verifyErrorSatisfies(error -> verifyHttpResponseError(error, HttpResponseStatus.BAD_REQUEST,
                "Invalid expression: Could not find a property named 'ThisFieldDoesNotExist' on type 'search.document'."));
    }

    private void verifyHttpResponseError(
        Throwable ex, HttpResponseStatus expectedResponseStatus, String expectedMessage) {

        assertEquals(SearchErrorException.class, ex.getClass());

        if (expectedResponseStatus != null) {
            assertEquals(
                expectedResponseStatus.code(),
                ((HttpResponseException) ex).getResponse().getStatusCode());
        }

        if (expectedMessage != null) {
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    ServiceStatistics getExpectedServiceStatistics() {
        ServiceCounters serviceCounters = new ServiceCounters()
            .setDocumentCounter(new ResourceCounter().setUsage(0).setQuota(null))
            .setIndexCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setIndexerCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setDataSourceCounter(new ResourceCounter().setUsage(0).setQuota(3L))
            .setStorageSizeCounter(new ResourceCounter().setUsage(0).setQuota(52428800L))
            .setSynonymMapCounter(new ResourceCounter().setUsage(0).setQuota(3L));

        ServiceLimits serviceLimits = new ServiceLimits()
            .setMaxFieldsPerIndex(1000)
            .setMaxFieldNestingDepthPerIndex(10)
            .setMaxComplexCollectionFieldsPerIndex(40)
            .setMaxComplexObjectsInCollectionsPerDocument(3000);

        return new ServiceStatistics()
            .setCounters(serviceCounters)
            .setLimits(serviceLimits);
    }

    static boolean liveMode() {
        return setupTestMode() == TestMode.LIVE;
    }

    static boolean playbackMode() {
        return setupTestMode() == TestMode.PLAYBACK;
    }

    static TestMode setupTestMode() {
        String testMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE);

        if (testMode != null) {
            try {
                return TestMode.valueOf(testMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignore) {
                return TestMode.PLAYBACK;
            }
        }

        return TestMode.PLAYBACK;
    }
}
