package com.azure.analytics.purview.scanning.implementation;

import com.azure.core.annotation.Delete;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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

/** An instance of this class provides access to all the operations defined in DataSources. */
public final class DataSourcesImpl {
    /** The proxy service used to perform REST calls. */
    private final DataSourcesService service;

    /** The service client containing this operation class. */
    private final MicrosoftScanningClientImpl client;

    /**
     * Initializes an instance of DataSourcesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    DataSourcesImpl(MicrosoftScanningClientImpl client) {
        this.service =
                RestProxy.create(DataSourcesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MicrosoftScanningClientDataSources to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{Endpoint}")
    @ServiceInterface(name = "MicrosoftScanningCli")
    private interface DataSourcesService {
        @Put("/datasources/{dataSourceName}")
        Mono<Response<BinaryData>> createOrUpdate(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/datasources/{dataSourceName}")
        Mono<Response<BinaryData>> get(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/datasources/{dataSourceName}")
        Mono<Response<BinaryData>> delete(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceName") String dataSourceName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/datasources")
        Mono<Response<BinaryData>> listAll(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("{nextLink}")
        Mono<Response<BinaryData>> listAllNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
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
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            String dataSourceName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createOrUpdate(
                                this.client.getEndpoint(),
                                dataSourceName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createOrUpdate(
                this.client.getEndpoint(),
                dataSourceName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
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
    public Mono<BinaryData> createOrUpdateAsync(String dataSourceName, RequestOptions requestOptions) {
        return createOrUpdateWithResponseAsync(dataSourceName, requestOptions)
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
    public Mono<BinaryData> createOrUpdateAsync(String dataSourceName, RequestOptions requestOptions, Context context) {
        return createOrUpdateWithResponseAsync(dataSourceName, requestOptions, context)
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
    public BinaryData createOrUpdate(String dataSourceName, RequestOptions requestOptions) {
        return createOrUpdateAsync(dataSourceName, requestOptions).block();
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
    public Response<BinaryData> createOrUpdateWithResponse(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        return createOrUpdateWithResponseAsync(dataSourceName, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getWithResponseAsync(String dataSourceName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.get(
                                this.client.getEndpoint(),
                                dataSourceName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> getWithResponseAsync(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.get(
                this.client.getEndpoint(),
                dataSourceName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
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
    public Mono<BinaryData> getAsync(String dataSourceName, RequestOptions requestOptions) {
        return getWithResponseAsync(dataSourceName, requestOptions)
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
    public Mono<BinaryData> getAsync(String dataSourceName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(dataSourceName, requestOptions, context)
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
    public BinaryData get(String dataSourceName, RequestOptions requestOptions) {
        return getAsync(dataSourceName, requestOptions).block();
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
    public Response<BinaryData> getWithResponse(String dataSourceName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(dataSourceName, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> deleteWithResponseAsync(String dataSourceName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.delete(
                                this.client.getEndpoint(),
                                dataSourceName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> deleteWithResponseAsync(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.delete(
                this.client.getEndpoint(),
                dataSourceName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
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
    public Mono<BinaryData> deleteAsync(String dataSourceName, RequestOptions requestOptions) {
        return deleteWithResponseAsync(dataSourceName, requestOptions)
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
    public Mono<BinaryData> deleteAsync(String dataSourceName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(dataSourceName, requestOptions, context)
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
    public BinaryData delete(String dataSourceName, RequestOptions requestOptions) {
        return deleteAsync(dataSourceName, requestOptions).block();
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
    public Response<BinaryData> deleteWithResponse(
            String dataSourceName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(dataSourceName, requestOptions, context).block();
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listAllSinglePageAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listAll(
                                        this.client.getEndpoint(),
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listAllSinglePageAsync(RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listAll(this.client.getEndpoint(), this.client.getApiVersion(), accept, requestOptions, context)
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
    public PagedFlux<BinaryData> listAllAsync(RequestOptions requestOptions) {
        return new PagedFlux<>(
                () -> listAllSinglePageAsync(requestOptions), nextLink -> listAllNextSinglePageAsync(nextLink, null));
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
    public PagedFlux<BinaryData> listAllAsync(RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
                () -> listAllSinglePageAsync(requestOptions, context),
                nextLink -> listAllNextSinglePageAsync(nextLink, null, context));
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
    public PagedIterable<BinaryData> listAll(RequestOptions requestOptions) {
        return new PagedIterable<>(listAllAsync(requestOptions));
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
    public PagedIterable<BinaryData> listAll(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(listAllAsync(requestOptions, context));
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
     *
     * @param nextLink The nextLink parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listAllNextSinglePageAsync(String nextLink, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listAllNext(
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
     *
     * @param nextLink The nextLink parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listAllNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listAllNext(nextLink, this.client.getEndpoint(), accept, requestOptions, context)
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
