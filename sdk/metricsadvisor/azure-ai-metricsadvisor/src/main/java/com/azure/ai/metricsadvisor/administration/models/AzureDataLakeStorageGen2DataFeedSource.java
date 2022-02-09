// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureDataLakeStorageGen2DataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureDataLakeStorageGen2DataFeedSource model.
 */
@Immutable
public final class AzureDataLakeStorageGen2DataFeedSource extends DataFeedSource {
    /*
     * Account name
     */
    private final String accountName;

    /*
     * Account key
     */
    private final String accountKey;

    /*
     * File system name (Container)
     */
    private final String fileSystemName;

    /*
     * Directory template
     */
    private final String directoryTemplate;

    /*
     * File template
     */
    private final String fileTemplate;

    /*
     * The id of the credential resource to authenticate the data source.
     */
    private final String credentialId;

    /*
     * The authentication type to access the data source.
     */
    private final DataSourceAuthenticationType authType;

    static {
        AzureDataLakeStorageGen2DataFeedSourceAccessor.setAccessor(
            new AzureDataLakeStorageGen2DataFeedSourceAccessor.Accessor() {
                @Override
                public String getAccountKey(AzureDataLakeStorageGen2DataFeedSource feedSource) {
                    return feedSource.getAccountKey();
                }
            });
    }

    private AzureDataLakeStorageGen2DataFeedSource(final String accountName,
                                                   final String accountKey,
                                                   final String fileSystemName,
                                                   final String directoryTemplate,
                                                   final String fileTemplate,
                                                   final String credentialId,
                                                   final DataSourceAuthenticationType authType) {
        this.accountName = accountName;
        this.accountKey = accountKey;
        this.fileSystemName = fileSystemName;
        this.directoryTemplate = directoryTemplate;
        this.fileTemplate = fileTemplate;
        this.credentialId = credentialId;
        this.authType = authType;
    }

    /**
     * Create a AzureDataLakeStorageGen2DataFeedSource with the given {@code accountKey} for authentication.
     *
     * @param accountName the name of the storage account.
     * @param accountKey the key of the storage account.
     * @param fileSystemName the file system name.
     * @param directoryTemplate the directory template of the storage account.
     * @param fileTemplate the file template.
     *
     * @return The AzureDataLakeStorageGen2DataFeedSource.
     */
    public static AzureDataLakeStorageGen2DataFeedSource fromBasicCredential(final String accountName,
                                                                             final String accountKey,
                                                                             final String fileSystemName,
                                                                             final String directoryTemplate,
                                                                             final String fileTemplate) {
        return new AzureDataLakeStorageGen2DataFeedSource(accountName,
            accountKey,
            fileSystemName,
            directoryTemplate,
            fileTemplate,
            null,
            DataSourceAuthenticationType.BASIC);
    }

    /**
     * Create a AzureDataLakeStorageGen2DataFeedSource with the {@code credentialId} identifying
     * a credential entity of type {@link DataSourceSqlServerConnectionString} that contains
     * the shared access key.
     *
     * @param accountName the name of the storage account.
     * @param fileSystemName the file system name.
     * @param directoryTemplate the directory template of the storage account.
     * @param fileTemplate the file template.
     * @param credentialId The unique id of a credential entity of type
     * {@link DataSourceDataLakeGen2SharedKey}.
     *
     * @return The AzureDataLakeStorageGen2DataFeedSource.
     */
    public static AzureDataLakeStorageGen2DataFeedSource fromSharedKeyCredential(
        final String accountName,
        final String fileSystemName,
        final String directoryTemplate,
        final String fileTemplate,
        final String credentialId) {
        return new AzureDataLakeStorageGen2DataFeedSource(accountName,
            null,
            fileSystemName,
            directoryTemplate,
            fileTemplate,
            credentialId,
            DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
    }

    /**
     * Create a AzureDataLakeStorageGen2DataFeedSource with the {@code credentialId}
     * identifying a credential entity of type {@link DataSourceServicePrincipal},
     * the entity contains Service Principal to access the Data Lake storage.
     *
     * @param accountName the name of the storage account.
     * @param fileSystemName the file system name.
     * @param directoryTemplate the directory template of the storage account.
     * @param fileTemplate the file template.
     * @param credentialId The unique id of a credential entity of type
     * {@link DataSourceServicePrincipal}.
     *
     * @return The AzureDataLakeStorageGen2DataFeedSource.
     */
    public static AzureDataLakeStorageGen2DataFeedSource fromServicePrincipalCredential(final String accountName,
                                                                                        final String fileSystemName,
                                                                                        final String directoryTemplate,
                                                                                        final String fileTemplate,
                                                                                        final String credentialId) {
        return new AzureDataLakeStorageGen2DataFeedSource(accountName,
            null,
            fileSystemName,
            directoryTemplate,
            fileTemplate,
            credentialId,
            DataSourceAuthenticationType.SERVICE_PRINCIPAL);
    }

    /**
     * Create a AzureDataLakeStorageGen2DataFeedSource with the {@code credentialId} identifying
     * a credential entity of type {@link DataSourceServicePrincipalInKeyVault}, the entity
     * contains details of the KeyVault holding the Service Principal to access the Data Lake storage.
     *
     * @param accountName the name of the storage account.
     * @param fileSystemName the file system name.
     * @param directoryTemplate the directory template of the storage account.
     * @param fileTemplate the file template.
     * @param credentialId The unique id of a credential entity of type
     * {@link DataSourceServicePrincipalInKeyVault}
     *
     * @return The AzureDataLakeStorageGen2DataFeedSource.
     */
    public static AzureDataLakeStorageGen2DataFeedSource fromServicePrincipalInKeyVaultCredential(
        final String accountName,
        final String fileSystemName,
        final String directoryTemplate,
        final String fileTemplate,
        final String credentialId) {
        return new AzureDataLakeStorageGen2DataFeedSource(accountName,
            null,
            fileSystemName,
            directoryTemplate,
            fileTemplate,
            credentialId,
            DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
    }

    /**
     * Get the the account name for the AzureDataLakeStorageGen2DataFeedSource.
     *
     * @return the accountName value.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Get the file system name or the container name.
     *
     * @return the fileSystemName value.
     */
    public String getFileSystemName() {
        return this.fileSystemName;
    }

    /**
     * Get the directory template.
     *
     * @return the directoryTemplate value.
     */
    public String getDirectoryTemplate() {
        return this.directoryTemplate;
    }

    /**
     * Get the file template.
     *
     * @return the fileTemplate value.
     */
    public String getFileTemplate() {
        return this.fileTemplate;
    }

    /**
     * Gets the id of the {@link DataSourceCredentialEntity credential resource} to authenticate the data source.
     *
     * @return The credential resource id.
     */
    public String getCredentialId() {
        return this.credentialId;
    }

    /**
     * Gets the authentication type to access the data source.
     *
     * @return The authentication type.
     */
    public DataSourceAuthenticationType getAuthenticationType() {
        return this.authType;
    }

    private String getAccountKey() {
        return this.accountKey;
    }
}
