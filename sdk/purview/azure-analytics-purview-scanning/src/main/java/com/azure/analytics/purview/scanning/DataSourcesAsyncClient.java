package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.DataSourcesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class, isAsync = true)
public final class DataSourcesAsyncClient {
    private final DataSourcesImpl serviceClient;

    /**
     * Initializes an instance of DataSources client.
     *
     * @param serviceClient the service client implementation.
     */
    DataSourcesAsyncClient(DataSourcesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Creates or Updates a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createOrUpdateWithResponse(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateWithResponseAsync(dataSourceName, requestOptions);
    }

    /**
     * Creates or Updates a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createOrUpdate(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateAsync(dataSourceName, requestOptions);
    }

    /**
     * Get a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponse(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.getWithResponseAsync(dataSourceName, requestOptions);
    }

    /**
     * Get a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> get(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.getAsync(dataSourceName, requestOptions);
    }

    /**
     * Deletes a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponse(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.deleteWithResponseAsync(dataSourceName, requestOptions);
    }

    /**
     * Deletes a data source.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scans: [
     *         {
     *             id: String
     *             name: String
     *             scanResults: [
     *                 {
     *                     parentId: String
     *                     id: String
     *                     resourceId: String
     *                     status: String
     *                     assetsDiscovered: Long
     *                     assetsClassified: Long
     *                     diagnostics: {
     *                         notifications: [
     *                             {
     *                                 message: String
     *                                 code: Integer
     *                             }
     *                         ]
     *                         exceptionCountMap: {
     *                             String: int
     *                         }
     *                     }
     *                     startTime: OffsetDateTime
     *                     queuedTime: OffsetDateTime
     *                     pipelineStartTime: OffsetDateTime
     *                     endTime: OffsetDateTime
     *                     scanRulesetVersion: Integer
     *                     scanRulesetType: String(Custom/System)
     *                     scanLevelType: String(Full/Incremental)
     *                     errorMessage: String
     *                     error: {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     (recursive schema, see above)
     *                                 ]
     *                             }
     *                         ]
     *                     }
     *                     runType: String
     *                     dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                 }
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> delete(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.deleteAsync(dataSourceName, requestOptions);
    }

    /**
     * List data sources in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             id: String
     *             name: String
     *             scans: [
     *                 {
     *                     id: String
     *                     name: String
     *                     scanResults: [
     *                         {
     *                             parentId: String
     *                             id: String
     *                             resourceId: String
     *                             status: String
     *                             assetsDiscovered: Long
     *                             assetsClassified: Long
     *                             diagnostics: {
     *                                 notifications: [
     *                                     {
     *                                         message: String
     *                                         code: Integer
     *                                     }
     *                                 ]
     *                                 exceptionCountMap: {
     *                                     String: int
     *                                 }
     *                             }
     *                             startTime: OffsetDateTime
     *                             queuedTime: OffsetDateTime
     *                             pipelineStartTime: OffsetDateTime
     *                             endTime: OffsetDateTime
     *                             scanRulesetVersion: Integer
     *                             scanRulesetType: String(Custom/System)
     *                             scanLevelType: String(Full/Incremental)
     *                             errorMessage: String
     *                             error: {
     *                                 code: String
     *                                 message: String
     *                                 target: String
     *                                 details: [
     *                                     {
     *                                         code: String
     *                                         message: String
     *                                         target: String
     *                                         details: [
     *                                             (recursive schema, see above)
     *                                         ]
     *                                     }
     *                                 ]
     *                             }
     *                             runType: String
     *                             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *                         }
     *                     ]
     *                 }
     *             ]
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listAll(RequestOptions requestOptions) {
        return this.serviceClient.listAllAsync(requestOptions);
    }
}
