package com.azure.analytics.purview.scanning.implementation;

import com.azure.core.annotation.Delete;
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

/** An instance of this class provides access to all the operations defined in ClassificationRules. */
public final class ClassificationRulesImpl {
    /** The proxy service used to perform REST calls. */
    private final ClassificationRulesService service;

    /** The service client containing this operation class. */
    private final MicrosoftScanningClientImpl client;

    /**
     * Initializes an instance of ClassificationRulesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ClassificationRulesImpl(MicrosoftScanningClientImpl client) {
        this.service =
                RestProxy.create(
                        ClassificationRulesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for MicrosoftScanningClientClassificationRules to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}")
    @ServiceInterface(name = "MicrosoftScanningCli")
    private interface ClassificationRulesService {
        @Get("/classificationrules/{classificationRuleName}")
        Mono<Response<BinaryData>> get(
                @HostParam("Endpoint") String endpoint,
                @PathParam("classificationRuleName") String classificationRuleName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/classificationrules/{classificationRuleName}")
        Mono<Response<BinaryData>> createOrUpdate(
                @HostParam("Endpoint") String endpoint,
                @PathParam("classificationRuleName") String classificationRuleName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/classificationrules/{classificationRuleName}")
        Mono<Response<BinaryData>> delete(
                @HostParam("Endpoint") String endpoint,
                @PathParam("classificationRuleName") String classificationRuleName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/classificationrules")
        Mono<Response<BinaryData>> listAll(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/classificationrules/{classificationRuleName}/versions")
        Mono<Response<BinaryData>> listVersionsByClassificationRuleName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("classificationRuleName") String classificationRuleName,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/classificationrules/{classificationRuleName}/versions/{classificationRuleVersion}/:tag")
        Mono<Response<BinaryData>> tagClassificationVersion(
                @HostParam("Endpoint") String endpoint,
                @PathParam("classificationRuleName") String classificationRuleName,
                @PathParam("classificationRuleVersion") int classificationRuleVersion,
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
        Mono<Response<BinaryData>> listVersionsByClassificationRuleNameNext(
                @PathParam(value = "nextLink", encoded = true) String nextLink,
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.get(
                                this.client.getEndpoint(),
                                classificationRuleName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.get(
                this.client.getEndpoint(),
                classificationRuleName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String classificationRuleName, RequestOptions requestOptions) {
        return getWithResponseAsync(classificationRuleName, requestOptions)
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
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAsync(String classificationRuleName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(classificationRuleName, requestOptions, context)
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
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String classificationRuleName, RequestOptions requestOptions) {
        return getAsync(classificationRuleName, requestOptions).block();
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return getWithResponseAsync(classificationRuleName, requestOptions, context).block();
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createOrUpdate(
                                this.client.getEndpoint(),
                                classificationRuleName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createOrUpdate(
                this.client.getEndpoint(),
                classificationRuleName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createOrUpdateAsync(String classificationRuleName, RequestOptions requestOptions) {
        return createOrUpdateWithResponseAsync(classificationRuleName, requestOptions)
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
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createOrUpdateAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return createOrUpdateWithResponseAsync(classificationRuleName, requestOptions, context)
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
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdate(String classificationRuleName, RequestOptions requestOptions) {
        return createOrUpdateAsync(classificationRuleName, requestOptions).block();
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return createOrUpdateWithResponseAsync(classificationRuleName, requestOptions, context).block();
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.delete(
                                this.client.getEndpoint(),
                                classificationRuleName,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponseAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.delete(
                this.client.getEndpoint(),
                classificationRuleName,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> deleteAsync(String classificationRuleName, RequestOptions requestOptions) {
        return deleteWithResponseAsync(classificationRuleName, requestOptions)
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
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> deleteAsync(String classificationRuleName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(classificationRuleName, requestOptions, context)
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
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData delete(String classificationRuleName, RequestOptions requestOptions) {
        return deleteAsync(classificationRuleName, requestOptions).block();
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return deleteWithResponseAsync(classificationRuleName, requestOptions, context).block();
    }

    /**
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listVersionsByClassificationRuleNameSinglePageAsync(
            String classificationRuleName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listVersionsByClassificationRuleName(
                                        this.client.getEndpoint(),
                                        classificationRuleName,
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
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<BinaryData>> listVersionsByClassificationRuleNameSinglePageAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listVersionsByClassificationRuleName(
                        this.client.getEndpoint(),
                        classificationRuleName,
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
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listVersionsByClassificationRuleNameAsync(
            String classificationRuleName, RequestOptions requestOptions) {
        return new PagedFlux<>(
                () -> listVersionsByClassificationRuleNameSinglePageAsync(classificationRuleName, requestOptions),
                nextLink -> listVersionsByClassificationRuleNameNextSinglePageAsync(nextLink, null));
    }

    /**
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listVersionsByClassificationRuleNameAsync(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return new PagedFlux<>(
                () ->
                        listVersionsByClassificationRuleNameSinglePageAsync(
                                classificationRuleName, requestOptions, context),
                nextLink -> listVersionsByClassificationRuleNameNextSinglePageAsync(nextLink, null, context));
    }

    /**
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByClassificationRuleName(
            String classificationRuleName, RequestOptions requestOptions) {
        return new PagedIterable<>(listVersionsByClassificationRuleNameAsync(classificationRuleName, requestOptions));
    }

    /**
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByClassificationRuleName(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(
                listVersionsByClassificationRuleNameAsync(classificationRuleName, requestOptions, context));
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> tagClassificationVersionWithResponseAsync(
            String classificationRuleName, int classificationRuleVersion, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.tagClassificationVersion(
                                this.client.getEndpoint(),
                                classificationRuleName,
                                classificationRuleVersion,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> tagClassificationVersionWithResponseAsync(
            String classificationRuleName,
            int classificationRuleVersion,
            RequestOptions requestOptions,
            Context context) {
        final String accept = "application/json";
        return service.tagClassificationVersion(
                this.client.getEndpoint(),
                classificationRuleName,
                classificationRuleVersion,
                this.client.getApiVersion(),
                accept,
                requestOptions,
                context);
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> tagClassificationVersionAsync(
            String classificationRuleName, int classificationRuleVersion, RequestOptions requestOptions) {
        return tagClassificationVersionWithResponseAsync(
                        classificationRuleName, classificationRuleVersion, requestOptions)
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
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> tagClassificationVersionAsync(
            String classificationRuleName,
            int classificationRuleVersion,
            RequestOptions requestOptions,
            Context context) {
        return tagClassificationVersionWithResponseAsync(
                        classificationRuleName, classificationRuleVersion, requestOptions, context)
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
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData tagClassificationVersion(
            String classificationRuleName, int classificationRuleVersion, RequestOptions requestOptions) {
        return tagClassificationVersionAsync(classificationRuleName, classificationRuleVersion, requestOptions).block();
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
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
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> tagClassificationVersionWithResponse(
            String classificationRuleName,
            int classificationRuleVersion,
            RequestOptions requestOptions,
            Context context) {
        return tagClassificationVersionWithResponseAsync(
                        classificationRuleName, classificationRuleVersion, requestOptions, context)
                .block();
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
    public Mono<PagedResponse<BinaryData>> listVersionsByClassificationRuleNameNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                        context ->
                                service.listVersionsByClassificationRuleNameNext(
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
    public Mono<PagedResponse<BinaryData>> listVersionsByClassificationRuleNameNextSinglePageAsync(
            String nextLink, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listVersionsByClassificationRuleNameNext(
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
