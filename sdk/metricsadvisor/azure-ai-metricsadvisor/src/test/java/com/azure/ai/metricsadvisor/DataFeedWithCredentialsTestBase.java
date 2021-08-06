// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataSourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DataSourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DataSourceSqlServerConnectionString;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.UUID;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_DATALAKEGEN2_ACCOUNT_KEY;
import static com.azure.ai.metricsadvisor.TestUtils.INGESTION_START_TIME;
import static com.azure.ai.metricsadvisor.TestUtils.SQL_SERVER_CONNECTION_STRING;

public abstract class DataFeedWithCredentialsTestBase extends MetricsAdvisorAdministrationClientTestBase {
    static final String SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_sql_con";
    static final String DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX
        = "java_create_data_source_cred_dlake_gen";
    static final String SP_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_sp";
    static final String SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_spkv";

    @Test
    abstract void testSqlServer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testDataLakeGen2(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    abstract void testDataExplorer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    abstract void testBlobStorage(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    protected void validateSqlServerFeedWithCredential(DataFeed dataFeed,
                                                       DataSourceCredentialEntity credential) {
        Assertions.assertTrue(dataFeed.getSource() instanceof SqlServerDataFeedSource);
        if (credential instanceof DataSourceSqlServerConnectionString) {
            Assertions.assertTrue(dataFeed.getSource() instanceof SqlServerDataFeedSource);
            Assertions.assertEquals(credential.getId(),
                ((SqlServerDataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING,
                ((SqlServerDataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else if (credential instanceof DataSourceServicePrincipal) {
            Assertions.assertEquals(credential.getId(),
                ((SqlServerDataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL,
                ((SqlServerDataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else if (credential instanceof DataSourceServicePrincipalInKeyVault) {
            Assertions.assertEquals(credential.getId(),
                ((SqlServerDataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV,
                ((SqlServerDataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else {
            throw new IllegalStateException("Unexpected cred type for SqlFeed credential: " + credential);
        }
    }

    protected void validateDataLakeFeedWithCredential(DataFeed dataFeed,
                                                       DataSourceCredentialEntity credential) {
        Assertions.assertTrue(dataFeed.getSource() instanceof AzureDataLakeStorageGen2DataFeedSource);
        if (credential instanceof DataSourceDataLakeGen2SharedKey) {
            Assertions.assertEquals(credential.getId(),
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY,
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else if (credential instanceof DataSourceServicePrincipal) {
            Assertions.assertEquals(credential.getId(),
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL,
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else if (credential instanceof DataSourceServicePrincipalInKeyVault) {
            Assertions.assertEquals(credential.getId(),
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV,
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else {
            throw new IllegalStateException("Unexpected cred type for DataLake credential: " + credential);
        }
    }

    protected void validateDataExplorerFeedWithCredential(DataFeed dataFeed,
                                                          DataSourceCredentialEntity credential) {
        Assertions.assertTrue(dataFeed.getSource() instanceof AzureDataExplorerDataFeedSource);
        if (credential instanceof DataSourceServicePrincipal) {
            Assertions.assertEquals(credential.getId(),
                ((AzureDataExplorerDataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL,
                ((AzureDataExplorerDataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else if (credential instanceof DataSourceServicePrincipalInKeyVault) {
            Assertions.assertEquals(credential.getId(),
                ((AzureDataExplorerDataFeedSource) dataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV,
                ((AzureDataExplorerDataFeedSource) dataFeed.getSource()).getAuthenticationType());
        } else {
            throw new IllegalStateException("Unexpected cred type for DataExplorer credential: " + credential);
        }
    }

    protected DataFeed initDataFeed() {
        return new DataFeed().setSchema(new DataFeedSchema(Arrays.asList(
            new DataFeedMetric("cost").setDisplayName("cost"),
            new DataFeedMetric("revenue").setDisplayName("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension("city").setDisplayName("city"),
                new DataFeedDimension("category").setDisplayName("category"))))
            .setName("java_create_data_feed_test_sample" + UUID.randomUUID())
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setIngestionSettings(new DataFeedIngestionSettings(INGESTION_START_TIME));
    }

    protected DataSourceSqlServerConnectionString initDatasourceSqlServerConnectionString() {
        final String name = SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
        return new DataSourceSqlServerConnectionString(name, SQL_SERVER_CONNECTION_STRING);
    }

    protected DataSourceDataLakeGen2SharedKey initDataSourceDataLakeGen2SharedKey() {
        final String name = DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
        return new DataSourceDataLakeGen2SharedKey(name, AZURE_DATALAKEGEN2_ACCOUNT_KEY);
    }

    protected DataSourceServicePrincipal initDatasourceServicePrincipal() {
        final String name = SP_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
        final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
        final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
        final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";
        return new DataSourceServicePrincipal(name, cId, tId, mockSecr);
    }

    protected DataSourceServicePrincipalInKeyVault initDatasourceServicePrincipalInKeyVault() {
        final StringBuilder kvEndpoint = new StringBuilder()
            .append("https://")
            .append(UUID.randomUUID())
            .append(".vault")
            .append(".azure.net");
        final String name = SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
        final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
        final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
        final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";

        return new DataSourceServicePrincipalInKeyVault()
            .setName(name)
            .setKeyVaultForDataSourceSecrets(kvEndpoint.toString(), cId, mockSecr)
            .setTenantId(tId)
            .setSecretNameForDataSourceClientId("DSClientID_1")
            .setSecretNameForDataSourceClientSecret("DSClientSer_1");
    }
}
