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
import com.azure.ai.metricsadvisor.models.DataLakeGen2SharedKeyCredentialEntity;
import com.azure.ai.metricsadvisor.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.models.SqlServerConnectionStringCredentialEntity;
import com.azure.ai.metricsadvisor.models.ServicePrincipalCredentialEntity;
import com.azure.ai.metricsadvisor.models.ServicePrincipalInKeyVaultCredentialEntity;
import com.azure.core.util.logging.ClientLogger;

/**
 * Helper class to convert between service level credential model to SDK exposed model.
 */
public final class DataSourceCredentialEntityTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(DataSourceCredentialEntityTransforms.class);

    private DataSourceCredentialEntityTransforms() {
    }

    /**
     * Transform configuration wire model to {@link DataSourceCredentialEntity}.
     *
     * @param innerCredential The wire model instance.
     *
     * @return The custom model instance.
     */
    public static DataSourceCredentialEntity fromInner(DataSourceCredential innerCredential) {
        if (innerCredential instanceof AzureSQLConnectionStringCredential) {
            final AzureSQLConnectionStringCredential sqlConnectionStringCredential
                = (AzureSQLConnectionStringCredential) innerCredential;
            final SqlServerConnectionStringCredentialEntity credentialEntity
                = new SqlServerConnectionStringCredentialEntity(
                    sqlConnectionStringCredential.getDataSourceCredentialName(),
                null);
            credentialEntity.setDescription(sqlConnectionStringCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof DataLakeGen2SharedKeyCredential) {
            final DataLakeGen2SharedKeyCredential dataLakeGen2SharedKeyCredential
                = (DataLakeGen2SharedKeyCredential) innerCredential;
            final DataLakeGen2SharedKeyCredentialEntity credentialEntity = new DataLakeGen2SharedKeyCredentialEntity(
                dataLakeGen2SharedKeyCredential.getDataSourceCredentialName(),
                null);
            credentialEntity.setDescription(dataLakeGen2SharedKeyCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof ServicePrincipalCredential) {
            final ServicePrincipalCredential servicePrincipalCredential = (ServicePrincipalCredential) innerCredential;
            final ServicePrincipalCredentialEntity credentialEntity =
                new ServicePrincipalCredentialEntity(servicePrincipalCredential.getDataSourceCredentialName(),
                    servicePrincipalCredential.getParameters().getClientId(),
                    servicePrincipalCredential.getParameters().getTenantId(),
                    null);
            credentialEntity.setDescription(servicePrincipalCredential.getDataSourceCredentialDescription());
            return credentialEntity;
        } else if (innerCredential instanceof ServicePrincipalInKVCredential) {
            final ServicePrincipalInKVCredential servicePrincipalInKVCredential
                = (ServicePrincipalInKVCredential) innerCredential;
            final ServicePrincipalInKeyVaultCredentialEntity credentialEntity =
                new ServicePrincipalInKeyVaultCredentialEntity();
            credentialEntity
                .setName(servicePrincipalInKVCredential.getDataSourceCredentialName())
                .setDescription(servicePrincipalInKVCredential.getDataSourceCredentialDescription())
                .setKeyVaultForDataSourceSecrets(servicePrincipalInKVCredential.getParameters().getKeyVaultEndpoint(),
                    servicePrincipalInKVCredential.getParameters().getKeyVaultClientId(),
                    null)
                .setTenantId(servicePrincipalInKVCredential.getParameters().getTenantId());
            return credentialEntity;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown inner credential type."));
        }
    }

    public static DataSourceCredential toInnerForCreate(DataSourceCredentialEntity credentialEntity) {
        if (credentialEntity instanceof SqlServerConnectionStringCredentialEntity) {
            final SqlServerConnectionStringCredentialEntity credential
                = (SqlServerConnectionStringCredentialEntity) credentialEntity;
            final AzureSQLConnectionStringCredential innerCredential = new AzureSQLConnectionStringCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new AzureSQLConnectionStringParam()
                .setConnectionString(SqlServerConnectionStringCredentialEntityAccessor
                    .getConnectionString(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DataLakeGen2SharedKeyCredentialEntity) {
            final DataLakeGen2SharedKeyCredentialEntity credential
                = (DataLakeGen2SharedKeyCredentialEntity) credentialEntity;
            final DataLakeGen2SharedKeyCredential innerCredential  = new DataLakeGen2SharedKeyCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential
                .setParameters(new DataLakeGen2SharedKeyParam()
                    .setAccountKey(DataLakeGen2SharedKeyCredentialEntityAccessor.getSharedKey(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof ServicePrincipalCredentialEntity) {
            final ServicePrincipalCredentialEntity credential
                = (ServicePrincipalCredentialEntity) credentialEntity;
            final ServicePrincipalCredential innerCredential  = new ServicePrincipalCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalParam()
                .setClientId(credential.getClientId())
                .setTenantId(credential.getTenantId())
                .setClientSecret(ServicePrincipalCredentialEntityAccessor.getClientSecret(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof ServicePrincipalInKeyVaultCredentialEntity) {
            final ServicePrincipalInKeyVaultCredentialEntity credential
                = (ServicePrincipalInKeyVaultCredentialEntity) credentialEntity;
            final ServicePrincipalInKVCredential innerCredential  = new ServicePrincipalInKVCredential();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalInKVParam()
                .setKeyVaultEndpoint(credential.getKeyVaultEndpoint())
                .setKeyVaultClientId(credential.getKeyVaultClientId())
                .setKeyVaultClientSecret(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getKeyVaultClientSecret(credential))
                .setServicePrincipalIdNameInKV(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getSecretNameForDataSourceClientId(credential))
                .setServicePrincipalSecretNameInKV(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getSecretNameForDataSourceClientSecret(credential))
                .setTenantId(credential.getTenantId()));
            return innerCredential;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown credential entity type."));
        }
    }

    public static DataSourceCredentialPatch toInnerForUpdate(DataSourceCredentialEntity credentialEntity) {
        if (credentialEntity instanceof SqlServerConnectionStringCredentialEntity) {
            final SqlServerConnectionStringCredentialEntity credential
                = (SqlServerConnectionStringCredentialEntity) credentialEntity;
            final AzureSQLConnectionStringCredentialPatch innerCredential = new AzureSQLConnectionStringCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new AzureSQLConnectionStringParamPatch()
                .setConnectionString(SqlServerConnectionStringCredentialEntityAccessor
                    .getConnectionString(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof DataLakeGen2SharedKeyCredentialEntity) {
            final DataLakeGen2SharedKeyCredentialEntity credential
                = (DataLakeGen2SharedKeyCredentialEntity) credentialEntity;
            final DataLakeGen2SharedKeyCredentialPatch innerCredential  = new DataLakeGen2SharedKeyCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential
                .setParameters(new DataLakeGen2SharedKeyParamPatch()
                    .setAccountKey(DataLakeGen2SharedKeyCredentialEntityAccessor.getSharedKey(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof ServicePrincipalCredentialEntity) {
            final ServicePrincipalCredentialEntity credential
                = (ServicePrincipalCredentialEntity) credentialEntity;
            final ServicePrincipalCredentialPatch innerCredential  = new ServicePrincipalCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalParamPatch()
                .setClientId(credential.getClientId())
                .setTenantId(credential.getTenantId())
                .setClientSecret(ServicePrincipalCredentialEntityAccessor.getClientSecret(credential)));

            return innerCredential;
        } else if (credentialEntity instanceof ServicePrincipalInKeyVaultCredentialEntity) {
            final ServicePrincipalInKeyVaultCredentialEntity credential
                = (ServicePrincipalInKeyVaultCredentialEntity) credentialEntity;
            final ServicePrincipalInKVCredentialPatch innerCredential  = new ServicePrincipalInKVCredentialPatch();
            innerCredential.setDataSourceCredentialName(credentialEntity.getName());
            innerCredential.setDataSourceCredentialDescription(credentialEntity.getDescription());
            innerCredential.setParameters(new ServicePrincipalInKVParamPatch()
                .setKeyVaultEndpoint(credential.getKeyVaultEndpoint())
                .setKeyVaultClientId(credential.getKeyVaultClientId())
                .setKeyVaultClientSecret(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getKeyVaultClientSecret(credential))
                .setServicePrincipalIdNameInKV(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getSecretNameForDataSourceClientId(credential))
                .setServicePrincipalSecretNameInKV(ServicePrincipalInKeyVaultCredentialEntityAccessor
                    .getSecretNameForDataSourceClientSecret(credential))
                .setTenantId(credential.getTenantId()));
            return innerCredential;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown credential entity type."));
        }
    }
}
