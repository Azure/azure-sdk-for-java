// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.ConsistencyPolicy;
import com.azure.cosmos.implementation.CosmosResourceType;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Index;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.ReplicationPolicy;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.implementation.User;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.Warning;
import com.azure.cosmos.implementation.directconnectivity.Address;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfoInternal;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.model package
 **/
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public final class ModelBridgeInternal {

    private ModelBridgeInternal() {}

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosConflictResponse createCosmosConflictResponse(ResourceResponse<Conflict> response) {
        return new CosmosConflictResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosContainerResponse createCosmosContainerResponse(ResourceResponse<DocumentCollection> response) {
        return new CosmosContainerResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosDatabaseResponse createCosmosDatabaseResponse(ResourceResponse<Database> response) {
        return new CosmosDatabaseResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> CosmosItemResponse<T> createCosmosAsyncItemResponse(ResourceResponse<Document> response, Class<T> classType, ItemDeserializer itemDeserializer) {
        return new CosmosItemResponse<>(response, classType, itemDeserializer);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosItemResponse<Object> createCosmosAsyncItemResponseWithObjectType(ResourceResponse<Document> response) {
        return new CosmosItemResponse<>(response, Object.class, null);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosPermissionResponse createCosmosPermissionResponse(ResourceResponse<Permission> response) {
        return new CosmosPermissionResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosStoredProcedureResponse createCosmosStoredProcedureResponse(ResourceResponse<StoredProcedure> response) {
        return new CosmosStoredProcedureResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosStoredProcedureResponse createCosmosStoredProcedureResponse(StoredProcedureResponse response) {
        return new CosmosStoredProcedureResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosStoredProcedureProperties createCosmosStoredProcedureProperties(String jsonString) {
        return new CosmosStoredProcedureProperties(jsonString);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosPermissionProperties createCosmosPermissionProperties(String jsonString) {
        return new CosmosPermissionProperties(jsonString);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosTriggerResponse createCosmosTriggerResponse(ResourceResponse<Trigger> response) {
        return new CosmosTriggerResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosUserDefinedFunctionResponse createCosmosUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response) {
        return new CosmosUserDefinedFunctionResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosUserResponse createCosmosUserResponse(ResourceResponse<User> response) {
        return new CosmosUserResponse(response);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosConflictProperties> getCosmosConflictPropertiesFromV2Results(List<Conflict> results) {
        return CosmosConflictProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static DocumentCollection getV2Collection(CosmosContainerProperties containerProperties) {
        return containerProperties.getV2Collection();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosContainerProperties> getCosmosContainerPropertiesFromV2Results(List<DocumentCollection> results) {
        return CosmosContainerProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosDatabaseProperties> getCosmosDatabasePropertiesFromV2Results(List<Database> results) {
        return CosmosDatabaseProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> InternalObjectNode getInternalObjectNode(CosmosItemResponse<T> cosmosItemResponse) {
        return cosmosItemResponse.getProperties();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Permission getPermission(CosmosPermissionProperties permissionProperties, String databaseName) {
        return permissionProperties.getPermission(databaseName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosPermissionProperties> getCosmosPermissionPropertiesFromResults(List<Permission> results) {
        return CosmosPermissionProperties.getPermissions(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosStoredProcedureProperties> getCosmosStoredProcedurePropertiesFromV2Results(List<StoredProcedure> results) {
        return CosmosStoredProcedureProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosTriggerProperties> getCosmosTriggerPropertiesFromV2Results(List<Trigger> results) {
        return CosmosTriggerProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosUserDefinedFunctionProperties> getCosmosUserDefinedFunctionPropertiesFromV2Results(List<UserDefinedFunction> results) {
        return CosmosUserDefinedFunctionProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static User getV2User(CosmosUserProperties cosmosUserProperties) {
        return cosmosUserProperties.getV2User();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static List<CosmosUserProperties> getCosmosUserPropertiesFromV2Results(List<User> results) {
        return CosmosUserProperties.getFromV2Results(results);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosConflictRequestOptions cosmosConflictRequestOptions) {
        return cosmosConflictRequestOptions.toRequestOptions();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosContainerRequestOptions cosmosContainerRequestOptions) {
        return cosmosContainerRequestOptions.toRequestOptions();
    }

//    @Warning(value = INTERNAL_USE_ONLY_WARNING)
//    public static CosmosContainerRequestOptions setOfferThroughput(CosmosContainerRequestOptions cosmosContainerRequestOptions,
//                                                                   Integer offerThroughput) {
//        return cosmosContainerRequestOptions.setOfferThroughput(offerThroughput);
//    }
//
//    @Warning(value = INTERNAL_USE_ONLY_WARNING)
//    public static CosmosContainerRequestOptions setThroughputProperties(CosmosContainerRequestOptions cosmosContainerRequestOptions,
//                                                                   ThroughputProperties throughputProperties) {
//        return cosmosContainerRequestOptions.setThroughputProperties(throughputProperties);
//    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions) {
        return cosmosDatabaseRequestOptions.toRequestOptions();
    }

//    @Warning(value = INTERNAL_USE_ONLY_WARNING)
//    public static CosmosDatabaseRequestOptions setOfferThroughput(CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions,
//                                                                   Integer offerThroughput) {
//        return cosmosDatabaseRequestOptions.setOfferThroughput(offerThroughput);
//    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosDatabaseRequestOptions setThroughputProperties(
        CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions,
        ThroughputProperties throughputProperties) {
        return cosmosDatabaseRequestOptions.setThroughputProperties(throughputProperties);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosContainerRequestOptions setThroughputProperties(
        CosmosContainerRequestOptions containerRequestOptions,
        ThroughputProperties throughputProperties) {
        return containerRequestOptions.setThroughputProperties(throughputProperties);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Offer updateOfferFromProperties(Offer offer, ThroughputProperties properties) {
        return properties.updateOfferFromProperties(offer);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosItemRequestOptions setPartitionKey(CosmosItemRequestOptions cosmosItemRequestOptions,
                                                           PartitionKey partitionKey) {
        return cosmosItemRequestOptions.setPartitionKey(partitionKey);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosItemRequestOptions cosmosItemRequestOptions) {
        return cosmosItemRequestOptions.toRequestOptions();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosItemRequestOptions createCosmosItemRequestOptions(PartitionKey partitionKey) {
        return new CosmosItemRequestOptions(partitionKey);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosPermissionRequestOptions cosmosPermissionRequestOptions) {
        return cosmosPermissionRequestOptions.toRequestOptions();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static RequestOptions toRequestOptions(CosmosStoredProcedureRequestOptions cosmosStoredProcedureRequestOptions) {
        return cosmosStoredProcedureRequestOptions.toRequestOptions();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static DatabaseAccount toDatabaseAccount(RxDocumentServiceResponse response) {
        DatabaseAccount account = response.getResource(DatabaseAccount.class);

        // read the headers and set to the account
        Map<String, String> responseHeader = response.getResponseHeaders();

        account.setMaxMediaStorageUsageInMB(
            Long.parseLong(responseHeader.get(HttpConstants.HttpHeaders.MAX_MEDIA_STORAGE_USAGE_IN_MB)));
        account.setMediaStorageUsageInMB(
            Long.parseLong(responseHeader.get(HttpConstants.HttpHeaders.CURRENT_MEDIA_STORAGE_USAGE_IN_MB)));

        return account;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @param options the query request options
     * @return the partitionKeyRangeId.
     */
    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String partitionKeyRangeIdInternal(CosmosQueryRequestOptions options) {
        return options.getPartitionKeyRangeIdInternal();
    }

    /**
     * Sets the PartitionKeyRangeId.
     *
     * @param options the query request options
     * @param partitionKeyRangeId the partition key range id
     * @return the partitionKeyRangeId.
     */
    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosQueryRequestOptions partitionKeyRangeIdInternal(CosmosQueryRequestOptions options, String partitionKeyRangeId) {
        return options.setPartitionKeyRangeIdInternal(partitionKeyRangeId);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T extends Resource> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
                                                                          Class<T> cls) {
        return new FeedResponse<T>(response.getQueryResponse(cls), response.getResponseHeaders());
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> FeedResponse<T> toFeedResponsePage(List<T> results, Map<String, String> headers, boolean noChanges) {
        return new FeedResponse<>(results, headers, noChanges);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T extends Resource> FeedResponse<T> toChaneFeedResponsePage(RxDocumentServiceResponse response,
                                                                               Class<T> cls) {
        return new FeedResponse<T>(noChanges(response) ? Collections.emptyList() : response.getQueryResponse(cls),
            response.getResponseHeaders(), noChanges(response));
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T extends Resource> boolean noChanges(FeedResponse<T> page) {
        return page.nochanges;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T extends Resource> boolean noChanges(RxDocumentServiceResponse rsp) {
        return rsp.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> FeedResponse<T> createFeedResponse(List<T> results,
                                                         Map<String, String> headers) {
        return new FeedResponse<>(results, headers);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results,
                                                                         Map<String,
                                                                         String> headers,
                                                                         ConcurrentMap<String, QueryMetrics> queryMetricsMap,
                                                                         QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext) {
        FeedResponse<T> feedResponse = new FeedResponse<>(results, headers, queryMetricsMap);
        feedResponse.setQueryPlanDiagnosticsContext(diagnosticsContext);
        return feedResponse;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> ConcurrentMap<String, QueryMetrics> queryMetricsMap(FeedResponse<T> feedResponse) {
        return feedResponse.queryMetricsMap();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> ConcurrentMap<String, QueryMetrics> queryMetrics(FeedResponse<T> feedResponse) {
        return feedResponse.queryMetrics();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext(FeedResponse<T> feedResponse) {
        return feedResponse.getQueryPlanDiagnosticsContext();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String toLower(RequestVerb verb) {
        return verb.toLowerCase();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.getVersion() != null && PartitionKeyDefinitionVersion.V2.val == pkd.getVersion().val;
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static PartitionKeyInternal getNonePartitionKey(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getNonePartitionKeyValue();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static PartitionKeyInternal getPartitionKeyInternal(PartitionKey partitionKey) {
        return partitionKey.getInternalPartitionKey();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static PartitionKey partitionKeyfromJsonString(String jsonString) {
        return PartitionKey.fromJsonString(jsonString);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Object getPartitionKeyObject(PartitionKey right) {
        return right.getKeyObject();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getAltLink(Resource resource) {
        return resource.getAltLink();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setAltLink(Resource resource, String altLink) {
        resource.setAltLink(altLink);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setResourceId(Resource resource, String resourceId) {
        resource.setResourceId(resourceId);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setResourceSelfLink(Resource resource, String selfLink) {
        resource.setSelfLink(selfLink);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setTimestamp(Resource resource, Instant date) {
        resource.setTimestamp(date);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> void setProperty(JsonSerializable jsonSerializable, String propertyName, T value) {
        jsonSerializable.set(propertyName, value);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ObjectNode getObjectNodeFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getObject(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void removeFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        jsonSerializable.remove(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Object getValue(JsonNode value) {
        return JsonSerializable.getValue(value);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static JsonSerializable instantiateJsonSerializable(ObjectNode objectNode, Class<?> klassType) {
        try {
            // the hot path should come through here to avoid serialization/deserialization
            if (klassType.equals(Document.class) || klassType.equals(OrderByRowResult.class) || klassType.equals(InternalObjectNode.class)
                || klassType.equals(PartitionKeyRange.class) || klassType.equals(Range.class)
                || klassType.equals(QueryInfo.class) || klassType.equals(PartitionedQueryExecutionInfoInternal.class)
                || klassType.equals(QueryItem.class)
                || klassType.equals(Address.class)
                || klassType.equals(DatabaseAccount.class) || klassType.equals(DatabaseAccountLocation.class)
                || klassType.equals(ReplicationPolicy.class) || klassType.equals(ConsistencyPolicy.class)
                || klassType.equals(DocumentCollection.class) || klassType.equals(Database.class)) {
                return (JsonSerializable) klassType.getDeclaredConstructor(ObjectNode.class).newInstance(objectNode);
            } else {
                return (JsonSerializable) klassType.getDeclaredConstructor(String.class).newInstance(Utils.toJson(Utils.getSimpleObjectMapper(), objectNode));
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Map<String, Object> getMapFromJsonSerializable(JsonSerializable jsonSerializable) {
        return jsonSerializable.getMap();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosResourceType fromServiceSerializedFormat(String cosmosResourceType) {
        return CosmosResourceType.fromServiceSerializedFormat(cosmosResourceType);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Boolean getBooleanFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getBoolean(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Double getDoubleFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getDouble(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Object getObjectByPathFromJsonSerializable(JsonSerializable jsonSerializable, List<String> propertyNames) {
        return jsonSerializable.getObjectByPath(propertyNames);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ByteBuffer serializeJsonToByteBuffer(JsonSerializable jsonSerializable) {
        return jsonSerializable.serializeJsonToByteBuffer();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> T toObjectFromJsonSerializable(JsonSerializable jsonSerializable, Class<T> c) {
        return jsonSerializable.toObject(c);
    }

    public static ByteBuffer serializeJsonToByteBuffer(JsonSerializable jsonSerializable, ObjectMapper objectMapper) {
        return jsonSerializable.serializeJsonToByteBuffer(objectMapper);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Object getObjectFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.get(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getStringFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getString(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Integer getIntFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getInt(propertyName);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String toJsonFromJsonSerializable(JsonSerializable jsonSerializable) {
        return jsonSerializable.toJson();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ObjectNode getPropertyBagFromJsonSerializable(JsonSerializable jsonSerializable) {
        if (jsonSerializable == null) {
            return null;
        }
        return jsonSerializable.getPropertyBag();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setQueryRequestOptionsContinuationTokenAndMaxItemCount(CosmosQueryRequestOptions options, String continuationToken, Integer maxItemCount) {
        options.setRequestContinuation(continuationToken);
        options.setMaxItemCount(maxItemCount);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setQueryRequestOptionsContinuationToken(CosmosQueryRequestOptions cosmosQueryRequestOptions, String continuationToken) {
        cosmosQueryRequestOptions.setRequestContinuation(continuationToken);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void setQueryRequestOptionsMaxItemCount(CosmosQueryRequestOptions cosmosQueryRequestOptions, Integer maxItemCount) {
        cosmosQueryRequestOptions.setMaxItemCount(maxItemCount);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ByteBuffer serializeJsonToByteBuffer(SqlQuerySpec sqlQuerySpec) {
        sqlQuerySpec.populatePropertyBag();
        return sqlQuerySpec.getJsonSerializable().serializeJsonToByteBuffer();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> T instantiateByObjectNode(ObjectNode objectNode, Class<T> c) {
        try {
            return c.getDeclaredConstructor(ObjectNode.class).newInstance(objectNode);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> void populatePropertyBag(T t) {
        if (t instanceof JsonSerializable) {
            ((JsonSerializable) t).populatePropertyBag();
        } else if (t instanceof CompositePath) {
            ((CompositePath) t).populatePropertyBag();
        } else if (t instanceof ConflictResolutionPolicy) {
            ((ConflictResolutionPolicy) t).populatePropertyBag();
        } else if (t instanceof ExcludedPath) {
            ((ExcludedPath) t).populatePropertyBag();
        } else if (t instanceof IncludedPath) {
            ((IncludedPath) t).populatePropertyBag();
        } else if (t instanceof IndexingPolicy) {
            ((IndexingPolicy) t).populatePropertyBag();
        } else if (t instanceof PartitionKeyDefinition) {
            ((PartitionKeyDefinition) t).populatePropertyBag();
        } else if (t instanceof SpatialSpec) {
            ((SpatialSpec) t).populatePropertyBag();
        } else if (t instanceof SqlParameter) {
            ((SqlParameter) t).populatePropertyBag();
        } else if (t instanceof SqlQuerySpec) {
            ((SqlQuerySpec) t).populatePropertyBag();
        } else if (t instanceof UniqueKey) {
            ((UniqueKey) t).populatePropertyBag();
        } else if (t instanceof UniqueKeyPolicy) {
            ((UniqueKeyPolicy) t).populatePropertyBag();
        } else {
            throw new IllegalArgumentException("populatePropertyBag method does not exists in class " + t.getClass());
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> JsonSerializable getJsonSerializable(T t) {
        if (t instanceof JsonSerializable) {
            return (JsonSerializable) t;
        } if (t instanceof CompositePath) {
            return ((CompositePath) t).getJsonSerializable();
        } else if (t instanceof ConflictResolutionPolicy) {
            return ((ConflictResolutionPolicy) t).getJsonSerializable();
        } else if (t instanceof ExcludedPath) {
            return ((ExcludedPath) t).getJsonSerializable();
        } else if (t instanceof IncludedPath) {
            return ((IncludedPath) t).getJsonSerializable();
        } else if (t instanceof IndexingPolicy) {
            return ((IndexingPolicy) t).getJsonSerializable();
        } else if (t instanceof PartitionKeyDefinition) {
            return ((PartitionKeyDefinition) t).getJsonSerializable();
        } else if (t instanceof SpatialSpec) {
            return ((SpatialSpec) t).getJsonSerializable();
        } else if (t instanceof SqlParameter) {
            return ((SqlParameter) t).getJsonSerializable();
        } else if (t instanceof SqlQuerySpec) {
            return ((SqlQuerySpec) t).getJsonSerializable();
        } else if (t instanceof UniqueKey) {
            return ((UniqueKey) t).getJsonSerializable();
        } else if (t instanceof UniqueKeyPolicy) {
            return ((UniqueKeyPolicy) t).getJsonSerializable();
        } else {
            throw new IllegalArgumentException("getJsonSerializable method does not exists in class " + t.getClass());
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static <T> Resource getResource(T t) {
        if (t == null) {
            return null;
        } else if (t instanceof Resource) {
            return (Resource) t;
        } else if (t instanceof CosmosConflictProperties) {
            return ((CosmosConflictProperties) t).getResource();
        } else if (t instanceof CosmosContainerProperties) {
            return ((CosmosContainerProperties) t).getResource();
        } else if (t instanceof CosmosDatabaseProperties) {
            return ((CosmosDatabaseProperties) t).getResource();
        } else if (t instanceof CosmosPermissionProperties) {
            return ((CosmosPermissionProperties) t).getResource();
        } else if (t instanceof CosmosStoredProcedureProperties) {
            return ((CosmosStoredProcedureProperties) t).getResource();
        } else if (t instanceof CosmosTriggerProperties) {
            return ((CosmosTriggerProperties) t).getResource();
        } else if (t instanceof CosmosUserDefinedFunctionProperties) {
            return ((CosmosUserDefinedFunctionProperties) t).getResource();
        } else if (t instanceof CosmosUserProperties) {
            return ((CosmosUserProperties) t).getResource();
        } else {
            throw new IllegalArgumentException("getResource method does not exists in class " + t.getClass());
        }
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Offer getOfferFromThroughputProperties(ThroughputProperties properties) {
        return properties.getOffer();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static ThroughputResponse createThroughputRespose(ResourceResponse<Offer> offerResourceResponse) {
        return new ThroughputResponse(offerResourceResponse);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void addQueryInfoToFeedResponse(FeedResponse<?> feedResponse, QueryInfo queryInfo){
        feedResponse.setQueryInfo(queryInfo);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static void addQueryPlanDiagnosticsContextToFeedResponse(FeedResponse<?> feedResponse, QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnosticsContext){
        feedResponse.setQueryPlanDiagnosticsContext(queryPlanDiagnosticsContext);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static QueryInfo getQueryInfoFromFeedResponse(FeedResponse<?> response) {
        return response.getQueryInfo();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosQueryRequestOptions createQueryRequestOptions(CosmosQueryRequestOptions options) {
        return new CosmosQueryRequestOptions(options);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Integer getMaxItemCountFromQueryRequestOptions(CosmosQueryRequestOptions options) {
        return options.getMaxItemCount();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static String getRequestContinuationFromQueryRequestOptions(CosmosQueryRequestOptions options) {
        return options.getRequestContinuation();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static Map<String, Object> getPropertiesFromQueryRequestOptions(CosmosQueryRequestOptions options) {
        return options.getProperties();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosQueryRequestOptions setQueryRequestOptionsProperties(CosmosQueryRequestOptions options, Map<String, Object> properties) {
        return options.setProperties(properties);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static boolean getEmptyPagesAllowedFromQueryRequestOptions(CosmosQueryRequestOptions options) {
        return options.isEmptyPagesAllowed();
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static CosmosQueryRequestOptions setQueryRequestOptionsEmptyPagesAllowed(CosmosQueryRequestOptions options, boolean emptyPageAllowed) {
        return options.setEmptyPagesAllowed(emptyPageAllowed);
    }

    @Warning(value = INTERNAL_USE_ONLY_WARNING)
    public static IndexingPolicy createIndexingPolicy(Index[] indexes) {
        return new IndexingPolicy(indexes);
    }
}
