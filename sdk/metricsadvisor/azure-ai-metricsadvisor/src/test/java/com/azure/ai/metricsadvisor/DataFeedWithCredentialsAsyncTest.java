// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DatasourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DatasourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DatasourceSqlServerConnectionString;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

public class DataFeedWithCredentialsAsyncTest extends DataFeedWithCredentialsTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testSqlServer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildAsyncClient();
        List<String> credIds = new ArrayList<>();

        final AtomicReference<DataFeed> dataFeed = new AtomicReference<>(initDataFeed());
        try {
            // Create SqlFeed with basic credentials in connection string.
            dataFeed.get().setSource(SqlServerDataFeedSource.fromBasicCredential(
                SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));

            StepVerifier.create(client.createDataFeed(dataFeed.get()))
                .assertNext(createdDataFeed -> {
                    dataFeed.set(createdDataFeed);
                    Assertions.assertTrue(createdDataFeed.getSource() instanceof SqlServerDataFeedSource);
                    Assertions.assertNull(((SqlServerDataFeedSource) createdDataFeed.getSource()).getCredentialId());
                    Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                        ((SqlServerDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
                })
                .verifyComplete();

            // Update SqlFeed to use MSI.
            dataFeed.get()
                .setSource(SqlServerDataFeedSource.fromManagedIdentityCredential(SQL_SERVER_CONNECTION_STRING,
                TEMPLATE_QUERY));

            StepVerifier.create(client.updateDataFeed(dataFeed.get()))
                .assertNext(updatedDataFeed -> {
                    dataFeed.set(updatedDataFeed);
                    Assertions.assertTrue(updatedDataFeed.getSource() instanceof SqlServerDataFeedSource);
                    Assertions.assertNull(((SqlServerDataFeedSource) updatedDataFeed.getSource()).getCredentialId());
                    Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
                        ((SqlServerDataFeedSource) updatedDataFeed.getSource()).getAuthenticationType());
                })
                .verifyComplete();


            // Create SqlConnStr cred and update DataFeed to use it.
            DataFeed d1 = sqlServerWithConnStringCred(client, dataFeed.get(), credIds);
            dataFeed.set(d1);

            // Create SP credential and Update SqlFeed to use it.
            DataFeed d2 = sqlServerWithServicePrincipalCred(client, dataFeed.get(), credIds);
            dataFeed.set(d2);

            // Create SPInKV credential and Update SqlFeed to use it.
            DataFeed d3 = sqlServerWithServicePrincipalInKVCred(client, dataFeed.get(), credIds);
            dataFeed.set(d3);
        } finally {
            try {
                StepVerifier.create(client.deleteDataFeed(dataFeed.get().getId())).verifyComplete();
            } finally {
                credIds.forEach(credentialId ->
                    StepVerifier.create(client.deleteDatasourceCredential(credentialId)).verifyComplete());
            }
        }
    }

    private DataFeed sqlServerWithConnStringCred(MetricsAdvisorAdministrationAsyncClient client,
                                                 DataFeed dataFeed,
                                                 List<String> credIds) {
        final AtomicReference<DatasourceSqlServerConnectionString> sqlConStrCred
            = new AtomicReference<>(initDatasourceSqlServerConnectionString());

        StepVerifier.create(client.createDatasourceCredential(sqlConStrCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceSqlServerConnectionString);
                credIds.add(createdCredential.getId());
                sqlConStrCred.set((DatasourceSqlServerConnectionString) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(SqlServerDataFeedSource.fromConnectionStringCredential(
            TEMPLATE_QUERY,
            sqlConStrCred.get().getId()));


        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateSqlServerFeedWithCredential(updatedDataFeed, sqlConStrCred.get());

            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    private DataFeed sqlServerWithServicePrincipalCred(MetricsAdvisorAdministrationAsyncClient client,
                                                       DataFeed dataFeed,
                                                       List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipal> servicePrincipalCred
            = new AtomicReference<>(initDatasourceServicePrincipal());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
                credIds.add(createdCredential.getId());
                servicePrincipalCred.set((DatasourceServicePrincipal) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalCredential(
            SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY,
            servicePrincipalCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateSqlServerFeedWithCredential(updatedDataFeed, servicePrincipalCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    private DataFeed sqlServerWithServicePrincipalInKVCred(MetricsAdvisorAdministrationAsyncClient client,
                                                           DataFeed dataFeed,
                                                           List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipalInKeyVault> servicePrincipalInKVCred
            = new AtomicReference<>(initDatasourceServicePrincipalInKeyVault());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalInKVCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
                credIds.add(createdCredential.getId());
                servicePrincipalInKVCred.set((DatasourceServicePrincipalInKeyVault) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
            SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY,
            servicePrincipalInKVCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateSqlServerFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testDataLakeGen2(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildAsyncClient();
        List<String> credIds = new ArrayList<>();

        final AtomicReference<DataFeed> dataFeed = new AtomicReference<>(initDataFeed());
        try {
            // Create DataLakeFeed with basic credentials in key.
            dataFeed.get().setSource(AzureDataLakeStorageGen2DataFeedSource.fromBasicCredential(
                "adsampledatalakegen2",
                AZURE_DATALAKEGEN2_ACCOUNT_KEY,
                TEST_DB_NAME,
                DIRECTORY_TEMPLATE,
                FILE_TEMPLATE));

            StepVerifier.create(client.createDataFeed(dataFeed.get()))
                .assertNext(createdDataFeed -> {
                    dataFeed.set(createdDataFeed);
                    Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureDataLakeStorageGen2DataFeedSource);
                    Assertions.assertNull(
                        ((AzureDataLakeStorageGen2DataFeedSource) createdDataFeed.getSource()).getCredentialId());
                    Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                        ((AzureDataLakeStorageGen2DataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
                })
                .verifyComplete();

            // Create SharedKey credential and Update DataLakeFeed to use it.
            DataFeed d1 = dataLakeWithSharedKeyCred(client, dataFeed.get(), credIds);
            dataFeed.set(d1);

            // Create SP credential and Update SqlFeed to use it.
            DataFeed d2 = dataLakeWithServicePrincipalCred(client, dataFeed.get(), credIds);
            dataFeed.set(d2);

            // Create SPInKV credential and Update SqlFeed to use it.
            DataFeed d3 = dataLakeWithServicePrincipalInKVCred(client, dataFeed.get(), credIds);
            dataFeed.set(d3);
        } finally {
            try {
                StepVerifier.create(client.deleteDataFeed(dataFeed.get().getId())).verifyComplete();
            } finally {
                credIds.forEach(credentialId ->
                    StepVerifier.create(client.deleteDatasourceCredential(credentialId)).verifyComplete());
            }
        }
    }

    private DataFeed dataLakeWithSharedKeyCred(MetricsAdvisorAdministrationAsyncClient client,
                                               DataFeed dataFeed,
                                               List<String> credIds) {
        final AtomicReference<DatasourceDataLakeGen2SharedKey> sharedKeyCred
            = new AtomicReference<>(initDataSourceDataLakeGen2SharedKey());

        StepVerifier.create(client.createDatasourceCredential(sharedKeyCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceDataLakeGen2SharedKey);
                credIds.add(createdCredential.getId());
                sharedKeyCred.set((DatasourceDataLakeGen2SharedKey) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromSharedKeyCredential("adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            sharedKeyCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateDataLakeFeedWithCredential(updatedDataFeed, sharedKeyCred.get());
            })
            .verifyComplete();

        return resultDataFeed.get();
    }

    private DataFeed dataLakeWithServicePrincipalCred(MetricsAdvisorAdministrationAsyncClient client,
                                                      DataFeed dataFeed,
                                                      List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipal> servicePrincipalCred
            = new AtomicReference<>(initDatasourceServicePrincipal());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
                credIds.add(createdCredential.getId());
                servicePrincipalCred.set((DatasourceServicePrincipal) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalCredential(
            "adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            servicePrincipalCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateDataLakeFeedWithCredential(updatedDataFeed, servicePrincipalCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    private DataFeed dataLakeWithServicePrincipalInKVCred(MetricsAdvisorAdministrationAsyncClient client,
                                                          DataFeed dataFeed,
                                                          List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipalInKeyVault> servicePrincipalInKVCred
            = new AtomicReference<>(initDatasourceServicePrincipalInKeyVault());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalInKVCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
                credIds.add(createdCredential.getId());
                servicePrincipalInKVCred.set((DatasourceServicePrincipalInKeyVault) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalInKeyVaultCredential(
            "adsampledatalakegen2",
            TEST_DB_NAME,
            DIRECTORY_TEMPLATE,
            FILE_TEMPLATE,
            servicePrincipalInKVCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateDataLakeFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testDataExplorer(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildAsyncClient();
        List<String> credIds = new ArrayList<>();

        final AtomicReference<DataFeed> dataFeed = new AtomicReference<>(initDataFeed());
        try {
            // Create SqlFeed with basic credentials in connection string.
            dataFeed.get().setSource(AzureDataExplorerDataFeedSource.fromBasicCredential(
                DATA_EXPLORER_CONNECTION_STRING,
                DATA_EXPLORER_QUERY));

            StepVerifier.create(client.createDataFeed(dataFeed.get()))
                .assertNext(createdDataFeed -> {
                    dataFeed.set(createdDataFeed);
                    Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureDataExplorerDataFeedSource);
                    Assertions.assertNull(((AzureDataExplorerDataFeedSource) createdDataFeed.getSource())
                        .getCredentialId());
                    Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                        ((AzureDataExplorerDataFeedSource) createdDataFeed.getSource())
                            .getAuthenticationType());
                })
                .verifyComplete();

            // Update DataExplorerFeed to use MSI.
            dataFeed.get()
                .setSource(AzureDataExplorerDataFeedSource.fromManagedIdentityCredential(
                    DATA_EXPLORER_CONNECTION_STRING,
                    DATA_EXPLORER_QUERY));

            StepVerifier.create(client.updateDataFeed(dataFeed.get()))
                .assertNext(updatedDataFeed -> {
                    dataFeed.set(updatedDataFeed);
                    Assertions.assertTrue(updatedDataFeed.getSource() instanceof AzureDataExplorerDataFeedSource);
                    Assertions.assertNull(((AzureDataExplorerDataFeedSource) updatedDataFeed.getSource())
                        .getCredentialId());
                    Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
                        ((AzureDataExplorerDataFeedSource) updatedDataFeed.getSource())
                            .getAuthenticationType());
                })
                .verifyComplete();

            // Create SP credential and Update DataExplorerFeed to use it.
            DataFeed d2 = dataExplorerWithServicePrincipalCred(client, dataFeed.get(), credIds);
            dataFeed.set(d2);

            // Create SPInKV credential and Update DataExplorerFeed to use it.
            DataFeed d3 = dataExplorerWithServicePrincipalInKVCred(client, dataFeed.get(), credIds);
            dataFeed.set(d3);
        } finally {
            try {
                StepVerifier.create(client.deleteDataFeed(dataFeed.get().getId())).verifyComplete();
            } finally {
                credIds.forEach(credentialId ->
                    StepVerifier.create(client.deleteDatasourceCredential(credentialId)).verifyComplete());
            }
        }
    }

    private DataFeed dataExplorerWithServicePrincipalCred(MetricsAdvisorAdministrationAsyncClient client,
                                                          DataFeed dataFeed,
                                                          List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipal> servicePrincipalCred
            = new AtomicReference<>(initDatasourceServicePrincipal());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipal);
                credIds.add(createdCredential.getId());
                servicePrincipalCred.set((DatasourceServicePrincipal) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalCredential(
            DATA_EXPLORER_CONNECTION_STRING,
            DATA_EXPLORER_QUERY,
            servicePrincipalCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateDataExplorerFeedWithCredential(updatedDataFeed, servicePrincipalCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    private DataFeed dataExplorerWithServicePrincipalInKVCred(MetricsAdvisorAdministrationAsyncClient client,
                                                              DataFeed dataFeed,
                                                              List<String> credIds) {
        final AtomicReference<DatasourceServicePrincipalInKeyVault> servicePrincipalInKVCred
            = new AtomicReference<>(initDatasourceServicePrincipalInKeyVault());

        StepVerifier.create(client.createDatasourceCredential(servicePrincipalInKVCred.get()))
            .assertNext(createdCredential -> {
                Assertions.assertTrue(createdCredential instanceof DatasourceServicePrincipalInKeyVault);
                credIds.add(createdCredential.getId());
                servicePrincipalInKVCred.set((DatasourceServicePrincipalInKeyVault) createdCredential);
            })
            .verifyComplete();

        dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
            DATA_EXPLORER_CONNECTION_STRING,
            DATA_EXPLORER_QUERY,
            servicePrincipalInKVCred.get().getId()));

        final AtomicReference<DataFeed> resultDataFeed = new AtomicReference<>();
        StepVerifier.create(client.updateDataFeed(dataFeed))
            .assertNext(updatedDataFeed -> {
                resultDataFeed.set(updatedDataFeed);
                super.validateDataExplorerFeedWithCredential(updatedDataFeed, servicePrincipalInKVCred.get());
            })
            .verifyComplete();
        return resultDataFeed.get();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void testBlobStorage(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildAsyncClient();
        final AtomicReference<DataFeed> dataFeed = new AtomicReference<>(initDataFeed());
        try {
            // Create BlobFeed with basic credentials in connection string.
            dataFeed.get().setSource(AzureBlobDataFeedSource.fromBasicCredential(
                BLOB_CONNECTION_STRING,
                TEST_DB_NAME, BLOB_TEMPLATE));

            StepVerifier.create(client.createDataFeed(dataFeed.get()))
                .assertNext(createdDataFeed -> {
                    dataFeed.set(createdDataFeed);
                    Assertions.assertTrue(createdDataFeed.getSource() instanceof AzureBlobDataFeedSource);
                    Assertions.assertEquals(DatasourceAuthenticationType.BASIC,
                        ((AzureBlobDataFeedSource) createdDataFeed.getSource()).getAuthenticationType());
                })
                .verifyComplete();

            // Update BlobFeed to use MSI.
            dataFeed.get()
                .setSource(AzureBlobDataFeedSource.fromManagedIdentityCredential(BLOB_CONNECTION_STRING,
                    TEST_DB_NAME, BLOB_TEMPLATE));

            StepVerifier.create(client.updateDataFeed(dataFeed.get()))
                .assertNext(updatedDataFeed -> {
                    dataFeed.set(updatedDataFeed);
                    Assertions.assertTrue(updatedDataFeed.getSource() instanceof AzureBlobDataFeedSource);
                    Assertions.assertEquals(DatasourceAuthenticationType.MANAGED_IDENTITY,
                        ((AzureBlobDataFeedSource) updatedDataFeed.getSource()).getAuthenticationType());
                })
                .verifyComplete();
        } finally {
            StepVerifier.create(client.deleteDataFeed(dataFeed.get().getId())).verifyComplete();
        }
    }
}
