package com.azure.analytics.purview.catalog.implementation;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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

/** An instance of this class provides access to all the operations defined in Lineages. */
public final class LineagesImpl {
    /** The proxy service used to perform REST calls. */
    private final LineagesService service;

    /** The service client containing this operation class. */
    private final PurviewCatalogServiceRestAPIDocumentImpl client;

    /**
     * Initializes an instance of LineagesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    LineagesImpl(PurviewCatalogServiceRestAPIDocumentImpl client) {
        this.service = RestProxy.create(LineagesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for PurviewCatalogServiceRestAPIDocumentLineages to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}/api")
    @ServiceInterface(name = "PurviewCatalogServic")
    private interface LineagesService {
        @Get("/atlas/v2/lineage/{guid}")
        Mono<Response<BinaryData>> getLineageGraph(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/lineage/{guid}/next/")
        Mono<Response<BinaryData>> nextPageLineage(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
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
    public Mono<Response<BinaryData>> getLineageGraphWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getLineageGraph(this.client.getEndpoint(), guid, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getLineageGraphWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getLineageGraph(this.client.getEndpoint(), guid, accept, requestOptions, context);
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
    public Mono<BinaryData> getLineageGraphAsync(String guid, RequestOptions requestOptions) {
        return getLineageGraphWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> getLineageGraphAsync(String guid, RequestOptions requestOptions, Context context) {
        return getLineageGraphWithResponseAsync(guid, requestOptions, context)
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
        return getLineageGraphAsync(guid, requestOptions).block();
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
        return getLineageGraphWithResponseAsync(guid, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> nextPageLineageWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.nextPageLineage(
                                this.client.getEndpoint(),
                                guid,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> nextPageLineageWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.nextPageLineage(
                this.client.getEndpoint(), guid, this.client.getApiVersion(), accept, requestOptions, context);
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
    public Mono<BinaryData> nextPageLineageAsync(String guid, RequestOptions requestOptions) {
        return nextPageLineageWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> nextPageLineageAsync(String guid, RequestOptions requestOptions, Context context) {
        return nextPageLineageWithResponseAsync(guid, requestOptions, context)
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
        return nextPageLineageAsync(guid, requestOptions).block();
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
        return nextPageLineageWithResponseAsync(guid, requestOptions, context).block();
    }
}
