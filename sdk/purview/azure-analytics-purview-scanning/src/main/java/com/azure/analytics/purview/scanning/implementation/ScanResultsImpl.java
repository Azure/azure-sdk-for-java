package com.azure.analytics.purview.scanning.implementation;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in ScanResults. */
public final class ScanResultsImpl {
    /** The proxy service used to perform REST calls. */
    private final ScanResultsService service;

    /** The service client containing this operation class. */
    private final MicrosoftScanningClientImpl client;

    /**
     * Initializes an instance of ScanResultsImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ScanResultsImpl(MicrosoftScanningClientImpl client) {
        this.service =
                RestProxy.create(ScanResultsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MicrosoftScanningClientScanResults to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{Endpoint}")
    @ServiceInterface(name = "MicrosoftScanningCli")
    private interface ScanResultsService {
        @Put("/datasources/{dataSourceName}/scans/{scanName}/runs/{runId}")
        Mono<Response<BinaryData>> runScan(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @PathParam("scanName") String scanName,
                @PathParam("runId") String runId,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/datasources/{dataSourceName}/scans/{scanName}/runs/{runId}/:cancel")
        Mono<Response<BinaryData>> cancelScan(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @PathParam("scanName") String scanName,
                @PathParam("runId") String runId,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/datasources/{dataSourceName}/scans/{scanName}/runs")
        Mono<Response<BinaryData>> listScanHistory(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @PathParam("scanName") String scanName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("{nextLink}")
        Mono<Response<BinaryData>> listScanHistoryNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
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
    public Mono<Response<BinaryData>> runScanWithResponseAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.runScan(
                                this.client.getEndpoint(),
                                dataSourceName,
                                scanName,
                                runId,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> runScanWithResponseAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.runScan(
                this.client.getEndpoint(),
                dataSourceName,
                scanName,
                runId,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
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
    public Mono<BinaryData> runScanAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        return runScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
    public Mono<BinaryData> runScanAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        return runScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
        return runScanAsync(dataSourceName, scanName, runId, requestOptions).block();
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
        return runScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> cancelScanWithResponseAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.cancelScan(
                                this.client.getEndpoint(),
                                dataSourceName,
                                scanName,
                                runId,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> cancelScanWithResponseAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.cancelScan(
                this.client.getEndpoint(),
                dataSourceName,
                scanName,
                runId,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
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
    public Mono<BinaryData> cancelScanAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions) {
        return cancelScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
    public Mono<BinaryData> cancelScanAsync(
            String dataSourceName, String scanName, String runId, RequestOptions requestOptions, Context context) {
        return cancelScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions, context)
                .flatMap(
                        (Response<BinaryData> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
        return cancelScanAsync(dataSourceName, scanName, runId, requestOptions).block();
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
        return cancelScanWithResponseAsync(dataSourceName, scanName, runId, requestOptions, context).block();
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listScanHistorySinglePageAsync(
            String dataSourceName, String scanName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listScanHistory(
                                        this.client.getEndpoint(),
                                        dataSourceName,
                                        scanName,
                                        this.client.getApiVersion(),
                                        accept,
                                        requestOptions,
                                        context))
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        getValues(res.getValue(), "value"),
                                        getNextLink(res.getValue(), "nextLink"),
                                        null));
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listScanHistorySinglePageAsync(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listScanHistory(
                        this.client.getEndpoint(),
                        dataSourceName,
                        scanName,
                        this.client.getApiVersion(),
                        accept,
                        requestOptions,
                        context)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        getValues(res.getValue(), "value"),
                                        getNextLink(res.getValue(), "nextLink"),
                                        null));
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
    public PagedFlux<BinaryData> listScanHistoryAsync(
            String dataSourceName, String scanName, RequestOptions requestOptions) {
        return new PagedFlux<>(
                () -> listScanHistorySinglePageAsync(dataSourceName, scanName, requestOptions),
                nextLink -> listScanHistoryNextSinglePageAsync(nextLink, null));
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
    public PagedFlux<BinaryData> listScanHistoryAsync(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
                () -> listScanHistorySinglePageAsync(dataSourceName, scanName, requestOptions, context),
                nextLink -> listScanHistoryNextSinglePageAsync(nextLink, null, context));
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
        return new PagedIterable<>(listScanHistoryAsync(dataSourceName, scanName, requestOptions));
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
        return new PagedIterable<>(listScanHistoryAsync(dataSourceName, scanName, requestOptions, context));
    }

    /**
     * Get the next page of items.
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
     * @param nextLink The nextLink parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listScanHistoryNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listScanHistoryNext(
                                        nextLink, this.client.getEndpoint(), accept, requestOptions, context))
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        getValues(res.getValue(), "value"),
                                        getNextLink(res.getValue(), "nextLink"),
                                        null));
    }

    /**
     * Get the next page of items.
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
     * @param nextLink The nextLink parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listScanHistoryNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listScanHistoryNext(nextLink, this.client.getEndpoint(), accept, requestOptions, context)
                .map(
                        res ->
                                new PagedResponseBase<>(
                                        res.getRequest(),
                                        res.getStatusCode(),
                                        res.getHeaders(),
                                        getValues(res.getValue(), "value"),
                                        getNextLink(res.getValue(), "nextLink"),
                                        null));
    }

    private List<BinaryData> getValues(BinaryData binaryData, String path) {
        try {
            Map<?, ?> obj = binaryData.toObject(Map.class);
            List<?> values = (List<?>) obj.get(path);
            return values.stream().map(BinaryData::fromObject).collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    private String getNextLink(BinaryData binaryData, String path) {
        try {
            Map<?, ?> obj = binaryData.toObject(Map.class);
            return (String) obj.get(path);
        } catch (Exception e) {
            return null;
        }
    }
}
