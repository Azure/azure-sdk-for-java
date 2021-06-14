// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AzureSQLConnectionStringCredential;
import com.azure.ai.metricsadvisor.implementation.models.AzureSQLConnectionStringCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureSQLConnectionStringParam;
import com.azure.ai.metricsadvisor.implementation.models.AzureSQLConnectionStringParamPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataLakeGen2SharedKeyCredential;
import com.azure.ai.metricsadvisor.implementation.models.DataLakeGen2SharedKeyCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataLakeGen2SharedKeyParam;
import com.azure.ai.metricsadvisor.implementation.models.DataLakeGen2SharedKeyParamPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredential;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalCredential;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalInKVCredential;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalInKVCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalInKVParam;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalInKVParamPatch;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalParam;
import com.azure.ai.metricsadvisor.implementation.models.ServicePrincipalParamPatch;
import com.azure.ai.metricsadvisor.administration.models.DatasourceDataLakeGen2SharedKey;
import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.DatasourceSqlServerConnectionString;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipal;
import com.azure.ai.metricsadvisor.administration.models.DatasourceServicePrincipalInKeyVault;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to convert between service level credential model to SDK exposed model.
 */
public final class DataSourceCredentialEntityTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(DataSourceCredentialEntityTransforms.class);

    private DataSourceCredentialEntityTransforms() {
    }

    /**
     * Transform configuration wire model to {@link DatasourceCredentialEntity}.
     *
     * @param innerCredential The wire model instance.
     *
     * @return The custom model instance.
     */
    public static DatasourceCredentialEntity fromInner(DataSourceCredential innerCredential) {
        if (innerCredential instanceof AzureSQLConnectionStringCredential) {
            final AzureSQLConnectionStringCredential sqlConnectionStringCredential
                = (AzureSQLConnectionStringCredential) innerCredential;
            final DatasourceSqlServerConnectionString credentialEntity
                = new DatasourceSqlServerConnectionString(
                    sqlConnectionStringCredential.getDataSourceCredentialName(),
                null);
            DataSourceSqlServerConnectionStringAccessor.setId(credentialEntity,
                sqlConnectionStringCredential.getDataSourceCredentialId().toString());
            credentialEntity.setDescription(sqlConnectionStringCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof DataLakeGen2SharedKeyCredential) {
            final DataLakeGen2SharedKeyCredential dataLakeGen2SharedKeyCredential
                = (DataLakeGen2SharedKeyCredential) innerCredential;
            final DatasourceDataLakeGen2SharedKey credentialEntity = new DatasourceDataLakeGen2SharedKey(
                dataLakeGen2SharedKeyCredential.getDataSourceCredentialName(),
                null);
            DataSourceDataLakeGen2SharedKeyAccessor.setId(credentialEntity,
                dataLakeGen2SharedKeyCredential.getDataSourceCredentialId().toString());
            credentialEntity.setDescription(dataLakeGen2SharedKeyCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof ServicePrincipalCredential) {
            final ServicePrincipalCredential servicePrincipalCredential = (ServicePrincipalCredential) innerCredential;
            final DatasourceServicePrincipal credentialEntity =
                new DatasourceServicePrincipal(servicePrincipalCredential.getDataSourceCredentialName(),
                    servicePrincipalCredential.getParameters().getClientId(),
                    servicePrincipalCredential.getParameters().getTenantId(),
                    null);
            DataSourceServicePrincipalAccessor.setId(credentialEntity,
                servicePrincipalCredential.getDataSourceCredentialId().toString());
            credentialEntity.setDescription(servicePrincipalCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof ServicePrincipalInKVCredential) {
            final ServicePrincipalInKVCredential servicePrincipalInKVCredential
                = (ServicePrincipalInKVCredential) innerCredential;
            final DatasourceServicePrincipalInKeyVault credentialEntity =
                new DatasourceServicePrincipalInKeyVault();
            credentialEntity
                .setName(servicePrincipalInKVCredential.getDataSourceCredentialName())
                .setDescription(servicePrincipalInKVCredential.getDataSourceCredentialDescription())
                .setKeyVaultForDatasourceSecrets(servicePrincipalInKVCredential.getParameters().getKeyVaultEndpoint(),
                    servicePrincipalInKVCredential.getParameters().getKeyVaultClientId(),
                    null)
                .setTenantId(servicePrincipalInKVCredential.getParameters().getTenantId())
                .setSecretNameForDatasourceClientId(
                    servicePrincipalInKVCredential.getParameters().getServicePrincipalIdNameInKV())
                .setSecretNameForDatasourceClientSecret(
                    servicePrincipalInKVCredential.getParameters().getServicePrincipalSecretNameInKV());

            DataSourceServicePrincipalInKeyVaultAccessor.setId(credentialEntity,
                servicePrincipalInKVCredential.getDataSourceCredentialId().toString());

            return credentialEntity;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown inner credential type."));
        }
    }

    public static DataSourceCredential toInnerForCreate(DatasourceCredentialEntity credentialEntity) {
        if (credentialEntity instanceof DatasourceSqlServerConnectionString) {
            final DatasourceSqlServerConnectionString credential
                = (DatasourceSqlServerConnectionString) credentialEntity;
            final AzureSQLConnectionStringCredential innerCredential = new AzureSQLConnectionStringCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new AzureSQLConnectionStringParam()
                .setConnectionString(DataSourceSqlServerConnectionStringAccessor
                    .getConnectionString(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceDataLakeGen2SharedKey) {
            final DatasourceDataLakeGen2SharedKey credential
                = (DatasourceDataLakeGen2SharedKey) credentialEntity;
            final DataLakeGen2SharedKeyCredential innerCredential  = new DataLakeGen2SharedKeyCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential
                .setParameters(new DataLakeGen2SharedKeyParam()
                    .setAccountKey(DataSourceDataLakeGen2SharedKeyAccessor.getSharedKey(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceServicePrincipal) {
            final DatasourceServicePrincipal credential
                = (DatasourceServicePrincipal) credentialEntity;
            final ServicePrincipalCredential innerCredential  = new ServicePrincipalCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalParam()
                .setClientId(credential.getClientId())
                .setTenantId(credential.getTenantId())
                .setClientSecret(DataSourceServicePrincipalAccessor.getClientSecret(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceServicePrincipalInKeyVault) {
            final DatasourceServicePrincipalInKeyVault credential
                = (DatasourceServicePrincipalInKeyVault) credentialEntity;
            final ServicePrincipalInKVCredential innerCredential  = new ServicePrincipalInKVCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalInKVParam()
                .setKeyVaultEndpoint(credential.getKeyVaultEndpoint())
                .setKeyVaultClientId(credential.getKeyVaultClientId())
                .setKeyVaultClientSecret(DataSourceServicePrincipalInKeyVaultAccessor
                    .getKeyVaultClientSecret(credential))
                .setServicePrincipalIdNameInKV(credential.getSecretNameForDatasourceClientId())
                .setServicePrincipalSecretNameInKV(credential.getSecretNameForDatasourceClientSecret())
                .setTenantId(credential.getTenantId()));
            return innerCredential;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown credential entity type."));
        }
    }

    public static DataSourceCredentialPatch toInnerForUpdate(DatasourceCredentialEntity credentialEntity) {
        if (credentialEntity instanceof DatasourceSqlServerConnectionString) {
            final DatasourceSqlServerConnectionString credential
                = (DatasourceSqlServerConnectionString) credentialEntity;
            final AzureSQLConnectionStringCredentialPatch innerCredential = new AzureSQLConnectionStringCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new AzureSQLConnectionStringParamPatch()
                .setConnectionString(DataSourceSqlServerConnectionStringAccessor
                    .getConnectionString(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceDataLakeGen2SharedKey) {
            final DatasourceDataLakeGen2SharedKey credential
                = (DatasourceDataLakeGen2SharedKey) credentialEntity;
            final DataLakeGen2SharedKeyCredentialPatch innerCredential  = new DataLakeGen2SharedKeyCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential
                .setParameters(new DataLakeGen2SharedKeyParamPatch()
                    .setAccountKey(DataSourceDataLakeGen2SharedKeyAccessor.getSharedKey(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceServicePrincipal) {
            final DatasourceServicePrincipal credential
                = (DatasourceServicePrincipal) credentialEntity;
            final ServicePrincipalCredentialPatch innerCredential  = new ServicePrincipalCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalParamPatch()
                .setClientId(credential.getClientId())
                .setTenantId(credential.getTenantId())
                .setClientSecret(DataSourceServicePrincipalAccessor.getClientSecret(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DatasourceServicePrincipalInKeyVault) {
            final DatasourceServicePrincipalInKeyVault credential
                = (DatasourceServicePrincipalInKeyVault) credentialEntity;
            final ServicePrincipalInKVCredentialPatch innerCredential  = new ServicePrincipalInKVCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalInKVParamPatch()
                .setKeyVaultEndpoint(credential.getKeyVaultEndpoint())
                .setKeyVaultClientId(credential.getKeyVaultClientId())
                .setKeyVaultClientSecret(DataSourceServicePrincipalInKeyVaultAccessor
                    .getKeyVaultClientSecret(credential))
                .setServicePrincipalIdNameInKV(credential.getSecretNameForDatasourceClientId())
                .setServicePrincipalSecretNameInKV(credential.getSecretNameForDatasourceClientSecret())
                .setTenantId(credential.getTenantId()));
            return innerCredential;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown credential entity type."));
        }
    }
}
