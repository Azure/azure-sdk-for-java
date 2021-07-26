package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.DiscoveriesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class, isAsync = true)
public final class DiscoveryAsyncClient {
    private final DiscoveriesImpl serviceClient;

    /**
     * Initializes an instance of Discoveries client.
     *
     * @param serviceClient the service client implementation.
     */
    DiscoveryAsyncClient(DiscoveriesImpl serviceClient) {
        this.serviceClient = serviceClient;
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
    public Mono<Response<BinaryData>> queryWithResponse(BinaryData searchRequest, RequestOptions requestOptions) {
        return this.serviceClient.queryWithResponseAsync(searchRequest, requestOptions);
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
    public Mono<BinaryData> query(BinaryData searchRequest, RequestOptions requestOptions) {
        return this.serviceClient.queryAsync(searchRequest, requestOptions);
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
    public Mono<Response<BinaryData>> suggestWithResponse(BinaryData suggestRequest, RequestOptions requestOptions) {
        return this.serviceClient.suggestWithResponseAsync(suggestRequest, requestOptions);
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
    public Mono<BinaryData> suggest(BinaryData suggestRequest, RequestOptions requestOptions) {
        return this.serviceClient.suggestAsync(suggestRequest, requestOptions);
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
    public Mono<Response<BinaryData>> browseWithResponse(BinaryData browseRequest, RequestOptions requestOptions) {
        return this.serviceClient.browseWithResponseAsync(browseRequest, requestOptions);
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
    public Mono<BinaryData> browse(BinaryData browseRequest, RequestOptions requestOptions) {
        return this.serviceClient.browseAsync(browseRequest, requestOptions);
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
    public Mono<Response<BinaryData>> autoCompleteWithResponse(
            BinaryData autoCompleteRequest, RequestOptions requestOptions) {
        return this.serviceClient.autoCompleteWithResponseAsync(autoCompleteRequest, requestOptions);
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
    public Mono<BinaryData> autoComplete(BinaryData autoCompleteRequest, RequestOptions requestOptions) {
        return this.serviceClient.autoCompleteAsync(autoCompleteRequest, requestOptions);
    }
}
