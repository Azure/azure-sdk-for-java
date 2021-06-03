// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.DatasourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DatasourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DatasourceSqlServerConnectionString;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_DATALAKEGEN2_ACCOUNT_KEY;
import static com.azure.ai.metricsadvisor.TestUtils.SQL_SERVER_CONNECTION_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class DatasourceCredentialTestBase extends MetricsAdvisorAdministrationClientTestBase {
    static final String SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_sql_con";
    static final String DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX
        = "java_create_data_source_cred_dlake_gen";
    static final String SP_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_sp";
    static final String SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX = "java_create_data_source_cred_spkv";

    @Test
    abstract void createSqlConnectionString(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createDataLakeGen2SharedKey(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createServicePrincipal(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createServicePrincipalInKV(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListDataSourceCredentials(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    void listDatasourceCredentialRunner(Consumer<List<DatasourceCredentialEntity>> testRunner) {
        List<DatasourceCredentialEntity> list = new ArrayList<>();
        creatDatasourceCredentialRunner(datasource -> list.add(datasource),
            DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
        creatDatasourceCredentialRunner(datasource -> list.add(datasource),
            DatasourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
        testRunner.accept(list);
    }

    void creatDatasourceCredentialRunner(Consumer<DatasourceCredentialEntity> testRunner,
                                         DatasourceAuthenticationType credentialType) {
        DatasourceCredentialEntity datasourceCredential;
        if (credentialType == DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING) {
            final String name = SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            datasourceCredential = new DatasourceSqlServerConnectionString(name, SQL_SERVER_CONNECTION_STRING);
        } else if (credentialType == DatasourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY) {
            final String name = DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            datasourceCredential = new DatasourceDataLakeGen2SharedKey(name, AZURE_DATALAKEGEN2_ACCOUNT_KEY);
        } else if (credentialType == DatasourceAuthenticationType.SERVICE_PRINCIPAL) {
            final String name = SP_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
            final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            datasourceCredential = new DatasourceServicePrincipal(name, cId, tId, mockSecr);
        } else if (credentialType == DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV) {
            final StringBuilder kvEndpoint = new StringBuilder()
                .append("https://")
                .append(UUID.randomUUID())
                .append(".vault")
                .append(".azure.net");
            final String name = SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
            final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";

            datasourceCredential = new DatasourceServicePrincipalInKeyVault()
                .setName(name)
                .setKeyVaultForDatasourceSecrets(kvEndpoint.toString(), cId, mockSecr)
                .setTenantId(tId)
                .setSecretNameForDatasourceClientId("DSClientID_1")
                .setSecretNameForDatasourceClientSecret("DSClientSer_1");
        } else {
            throw new IllegalStateException("Unexpected value for DataSourceCredentialType: " + credentialType);
        }
        testRunner.accept(datasourceCredential);
    }

    void validateCredentialResult(DatasourceCredentialEntity expectedCredential,
                                  DatasourceCredentialEntity actualCredential,
                                  DatasourceAuthenticationType credentialType) {
        assertNotNull(actualCredential.getId());
        assertNotNull(actualCredential.getName());

        if (credentialType == DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING) {
            Assertions.assertTrue(actualCredential instanceof DatasourceSqlServerConnectionString);
            assertTrue(actualCredential.getName().startsWith(SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX));
        } else if (credentialType == DatasourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY) {
            Assertions.assertTrue(actualCredential instanceof DatasourceDataLakeGen2SharedKey);
            assertTrue(actualCredential.getName().startsWith(DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX));
        } else if (credentialType == DatasourceAuthenticationType.SERVICE_PRINCIPAL) {
            Assertions.assertTrue(actualCredential instanceof DatasourceServicePrincipal);
            assertTrue(actualCredential.getName().startsWith(SP_DATASOURCE_CRED_NAME_PREFIX));
            DatasourceServicePrincipal actualCredentialSP = (DatasourceServicePrincipal) actualCredential;
            assertNotNull(actualCredentialSP.getClientId());
            assertNotNull(actualCredentialSP.getTenantId());
            assertEquals(((DatasourceServicePrincipal) expectedCredential).getClientId(),
                actualCredentialSP.getClientId());
            assertEquals(((DatasourceServicePrincipal) expectedCredential).getTenantId(),
                actualCredentialSP.getTenantId());
        } else if (credentialType == DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV) {
            Assertions.assertTrue(actualCredential instanceof DatasourceServicePrincipalInKeyVault);
            assertTrue(actualCredential.getName().startsWith(SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX));
            DatasourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DatasourceServicePrincipalInKeyVault) actualCredential;
            assertNotNull(actualCredentialSPInKV.getKeyVaultEndpoint());
            assertNotNull(actualCredentialSPInKV.getKeyVaultClientId());
            assertNotNull(actualCredentialSPInKV.getTenantId());
            assertNotNull(actualCredentialSPInKV.getSecretNameForDatasourceClientId());
            assertNotNull(actualCredentialSPInKV.getSecretNameForDatasourceClientSecret());
            assertEquals(((DatasourceServicePrincipalInKeyVault) expectedCredential).getKeyVaultClientId(),
                actualCredentialSPInKV.getKeyVaultClientId());
            assertEquals(((DatasourceServicePrincipalInKeyVault) expectedCredential).getTenantId(),
                actualCredentialSPInKV.getTenantId());
            assertEquals(((DatasourceServicePrincipalInKeyVault) expectedCredential)
                    .getSecretNameForDatasourceClientId(),
                actualCredentialSPInKV.getSecretNameForDatasourceClientId());
            assertEquals(((DatasourceServicePrincipalInKeyVault) expectedCredential)
                    .getSecretNameForDatasourceClientSecret(),
                actualCredentialSPInKV.getSecretNameForDatasourceClientSecret());
        } else {
            throw new IllegalStateException("Unexpected value for DataSourceCredentialType: " + credentialType);
        }
    }
}
