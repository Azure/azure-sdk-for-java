// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureCosmosDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureLogAnalyticsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureTableDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedAutoRollUpMethod;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.models.InfluxDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.MongoDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.PostgreSqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
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
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_ID;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_SECRET;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_TENANT_ID;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_LOG_ANALYTICS_WORKSPACE_ID;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.COSMOS_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.DIRECTORY_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.FILE_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.INFLUX_DB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.INFLUX_DB_PASSWORD;
import static com.azure.ai.metricsadvisor.TestUtils.INGESTION_START_TIME;
import static com.azure.ai.metricsadvisor.TestUtils.LOG_ANALYTICS_QUERY;
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
        if (dataFeedSourceType == DataFeedSourceType.AZURE_APP_INSIGHTS) {
            dataFeed = new DataFeed().setSource(new AzureAppInsightsDataFeedSource(
                APP_INSIGHTS_APPLICATION_ID, APP_INSIGHTS_API_KEY, TestUtils.AZURE_CLOUD, APP_INSIGHTS_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_BLOB) {
            dataFeed = new DataFeed().setSource(AzureBlobDataFeedSource.fromBasicCredential(
                BLOB_CONNECTION_STRING,
                TEST_DB_NAME, BLOB_TEMPLATE));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_DATA_EXPLORER) {
            dataFeed =
                new DataFeed().setSource(AzureDataExplorerDataFeedSource.fromBasicCredential(
                    DATA_EXPLORER_CONNECTION_STRING,
                    DATA_EXPLORER_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_TABLE) {
            dataFeed = new DataFeed().setSource(new AzureTableDataFeedSource(TABLE_CONNECTION_STRING,
                TABLE_QUERY, TEST_DB_NAME));
        } else if (dataFeedSourceType == DataFeedSourceType.INFLUX_DB) {
            dataFeed = new DataFeed().setSource(new InfluxDbDataFeedSource(INFLUX_DB_CONNECTION_STRING,
                TEST_DB_NAME, "adreadonly", INFLUX_DB_PASSWORD, TEMPLATE_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.MONGO_DB) {
            dataFeed = new DataFeed().setSource(new MongoDbDataFeedSource(MONGO_DB_CONNECTION_STRING,
                TEST_DB_NAME, MONGO_COMMAND));
        } else if (dataFeedSourceType == DataFeedSourceType.MYSQL_DB) {
            dataFeed = new DataFeed().setSource(new MySqlDataFeedSource(MYSQL_DB_CONNECTION_STRING,
                TEMPLATE_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.POSTGRE_SQL_DB) {
            dataFeed = new DataFeed().setSource(new PostgreSqlDataFeedSource(POSTGRE_SQL_DB_CONNECTION_STRING,
                TEMPLATE_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.SQL_SERVER_DB) {
            dataFeed = new DataFeed().setSource(SqlServerDataFeedSource.fromBasicCredential(
                SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_COSMOS_DB) {
            dataFeed = new DataFeed().setSource(new AzureCosmosDbDataFeedSource(COSMOS_DB_CONNECTION_STRING,
                TEMPLATE_QUERY, TEST_DB_NAME, TEST_DB_NAME));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2) {
            dataFeed = new DataFeed().setSource(AzureDataLakeStorageGen2DataFeedSource.fromBasicCredential(
                "adsampledatalakegen2",
                AZURE_DATALAKEGEN2_ACCOUNT_KEY,
                TEST_DB_NAME, DIRECTORY_TEMPLATE, FILE_TEMPLATE));
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_LOG_ANALYTICS) {
            dataFeed = new DataFeed().setSource(AzureLogAnalyticsDataFeedSource.fromBasicCredential(
                AZURE_METRICS_ADVISOR_TENANT_ID,
                AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_ID,
                AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_SECRET,
                AZURE_METRICS_ADVISOR_LOG_ANALYTICS_WORKSPACE_ID,
                LOG_ANALYTICS_QUERY));
        } else {
            throw new IllegalStateException("Unexpected value: " + dataFeedSourceType);
        }

        testRunner.accept(dataFeed.setSchema(new DataFeedSchema(Arrays.asList(
            new DataFeedMetric("cost").setDisplayName("cost"),
            new DataFeedMetric("revenue").setDisplayName("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension("city").setDisplayName("city"),
                new DataFeedDimension("category").setDisplayName("category"))))
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
        if (dataFeedSourceType == DataFeedSourceType.AZURE_APP_INSIGHTS) {
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
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_BLOB) {
            final AzureBlobDataFeedSource expBlobDataFeedSource =
                (AzureBlobDataFeedSource) expectedDataFeed.getSource();
            final AzureBlobDataFeedSource actualBlobDataFeedSource =
                (AzureBlobDataFeedSource) actualDataFeed.getSource();
            assertEquals(expBlobDataFeedSource.getBlobTemplate(), actualBlobDataFeedSource.getBlobTemplate());
            // connection string is no longer returned from the service.
            // assertNotNull(actualBlobDataFeedSource.getConnectionString());
            assertEquals(expBlobDataFeedSource.getContainer(), actualBlobDataFeedSource.getContainer());
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_DATA_EXPLORER) {
            final AzureDataExplorerDataFeedSource expExplorerDataFeedSource =
                (AzureDataExplorerDataFeedSource) expectedDataFeed.getSource();
            final AzureDataExplorerDataFeedSource actualExplorerDataFeedSource =
                (AzureDataExplorerDataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualExplorerDataFeedSource.getConnectionString());
            assertEquals(expExplorerDataFeedSource.getQuery(), actualExplorerDataFeedSource.getQuery());
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_TABLE) {
            final AzureTableDataFeedSource expTableDataFeedSource =
                (AzureTableDataFeedSource) expectedDataFeed.getSource();
            final AzureTableDataFeedSource actualTableDataFeedSource =
                (AzureTableDataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualTableDataFeedSource.getConnectionString());
            assertEquals(expTableDataFeedSource.getTableName(), actualTableDataFeedSource.getTableName());
            assertEquals(expTableDataFeedSource.getQueryScript(), actualTableDataFeedSource.getQueryScript());
        } else if (dataFeedSourceType == DataFeedSourceType.INFLUX_DB) {
            final InfluxDbDataFeedSource expInfluxDataFeedSource =
                (InfluxDbDataFeedSource) expectedDataFeed.getSource();
            final InfluxDbDataFeedSource actualInfluxDataFeedSource =
                (InfluxDbDataFeedSource) actualDataFeed.getSource();
            assertNotNull(actualInfluxDataFeedSource.getConnectionString());
            assertEquals(expInfluxDataFeedSource.getDatabase(), actualInfluxDataFeedSource.getDatabase());
            // assertNotNull(actualInfluxDataFeedSource.getPassword());
            assertNotNull(actualInfluxDataFeedSource.getUserName());
        } else if (dataFeedSourceType == DataFeedSourceType.MONGO_DB) {
            final MongoDbDataFeedSource expMongoDataFeedSource =
                (MongoDbDataFeedSource) expectedDataFeed.getSource();
            final MongoDbDataFeedSource actualMongoDataFeedSource =
                (MongoDbDataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualMongoDataFeedSource.getConnectionString());
            assertEquals(expMongoDataFeedSource.getDatabase(), actualMongoDataFeedSource.getDatabase());
            assertEquals(expMongoDataFeedSource.getCommand(), actualMongoDataFeedSource.getCommand());
        } else if (dataFeedSourceType == DataFeedSourceType.MYSQL_DB) {
            final MySqlDataFeedSource expMySqlDataFeedSource = (MySqlDataFeedSource) expectedDataFeed.getSource();
            final MySqlDataFeedSource actualMySqlDataFeedSource = (MySqlDataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualMySqlDataFeedSource.getConnectionString());
            assertEquals(expMySqlDataFeedSource.getQuery(), actualMySqlDataFeedSource.getQuery());
        } else if (dataFeedSourceType == DataFeedSourceType.POSTGRE_SQL_DB) {
            final PostgreSqlDataFeedSource expPostGreDataFeedSource =
                (PostgreSqlDataFeedSource) expectedDataFeed.getSource();
            final PostgreSqlDataFeedSource actualPostGreDataFeedSource =
                (PostgreSqlDataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualPostGreDataFeedSource.getConnectionString());
            assertEquals(expPostGreDataFeedSource.getQuery(), actualPostGreDataFeedSource.getQuery());
        } else if (dataFeedSourceType == DataFeedSourceType.SQL_SERVER_DB) {
            final SqlServerDataFeedSource expSqlServerDataFeedSource =
                (SqlServerDataFeedSource) expectedDataFeed.getSource();
            final SqlServerDataFeedSource actualSqlServerDataFeedSource =
                (SqlServerDataFeedSource) actualDataFeed.getSource();
            // connection string is no longer returned from the service.
            // assertNotNull(actualSqlServerDataFeedSource.getConnectionString());
            assertEquals(expSqlServerDataFeedSource.getQuery(), actualSqlServerDataFeedSource.getQuery());
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_COSMOS_DB) {
            final AzureCosmosDbDataFeedSource expCosmosDataFeedSource =
                (AzureCosmosDbDataFeedSource) expectedDataFeed.getSource();
            final AzureCosmosDbDataFeedSource actualCosmosDataFeedSource =
                (AzureCosmosDbDataFeedSource) actualDataFeed.getSource();
            assertEquals(expCosmosDataFeedSource.getCollectionId(), actualCosmosDataFeedSource.getCollectionId());
            // assertNotNull(actualCosmosDataFeedSource.getConnectionString());
            assertEquals(expCosmosDataFeedSource.getDatabase(), actualCosmosDataFeedSource.getDatabase());
            assertEquals(expCosmosDataFeedSource.getSqlQuery(), actualCosmosDataFeedSource.getSqlQuery());
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2) {
            final AzureDataLakeStorageGen2DataFeedSource expDataLakeStorageGen2DataFeedSource =
                (AzureDataLakeStorageGen2DataFeedSource) expectedDataFeed.getSource();
            final AzureDataLakeStorageGen2DataFeedSource actualDataLakeFeedSource =
                (AzureDataLakeStorageGen2DataFeedSource) actualDataFeed.getSource();
            // assertNotNull(actualDataLakeFeedSource.getAccountKey());
            assertNotNull(actualDataLakeFeedSource.getAccountName());
            assertEquals(expDataLakeStorageGen2DataFeedSource.getDirectoryTemplate(),
                actualDataLakeFeedSource.getDirectoryTemplate());
            assertEquals(expDataLakeStorageGen2DataFeedSource.getFileSystemName(),
                actualDataLakeFeedSource.getFileSystemName());
            assertEquals(expDataLakeStorageGen2DataFeedSource.getFileTemplate(),
                actualDataLakeFeedSource.getFileTemplate());
        } else if (dataFeedSourceType == DataFeedSourceType.AZURE_LOG_ANALYTICS) {
            final AzureLogAnalyticsDataFeedSource expLogAnalyticsDataFeedSource =
                (AzureLogAnalyticsDataFeedSource) expectedDataFeed.getSource();
            final AzureLogAnalyticsDataFeedSource logAnalyticsDataFeedSource =
                (AzureLogAnalyticsDataFeedSource) actualDataFeed.getSource();
            assertNotNull(logAnalyticsDataFeedSource.getQuery());
            assertNotNull(logAnalyticsDataFeedSource.getClientId());
            assertNotNull(logAnalyticsDataFeedSource.getTenantId());
//            assertEquals(expLogAnalyticsDataFeedSource.getClientId(),
//                logAnalyticsDataFeedSource.getClientId());
//            assertEquals(expLogAnalyticsDataFeedSource.getTenantId(),
//                logAnalyticsDataFeedSource.getTenantId());
            assertEquals(expLogAnalyticsDataFeedSource.getQuery(), LOG_ANALYTICS_QUERY);
        } else {
            throw new IllegalStateException("Unexpected value: " + dataFeedSourceType);
        }
    }

    private void validateDataFeedOptions(DataFeedOptions expectedOptions, DataFeedOptions actualOptions) {
        if (expectedOptions != null) {
            assertEquals(expectedOptions.getDescription(), actualOptions.getDescription());
            assertEquals(expectedOptions.getActionLinkTemplate(), actualOptions.getActionLinkTemplate());
            assertIterableEquals(expectedOptions.getAdmins(), actualOptions.getAdmins());
            assertIterableEquals(expectedOptions.getViewers(), actualOptions.getViewers());
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
