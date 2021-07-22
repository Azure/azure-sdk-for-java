package com.azure.analytics.purview.scanning.implementation;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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

/** An instance of this class provides access to all the operations defined in SystemScanRulesets. */
public final class SystemScanRulesetsImpl {
    /** The proxy service used to perform REST calls. */
    private final SystemScanRulesetsService service;

    /** The service client containing this operation class. */
    private final MicrosoftScanningClientImpl client;

    /**
     * Initializes an instance of SystemScanRulesetsImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    SystemScanRulesetsImpl(MicrosoftScanningClientImpl client) {
        this.service =
                RestProxy.create(
                        SystemScanRulesetsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MicrosoftScanningClientSystemScanRulesets to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}")
    @ServiceInterface(name = "MicrosoftScanningCli")
    private interface SystemScanRulesetsService {
        @Get("/systemScanRulesets")
        Mono<Response<BinaryData>> listAll(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/systemScanRulesets/datasources/{dataSourceType}")
        Mono<Response<BinaryData>> get(
                @HostParam("Endpoint") String endpoint,
                @PathParam("dataSourceType") String dataSourceType,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/systemScanRulesets/versions/{version}")
        Mono<Response<BinaryData>> getByVersion(
                @HostParam("Endpoint") String endpoint,
                @PathParam("version") int version,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/systemScanRulesets/versions/latest")
        Mono<Response<BinaryData>> getLatest(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/systemScanRulesets/versions")
        Mono<Response<BinaryData>> listVersionsByDataSource(
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

        @Get("{nextLink}")
        Mono<Response<BinaryData>> listVersionsByDataSourceNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(String dataSourceType, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.get(
                                this.client.getEndpoint(),
                                dataSourceType,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(
            String dataSourceType, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.get(
                this.client.getEndpoint(),
                dataSourceType,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
    }

    /**
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String dataSourceType, RequestOptions requestOptions) {
        return getWithResponseAsync(dataSourceType, requestOptions)
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
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String dataSourceType, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(dataSourceType, requestOptions, context)
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
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String dataSourceType, RequestOptions requestOptions) {
        return getAsync(dataSourceType, requestOptions).block();
    }

    /**
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(String dataSourceType, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(dataSourceType, requestOptions, context).block();
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getByVersionWithResponseAsync(int version, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getByVersion(
                                this.client.getEndpoint(),
                                version,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getByVersionWithResponseAsync(
            int version, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getByVersion(
                this.client.getEndpoint(), version, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getByVersionAsync(int version, RequestOptions requestOptions) {
        return getByVersionWithResponseAsync(version, requestOptions)
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
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getByVersionAsync(int version, RequestOptions requestOptions, Context context) {
        return getByVersionWithResponseAsync(version, requestOptions, context)
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
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getByVersion(int version, RequestOptions requestOptions) {
        return getByVersionAsync(version, requestOptions).block();
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getByVersionWithResponse(int version, RequestOptions requestOptions, Context context) {
        return getByVersionWithResponseAsync(version, requestOptions, context).block();
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getLatestWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getLatest(
                                this.client.getEndpoint(),
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getLatestWithResponseAsync(RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getLatest(
                this.client.getEndpoint(), this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getLatestAsync(RequestOptions requestOptions) {
        return getLatestWithResponseAsync(requestOptions)
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
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getLatestAsync(RequestOptions requestOptions, Context context) {
        return getLatestWithResponseAsync(requestOptions, context)
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
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getLatest(RequestOptions requestOptions) {
        return getLatestAsync(requestOptions).block();
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getLatestWithResponse(RequestOptions requestOptions, Context context) {
        return getLatestWithResponseAsync(requestOptions, context).block();
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listVersionsByDataSourceSinglePageAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listVersionsByDataSource(
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
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listVersionsByDataSourceSinglePageAsync(
            RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listVersionsByDataSource(
                        this.client.getEndpoint(), this.client.getApiVersion(), accept, requestOptions, context)
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
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listVersionsByDataSourceAsync(RequestOptions requestOptions) {
        return new PagedFlux<>(
                () -> listVersionsByDataSourceSinglePageAsync(requestOptions),
                nextLink -> listVersionsByDataSourceNextSinglePageAsync(nextLink, null));
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listVersionsByDataSourceAsync(RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
                () -> listVersionsByDataSourceSinglePageAsync(requestOptions, context),
                nextLink -> listVersionsByDataSourceNextSinglePageAsync(nextLink, null, context));
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByDataSource(RequestOptions requestOptions) {
        return new PagedIterable<>(listVersionsByDataSourceAsync(requestOptions));
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByDataSource(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(listVersionsByDataSourceAsync(requestOptions, context));
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
    public Mono<PagedResponse<BinaryData>> listVersionsByDataSourceNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listVersionsByDataSourceNext(
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
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
    public Mono<PagedResponse<BinaryData>> listVersionsByDataSourceNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listVersionsByDataSourceNext(
                        nextLink, this.client.getEndpoint(), accept, requestOptions, context)
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
