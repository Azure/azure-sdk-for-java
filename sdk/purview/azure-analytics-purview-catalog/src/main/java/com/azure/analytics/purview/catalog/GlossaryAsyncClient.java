package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.GlossariesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class, isAsync = true)
public final class GlossaryAsyncClient {
    private final GlossariesImpl serviceClient;

    /**
     * Initializes an instance of Glossaries client.
     *
     * @param serviceClient the service client implementation.
     */
    GlossaryAsyncClient(GlossariesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossariesWithResponse(RequestOptions requestOptions) {
        return this.serviceClient.listGlossariesWithResponseAsync(requestOptions);
    }

    /**
     * Get all glossaries registered with Atlas.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         language: String
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         usage: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaries(RequestOptions requestOptions) {
        return this.serviceClient.listGlossariesAsync(requestOptions);
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryWithResponse(
            BinaryData atlasGlossary, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryWithResponseAsync(atlasGlossary, requestOptions);
    }

    /**
     * Create a glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossary(BinaryData atlasGlossary, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryAsync(atlasGlossary, requestOptions);
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoriesWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategoriesWithResponseAsync(glossaryCategory, requestOptions);
    }

    /**
     * Create glossary category in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategories(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategoriesAsync(glossaryCategory, requestOptions);
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryCategoryWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategoryWithResponseAsync(glossaryCategory, requestOptions);
    }

    /**
     * Create a glossary category.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryCategory(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategoryAsync(glossaryCategory, requestOptions);
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryCategoryWithResponse(
            String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions);
    }

    /**
     * Get specific glossary category by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryCategoryAsync(categoryGuid, requestOptions);
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
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
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryCategoryWithResponseAsync(
                categoryGuid, glossaryCategory, requestOptions);
    }

    /**
     * Update the given glossary category by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
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
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryCategory(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryCategoryAsync(categoryGuid, glossaryCategory, requestOptions);
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryCategoryWithResponse(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryCategoryWithResponseAsync(categoryGuid, requestOptions);
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryCategoryAsync(categoryGuid, requestOptions);
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryCategoryWithResponseAsync(
                categoryGuid, partialUpdates, requestOptions);
    }

    /**
     * Update the glossary category partially.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     childrenCategories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     parentCategory: (recursive schema, see parentCategory above)
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryCategory(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryCategoryAsync(categoryGuid, partialUpdates, requestOptions);
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedCategoriesWithResponse(
            String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedCategoriesWithResponseAsync(categoryGuid, requestOptions);
    }

    /**
     * Get all related categories (parent and children). Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedCategories(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedCategoriesAsync(categoryGuid, requestOptions);
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listCategoryTermsWithResponse(
            String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listCategoryTermsWithResponseAsync(categoryGuid, requestOptions);
    }

    /**
     * Get all terms associated with the specific category.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listCategoryTerms(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listCategoryTermsAsync(categoryGuid, requestOptions);
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTermWithResponseAsync(glossaryTerm, requestOptions);
    }

    /**
     * Create a glossary term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTerm(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTermAsync(glossaryTerm, requestOptions);
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryTermWithResponse(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryTermWithResponseAsync(termGuid, requestOptions);
    }

    /**
     * Get a specific glossary term by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryTermAsync(termGuid, requestOptions);
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
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
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryTermWithResponse(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryTermWithResponseAsync(termGuid, glossaryTerm, requestOptions);
    }

    /**
     * Update the given glossary term by its GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
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
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossaryTerm(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryTermAsync(termGuid, glossaryTerm, requestOptions);
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryTermWithResponse(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryTermWithResponseAsync(termGuid, requestOptions);
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryTermAsync(termGuid, requestOptions);
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryTermWithResponse(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryTermWithResponseAsync(termGuid, partialUpdates, requestOptions);
    }

    /**
     * Update the glossary term partially.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     abbreviation: String
     *     templateName: [
     *         Object
     *     ]
     *     anchor: {
     *         displayText: String
     *         glossaryGuid: String
     *         relationGuid: String
     *     }
     *     antonyms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     createTime: Float
     *     createdBy: String
     *     updateTime: Float
     *     updatedBy: String
     *     status: String(Draft/Approved/Alert/Expired)
     *     resources: [
     *         {
     *             displayName: String
     *             url: String
     *         }
     *     ]
     *     contacts: {
     *         String: [
     *             {
     *                 id: String
     *                 info: String
     *             }
     *         ]
     *     }
     *     attributes: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     assignedEntities: [
     *         {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *             displayText: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             relationshipType: String
     *             relationshipAttributes: {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *             }
     *             relationshipGuid: String
     *             relationshipStatus: String(ACTIVE/DELETED)
     *         }
     *     ]
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             relationGuid: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         }
     *     ]
     *     classifies: [
     *         (recursive schema, see above)
     *     ]
     *     examples: [
     *         String
     *     ]
     *     isA: [
     *         (recursive schema, see above)
     *     ]
     *     preferredTerms: [
     *         (recursive schema, see above)
     *     ]
     *     preferredToTerms: [
     *         (recursive schema, see above)
     *     ]
     *     replacedBy: [
     *         (recursive schema, see above)
     *     ]
     *     replacementTerms: [
     *         (recursive schema, see above)
     *     ]
     *     seeAlso: [
     *         (recursive schema, see above)
     *     ]
     *     synonyms: [
     *         (recursive schema, see above)
     *     ]
     *     translatedTerms: [
     *         (recursive schema, see above)
     *     ]
     *     translationTerms: [
     *         (recursive schema, see above)
     *     ]
     *     usage: String
     *     validValues: [
     *         (recursive schema, see above)
     *     ]
     *     validValuesFor: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossaryTerm(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryTermAsync(termGuid, partialUpdates, requestOptions);
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createGlossaryTermsWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTermsWithResponseAsync(glossaryTerm, requestOptions);
    }

    /**
     * Create glossary terms in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     (recursive schema, see above)
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> createGlossaryTerms(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTermsAsync(glossaryTerm, requestOptions);
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntitiesAssignedWithTermWithResponse(
            String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getEntitiesAssignedWithTermWithResponseAsync(termGuid, requestOptions);
    }

    /**
     * Get all related objects assigned with the specified term.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntitiesAssignedWithTerm(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getEntitiesAssignedWithTermAsync(termGuid, requestOptions);
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> assignTermToEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.assignTermToEntitiesWithResponseAsync(termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Assign the given term to the provided list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> assignTermToEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.assignTermToEntitiesAsync(termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.removeTermAssignmentFromEntitiesWithResponseAsync(
                termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.removeTermAssignmentFromEntitiesAsync(termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.deleteTermAssignmentFromEntitiesWithResponseAsync(
                termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Delete the term assignment for the given list of related objects.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *         displayText: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         relationshipType: String
     *         relationshipAttributes: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *         }
     *         relationshipGuid: String
     *         relationshipStatus: String(ACTIVE/DELETED)
     *     }
     * ]
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        return this.serviceClient.deleteTermAssignmentFromEntitiesAsync(termGuid, relatedObjectIds, requestOptions);
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listRelatedTermsWithResponse(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedTermsWithResponseAsync(termGuid, requestOptions);
    }

    /**
     * Get all related terms for a specific term by its GUID. Limit, offset, and sort parameters are currently not being
     * enabled and won't work even they are passed.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listRelatedTerms(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedTermsAsync(termGuid, requestOptions);
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getGlossaryWithResponse(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get a specific Glossary by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryAsync(glossaryGuid, requestOptions);
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> updateGlossaryWithResponse(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryWithResponseAsync(glossaryGuid, updatedGlossary, requestOptions);
    }

    /**
     * Update the given glossary.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> updateGlossary(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryAsync(glossaryGuid, updatedGlossary, requestOptions);
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGlossaryWithResponse(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.deleteGlossaryAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesWithResponse(
            String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategoriesWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get the categories belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         childrenCategories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 parentCategoryGuid: String
     *                 relationGuid: String
     *             }
     *         ]
     *         parentCategory: (recursive schema, see parentCategory above)
     *         terms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategories(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategoriesAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryCategoriesHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategoriesHeadersWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get the category headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         categoryGuid: String
     *         description: String
     *         displayText: String
     *         parentCategoryGuid: String
     *         relationGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryCategoriesHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategoriesHeadersAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getDetailedGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getDetailedGlossaryWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get a specific glossary with detailed information.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     *     categoryInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             anchor: {
     *                 displayText: String
     *                 glossaryGuid: String
     *                 relationGuid: String
     *             }
     *             childrenCategories: [
     *                 (recursive schema, see above)
     *             ]
     *             parentCategory: (recursive schema, see parentCategory above)
     *             terms: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     *     termInfo: {
     *         String: {
     *             guid: String
     *             classifications: [
     *                 (recursive schema, see above)
     *             ]
     *             longDescription: String
     *             name: String
     *             qualifiedName: String
     *             shortDescription: String
     *             lastModifiedTS: String
     *             abbreviation: String
     *             templateName: [
     *                 Object
     *             ]
     *             anchor: (recursive schema, see anchor above)
     *             antonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             updateTime: Float
     *             updatedBy: String
     *             status: String(Draft/Approved/Alert/Expired)
     *             resources: [
     *                 {
     *                     displayName: String
     *                     url: String
     *                 }
     *             ]
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *             attributes: {
     *                 String: {
     *                     String: Object
     *                 }
     *             }
     *             assignedEntities: [
     *                 {
     *                     guid: String
     *                     typeName: String
     *                     uniqueAttributes: {
     *                         String: Object
     *                     }
     *                     displayText: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     relationshipType: String
     *                     relationshipAttributes: {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                     }
     *                     relationshipGuid: String
     *                     relationshipStatus: String(ACTIVE/DELETED)
     *                 }
     *             ]
     *             categories: [
     *                 {
     *                     categoryGuid: String
     *                     description: String
     *                     displayText: String
     *                     relationGuid: String
     *                     status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 }
     *             ]
     *             classifies: [
     *                 (recursive schema, see above)
     *             ]
     *             examples: [
     *                 String
     *             ]
     *             isA: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             preferredToTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             replacedBy: [
     *                 (recursive schema, see above)
     *             ]
     *             replacementTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             seeAlso: [
     *                 (recursive schema, see above)
     *             ]
     *             synonyms: [
     *                 (recursive schema, see above)
     *             ]
     *             translatedTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             translationTerms: [
     *                 (recursive schema, see above)
     *             ]
     *             usage: String
     *             validValues: [
     *                 (recursive schema, see above)
     *             ]
     *             validValuesFor: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getDetailedGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getDetailedGlossaryAsync(glossaryGuid, requestOptions);
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> partialUpdateGlossaryWithResponse(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryWithResponseAsync(glossaryGuid, partialUpdates, requestOptions);
    }

    /**
     * Update the glossary partially. Some properties such as qualifiedName are not allowed to be updated.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     String: String
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guid: String
     *     classifications: [
     *         {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             entityGuid: String
     *             entityStatus: String(ACTIVE/DELETED)
     *             removePropagationsOnEntityDelete: Boolean
     *             validityPeriods: [
     *                 {
     *                     endTime: String
     *                     startTime: String
     *                     timeZone: String
     *                 }
     *             ]
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *         }
     *     ]
     *     longDescription: String
     *     name: String
     *     qualifiedName: String
     *     shortDescription: String
     *     lastModifiedTS: String
     *     categories: [
     *         {
     *             categoryGuid: String
     *             description: String
     *             displayText: String
     *             parentCategoryGuid: String
     *             relationGuid: String
     *         }
     *     ]
     *     language: String
     *     terms: [
     *         {
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     usage: String
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> partialUpdateGlossary(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryAsync(glossaryGuid, partialUpdates, requestOptions);
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermsWithResponse(
            String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTermsWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get terms belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTerms(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTermsAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listGlossaryTermHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTermHeadersWithResponseAsync(glossaryGuid, requestOptions);
    }

    /**
     * Get term headers belonging to a specific glossary.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>sort</td><td>String</td><td>No</td><td>The sort order, ASC (default) or DESC.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         description: String
     *         displayText: String
     *         expression: String
     *         relationGuid: String
     *         source: String
     *         status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *         steward: String
     *         termGuid: String
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listGlossaryTermHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTermHeadersAsync(glossaryGuid, requestOptions);
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvWithResponse(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsvWithResponseAsync(glossaryGuid, file, requestOptions);
    }

    /**
     * Import Glossary Terms from local csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsv(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsvAsync(glossaryGuid, file, requestOptions);
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> importGlossaryTermsViaCsvByGlossaryNameWithResponse(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsvByGlossaryNameWithResponseAsync(
                glossaryName, file, requestOptions);
    }

    /**
     * Import Glossary Terms from local csv file by glossaryName.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>contentLength</td><td>long</td><td>Yes</td><td>The contentLength parameter</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> importGlossaryTermsViaCsvByGlossaryName(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsvByGlossaryNameAsync(glossaryName, file, requestOptions);
    }

    /**
     * Get the status of import csv operation.
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
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getImportCsvOperationStatusWithResponse(
            String operationGuid, RequestOptions requestOptions) {
        return this.serviceClient.getImportCsvOperationStatusWithResponseAsync(operationGuid, requestOptions);
    }

    /**
     * Get the status of import csv operation.
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
     *     status: String(NotStarted/Succeeded/Failed/Running)
     *     createTime: String
     *     lastUpdateTime: String
     *     properties: {
     *         importedTerms: String
     *         totalTermsDetected: String
     *     }
     *     error: {
     *         errorCode: Integer
     *         errorMessage: String
     *     }
     * }
     * }</pre>
     *
     * @param operationGuid The globally unique identifier for async operation/job.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getImportCsvOperationStatus(String operationGuid, RequestOptions requestOptions) {
        return this.serviceClient.getImportCsvOperationStatusAsync(operationGuid, requestOptions);
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> exportGlossaryTermsAsCsvWithResponse(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        return this.serviceClient.exportGlossaryTermsAsCsvWithResponseAsync(glossaryGuid, termGuids, requestOptions);
    }

    /**
     * Export Glossary Terms as csv file.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * Flux<ByteBuffer>
     * }</pre>
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Flux<ByteBuffer>> exportGlossaryTermsAsCsv(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        return this.serviceClient.exportGlossaryTermsAsCsvAsync(glossaryGuid, termGuids, requestOptions);
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listTermsByGlossaryNameWithResponse(
            String glossaryName, RequestOptions requestOptions) {
        return this.serviceClient.listTermsByGlossaryNameWithResponseAsync(glossaryName, requestOptions);
    }

    /**
     * Get terms by glossary name.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>includeTermHierarchy</td><td>String</td><td>No</td><td>Whether include term hierarchy</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         guid: String
     *         classifications: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 entityGuid: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 removePropagationsOnEntityDelete: Boolean
     *                 validityPeriods: [
     *                     {
     *                         endTime: String
     *                         startTime: String
     *                         timeZone: String
     *                     }
     *                 ]
     *                 source: String
     *                 sourceDetails: {
     *                     String: Object
     *                 }
     *             }
     *         ]
     *         longDescription: String
     *         name: String
     *         qualifiedName: String
     *         shortDescription: String
     *         lastModifiedTS: String
     *         abbreviation: String
     *         templateName: [
     *             Object
     *         ]
     *         anchor: {
     *             displayText: String
     *             glossaryGuid: String
     *             relationGuid: String
     *         }
     *         antonyms: [
     *             {
     *                 description: String
     *                 displayText: String
     *                 expression: String
     *                 relationGuid: String
     *                 source: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *                 steward: String
     *                 termGuid: String
     *             }
     *         ]
     *         createTime: Float
     *         createdBy: String
     *         updateTime: Float
     *         updatedBy: String
     *         status: String(Draft/Approved/Alert/Expired)
     *         resources: [
     *             {
     *                 displayName: String
     *                 url: String
     *             }
     *         ]
     *         contacts: {
     *             String: [
     *                 {
     *                     id: String
     *                     info: String
     *                 }
     *             ]
     *         }
     *         attributes: {
     *             String: {
     *                 String: Object
     *             }
     *         }
     *         assignedEntities: [
     *             {
     *                 guid: String
     *                 typeName: String
     *                 uniqueAttributes: {
     *                     String: Object
     *                 }
     *                 displayText: String
     *                 entityStatus: String(ACTIVE/DELETED)
     *                 relationshipType: String
     *                 relationshipAttributes: {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                 }
     *                 relationshipGuid: String
     *                 relationshipStatus: String(ACTIVE/DELETED)
     *             }
     *         ]
     *         categories: [
     *             {
     *                 categoryGuid: String
     *                 description: String
     *                 displayText: String
     *                 relationGuid: String
     *                 status: String(DRAFT/ACTIVE/DEPRECATED/OBSOLETE/OTHER)
     *             }
     *         ]
     *         classifies: [
     *             (recursive schema, see above)
     *         ]
     *         examples: [
     *             String
     *         ]
     *         isA: [
     *             (recursive schema, see above)
     *         ]
     *         preferredTerms: [
     *             (recursive schema, see above)
     *         ]
     *         preferredToTerms: [
     *             (recursive schema, see above)
     *         ]
     *         replacedBy: [
     *             (recursive schema, see above)
     *         ]
     *         replacementTerms: [
     *             (recursive schema, see above)
     *         ]
     *         seeAlso: [
     *             (recursive schema, see above)
     *         ]
     *         synonyms: [
     *             (recursive schema, see above)
     *         ]
     *         translatedTerms: [
     *             (recursive schema, see above)
     *         ]
     *         translationTerms: [
     *             (recursive schema, see above)
     *         ]
     *         usage: String
     *         validValues: [
     *             (recursive schema, see above)
     *         ]
     *         validValuesFor: [
     *             (recursive schema, see above)
     *         ]
     *     }
     * ]
     * }</pre>
     *
     * @param glossaryName The name of the glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listTermsByGlossaryName(String glossaryName, RequestOptions requestOptions) {
        return this.serviceClient.listTermsByGlossaryNameAsync(glossaryName, requestOptions);
    }
}
