package com.azure.analytics.purview.scanning.implementation;

import com.azure.core.annotation.BodyParam;
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

/** An instance of this class provides access to all the operations defined in KeyVaultConnections. */
public final class KeyVaultConnectionsImpl {
    /** The proxy service used to perform REST calls. */
    private final KeyVaultConnectionsService service;

    /** The service client containing this operation class. */
    private final MicrosoftScanningClientImpl client;

    /**
     * Initializes an instance of KeyVaultConnectionsImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    KeyVaultConnectionsImpl(MicrosoftScanningClientImpl client) {
        this.service =
                RestProxy.create(
                        KeyVaultConnectionsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MicrosoftScanningClientKeyVaultConnections to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}")
    @ServiceInterface(name = "MicrosoftScanningCli")
    private interface KeyVaultConnectionsService {
        @Get("/azureKeyVaults/{keyVaultName}")
        Mono<Response<BinaryData>> get(
                @HostParam("Endpoint") String endpoint,
                @PathParam("keyVaultName") String keyVaultName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/azureKeyVaults/{keyVaultName}")
        Mono<Response<BinaryData>> create(
                @HostParam("Endpoint") String endpoint,
                @PathParam("keyVaultName") String keyVaultName,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData body,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/azureKeyVaults/{keyVaultName}")
        Mono<Response<BinaryData>> delete(
                @HostParam("Endpoint") String endpoint,
                @PathParam("keyVaultName") String keyVaultName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/azureKeyVaults")
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
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(String keyVaultName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.get(
                                this.client.getEndpoint(),
                                keyVaultName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(
            String keyVaultName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.get(
                this.client.getEndpoint(), keyVaultName, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String keyVaultName, RequestOptions requestOptions) {
        return getWithResponseAsync(keyVaultName, requestOptions)
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
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String keyVaultName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(keyVaultName, requestOptions, context)
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
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String keyVaultName, RequestOptions requestOptions) {
        return getAsync(keyVaultName, requestOptions).block();
    }

    /**
     * Gets key vault information.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(String keyVaultName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(keyVaultName, requestOptions, context).block();
    }

    /**
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createWithResponseAsync(
            String keyVaultName, BinaryData body, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.create(
                                this.client.getEndpoint(),
                                keyVaultName,
                                this.client.getApiVersion(),
                                body,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createWithResponseAsync(
            String keyVaultName, BinaryData body, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.create(
                this.client.getEndpoint(),
                keyVaultName,
                this.client.getApiVersion(),
                body,
                accept,
                requestOptions,
                context);
    }

    /**
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createAsync(String keyVaultName, BinaryData body, RequestOptions requestOptions) {
        return createWithResponseAsync(keyVaultName, body, requestOptions)
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
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createAsync(
            String keyVaultName, BinaryData body, RequestOptions requestOptions, Context context) {
        return createWithResponseAsync(keyVaultName, body, requestOptions, context)
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
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData create(String keyVaultName, BinaryData body, RequestOptions requestOptions) {
        return createAsync(keyVaultName, body, requestOptions).block();
    }

    /**
     * Creates an instance of a key vault connection.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createWithResponse(
            String keyVaultName, BinaryData body, RequestOptions requestOptions, Context context) {
        return createWithResponseAsync(keyVaultName, body, requestOptions, context).block();
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponseAsync(String keyVaultName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.delete(
                                this.client.getEndpoint(),
                                keyVaultName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponseAsync(
            String keyVaultName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.delete(
                this.client.getEndpoint(), keyVaultName, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> deleteAsync(String keyVaultName, RequestOptions requestOptions) {
        return deleteWithResponseAsync(keyVaultName, requestOptions)
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
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> deleteAsync(String keyVaultName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(keyVaultName, requestOptions, context)
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
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData delete(String keyVaultName, RequestOptions requestOptions) {
        return deleteAsync(keyVaultName, requestOptions).block();
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteWithResponse(
            String keyVaultName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(keyVaultName, requestOptions, context).block();
    }

    /**
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     * List key vault connections in account.
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
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
