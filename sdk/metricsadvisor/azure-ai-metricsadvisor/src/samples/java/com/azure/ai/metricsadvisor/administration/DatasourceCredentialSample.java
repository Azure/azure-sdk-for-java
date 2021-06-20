// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;
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

        DatasourceCredentialEntity datasourceCredential = new DatasourceServicePrincipalInKeyVault()
            .setName(name)
            .setKeyVaultForDatasourceSecrets("kv", cId, mockSecr)
            .setTenantId(tId)
            .setSecretNameForDatasourceClientId("DSClientID_1")
            .setSecretNameForDatasourceClientSecret("DSClientSer_1");

        DatasourceCredentialEntity createdDatasourceCredentialEntity = advisorAdministrationClient
            .createDatasourceCredential(datasourceCredential);


        System.out.printf("Created Datasource credential entity: %s");

        // Retrieve the datasource credential entity that just created.
        DatasourceCredentialEntity fetchDatasourceCredEntity
            = advisorAdministrationClient.getDatasourceCredential(createdDatasourceCredentialEntity.getId());
        System.out.printf("Fetched Datasource credential entity%n");

        System.out.printf("Datasource credential entity Id : %s%n", fetchDatasourceCredEntity.getId());
        System.out.printf("Datasource credential entity name : %s%n", fetchDatasourceCredEntity.getName());
        if (fetchDatasourceCredEntity instanceof DatasourceServicePrincipalInKeyVault) {
            DatasourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DatasourceServicePrincipalInKeyVault) fetchDatasourceCredEntity;
            System.out
                .printf("Actual credential entity key vault endpoint: %s%n",
                    actualCredentialSPInKV.getKeyVaultEndpoint());
            System.out.printf("Actual credential entity key vault client Id: %s%n",
                actualCredentialSPInKV.getKeyVaultClientId());
            System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDatasourceClientId());
            System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                actualCredentialSPInKV.getSecretNameForDatasourceClientSecret());
        }

        // Update the datasource credential entity.
        DatasourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
        if (fetchDatasourceCredEntity instanceof DatasourceServicePrincipalInKeyVault) {
            actualCredentialSPInKV = (DatasourceServicePrincipalInKeyVault) fetchDatasourceCredEntity;
        }

        DatasourceCredentialEntity updatedDatasourceCred =
            advisorAdministrationClient.updateDatasourceCredential(
                actualCredentialSPInKV.setSecretNameForDatasourceClientId("clientIdSecretName"));

        System.out.printf("Updated datasource credential entity%n");
        System.out.printf("Updated datasource credential entity client Id: %s%n",
            ((DatasourceServicePrincipalInKeyVault) updatedDatasourceCred)
                .getSecretNameForDatasourceClientId());


        // Delete the datasource credential entity.
        advisorAdministrationClient.deleteDatasourceCredential(fetchDatasourceCredEntity.getId());

        System.out.printf("Deleted datasource credential entity%n");

        // List datasource credential entity.
        System.out.printf("Listing datasource credential entity%n");
        advisorAdministrationClient.listDatasourceCredentials()
            .forEach(datasourceCredentialEntity -> {
                System.out.printf("Datasource credential entity Id: %s%n", datasourceCredentialEntity.getId());
                System.out.printf("Datasource credential entity name: %s%n", datasourceCredentialEntity.getName());
                System.out.printf("Datasource credential entity description: %s%n",
                    datasourceCredentialEntity.getDescription());
                if (datasourceCredentialEntity instanceof DatasourceServicePrincipalInKeyVault) {
                    DatasourceServicePrincipalInKeyVault actualCredentialSPInKVItem
                        = (DatasourceServicePrincipalInKeyVault) datasourceCredentialEntity;
                    System.out
                        .printf("Actual credential entity key vault endpoint: %s%n",
                            actualCredentialSPInKVItem.getKeyVaultEndpoint());
                    System.out.printf("Actual credential entity key vault client Id: %s%n",
                        actualCredentialSPInKVItem.getKeyVaultClientId());
                    System.out.printf("Actual credential entity key vault secret name for data source: %s%n",
                        actualCredentialSPInKVItem.getSecretNameForDatasourceClientId());
                    System.out.printf("Actual credential entity key vault secret for data source: %s%n",
                        actualCredentialSPInKVItem.getSecretNameForDatasourceClientSecret());
                }
            });
    }
}
