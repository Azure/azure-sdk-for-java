package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.LineagesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class)
public final class LineageClient {
    private final LineagesImpl serviceClient;

    /**
     * Initializes an instance of Lineages client.
     *
     * @param serviceClient the service client implementation.
     */
    LineageClient(LineagesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get lineage info of the entity specified by GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>depth</td><td>String</td><td>No</td><td>The number of hops for lineage.</td></tr>
     *     <tr><td>width</td><td>String</td><td>No</td><td>The number of max expanding width in lineage.</td></tr>
     *     <tr><td>direction</td><td>String</td><td>Yes</td><td>The direction of the lineage, which could be INPUT, OUTPUT or BOTH.</td></tr>
     *     <tr><td>includeParent</td><td>String</td><td>No</td><td>True to include the parent chain in the response.</td></tr>
     *     <tr><td>getDerivedLineage</td><td>String</td><td>No</td><td>True to include derived lineage in the response</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     baseEntityGuid: String
     *     guidEntityMap: {
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
     *     widthCounts: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     lineageDepth: Integer
     *     lineageWidth: Integer
     *     includeParent: Boolean
     *     childrenCount: Integer
     *     lineageDirection: String(INPUT/OUTPUT/BOTH)
     *     parentRelations: [
     *         {
     *             childEntityId: String
     *             relationshipId: String
     *             parentEntityId: String
     *         }
     *     ]
     *     relations: [
     *         {
     *             fromEntityId: String
     *             relationshipId: String
     *             toEntityId: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getLineageGraph(String guid, RequestOptions requestOptions) {
        return this.serviceClient.getLineageGraph(guid, requestOptions);
    }

    /**
     * Get lineage info of the entity specified by GUID.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>depth</td><td>String</td><td>No</td><td>The number of hops for lineage.</td></tr>
     *     <tr><td>width</td><td>String</td><td>No</td><td>The number of max expanding width in lineage.</td></tr>
     *     <tr><td>direction</td><td>String</td><td>Yes</td><td>The direction of the lineage, which could be INPUT, OUTPUT or BOTH.</td></tr>
     *     <tr><td>includeParent</td><td>String</td><td>No</td><td>True to include the parent chain in the response.</td></tr>
     *     <tr><td>getDerivedLineage</td><td>String</td><td>No</td><td>True to include derived lineage in the response</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     baseEntityGuid: String
     *     guidEntityMap: {
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
     *     widthCounts: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     lineageDepth: Integer
     *     lineageWidth: Integer
     *     includeParent: Boolean
     *     childrenCount: Integer
     *     lineageDirection: String(INPUT/OUTPUT/BOTH)
     *     parentRelations: [
     *         {
     *             childEntityId: String
     *             relationshipId: String
     *             parentEntityId: String
     *         }
     *     ]
     *     relations: [
     *         {
     *             fromEntityId: String
     *             relationshipId: String
     *             toEntityId: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getLineageGraphWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getLineageGraphWithResponse(guid, requestOptions, context);
    }

    /**
     * Return immediate next page lineage info about entity with pagination.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>direction</td><td>String</td><td>Yes</td><td>The direction of the lineage, which could be INPUT, OUTPUT or BOTH.</td></tr>
     *     <tr><td>getDerivedLineage</td><td>String</td><td>No</td><td>True to include derived lineage in the response</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     baseEntityGuid: String
     *     guidEntityMap: {
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
     *     widthCounts: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     lineageDepth: Integer
     *     lineageWidth: Integer
     *     includeParent: Boolean
     *     childrenCount: Integer
     *     lineageDirection: String(INPUT/OUTPUT/BOTH)
     *     parentRelations: [
     *         {
     *             childEntityId: String
     *             relationshipId: String
     *             parentEntityId: String
     *         }
     *     ]
     *     relations: [
     *         {
     *             fromEntityId: String
     *             relationshipId: String
     *             toEntityId: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData nextPageLineage(String guid, RequestOptions requestOptions) {
        return this.serviceClient.nextPageLineage(guid, requestOptions);
    }

    /**
     * Return immediate next page lineage info about entity with pagination.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>direction</td><td>String</td><td>Yes</td><td>The direction of the lineage, which could be INPUT, OUTPUT or BOTH.</td></tr>
     *     <tr><td>getDerivedLineage</td><td>String</td><td>No</td><td>True to include derived lineage in the response</td></tr>
     *     <tr><td>offset</td><td>String</td><td>No</td><td>The offset for pagination purpose.</td></tr>
     *     <tr><td>limit</td><td>String</td><td>No</td><td>The page size - by default there is no paging.</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     baseEntityGuid: String
     *     guidEntityMap: {
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
     *     widthCounts: {
     *         String: {
     *             String: Object
     *         }
     *     }
     *     lineageDepth: Integer
     *     lineageWidth: Integer
     *     includeParent: Boolean
     *     childrenCount: Integer
     *     lineageDirection: String(INPUT/OUTPUT/BOTH)
     *     parentRelations: [
     *         {
     *             childEntityId: String
     *             relationshipId: String
     *             parentEntityId: String
     *         }
     *     ]
     *     relations: [
     *         {
     *             fromEntityId: String
     *             relationshipId: String
     *             toEntityId: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> nextPageLineageWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return this.serviceClient.nextPageLineageWithResponse(guid, requestOptions, context);
    }
}
