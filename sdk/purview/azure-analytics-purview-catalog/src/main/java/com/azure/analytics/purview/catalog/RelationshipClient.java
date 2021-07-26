package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.RelationshipsImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class)
public final class RelationshipClient {
    private final RelationshipsImpl serviceClient;

    /**
     * Initializes an instance of Relationships client.
     *
     * @param serviceClient the service client implementation.
     */
    RelationshipClient(RelationshipsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Create a new relationship between entities.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     createTime: Float
     *     createdBy: String
     *     end1: {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *     }
     *     end2: (recursive schema, see end2 above)
     *     guid: String
     *     homeId: String
     *     label: String
     *     provenanceType: Float
     *     status: String(ACTIVE/DELETED)
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
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
    public BinaryData create(BinaryData relationship, RequestOptions requestOptions) {
        return this.serviceClient.create(relationship, requestOptions);
    }

    /**
     * Create a new relationship between entities.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     createTime: Float
     *     createdBy: String
     *     end1: {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *     }
     *     end2: (recursive schema, see end2 above)
     *     guid: String
     *     homeId: String
     *     label: String
     *     provenanceType: Float
     *     status: String(ACTIVE/DELETED)
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
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
    public Response<BinaryData> createWithResponse(
            BinaryData relationship, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createWithResponse(relationship, requestOptions, context);
    }

    /**
     * Update an existing relationship between entities.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     createTime: Float
     *     createdBy: String
     *     end1: {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *     }
     *     end2: (recursive schema, see end2 above)
     *     guid: String
     *     homeId: String
     *     label: String
     *     provenanceType: Float
     *     status: String(ACTIVE/DELETED)
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
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
    public BinaryData update(BinaryData relationship, RequestOptions requestOptions) {
        return this.serviceClient.update(relationship, requestOptions);
    }

    /**
     * Update an existing relationship between entities.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     attributes: {
     *         String: Object
     *     }
     *     typeName: String
     *     lastModifiedTS: String
     *     createTime: Float
     *     createdBy: String
     *     end1: {
     *         guid: String
     *         typeName: String
     *         uniqueAttributes: {
     *             String: Object
     *         }
     *     }
     *     end2: (recursive schema, see end2 above)
     *     guid: String
     *     homeId: String
     *     label: String
     *     provenanceType: Float
     *     status: String(ACTIVE/DELETED)
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
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
    public Response<BinaryData> updateWithResponse(
            BinaryData relationship, RequestOptions requestOptions, Context context) {
        return this.serviceClient.updateWithResponse(relationship, requestOptions, context);
    }

    /**
     * Get relationship information between entities by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>extendedInfo</td><td>String</td><td>No</td><td>Limits whether includes extended information.</td></tr>
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
     *     relationship: {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         createTime: Float
     *         createdBy: String
     *         end1: {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *         }
     *         end2: (recursive schema, see end2 above)
     *         guid: String
     *         homeId: String
     *         label: String
     *         provenanceType: Float
     *         status: String(ACTIVE/DELETED)
     *         updateTime: Float
     *         updatedBy: String
     *         version: Float
     *     }
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String guid, RequestOptions requestOptions) {
        return this.serviceClient.get(guid, requestOptions);
    }

    /**
     * Get relationship information between entities by its GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>extendedInfo</td><td>String</td><td>No</td><td>Limits whether includes extended information.</td></tr>
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
     *     relationship: {
     *         attributes: {
     *             String: Object
     *         }
     *         typeName: String
     *         lastModifiedTS: String
     *         createTime: Float
     *         createdBy: String
     *         end1: {
     *             guid: String
     *             typeName: String
     *             uniqueAttributes: {
     *                 String: Object
     *             }
     *         }
     *         end2: (recursive schema, see end2 above)
     *         guid: String
     *         homeId: String
     *         label: String
     *         provenanceType: Float
     *         status: String(ACTIVE/DELETED)
     *         updateTime: Float
     *         updatedBy: String
     *         version: Float
     *     }
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getWithResponse(guid, requestOptions, context);
    }

    /**
     * Delete a relationship between entities by its GUID.
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String guid, RequestOptions requestOptions) {
        this.serviceClient.delete(guid, requestOptions);
    }

    /**
     * Delete a relationship between entities by its GUID.
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteWithResponse(guid, requestOptions, context);
    }
}
