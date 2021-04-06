// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureCosmosDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureTableDataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedAutoRollUpMethod;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.models.DataFeedMissingDataPointFillType;
import com.azure.ai.metricsadvisor.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.models.ElasticsearchDataFeedSource;
import com.azure.ai.metricsadvisor.models.HttpRequestDataFeedSource;
import com.azure.ai.metricsadvisor.models.InfluxDBDataFeedSource;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.ai.metricsadvisor.models.MongoDBDataFeedSource;
import com.azure.ai.metricsadvisor.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.PostgreSqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.SQLServerDataFeedSource;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.TestUtils.APP_INSIGHTS_API_KEY;
import static com.azure.ai.metricsadvisor.TestUtils.APP_INSIGHTS_APPLICATION_ID;
import static com.azure.ai.metricsadvisor.TestUtils.APP_INSIGHTS_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_DATALAKEGEN2_ACCOUNT_KEY;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.COSMOS_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.DIRECTORY_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.ELASTIC_SEARCH_AUTH_HEADER;
import static com.azure.ai.metricsadvisor.TestUtils.ELASTIC_SEARCH_HOST;
import static com.azure.ai.metricsadvisor.TestUtils.FILE_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.HTTP_URL;
import static com.azure.ai.metricsadvisor.TestUtils.INFLUX_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.INFLUX_DB_PASSWORD;
import static com.azure.ai.metricsadvisor.TestUtils.INGESTION_START_TIME;
import static com.azure.ai.metricsadvisor.TestUtils.MONGO_COMMAND;
import static com.azure.ai.metricsadvisor.TestUtils.MONGO_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.MYSQL_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.POSTGRE_SQL_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.SQL_SERVER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.TABLE_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.TABLE_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.TEMPLATE_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.TEST_DB_NAME;
import static com.azure.ai.metricsadvisor.TestUtils.getAzureBlobDataFeedSample;
import static com.azure.ai.metricsadvisor.TestUtils.getSQLDataFeedSample;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class DataFeedTestBase extends MetricsAdvisorAdministrationClientTestBase {

    @Override
    protected void beforeTest() {
    }

    @Test
    abstract void createSQLServerDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeed(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeedTop3(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeedFilterByCreator(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeedFilterBySourceType(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeedFilterByStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataFeedFilterByGranularityType(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion);

    void listDataFeedRunner(Consumer<List<DataFeed>> testRunner) {
        // create data feeds
        testRunner.accept(Arrays.asList(getAzureBlobDataFeedSample(), getSQLDataFeedSample()));
    }

    void creatDataFeedRunner(Consumer<DataFeed> testRunner, DataFeedSourceType dataFeedSourceType) {
        // create data feeds
        DataFeed dataFeed;
        switch (dataFeedSourceType) {
            case AZURE_APP_INSIGHTS:
                dataFeed = new DataFeed().setSource(new AzureAppInsightsDataFeedSource(
                    APP_INSIGHTS_APPLICATION_ID, APP_INSIGHTS_API_KEY, TestUtils.AZURE_CLOUD, APP_INSIGHTS_QUERY));
                break;
            case AZURE_BLOB:
                dataFeed = new DataFeed().setSource(new AzureBlobDataFeedSource(BLOB_CONNECTION_STRING,
                    TEST_DB_NAME, BLOB_TEMPLATE));
                break;
            case AZURE_DATA_EXPLORER:
                dataFeed =
                    new DataFeed().setSource(new AzureDataExplorerDataFeedSource(DATA_EXPLORER_CONNECTION_STRING,
                        DATA_EXPLORER_QUERY));
                break;
            case AZURE_TABLE:
                dataFeed = new DataFeed().setSource(new AzureTableDataFeedSource(TABLE_CONNECTION_STRING,
                    TABLE_QUERY, TEST_DB_NAME));
                break;
            case HTTP_REQUEST:
                dataFeed = new DataFeed().setSource(new HttpRequestDataFeedSource(HTTP_URL, "GET"));
                break;
            case INFLUX_DB:
                dataFeed = new DataFeed().setSource(new InfluxDBDataFeedSource(INFLUX_DB_CONNECTION_STRING,
                    TEST_DB_NAME, "adreadonly", INFLUX_DB_PASSWORD, TEMPLATE_QUERY));
                break;
            case MONGO_DB:
                dataFeed = new DataFeed().setSource(new MongoDBDataFeedSource(MONGO_DB_CONNECTION_STRING,
                    TEST_DB_NAME, MONGO_COMMAND));
                break;
            case MYSQL_DB:
                dataFeed = new DataFeed().setSource(new MySqlDataFeedSource(MYSQL_DB_CONNECTION_STRING,
                    TEMPLATE_QUERY));
                break;
            case POSTGRE_SQL_DB:
                dataFeed = new DataFeed().setSource(new PostgreSqlDataFeedSource(POSTGRE_SQL_DB_CONNECTION_STRING,
                    TEMPLATE_QUERY));
                break;
            case SQL_SERVER_DB:
                dataFeed = new DataFeed().setSource(new SQLServerDataFeedSource(SQL_SERVER_CONNECTION_STRING,
                    TEMPLATE_QUERY));
                break;
            case AZURE_COSMOS_DB:
                dataFeed = new DataFeed().setSource(new AzureCosmosDataFeedSource(COSMOS_DB_CONNECTION_STRING,
                    TEMPLATE_QUERY, TEST_DB_NAME, TEST_DB_NAME));
                break;
            case ELASTIC_SEARCH:
                dataFeed = new DataFeed().setSource(new ElasticsearchDataFeedSource(ELASTIC_SEARCH_HOST, "9200",
                    ELASTIC_SEARCH_AUTH_HEADER, TEMPLATE_QUERY));
                break;
            case AZURE_DATA_LAKE_STORAGE_GEN2:
                dataFeed = new DataFeed().setSource(new AzureDataLakeStorageGen2DataFeedSource(
                    "adsampledatalakegen2",
                    AZURE_DATALAKEGEN2_ACCOUNT_KEY,
                    TEST_DB_NAME, DIRECTORY_TEMPLATE, FILE_TEMPLATE));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataFeedSourceType);
        }
        testRunner.accept(dataFeed.setSchema(new DataFeedSchema(Arrays.asList(
            new DataFeedMetric().setName("cost").setDisplayName("cost"),
            new DataFeedMetric().setName("revenue").setDisplayName("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension().setName("city").setDisplayName("city"),
                new DataFeedDimension().setName("category").setDisplayName("category"))))
            .setName("java_create_data_feed_test_sample" + UUID.randomUUID())
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setIngestionSettings(new DataFeedIngestionSettings(INGESTION_START_TIME)));
    }


    void validateDataFeedResult(DataFeed expectedDataFeed, DataFeed actualDataFeed,
        DataFeedSourceType dataFeedSourceType) {
        assertNotNull(actualDataFeed.getId());
        assertNotNull(actualDataFeed.getCreatedTime());
        assertNotNull(actualDataFeed.getMetricIds());
        assertNotNull(actualDataFeed.getName());
        assertEquals(2, actualDataFeed.getMetricIds().size());
        assertEquals(dataFeedSourceType, actualDataFeed.getSourceType());
        assertNotNull(actualDataFeed.getStatus());
        validateDataFeedGranularity(expectedDataFeed.getGranularity(), actualDataFeed.getGranularity());
        validateDataFeedIngestionSettings(expectedDataFeed.getIngestionSettings(),
            actualDataFeed.getIngestionSettings());
        validateDataFeedSchema(expectedDataFeed.getSchema(), actualDataFeed.getSchema());
        validateDataFeedOptions(expectedDataFeed.getOptions(), actualDataFeed.getOptions());
        validateDataFeedSource(expectedDataFeed, actualDataFeed, dataFeedSourceType);
    }

    private void validateDataFeedSource(DataFeed expectedDataFeed, DataFeed actualDataFeed,
        DataFeedSourceType dataFeedSourceType) {
        switch (dataFeedSourceType) {
            case AZURE_APP_INSIGHTS:
                final AzureAppInsightsDataFeedSource expAzureAppInsightsDataFeedSource =
                    (AzureAppInsightsDataFeedSource) expectedDataFeed.getSource();
                final AzureAppInsightsDataFeedSource actualAzureAppInsightsDataFeedSource =
                    (AzureAppInsightsDataFeedSource) actualDataFeed.getSource();
                // ApiKey and applicationId are no longer returned from the service.
                // assertNotNull(actualAzureAppInsightsDataFeedSource.getApiKey());
                // assertNotNull(actualAzureAppInsightsDataFeedSource.getApplicationId());
                assertEquals(expAzureAppInsightsDataFeedSource.getQuery(),
                    actualAzureAppInsightsDataFeedSource.getQuery());
                assertEquals(expAzureAppInsightsDataFeedSource.getAzureCloud(),
                    actualAzureAppInsightsDataFeedSource.getAzureCloud());
                break;
            case AZURE_BLOB:
                final AzureBlobDataFeedSource expBlobDataFeedSource =
                    (AzureBlobDataFeedSource) expectedDataFeed.getSource();
                final AzureBlobDataFeedSource actualBlobDataFeedSource =
                    (AzureBlobDataFeedSource) actualDataFeed.getSource();
                assertEquals(expBlobDataFeedSource.getBlobTemplate(), actualBlobDataFeedSource.getBlobTemplate());
                // connection string is no longer returned from the service.
                // assertNotNull(actualBlobDataFeedSource.getConnectionString());
                assertEquals(expBlobDataFeedSource.getContainer(), actualBlobDataFeedSource.getContainer());
                break;
            case AZURE_DATA_EXPLORER:
                final AzureDataExplorerDataFeedSource expExplorerDataFeedSource =
                    (AzureDataExplorerDataFeedSource) expectedDataFeed.getSource();
                final AzureDataExplorerDataFeedSource actualExplorerDataFeedSource =
                    (AzureDataExplorerDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualExplorerDataFeedSource.getConnectionString());
                assertEquals(expExplorerDataFeedSource.getQuery(), actualExplorerDataFeedSource.getQuery());
                break;
            case AZURE_TABLE:
                final AzureTableDataFeedSource expTableDataFeedSource =
                    (AzureTableDataFeedSource) expectedDataFeed.getSource();
                final AzureTableDataFeedSource actualTableDataFeedSource =
                    (AzureTableDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualTableDataFeedSource.getConnectionString());
                assertEquals(expTableDataFeedSource.getTableName(), actualTableDataFeedSource.getTableName());
                assertEquals(expTableDataFeedSource.getQueryScript(), actualTableDataFeedSource.getQueryScript());
                break;
            case HTTP_REQUEST:
                final HttpRequestDataFeedSource expHttpDataFeedSource =
                    (HttpRequestDataFeedSource) expectedDataFeed.getSource();
                final HttpRequestDataFeedSource actualHttpDataFeedSource =
                    (HttpRequestDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualHttpDataFeedSource.getUrl());
                assertEquals(expHttpDataFeedSource.getHttpHeader(), actualHttpDataFeedSource.getHttpHeader());
                assertEquals(expHttpDataFeedSource.getPayload(), actualHttpDataFeedSource.getPayload());
                assertEquals(expHttpDataFeedSource.getHttpMethod(), actualHttpDataFeedSource.getHttpMethod());
                break;
            case INFLUX_DB:
                final InfluxDBDataFeedSource expInfluxDataFeedSource =
                    (InfluxDBDataFeedSource) expectedDataFeed.getSource();
                final InfluxDBDataFeedSource actualInfluxDataFeedSource =
                    (InfluxDBDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualInfluxDataFeedSource.getConnectionString());
                assertEquals(expInfluxDataFeedSource.getDatabase(), actualInfluxDataFeedSource.getDatabase());
                assertNotNull(actualInfluxDataFeedSource.getPassword());
                assertNotNull(actualInfluxDataFeedSource.getUserName());
                break;
            case MONGO_DB:
                final MongoDBDataFeedSource expMongoDataFeedSource =
                    (MongoDBDataFeedSource) expectedDataFeed.getSource();
                final MongoDBDataFeedSource actualMongoDataFeedSource =
                    (MongoDBDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualMongoDataFeedSource.getConnectionString());
                assertEquals(expMongoDataFeedSource.getDatabase(), actualMongoDataFeedSource.getDatabase());
                assertEquals(expMongoDataFeedSource.getCommand(), actualMongoDataFeedSource.getCommand());
                break;
            case MYSQL_DB:
                final MySqlDataFeedSource expMySqlDataFeedSource = (MySqlDataFeedSource) expectedDataFeed.getSource();
                final MySqlDataFeedSource actualMySqlDataFeedSource = (MySqlDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualMySqlDataFeedSource.getConnectionString());
                assertEquals(expMySqlDataFeedSource.getQuery(), actualMySqlDataFeedSource.getQuery());
                break;
            case POSTGRE_SQL_DB:
                final PostgreSqlDataFeedSource expPostGreDataFeedSource =
                    (PostgreSqlDataFeedSource) expectedDataFeed.getSource();
                final PostgreSqlDataFeedSource actualPostGreDataFeedSource =
                    (PostgreSqlDataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualPostGreDataFeedSource.getConnectionString());
                assertEquals(expPostGreDataFeedSource.getQuery(), actualPostGreDataFeedSource.getQuery());
                break;
            case SQL_SERVER_DB:
                final SQLServerDataFeedSource expSqlServerDataFeedSource =
                    (SQLServerDataFeedSource) expectedDataFeed.getSource();
                final SQLServerDataFeedSource actualSqlServerDataFeedSource =
                    (SQLServerDataFeedSource) actualDataFeed.getSource();
                // connection string is no longer returned from the service.
                // assertNotNull(actualSqlServerDataFeedSource.getConnectionString());
                assertEquals(expSqlServerDataFeedSource.getQuery(), actualSqlServerDataFeedSource.getQuery());
                break;
            case AZURE_COSMOS_DB:
                final AzureCosmosDataFeedSource expCosmosDataFeedSource =
                    (AzureCosmosDataFeedSource) expectedDataFeed.getSource();
                final AzureCosmosDataFeedSource actualCosmosDataFeedSource =
                    (AzureCosmosDataFeedSource) actualDataFeed.getSource();
                assertEquals(expCosmosDataFeedSource.getCollectionId(), actualCosmosDataFeedSource.getCollectionId());
                assertNotNull(actualCosmosDataFeedSource.getConnectionString());
                assertEquals(expCosmosDataFeedSource.getDatabase(), actualCosmosDataFeedSource.getDatabase());
                assertEquals(expCosmosDataFeedSource.getSqlQuery(), actualCosmosDataFeedSource.getSqlQuery());
                break;
            case ELASTIC_SEARCH:
                final ElasticsearchDataFeedSource expElasticsearchDataFeedSource =
                    (ElasticsearchDataFeedSource) expectedDataFeed.getSource();
                final ElasticsearchDataFeedSource actualDataFeedSource =
                    (ElasticsearchDataFeedSource) actualDataFeed.getSource();
                // Auth header is no longer returned from the service.
                // assertNotNull(actualDataFeedSource.getAuthHeader());
                assertNotNull(actualDataFeedSource.getHost());
                assertEquals(expElasticsearchDataFeedSource.getPort(), actualDataFeedSource.getPort());
                assertEquals(expElasticsearchDataFeedSource.getQuery(), actualDataFeedSource.getQuery());
                break;
            case AZURE_DATA_LAKE_STORAGE_GEN2:
                final AzureDataLakeStorageGen2DataFeedSource expDataLakeStorageGen2DataFeedSource =
                    (AzureDataLakeStorageGen2DataFeedSource) expectedDataFeed.getSource();
                final AzureDataLakeStorageGen2DataFeedSource actualDataLakeFeedSource =
                    (AzureDataLakeStorageGen2DataFeedSource) actualDataFeed.getSource();
                assertNotNull(actualDataLakeFeedSource.getAccountKey());
                assertNotNull(actualDataLakeFeedSource.getAccountName());
                assertEquals(expDataLakeStorageGen2DataFeedSource.getDirectoryTemplate(),
                    actualDataLakeFeedSource.getDirectoryTemplate());
                assertEquals(expDataLakeStorageGen2DataFeedSource.getFileSystemName(),
                    actualDataLakeFeedSource.getFileSystemName());
                assertEquals(expDataLakeStorageGen2DataFeedSource.getFileTemplate(),
                    actualDataLakeFeedSource.getFileTemplate());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataFeedSourceType);
        }
    }

    private void validateDataFeedOptions(DataFeedOptions expectedOptions, DataFeedOptions actualOptions) {
        if (expectedOptions != null) {
            assertEquals(expectedOptions.getDescription(), actualOptions.getDescription());
            assertEquals(expectedOptions.getActionLinkTemplate(), actualOptions.getActionLinkTemplate());
            assertIterableEquals(expectedOptions.getAdminEmails(), actualOptions.getAdminEmails());
            assertIterableEquals(expectedOptions.getViewerEmails(), actualOptions.getViewerEmails());
            assertNotNull(actualOptions.getAccessMode());
            if (expectedOptions.getAccessMode() != null) {
                assertEquals(expectedOptions.getAccessMode(), actualOptions.getAccessMode());
            }

            validateRollUpSettings(expectedOptions.getRollupSettings(), actualOptions.getRollupSettings());
            validateFillSettings(expectedOptions.getMissingDataPointFillSettings(),
                actualOptions.getMissingDataPointFillSettings());
        } else {
            // setting defaults
            validateRollUpSettings(new DataFeedRollupSettings().setRollupType(DataFeedRollupType.NO_ROLLUP),
                actualOptions.getRollupSettings());
            validateFillSettings(new DataFeedMissingDataPointFillSettings()
                    .setFillType(DataFeedMissingDataPointFillType.PREVIOUS_VALUE).setCustomFillValue(0.0),
                actualOptions.getMissingDataPointFillSettings());
        }
    }

    private void validateFillSettings(DataFeedMissingDataPointFillSettings expectedFillSettings,
        DataFeedMissingDataPointFillSettings actualFillSettings) {
        assertEquals(expectedFillSettings.getCustomFillValue(), actualFillSettings.getCustomFillValue());
        assertEquals(expectedFillSettings.getFillType(), actualFillSettings.getFillType());
    }

    private void validateRollUpSettings(DataFeedRollupSettings expectedRollUpSettings,
        DataFeedRollupSettings actualRollUpSettings) {
        assertEquals(expectedRollUpSettings.getRollupIdentificationValue(), actualRollUpSettings
            .getRollupIdentificationValue());
        assertEquals(expectedRollUpSettings.getRollupType(), actualRollUpSettings.getRollupType());
        if (expectedRollUpSettings.getDataFeedAutoRollUpMethod() != null) {
            assertEquals(expectedRollUpSettings.getDataFeedAutoRollUpMethod(), actualRollUpSettings
                .getDataFeedAutoRollUpMethod());
        }
        assertEquals(DataFeedAutoRollUpMethod.NONE, actualRollUpSettings
            .getDataFeedAutoRollUpMethod());

    }

    private void validateDataFeedSchema(DataFeedSchema expectedDataFeedSchema, DataFeedSchema actualDataFeedSchema) {
        assertEquals(expectedDataFeedSchema.getDimensions().size(), actualDataFeedSchema.getDimensions().size());
        expectedDataFeedSchema.getDimensions().sort(Comparator.comparing(DataFeedDimension::getName));
        actualDataFeedSchema.getDimensions().sort(Comparator.comparing(DataFeedDimension::getName));
        for (int i = 0; i < expectedDataFeedSchema.getDimensions().size(); i++) {
            DataFeedDimension expectedDimension = expectedDataFeedSchema.getDimensions().get(i);
            DataFeedDimension actualDimension = actualDataFeedSchema.getDimensions().get(i);
            assertEquals(expectedDimension.getName(), actualDimension.getName());
            assertNotNull(actualDimension.getDisplayName());
            if (expectedDimension.getDisplayName() != null) {
                assertEquals(expectedDimension.getDisplayName(), actualDimension.getDisplayName());
            } else {
                assertEquals(expectedDimension.getName(), actualDimension.getDisplayName());
            }
        }

        assertEquals(expectedDataFeedSchema.getMetrics().size(), actualDataFeedSchema.getMetrics().size());
        expectedDataFeedSchema.getMetrics().sort(Comparator.comparing(DataFeedMetric::getName));
        actualDataFeedSchema.getMetrics().sort(Comparator.comparing(DataFeedMetric::getName));
        for (int i = 0; i < expectedDataFeedSchema.getMetrics().size(); i++) {
            DataFeedMetric expectedMetric = expectedDataFeedSchema.getMetrics().get(i);
            DataFeedMetric actualMetric = actualDataFeedSchema.getMetrics().get(i);
            assertNotNull(actualMetric.getId());
            assertEquals(expectedMetric.getName(), actualMetric.getName());
            if (expectedMetric.getDescription() != null) {
                assertEquals(expectedMetric.getDescription(), actualMetric.getDescription());
            }
            assertNotNull(actualMetric.getDescription());
            if (expectedMetric.getDisplayName() != null) {
                assertEquals(expectedMetric.getDisplayName(), actualMetric.getDisplayName());
            } else {
                assertEquals(expectedMetric.getName(), actualMetric.getDisplayName());
            }
        }
        assertNotNull(actualDataFeedSchema.getTimestampColumn());
    }

    private void validateDataFeedGranularity(DataFeedGranularity expectedGranularity,
        DataFeedGranularity actualGranularity) {
        assertEquals(expectedGranularity.getGranularityType(), actualGranularity.getGranularityType());
        if (DataFeedGranularityType.CUSTOM.equals(actualGranularity.getGranularityType())) {
            assertEquals(expectedGranularity.getCustomGranularityValue(),
                actualGranularity.getCustomGranularityValue());
        }
    }

    private void validateDataFeedIngestionSettings(DataFeedIngestionSettings expectedIngestion,
        DataFeedIngestionSettings actualIngestion) {
        assertEquals(expectedIngestion.getIngestionStartTime(), actualIngestion.getIngestionStartTime());
        assertEquals(expectedIngestion.getDataSourceRequestConcurrency(),
            actualIngestion.getDataSourceRequestConcurrency());
        assertEquals(Duration.ofSeconds(-1), actualIngestion.getIngestionRetryDelay());
        assertEquals(Duration.ofSeconds(0), actualIngestion.getIngestionStartOffset());
        assertEquals(Duration.ofSeconds(-1), actualIngestion.getStopRetryAfter());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
