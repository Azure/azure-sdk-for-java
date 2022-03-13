// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataSourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DataSourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DataSourceSqlServerConnectionString;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_DATALAKEGEN2_ACCOUNT_KEY;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.BLOB_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.DATA_EXPLORER_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.DIRECTORY_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.FILE_TEMPLATE;
import static com.azure.ai.metricsadvisor.TestUtils.SQL_SERVER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.TEMPLATE_QUERY;
import static com.azure.ai.metricsadvisor.TestUtils.TEST_DB_NAME;

public class DataFeedWithCredentialsTest extends DataFeedWithCredentialsTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testSqlServer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
        List<String> credIds = new ArrayList<>();

        DataFeed dataFeed = super.initDataFeed();
        try {
            // Create SqlFeed with basic credentials in connection string.
            dataFeed.setSource(SqlServerDataFeedSource.fromBasicCredential(
                SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));

            DataFeed createdDataFeed = client.createDataFeed(dataFeed);
            Assertions.assertTrue(createdDataFeed.getSource() instanceof SqlServerDataFeedSource);
            Assertions.assertNull(((SqlServerDataFeedSource) createdDataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.BASIC,
                ((SqlServerDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
            dataFeed = createdDataFeed;

            // Update SqlFeed to use MSI.
            dataFeed
                .setSource(SqlServerDataFeedSource.fromManagedIdentityCredential(SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));

            DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
            Assertions.assertTrue(updatedDataFeed.getSource() instanceof SqlServerDataFeedSource);
            Assertions.assertNull(((SqlServerDataFeedSource) updatedDataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.MANAGED_IDENTITY,
                ((SqlServerDataFeedSource) updatedDataFeed.getSource()).getAuthenticationType());
            dataFeed = updatedDataFeed;

            // Create SqlConnStr cred and update DataFeed to use it.
            dataFeed = sqlServerWithConnStringCred(client, dataFeed, credIds);

            // Create SP credential and Update SqlFeed to use it.
            dataFeed = sqlServerWithServicePrincipalCred(client, dataFeed, credIds);

            // Create SPInKV credential and Update SqlFeed to use it.
            dataFeed = sqlServerWithServicePrincipalInKVCred(client, dataFeed, credIds);
        } finally {
            try {
                client.deleteDataFeed(dataFeed.getId());
            } finally {
                credIds.forEach(credentialId -> client.deleteDataSourceCredential(credentialId));
            }
        }
    }

    private DataFeed sqlServerWithConnStringCred(MetricsAdvisorAdministrationClient client,
                                                 DataFeed dataFeed,
                                                 List<String> credIds) {
        DataSourceSqlServerConnectionString sqlConStrCred = initDatasourceSqlServerConnectionString();
        final DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(sqlConStrCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceSqlServerConnectionString);
        credIds.add(createdCredential.getId());
        sqlConStrCred = (DataSourceSqlServerConnectionString) createdCredential;

        dataFeed.setSource(SqlServerDataFeedSource.fromConnectionStringCredential(
            TEMPLATE_QUERY,
            sqlConStrCred.getId()));

        final DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateSqlServerFeedWithCredential(updatedDataFeed, sqlConStrCred);
        return updatedDataFeed;
    }

    private DataFeed sqlServerWithServicePrincipalCred(MetricsAdvisorAdministrationClient client,
                                                       DataFeed dataFeed,
                                                       List<String> credIds) {
        DataSourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = ((DataSourceServicePrincipal) createdCredential);

        dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalCredential(
            SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY,
            servicePrincipalCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateSqlServerFeedWithCredential(updatedDataFeed, servicePrincipalCred);
        return updatedDataFeed;
    }

    private DataFeed sqlServerWithServicePrincipalInKVCred(MetricsAdvisorAdministrationClient client,
                                                           DataFeed dataFeed,
                                                           List<String> credIds) {
        DataSourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DataSourceServicePrincipalInKeyVault) createdCredential;

        dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
            SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY,
            servicePrincipalInKVCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateSqlServerFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred);
        return updatedDataFeed;
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testDataLakeGen2(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
        List<String> credIds = new ArrayList<>();

        DataFeed dataFeed = initDataFeed();
        try {
            // Create DataLakeFeed with basic credentials in key.
            dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromBasicCredential(
                "adsampledatalakegen2",
                AZURE_DATALAKEGEN2_ACCOUNT_KEY,
                TEST_DB_NAME,
                DIRECTORY_TEMPLATE,
                FILE_TEMPLATE));

            DataFeed createdDataFeed = client.createDataFeed(dataFeed);
            Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureDataLakeStorageGen2DataFeedSource);
            Assertions.assertNull(
                ((AzureDataLakeStorageGen2DataFeedSource) createdDataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.BASIC,
                ((AzureDataLakeStorageGen2DataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
            dataFeed = createdDataFeed;

            // Create SharedKey credential and Update DataLakeFeed to use it.
            dataFeed = dataLakeWithSharedKeyCred(client, dataFeed, credIds);

            // Create SP credential and Update SqlFeed to use it.
            dataFeed = dataLakeWithServicePrincipalCred(client, dataFeed, credIds);

            // Create SPInKV credential and Update SqlFeed to use it.
            dataFeed = dataLakeWithServicePrincipalInKVCred(client, dataFeed, credIds);
        } finally {
            try {
                client.deleteDataFeed(dataFeed.getId());
            } finally {
                credIds.forEach(credentialId ->
                    client.deleteDataSourceCredential(credentialId));
            }
        }
    }

    private DataFeed dataLakeWithSharedKeyCred(MetricsAdvisorAdministrationClient client,
                                               DataFeed dataFeed,
                                               List<String> credIds) {
        DataSourceDataLakeGen2SharedKey sharedKeyCred = initDataSourceDataLakeGen2SharedKey();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(sharedKeyCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceDataLakeGen2SharedKey);
        credIds.add(createdCredential.getId());
        sharedKeyCred = (DataSourceDataLakeGen2SharedKey) createdCredential;

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromSharedKeyCredential("adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            sharedKeyCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateDataLakeFeedWithCredential(updatedDataFeed, sharedKeyCred);
        return updatedDataFeed;
    }

    private DataFeed dataLakeWithServicePrincipalCred(MetricsAdvisorAdministrationClient client,
                                                      DataFeed dataFeed,
                                                      List<String> credIds) {
        DataSourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = (DataSourceServicePrincipal) createdCredential;

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalCredential(
            "adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            servicePrincipalCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateDataLakeFeedWithCredential(updatedDataFeed, servicePrincipalCred);
        return updatedDataFeed;
    }

    private DataFeed dataLakeWithServicePrincipalInKVCred(MetricsAdvisorAdministrationClient client,
                                                          DataFeed dataFeed,
                                                          List<String> credIds) {
        DataSourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DataSourceServicePrincipalInKeyVault) createdCredential;

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalInKeyVaultCredential(
            "adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            servicePrincipalInKVCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateDataLakeFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred);
        return updatedDataFeed;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testDataExplorer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
        List<String> credIds = new ArrayList<>();

        DataFeed dataFeed = initDataFeed();
        try {
            // Create SqlFeed with basic credentials in connection string.
            dataFeed.setSource(AzureDataExplorerDataFeedSource.fromBasicCredential(
                DATA_EXPLORER_CONNECTION_STRING,
                DATA_EXPLORER_QUERY));

            DataFeed createdDataFeed = client.createDataFeed(dataFeed);
            Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureDataExplorerDataFeedSource);
            Assertions.assertNull(((AzureDataExplorerDataFeedSource) createdDataFeed.getSource())
                .getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.BASIC,
                ((AzureDataExplorerDataFeedSource) createdDataFeed.getSource())
                    .getAuthenticationType());
            dataFeed = createdDataFeed;


            // Update DataExplorerFeed to use MSI.
            dataFeed
                .setSource(AzureDataExplorerDataFeedSource.fromManagedIdentityCredential(
                    DATA_EXPLORER_CONNECTION_STRING,
                    DATA_EXPLORER_QUERY));

            DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
            Assertions.assertTrue(updatedDataFeed.getSource() instanceof AzureDataExplorerDataFeedSource);
            Assertions.assertNull(((AzureDataExplorerDataFeedSource) updatedDataFeed.getSource())
                .getCredentialId());
            Assertions.assertEquals(DataSourceAuthenticationType.MANAGED_IDENTITY,
                ((AzureDataExplorerDataFeedSource) updatedDataFeed.getSource())
                    .getAuthenticationType());
            dataFeed = updatedDataFeed;


            // Create SP credential and Update DataExplorerFeed to use it.
            dataFeed = dataExplorerWithServicePrincipalCred(client, dataFeed, credIds);

            // Create SPInKV credential and Update DataExplorerFeed to use it.
            dataFeed = dataExplorerWithServicePrincipalInKVCred(client, dataFeed, credIds);
        } finally {
            try {
                client.deleteDataFeed(dataFeed.getId());
            } finally {
                credIds.forEach(credentialId -> client.deleteDataSourceCredential(credentialId));
            }
        }
    }

    private DataFeed dataExplorerWithServicePrincipalCred(MetricsAdvisorAdministrationClient client,
                                                          DataFeed dataFeed,
                                                          List<String> credIds) {
        DataSourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();

        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalCred);

        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = (DataSourceServicePrincipal) createdCredential;

        dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalCredential(
            DATA_EXPLORER_CONNECTION_STRING,
            DATA_EXPLORER_QUERY,
            servicePrincipalCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateDataExplorerFeedWithCredential(updatedDataFeed, servicePrincipalCred);
        return updatedDataFeed;
    }

    private DataFeed dataExplorerWithServicePrincipalInKVCred(MetricsAdvisorAdministrationClient client,
                                                              DataFeed dataFeed,
                                                              List<String> credIds) {
        DataSourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DataSourceCredentialEntity createdCredential = client.createDataSourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DataSourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DataSourceServicePrincipalInKeyVault) createdCredential;


        dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
            DATA_EXPLORER_CONNECTION_STRING,
            DATA_EXPLORER_QUERY,
            servicePrincipalInKVCred.getId()));

        DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
        super.validateDataExplorerFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred);
        return updatedDataFeed;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testBlobStorage(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
        DataFeed dataFeed = initDataFeed();
        try {
            // Create BlobFeed with basic credentials in connection string.
            dataFeed.setSource(AzureBlobDataFeedSource.fromBasicCredential(
                BLOB_CONNECTION_STRING,
                TEST_DB_NAME, BLOB_TEMPLATE));

            DataFeed createdDataFeed = client.createDataFeed(dataFeed);
            Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureBlobDataFeedSource);
            Assertions.assertEquals(DataSourceAuthenticationType.BASIC,
                ((AzureBlobDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
            dataFeed = createdDataFeed;

            // Update BlobFeed to use MSI.
            dataFeed
                .setSource(AzureBlobDataFeedSource.fromManagedIdentityCredential(BLOB_CONNECTION_STRING,
                    TEST_DB_NAME, BLOB_TEMPLATE));

            DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
            Assertions.assertTrue(updatedDataFeed.getSource() instanceof AzureBlobDataFeedSource);
            Assertions.assertEquals(DataSourceAuthenticationType.MANAGED_IDENTITY,
                ((AzureBlobDataFeedSource) updatedDataFeed.getSource()).getAuthenticationType());
            dataFeed = updatedDataFeed;
        } finally {
            client.deleteDataFeed(dataFeed.getId());
        }
    }
}
