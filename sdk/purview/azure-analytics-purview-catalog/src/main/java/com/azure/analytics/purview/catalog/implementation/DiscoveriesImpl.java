package com.azure.analytics.purview.catalog.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Discoveries. */
public final class DiscoveriesImpl {
    /** The proxy service used to perform REST calls. */
    private final DiscoveriesService service;

    /** The service client containing this operation class. */
    private final PurviewCatalogServiceRestAPIDocumentImpl client;

    /**
     * Initializes an instance of DiscoveriesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    DiscoveriesImpl(PurviewCatalogServiceRestAPIDocumentImpl client) {
        this.service =
                RestProxy.create(DiscoveriesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for PurviewCatalogServiceRestAPIDocumentDiscoveries to be used by the
     * proxy service to perform REST calls.
     */
    @Host("{Endpoint}/api")
    @ServiceInterface(name = "PurviewCatalogServic")
    private interface DiscoveriesService {
        @Post("/search/query")
        Mono<Response<BinaryData>> query(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData searchRequest,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/search/suggest")
        Mono<Response<BinaryData>> suggest(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData suggestRequest,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/browse")
        Mono<Response<BinaryData>> browse(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData browseRequest,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/search/autocomplete")
        Mono<Response<BinaryData>> autoComplete(
                @HostParam("Endpoint") String endpoint,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") BinaryData autoCompleteRequest,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> queryWithResponseAsync(BinaryData searchRequest, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.query(
                                this.client.getEndpoint(),
                                this.client.getApiVersion(),
                                searchRequest,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> queryWithResponseAsync(
            BinaryData searchRequest, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.query(
                this.client.getEndpoint(), this.client.getApiVersion(), searchRequest, accept, requestOptions, context);
    }

    /**
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> queryAsync(BinaryData searchRequest, RequestOptions requestOptions) {
        return queryWithResponseAsync(searchRequest, requestOptions)
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
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> queryAsync(BinaryData searchRequest, RequestOptions requestOptions, Context context) {
        return queryWithResponseAsync(searchRequest, requestOptions, context)
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
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData query(BinaryData searchRequest, RequestOptions requestOptions) {
        return queryAsync(searchRequest, requestOptions).block();
    }

    /**
     * Gets data using search.
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
     *     keywords: String
     *     offset: Integer
     *     limit: Integer
     *     filter: Object
     *     facets: [
     *         {
     *             count: Integer
     *             facet: String
     *             sort: Object
     *         }
     *     ]
     *     taxonomySetting: {
     *         assetTypes: [
     *             String
     *         ]
     *         facet: (recursive schema, see facet above)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     searchFacets: {
     *         assetType: [
     *             {
     *                 count: Integer
     *                 value: String
     *             }
     *         ]
     *         classification: [
     *             (recursive schema, see above)
     *         ]
     *         classificationCategory: [
     *             (recursive schema, see above)
     *         ]
     *         contactId: [
     *             (recursive schema, see above)
     *         ]
     *         fileExtension: [
     *             (recursive schema, see above)
     *         ]
     *         label: [
     *             (recursive schema, see above)
     *         ]
     *         term: [
     *             (recursive schema, see above)
     *         ]
     *     }
     *     value: [
     *         {
     *             searchScore: Float
     *             searchHighlights: {
     *                 id: [
     *                     String
     *                 ]
     *                 qualifiedName: [
     *                     String
     *                 ]
     *                 name: [
     *                     String
     *                 ]
     *                 description: [
     *                     String
     *                 ]
     *                 entityType: [
     *                     String
     *                 ]
     *             }
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> queryWithResponse(
            BinaryData searchRequest, RequestOptions requestOptions, Context context) {
        return queryWithResponseAsync(searchRequest, requestOptions, context).block();
    }

    /**
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> suggestWithResponseAsync(
            BinaryData suggestRequest, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.suggest(
                                this.client.getEndpoint(),
                                this.client.getApiVersion(),
                                suggestRequest,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> suggestWithResponseAsync(
            BinaryData suggestRequest, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.suggest(
                this.client.getEndpoint(),
                this.client.getApiVersion(),
                suggestRequest,
                accept,
                requestOptions,
                context);
    }

    /**
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> suggestAsync(BinaryData suggestRequest, RequestOptions requestOptions) {
        return suggestWithResponseAsync(suggestRequest, requestOptions)
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
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> suggestAsync(BinaryData suggestRequest, RequestOptions requestOptions, Context context) {
        return suggestWithResponseAsync(suggestRequest, requestOptions, context)
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
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData suggest(BinaryData suggestRequest, RequestOptions requestOptions) {
        return suggestAsync(suggestRequest, requestOptions).block();
    }

    /**
     * Get search suggestions by query criteria.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             searchScore: Float
     *             searchText: String
     *             description: String
     *             id: String
     *             name: String
     *             owner: String
     *             qualifiedName: String
     *             entityType: String
     *             classification: [
     *                 String
     *             ]
     *             label: [
     *                 String
     *             ]
     *             term: [
     *                 {
     *                     name: String
     *                     glossaryName: String
     *                     guid: String
     *                 }
     *             ]
     *             contact: [
     *                 {
     *                     id: String
     *                     info: String
     *                     contactType: String
     *                 }
     *             ]
     *             assetType: [
     *                 String
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> suggestWithResponse(
            BinaryData suggestRequest, RequestOptions requestOptions, Context context) {
        return suggestWithResponseAsync(suggestRequest, requestOptions, context).block();
    }

    /**
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> browseWithResponseAsync(BinaryData browseRequest, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.browse(
                                this.client.getEndpoint(),
                                this.client.getApiVersion(),
                                browseRequest,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> browseWithResponseAsync(
            BinaryData browseRequest, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.browse(
                this.client.getEndpoint(), this.client.getApiVersion(), browseRequest, accept, requestOptions, context);
    }

    /**
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> browseAsync(BinaryData browseRequest, RequestOptions requestOptions) {
        return browseWithResponseAsync(browseRequest, requestOptions)
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
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> browseAsync(BinaryData browseRequest, RequestOptions requestOptions, Context context) {
        return browseWithResponseAsync(browseRequest, requestOptions, context)
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
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData browse(BinaryData browseRequest, RequestOptions requestOptions) {
        return browseAsync(browseRequest, requestOptions).block();
    }

    /**
     * Browse entities by path or entity type.
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
     *     entityType: String
     *     path: String
     *     limit: Integer
     *     offset: Integer
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     searchCount: Integer
     *     value: [
     *         {
     *             entityType: String
     *             id: String
     *             isLeaf: Boolean
     *             name: String
     *             owner: [
     *                 {
     *                     id: String
     *                     displayName: String
     *                     mail: String
     *                     contactType: String
     *                 }
     *             ]
     *             path: String
     *             qualifiedName: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> browseWithResponse(
            BinaryData browseRequest, RequestOptions requestOptions, Context context) {
        return browseWithResponseAsync(browseRequest, requestOptions, context).block();
    }

    /**
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> autoCompleteWithResponseAsync(
            BinaryData autoCompleteRequest, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.autoComplete(
                                this.client.getEndpoint(),
                                this.client.getApiVersion(),
                                autoCompleteRequest,
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> autoCompleteWithResponseAsync(
            BinaryData autoCompleteRequest, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.autoComplete(
                this.client.getEndpoint(),
                this.client.getApiVersion(),
                autoCompleteRequest,
                accept,
                requestOptions,
                context);
    }

    /**
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> autoCompleteAsync(BinaryData autoCompleteRequest, RequestOptions requestOptions) {
        return autoCompleteWithResponseAsync(autoCompleteRequest, requestOptions)
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
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> autoCompleteAsync(
            BinaryData autoCompleteRequest, RequestOptions requestOptions, Context context) {
        return autoCompleteWithResponseAsync(autoCompleteRequest, requestOptions, context)
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
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData autoComplete(BinaryData autoCompleteRequest, RequestOptions requestOptions) {
        return autoCompleteAsync(autoCompleteRequest, requestOptions).block();
    }

    /**
     * Get auto complete options.
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
     *     keywords: String
     *     limit: Integer
     *     filter: Object
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     value: [
     *         {
     *             text: String
     *             queryPlusText: String
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> autoCompleteWithResponse(
            BinaryData autoCompleteRequest, RequestOptions requestOptions, Context context) {
        return autoCompleteWithResponseAsync(autoCompleteRequest, requestOptions, context).block();
    }
}
