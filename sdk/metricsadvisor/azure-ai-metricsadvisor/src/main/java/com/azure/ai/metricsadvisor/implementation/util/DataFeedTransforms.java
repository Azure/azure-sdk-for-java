// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.implementation.models.AuthenticationTypeEnum;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataExplorerDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataExplorerDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2DataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2DataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2Parameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2ParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureEventHubsDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureEventHubsDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureEventHubsParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureEventHubsParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureLogAnalyticsDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureLogAnalyticsDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureLogAnalyticsParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureLogAnalyticsParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataFeedDetail;
import com.azure.ai.metricsadvisor.implementation.models.DataFeedDetailPatch;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.MySqlDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.MySqlDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.PostgreSqlDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.PostgreSqlDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.SQLServerDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.SQLServerDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.SQLSourceParameterPatch;
import com.azure.ai.metricsadvisor.implementation.models.SqlSourceParameter;
import com.azure.ai.metricsadvisor.administration.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureCosmosDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureEventHubsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureLogAnalyticsDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.AzureTableDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedAccessMode;
import com.azure.ai.metricsadvisor.administration.models.DataFeedAutoRollUpMethod;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillType;
import com.azure.ai.metricsadvisor.models.InfluxDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.MongoDbDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.PostgreSqlDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level data feed model to SDK exposed model.
 */
public final class DataFeedTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(DataFeedTransforms.class);

    private DataFeedTransforms() {
    }

    /**
     * A converter between {@link DataFeedDetail} and {@link DataFeed}.
     */
    public static DataFeed fromInner(final DataFeedDetail dataFeedDetail) {
        final DataFeedGranularity dataFeedGranularity = new DataFeedGranularity()
            .setGranularityType(DataFeedGranularityType.fromString(dataFeedDetail.getGranularityName().toString()));
        if (dataFeedDetail.getGranularityAmount() != null) {
            dataFeedGranularity.setCustomGranularityValue(dataFeedDetail.getGranularityAmount());
        }

        final DataFeed dataFeed = setDataFeedSourceType(dataFeedDetail);
        dataFeed
            .setName(dataFeedDetail.getDataFeedName())
            .setSchema(new DataFeedSchema(fromInnerMetricList(dataFeedDetail.getMetrics()))
                .setDimensions(fromInnerDimensionList(dataFeedDetail.getDimension()))
                .setTimestampColumn(dataFeedDetail.getTimestampColumn()))
            .setGranularity(dataFeedGranularity)
            .setIngestionSettings(new DataFeedIngestionSettings(dataFeedDetail.getDataStartFrom())
                .setDataSourceRequestConcurrency(dataFeedDetail.getMaxConcurrency())
                .setIngestionRetryDelay(Duration.ofSeconds(dataFeedDetail.getMinRetryIntervalInSeconds()))
                .setIngestionStartOffset(Duration.ofSeconds(dataFeedDetail.getStartOffsetInSeconds()))
                .setStopRetryAfter(Duration.ofSeconds(dataFeedDetail.getStopRetryAfterInSeconds())))
            .setOptions(new DataFeedOptions()
                .setDescription(dataFeedDetail.getDataFeedDescription())
                .setMissingDataPointFillSettings(new DataFeedMissingDataPointFillSettings()
                    .setCustomFillValue(dataFeedDetail.getFillMissingPointValue())
                    .setFillType(DataFeedMissingDataPointFillType.fromString(
                        dataFeedDetail.getFillMissingPointType().toString())))
                .setAccessMode(DataFeedAccessMode.fromString(dataFeedDetail.getViewMode().toString()))
                .setAdmins(dataFeedDetail.getAdmins())
                .setRollupSettings(new DataFeedRollupSettings()
                    .setAlreadyRollup(dataFeedDetail.getAllUpIdentification())
                    .setAutoRollup(DataFeedAutoRollUpMethod.fromString(dataFeedDetail.getRollUpMethod().toString()),
                        dataFeedDetail.getRollUpColumns())
                    .setRollupType(DataFeedRollupType.fromString(dataFeedDetail.getNeedRollup().toString())))
                .setActionLinkTemplate(dataFeedDetail.getActionLinkTemplate())
                .setViewers(dataFeedDetail.getViewers()));

        DataFeedHelper.setId(dataFeed, dataFeedDetail.getDataFeedId().toString());
        DataFeedHelper.setCreatedTime(dataFeed, dataFeedDetail.getCreatedTime());
        DataFeedHelper.setIsAdmin(dataFeed, dataFeedDetail.isAdmin());
        DataFeedHelper.setCreator(dataFeed, dataFeedDetail.getCreator());
        DataFeedHelper.setStatus(dataFeed, DataFeedStatus.fromString(dataFeedDetail.getStatus().toString()));
        DataFeedHelper.setMetricIds(dataFeed,
            dataFeedDetail.getMetrics().stream()
                .collect(Collectors.toMap(com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric::getId,
                    com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric::getName)));
        return dataFeed;
    }

    /**
     * Helper to check the service provided data feed source type and map to SDK equivalent data feed source type.
     *
     * @param dataFeedDetail the service returned data feed detail model
     *
     * @return the updated/created SDK equivalent data feed model.
     */
    private static DataFeed setDataFeedSourceType(final DataFeedDetail dataFeedDetail) {
        final DataFeedSourceType dataFeedSourceType;
        final DataFeed dataFeed = new DataFeed();

        if (dataFeedDetail instanceof AzureApplicationInsightsDataFeed) {
            final AzureApplicationInsightsParameter dataSourceParameter =
                ((AzureApplicationInsightsDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureAppInsightsDataFeedSource(
                dataSourceParameter.getApplicationId(),
                dataSourceParameter.getApiKey(), dataSourceParameter.getAzureCloud(),
                dataSourceParameter.getQuery()));
            dataFeedSourceType = DataFeedSourceType.AZURE_APP_INSIGHTS;
        } else if (dataFeedDetail instanceof AzureBlobDataFeed) {
            final AzureBlobParameter dataSourceParameter = ((AzureBlobDataFeed) dataFeedDetail)
                .getDataSourceParameter();
            if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.BASIC) {
                dataFeed.setSource(AzureBlobDataFeedSource.fromBasicCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getContainer(),
                    dataSourceParameter.getBlobTemplate()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.MANAGED_IDENTITY) {
                dataFeed.setSource(AzureBlobDataFeedSource.fromManagedIdentityCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getContainer(),
                    dataSourceParameter.getBlobTemplate()));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("AuthType %s not supported for Blob", dataFeedDetail.getAuthenticationType())));
            }
            dataFeedSourceType = DataFeedSourceType.AZURE_BLOB;
        } else if (dataFeedDetail instanceof AzureCosmosDBDataFeed) {
            final AzureCosmosDBParameter dataSourceParameter =
                ((AzureCosmosDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureCosmosDbDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getSqlQuery(),
                dataSourceParameter.getDatabase(),
                dataSourceParameter.getCollectionId()
            ));
            dataFeedSourceType = DataFeedSourceType.AZURE_COSMOS_DB;
        } else if (dataFeedDetail instanceof AzureDataExplorerDataFeed) {
            final SqlSourceParameter dataSourceParameter =
                ((AzureDataExplorerDataFeed) dataFeedDetail).getDataSourceParameter();
            if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.BASIC) {
                dataFeed.setSource(AzureDataExplorerDataFeedSource.fromBasicCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.MANAGED_IDENTITY) {
                dataFeed.setSource(AzureDataExplorerDataFeedSource.fromManagedIdentityCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL) {
                dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery(),
                    dataFeedDetail.getCredentialId()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL_IN_KV) {
                dataFeed.setSource(AzureDataExplorerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery(),
                    dataFeedDetail.getCredentialId()
                ));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("AuthType %s not supported for AzureDataExplorer",
                        dataFeedDetail.getAuthenticationType())));
            }
            dataFeedSourceType = DataFeedSourceType.AZURE_DATA_EXPLORER;
        } else if (dataFeedDetail instanceof AzureEventHubsDataFeed) {
            final AzureEventHubsParameter azureEventHubsParameter =
                ((AzureEventHubsDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureEventHubsDataFeedSource(
                azureEventHubsParameter.getConnectionString(),
                azureEventHubsParameter.getConsumerGroup()));
            dataFeedSourceType = DataFeedSourceType.AZURE_EVENT_HUBS;
        } else if (dataFeedDetail instanceof AzureTableDataFeed) {
            final AzureTableParameter dataSourceParameter = ((AzureTableDataFeed) dataFeedDetail)
                .getDataSourceParameter();
            dataFeed.setSource(new AzureTableDataFeedSource(dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery(), dataSourceParameter.getTable()));
            dataFeedSourceType = DataFeedSourceType.AZURE_TABLE;
        } else if (dataFeedDetail instanceof InfluxDBDataFeed) {
            final InfluxDBParameter dataSourceParameter = ((InfluxDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new InfluxDbDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getDatabase(),
                dataSourceParameter.getUserName(),
                dataSourceParameter.getPassword(),
                dataSourceParameter.getQuery()
            ));
            dataFeedSourceType = DataFeedSourceType.INFLUX_DB;
        } else if (dataFeedDetail instanceof MySqlDataFeed) {
            final SqlSourceParameter dataSourceParameter = ((MySqlDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new MySqlDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery()
            ));
            dataFeedSourceType = DataFeedSourceType.MYSQL_DB;
        } else if (dataFeedDetail instanceof PostgreSqlDataFeed) {
            final SqlSourceParameter dataSourceParameter =
                ((PostgreSqlDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new PostgreSqlDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery()
            ));
            dataFeedSourceType = DataFeedSourceType.POSTGRE_SQL_DB;
        } else if (dataFeedDetail instanceof SQLServerDataFeed) {
            final SqlSourceParameter dataSourceParameter = ((SQLServerDataFeed) dataFeedDetail)
                .getDataSourceParameter();
            if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.BASIC) {
                dataFeed.setSource(SqlServerDataFeedSource.fromBasicCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.MANAGED_IDENTITY) {
                dataFeed.setSource(SqlServerDataFeedSource.fromManagedIdentityCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.AZURE_SQLCONNECTION_STRING) {
                dataFeed.setSource(SqlServerDataFeedSource.fromConnectionStringCredential(
                    dataSourceParameter.getQuery(),
                    dataFeedDetail.getCredentialId()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL) {
                dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery(),
                    dataFeedDetail.getCredentialId()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL_IN_KV) {
                dataFeed.setSource(SqlServerDataFeedSource.fromServicePrincipalInKeyVaultCredential(
                    dataSourceParameter.getConnectionString(),
                    dataSourceParameter.getQuery(),
                    dataFeedDetail.getCredentialId()));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("AuthType %s not supported for AzureSqlServer",
                        dataFeedDetail.getAuthenticationType())));
            }
            dataFeedSourceType = DataFeedSourceType.SQL_SERVER_DB;
        } else if (dataFeedDetail instanceof MongoDBDataFeed) {
            final MongoDBParameter dataSourceParameter = ((MongoDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new MongoDbDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getDatabase(),
                dataSourceParameter.getCommand()
            ));
            dataFeedSourceType = DataFeedSourceType.MONGO_DB;
        } else if (dataFeedDetail instanceof AzureDataLakeStorageGen2DataFeed) {
            final AzureDataLakeStorageGen2Parameter azureDataLakeStorageGen2Parameter =
                ((AzureDataLakeStorageGen2DataFeed) dataFeedDetail).getDataSourceParameter();
            if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.BASIC) {
                dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromBasicCredential(
                    azureDataLakeStorageGen2Parameter.getAccountName(),
                    azureDataLakeStorageGen2Parameter.getAccountKey(),
                    azureDataLakeStorageGen2Parameter.getFileSystemName(),
                    azureDataLakeStorageGen2Parameter.getDirectoryTemplate(),
                    azureDataLakeStorageGen2Parameter.getFileTemplate()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.DATA_LAKE_GEN2SHARED_KEY) {
                dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromSharedKeyCredential(
                    azureDataLakeStorageGen2Parameter.getAccountName(),
                    azureDataLakeStorageGen2Parameter.getFileSystemName(),
                    azureDataLakeStorageGen2Parameter.getDirectoryTemplate(),
                    azureDataLakeStorageGen2Parameter.getFileTemplate(),
                    dataFeedDetail.getCredentialId()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL) {
                dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalCredential(
                    azureDataLakeStorageGen2Parameter.getAccountName(),
                    azureDataLakeStorageGen2Parameter.getFileSystemName(),
                    azureDataLakeStorageGen2Parameter.getDirectoryTemplate(),
                    azureDataLakeStorageGen2Parameter.getFileTemplate(),
                    dataFeedDetail.getCredentialId()
                ));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL_IN_KV) {
                dataFeed.setSource(AzureDataLakeStorageGen2DataFeedSource.fromServicePrincipalInKeyVaultCredential(
                    azureDataLakeStorageGen2Parameter.getAccountName(),
                    azureDataLakeStorageGen2Parameter.getFileSystemName(),
                    azureDataLakeStorageGen2Parameter.getDirectoryTemplate(),
                    azureDataLakeStorageGen2Parameter.getFileTemplate(),
                    dataFeedDetail.getCredentialId()
                ));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("AuthType %s not supported for AzureDataLakeStorageGen2",
                        dataFeedDetail.getAuthenticationType())));
            }
            dataFeedSourceType = DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2;
        } else if (dataFeedDetail instanceof AzureLogAnalyticsDataFeed) {
            final AzureLogAnalyticsParameter azureLogAnalyticsDataFeed =
                ((AzureLogAnalyticsDataFeed) dataFeedDetail).getDataSourceParameter();
            if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.BASIC) {
                dataFeed.setSource(AzureLogAnalyticsDataFeedSource.fromBasicCredential(
                    azureLogAnalyticsDataFeed.getTenantId(),
                    azureLogAnalyticsDataFeed.getClientId(),
                    azureLogAnalyticsDataFeed.getClientSecret(),
                    azureLogAnalyticsDataFeed.getWorkspaceId(),
                    azureLogAnalyticsDataFeed.getQuery()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL) {
                dataFeed.setSource(AzureLogAnalyticsDataFeedSource.fromServicePrincipalCredential(
                    azureLogAnalyticsDataFeed.getWorkspaceId(),
                    azureLogAnalyticsDataFeed.getQuery(),
                    dataFeedDetail.getCredentialId()));
            } else if (dataFeedDetail.getAuthenticationType() == AuthenticationTypeEnum.SERVICE_PRINCIPAL_IN_KV) {
                dataFeed.setSource(AzureLogAnalyticsDataFeedSource.fromServicePrincipalInKeyVaultCredential(
                    azureLogAnalyticsDataFeed.getWorkspaceId(),
                    azureLogAnalyticsDataFeed.getQuery(),
                    dataFeedDetail.getCredentialId()));
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("AuthType %s not supported for AzureLogAnalytics",
                        dataFeedDetail.getAuthenticationType())));
            }
            dataFeedSourceType = DataFeedSourceType.AZURE_LOG_ANALYTICS;
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                String.format("Data feed source type %s not supported", dataFeedDetail.getClass().getCanonicalName())));
        }
        DataFeedHelper.setSourceType(dataFeed, dataFeedSourceType);
        return dataFeed;
    }

    /**
     * Helper to map the SDK level data feed model to service required DataFeedDetail model.
     *
     * @param dataFeedSource the SDK level data feed source.
     *
     * @return the service mapped DataFeedDetail model.
     */
    public static DataFeedDetail toDataFeedDetailSource(final DataFeedSource dataFeedSource) {
        final DataFeedDetail dataFeedDetail;
        if (dataFeedSource instanceof AzureAppInsightsDataFeedSource) {
            final AzureAppInsightsDataFeedSource azureAppInsightsDataFeedSource =
                ((AzureAppInsightsDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureApplicationInsightsDataFeed()
                .setDataSourceParameter(new AzureApplicationInsightsParameter()
                    .setApiKey(azureAppInsightsDataFeedSource.getApiKey())
                    .setApplicationId(azureAppInsightsDataFeedSource.getApplicationId())
                    .setAzureCloud(azureAppInsightsDataFeedSource.getAzureCloud())
                    .setQuery(azureAppInsightsDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof AzureBlobDataFeedSource) {
            final AzureBlobDataFeedSource azureBlobDataFeedSource = ((AzureBlobDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureBlobDataFeed()
                .setDataSourceParameter(new AzureBlobParameter()
                    .setConnectionString(AzureBlobDataFeedSourceAccessor.getConnectionString(azureBlobDataFeedSource))
                    .setContainer(azureBlobDataFeedSource.getContainer())
                    .setBlobTemplate(azureBlobDataFeedSource.getBlobTemplate()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureBlobDataFeedSource.getAuthenticationType().toString()));
        } else if (dataFeedSource instanceof AzureCosmosDbDataFeedSource) {
            final AzureCosmosDbDataFeedSource azureCosmosDbDataFeedSource = ((AzureCosmosDbDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureCosmosDBDataFeed()
                .setDataSourceParameter(new AzureCosmosDBParameter()
                    .setConnectionString(AzureCosmosDbDataFeedSourceAccessor
                        .getConnectionString(azureCosmosDbDataFeedSource))
                    .setCollectionId(azureCosmosDbDataFeedSource.getCollectionId())
                    .setDatabase(azureCosmosDbDataFeedSource.getDatabase())
                    .setSqlQuery(azureCosmosDbDataFeedSource.getSqlQuery()));
        } else if (dataFeedSource instanceof AzureDataExplorerDataFeedSource) {
            final AzureDataExplorerDataFeedSource azureDataExplorerDataFeedSource =
                ((AzureDataExplorerDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureDataExplorerDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(
                        AzureDataExplorerDataFeedSourceAccessor.getConnectionString(azureDataExplorerDataFeedSource))
                    .setQuery(azureDataExplorerDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureDataExplorerDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureDataExplorerDataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof AzureEventHubsDataFeedSource) {
            final AzureEventHubsDataFeedSource azureEventHubsDataFeedSource =
                ((AzureEventHubsDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureEventHubsDataFeed()
                .setDataSourceParameter(new AzureEventHubsParameter()
                    .setConnectionString(AzureEventHubsDataFeedSourceAccessor.
                        getConnectionString(azureEventHubsDataFeedSource))
                    .setConsumerGroup(azureEventHubsDataFeedSource.getConsumerGroup()));
        } else if (dataFeedSource instanceof AzureTableDataFeedSource) {
            final AzureTableDataFeedSource azureTableDataFeedSource = ((AzureTableDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureTableDataFeed()
                .setDataSourceParameter(new AzureTableParameter()
                    .setConnectionString(AzureTableDataFeedSourceAccessor
                        .getConnectionString(azureTableDataFeedSource))
                    .setTable(azureTableDataFeedSource.getTableName())
                    .setQuery(azureTableDataFeedSource.getQueryScript()));
        } else if (dataFeedSource instanceof InfluxDbDataFeedSource) {
            final InfluxDbDataFeedSource influxDBDataFeedSource = ((InfluxDbDataFeedSource) dataFeedSource);
            dataFeedDetail = new InfluxDBDataFeed()
                .setDataSourceParameter(new InfluxDBParameter()
                    .setConnectionString(influxDBDataFeedSource.getConnectionString())
                    .setDatabase(influxDBDataFeedSource.getDatabase())
                    .setQuery(influxDBDataFeedSource.getQuery())
                    .setPassword(InfluxDbDataFeedSourceAccessor.getPassword(influxDBDataFeedSource))
                    .setUserName(influxDBDataFeedSource.getUserName()));
        } else if (dataFeedSource instanceof MySqlDataFeedSource) {
            final MySqlDataFeedSource mySqlDataFeedSource = ((MySqlDataFeedSource) dataFeedSource);
            dataFeedDetail = new MySqlDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(MySqlDataFeedSourceAccessor.getConnectionString(mySqlDataFeedSource))
                    .setQuery(mySqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof PostgreSqlDataFeedSource) {
            final PostgreSqlDataFeedSource postgreSqlDataFeedSource = ((PostgreSqlDataFeedSource) dataFeedSource);
            dataFeedDetail = new PostgreSqlDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(PostgreSqlDataFeedSourceAccessor.getConnectionString(postgreSqlDataFeedSource))
                    .setQuery(postgreSqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof SqlServerDataFeedSource) {
            final SqlServerDataFeedSource sqlServerDataFeedSource = ((SqlServerDataFeedSource) dataFeedSource);
            dataFeedDetail = new SQLServerDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(SqlServerDataFeedSourceAccessor.getConnectionString(sqlServerDataFeedSource))
                    .setQuery(sqlServerDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(sqlServerDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(sqlServerDataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof MongoDbDataFeedSource) {
            final MongoDbDataFeedSource mongoDbDataFeedSource = ((MongoDbDataFeedSource) dataFeedSource);
            dataFeedDetail = new MongoDBDataFeed()
                .setDataSourceParameter(new MongoDBParameter()
                    .setConnectionString(MongoDbDataFeedSourceAccessor.getConnectionString(mongoDbDataFeedSource))
                    .setCommand(mongoDbDataFeedSource.getCommand())
                    .setDatabase(mongoDbDataFeedSource.getDatabase()));
        } else if (dataFeedSource instanceof AzureDataLakeStorageGen2DataFeedSource) {
            final AzureDataLakeStorageGen2DataFeedSource azureDataLakeStorageGen2DataFeedSource =
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureDataLakeStorageGen2DataFeed()
                .setDataSourceParameter(new AzureDataLakeStorageGen2Parameter()
                    .setAccountKey(AzureDataLakeStorageGen2DataFeedSourceAccessor
                        .getAccountKey(azureDataLakeStorageGen2DataFeedSource))
                    .setAccountName(azureDataLakeStorageGen2DataFeedSource.getAccountName())
                    .setDirectoryTemplate(azureDataLakeStorageGen2DataFeedSource.getDirectoryTemplate())
                    .setFileSystemName(azureDataLakeStorageGen2DataFeedSource.getFileSystemName())
                    .setFileTemplate(azureDataLakeStorageGen2DataFeedSource.getFileTemplate()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureDataLakeStorageGen2DataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureDataLakeStorageGen2DataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof AzureLogAnalyticsDataFeedSource) {
            final AzureLogAnalyticsDataFeedSource azureLogAnalyticsDataFeedSource =
                ((AzureLogAnalyticsDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureLogAnalyticsDataFeed()
                .setDataSourceParameter(new AzureLogAnalyticsParameter()
                    .setTenantId(azureLogAnalyticsDataFeedSource.getTenantId())
                    .setClientId(azureLogAnalyticsDataFeedSource.getClientId())
                    .setClientSecret(AzureLogAnalyticsDataFeedSourceAccessor
                        .getClientSecret(azureLogAnalyticsDataFeedSource))
                    .setWorkspaceId(azureLogAnalyticsDataFeedSource.getWorkspaceId())
                    .setQuery(azureLogAnalyticsDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureLogAnalyticsDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureLogAnalyticsDataFeedSource.getCredentialId());
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                String.format("Data feed source type %s not supported", dataFeedSource.getClass().getCanonicalName())));
        }
        return dataFeedDetail;
    }

    /**
     * Helper to map the SDK level data feed model to service required DataFeedDetailPatch model.
     *
     * @param dataFeedSource the SDK level data feed source.
     *
     * @return the service mapped DataFeedDetailPatch model.
     */
    public static DataFeedDetailPatch toInnerForUpdate(final DataFeedSource dataFeedSource) {
        final DataFeedDetailPatch dataFeedDetailPatch;
        if (dataFeedSource instanceof AzureAppInsightsDataFeedSource) {
            final AzureAppInsightsDataFeedSource azureAppInsightsDataFeedSource =
                ((AzureAppInsightsDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureApplicationInsightsDataFeedPatch()
                .setDataSourceParameter(new AzureApplicationInsightsParameterPatch()
                    .setApiKey(azureAppInsightsDataFeedSource.getApiKey())
                    .setApplicationId(azureAppInsightsDataFeedSource.getApplicationId())
                    .setAzureCloud(azureAppInsightsDataFeedSource.getAzureCloud())
                    .setQuery(azureAppInsightsDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof AzureBlobDataFeedSource) {
            final AzureBlobDataFeedSource azureBlobDataFeedSource = ((AzureBlobDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureBlobDataFeedPatch()
                .setDataSourceParameter(new AzureBlobParameterPatch()
                    .setConnectionString(AzureBlobDataFeedSourceAccessor.getConnectionString(azureBlobDataFeedSource))
                    .setContainer(azureBlobDataFeedSource.getContainer())
                    .setBlobTemplate(azureBlobDataFeedSource.getBlobTemplate()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureBlobDataFeedSource.getAuthenticationType().toString()));
        } else if (dataFeedSource instanceof AzureCosmosDbDataFeedSource) {
            final AzureCosmosDbDataFeedSource azureCosmosDbDataFeedSource = ((AzureCosmosDbDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureCosmosDBDataFeedPatch()
                .setDataSourceParameter(new AzureCosmosDBParameterPatch()
                    .setConnectionString(AzureCosmosDbDataFeedSourceAccessor
                        .getConnectionString(azureCosmosDbDataFeedSource))
                    .setCollectionId(azureCosmosDbDataFeedSource.getCollectionId())
                    .setDatabase(azureCosmosDbDataFeedSource.getDatabase())
                    .setSqlQuery(azureCosmosDbDataFeedSource.getSqlQuery()));
        } else if (dataFeedSource instanceof AzureDataExplorerDataFeedSource) {
            final AzureDataExplorerDataFeedSource azureDataExplorerDataFeedSource =
                ((AzureDataExplorerDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureDataExplorerDataFeedPatch()
                .setDataSourceParameter(new SQLSourceParameterPatch()
                    .setConnectionString(
                        AzureDataExplorerDataFeedSourceAccessor.getConnectionString(azureDataExplorerDataFeedSource))
                    .setQuery(azureDataExplorerDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureDataExplorerDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureDataExplorerDataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof AzureEventHubsDataFeedSource) {
            final AzureEventHubsDataFeedSource azureEventHubsDataFeedSource =
                ((AzureEventHubsDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureEventHubsDataFeedPatch()
                .setDataSourceParameter(new AzureEventHubsParameterPatch()
                    .setConnectionString(AzureEventHubsDataFeedSourceAccessor
                        .getConnectionString(azureEventHubsDataFeedSource))
                    .setConsumerGroup(azureEventHubsDataFeedSource.getConsumerGroup()));
        } else if (dataFeedSource instanceof AzureTableDataFeedSource) {
            final AzureTableDataFeedSource azureTableDataFeedSource = ((AzureTableDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureTableDataFeedPatch()
                .setDataSourceParameter(new AzureTableParameterPatch()
                    .setConnectionString(AzureTableDataFeedSourceAccessor.getConnectionString(azureTableDataFeedSource))
                    .setTable(azureTableDataFeedSource.getTableName())
                    .setQuery(azureTableDataFeedSource.getQueryScript()));
        } else if (dataFeedSource instanceof InfluxDbDataFeedSource) {
            final InfluxDbDataFeedSource influxDBDataFeedSource = ((InfluxDbDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new InfluxDBDataFeedPatch()
                .setDataSourceParameter(new InfluxDBParameterPatch()
                    .setConnectionString(influxDBDataFeedSource.getConnectionString())
                    .setDatabase(influxDBDataFeedSource.getDatabase())
                    .setQuery(influxDBDataFeedSource.getQuery())
                    .setPassword(InfluxDbDataFeedSourceAccessor.getPassword(influxDBDataFeedSource))
                    .setUserName(influxDBDataFeedSource.getUserName()));
        } else if (dataFeedSource instanceof MySqlDataFeedSource) {
            final MySqlDataFeedSource mySqlDataFeedSource = ((MySqlDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new MySqlDataFeedPatch()
                .setDataSourceParameter(new SQLSourceParameterPatch()
                    .setConnectionString(MySqlDataFeedSourceAccessor.getConnectionString(mySqlDataFeedSource))
                    .setQuery(mySqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof PostgreSqlDataFeedSource) {
            final PostgreSqlDataFeedSource postgreSqlDataFeedSource = ((PostgreSqlDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new PostgreSqlDataFeedPatch()
                .setDataSourceParameter(new SQLSourceParameterPatch()
                    .setConnectionString(PostgreSqlDataFeedSourceAccessor.getConnectionString(postgreSqlDataFeedSource))
                    .setQuery(postgreSqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof SqlServerDataFeedSource) {
            final SqlServerDataFeedSource sqlServerDataFeedSource = ((SqlServerDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new SQLServerDataFeedPatch()
                .setDataSourceParameter(new SQLSourceParameterPatch()
                    .setConnectionString(SqlServerDataFeedSourceAccessor.getConnectionString(sqlServerDataFeedSource))
                    .setQuery(sqlServerDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(sqlServerDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(sqlServerDataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof MongoDbDataFeedSource) {
            final MongoDbDataFeedSource mongoDbDataFeedSource = ((MongoDbDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new MongoDBDataFeedPatch()
                .setDataSourceParameter(new MongoDBParameterPatch()
                    .setConnectionString(MongoDbDataFeedSourceAccessor.getConnectionString(mongoDbDataFeedSource))
                    .setCommand(mongoDbDataFeedSource.getCommand())
                    .setDatabase(mongoDbDataFeedSource.getDatabase()));
        } else if (dataFeedSource instanceof AzureDataLakeStorageGen2DataFeedSource) {
            final AzureDataLakeStorageGen2DataFeedSource azureDataLakeStorageGen2DataFeedSource =
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureDataLakeStorageGen2DataFeedPatch()
                .setDataSourceParameter(new AzureDataLakeStorageGen2ParameterPatch()
                    .setAccountKey(AzureDataLakeStorageGen2DataFeedSourceAccessor
                        .getAccountKey(azureDataLakeStorageGen2DataFeedSource))
                    .setAccountName(azureDataLakeStorageGen2DataFeedSource.getAccountName())
                    .setDirectoryTemplate(azureDataLakeStorageGen2DataFeedSource.getDirectoryTemplate())
                    .setFileSystemName(azureDataLakeStorageGen2DataFeedSource.getFileSystemName())
                    .setFileTemplate(azureDataLakeStorageGen2DataFeedSource.getFileTemplate()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureDataLakeStorageGen2DataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureDataLakeStorageGen2DataFeedSource.getCredentialId());
        } else if (dataFeedSource instanceof AzureLogAnalyticsDataFeedSource) {
            final AzureLogAnalyticsDataFeedSource azureLogAnalyticsDataFeedSource =
                ((AzureLogAnalyticsDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureLogAnalyticsDataFeedPatch()
                .setDataSourceParameter(new AzureLogAnalyticsParameterPatch()
                    .setTenantId(azureLogAnalyticsDataFeedSource.getTenantId())
                    .setClientId(azureLogAnalyticsDataFeedSource.getClientId())
                    .setClientSecret(AzureLogAnalyticsDataFeedSourceAccessor
                        .getClientSecret(azureLogAnalyticsDataFeedSource))
                    .setWorkspaceId(azureLogAnalyticsDataFeedSource.getWorkspaceId())
                    .setQuery(azureLogAnalyticsDataFeedSource.getQuery()))
                .setAuthenticationType(AuthenticationTypeEnum
                    .fromString(azureLogAnalyticsDataFeedSource.getAuthenticationType().toString()))
                .setCredentialId(azureLogAnalyticsDataFeedSource.getCredentialId());
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                String.format("Data feed source type %s not supported.",
                    dataFeedSource.getClass().getCanonicalName())));
        }

        return dataFeedDetailPatch;
    }

    public static List<com.azure.ai.metricsadvisor.implementation.models.DataFeedDimension>
        toInnerDimensionsListForCreate(List<DataFeedDimension> dimensions) {
        List<com.azure.ai.metricsadvisor.implementation.models.DataFeedDimension> innerDimensions = null;
        if (dimensions != null) {
            innerDimensions = new ArrayList<>();
            for (DataFeedDimension dimension : dimensions) {
                innerDimensions.add(new com.azure.ai.metricsadvisor.implementation.models.DataFeedDimension()
                    .setName(dimension.getName()).setDisplayName(dimension.getDisplayName()));
            }
        }
        return innerDimensions;
    }

    public static List<com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric>
        toInnerMetricsListForCreate(List<DataFeedMetric> metrics) {
        List<com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric> innerMetrics = null;
        if (metrics != null) {
            innerMetrics = new ArrayList<>();
            for (DataFeedMetric metric : metrics) {
                innerMetrics.add(new com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric()
                    .setName(metric.getName())
                    .setDisplayName(metric.getDisplayName())
                    .setDescription(metric.getDescription()));
            }
        }
        return innerMetrics;
    }

    private static List<DataFeedDimension>
        fromInnerDimensionList(List<com.azure.ai.metricsadvisor.implementation.models.DataFeedDimension> innerList) {
        if (innerList == null) {
            return null;
        } else {
            List<DataFeedDimension> dimensions = new ArrayList<>();
            for (com.azure.ai.metricsadvisor.implementation.models.DataFeedDimension inner : innerList) {
                dimensions.add(new DataFeedDimension(inner.getName()).setDisplayName(inner.getDisplayName()));
            }
            return dimensions;
        }
    }

    private static List<DataFeedMetric> fromInnerMetricList(
        List<com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric> innerList) {
        if (innerList == null) {
            return null;
        } else {
            List<DataFeedMetric> metrics = new ArrayList<>();
            for (com.azure.ai.metricsadvisor.implementation.models.DataFeedMetric inner : innerList) {
                DataFeedMetric metric = new DataFeedMetric(inner.getName())
                    .setDisplayName(inner.getDisplayName())
                    .setDescription(inner.getDescription());
                DataFeedMetricAccessor.setId(metric, inner.getId());
                metrics.add(metric);
            }
            return metrics;
        }
    }
}
