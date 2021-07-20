package com.azure.analytics.purview.catalog.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import java.util.List;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Entities. */
public final class EntitiesImpl {
    /** The proxy service used to perform REST calls. */
    private final EntitiesService service;

    /** The service client containing this operation class. */
    private final PurviewCatalogServiceRestAPIDocumentImpl client;

    /**
     * Initializes an instance of EntitiesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    EntitiesImpl(PurviewCatalogServiceRestAPIDocumentImpl client) {
        this.service = RestProxy.create(EntitiesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for PurviewCatalogServiceRestAPIDocumentEntities to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}/api")
    @ServiceInterface(name = "PurviewCatalogServic")
    private interface EntitiesService {
        @Post("/atlas/v2/entity")
        Mono<Response<BinaryData>> createOrUpdate(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData entity,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/bulk")
        Mono<Response<BinaryData>> listByGuids(
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/entity/bulk")
        Mono<Response<BinaryData>> createOrUpdateEntities(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData entities,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/entity/bulk")
        Mono<Response<BinaryData>> deleteByGuids(
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/entity/bulk/classification")
        Mono<Response<Void>> addClassification(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData request,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/guid/{guid}")
        Mono<Response<BinaryData>> getByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/entity/guid/{guid}")
        Mono<Response<BinaryData>> partialUpdateEntityAttributeByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @BodyParam("application/json") BinaryData body,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/entity/guid/{guid}")
        Mono<Response<BinaryData>> deleteByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/guid/{guid}/classification/{classificationName}")
        Mono<Response<BinaryData>> getClassification(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @PathParam("classificationName") String classificationName,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/entity/guid/{guid}/classification/{classificationName}")
        Mono<Response<Void>> deleteClassification(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @PathParam("classificationName") String classificationName,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/guid/{guid}/classifications")
        Mono<Response<BinaryData>> getClassifications(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/entity/guid/{guid}/classifications")
        Mono<Response<Void>> addClassifications(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @BodyParam("application/json") BinaryData classifications,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/entity/guid/{guid}/classifications")
        Mono<Response<Void>> updateClassifications(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @BodyParam("application/json") BinaryData classifications,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/uniqueAttribute/type/{typeName}")
        Mono<Response<BinaryData>> getByUniqueAttributes(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/entity/uniqueAttribute/type/{typeName}")
        Mono<Response<BinaryData>> partialUpdateEntityByUniqueAttributes(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @BodyParam("application/json") BinaryData atlasEntityWithExtInfo,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/entity/uniqueAttribute/type/{typeName}")
        Mono<Response<BinaryData>> deleteByUniqueAttribute(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/entity/uniqueAttribute/type/{typeName}/classification/{classificationName}")
        Mono<Response<Void>> deleteClassificationByUniqueAttribute(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @PathParam("classificationName") String classificationName,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/entity/uniqueAttribute/type/{typeName}/classifications")
        Mono<Response<Void>> addClassificationsByUniqueAttribute(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @BodyParam("application/json") BinaryData atlasClassificationArray,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/entity/uniqueAttribute/type/{typeName}/classifications")
        Mono<Response<Void>> updateClassificationsByUniqueAttribute(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @BodyParam("application/json") BinaryData atlasClassificationArray,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/entity/bulk/setClassifications")
        Mono<Response<List<String>>> setClassifications(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData entityHeaders,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/bulk/uniqueAttribute/type/{typeName}")
        Mono<Response<BinaryData>> getEntitiesByUniqueAttributes(
                @HostParam("Endpoint") String endpoint,
                @PathParam("typeName") String typeName,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/entity/guid/{guid}/header")
        Mono<Response<BinaryData>> getHeader(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
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
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            BinaryData entity, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.createOrUpdate(this.client.getEndpoint(), entity, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> createOrUpdateWithResponseAsync(
            BinaryData entity, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createOrUpdate(this.client.getEndpoint(), entity, accept, requestOptions, context);
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
    public Mono<BinaryData> createOrUpdateAsync(BinaryData entity, RequestOptions requestOptions) {
        return createOrUpdateWithResponseAsync(entity, requestOptions)
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
    public Mono<BinaryData> createOrUpdateAsync(BinaryData entity, RequestOptions requestOptions, Context context) {
        return createOrUpdateWithResponseAsync(entity, requestOptions, context)
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
        return createOrUpdateAsync(entity, requestOptions).block();
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
        return createOrUpdateWithResponseAsync(entity, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> listByGuidsWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.listByGuids(this.client.getEndpoint(), accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> listByGuidsWithResponseAsync(RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listByGuids(this.client.getEndpoint(), accept, requestOptions, context);
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
    public Mono<BinaryData> listByGuidsAsync(RequestOptions requestOptions) {
        return listByGuidsWithResponseAsync(requestOptions)
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
    public Mono<BinaryData> listByGuidsAsync(RequestOptions requestOptions, Context context) {
        return listByGuidsWithResponseAsync(requestOptions, context)
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
        return listByGuidsAsync(requestOptions).block();
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
        return listByGuidsWithResponseAsync(requestOptions, context).block();
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
    public Mono<Response<BinaryData>> createOrUpdateEntitiesWithResponseAsync(
            BinaryData entities, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createOrUpdateEntities(
                                this.client.getEndpoint(), entities, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> createOrUpdateEntitiesWithResponseAsync(
            BinaryData entities, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createOrUpdateEntities(this.client.getEndpoint(), entities, accept, requestOptions, context);
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
    public Mono<BinaryData> createOrUpdateEntitiesAsync(BinaryData entities, RequestOptions requestOptions) {
        return createOrUpdateEntitiesWithResponseAsync(entities, requestOptions)
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
    public Mono<BinaryData> createOrUpdateEntitiesAsync(
            BinaryData entities, RequestOptions requestOptions, Context context) {
        return createOrUpdateEntitiesWithResponseAsync(entities, requestOptions, context)
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
        return createOrUpdateEntitiesAsync(entities, requestOptions).block();
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
        return createOrUpdateEntitiesWithResponseAsync(entities, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> deleteByGuidsWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.deleteByGuids(this.client.getEndpoint(), accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> deleteByGuidsWithResponseAsync(RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.deleteByGuids(this.client.getEndpoint(), accept, requestOptions, context);
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
    public Mono<BinaryData> deleteByGuidsAsync(RequestOptions requestOptions) {
        return deleteByGuidsWithResponseAsync(requestOptions)
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
    public Mono<BinaryData> deleteByGuidsAsync(RequestOptions requestOptions, Context context) {
        return deleteByGuidsWithResponseAsync(requestOptions, context)
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
        return deleteByGuidsAsync(requestOptions).block();
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
        return deleteByGuidsWithResponseAsync(requestOptions, context).block();
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
    public Mono<Response<Void>> addClassificationWithResponseAsync(BinaryData request, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context -> service.addClassification(this.client.getEndpoint(), request, requestOptions, context));
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
    public Mono<Response<Void>> addClassificationWithResponseAsync(
            BinaryData request, RequestOptions requestOptions, Context context) {
        return service.addClassification(this.client.getEndpoint(), request, requestOptions, context);
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
    public Mono<Void> addClassificationAsync(BinaryData request, RequestOptions requestOptions) {
        return addClassificationWithResponseAsync(request, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> addClassificationAsync(BinaryData request, RequestOptions requestOptions, Context context) {
        return addClassificationWithResponseAsync(request, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        addClassificationAsync(request, requestOptions).block();
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
        return addClassificationWithResponseAsync(request, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getByGuidWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
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
    public Mono<BinaryData> getByGuidAsync(String guid, RequestOptions requestOptions) {
        return getByGuidWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> getByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getByGuidWithResponseAsync(guid, requestOptions, context)
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
        return getByGuidAsync(guid, requestOptions).block();
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
        return getByGuidWithResponseAsync(guid, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> partialUpdateEntityAttributeByGuidWithResponseAsync(
            String guid, BinaryData body, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.partialUpdateEntityAttributeByGuid(
                                this.client.getEndpoint(), guid, body, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> partialUpdateEntityAttributeByGuidWithResponseAsync(
            String guid, BinaryData body, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.partialUpdateEntityAttributeByGuid(
                this.client.getEndpoint(), guid, body, accept, requestOptions, context);
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
    public Mono<BinaryData> partialUpdateEntityAttributeByGuidAsync(
            String guid, BinaryData body, RequestOptions requestOptions) {
        return partialUpdateEntityAttributeByGuidWithResponseAsync(guid, body, requestOptions)
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
    public Mono<BinaryData> partialUpdateEntityAttributeByGuidAsync(
            String guid, BinaryData body, RequestOptions requestOptions, Context context) {
        return partialUpdateEntityAttributeByGuidWithResponseAsync(guid, body, requestOptions, context)
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
        return partialUpdateEntityAttributeByGuidAsync(guid, body, requestOptions).block();
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
        return partialUpdateEntityAttributeByGuidWithResponseAsync(guid, body, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> deleteByGuidWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.deleteByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> deleteByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.deleteByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
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
    public Mono<BinaryData> deleteByGuidAsync(String guid, RequestOptions requestOptions) {
        return deleteByGuidWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> deleteByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return deleteByGuidWithResponseAsync(guid, requestOptions, context)
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
        return deleteByGuidAsync(guid, requestOptions).block();
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
        return deleteByGuidWithResponseAsync(guid, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getClassificationWithResponseAsync(
            String guid, String classificationName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getClassification(
                                this.client.getEndpoint(), guid, classificationName, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getClassificationWithResponseAsync(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getClassification(
                this.client.getEndpoint(), guid, classificationName, accept, requestOptions, context);
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
    public Mono<BinaryData> getClassificationAsync(
            String guid, String classificationName, RequestOptions requestOptions) {
        return getClassificationWithResponseAsync(guid, classificationName, requestOptions)
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
    public Mono<BinaryData> getClassificationAsync(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        return getClassificationWithResponseAsync(guid, classificationName, requestOptions, context)
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
        return getClassificationAsync(guid, classificationName, requestOptions).block();
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
        return getClassificationWithResponseAsync(guid, classificationName, requestOptions, context).block();
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteClassificationWithResponseAsync(
            String guid, String classificationName, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.deleteClassification(
                                this.client.getEndpoint(), guid, classificationName, requestOptions, context));
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteClassificationWithResponseAsync(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        return service.deleteClassification(
                this.client.getEndpoint(), guid, classificationName, requestOptions, context);
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteClassificationAsync(String guid, String classificationName, RequestOptions requestOptions) {
        return deleteClassificationWithResponseAsync(guid, classificationName, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteClassificationAsync(
            String guid, String classificationName, RequestOptions requestOptions, Context context) {
        return deleteClassificationWithResponseAsync(guid, classificationName, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete a given classification from an existing entity represented by a GUID.
     *
     * @param guid The globally unique identifier of the entity.
     * @param classificationName The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteClassification(String guid, String classificationName, RequestOptions requestOptions) {
        deleteClassificationAsync(guid, classificationName, requestOptions).block();
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
        return deleteClassificationWithResponseAsync(guid, classificationName, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getClassificationsWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getClassifications(this.client.getEndpoint(), guid, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getClassificationsWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getClassifications(this.client.getEndpoint(), guid, accept, requestOptions, context);
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
    public Mono<BinaryData> getClassificationsAsync(String guid, RequestOptions requestOptions) {
        return getClassificationsWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> getClassificationsAsync(String guid, RequestOptions requestOptions, Context context) {
        return getClassificationsWithResponseAsync(guid, requestOptions, context)
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
        return getClassificationsAsync(guid, requestOptions).block();
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
        return getClassificationsWithResponseAsync(guid, requestOptions, context).block();
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
    public Mono<Response<Void>> addClassificationsWithResponseAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.addClassifications(
                                this.client.getEndpoint(), guid, classifications, requestOptions, context));
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
    public Mono<Response<Void>> addClassificationsWithResponseAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return service.addClassifications(this.client.getEndpoint(), guid, classifications, requestOptions, context);
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
    public Mono<Void> addClassificationsAsync(String guid, BinaryData classifications, RequestOptions requestOptions) {
        return addClassificationsWithResponseAsync(guid, classifications, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> addClassificationsAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return addClassificationsWithResponseAsync(guid, classifications, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        addClassificationsAsync(guid, classifications, requestOptions).block();
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
        return addClassificationsWithResponseAsync(guid, classifications, requestOptions, context).block();
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
    public Mono<Response<Void>> updateClassificationsWithResponseAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.updateClassifications(
                                this.client.getEndpoint(), guid, classifications, requestOptions, context));
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
    public Mono<Response<Void>> updateClassificationsWithResponseAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return service.updateClassifications(this.client.getEndpoint(), guid, classifications, requestOptions, context);
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
    public Mono<Void> updateClassificationsAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions) {
        return updateClassificationsWithResponseAsync(guid, classifications, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> updateClassificationsAsync(
            String guid, BinaryData classifications, RequestOptions requestOptions, Context context) {
        return updateClassificationsWithResponseAsync(guid, classifications, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        updateClassificationsAsync(guid, classifications, requestOptions).block();
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
        return updateClassificationsWithResponseAsync(guid, classifications, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getByUniqueAttributesWithResponseAsync(
            String typeName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getByUniqueAttributes(
                                this.client.getEndpoint(), typeName, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getByUniqueAttributesWithResponseAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getByUniqueAttributes(this.client.getEndpoint(), typeName, accept, requestOptions, context);
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
    public Mono<BinaryData> getByUniqueAttributesAsync(String typeName, RequestOptions requestOptions) {
        return getByUniqueAttributesWithResponseAsync(typeName, requestOptions)
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
    public Mono<BinaryData> getByUniqueAttributesAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        return getByUniqueAttributesWithResponseAsync(typeName, requestOptions, context)
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
        return getByUniqueAttributesAsync(typeName, requestOptions).block();
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
        return getByUniqueAttributesWithResponseAsync(typeName, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> partialUpdateEntityByUniqueAttributesWithResponseAsync(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.partialUpdateEntityByUniqueAttributes(
                                this.client.getEndpoint(),
                                typeName,
                                atlasEntityWithExtInfo,
                                accept,
                                requestOptions,
                                context));
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
    public Mono<Response<BinaryData>> partialUpdateEntityByUniqueAttributesWithResponseAsync(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.partialUpdateEntityByUniqueAttributes(
                this.client.getEndpoint(), typeName, atlasEntityWithExtInfo, accept, requestOptions, context);
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
    public Mono<BinaryData> partialUpdateEntityByUniqueAttributesAsync(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions) {
        return partialUpdateEntityByUniqueAttributesWithResponseAsync(typeName, atlasEntityWithExtInfo, requestOptions)
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
    public Mono<BinaryData> partialUpdateEntityByUniqueAttributesAsync(
            String typeName, BinaryData atlasEntityWithExtInfo, RequestOptions requestOptions, Context context) {
        return partialUpdateEntityByUniqueAttributesWithResponseAsync(
                        typeName, atlasEntityWithExtInfo, requestOptions, context)
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
        return partialUpdateEntityByUniqueAttributesAsync(typeName, atlasEntityWithExtInfo, requestOptions).block();
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
        return partialUpdateEntityByUniqueAttributesWithResponseAsync(
                        typeName, atlasEntityWithExtInfo, requestOptions, context)
                .block();
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
    public Mono<Response<BinaryData>> deleteByUniqueAttributeWithResponseAsync(
            String typeName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.deleteByUniqueAttribute(
                                this.client.getEndpoint(), typeName, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> deleteByUniqueAttributeWithResponseAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.deleteByUniqueAttribute(this.client.getEndpoint(), typeName, accept, requestOptions, context);
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
    public Mono<BinaryData> deleteByUniqueAttributeAsync(String typeName, RequestOptions requestOptions) {
        return deleteByUniqueAttributeWithResponseAsync(typeName, requestOptions)
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
    public Mono<BinaryData> deleteByUniqueAttributeAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        return deleteByUniqueAttributeWithResponseAsync(typeName, requestOptions, context)
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
        return deleteByUniqueAttributeAsync(typeName, requestOptions).block();
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
        return deleteByUniqueAttributeWithResponseAsync(typeName, requestOptions, context).block();
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
    public Mono<Response<Void>> deleteClassificationByUniqueAttributeWithResponseAsync(
            String typeName, String classificationName, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.deleteClassificationByUniqueAttribute(
                                this.client.getEndpoint(), typeName, classificationName, requestOptions, context));
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
    public Mono<Response<Void>> deleteClassificationByUniqueAttributeWithResponseAsync(
            String typeName, String classificationName, RequestOptions requestOptions, Context context) {
        return service.deleteClassificationByUniqueAttribute(
                this.client.getEndpoint(), typeName, classificationName, requestOptions, context);
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
    public Mono<Void> deleteClassificationByUniqueAttributeAsync(
            String typeName, String classificationName, RequestOptions requestOptions) {
        return deleteClassificationByUniqueAttributeWithResponseAsync(typeName, classificationName, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> deleteClassificationByUniqueAttributeAsync(
            String typeName, String classificationName, RequestOptions requestOptions, Context context) {
        return deleteClassificationByUniqueAttributeWithResponseAsync(
                        typeName, classificationName, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        deleteClassificationByUniqueAttributeAsync(typeName, classificationName, requestOptions).block();
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
        return deleteClassificationByUniqueAttributeWithResponseAsync(
                        typeName, classificationName, requestOptions, context)
                .block();
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
    public Mono<Response<Void>> addClassificationsByUniqueAttributeWithResponseAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.addClassificationsByUniqueAttribute(
                                this.client.getEndpoint(),
                                typeName,
                                atlasClassificationArray,
                                requestOptions,
                                context));
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
    public Mono<Response<Void>> addClassificationsByUniqueAttributeWithResponseAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return service.addClassificationsByUniqueAttribute(
                this.client.getEndpoint(), typeName, atlasClassificationArray, requestOptions, context);
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
    public Mono<Void> addClassificationsByUniqueAttributeAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        return addClassificationsByUniqueAttributeWithResponseAsync(typeName, atlasClassificationArray, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> addClassificationsByUniqueAttributeAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return addClassificationsByUniqueAttributeWithResponseAsync(
                        typeName, atlasClassificationArray, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        addClassificationsByUniqueAttributeAsync(typeName, atlasClassificationArray, requestOptions).block();
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
        return addClassificationsByUniqueAttributeWithResponseAsync(
                        typeName, atlasClassificationArray, requestOptions, context)
                .block();
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
    public Mono<Response<Void>> updateClassificationsByUniqueAttributeWithResponseAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context ->
                        service.updateClassificationsByUniqueAttribute(
                                this.client.getEndpoint(),
                                typeName,
                                atlasClassificationArray,
                                requestOptions,
                                context));
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
    public Mono<Response<Void>> updateClassificationsByUniqueAttributeWithResponseAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return service.updateClassificationsByUniqueAttribute(
                this.client.getEndpoint(), typeName, atlasClassificationArray, requestOptions, context);
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
    public Mono<Void> updateClassificationsByUniqueAttributeAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions) {
        return updateClassificationsByUniqueAttributeWithResponseAsync(
                        typeName, atlasClassificationArray, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
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
    public Mono<Void> updateClassificationsByUniqueAttributeAsync(
            String typeName, BinaryData atlasClassificationArray, RequestOptions requestOptions, Context context) {
        return updateClassificationsByUniqueAttributeWithResponseAsync(
                        typeName, atlasClassificationArray, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
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
        updateClassificationsByUniqueAttributeAsync(typeName, atlasClassificationArray, requestOptions).block();
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
        return updateClassificationsByUniqueAttributeWithResponseAsync(
                        typeName, atlasClassificationArray, requestOptions, context)
                .block();
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
    public Mono<Response<List<String>>> setClassificationsWithResponseAsync(
            BinaryData entityHeaders, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.setClassifications(
                                this.client.getEndpoint(), entityHeaders, accept, requestOptions, context));
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
    public Mono<Response<List<String>>> setClassificationsWithResponseAsync(
            BinaryData entityHeaders, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.setClassifications(this.client.getEndpoint(), entityHeaders, accept, requestOptions, context);
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
    public Mono<List<String>> setClassificationsAsync(BinaryData entityHeaders, RequestOptions requestOptions) {
        return setClassificationsWithResponseAsync(entityHeaders, requestOptions)
                .flatMap(
                        (Response<List<String>> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
    public Mono<List<String>> setClassificationsAsync(
            BinaryData entityHeaders, RequestOptions requestOptions, Context context) {
        return setClassificationsWithResponseAsync(entityHeaders, requestOptions, context)
                .flatMap(
                        (Response<List<String>> res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
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
    public List<String> setClassifications(BinaryData entityHeaders, RequestOptions requestOptions) {
        return setClassificationsAsync(entityHeaders, requestOptions).block();
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
    public Response<List<String>> setClassificationsWithResponse(
            BinaryData entityHeaders, RequestOptions requestOptions, Context context) {
        return setClassificationsWithResponseAsync(entityHeaders, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getEntitiesByUniqueAttributesWithResponseAsync(
            String typeName, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getEntitiesByUniqueAttributes(
                                this.client.getEndpoint(), typeName, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getEntitiesByUniqueAttributesWithResponseAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEntitiesByUniqueAttributes(
                this.client.getEndpoint(), typeName, accept, requestOptions, context);
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
    public Mono<BinaryData> getEntitiesByUniqueAttributesAsync(String typeName, RequestOptions requestOptions) {
        return getEntitiesByUniqueAttributesWithResponseAsync(typeName, requestOptions)
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
    public Mono<BinaryData> getEntitiesByUniqueAttributesAsync(
            String typeName, RequestOptions requestOptions, Context context) {
        return getEntitiesByUniqueAttributesWithResponseAsync(typeName, requestOptions, context)
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
        return getEntitiesByUniqueAttributesAsync(typeName, requestOptions).block();
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
        return getEntitiesByUniqueAttributesWithResponseAsync(typeName, requestOptions, context).block();
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
    public Mono<Response<BinaryData>> getHeaderWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getHeader(this.client.getEndpoint(), guid, accept, requestOptions, context));
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
    public Mono<Response<BinaryData>> getHeaderWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getHeader(this.client.getEndpoint(), guid, accept, requestOptions, context);
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
    public Mono<BinaryData> getHeaderAsync(String guid, RequestOptions requestOptions) {
        return getHeaderWithResponseAsync(guid, requestOptions)
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
    public Mono<BinaryData> getHeaderAsync(String guid, RequestOptions requestOptions, Context context) {
        return getHeaderWithResponseAsync(guid, requestOptions, context)
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
        return getHeaderAsync(guid, requestOptions).block();
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
        return getHeaderWithResponseAsync(guid, requestOptions, context).block();
    }
}
