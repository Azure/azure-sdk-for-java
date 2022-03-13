// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Async sample demonstrates how to create, get, update, delete and list datasource credential entity.
 */
public class DatasourceCredentialAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationAsyncClient advisorAdministrationAsyncClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        // Create Datasource credential entity
        System.out.printf("Creating Datasource Credential entity%n");

        final String name = "sample_name" + UUID.randomUUID();
        final String cId = "f45668b2-bffa-11eb-8529-0246ac130003";
        final String tId = "67890ded-5e07-4e52-b225-4ae8f905afb5";
        final String mockSecr = "890hy69-5e07-4e52-b225-4ae8f905afb5";

        DataSourceCredentialEntity datasourceCredential = new DataSourceServicePrincipalInKeyVault()
            .setName(name)
            .setKeyVaultForDataSourceSecrets("kv", cId, mockSecr)
            .setTenantId(tId)
            .setSecretNameForDataSourceClientId("DSClientID_1")
            .setSecretNameForDataSourceClientSecret("DSClientSer_1");

        final Mono<DataSourceCredentialEntity> createdDatasourceCredentialEntityMono = advisorAdministrationAsyncClient
            .createDataSourceCredential(datasourceCredential);

        createdDatasourceCredentialEntityMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating Datasource credential entity%n"))
            .doOnSuccess(datasourceCredentialEntity ->
                System.out.printf("Created Datasource credential entity: %s%n", datasourceCredentialEntity.getId()));

        // Retrieve the datasource credential entity that just created.
        Mono<DataSourceCredentialEntity> fetchDataFeedMono =
            createdDatasourceCredentialEntityMono.flatMap(createdDatasourceCredEntity -> {
                return advisorAdministrationAsyncClient.getDataSourceCredential(createdDatasourceCredEntity.getId())
                    .doOnSubscribe(__ ->
                        System.out
                            .printf("Fetching Datasource credential entity: %s%n", createdDatasourceCredEntity.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched Datasource credential entity%n"))
                    .doOnNext(credentialEntity -> {
                        System.out.printf("Datasource credential entity Id : %s%n", credentialEntity.getId());
                        System.out.printf("Datasource credential entity name : %s%n", credentialEntity.getName());
                        if (credentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                                = (DataSourceServicePrincipalInKeyVault) credentialEntity;
                            System.out
                                .printf("Actual credential entity key vault endpoint: %s%n",
                                    actualCredentialSPInKV.getKeyVaultEndpoint());
                            System.out.printf("Actual credential entity key vault client Id: %s%n",
                                actualCredentialSPInKV.getKeyVaultClientId());
                            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
                            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
                        }
                    });
            });

        // Update the datasource credential entity.
        Mono<DataSourceCredentialEntity> updateDatasourcCredMono = fetchDataFeedMono
            .flatMap(datasourceCredEntity -> {
                DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
                if (datasourceCredEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    actualCredentialSPInKV = (DataSourceServicePrincipalInKeyVault) datasourceCredEntity;
                }

                return advisorAdministrationAsyncClient.updateDataSourceCredential(
                    actualCredentialSPInKV.setSecretNameForDataSourceClientId("clientIdSecretName"))
                    .doOnSubscribe(__ ->
                        System.out.printf("Updating datasource credential entity: %s%n", datasourceCredEntity.getId()))
                    .doOnSuccess(config -> {

                        System.out.printf("Updated datasource credential entity%n");
                        System.out.printf("Updated datasource credential entity client Id: %s%n",
                            ((DataSourceServicePrincipalInKeyVault) datasourceCredEntity)
                                .getSecretNameForDataSourceClientId());
                    });
            });

        // Delete the datasource credential entity.
        Mono<Void> deleteDatasourceCredMono = updateDatasourcCredMono.flatMap(datasourceCredEntity -> {
            return advisorAdministrationAsyncClient.deleteDataSourceCredential(datasourceCredEntity.getId())
                .doOnSubscribe(__ ->
                    System.out.printf("Deleting datasource credential entity: %s%n", datasourceCredEntity.getId()))
                .doOnSuccess(config ->
                    System.out.printf("Deleted datasource credential entity%n"));
        });

        /*
          This will block until all the above CRUD on operation on email hook is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        deleteDatasourceCredMono.block();

        // List datasource credential entity.
        System.out.printf("Listing datasource credential entity%n");
        advisorAdministrationAsyncClient.listDataSourceCredentials()
            .doOnNext(datasourceCredentialEntity -> {
                System.out.printf("Datasource credential entity Id: %s%n", datasourceCredentialEntity.getId());
                System.out.printf("Datasource credential entity name: %s%n", datasourceCredentialEntity.getName());
                System.out.printf("Datasource credential entity description: %s%n",
                    datasourceCredentialEntity.getDescription());
                if (datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                        = (DataSourceServicePrincipalInKeyVault) datasourceCredentialEntity;
                    System.out
                        .printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKV.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault client Id: %s%n",
                        actualCredentialSPInKV.getKeyVaultClientId());
                    System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientId());
                    System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                        actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
                }
            });
    }
}
