package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.ScansImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class)
public final class ScansClient {
    private final ScansImpl serviceClient;

    /**
     * Initializes an instance of Scans client.
     *
     * @param serviceClient the service client implementation.
     */
    ScansClient(ScansImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Creates an instance of a scan.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdate(
            String dataSourceName, String scanName, BinaryData body, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdate(dataSourceName, scanName, body, requestOptions);
    }

    /**
     * Creates an instance of a scan.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(
            String dataSourceName, String scanName, BinaryData body, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createOrUpdateWithResponse(dataSourceName, scanName, body, requestOptions, context);
    }

    /**
     * Gets a scan information.
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
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.get(dataSourceName, scanName, requestOptions);
    }

    /**
     * Gets a scan information.
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
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getWithResponse(dataSourceName, scanName, requestOptions, context);
    }

    /**
     * Deletes the scan associated with the data source.
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
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData delete(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.delete(dataSourceName, scanName, requestOptions);
    }

    /**
     * Deletes the scan associated with the data source.
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
     *     scanResults: [
     *         {
     *             parentId: String
     *             id: String
     *             resourceId: String
     *             status: String
     *             assetsDiscovered: Long
     *             assetsClassified: Long
     *             diagnostics: {
     *                 notifications: [
     *                     {
     *                         message: String
     *                         code: Integer
     *                     }
     *                 ]
     *                 exceptionCountMap: {
     *                     String: int
     *                 }
     *             }
     *             startTime: OffsetDateTime
     *             queuedTime: OffsetDateTime
     *             pipelineStartTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             scanRulesetVersion: Integer
     *             scanRulesetType: String(Custom/System)
     *             scanLevelType: String(Full/Incremental)
     *             errorMessage: String
     *             error: {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     {
     *                         code: String
     *                         message: String
     *                         target: String
     *                         details: [
     *                             (recursive schema, see above)
     *                         ]
     *                     }
     *                 ]
     *             }
     *             runType: String
     *             dataSourceType: String(None/AzureSubscription/AzureResourceGroup/AzureSynapseWorkspace/AzureSynapse/AdlsGen1/AdlsGen2/AmazonAccount/AmazonS3/AmazonSql/AzureCosmosDb/AzureDataExplorer/AzureFileService/AzureSqlDatabase/AmazonPostgreSql/AzurePostgreSql/SqlServerDatabase/AzureSqlDatabaseManagedInstance/AzureSqlDataWarehouse/AzureMySql/AzureStorage/Teradata/Oracle/SapS4Hana/SapEcc/PowerBI)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteWithResponse(dataSourceName, scanName, requestOptions, context);
    }

    /**
     * List scans in data source.
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
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listByDataSource(String dataSourceName, RequestOptions requestOptions) {
        return this.serviceClient.listByDataSource(dataSourceName, requestOptions);
    }

    /**
     * List scans in data source.
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
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listByDataSource(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listByDataSource(dataSourceName, requestOptions, context);
    }
}
