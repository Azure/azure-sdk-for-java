package com.azure.analytics.purview.catalog;

import com.azure.analytics.purview.catalog.implementation.TypesImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous PurviewCatalogServiceRestAPIDocument type. */
@ServiceClient(builder = PurviewCatalogServiceRestAPIDocumentBuilder.class)
public final class TypesClient {
    private final TypesImpl serviceClient;

    /**
     * Initializes an instance of Types client.
     *
     * @param serviceClient the service client implementation.
     */
    TypesClient(TypesImpl serviceClient) {
        this.serviceClient = serviceClient;
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
        return this.serviceClient.getClassificationDefByGuid(guid, requestOptions);
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
        return this.serviceClient.getClassificationDefByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getClassificationDefByName(name, requestOptions);
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
        return this.serviceClient.getClassificationDefByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getEntityDefinitionByGuid(guid, requestOptions);
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
        return this.serviceClient.getEntityDefinitionByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getEntityDefinitionByName(name, requestOptions);
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
        return this.serviceClient.getEntityDefinitionByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getEnumDefByGuid(guid, requestOptions);
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
        return this.serviceClient.getEnumDefByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getEnumDefByName(name, requestOptions);
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
        return this.serviceClient.getEnumDefByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getRelationshipDefByGuid(guid, requestOptions);
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
        return this.serviceClient.getRelationshipDefByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getRelationshipDefByName(name, requestOptions);
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
        return this.serviceClient.getRelationshipDefByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getStructDefByGuid(guid, requestOptions);
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
        return this.serviceClient.getStructDefByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getStructDefByName(name, requestOptions);
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
        return this.serviceClient.getStructDefByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getTypeDefinitionByGuid(guid, requestOptions);
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
        return this.serviceClient.getTypeDefinitionByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getTypeDefinitionByName(name, requestOptions);
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
        return this.serviceClient.getTypeDefinitionByNameWithResponse(name, requestOptions, context);
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTypeByName(String name, RequestOptions requestOptions) {
        this.serviceClient.deleteTypeByName(name, requestOptions);
    }

    /**
     * Delete API for type identified by its name.
     *
     * @param name The name of the type.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTypeByNameWithResponse(String name, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteTypeByNameWithResponse(name, requestOptions, context);
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
        return this.serviceClient.getAllTypeDefinitions(requestOptions);
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
        return this.serviceClient.getAllTypeDefinitionsWithResponse(requestOptions, context);
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
        return this.serviceClient.createTypeDefinitions(typesDef, requestOptions);
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
        return this.serviceClient.createTypeDefinitionsWithResponse(typesDef, requestOptions, context);
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
        return this.serviceClient.updateAtlasTypeDefinitions(typesDef, requestOptions);
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
        return this.serviceClient.updateAtlasTypeDefinitionsWithResponse(typesDef, requestOptions, context);
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
        this.serviceClient.deleteTypeDefinitions(typesDef, requestOptions);
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
        return this.serviceClient.deleteTypeDefinitionsWithResponse(typesDef, requestOptions, context);
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
        return this.serviceClient.listTypeDefinitionHeaders(requestOptions);
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
        return this.serviceClient.listTypeDefinitionHeadersWithResponse(requestOptions, context);
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
        return this.serviceClient.getTermTemplateDefByGuid(guid, requestOptions);
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
        return this.serviceClient.getTermTemplateDefByGuidWithResponse(guid, requestOptions, context);
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
        return this.serviceClient.getTermTemplateDefByName(name, requestOptions);
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
        return this.serviceClient.getTermTemplateDefByNameWithResponse(name, requestOptions, context);
    }
}
