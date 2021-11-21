// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.util.UUID;

/**
 * Async sample demonstrates how to create, get, update, delete and list datasource credential entity.
 */
public class DatasourceCredentialSample {
    public static void main(String[] args) {
        final MetricsAdvisorAdministrationClient advisorAdministrationClient =
            new MetricsAdvisorAdministrationClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

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

        DataSourceCredentialEntity createdDataSourceCredentialEntity = advisorAdministrationClient
            .createDataSourceCredential(datasourceCredential);


        System.out.printf("Created Datasource credential entity: %s%n", createdDataSourceCredentialEntity.getName());

        // Retrieve the datasource credential entity that just created.
        DataSourceCredentialEntity fetchDatasourceCredEntity
            = advisorAdministrationClient.getDataSourceCredential(createdDataSourceCredentialEntity.getId());
        System.out.printf("Fetched Datasource credential entity%n");

        System.out.printf("Datasource credential entity Id : %s%n", fetchDatasourceCredEntity.getId());
        System.out.printf("Datasource credential entity name : %s%n", fetchDatasourceCredEntity.getName());
        if (fetchDatasourceCredEntity instanceof DataSourceServicePrincipalInKeyVault) {
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) fetchDatasourceCredEntity;
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

        // Update the datasource credential entity.
        DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
        if (fetchDatasourceCredEntity instanceof DataSourceServicePrincipalInKeyVault) {
            actualCredentialSPInKV = (DataSourceServicePrincipalInKeyVault) fetchDatasourceCredEntity;
        }

        DataSourceCredentialEntity updatedDatasourceCred =
            advisorAdministrationClient.updateDataSourceCredential(
                actualCredentialSPInKV.setSecretNameForDataSourceClientId("clientIdSecretName"));

        System.out.printf("Updated datasource credential entity%n");
        System.out.printf("Updated datasource credential entity client Id: %s%n",
            ((DataSourceServicePrincipalInKeyVault) updatedDatasourceCred)
                .getSecretNameForDataSourceClientId());


        // Delete the datasource credential entity.
        advisorAdministrationClient.deleteDataSourceCredential(fetchDatasourceCredEntity.getId());

        System.out.printf("Deleted datasource credential entity%n");

        // List datasource credential entity.
        System.out.printf("Listing datasource credential entity%n");
        advisorAdministrationClient.listDataSourceCredentials()
            .forEach(datasourceCredentialEntity -> {
                System.out.printf("Datasource credential entity Id: %s%n", datasourceCredentialEntity.getId());
                System.out.printf("Datasource credential entity name: %s%n", datasourceCredentialEntity.getName());
                System.out.printf("Datasource credential entity description: %s%n",
                    datasourceCredentialEntity.getDescription());
                if (datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault) {
                    DataSourceServicePrincipalInKeyVault actualCredentialSPInKVItem
                        = (DataSourceServicePrincipalInKeyVault) datasourceCredentialEntity;
                    System.out
                        .printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKVItem.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault client Id: %s%n",
                        actualCredentialSPInKVItem.getKeyVaultClientId());
                    System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                        actualCredentialSPInKVItem.getSecretNameForDataSourceClientId());
                    System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                        actualCredentialSPInKVItem.getSecretNameForDataSourceClientSecret());
                }
            });
    }
}
