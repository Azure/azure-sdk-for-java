// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.azure.core.test.TestBase.AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL;
import static com.azure.core.test.TestBase.getHttpClients;

/**
 * Utility class for common methods and constants used in test classes.
 */
public final class TestUtils {
    private static final String AZURE_METRICS_ADVISOR_TEST_SERVICE_VERSIONS =
        "AZURE_METRIC_ADVISOR_TEST_SERVICE_VERSIONS";
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String INVALID_ENDPOINT = "https://notreal.azure.com";

    static final String INCORRECT_UUID = "a0a3998a-4c4affe66b7";
    static final String INCORRECT_UUID_ERROR = "Invalid UUID string: " + INCORRECT_UUID;
    static final String DATAFEED_ID_REQUIRED_ERROR = "'dataFeedId' cannot be null.";
    static final OffsetDateTime INGESTION_START_TIME = OffsetDateTime.parse("2019-10-01T00:00:00Z");

    public static final String AZURE_METRICS_ADVISOR_ENDPOINT = "AZURE_METRICS_ADVISOR_ENDPOINT";

    static final String TEMPLATE_QUERY = "select * from adsample2 where Timestamp = @StartTime";
    static final String TABLE_QUERY = "PartitionKey ge '@StartTime' and PartitionKey lt '@EndTime'";
    static final String DATA_EXPLORER_QUERY = "let StartDateTime = datetime(@StartTime);"
        + "let EndDateTime = StartDateTime + 1d;"
        + "adsample| where Timestamp >= StartDateTime and Timestamp < EndDateTime";
    static final String APP_INSIGHTS_QUERY = "let gran=60m; let starttime=datetime(@StartTime); "
        + "let endtime=starttime + gran; requests | where timestamp >= starttime and timestamp < endtime "
        + "| summarize request_count = count(), duration_avg_ms = avg(duration), duration_95th_ms = percentile"
        + "(duration, 95), duration_max_ms = max(duration) by resultCode";
    static final String LOG_ANALYTICS_QUERY = "where StartTime >=datetime(@StartTime) and EndTime <datetime(@EndTime)"
        + "| summarize count_per_type=count() by DataType";
    static final String MONGO_COMMAND = "{\"find\": \"adsample\",\"filter\": { Timestamp: { $eq: @StartTime }}"
        + "\"batchSize\": 2000,}";
    static final String TEST_DB_NAME = "adsample";
    static final String BLOB_TEMPLATE = "%Y/%m/%d/%h/JsonFormatV2.json";
    static final String AZURE_CLOUD = "Azure Global";
    static final String DIRECTORY_TEMPLATE = "%Y/%m/%d";
    static final String FILE_TEMPLATE = "adsample.json";

    static final String SQL_SERVER_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_SQL_SERVER_CONNECTION_STRING", "conn-string");

    static final String BLOB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_AZURE_BLOB_CONNECTION_STRING", "con-string");

    static final String DATA_EXPLORER_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_AZURE_DATA_EXPLORER_CONNECTION_STRING", "con-string");

    static final String TABLE_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_AZURE_TABLE_CONNECTION_STRING", "con-string");

    static final String INFLUX_DB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_INFLUX_DB_CONNECTION_STRING", "con-string");

    static final String MONGO_DB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_AZURE_MONGODB_CONNECTION_STRING", "con-string");

    static final String POSTGRE_SQL_DB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_POSTGRESQL_CONNECTION_STRING", "con-string");

    static final String MYSQL_DB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_MYSQL_CONNECTION_STRING", "con-string");

    static final String COSMOS_DB_CONNECTION_STRING = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_COSMOS_DB_CONNECTION_STRING", "con-string");

    static final String APP_INSIGHTS_API_KEY = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_APPLICATION_INSIGHTS_API_KEY", "apiKey");

    static final String APP_INSIGHTS_APPLICATION_ID = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_APPLICATION_INSIGHTS_APPLICATION_ID", "applicationId");

    static final String INFLUX_DB_PASSWORD = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_INFLUX_DB_PASSWORD", "testPassword");

    static final String AZURE_DATALAKEGEN2_ACCOUNT_KEY = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_AZURE_DATALAKE_ACCOUNT_KEY", "azDataLakeAccountKey");

    static final String AZURE_METRICS_ADVISOR_TENANT_ID = Configuration
        .getGlobalConfiguration()
        .get("AZURE_CLIENT_ID", "azTenantId");

    static final String AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_ID = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_ID", "azClientId");

    static final String AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_SECRET = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_LOG_ANALYTICS_CLIENT_SECRET", "azClientSecret");

    static final String AZURE_METRICS_ADVISOR_LOG_ANALYTICS_WORKSPACE_ID = Configuration
        .getGlobalConfiguration()
        .get("AZURE_METRICS_ADVISOR_LOG_ANALYTICS_WORKSPACE_ID", "azWorkspaceId");

    static final long DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS = 60;

    private TestUtils() {
    }

    static DataFeed getSQLDataFeedSample() {
        return new DataFeed().setSource(SqlServerDataFeedSource.fromBasicCredential(SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY)).setSchema(new DataFeedSchema(Arrays.asList(
                new DataFeedMetric("cost"),
                new DataFeedMetric("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension("city"),
                new DataFeedDimension("category"))))
            .setName("java_SQL_create_data_feed_test_sample" + UUID.randomUUID())
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setIngestionSettings(new DataFeedIngestionSettings(INGESTION_START_TIME));
    }

    static DataFeed getAzureBlobDataFeedSample() {
        return new DataFeed().setSource(AzureBlobDataFeedSource.fromBasicCredential(BLOB_CONNECTION_STRING,
            "BLOB_CONTAINER", "BLOB_TEMPLATE_NAME")).setSchema(new DataFeedSchema(Arrays.asList(
                new DataFeedMetric("cost"),
                new DataFeedMetric("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension("city"),
                new DataFeedDimension("category"))))
            .setName("java_BLOB_create_data_feed_test_sample" + UUID.randomUUID())
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setIngestionSettings(new DataFeedIngestionSettings(INGESTION_START_TIME));
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients()
            .forEach(httpClient -> Arrays.stream(MetricsAdvisorServiceVersion.values()).filter(
                TestUtils::shouldServiceVersionBeTested)
                .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));
        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link MetricsAdvisorServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     * <p>
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(MetricsAdvisorServiceVersion serviceVersion) {
        String serviceVersionFromEnv =
            Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_TEST_SERVICE_VERSIONS);
        if (CoreUtils.isNullOrEmpty(serviceVersionFromEnv)) {
            return MetricsAdvisorServiceVersion.getLatest() == serviceVersion;
        }
        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(serviceVersionFromEnv)) {
            return true;
        }
        String[] configuredServiceVersionList = serviceVersionFromEnv.split(",");
        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }
}
