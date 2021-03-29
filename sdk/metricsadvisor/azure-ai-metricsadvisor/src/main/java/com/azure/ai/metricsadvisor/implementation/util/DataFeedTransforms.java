// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureApplicationInsightsParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureBlobParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureCosmosDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataExplorerDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataExplorerDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2DataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2DataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureDataLakeStorageGen2Parameter;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.AzureTableParameter;
import com.azure.ai.metricsadvisor.implementation.models.DataFeedDetail;
import com.azure.ai.metricsadvisor.implementation.models.DataFeedDetailPatch;
import com.azure.ai.metricsadvisor.implementation.models.ElasticsearchDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.ElasticsearchDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.ElasticsearchParameter;
import com.azure.ai.metricsadvisor.implementation.models.HttpRequestDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.HttpRequestDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.HttpRequestParameter;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.InfluxDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.MongoDBParameter;
import com.azure.ai.metricsadvisor.implementation.models.MySqlDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.MySqlDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.PostgreSqlDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.PostgreSqlDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.SQLServerDataFeed;
import com.azure.ai.metricsadvisor.implementation.models.SQLServerDataFeedPatch;
import com.azure.ai.metricsadvisor.implementation.models.SqlSourceParameter;
import com.azure.ai.metricsadvisor.models.AzureAppInsightsDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureBlobDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureCosmosDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureDataExplorerDataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureDataLakeStorageGen2DataFeedSource;
import com.azure.ai.metricsadvisor.models.AzureTableDataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedAccessMode;
import com.azure.ai.metricsadvisor.models.DataFeedAutoRollUpMethod;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.models.DataFeedRollupType;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataFeedSource;
import com.azure.ai.metricsadvisor.models.DataFeedSourceType;
import com.azure.ai.metricsadvisor.models.DataFeedStatus;
import com.azure.ai.metricsadvisor.models.DataFeedMissingDataPointFillType;
import com.azure.ai.metricsadvisor.models.ElasticsearchDataFeedSource;
import com.azure.ai.metricsadvisor.models.HttpRequestDataFeedSource;
import com.azure.ai.metricsadvisor.models.InfluxDBDataFeedSource;
import com.azure.ai.metricsadvisor.models.MongoDBDataFeedSource;
import com.azure.ai.metricsadvisor.models.MySqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.PostgreSqlDataFeedSource;
import com.azure.ai.metricsadvisor.models.SQLServerDataFeedSource;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
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
            .setSchema(new DataFeedSchema(dataFeedDetail.getMetrics())
                .setDimensions(dataFeedDetail.getDimension())
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
                .setAdminEmails(dataFeedDetail.getAdmins())
                .setRollupSettings(new DataFeedRollupSettings()
                    .setAlreadyRollup(dataFeedDetail.getAllUpIdentification())
                    .setAutoRollup(DataFeedAutoRollUpMethod.fromString(dataFeedDetail.getRollUpMethod().toString()),
                        dataFeedDetail.getRollUpColumns())
                    .setRollupType(DataFeedRollupType.fromString(dataFeedDetail.getNeedRollup().toString())))
                .setActionLinkTemplate(dataFeedDetail.getActionLinkTemplate())
                .setViewerEmails(dataFeedDetail.getViewers()));

        DataFeedHelper.setId(dataFeed, dataFeedDetail.getDataFeedId().toString());
        DataFeedHelper.setCreatedTime(dataFeed, dataFeedDetail.getCreatedTime());
        DataFeedHelper.setIsAdmin(dataFeed, dataFeedDetail.isAdmin());
        DataFeedHelper.setCreator(dataFeed, dataFeedDetail.getCreator());
        DataFeedHelper.setStatus(dataFeed, DataFeedStatus.fromString(dataFeedDetail.getStatus().toString()));
        DataFeedHelper.setMetricIds(dataFeed,
            dataFeedDetail.getMetrics().stream()
                .collect(Collectors.toMap(DataFeedMetric::getId, DataFeedMetric::getName)));
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
            dataFeed.setSource(new AzureBlobDataFeedSource(dataSourceParameter.getConnectionString(),
                dataSourceParameter.getContainer(), dataSourceParameter.getBlobTemplate()));
            dataFeedSourceType = DataFeedSourceType.AZURE_BLOB;
        } else if (dataFeedDetail instanceof AzureCosmosDBDataFeed) {
            final AzureCosmosDBParameter dataSourceParameter =
                ((AzureCosmosDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureCosmosDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getSqlQuery(),
                dataSourceParameter.getDatabase(),
                dataSourceParameter.getCollectionId()
            ));
            dataFeedSourceType = DataFeedSourceType.AZURE_COSMOS_DB;
        } else if (dataFeedDetail instanceof AzureDataExplorerDataFeed) {
            final SqlSourceParameter dataSourceParameter =
                ((AzureDataExplorerDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureDataExplorerDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery()
            ));
            dataFeedSourceType = DataFeedSourceType.AZURE_DATA_EXPLORER;
        } else if (dataFeedDetail instanceof AzureTableDataFeed) {
            final AzureTableParameter dataSourceParameter = ((AzureTableDataFeed) dataFeedDetail)
                .getDataSourceParameter();
            dataFeed.setSource(new AzureTableDataFeedSource(dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery(), dataSourceParameter.getTable()));
            dataFeedSourceType = DataFeedSourceType.AZURE_TABLE;
        } else if (dataFeedDetail instanceof HttpRequestDataFeed) {
            final HttpRequestParameter dataSourceParameter =
                ((HttpRequestDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new HttpRequestDataFeedSource(
                dataSourceParameter.getUrl(),
                dataSourceParameter.getHttpMethod()
            ));
            dataFeedSourceType = DataFeedSourceType.HTTP_REQUEST;
        } else if (dataFeedDetail instanceof InfluxDBDataFeed) {
            final InfluxDBParameter dataSourceParameter = ((InfluxDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new InfluxDBDataFeedSource(
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
            dataFeed.setSource(new SQLServerDataFeedSource(dataSourceParameter.getConnectionString(),
                dataSourceParameter.getQuery()));
            dataFeedSourceType = DataFeedSourceType.SQL_SERVER_DB;
        } else if (dataFeedDetail instanceof MongoDBDataFeed) {
            final MongoDBParameter dataSourceParameter = ((MongoDBDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new MongoDBDataFeedSource(
                dataSourceParameter.getConnectionString(),
                dataSourceParameter.getDatabase(),
                dataSourceParameter.getCommand()
            ));
            dataFeedSourceType = DataFeedSourceType.MONGO_DB;
        } else if (dataFeedDetail instanceof ElasticsearchDataFeed) {
            final ElasticsearchParameter elasticsearchParameter =
                ((ElasticsearchDataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new ElasticsearchDataFeedSource(
                elasticsearchParameter.getHost(),
                elasticsearchParameter.getPort(),
                elasticsearchParameter.getAuthHeader(),
                elasticsearchParameter.getQuery()
                ));
            dataFeedSourceType = DataFeedSourceType.ELASTIC_SEARCH;
        } else if (dataFeedDetail instanceof AzureDataLakeStorageGen2DataFeed) {
            final AzureDataLakeStorageGen2Parameter azureDataLakeStorageGen2Parameter =
                ((AzureDataLakeStorageGen2DataFeed) dataFeedDetail).getDataSourceParameter();
            dataFeed.setSource(new AzureDataLakeStorageGen2DataFeedSource(
                azureDataLakeStorageGen2Parameter.getAccountName(),
                azureDataLakeStorageGen2Parameter.getAccountKey(),
                azureDataLakeStorageGen2Parameter.getFileSystemName(),
                azureDataLakeStorageGen2Parameter.getDirectoryTemplate(),
                azureDataLakeStorageGen2Parameter.getFileTemplate()
            ));
            dataFeedSourceType = DataFeedSourceType.AZURE_DATA_LAKE_STORAGE_GEN2;
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
                    .setConnectionString(azureBlobDataFeedSource.getConnectionString())
                    .setContainer(azureBlobDataFeedSource.getContainer())
                    .setBlobTemplate(azureBlobDataFeedSource.getBlobTemplate()));
        } else if (dataFeedSource instanceof AzureCosmosDataFeedSource) {
            final AzureCosmosDataFeedSource azureCosmosDataFeedSource = ((AzureCosmosDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureCosmosDBDataFeed()
                .setDataSourceParameter(new AzureCosmosDBParameter()
                    .setConnectionString(azureCosmosDataFeedSource.getConnectionString())
                    .setCollectionId(azureCosmosDataFeedSource.getCollectionId())
                    .setDatabase(azureCosmosDataFeedSource.getDatabase())
                    .setSqlQuery(azureCosmosDataFeedSource.getSqlQuery()));
        } else if (dataFeedSource instanceof AzureDataExplorerDataFeedSource) {
            final AzureDataExplorerDataFeedSource azureDataExplorerDataFeedSource =
                ((AzureDataExplorerDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureDataExplorerDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(azureDataExplorerDataFeedSource.getConnectionString())
                    .setQuery(azureDataExplorerDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof AzureTableDataFeedSource) {
            final AzureTableDataFeedSource azureTableDataFeedSource = ((AzureTableDataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureTableDataFeed()
                .setDataSourceParameter(new AzureTableParameter()
                    .setConnectionString(azureTableDataFeedSource.getConnectionString())
                    .setTable(azureTableDataFeedSource.getTableName())
                    .setQuery(azureTableDataFeedSource.getQueryScript()));
        } else if (dataFeedSource instanceof HttpRequestDataFeedSource) {
            final HttpRequestDataFeedSource httpRequestDataFeedSource = ((HttpRequestDataFeedSource) dataFeedSource);
            dataFeedDetail = new HttpRequestDataFeed()
                .setDataSourceParameter(new HttpRequestParameter()
                    .setHttpHeader(httpRequestDataFeedSource.getHttpHeader())
                    .setHttpMethod(httpRequestDataFeedSource.getHttpMethod())
                    .setPayload(httpRequestDataFeedSource.getPayload())
                    .setUrl(httpRequestDataFeedSource.getUrl()));
        } else if (dataFeedSource instanceof InfluxDBDataFeedSource) {
            final InfluxDBDataFeedSource influxDBDataFeedSource = ((InfluxDBDataFeedSource) dataFeedSource);
            dataFeedDetail = new InfluxDBDataFeed()
                .setDataSourceParameter(new InfluxDBParameter()
                    .setConnectionString(influxDBDataFeedSource.getConnectionString())
                    .setDatabase(influxDBDataFeedSource.getDatabase())
                    .setQuery(influxDBDataFeedSource.getQuery())
                    .setPassword(influxDBDataFeedSource.getPassword())
                    .setUserName(influxDBDataFeedSource.getUserName()));
        } else if (dataFeedSource instanceof MySqlDataFeedSource) {
            final MySqlDataFeedSource mySqlDataFeedSource = ((MySqlDataFeedSource) dataFeedSource);
            dataFeedDetail = new MySqlDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(mySqlDataFeedSource.getConnectionString())
                    .setQuery(mySqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof PostgreSqlDataFeedSource) {
            final PostgreSqlDataFeedSource postgreSqlDataFeedSource = ((PostgreSqlDataFeedSource) dataFeedSource);
            dataFeedDetail = new PostgreSqlDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(postgreSqlDataFeedSource.getConnectionString())
                    .setQuery(postgreSqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof SQLServerDataFeedSource) {
            final SQLServerDataFeedSource sqlServerDataFeedSource = ((SQLServerDataFeedSource) dataFeedSource);
            dataFeedDetail = new SQLServerDataFeed()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(sqlServerDataFeedSource.getConnectionString())
                    .setQuery(sqlServerDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof MongoDBDataFeedSource) {
            final MongoDBDataFeedSource azureCosmosDataFeedSource = ((MongoDBDataFeedSource) dataFeedSource);
            dataFeedDetail = new MongoDBDataFeed()
                .setDataSourceParameter(new MongoDBParameter()
                    .setConnectionString(azureCosmosDataFeedSource.getConnectionString())
                    .setCommand(azureCosmosDataFeedSource.getCommand())
                    .setDatabase(azureCosmosDataFeedSource.getDatabase()));
        } else if (dataFeedSource instanceof AzureDataLakeStorageGen2DataFeedSource) {
            final AzureDataLakeStorageGen2DataFeedSource azureDataLakeStorageGen2DataFeedSource =
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeedSource);
            dataFeedDetail = new AzureDataLakeStorageGen2DataFeed()
                .setDataSourceParameter(new AzureDataLakeStorageGen2Parameter()
                    .setAccountKey(azureDataLakeStorageGen2DataFeedSource.getAccountKey())
                    .setAccountName(azureDataLakeStorageGen2DataFeedSource.getAccountName())
                    .setDirectoryTemplate(azureDataLakeStorageGen2DataFeedSource.getDirectoryTemplate())
                    .setFileSystemName(azureDataLakeStorageGen2DataFeedSource.getFileSystemName())
                    .setFileTemplate(azureDataLakeStorageGen2DataFeedSource.getFileTemplate()));
        } else if (dataFeedSource instanceof ElasticsearchDataFeedSource) {
            final ElasticsearchDataFeedSource elasticsearchDataFeedSource =
                ((ElasticsearchDataFeedSource) dataFeedSource);
            dataFeedDetail = new ElasticsearchDataFeed()
                .setDataSourceParameter(new ElasticsearchParameter()
                    .setAuthHeader(elasticsearchDataFeedSource.getAuthHeader())
                    .setHost(elasticsearchDataFeedSource.getHost())
                    .setPort(elasticsearchDataFeedSource.getPort())
                    .setQuery(elasticsearchDataFeedSource.getQuery()));
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
                .setDataSourceParameter(new AzureApplicationInsightsParameter()
                    .setApiKey(azureAppInsightsDataFeedSource.getApiKey())
                    .setApplicationId(azureAppInsightsDataFeedSource.getApplicationId())
                    .setAzureCloud(azureAppInsightsDataFeedSource.getAzureCloud())
                    .setQuery(azureAppInsightsDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof AzureBlobDataFeedSource) {
            final AzureBlobDataFeedSource azureBlobDataFeedSource = ((AzureBlobDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureBlobDataFeedPatch()
                .setDataSourceParameter(new AzureBlobParameter()
                    .setConnectionString(azureBlobDataFeedSource.getConnectionString())
                    .setContainer(azureBlobDataFeedSource.getContainer())
                    .setBlobTemplate(azureBlobDataFeedSource.getBlobTemplate()));
        } else if (dataFeedSource instanceof AzureCosmosDataFeedSource) {
            final AzureCosmosDataFeedSource azureCosmosDataFeedSource = ((AzureCosmosDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureCosmosDBDataFeedPatch()
                .setDataSourceParameter(new AzureCosmosDBParameter()
                    .setConnectionString(azureCosmosDataFeedSource.getConnectionString())
                    .setCollectionId(azureCosmosDataFeedSource.getCollectionId())
                    .setDatabase(azureCosmosDataFeedSource.getDatabase())
                    .setSqlQuery(azureCosmosDataFeedSource.getSqlQuery()));
        } else if (dataFeedSource instanceof AzureDataExplorerDataFeedSource) {
            final AzureDataExplorerDataFeedSource azureDataExplorerDataFeedSource =
                ((AzureDataExplorerDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureDataExplorerDataFeedPatch()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(azureDataExplorerDataFeedSource.getConnectionString())
                    .setQuery(azureDataExplorerDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof AzureTableDataFeedSource) {
            final AzureTableDataFeedSource azureTableDataFeedSource = ((AzureTableDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureTableDataFeedPatch()
                .setDataSourceParameter(new AzureTableParameter()
                    .setConnectionString(azureTableDataFeedSource.getConnectionString())
                    .setTable(azureTableDataFeedSource.getTableName())
                    .setQuery(azureTableDataFeedSource.getQueryScript()));
        } else if (dataFeedSource instanceof HttpRequestDataFeedSource) {
            final HttpRequestDataFeedSource httpRequestDataFeedSource = ((HttpRequestDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new HttpRequestDataFeedPatch()
                .setDataSourceParameter(new HttpRequestParameter()
                    .setHttpHeader(httpRequestDataFeedSource.getHttpHeader())
                    .setHttpMethod(httpRequestDataFeedSource.getHttpMethod())
                    .setPayload(httpRequestDataFeedSource.getPayload())
                    .setUrl(httpRequestDataFeedSource.getUrl()));
        } else if (dataFeedSource instanceof InfluxDBDataFeedSource) {
            final InfluxDBDataFeedSource influxDBDataFeedSource = ((InfluxDBDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new InfluxDBDataFeedPatch()
                .setDataSourceParameter(new InfluxDBParameter()
                    .setConnectionString(influxDBDataFeedSource.getConnectionString())
                    .setDatabase(influxDBDataFeedSource.getDatabase())
                    .setQuery(influxDBDataFeedSource.getQuery())
                    .setPassword(influxDBDataFeedSource.getPassword())
                    .setUserName(influxDBDataFeedSource.getUserName()));
        } else if (dataFeedSource instanceof MySqlDataFeedSource) {
            final MySqlDataFeedSource mySqlDataFeedSource = ((MySqlDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new MySqlDataFeedPatch()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(mySqlDataFeedSource.getConnectionString())
                    .setQuery(mySqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof PostgreSqlDataFeedSource) {
            final PostgreSqlDataFeedSource postgreSqlDataFeedSource = ((PostgreSqlDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new PostgreSqlDataFeedPatch()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(postgreSqlDataFeedSource.getConnectionString())
                    .setQuery(postgreSqlDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof SQLServerDataFeedSource) {
            final SQLServerDataFeedSource sqlServerDataFeedSource = ((SQLServerDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new SQLServerDataFeedPatch()
                .setDataSourceParameter(new SqlSourceParameter()
                    .setConnectionString(sqlServerDataFeedSource.getConnectionString())
                    .setQuery(sqlServerDataFeedSource.getQuery()));
        } else if (dataFeedSource instanceof MongoDBDataFeedSource) {
            final MongoDBDataFeedSource azureCosmosDataFeedSource = ((MongoDBDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new MongoDBDataFeedPatch()
                .setDataSourceParameter(new MongoDBParameter()
                    .setConnectionString(azureCosmosDataFeedSource.getConnectionString())
                    .setCommand(azureCosmosDataFeedSource.getCommand())
                    .setDatabase(azureCosmosDataFeedSource.getDatabase()));
        } else if (dataFeedSource instanceof AzureDataLakeStorageGen2DataFeedSource) {
            final AzureDataLakeStorageGen2DataFeedSource azureDataLakeStorageGen2DataFeedSource =
                ((AzureDataLakeStorageGen2DataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new AzureDataLakeStorageGen2DataFeedPatch()
                .setDataSourceParameter(new AzureDataLakeStorageGen2Parameter()
                    .setAccountKey(azureDataLakeStorageGen2DataFeedSource.getAccountKey())
                    .setAccountName(azureDataLakeStorageGen2DataFeedSource.getAccountName())
                    .setDirectoryTemplate(azureDataLakeStorageGen2DataFeedSource.getDirectoryTemplate())
                    .setFileSystemName(azureDataLakeStorageGen2DataFeedSource.getFileSystemName())
                    .setFileTemplate(azureDataLakeStorageGen2DataFeedSource.getFileTemplate()));
        } else if (dataFeedSource instanceof ElasticsearchDataFeedSource) {
            final ElasticsearchDataFeedSource elasticsearchDataFeedSource =
                ((ElasticsearchDataFeedSource) dataFeedSource);
            dataFeedDetailPatch = new ElasticsearchDataFeedPatch()
                .setDataSourceParameter(new ElasticsearchParameter()
                    .setAuthHeader(elasticsearchDataFeedSource.getAuthHeader())
                    .setHost(elasticsearchDataFeedSource.getHost())
                    .setPort(elasticsearchDataFeedSource.getPort())
                    .setQuery(elasticsearchDataFeedSource.getQuery()));
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                String.format("Data feed source type %s not supported.",
                    dataFeedSource.getClass().getCanonicalName())));
        }
        return dataFeedDetailPatch;
    }
}
