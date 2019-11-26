// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.implementation.serializer.jsonwrapper.JsonWrapper;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.search.common.DataSources;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataType;
import com.azure.search.models.DistanceScoringFunction;
import com.azure.search.models.DistanceScoringParameters;
import com.azure.search.models.Field;
import com.azure.search.models.FreshnessScoringFunction;
import com.azure.search.models.FreshnessScoringParameters;
import com.azure.search.models.Index;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ResourceCounter;
import com.azure.search.models.ScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.ServiceCounters;
import com.azure.search.models.ServiceLimits;
import com.azure.search.models.ServiceStatistics;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.Suggester;
import com.azure.search.models.TagScoringFunction;
import com.azure.search.models.TagScoringParameters;
import com.azure.search.models.TextWeights;
import com.azure.search.test.environment.models.ModelComparer;
import com.azure.search.test.environment.setup.AzureSearchResources;
import com.azure.search.test.environment.setup.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static com.azure.search.DataSourceTestBase.azureSql;
import java.util.Objects;
import java.util.UUID;

public abstract class SearchServiceTestBase extends TestBase {

    private static final String DEFAULT_DNS_SUFFIX = "search.windows.net";
    private static final String DOGFOOD_DNS_SUFFIX = "search-dogfood.windows-int.net";

    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String RESOURCE_NAME_PREFIX = "azs-";
    // The connection string we use here, as well as table name and target index schema, use the USGS database
    // that we set up to support our code samples.
    //
    // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
    // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
    private static final String SQL_CONN_STRING_FIXTURE = "Server=tcp:xxx.database.windows.net,1433;Database=xxx;User ID=reader;Password=xxx;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;";

    protected static final String HOTEL_INDEX_NAME = "hotels";

    public static final String AZURE_SQL_CONN_STRING_READONLY =
        "Server=tcp:azs-playground.database.windows.net,1433;Database=usgs;User ID=reader;Password=EdrERBt3j6mZDP;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;"; // [SuppressMessage("Microsoft.Security", "CS001:SecretInline")]

    public static final String SQL_DATASOURCE_NAME = "azs-java-test-sql";
    public static final String BLOB_DATASOURCE_NAME = "azs-java-live-blob";
    public static final String BLOB_DATASOURCE_TEST_NAME = "azs-java-test-blob";
    public static final String COSMOS_DATASOURCE_NAME = "azs-java-live-blob";

    private String searchServiceName;
    private String searchDnsSuffix;
    protected String endpoint;
    protected ApiKeyCredentials apiKeyCredentials;
    protected SearchIndexService searchServiceHotelsIndex;

    private static String testEnvironment;
    private static AzureSearchResources azureSearchResources;

    private JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

    @Override
    public String getTestName() {
        return testName.getMethodName();
    }

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() {
        initializeAzureResources();
    }

    @AfterClass
    public static void afterClass() {
        azureSearchResources.deleteResourceGroup();
    }

    @Override
    protected void beforeTest() {
        searchDnsSuffix = testEnvironment.equals("DOGFOOD") ? DOGFOOD_DNS_SUFFIX : DEFAULT_DNS_SUFFIX;
        if (!interceptorManager.isPlaybackMode()) {
            azureSearchResources.initialize();
            azureSearchResources.createResourceGroup();
            azureSearchResources.createService();

            searchServiceName = azureSearchResources.getSearchServiceName();
            apiKeyCredentials = new ApiKeyCredentials(azureSearchResources.getSearchAdminKey());
        }
        endpoint = String.format("https://%s.%s", searchServiceName, searchDnsSuffix);
        jsonApi.configureTimezone();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        azureSearchResources.deleteService();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchServiceClientBuilder()
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    protected Index createTestIndex() {
        Map<String, Double> weights = new HashMap<String, Double>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        return new Index()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.FALSE)
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
                    .setFacetable(Boolean.FALSE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.FALSE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzer(AnalyzerName.EN_LUCENE.toString())
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("DescriptionFr")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzer(AnalyzerName.FR_LUCENE.toString())
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description_Custom")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setSearchAnalyzer(AnalyzerName.STOP.toString())
                    .setIndexAnalyzer(AnalyzerName.STOP.toString())
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
                    .setType(DataType.COLLECTION_EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.FALSE)
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
                    .setFacetable(Boolean.FALSE)
                    .setRetrievable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Rooms")
                    .setType(DataType.COLLECTION_EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("Description")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.EN_LUCENE.toString()),
                        new Field()
                            .setName("DescriptionFr")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.FR_LUCENE.toString()),
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
                            .setSearchable(Boolean.FALSE)
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
                            .setType(DataType.COLLECTION_EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.FALSE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                        )
                    ),
                new Field()
                    .setName("TotalGuests")
                    .setType(DataType.EDM_INT64)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.FALSE
                    ),
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
            .setSuggesters(Arrays.asList(new Suggester()
                .setName("FancySuggester")
                .setSourceFields(Arrays.asList("HotelName"))));
    }

    protected DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureBlobStorage(
            BLOB_DATASOURCE_NAME,
            "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;",
            "fakecontainer",
            "/fakefolder/",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestSqlDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy, DataChangeDetectionPolicy changeDetectionPolicy) {
        return azureSql(
            SQL_DATASOURCE_NAME,
            SQL_CONN_STRING_FIXTURE,
            "GeoNamesRI",
            changeDetectionPolicy,
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestSqlDataSource() {
        DataDeletionDetectionPolicy deletionDetectionPolicy = null;
        return azureSql(
            SQL_DATASOURCE_NAME,
            AZURE_SQL_CONN_STRING_READONLY,
            "GeoNamesRI",
            null,
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestCosmosDbDataSource(
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.cosmosDb(
            COSMOS_DATASOURCE_NAME,
            "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase",
            "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark",
            useChangeDetection,
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    /**
     * create a new blob datasource object
     * @return the created datasource
     */
    protected DataSource createBlobDataSource() {
        String storageConnString = "";
        String blobContainerDatasourceName = "";
        if (!interceptorManager.isPlaybackMode()) {

            // First, we create a storage account
            storageConnString = azureSearchResources.createStorageAccount();
            // Next, we create the blobs container
            blobContainerDatasourceName =
                azureSearchResources.createBlobContainer(storageConnString);
        }

        // create the new datasource object for this storage account and container
        return DataSources.azureBlobStorage(
            BLOB_DATASOURCE_NAME,
            storageConnString,
            blobContainerDatasourceName,
            "/",
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("fieldName")
                .setSoftDeleteMarkerValue("someValue"),
            "real live blob"
        );
    }

    private static void initializeAzureResources() {
        String appId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        String azureDomainId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID);
        String secret = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
        String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);

        testEnvironment = Configuration.getGlobalConfiguration().get("AZURE_TEST_ENVIRONMENT");
        if (testEnvironment == null) {
            testEnvironment = "AZURE";
        } else {
            testEnvironment = testEnvironment.toUpperCase(Locale.US);
        }

        AzureEnvironment environment = testEnvironment.equals("DOGFOOD") ? getDogfoodEnvironment() : AzureEnvironment.AZURE;

        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            appId,
            azureDomainId,
            secret,
            environment);

        azureSearchResources = new AzureSearchResources(applicationTokenCredentials, subscriptionId, Region.US_EAST);
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
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchIndexClientBuilder()
                .endpoint(String.format("https://%s.%s", searchServiceName, searchDnsSuffix))
                .indexName(indexName)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchIndexClientBuilder()
                .endpoint(String.format("https://%s.%s", searchServiceName, searchDnsSuffix))
                .indexName(indexName)
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    protected void assertFieldsEqual(Field expected, Field actual) {
        Assert.assertEquals(expected.getName(), actual.getName());

        // ONLY verify the properties we set explicitly.
        if (expected.isKey() != null) {
            Assert.assertEquals(expected.isKey(), actual.isKey());
        }
        if (expected.isSearchable() != null) {
            Assert.assertEquals(expected.isSearchable(), actual.isSearchable());
        }
        if (expected.isFilterable() != null) {
            Assert.assertEquals(expected.isFilterable(), actual.isFilterable());
        }
        if (expected.isSortable() != null) {
            Assert.assertEquals(expected.isSortable(), actual.isSortable());
        }
        if (expected.isFacetable() != null) {
            Assert.assertEquals(expected.isFacetable(), actual.isFacetable());
        }
        if (expected.isRetrievable() != null) {
            Assert.assertEquals(expected.isRetrievable(), actual.isRetrievable());
        }
    }

    protected void assertIndexesEqual(Index expected, Index actual) {
        Double delta = 0.0;

        // Fields
        List<Field> expectedFields = expected.getFields();
        List<Field> actualFields = actual.getFields();
        if (expectedFields != null && actualFields != null) {
            Assert.assertEquals(expectedFields.size(), actualFields.size());
            for (int i = 0; i < expectedFields.size(); i++) {
                Field expectedField = expectedFields.get(i);
                Field actualField = actualFields.get(i);

                assertFieldsEqual(expectedField, actualField);

                // (Secondary) fields
                List<Field> expectedSecondaryFields = expectedField.getFields();
                List<Field> actualSecondaryFields = actualField.getFields();
                if (expectedSecondaryFields != null && actualSecondaryFields != null) {
                    Assert.assertEquals(expectedSecondaryFields.size(), actualSecondaryFields.size());
                    for (int j = 0; j < expectedSecondaryFields.size(); j++) {
                        // Per setup in createTestIndex(), Field property has depth up to 2.
                        // Assert that 3rd level Field property doesn't exist to guard against future improper usage.
                        Assert.assertNull(expectedSecondaryFields.get(j).getFields());
                        assertFieldsEqual(expectedSecondaryFields.get(j), actualSecondaryFields.get(j));
                    }
                }
            }
        }

        List<ScoringProfile> expectedScoringProfiles = expected.getScoringProfiles();
        List<ScoringProfile> actualScoringProfiles = actual.getScoringProfiles();

        if (expectedScoringProfiles != null && actualScoringProfiles != null) {
            // Scoring profiles
            Assert.assertEquals(expectedScoringProfiles.size(), actualScoringProfiles.size());
            for (int i = 0; i < expectedScoringProfiles.size(); i++) {
                ScoringProfile expectedScoringProfile = expectedScoringProfiles.get(i);
                ScoringProfile actualScoringProfile = actualScoringProfiles.get(i);

                Assert.assertEquals(expectedScoringProfile.getName(), actualScoringProfile.getName());
                Assert.assertTrue(Objects.equals(expectedScoringProfile.getFunctionAggregation(), actualScoringProfile.getFunctionAggregation()));

                // Scoring functions
                Assert.assertEquals(expectedScoringProfile.getFunctions().size(), actualScoringProfile.getFunctions().size());
                for (int j = 0; j < expectedScoringProfile.getFunctions().size(); j++) {
                    ScoringFunction expectedFunction = expectedScoringProfile.getFunctions().get(j);
                    ScoringFunction actualFunction = expectedScoringProfile.getFunctions().get(j);
                    Assert.assertEquals(expectedFunction.getFieldName(), actualFunction.getFieldName());
                    Assert.assertEquals(expectedFunction.getBoost(), actualFunction.getBoost(), delta);
                    Assert.assertEquals(expectedFunction.getInterpolation(), actualFunction.getInterpolation());

                    if (expectedFunction instanceof MagnitudeScoringFunction) {
                        MagnitudeScoringFunction expectedMsf = (MagnitudeScoringFunction) expectedFunction;
                        MagnitudeScoringFunction actualMsf = (MagnitudeScoringFunction) actualFunction;
                        MagnitudeScoringParameters expectedParams = expectedMsf.getParameters();
                        MagnitudeScoringParameters actualParams = actualMsf.getParameters();
                        Assert.assertEquals(expectedParams.getBoostingRangeStart(), actualParams.getBoostingRangeStart(), delta);
                        Assert.assertEquals(expectedParams.getBoostingRangeEnd(), actualParams.getBoostingRangeEnd(), delta);
                    } else if (expectedFunction instanceof DistanceScoringFunction) {
                        DistanceScoringFunction expectedDsf = (DistanceScoringFunction) expectedFunction;
                        DistanceScoringFunction actualDsf = (DistanceScoringFunction) actualFunction;
                        DistanceScoringParameters expectedParams = expectedDsf.getParameters();
                        DistanceScoringParameters actualParams = actualDsf.getParameters();
                        Assert.assertEquals(expectedParams.getBoostingDistance(), actualParams.getBoostingDistance(), delta);
                        Assert.assertEquals(expectedParams.getReferencePointParameter(), actualParams.getReferencePointParameter());
                    } else if (expectedFunction instanceof FreshnessScoringFunction) {
                        Assert.assertEquals(((FreshnessScoringFunction) expectedFunction).getParameters().getBoostingDuration(),
                            ((FreshnessScoringFunction) actualFunction).getParameters().getBoostingDuration());
                    } else if (expectedFunction instanceof TagScoringFunction) {
                        Assert.assertEquals(((TagScoringFunction) expectedFunction).getParameters().getTagsParameter(),
                            ((TagScoringFunction) actualFunction).getParameters().getTagsParameter());
                    } else {
                        throw new NotImplementedException("The comparison of scoring function type "
                            + expectedFunction.getClass() + " is not implemented yet.");
                    }
                }
                if (expectedScoringProfile.getTextWeights() != null && actualScoringProfile.getTextWeights().getWeights() != null) {
                    Assert.assertEquals(expectedScoringProfile.getTextWeights().getWeights().size(), actualScoringProfile.getTextWeights().getWeights().size());
                }
            }
        }

        // Default scoring profile
        Assert.assertEquals(expected.getDefaultScoringProfile(), actual.getDefaultScoringProfile());

        // Cors options
        CorsOptions expectedCorsOptions = expected.getCorsOptions();
        CorsOptions actualCorsOptions = actual.getCorsOptions();
        if (expectedCorsOptions != null && actualCorsOptions != null) {
            Assert.assertTrue(ModelComparer.collectionEquals(expectedCorsOptions.getAllowedOrigins(), actualCorsOptions.getAllowedOrigins()));
            Assert.assertEquals(expectedCorsOptions.getMaxAgeInSeconds(), actualCorsOptions.getMaxAgeInSeconds());
        }

        // Suggesters
        List<Suggester> expectedSuggesters = expected.getSuggesters();
        List<Suggester> actualSuggesters = actual.getSuggesters();
        if (expectedSuggesters != null && actualSuggesters != null) {
            Assert.assertEquals(expectedSuggesters.size(), actualSuggesters.size());
            for (int i = 0; i < expectedSuggesters.size(); i++) {
                Suggester expectedSuggester = expectedSuggesters.get(i);
                Suggester actualSuggester = actualSuggesters.get(i);
                Assert.assertEquals(expectedSuggester.getName(), actualSuggester.getName());
                Assert.assertTrue(ModelComparer.collectionEquals(expectedSuggester.getSourceFields(), actualSuggester.getSourceFields()));
            }
        }
    }

    protected void waitForIndexing() {
        // Wait 2 secs to allow index request to finish
        if (!interceptorManager.isPlaybackMode()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * If the document schema is known, user can convert the properties to a specific object type
     *
     * @param cls Class type of the document object to convert to
     * @param <T> type
     * @return an object of the request type
     */
    protected <T> T convertToType(Object document, Class<T> cls) {
        return jsonApi.convertObjectToType(document, cls);
    }

    protected void addFieldToIndex(Index index, Field field) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(index.getFields());
        fields.add(field);

        index.setFields(fields);
    }

    /**
     * Constructs a request options object with client request Id.
     * @return a RequestOptions object with ClientRequestId.
     */
    protected RequestOptions generateRequestOptions() {
        return new RequestOptions()
            .setClientRequestId(UUID.randomUUID());
    }

    void assertException(
        Runnable exceptionThrower, Class<? extends Exception> expectedExceptionType, String expectedMessage) {
        try {
            exceptionThrower.run();
            Assert.fail();

        } catch (Throwable ex) {
            // Check that this is not the "Assert.fail()" above:
            Assert.assertNotEquals(AssertionError.class, ex.getClass());

            Assert.assertEquals(expectedExceptionType, ex.getClass());
            if (expectedMessage != null) {
                Assert.assertTrue(ex.getMessage().contains(expectedMessage));
            }
        }
    }

    protected ServiceStatistics getExpectedServiceStatistics() {
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
}
