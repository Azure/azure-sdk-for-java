package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.ScanResultsImpl;
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
public final class ScanResultClient {
    private final ScanResultsImpl serviceClient;

    /**
     * Initializes an instance of ScanResults client.
     *
     * @param serviceClient the service client implementation.
     */
    ScanResultClient(ScanResultsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Runs the scan.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>scanLevel</td><td>String</td><td>No</td><td>The scanLevel parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param runId The runId parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData runScan(String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        return this.serviceClient.runScan(dataSourceName, scanName, runId, requestOptions);
    }

    /**
     * Runs the scan.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>scanLevel</td><td>String</td><td>No</td><td>The scanLevel parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param runId The runId parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> runScanWithResponse(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        return this.serviceClient.runScanWithResponse(dataSourceName, scanName, runId, requestOptions, context);
    }

    /**
     * Cancels a scan.
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
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param runId The runId parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData cancelScan(String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        return this.serviceClient.cancelScan(dataSourceName, scanName, runId, requestOptions);
    }

    /**
     * Cancels a scan.
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
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param runId The runId parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> cancelScanWithResponse(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        return this.serviceClient.cancelScanWithResponse(dataSourceName, scanName, runId, requestOptions, context);
    }

    /**
     * Lists the scan history of a scan.
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
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listScanHistory(
            String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.listScanHistory(dataSourceName, scanName, requestOptions);
    }

    /**
     * Lists the scan history of a scan.
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
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listScanHistory(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listScanHistory(dataSourceName, scanName, requestOptions, context);
    }
}
