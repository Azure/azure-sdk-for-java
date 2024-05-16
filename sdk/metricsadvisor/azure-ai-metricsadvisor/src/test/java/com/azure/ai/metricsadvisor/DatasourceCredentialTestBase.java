// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.DataSourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DataSourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DataSourceServicePrincipalInKeyVault;
import com.azure.ai.metricsadvisor.administration.models.DataSourceSqlServerConnectionString;
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

    void listDatasourceCredentialRunner(Consumer<List<DataSourceCredentialEntity>> testRunner) {
        List<DataSourceCredentialEntity> list = new ArrayList<>();
        creatDatasourceCredentialRunner(datasource -> list.add(datasource),
            DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
        creatDatasourceCredentialRunner(datasource -> list.add(datasource),
            DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
        testRunner.accept(list);
    }

    void creatDatasourceCredentialRunner(Consumer<DataSourceCredentialEntity> testRunner,
                                         DataSourceAuthenticationType credentialType) {
        DataSourceCredentialEntity datasourceCredential;
        if (credentialType == DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING) {
            final String name = SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            datasourceCredential = new DataSourceSqlServerConnectionString(name, SQL_SERVER_CONNECTION_STRING);
        } else if (credentialType == DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY) {
            final String name = DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            datasourceCredential = new DataSourceDataLakeGen2SharedKey(name, AZURE_DATALAKEGEN2_ACCOUNT_KEY);
        } else if (credentialType == DataSourceAuthenticationType.SERVICE_PRINCIPAL) {
            final String name = SP_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
            final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            datasourceCredential = new DataSourceServicePrincipal(name, cId, tId, mockSecr);
        } else if (credentialType == DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV) {
            final StringBuilder kvEndpoint = new StringBuilder()
                .append("https://")
                .append(UUID.randomUUID())
                .append(".vault")
                .append(".azure.net");
            final String name = SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX + UUID.randomUUID();
            final String cId = "e70248b2-bffa-11eb-8529-0242ac130003";
            final String tId = "45389ded-5e07-4e52-b225-4ae8f905afb5";
            final String mockSecr = "45389ded-5e07-4e52-b225-4ae8f905afb5";

            datasourceCredential = new DataSourceServicePrincipalInKeyVault()
                .setName(name)
                .setKeyVaultForDataSourceSecrets(kvEndpoint.toString(), cId, mockSecr)
                .setTenantId(tId)
                .setSecretNameForDataSourceClientId("DSClientID_1")
                .setSecretNameForDataSourceClientSecret("DSClientSer_1");
        } else {
            throw new IllegalStateException("Unexpected value for DataSourceCredentialType: " + credentialType);
        }
        testRunner.accept(datasourceCredential);
    }

    void validateCredentialResult(DataSourceCredentialEntity expectedCredential,
                                  DataSourceCredentialEntity actualCredential,
                                  DataSourceAuthenticationType credentialType) {
        assertNotNull(actualCredential.getId());
        assertNotNull(actualCredential.getName());

        if (credentialType == DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING) {
            Assertions.assertTrue(actualCredential instanceof DataSourceSqlServerConnectionString);
            assertTrue(actualCredential.getName().startsWith(SQL_CONNECTION_DATASOURCE_CRED_NAME_PREFIX));
        } else if (credentialType == DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY) {
            Assertions.assertTrue(actualCredential instanceof DataSourceDataLakeGen2SharedKey);
            assertTrue(actualCredential.getName().startsWith(DATA_LAKE_GEN2_SHARED_KEY_DATASOURCE_CRED_NAME_PREFIX));
        } else if (credentialType == DataSourceAuthenticationType.SERVICE_PRINCIPAL) {
            Assertions.assertTrue(actualCredential instanceof DataSourceServicePrincipal);
            assertTrue(actualCredential.getName().startsWith(SP_DATASOURCE_CRED_NAME_PREFIX));
            DataSourceServicePrincipal actualCredentialSP = (DataSourceServicePrincipal) actualCredential;
            assertNotNull(actualCredentialSP.getClientId());
            assertNotNull(actualCredentialSP.getTenantId());
            assertEquals(((DataSourceServicePrincipal) expectedCredential).getClientId(),
                actualCredentialSP.getClientId());
            assertEquals(((DataSourceServicePrincipal) expectedCredential).getTenantId(),
                actualCredentialSP.getTenantId());
        } else if (credentialType == DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV) {
            Assertions.assertTrue(actualCredential instanceof DataSourceServicePrincipalInKeyVault);
            assertTrue(actualCredential.getName().startsWith(SP_IN_KV_DATASOURCE_CRED_NAME_PREFIX));
            DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
                = (DataSourceServicePrincipalInKeyVault) actualCredential;
            assertNotNull(actualCredentialSPInKV.getKeyVaultEndpoint());
            assertNotNull(actualCredentialSPInKV.getKeyVaultClientId());
            assertNotNull(actualCredentialSPInKV.getTenantId());
            assertNotNull(actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            assertNotNull(actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
            assertEquals(((DataSourceServicePrincipalInKeyVault) expectedCredential).getKeyVaultClientId(),
                actualCredentialSPInKV.getKeyVaultClientId());
            assertEquals(((DataSourceServicePrincipalInKeyVault) expectedCredential).getTenantId(),
                actualCredentialSPInKV.getTenantId());
            assertEquals(((DataSourceServicePrincipalInKeyVault) expectedCredential)
                    .getSecretNameForDataSourceClientId(),
                actualCredentialSPInKV.getSecretNameForDataSourceClientId());
            assertEquals(((DataSourceServicePrincipalInKeyVault) expectedCredential)
                    .getSecretNameForDataSourceClientSecret(),
                actualCredentialSPInKV.getSecretNameForDataSourceClientSecret());
        } else {
            throw new IllegalStateException("Unexpected value for DataSourceCredentialType: " + credentialType);
        }
    }
}
