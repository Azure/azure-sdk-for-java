package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.EntitiesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class)
public final class EntityClient {
    private final EntitiesImpl serviceClient;

    /**
     * Initializes an instance of Entities client.
     *
     * @param serviceClient the service client implementation.
     */
    EntityClient(EntitiesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Create or update an entity in Atlas. Existing entity is matched using its unique guid if supplied or by its
     * unique attributes eg: qualifiedName. Map and array of collections are not well supported. E.g.,
     * array&lt;array&lt;int&gt;&gt;, array&lt;map&lt;string, int&gt;&gt;.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdate(BinaryData entity, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdate(entity, requestOptions);
    }

    /**
     * Create or update an entity in Atlas. Existing entity is matched using its unique guid if supplied or by its
     * unique attributes eg: qualifiedName. Map and array of collections are not well supported. E.g.,
     * array&lt;array&lt;int&gt;&gt;, array&lt;map&lt;string, int&gt;&gt;.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(
            BinaryData entity, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createOrUpdateWithResponse(entity, requestOptions, context);
    }

    /**
     * List entities in bulk identified by its GUIDs.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>guids</td><td>String</td><td>Yes</td><td>An array of GUIDs of entities to create.</td></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>excludeRelationshipTypes</td><td>String</td><td>No</td><td>An array of the relationship types need to be excluded from the response.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listByGuids(RequestOptions requestOptions) {
        return this.serviceClient.listByGuids(requestOptions);
    }

    /**
     * List entities in bulk identified by its GUIDs.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>guids</td><td>String</td><td>Yes</td><td>An array of GUIDs of entities to create.</td></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>excludeRelationshipTypes</td><td>String</td><td>No</td><td>An array of the relationship types need to be excluded from the response.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listByGuidsWithResponse(RequestOptions requestOptions, Context context) {
        return this.serviceClient.listByGuidsWithResponse(requestOptions, context);
    }

    /**
     * Create or update entities in Atlas in bulk. Existing entity is matched using its unique guid if supplied or by
     * its unique attributes eg: qualifiedName. Map and array of collections are not well supported. E.g.,
     * array&lt;array&lt;int&gt;&gt;, array&lt;map&lt;string, int&gt;&gt;.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdateEntities(BinaryData entities, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateEntities(entities, requestOptions);
    }

    /**
     * Create or update entities in Atlas in bulk. Existing entity is matched using its unique guid if supplied or by
     * its unique attributes eg: qualifiedName. Map and array of collections are not well supported. E.g.,
     * array&lt;array&lt;int&gt;&gt;, array&lt;map&lt;string, int&gt;&gt;.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateEntitiesWithResponse(
            BinaryData entities, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createOrUpdateEntitiesWithResponse(entities, requestOptions, context);
    }

    /**
     * Delete a list of entities in bulk identified by their GUIDs or unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>guids</td><td>String</td><td>Yes</td><td>An array of GUIDs of entities to delete.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData deleteByGuids(RequestOptions requestOptions) {
        return this.serviceClient.deleteByGuids(requestOptions);
    }

    /**
     * Delete a list of entities in bulk identified by their GUIDs or unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>guids</td><td>String</td><td>Yes</td><td>An array of GUIDs of entities to delete.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteByGuidsWithResponse(RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteByGuidsWithResponse(requestOptions, context);
    }

    /**
     * Associate a classification to multiple entities in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classification: {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     *     entityGuids: [
     *         String
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addClassification(BinaryData request, RequestOptions requestOptions) {
        this.serviceClient.addClassification(request, requestOptions);
    }

    /**
     * Associate a classification to multiple entities in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classification: {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     *     entityGuids: [
     *         String
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addClassificationWithResponse(
            BinaryData request, RequestOptions requestOptions, Context context) {
        return this.serviceClient.addClassificationWithResponse(request, requestOptions, context);
    }

    /**
     * Get complete definition of an entity given its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getByGuid(String guid, RequestOptions requestOptions) {
        return this.serviceClient.getByGuid(guid, requestOptions);
    }

    /**
     * Get complete definition of an entity given its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getByGuidWithResponse(String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getByGuidWithResponse(guid, requestOptions, context);
    }

    /**
     * Update entity partially - create or update entity attribute identified by its GUID. Supports only primitive
     * attribute type and entity references. It does not support updating complex types like arrays, and maps. Null
     * updates are not possible.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>name</td><td>String</td><td>Yes</td><td>The name of the attribute.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Object
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData partialUpdateEntityAttributeByGuid(String guid, BinaryData body, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateEntityAttributeByGuid(guid, body, requestOptions);
    }

    /**
     * Update entity partially - create or update entity attribute identified by its GUID. Supports only primitive
     * attribute type and entity references. It does not support updating complex types like arrays, and maps. Null
     * updates are not possible.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>name</td><td>String</td><td>Yes</td><td>The name of the attribute.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * Object
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> partialUpdateEntityAttributeByGuidWithResponse(
            String guid, BinaryData body, RequestOptions requestOptions, Context context) {
        return this.serviceClient.partialUpdateEntityAttributeByGuidWithResponse(guid, body, requestOptions, context);
    }

    /**
     * Delete an entity identified by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData deleteByGuid(String guid, RequestOptions requestOptions) {
        return this.serviceClient.deleteByGuid(guid, requestOptions);
    }

    /**
     * Delete an entity identified by its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteByGuidWithResponse(String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteByGuidWithResponse(guid, requestOptions, context);
    }

    /**
     * List classifications for a given entity represented by a GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     entityGuid: String
     *     entityStatus: String(ACTIVE/DELETED)
     *     removePropagationsOnEntityDelete: Boolean
     *     validityPeriods: [
     *         {
     *             endTime: String
     *             startTime: String
     *             timeZone: String
     *         }
     *     ]
     *     source: String
     *     sourceDetails: {
     *         String: Object
     *     }
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getClassification(String guid, String classificationName, RequestOptions requestOptions) {
        return this.serviceClient.getClassification(guid, classificationName, requestOptions);
    }

    /**
     * List classifications for a given entity represented by a GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     entityGuid: String
     *     entityStatus: String(ACTIVE/DELETED)
     *     removePropagationsOnEntityDelete: Boolean
     *     validityPeriods: [
     *         {
     *             endTime: String
     *             startTime: String
     *             timeZone: String
     *         }
     *     ]
     *     source: String
     *     sourceDetails: {
     *         String: Object
     *     }
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getClassificationWithResponse(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getClassificationWithResponse(guid, classificationName, requestOptions, context);
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteClassification(String guid, String classificationName, RequestOptions requestOptions) {
        this.serviceClient.deleteClassification(guid, classificationName, requestOptions);
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationWithResponse(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteClassificationWithResponse(guid, classificationName, requestOptions, context);
    }

    /**
     * List classifications for a given entity represented by a GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     list: [
     *         Object
     *     ]
     *     pageSize: Integer
     *     sortBy: String
     *     sortType: String(NONE/ASC/DESC)
     *     startIndex: Long
     *     totalCount: Long
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getClassifications(String guid, RequestOptions requestOptions) {
        return this.serviceClient.getClassifications(guid, requestOptions);
    }

    /**
     * List classifications for a given entity represented by a GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     list: [
     *         Object
     *     ]
     *     pageSize: Integer
     *     sortBy: String
     *     sortType: String(NONE/ASC/DESC)
     *     startIndex: Long
     *     totalCount: Long
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getClassificationsWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getClassificationsWithResponse(guid, requestOptions, context);
    }

    /**
     * Add classifications to an existing entity represented by a GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addClassifications(String guid, BinaryData classifications, RequestOptions requestOptions) {
        this.serviceClient.addClassifications(guid, classifications, requestOptions);
    }

    /**
     * Add classifications to an existing entity represented by a GUID.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addClassificationsWithResponse(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return this.serviceClient.addClassificationsWithResponse(guid, classifications, requestOptions, context);
    }

    /**
     * Update classifications to an existing entity represented by a guid.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateClassifications(String guid, BinaryData classifications, RequestOptions requestOptions) {
        this.serviceClient.updateClassifications(guid, classifications, requestOptions);
    }

    /**
     * Update classifications to an existing entity represented by a guid.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateClassificationsWithResponse(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateClassificationsWithResponse(guid, classifications, requestOptions, context);
    }

    /**
     * Get complete definition of an entity given its type and unique attribute. In addition to the typeName path
     * parameter, attribute key-value pair(s) can be provided in the following format:
     * attr:\&lt;attrName&gt;=&lt;attrValue&gt;. NOTE: The attrName and attrValue should be unique across entities, eg.
     * qualifiedName. The REST request would look something like this: GET
     * /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getByUniqueAttributes(String typeName, RequestOptions requestOptions) {
        return this.serviceClient.getByUniqueAttributes(typeName, requestOptions);
    }

    /**
     * Get complete definition of an entity given its type and unique attribute. In addition to the typeName path
     * parameter, attribute key-value pair(s) can be provided in the following format:
     * attr:\&lt;attrName&gt;=&lt;attrValue&gt;. NOTE: The attrName and attrValue should be unique across entities, eg.
     * qualifiedName. The REST request would look something like this: GET
     * /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getByUniqueAttributesWithResponse(
            String typeName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getByUniqueAttributesWithResponse(typeName, requestOptions, context);
    }

    /**
     * Update entity partially - Allow a subset of attributes to be updated on an entity which is identified by its type
     * and unique attribute eg: Referenceable.qualifiedName. Null updates are not possible. In addition to the typeName
     * path parameter, attribute key-value pair(s) can be provided in the following format:
     * attr:&lt;attrName&gt;=&lt;attrValue&gt;. NOTE: The attrName and attrValue should be unique across entities, eg.
     * qualifiedName. The REST request would look something like this: PUT
     * /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData partialUpdateEntityByUniqueAttributes(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions) {
        return this.serviceClient.partialUpdateEntityByUniqueAttributes(
                typeName, atlasEntityWithExtInfo, requestOptions);
    }

    /**
     * Update entity partially - Allow a subset of attributes to be updated on an entity which is identified by its type
     * and unique attribute eg: Referenceable.qualifiedName. Null updates are not possible. In addition to the typeName
     * path parameter, attribute key-value pair(s) can be provided in the following format:
     * attr:&lt;attrName&gt;=&lt;attrValue&gt;. NOTE: The attrName and attrValue should be unique across entities, eg.
     * qualifiedName. The REST request would look something like this: PUT
     * /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entity: (recursive schema, see entity above)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     (recursive schema, see above)
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     (recursive schema, see above)
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> partialUpdateEntityByUniqueAttributesWithResponse(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions, Context context) {
        return this.serviceClient.partialUpdateEntityByUniqueAttributesWithResponse(
                typeName, atlasEntityWithExtInfo, requestOptions, context);
    }

    /**
     * Delete an entity identified by its type and unique attributes. In addition to the typeName path parameter,
     * attribute key-value pair(s) can be provided in the following format: attr:\&lt;attrName&gt;=\&lt;attrValue&gt;.
     * NOTE: The attrName and attrValue should be unique across entities, eg. qualifiedName. The REST request would look
     * something like this: DELETE /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData deleteByUniqueAttribute(String typeName, RequestOptions requestOptions) {
        return this.serviceClient.deleteByUniqueAttribute(typeName, requestOptions);
    }

    /**
     * Delete an entity identified by its type and unique attributes. In addition to the typeName path parameter,
     * attribute key-value pair(s) can be provided in the following format: attr:\&lt;attrName&gt;=\&lt;attrValue&gt;.
     * NOTE: The attrName and attrValue should be unique across entities, eg. qualifiedName. The REST request would look
     * something like this: DELETE /v2/entity/uniqueAttribute/type/aType?attr:aTypeAttribute=someValue.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidAssignments: {
     *         String: String
     *     }
     *     mutatedEntities: {
     *         String: [
     *             {
     *                 attributes: {
     *                     String: Object
     *                 }
     *                 typeName: String
     *                 lastModifiedTS: String
     *                 classificationNames: [
     *                     String
     *                 ]
     *                 classifications: [
     *                     {
     *                         attributes: {
     *                             String: Object
     *                         }
     *                         typeName: String
     *                         lastModifiedTS: String
     *                         entityGuid: String
     *                         entityStatus: String(ACTIVE/DELETED)
     *                         removePropagationsOnEntityDelete: Boolean
     *                         validityPeriods: [
     *                             {
     *                                 endTime: String
     *                                 startTime: String
     *                                 timeZone: String
     *                             }
     *                         ]
     *                         source: String
     *                         sourceDetails: {
     *                             String: Object
     *                         }
     *                     }
     *                 ]
     *                 displayText: String
     *                 guid: String
     *                 meaningNames: [
     *                     String
     *                 ]
     *                 meanings: [
     *                     {
     *                         confidence: Integer
     *                         createdBy: String
     *                         description: String
     *                         displayText: String
     *                         expression: String
     *                         relationGuid: String
     *                         source: String
     *                         status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                         steward: String
     *                         termGuid: String
     *                     }
     *                 ]
     *                 status: String(ACTIVE/DELETED)
     *             }
     *         ]
     *     }
     *     partialUpdatedEntities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteByUniqueAttributeWithResponse(
            String typeName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteByUniqueAttributeWithResponse(typeName, requestOptions, context);
    }

    /**
     * Delete a given classification from an entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * @param typeName The name of the type.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteClassificationByUniqueAttribute(
            String typeName, String classificationName, RequestOptions requestOptions) {
        this.serviceClient.deleteClassificationByUniqueAttribute(typeName, classificationName, requestOptions);
    }

    /**
     * Delete a given classification from an entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * @param typeName The name of the type.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationByUniqueAttributeWithResponse(
            String typeName, String classificationName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteClassificationByUniqueAttributeWithResponse(
                typeName, classificationName, requestOptions, context);
    }

    /**
     * Add classification to the entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addClassificationsByUniqueAttribute(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        this.serviceClient.addClassificationsByUniqueAttribute(typeName, atlasClassificationArray, requestOptions);
    }

    /**
     * Add classification to the entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addClassificationsByUniqueAttributeWithResponse(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return this.serviceClient.addClassificationsByUniqueAttributeWithResponse(
                typeName, atlasClassificationArray, requestOptions, context);
    }

    /**
     * Update classification on an entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateClassificationsByUniqueAttribute(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        this.serviceClient.updateClassificationsByUniqueAttribute(typeName, atlasClassificationArray, requestOptions);
    }

    /**
     * Update classification on an entity identified by its type and unique attributes.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>attrQualifiedName</td><td>String</td><td>No</td><td>The qualified name of the entity.</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         entityGuid: String
     *         entityStatus: String(ACTIVE/DELETED)
     *         removePropagationsOnEntityDelete: Boolean
     *         validityPeriods: [
     *             {
     *                 endTime: String
     *                 startTime: String
     *                 timeZone: String
     *             }
     *         ]
     *         source: String
     *         sourceDetails: {
     *             String: Object
     *         }
     *     }
     * ]
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateClassificationsByUniqueAttributeWithResponse(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateClassificationsByUniqueAttributeWithResponse(
                typeName, atlasClassificationArray, requestOptions, context);
    }

    /**
     * Set classifications on entities in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidHeaderMap: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classificationNames: [
     *                 String
     *             ]
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             displayText: String
     *             guid: String
     *             meaningNames: [
     *                 String
     *             ]
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             status: String(ACTIVE/DELETED)
     *         }
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData setClassifications(BinaryData entityHeaders, RequestOptions requestOptions) {
        return this.serviceClient.setClassifications(entityHeaders, requestOptions);
    }

    /**
     * Set classifications on entities in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     guidHeaderMap: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classificationNames: [
     *                 String
     *             ]
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             displayText: String
     *             guid: String
     *             meaningNames: [
     *                 String
     *             ]
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             status: String(ACTIVE/DELETED)
     *         }
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     String
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> setClassificationsWithResponse(
            BinaryData entityHeaders, RequestOptions requestOptions, Context context) {
        return this.serviceClient.setClassificationsWithResponse(entityHeaders, requestOptions, context);
    }

    /**
     * Bulk API to retrieve list of entities identified by its unique attributes.
     *
     * <p>In addition to the typeName path parameter, attribute key-value pair(s) can be provided in the following
     * format
     *
     * <p>typeName=\&lt;typeName&gt;&amp;attr_1:\&lt;attrName&gt;=\&lt;attrValue&gt;&amp;attr_2:\&lt;attrName&gt;=\&lt;attrValue&gt;&amp;attr_3:\&lt;attrName&gt;=\&lt;attrValue&gt;
     *
     * <p>NOTE: The attrName should be an unique attribute for the given entity-type
     *
     * <p>The REST request would look something like this
     *
     * <p>GET
     * /v2/entity/bulk/uniqueAttribute/type/hive_db?attr_0:qualifiedName=db1@cl1&amp;attr_2:qualifiedName=db2@cl1.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>attrNQualifiedName</td><td>String</td><td>No</td><td>Qualified name of an entity. E.g. to find 2 entities you can set attrs_0:qualifiedName=db1@cl1&amp;attrs_2:qualifiedName=db2@cl1</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEntitiesByUniqueAttributes(String typeName, RequestOptions requestOptions) {
        return this.serviceClient.getEntitiesByUniqueAttributes(typeName, requestOptions);
    }

    /**
     * Bulk API to retrieve list of entities identified by its unique attributes.
     *
     * <p>In addition to the typeName path parameter, attribute key-value pair(s) can be provided in the following
     * format
     *
     * <p>typeName=\&lt;typeName&gt;&amp;attr_1:\&lt;attrName&gt;=\&lt;attrValue&gt;&amp;attr_2:\&lt;attrName&gt;=\&lt;attrValue&gt;&amp;attr_3:\&lt;attrName&gt;=\&lt;attrValue&gt;
     *
     * <p>NOTE: The attrName should be an unique attribute for the given entity-type
     *
     * <p>The REST request would look something like this
     *
     * <p>GET
     * /v2/entity/bulk/uniqueAttribute/type/hive_db?attr_0:qualifiedName=db1@cl1&amp;attr_2:qualifiedName=db2@cl1.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>minExtInfo</td><td>String</td><td>No</td><td>Whether to return minimal information for referred entities.</td></tr>
     *     <tr><td>ignoreRelationships</td><td>String</td><td>No</td><td>Whether to ignore relationship attributes.</td></tr>
     *     <tr><td>attrNQualifiedName</td><td>String</td><td>No</td><td>Qualified name of an entity. E.g. to find 2 entities you can set attrs_0:qualifiedName=db1@cl1&amp;attrs_2:qualifiedName=db2@cl1</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     referredEntities: {
     *         String: {
     *             attributes: {
     *                 String: Object
     *             }
     *             typeName: String
     *             lastModifiedTS: String
     *             classifications: [
     *                 {
     *                     attributes: {
     *                         String: Object
     *                     }
     *                     typeName: String
     *                     lastModifiedTS: String
     *                     entityGuid: String
     *                     entityStatus: String(ACTIVE/DELETED)
     *                     removePropagationsOnEntityDelete: Boolean
     *                     validityPeriods: [
     *                         {
     *                             endTime: String
     *                             startTime: String
     *                             timeZone: String
     *                         }
     *                     ]
     *                     source: String
     *                     sourceDetails: {
     *                         String: Object
     *                     }
     *                 }
     *             ]
     *             createTime: Float
     *             createdBy: String
     *             guid: String
     *             homeId: String
     *             meanings: [
     *                 {
     *                     confidence: Integer
     *                     createdBy: String
     *                     description: String
     *                     displayText: String
     *                     expression: String
     *                     relationGuid: String
     *                     source: String
     *                     status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *                     steward: String
     *                     termGuid: String
     *                 }
     *             ]
     *             provenanceType: Float
     *             proxy: Boolean
     *             relationshipAttributes: {
     *                 String: Object
     *             }
     *             status: String(ACTIVE/DELETED)
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             source: String
     *             sourceDetails: {
     *                 String: Object
     *             }
     *             contacts: {
     *                 String: [
     *                     {
     *                         id: String
     *                         info: String
     *                     }
     *                 ]
     *             }
     *         }
     *     }
     *     entities: [
     *         (recursive schema, see above)
     *     ]
     * }
     * }</pre>
     *
     * @param typeName The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEntitiesByUniqueAttributesWithResponse(
            String typeName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getEntitiesByUniqueAttributesWithResponse(typeName, requestOptions, context);
    }

    /**
     * Get entity header given its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     classificationNames: [
     *         String
     *     ]
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
     *     displayText: String
     *     guid: String
     *     meaningNames: [
     *         String
     *     ]
     *     meanings: [
     *         {
     *             confidence: Integer
     *             createdBy: String
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     status: String(ACTIVE/DELETED)
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getHeader(String guid, RequestOptions requestOptions) {
        return this.serviceClient.getHeader(guid, requestOptions);
    }

    /**
     * Get entity header given its GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     classificationNames: [
     *         String
     *     ]
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
     *     displayText: String
     *     guid: String
     *     meaningNames: [
     *         String
     *     ]
     *     meanings: [
     *         {
     *             confidence: Integer
     *             createdBy: String
     *             description: String
     *             displayText: String
     *             expression: String
     *             relationGuid: String
     *             source: String
     *             status: String(DISCOVERED/PROPOSED/IMPORTED/VALIDATED/DEPRECATED/OBSOLETE/OTHER)
     *             steward: String
     *             termGuid: String
     *         }
     *     ]
     *     status: String(ACTIVE/DELETED)
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getHeaderWithResponse(String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getHeaderWithResponse(guid, requestOptions, context);
    }
}
