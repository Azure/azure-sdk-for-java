package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.GlossariesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;

/** Initializes a new instance of the synchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class)
public final class GlossaryClient {
    private final GlossariesImpl serviceClient;

    /**
     * Initializes an instance of Glossaries client.
     *
     * @param serviceClient the service client implementation.
     */
    GlossaryClient(GlossariesImpl serviceClient) {
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
    public BinaryData listGlossaries(RequestOptions requestOptions) {
        return this.serviceClient.listGlossaries(requestOptions);
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
    public Response<BinaryData> listGlossariesWithResponse(RequestOptions requestOptions, Context context) {
        return this.serviceClient.listGlossariesWithResponse(requestOptions, context);
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
    public BinaryData createGlossary(BinaryData atlasGlossary, RequestOptions requestOptions) {
        return this.serviceClient.createGlossary(atlasGlossary, requestOptions);
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
    public Response<BinaryData> createGlossaryWithResponse(
            BinaryData atlasGlossary, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createGlossaryWithResponse(atlasGlossary, requestOptions, context);
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
    public BinaryData createGlossaryCategories(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategories(glossaryCategory, requestOptions);
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
    public Response<BinaryData> createGlossaryCategoriesWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createGlossaryCategoriesWithResponse(glossaryCategory, requestOptions, context);
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
    public BinaryData createGlossaryCategory(BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryCategory(glossaryCategory, requestOptions);
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
    public Response<BinaryData> createGlossaryCategoryWithResponse(
            BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createGlossaryCategoryWithResponse(glossaryCategory, requestOptions, context);
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
    public BinaryData getGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryCategory(categoryGuid, requestOptions);
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
    public Response<BinaryData> getGlossaryCategoryWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getGlossaryCategoryWithResponse(categoryGuid, requestOptions, context);
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
    public BinaryData updateGlossaryCategory(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryCategory(categoryGuid, glossaryCategory, requestOptions);
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
    public Response<BinaryData> updateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData glossaryCategory, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateGlossaryCategoryWithResponse(
                categoryGuid, glossaryCategory, requestOptions, context);
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossaryCategory(String categoryGuid, RequestOptions requestOptions) {
        this.serviceClient.deleteGlossaryCategory(categoryGuid, requestOptions);
    }

    /**
     * Delete a glossary category.
     *
     * @param categoryGuid The globally unique identifier of the category.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryCategoryWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteGlossaryCategoryWithResponse(categoryGuid, requestOptions, context);
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
    public BinaryData partialUpdateGlossaryCategory(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryCategory(categoryGuid, partialUpdates, requestOptions);
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
    public Response<BinaryData> partialUpdateGlossaryCategoryWithResponse(
            String categoryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return this.serviceClient.partialUpdateGlossaryCategoryWithResponse(
                categoryGuid, partialUpdates, requestOptions, context);
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
    public BinaryData listRelatedCategories(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedCategories(categoryGuid, requestOptions);
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
    public Response<BinaryData> listRelatedCategoriesWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listRelatedCategoriesWithResponse(categoryGuid, requestOptions, context);
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
    public BinaryData listCategoryTerms(String categoryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listCategoryTerms(categoryGuid, requestOptions);
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
    public Response<BinaryData> listCategoryTermsWithResponse(
            String categoryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listCategoryTermsWithResponse(categoryGuid, requestOptions, context);
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
    public BinaryData createGlossaryTerm(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTerm(glossaryTerm, requestOptions);
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
    public Response<BinaryData> createGlossaryTermWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createGlossaryTermWithResponse(glossaryTerm, requestOptions, context);
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
    public BinaryData getGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossaryTerm(termGuid, requestOptions);
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
    public Response<BinaryData> getGlossaryTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getGlossaryTermWithResponse(termGuid, requestOptions, context);
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
    public BinaryData updateGlossaryTerm(String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossaryTerm(termGuid, glossaryTerm, requestOptions);
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
    public Response<BinaryData> updateGlossaryTermWithResponse(
            String termGuid, BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateGlossaryTermWithResponse(termGuid, glossaryTerm, requestOptions, context);
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossaryTerm(String termGuid, RequestOptions requestOptions) {
        this.serviceClient.deleteGlossaryTerm(termGuid, requestOptions);
    }

    /**
     * Delete a glossary term.
     *
     * @param termGuid The globally unique identifier for glossary term.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteGlossaryTermWithResponse(termGuid, requestOptions, context);
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
    public BinaryData partialUpdateGlossaryTerm(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossaryTerm(termGuid, partialUpdates, requestOptions);
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
    public Response<BinaryData> partialUpdateGlossaryTermWithResponse(
            String termGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return this.serviceClient.partialUpdateGlossaryTermWithResponse(
                termGuid, partialUpdates, requestOptions, context);
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
    public BinaryData createGlossaryTerms(BinaryData glossaryTerm, RequestOptions requestOptions) {
        return this.serviceClient.createGlossaryTerms(glossaryTerm, requestOptions);
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
    public Response<BinaryData> createGlossaryTermsWithResponse(
            BinaryData glossaryTerm, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createGlossaryTermsWithResponse(glossaryTerm, requestOptions, context);
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
    public BinaryData getEntitiesAssignedWithTerm(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.getEntitiesAssignedWithTerm(termGuid, requestOptions);
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
    public Response<BinaryData> getEntitiesAssignedWithTermWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getEntitiesAssignedWithTermWithResponse(termGuid, requestOptions, context);
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
    public void assignTermToEntities(String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        this.serviceClient.assignTermToEntities(termGuid, relatedObjectIds, requestOptions);
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
    public Response<Void> assignTermToEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return this.serviceClient.assignTermToEntitiesWithResponse(termGuid, relatedObjectIds, requestOptions, context);
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
    public void removeTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        this.serviceClient.removeTermAssignmentFromEntities(termGuid, relatedObjectIds, requestOptions);
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
    public Response<Void> removeTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return this.serviceClient.removeTermAssignmentFromEntitiesWithResponse(
                termGuid, relatedObjectIds, requestOptions, context);
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
    public void deleteTermAssignmentFromEntities(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions) {
        this.serviceClient.deleteTermAssignmentFromEntities(termGuid, relatedObjectIds, requestOptions);
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
    public Response<Void> deleteTermAssignmentFromEntitiesWithResponse(
            String termGuid, BinaryData relatedObjectIds, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteTermAssignmentFromEntitiesWithResponse(
                termGuid, relatedObjectIds, requestOptions, context);
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
    public BinaryData listRelatedTerms(String termGuid, RequestOptions requestOptions) {
        return this.serviceClient.listRelatedTerms(termGuid, requestOptions);
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
    public Response<BinaryData> listRelatedTermsWithResponse(
            String termGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listRelatedTermsWithResponse(termGuid, requestOptions, context);
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
    public BinaryData getGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getGlossary(glossaryGuid, requestOptions);
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
    public Response<BinaryData> getGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getGlossaryWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData updateGlossary(String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions) {
        return this.serviceClient.updateGlossary(glossaryGuid, updatedGlossary, requestOptions);
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
    public Response<BinaryData> updateGlossaryWithResponse(
            String glossaryGuid, BinaryData updatedGlossary, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateGlossaryWithResponse(glossaryGuid, updatedGlossary, requestOptions, context);
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGlossary(String glossaryGuid, RequestOptions requestOptions) {
        this.serviceClient.deleteGlossary(glossaryGuid, requestOptions);
    }

    /**
     * Delete a glossary.
     *
     * @param glossaryGuid The globally unique identifier for glossary.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteGlossaryWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData listGlossaryCategories(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategories(glossaryGuid, requestOptions);
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
    public Response<BinaryData> listGlossaryCategoriesWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listGlossaryCategoriesWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData listGlossaryCategoriesHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryCategoriesHeaders(glossaryGuid, requestOptions);
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
    public Response<BinaryData> listGlossaryCategoriesHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listGlossaryCategoriesHeadersWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData getDetailedGlossary(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.getDetailedGlossary(glossaryGuid, requestOptions);
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
    public Response<BinaryData> getDetailedGlossaryWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getDetailedGlossaryWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData partialUpdateGlossary(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateGlossary(glossaryGuid, partialUpdates, requestOptions);
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
    public Response<BinaryData> partialUpdateGlossaryWithResponse(
            String glossaryGuid, BinaryData partialUpdates, RequestOptions requestOptions, Context context) {
        return this.serviceClient.partialUpdateGlossaryWithResponse(
                glossaryGuid, partialUpdates, requestOptions, context);
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
    public BinaryData listGlossaryTerms(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTerms(glossaryGuid, requestOptions);
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
    public Response<BinaryData> listGlossaryTermsWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listGlossaryTermsWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData listGlossaryTermHeaders(String glossaryGuid, RequestOptions requestOptions) {
        return this.serviceClient.listGlossaryTermHeaders(glossaryGuid, requestOptions);
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
    public Response<BinaryData> listGlossaryTermHeadersWithResponse(
            String glossaryGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listGlossaryTermHeadersWithResponse(glossaryGuid, requestOptions, context);
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
    public BinaryData importGlossaryTermsViaCsv(String glossaryGuid, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsv(glossaryGuid, file, requestOptions);
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
    public Response<BinaryData> importGlossaryTermsViaCsvWithResponse(
            String glossaryGuid, BinaryData file, RequestOptions requestOptions, Context context) {
        return this.serviceClient.importGlossaryTermsViaCsvWithResponse(glossaryGuid, file, requestOptions, context);
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
    public BinaryData importGlossaryTermsViaCsvByGlossaryName(
            String glossaryName, BinaryData file, RequestOptions requestOptions) {
        return this.serviceClient.importGlossaryTermsViaCsvByGlossaryName(glossaryName, file, requestOptions);
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
    public Response<BinaryData> importGlossaryTermsViaCsvByGlossaryNameWithResponse(
            String glossaryName, BinaryData file, RequestOptions requestOptions, Context context) {
        return this.serviceClient.importGlossaryTermsViaCsvByGlossaryNameWithResponse(
                glossaryName, file, requestOptions, context);
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
    public BinaryData getImportCsvOperationStatus(String operationGuid, RequestOptions requestOptions) {
        return this.serviceClient.getImportCsvOperationStatus(operationGuid, requestOptions);
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
    public Response<BinaryData> getImportCsvOperationStatusWithResponse(
            String operationGuid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getImportCsvOperationStatusWithResponse(operationGuid, requestOptions, context);
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
    public Flux<ByteBuffer> exportGlossaryTermsAsCsv(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions) {
        return this.serviceClient.exportGlossaryTermsAsCsv(glossaryGuid, termGuids, requestOptions);
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
    public Response<Flux<ByteBuffer>> exportGlossaryTermsAsCsvWithResponse(
            String glossaryGuid, BinaryData termGuids, RequestOptions requestOptions, Context context) {
        return this.serviceClient.exportGlossaryTermsAsCsvWithResponse(
                glossaryGuid, termGuids, requestOptions, context);
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
    public BinaryData listTermsByGlossaryName(String glossaryName, RequestOptions requestOptions) {
        return this.serviceClient.listTermsByGlossaryName(glossaryName, requestOptions);
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
    public Response<BinaryData> listTermsByGlossaryNameWithResponse(
            String glossaryName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listTermsByGlossaryNameWithResponse(glossaryName, requestOptions, context);
    }
}
