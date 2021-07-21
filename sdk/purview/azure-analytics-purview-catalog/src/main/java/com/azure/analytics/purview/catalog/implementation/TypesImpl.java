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

/** An instance of this class provides access to all the operations defined in Types. */
public final class TypesImpl {
    /** The proxy service used to perform REST calls. */
    private final TypesService service;

    /** The service client containing this operation class. */
    private final PurviewCatalogServiceRestAPIDocumentImpl client;

    /**
     * Initializes an instance of TypesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    TypesImpl(PurviewCatalogServiceRestAPIDocumentImpl client) {
        this.service = RestProxy.create(TypesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for PurviewCatalogServiceRestAPIDocumentTypes to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{Endpoint}/api")
    @ServiceInterface(name = "PurviewCatalogServic")
    private interface TypesService {
        @Get("/atlas/v2/types/classificationdef/guid/{guid}")
        Mono<Response<BinaryData>> getClassificationDefByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/classificationdef/name/{name}")
        Mono<Response<BinaryData>> getClassificationDefByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/entitydef/guid/{guid}")
        Mono<Response<BinaryData>> getEntityDefinitionByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/entitydef/name/{name}")
        Mono<Response<BinaryData>> getEntityDefinitionByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/enumdef/guid/{guid}")
        Mono<Response<BinaryData>> getEnumDefByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/enumdef/name/{name}")
        Mono<Response<BinaryData>> getEnumDefByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/relationshipdef/guid/{guid}")
        Mono<Response<BinaryData>> getRelationshipDefByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/relationshipdef/name/{name}")
        Mono<Response<BinaryData>> getRelationshipDefByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/structdef/guid/{guid}")
        Mono<Response<BinaryData>> getStructDefByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/structdef/name/{name}")
        Mono<Response<BinaryData>> getStructDefByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/typedef/guid/{guid}")
        Mono<Response<BinaryData>> getTypeDefinitionByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/typedef/name/{name}")
        Mono<Response<BinaryData>> getTypeDefinitionByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/types/typedef/name/{name}")
        Mono<Response<Void>> deleteTypeByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/typedefs")
        Mono<Response<BinaryData>> getAllTypeDefinitions(
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Post("/atlas/v2/types/typedefs")
        Mono<Response<BinaryData>> createTypeDefinitions(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData typesDef,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Put("/atlas/v2/types/typedefs")
        Mono<Response<BinaryData>> updateAtlasTypeDefinitions(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData typesDef,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Delete("/atlas/v2/types/typedefs")
        Mono<Response<Void>> deleteTypeDefinitions(
                @HostParam("Endpoint") String endpoint,
                @BodyParam("application/json") BinaryData typesDef,
                RequestOptions requestOptions,
                Context context);

        @Get("/atlas/v2/types/typedefs/headers")
        Mono<Response<BinaryData>> listTypeDefinitionHeaders(
                @HostParam("Endpoint") String endpoint,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/types/termtemplatedef/guid/{guid}")
        Mono<Response<BinaryData>> getTermTemplateDefByGuid(
                @HostParam("Endpoint") String endpoint,
                @PathParam("guid") String guid,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);

        @Get("/types/termtemplatedef/name/{name}")
        Mono<Response<BinaryData>> getTermTemplateDefByName(
                @HostParam("Endpoint") String endpoint,
                @PathParam("name") String name,
                @QueryParam("api-version") String apiVersion,
                @HeaderParam("Accept") String accept,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getClassificationDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getClassificationDefByGuid(
                                this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getClassificationDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getClassificationDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getClassificationDefByGuidAsync(String guid, RequestOptions requestOptions) {
        return getClassificationDefByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getClassificationDefByGuidAsync(
            String guid, RequestOptions requestOptions, Context context) {
        return getClassificationDefByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getClassificationDefByGuid(String guid, RequestOptions requestOptions) {
        return getClassificationDefByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the classification definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getClassificationDefByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getClassificationDefByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getClassificationDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getClassificationDefByName(
                                this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getClassificationDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getClassificationDefByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getClassificationDefByNameAsync(String name, RequestOptions requestOptions) {
        return getClassificationDefByNameWithResponseAsync(name, requestOptions)
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
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getClassificationDefByNameAsync(
            String name, RequestOptions requestOptions, Context context) {
        return getClassificationDefByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getClassificationDefByName(String name, RequestOptions requestOptions) {
        return getClassificationDefByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the classification definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the classification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getClassificationDefByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getClassificationDefByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntityDefinitionByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getEntityDefinitionByGuid(
                                this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntityDefinitionByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEntityDefinitionByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntityDefinitionByGuidAsync(String guid, RequestOptions requestOptions) {
        return getEntityDefinitionByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntityDefinitionByGuidAsync(
            String guid, RequestOptions requestOptions, Context context) {
        return getEntityDefinitionByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEntityDefinitionByGuid(String guid, RequestOptions requestOptions) {
        return getEntityDefinitionByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the Entity definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEntityDefinitionByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getEntityDefinitionByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntityDefinitionByNameWithResponseAsync(
            String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getEntityDefinitionByName(
                                this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEntityDefinitionByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEntityDefinitionByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntityDefinitionByNameAsync(String name, RequestOptions requestOptions) {
        return getEntityDefinitionByNameWithResponseAsync(name, requestOptions)
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
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEntityDefinitionByNameAsync(
            String name, RequestOptions requestOptions, Context context) {
        return getEntityDefinitionByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEntityDefinitionByName(String name, RequestOptions requestOptions) {
        return getEntityDefinitionByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the entity definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the entity.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEntityDefinitionByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getEntityDefinitionByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEnumDefByGuidWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getEnumDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEnumDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEnumDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEnumDefByGuidAsync(String guid, RequestOptions requestOptions) {
        return getEnumDefByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEnumDefByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getEnumDefByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEnumDefByGuid(String guid, RequestOptions requestOptions) {
        return getEnumDefByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the enum definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEnumDefByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getEnumDefByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEnumDefByNameWithResponseAsync(String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getEnumDefByName(this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEnumDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getEnumDefByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEnumDefByNameAsync(String name, RequestOptions requestOptions) {
        return getEnumDefByNameWithResponseAsync(name, requestOptions)
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
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getEnumDefByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return getEnumDefByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getEnumDefByName(String name, RequestOptions requestOptions) {
        return getEnumDefByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the enum definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the enum.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEnumDefByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getEnumDefByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getRelationshipDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getRelationshipDefByGuid(
                                this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getRelationshipDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getRelationshipDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getRelationshipDefByGuidAsync(String guid, RequestOptions requestOptions) {
        return getRelationshipDefByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getRelationshipDefByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getRelationshipDefByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getRelationshipDefByGuid(String guid, RequestOptions requestOptions) {
        return getRelationshipDefByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the relationship definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getRelationshipDefByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getRelationshipDefByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getRelationshipDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getRelationshipDefByName(
                                this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getRelationshipDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getRelationshipDefByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getRelationshipDefByNameAsync(String name, RequestOptions requestOptions) {
        return getRelationshipDefByNameWithResponseAsync(name, requestOptions)
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
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getRelationshipDefByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return getRelationshipDefByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getRelationshipDefByName(String name, RequestOptions requestOptions) {
        return getRelationshipDefByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the relationship definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     * }
     * }</pre>
     *
     * @param name The name of the relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getRelationshipDefByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getRelationshipDefByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getStructDefByGuidWithResponseAsync(String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getStructDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getStructDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getStructDefByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getStructDefByGuidAsync(String guid, RequestOptions requestOptions) {
        return getStructDefByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getStructDefByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getStructDefByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getStructDefByGuid(String guid, RequestOptions requestOptions) {
        return getStructDefByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the struct definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getStructDefByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getStructDefByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getStructDefByNameWithResponseAsync(String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getStructDefByName(this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getStructDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getStructDefByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getStructDefByNameAsync(String name, RequestOptions requestOptions) {
        return getStructDefByNameWithResponseAsync(name, requestOptions)
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
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getStructDefByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return getStructDefByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getStructDefByName(String name, RequestOptions requestOptions) {
        return getStructDefByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the struct definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the struct.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getStructDefByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getStructDefByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTypeDefinitionByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getTypeDefinitionByGuid(
                                this.client.getEndpoint(), guid, accept, requestOptions, context));
    }

    /**
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTypeDefinitionByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getTypeDefinitionByGuid(this.client.getEndpoint(), guid, accept, requestOptions, context);
    }

    /**
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTypeDefinitionByGuidAsync(String guid, RequestOptions requestOptions) {
        return getTypeDefinitionByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTypeDefinitionByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getTypeDefinitionByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getTypeDefinitionByGuid(String guid, RequestOptions requestOptions) {
        return getTypeDefinitionByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the type definition for the given GUID.
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getTypeDefinitionByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getTypeDefinitionByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTypeDefinitionByNameWithResponseAsync(
            String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getTypeDefinitionByName(
                                this.client.getEndpoint(), name, accept, requestOptions, context));
    }

    /**
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTypeDefinitionByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getTypeDefinitionByName(this.client.getEndpoint(), name, accept, requestOptions, context);
    }

    /**
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTypeDefinitionByNameAsync(String name, RequestOptions requestOptions) {
        return getTypeDefinitionByNameWithResponseAsync(name, requestOptions)
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
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTypeDefinitionByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return getTypeDefinitionByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getTypeDefinitionByName(String name, RequestOptions requestOptions) {
        return getTypeDefinitionByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the type definition by its name (unique).
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     entityTypes: [
     *         String
     *     ]
     *     subTypes: [
     *         String
     *     ]
     *     superTypes: [
     *         String
     *     ]
     *     relationshipAttributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *             isLegacyAttribute: Boolean
     *             relationshipTypeName: String
     *         }
     *     ]
     *     defaultValue: String
     *     elementDefs: [
     *         {
     *             description: String
     *             ordinal: Float
     *             value: String
     *         }
     *     ]
     *     endDef1: {
     *         cardinality: String(SINGLE/LIST/SET)
     *         description: String
     *         isContainer: Boolean
     *         isLegacyAttribute: Boolean
     *         name: String
     *         type: String
     *     }
     *     endDef2: (recursive schema, see endDef2 above)
     *     relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *     relationshipLabel: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 (recursive schema, see above)
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getTypeDefinitionByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getTypeDefinitionByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTypeByNameWithResponseAsync(String name, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context -> service.deleteTypeByName(this.client.getEndpoint(), name, requestOptions, context));
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTypeByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        return service.deleteTypeByName(this.client.getEndpoint(), name, requestOptions, context);
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTypeByNameAsync(String name, RequestOptions requestOptions) {
        return deleteTypeByNameWithResponseAsync(name, requestOptions).flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTypeByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return deleteTypeByNameWithResponseAsync(name, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTypeByName(String name, RequestOptions requestOptions) {
        deleteTypeByNameAsync(name, requestOptions).block();
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTypeByNameWithResponse(String name, RequestOptions requestOptions, Context context) {
        return deleteTypeByNameWithResponseAsync(name, requestOptions, context).block();
    }

    /**
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAllTypeDefinitionsWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context -> service.getAllTypeDefinitions(this.client.getEndpoint(), accept, requestOptions, context));
    }

    /**
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAllTypeDefinitionsWithResponseAsync(
            RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getAllTypeDefinitions(this.client.getEndpoint(), accept, requestOptions, context);
    }

    /**
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAllTypeDefinitionsAsync(RequestOptions requestOptions) {
        return getAllTypeDefinitionsWithResponseAsync(requestOptions)
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
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getAllTypeDefinitionsAsync(RequestOptions requestOptions, Context context) {
        return getAllTypeDefinitionsWithResponseAsync(requestOptions, context)
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
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getAllTypeDefinitions(RequestOptions requestOptions) {
        return getAllTypeDefinitionsAsync(requestOptions).block();
    }

    /**
     * Get all type definitions in Atlas in bulk.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getAllTypeDefinitionsWithResponse(RequestOptions requestOptions, Context context) {
        return getAllTypeDefinitionsWithResponseAsync(requestOptions, context).block();
    }

    /**
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<Response<BinaryData>> createTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.createTypeDefinitions(
                                this.client.getEndpoint(), typesDef, accept, requestOptions, context));
    }

    /**
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<Response<BinaryData>> createTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.createTypeDefinitions(this.client.getEndpoint(), typesDef, accept, requestOptions, context);
    }

    /**
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<BinaryData> createTypeDefinitionsAsync(BinaryData typesDef, RequestOptions requestOptions) {
        return createTypeDefinitionsWithResponseAsync(typesDef, requestOptions)
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
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<BinaryData> createTypeDefinitionsAsync(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return createTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context)
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
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public BinaryData createTypeDefinitions(BinaryData typesDef, RequestOptions requestOptions) {
        return createTypeDefinitionsAsync(typesDef, requestOptions).block();
    }

    /**
     * Create all atlas type definitions in bulk, only new definitions will be created. Any changes to the existing
     * definitions will be discarded.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Response<BinaryData> createTypeDefinitionsWithResponse(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return createTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context).block();
    }

    /**
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<Response<BinaryData>> updateAtlasTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.updateAtlasTypeDefinitions(
                                this.client.getEndpoint(), typesDef, accept, requestOptions, context));
    }

    /**
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<Response<BinaryData>> updateAtlasTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.updateAtlasTypeDefinitions(this.client.getEndpoint(), typesDef, accept, requestOptions, context);
    }

    /**
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<BinaryData> updateAtlasTypeDefinitionsAsync(BinaryData typesDef, RequestOptions requestOptions) {
        return updateAtlasTypeDefinitionsWithResponseAsync(typesDef, requestOptions)
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
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Mono<BinaryData> updateAtlasTypeDefinitionsAsync(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return updateAtlasTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context)
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
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public BinaryData updateAtlasTypeDefinitions(BinaryData typesDef, RequestOptions requestOptions) {
        return updateAtlasTypeDefinitionsAsync(typesDef, requestOptions).block();
    }

    /**
     * Update all types in bulk, changes detected in the type definitions would be persisted.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
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
    public Response<BinaryData> updateAtlasTypeDefinitionsWithResponse(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return updateAtlasTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context).block();
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions) {
        return FluxUtil.withContext(
                context -> service.deleteTypeDefinitions(this.client.getEndpoint(), typesDef, requestOptions, context));
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTypeDefinitionsWithResponseAsync(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return service.deleteTypeDefinitions(this.client.getEndpoint(), typesDef, requestOptions, context);
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTypeDefinitionsAsync(BinaryData typesDef, RequestOptions requestOptions) {
        return deleteTypeDefinitionsWithResponseAsync(typesDef, requestOptions)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTypeDefinitionsAsync(BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return deleteTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTypeDefinitions(BinaryData typesDef, RequestOptions requestOptions) {
        deleteTypeDefinitionsAsync(typesDef, requestOptions).block();
    }

    /**
     * Delete API for all types in bulk.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     classificationDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: {
     *                 availableLocales: [
     *                     String
     *                 ]
     *                 calendar: Float
     *                 dateInstance: (recursive schema, see dateInstance above)
     *                 dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *                 instance: (recursive schema, see instance above)
     *                 lenient: Boolean
     *                 numberFormat: {
     *                     availableLocales: [
     *                         String
     *                     ]
     *                     currency: String
     *                     currencyInstance: (recursive schema, see currencyInstance above)
     *                     groupingUsed: Boolean
     *                     instance: (recursive schema, see instance above)
     *                     integerInstance: (recursive schema, see integerInstance above)
     *                     maximumFractionDigits: Integer
     *                     maximumIntegerDigits: Integer
     *                     minimumFractionDigits: Integer
     *                     minimumIntegerDigits: Integer
     *                     numberInstance: (recursive schema, see numberInstance above)
     *                     parseIntegerOnly: Boolean
     *                     percentInstance: (recursive schema, see percentInstance above)
     *                     roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *                 }
     *                 timeInstance: (recursive schema, see timeInstance above)
     *                 timeZone: {
     *                     dstSavings: Integer
     *                     id: String
     *                     availableIds: [
     *                         String
     *                     ]
     *                     defaultProperty: (recursive schema, see defaultProperty above)
     *                     displayName: String
     *                     rawOffset: Integer
     *                 }
     *             }
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         {
     *                             params: {
     *                                 String: Object
     *                             }
     *                             type: String
     *                         }
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                 }
     *             ]
     *             entityTypes: [
     *                 String
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *         }
     *     ]
     *     entityDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             subTypes: [
     *                 String
     *             ]
     *             superTypes: [
     *                 String
     *             ]
     *             relationshipAttributeDefs: [
     *                 {
     *                     cardinality: String(SINGLE/LIST/SET)
     *                     constraints: [
     *                         (recursive schema, see above)
     *                     ]
     *                     defaultValue: String
     *                     description: String
     *                     includeInNotification: Boolean
     *                     isIndexable: Boolean
     *                     isOptional: Boolean
     *                     isUnique: Boolean
     *                     name: String
     *                     options: {
     *                         String: String
     *                     }
     *                     typeName: String
     *                     valuesMaxCount: Integer
     *                     valuesMinCount: Integer
     *                     isLegacyAttribute: Boolean
     *                     relationshipTypeName: String
     *                 }
     *             ]
     *         }
     *     ]
     *     enumDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             defaultValue: String
     *             elementDefs: [
     *                 {
     *                     description: String
     *                     ordinal: Float
     *                     value: String
     *                 }
     *             ]
     *         }
     *     ]
     *     relationshipDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *             endDef1: {
     *                 cardinality: String(SINGLE/LIST/SET)
     *                 description: String
     *                 isContainer: Boolean
     *                 isLegacyAttribute: Boolean
     *                 name: String
     *                 type: String
     *             }
     *             endDef2: (recursive schema, see endDef2 above)
     *             relationshipCategory: String(ASSOCIATION/AGGREGATION/COMPOSITION)
     *             relationshipLabel: String
     *         }
     *     ]
     *     structDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     *     termTemplateDefs: [
     *         {
     *             category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *             createTime: Float
     *             createdBy: String
     *             dateFormatter: (recursive schema, see dateFormatter above)
     *             description: String
     *             guid: String
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             serviceType: String
     *             typeVersion: String
     *             updateTime: Float
     *             updatedBy: String
     *             version: Float
     *             lastModifiedTS: String
     *             attributeDefs: [
     *                 (recursive schema, see above)
     *             ]
     *         }
     *     ]
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTypeDefinitionsWithResponse(
            BinaryData typesDef, RequestOptions requestOptions, Context context) {
        return deleteTypeDefinitionsWithResponseAsync(typesDef, requestOptions, context).block();
    }

    /**
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listTypeDefinitionHeadersWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.listTypeDefinitionHeaders(this.client.getEndpoint(), accept, requestOptions, context));
    }

    /**
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listTypeDefinitionHeadersWithResponseAsync(
            RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.listTypeDefinitionHeaders(this.client.getEndpoint(), accept, requestOptions, context);
    }

    /**
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listTypeDefinitionHeadersAsync(RequestOptions requestOptions) {
        return listTypeDefinitionHeadersWithResponseAsync(requestOptions)
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
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> listTypeDefinitionHeadersAsync(RequestOptions requestOptions, Context context) {
        return listTypeDefinitionHeadersWithResponseAsync(requestOptions, context)
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
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData listTypeDefinitionHeaders(RequestOptions requestOptions) {
        return listTypeDefinitionHeadersAsync(requestOptions).block();
    }

    /**
     * List all type definitions returned as a list of minimal information header.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>includeTermTemplate</td><td>String</td><td>No</td><td>Whether include termtemplatedef when return all typedefs.
     * This is always true when search filter type=term_template</td></tr>
     *     <tr><td>type</td><td>String</td><td>No</td><td>Typedef name as search filter when get typedefs.</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     {
     *         category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *         guid: String
     *         name: String
     *     }
     * ]
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listTypeDefinitionHeadersWithResponse(RequestOptions requestOptions, Context context) {
        return listTypeDefinitionHeadersWithResponseAsync(requestOptions, context).block();
    }

    /**
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTermTemplateDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getTermTemplateDefByGuid(
                                this.client.getEndpoint(),
                                guid,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTermTemplateDefByGuidWithResponseAsync(
            String guid, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getTermTemplateDefByGuid(
                this.client.getEndpoint(), guid, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTermTemplateDefByGuidAsync(String guid, RequestOptions requestOptions) {
        return getTermTemplateDefByGuidWithResponseAsync(guid, requestOptions)
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
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTermTemplateDefByGuidAsync(String guid, RequestOptions requestOptions, Context context) {
        return getTermTemplateDefByGuidWithResponseAsync(guid, requestOptions, context)
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
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getTermTemplateDefByGuid(String guid, RequestOptions requestOptions) {
        return getTermTemplateDefByGuidAsync(guid, requestOptions).block();
    }

    /**
     * Get the term template definition for the given GUID.
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param guid The globally unique identifier of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getTermTemplateDefByGuidWithResponse(
            String guid, RequestOptions requestOptions, Context context) {
        return getTermTemplateDefByGuidWithResponseAsync(guid, requestOptions, context).block();
    }

    /**
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTermTemplateDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getTermTemplateDefByName(
                                this.client.getEndpoint(),
                                name,
                                this.client.getApiVersion(),
                                accept,
                                requestOptions,
                                context));
    }

    /**
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getTermTemplateDefByNameWithResponseAsync(
            String name, RequestOptions requestOptions, Context context) {
        final String accept = "application/json";
        return service.getTermTemplateDefByName(
                this.client.getEndpoint(), name, this.client.getApiVersion(), accept, requestOptions, context);
    }

    /**
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTermTemplateDefByNameAsync(String name, RequestOptions requestOptions) {
        return getTermTemplateDefByNameWithResponseAsync(name, requestOptions)
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
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> getTermTemplateDefByNameAsync(String name, RequestOptions requestOptions, Context context) {
        return getTermTemplateDefByNameWithResponseAsync(name, requestOptions, context)
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
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getTermTemplateDefByName(String name, RequestOptions requestOptions) {
        return getTermTemplateDefByNameAsync(name, requestOptions).block();
    }

    /**
     * Get the term template definition by its name (unique).
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
     *     category: String(PRIMITIVE/OBJECT_ID_TYPE/ENUM/STRUCT/CLASSIFICATION/ENTITY/ARRAY/MAP/RELATIONSHIP/TERM_TEMPLATE)
     *     createTime: Float
     *     createdBy: String
     *     dateFormatter: {
     *         availableLocales: [
     *             String
     *         ]
     *         calendar: Float
     *         dateInstance: (recursive schema, see dateInstance above)
     *         dateTimeInstance: (recursive schema, see dateTimeInstance above)
     *         instance: (recursive schema, see instance above)
     *         lenient: Boolean
     *         numberFormat: {
     *             availableLocales: [
     *                 String
     *             ]
     *             currency: String
     *             currencyInstance: (recursive schema, see currencyInstance above)
     *             groupingUsed: Boolean
     *             instance: (recursive schema, see instance above)
     *             integerInstance: (recursive schema, see integerInstance above)
     *             maximumFractionDigits: Integer
     *             maximumIntegerDigits: Integer
     *             minimumFractionDigits: Integer
     *             minimumIntegerDigits: Integer
     *             numberInstance: (recursive schema, see numberInstance above)
     *             parseIntegerOnly: Boolean
     *             percentInstance: (recursive schema, see percentInstance above)
     *             roundingMode: String(UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN/UNNECESSARY)
     *         }
     *         timeInstance: (recursive schema, see timeInstance above)
     *         timeZone: {
     *             dstSavings: Integer
     *             id: String
     *             availableIds: [
     *                 String
     *             ]
     *             defaultProperty: (recursive schema, see defaultProperty above)
     *             displayName: String
     *             rawOffset: Integer
     *         }
     *     }
     *     description: String
     *     guid: String
     *     name: String
     *     options: {
     *         String: String
     *     }
     *     serviceType: String
     *     typeVersion: String
     *     updateTime: Float
     *     updatedBy: String
     *     version: Float
     *     lastModifiedTS: String
     *     attributeDefs: [
     *         {
     *             cardinality: String(SINGLE/LIST/SET)
     *             constraints: [
     *                 {
     *                     params: {
     *                         String: Object
     *                     }
     *                     type: String
     *                 }
     *             ]
     *             defaultValue: String
     *             description: String
     *             includeInNotification: Boolean
     *             isIndexable: Boolean
     *             isOptional: Boolean
     *             isUnique: Boolean
     *             name: String
     *             options: {
     *                 String: String
     *             }
     *             typeName: String
     *             valuesMaxCount: Integer
     *             valuesMinCount: Integer
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param name The name of the term template.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getTermTemplateDefByNameWithResponse(
            String name, RequestOptions requestOptions, Context context) {
        return getTermTemplateDefByNameWithResponseAsync(name, requestOptions, context).block();
    }
}
