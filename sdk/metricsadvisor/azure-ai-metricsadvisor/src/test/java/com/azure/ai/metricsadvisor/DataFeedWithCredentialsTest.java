// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DatasourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DatasourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DatasourceSqlServerConnectionString;
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
            Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                ((SqlServerDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
            dataFeed = createdDataFeed;

            // Update SqlFeed to use MSI.
            dataFeed
                .setSource(SqlServerDataFeedSource.fromManagedIdentityCredential(SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));

            DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
            Assertions.assertTrue(updatedDataFeed.getSource() instanceof SqlServerDataFeedSource);
            Assertions.assertNull(((SqlServerDataFeedSource) updatedDataFeed.getSource()).getCredentialId());
            Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
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
                credIds.forEach(credentialId -> client.deleteDatasourceCredential(credentialId));
            }
        }
    }

    private DataFeed sqlServerWithConnStringCred(MetricsAdvisorAdministrationClient client,
                                                 DataFeed dataFeed,
                                                 List<String> credIds) {
        DatasourceSqlServerConnectionString sqlConStrCred = initDatasourceSqlServerConnectionString();
        final DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(sqlConStrCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceSqlServerConnectionString);
        credIds.add(createdCredential.getId());
        sqlConStrCred = (DatasourceSqlServerConnectionString) createdCredential;

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
        DatasourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = ((DatasourceServicePrincipal) createdCredential);

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
        DatasourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DatasourceServicePrincipalInKeyVault) createdCredential;

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
            Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
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
                    client.deleteDatasourceCredential(credentialId));
            }
        }
    }

    private DataFeed dataLakeWithSharedKeyCred(MetricsAdvisorAdministrationClient client,
                                               DataFeed dataFeed,
                                               List<String> credIds) {
        DatasourceDataLakeGen2SharedKey sharedKeyCred = initDataSourceDataLakeGen2SharedKey();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(sharedKeyCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceDataLakeGen2SharedKey);
        credIds.add(createdCredential.getId());
        sharedKeyCred = (DatasourceDataLakeGen2SharedKey) createdCredential;

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
        DatasourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = (DatasourceServicePrincipal) createdCredential;

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
        DatasourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DatasourceServicePrincipalInKeyVault) createdCredential;

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
            Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
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
            Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
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
                credIds.forEach(credentialId -> client.deleteDatasourceCredential(credentialId));
            }
        }
    }

    private DataFeed dataExplorerWithServicePrincipalCred(MetricsAdvisorAdministrationClient client,
                                                          DataFeed dataFeed,
                                                          List<String> credIds) {
        DatasourceServicePrincipal servicePrincipalCred = initDatasourceServicePrincipal();

        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalCred);

        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
        credIds.add(createdCredential.getId());
        servicePrincipalCred = (DatasourceServicePrincipal) createdCredential;

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
        DatasourceServicePrincipalInKeyVault servicePrincipalInKVCred = initDatasourceServicePrincipalInKeyVault();
        DatasourceCredentialEntity createdCredential = client.createDatasourceCredential(servicePrincipalInKVCred);
        Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
        credIds.add(createdCredential.getId());
        servicePrincipalInKVCred = (DatasourceServicePrincipalInKeyVault) createdCredential;


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
            Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                ((AzureBlobDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
            dataFeed = createdDataFeed;

            // Update BlobFeed to use MSI.
            dataFeed
                .setSource(AzureBlobDataFeedSource.fromManagedIdentityCredential(BLOB_CONNECTION_STRING,
                    TEST_DB_NAME, BLOB_TEMPLATE));

            DataFeed updatedDataFeed = client.updateDataFeed(dataFeed);
            Assertions.assertTrue(updatedDataFeed.getSource() instanceof AzureBlobDataFeedSource);
            Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
                ((AzureBlobDataFeedSource) updatedDataFeed.getSource()).getAuthenticationType());
            dataFeed = updatedDataFeed;
        } finally {
            client.deleteDataFeed(dataFeed.getId());
        }
    }
}
