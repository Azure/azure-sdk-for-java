// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosStoredProcedure;
import com.azure.cosmos.CosmosTrigger;
import com.azure.cosmos.CosmosUserDefinedFunction;
import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.CosmosResourceType;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
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
import com.azure.cosmos.implementation.directconnectivity.Address;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfoInternal;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.implementation.query.orderbyquery.OrderByRowResult;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos.model package
 **/
public final class ModelBridgeInternal {

    public static CosmosAsyncConflictResponse createCosmosAsyncConflictResponse(ResourceResponse<Conflict> response,
                                                                                CosmosAsyncContainer container) {
        return new CosmosAsyncConflictResponse(response, container);
    }

    public static CosmosAsyncContainerResponse createCosmosAsyncContainerResponse(ResourceResponse<DocumentCollection> response,
                                                                                  CosmosAsyncDatabase database) {
        return new CosmosAsyncContainerResponse(response, database);
    }

    public static CosmosAsyncDatabaseResponse createCosmosAsyncDatabaseResponse(ResourceResponse<Database> response,
                                                                                 CosmosAsyncClient client) {
        return new CosmosAsyncDatabaseResponse(response, client);
    }

    public static <T> CosmosAsyncItemResponse<T> createCosmosAsyncItemResponse(ResourceResponse<Document> response, Class<T> classType) {
        return new CosmosAsyncItemResponse<>(response, classType);
    }

    public static CosmosAsyncItemResponse<Object> createCosmosAsyncItemResponseWithObjectType(ResourceResponse<Document> response) {
        return new CosmosAsyncItemResponse<>(response, Object.class);
    }

    public static CosmosAsyncPermissionResponse createCosmosAsyncPermissionResponse(ResourceResponse<Permission> response,
                                                                                    CosmosAsyncUser cosmosUser) {
        return new CosmosAsyncPermissionResponse(response, cosmosUser);
    }

    public static CosmosAsyncStoredProcedureResponse createCosmosAsyncStoredProcedureResponse(ResourceResponse<StoredProcedure> response,
                                                                                              CosmosAsyncContainer cosmosContainer) {
        return new CosmosAsyncStoredProcedureResponse(response, cosmosContainer);
    }

    public static CosmosAsyncStoredProcedureResponse createCosmosAsyncStoredProcedureResponse(StoredProcedureResponse response,
                                                                                              CosmosAsyncContainer cosmosContainer,
                                                                                              String storedProcedureId) {
        return new CosmosAsyncStoredProcedureResponse(response, cosmosContainer, storedProcedureId);
    }

    public static CosmosStoredProcedureProperties createCosmosStoredProcedureProperties(String jsonString) {
        return new CosmosStoredProcedureProperties(jsonString);
    }

    public static CosmosPermissionProperties createCosmosPermissionProperties(String jsonString) {
        return new CosmosPermissionProperties(jsonString);
    }

    public static CosmosAsyncTriggerResponse createCosmosAsyncTriggerResponse(ResourceResponse<Trigger> response,
                                                                              CosmosAsyncContainer container) {
        return new CosmosAsyncTriggerResponse(response, container);
    }

    public static CosmosAsyncUserDefinedFunctionResponse createCosmosAsyncUserDefinedFunctionResponse(ResourceResponse<UserDefinedFunction> response,
                                                                                                      CosmosAsyncContainer container) {
        return new CosmosAsyncUserDefinedFunctionResponse(response, container);
    }

    public static CosmosAsyncUserResponse createCosmosAsyncUserResponse(ResourceResponse<User> response, CosmosAsyncDatabase database) {
        return new CosmosAsyncUserResponse(response, database);
    }

    public static CosmosContainerResponse createCosmosContainerResponse(CosmosAsyncContainerResponse response,
                                                                        CosmosDatabase database, CosmosClient client) {
        return new CosmosContainerResponse(response, database, client);
    }

    public static CosmosUserResponse createCosmosUserResponse(CosmosAsyncUserResponse response, CosmosDatabase database) {
        return new CosmosUserResponse(response, database);
    }

    public static <T> CosmosItemResponse<T> createCosmosItemResponse(CosmosAsyncItemResponse<T> response) {
        return new CosmosItemResponse<>(response);
    }

    public static CosmosDatabaseResponse createCosmosDatabaseResponse(CosmosAsyncDatabaseResponse response, CosmosClient client) {
        return new CosmosDatabaseResponse(response, client);
    }

    public static CosmosStoredProcedureResponse createCosmosStoredProcedureResponse(CosmosAsyncStoredProcedureResponse resourceResponse,
                                                CosmosStoredProcedure storedProcedure) {
        return new CosmosStoredProcedureResponse(resourceResponse, storedProcedure);
    }

    public static CosmosUserDefinedFunctionResponse createCosmosUserDefinedFunctionResponse(CosmosAsyncUserDefinedFunctionResponse resourceResponse,
                                                    CosmosUserDefinedFunction userDefinedFunction) {
        return new CosmosUserDefinedFunctionResponse(resourceResponse, userDefinedFunction);
    }

    public static CosmosTriggerResponse createCosmosTriggerResponse(CosmosAsyncTriggerResponse asyncResponse,
                                        CosmosTrigger syncTrigger) {
        return new CosmosTriggerResponse(asyncResponse, syncTrigger);
    }

    public static List<CosmosConflictProperties> getCosmosConflictPropertiesFromV2Results(List<Conflict> results) {
        return CosmosConflictProperties.getFromV2Results(results);
    }

    public static DocumentCollection getV2Collection(CosmosContainerProperties containerProperties) {
        return containerProperties.getV2Collection();
    }

    public static List<CosmosContainerProperties> getCosmosContainerPropertiesFromV2Results(List<DocumentCollection> results) {
        return CosmosContainerProperties.getFromV2Results(results);
    }

    public static List<CosmosDatabaseProperties> getCosmosDatabasePropertiesFromV2Results(List<Database> results) {
        return CosmosDatabaseProperties.getFromV2Results(results);
    }

    public static <T> CosmosItemProperties getCosmosItemProperties(CosmosAsyncItemResponse<T> cosmosItemResponse) {
        return cosmosItemResponse.getProperties();
    }

    public static <T> CosmosItemProperties getCosmosItemProperties(CosmosItemResponse<T> cosmosItemResponse) {
        return cosmosItemResponse.getProperties();
    }

    public static Permission getV2Permissions(CosmosPermissionProperties permissionSettings) {
        return permissionSettings.getV2Permissions();
    }

    public static List<CosmosPermissionProperties> getCosmosPermissionPropertiesFromV2Results(List<Permission> results) {
        return CosmosPermissionProperties.getFromV2Results(results);
    }

    public static List<CosmosStoredProcedureProperties> getCosmosStoredProcedurePropertiesFromV2Results(List<StoredProcedure> results) {
        return CosmosStoredProcedureProperties.getFromV2Results(results);
    }

    public static List<CosmosTriggerProperties> getCosmosTriggerPropertiesFromV2Results(List<Trigger> results) {
        return CosmosTriggerProperties.getFromV2Results(results);
    }

    public static List<CosmosUserDefinedFunctionProperties> getCosmosUserDefinedFunctionPropertiesFromV2Results(List<UserDefinedFunction> results) {
        return CosmosUserDefinedFunctionProperties.getFromV2Results(results);
    }

    public static User getV2User(CosmosUserProperties cosmosUserProperties) {
        return cosmosUserProperties.getV2User();
    }

    public static List<CosmosUserProperties> getCosmosUserPropertiesFromV2Results(List<User> results) {
        return CosmosUserProperties.getFromV2Results(results);
    }

    public static RequestOptions toRequestOptions(CosmosConflictRequestOptions cosmosConflictRequestOptions) {
        return cosmosConflictRequestOptions.toRequestOptions();
    }

    public static RequestOptions toRequestOptions(CosmosContainerRequestOptions cosmosContainerRequestOptions) {
        return cosmosContainerRequestOptions.toRequestOptions();
    }

    public static CosmosContainerRequestOptions setOfferThroughput(CosmosContainerRequestOptions cosmosContainerRequestOptions,
                                                                   Integer offerThroughput) {
        return cosmosContainerRequestOptions.setOfferThroughput(offerThroughput);
    }

    public static RequestOptions toRequestOptions(CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions) {
        return cosmosDatabaseRequestOptions.toRequestOptions();
    }

    public static CosmosDatabaseRequestOptions setOfferThroughput(CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions,
                                                                   Integer offerThroughput) {
        return cosmosDatabaseRequestOptions.setOfferThroughput(offerThroughput);
    }

    public static CosmosDatabaseRequestOptions setOfferProperties(
        CosmosDatabaseRequestOptions cosmosDatabaseRequestOptions,
        ThroughputProperties throughputProperties) {
        return cosmosDatabaseRequestOptions.setThroughputProperties(throughputProperties);
    }

    public static CosmosContainerRequestOptions setOfferProperties(
        CosmosContainerRequestOptions containerRequestOptions,
        ThroughputProperties throughputProperties) {
        return containerRequestOptions.setThroughputProperties(throughputProperties);
    }

    public static Offer updateOfferFromProperties(Offer offer, ThroughputProperties properties) {
        return properties.updateOfferFromProperties(offer);
    }

    public static CosmosItemRequestOptions setPartitionKey(CosmosItemRequestOptions cosmosItemRequestOptions,
                                                           PartitionKey partitionKey) {
        return cosmosItemRequestOptions.setPartitionKey(partitionKey);
    }

    public static RequestOptions toRequestOptions(CosmosItemRequestOptions cosmosItemRequestOptions) {
        return cosmosItemRequestOptions.toRequestOptions();
    }

    public static CosmosItemRequestOptions createCosmosItemRequestOptions(PartitionKey partitionKey) {
        return new CosmosItemRequestOptions(partitionKey);
    }

    public static RequestOptions toRequestOptions(CosmosPermissionRequestOptions cosmosPermissionRequestOptions) {
        return cosmosPermissionRequestOptions.toRequestOptions();
    }

    public static RequestOptions toRequestOptions(CosmosStoredProcedureRequestOptions cosmosStoredProcedureRequestOptions) {
        return cosmosStoredProcedureRequestOptions.toRequestOptions();
    }

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
     * @param options the feed options
     * @return the partitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(FeedOptions options) {
        return options.getPartitionKeyRangeIdInternal();
    }

    /**
     * Sets the PartitionKeyRangeId.
     *
     * @param options the feed options
     * @param partitionKeyRangeId the partition key range id
     * @return the partitionKeyRangeId.
     */
    public static FeedOptions partitionKeyRangeIdInternal(FeedOptions options, String partitionKeyRangeId) {
        return options.setPartitionKeyRangeIdInternal(partitionKeyRangeId);
    }

    public static <T extends Resource> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
                                                                          Class<T> cls) {
        return new FeedResponse<T>(response.getQueryResponse(cls), response.getResponseHeaders());
    }

    public static <T> FeedResponse<T> toFeedResponsePage(List<T> results, Map<String, String> headers, boolean noChanges) {
        return new FeedResponse<>(results, headers, noChanges);
    }

    public static <T extends Resource> FeedResponse<T> toChaneFeedResponsePage(RxDocumentServiceResponse response,
                                                                               Class<T> cls) {
        return new FeedResponse<T>(noChanges(response) ? Collections.emptyList() : response.getQueryResponse(cls),
            response.getResponseHeaders(), noChanges(response));
    }

    public static <T extends Resource> boolean noChanges(FeedResponse<T> page) {
        return page.nochanges;
    }

    public static <T extends Resource> boolean noChanges(RxDocumentServiceResponse rsp) {
        return rsp.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED;
    }

    public static <T> FeedResponse<T> createFeedResponse(List<T> results,
                                                         Map<String, String> headers) {
        return new FeedResponse<>(results, headers);
    }

    public static <T> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results,
                                                                         Map<String, String> headers, ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        return new FeedResponse<>(results, headers, queryMetricsMap);
    }

    public static <T> ConcurrentMap<String, QueryMetrics> queryMetricsMap(FeedResponse<T> feedResponse) {
        return feedResponse.queryMetricsMap();
    }

    public static <T> ConcurrentMap<String, QueryMetrics> queryMetrics(FeedResponse<T> feedResponse) {
        return feedResponse.queryMetrics();
    }

    public static String toLower(RequestVerb verb) {
        return verb.toLowerCase();
    }

    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.getVersion() != null && PartitionKeyDefinitionVersion.V2.val == pkd.getVersion().val;
    }

    public static PartitionKeyInternal getNonePartitionKey(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getNonePartitionKeyValue();
    }

    public static PartitionKeyInternal getPartitionKeyInternal(PartitionKey partitionKey) {
        return partitionKey.getInternalPartitionKey();
    }

    public static PartitionKey partitionKeyfromJsonString(String jsonString) {
        return PartitionKey.fromJsonString(jsonString);
    }

    public static Object getPartitionKeyObject(PartitionKey right) {
        return right.getKeyObject();
    }

    public static String getAltLink(Resource resource) {
        return resource.getAltLink();
    }

    public static void setAltLink(Resource resource, String altLink) {
        resource.setAltLink(altLink);
    }

    public static void setResourceId(Resource resource, String resourceId) {
        resource.setResourceId(resourceId);
    }

    public static void setResourceSelfLink(Resource resource, String selfLink) {
        resource.setSelfLink(selfLink);
    }

    public static void setTimestamp(Resource resource, OffsetDateTime date) {
        resource.setTimestamp(date);
    }

    public static void validateResource(Resource resource) {
        Resource.validateResource(resource);
    }

    public static <T> void setProperty(JsonSerializable jsonSerializable, String propertyName, T value) {
        jsonSerializable.set(propertyName, value);
    }

    public static ObjectNode getObjectNodeFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getObject(propertyName);
    }

    public static void removeFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        jsonSerializable.remove(propertyName);
    }

    public static Object getValue(JsonNode value) {
        return JsonSerializable.getValue(value);
    }

    public static CosmosError createCosmosError(ObjectNode objectNode) {
        return new CosmosError(objectNode);
    }

    public static CosmosError createCosmosError(String jsonString) {
        return new CosmosError(jsonString);
    }

    public static void populatePropertyBagJsonSerializable(JsonSerializable jsonSerializable) {
        jsonSerializable.populatePropertyBag();
    }

    public static JsonSerializable instantiateJsonSerializable(ObjectNode objectNode, Class<?> klassType) {
        try {
            // the hot path should come through here to avoid serialization/deserialization
            if (klassType.equals(Document.class) || klassType.equals(OrderByRowResult.class) || klassType.equals(CosmosItemProperties.class)
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

    public static Map<String, Object> getMapFromJsonSerializable(JsonSerializable jsonSerializable) {
        return jsonSerializable.getMap();
    }

    public static CosmosResourceType fromServiceSerializedFormat(String cosmosResourceType) {
        return CosmosResourceType.fromServiceSerializedFormat(cosmosResourceType);
    }

    public static Boolean getBooleanFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getBoolean(propertyName);
    }

    public static Double getDoubleFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getDouble(propertyName);
    }

    public static Object getObjectByPathFromJsonSerializable(JsonSerializable jsonSerializable, List<String> propertyNames) {
        return jsonSerializable.getObjectByPath(propertyNames);
    }

    public static ByteBuffer serializeJsonToByteBuffer(JsonSerializable jsonSerializable) {
        return jsonSerializable.serializeJsonToByteBuffer();
    }

    public static <T> T toObjectFromJsonSerializable(JsonSerializable jsonSerializable, Class<T> c) {
        return jsonSerializable.toObject(c);
    }

    public static Object getObjectFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.get(propertyName);
    }

    public static String getStringFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getString(propertyName);
    }

    public static Integer getIntFromJsonSerializable(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getInt(propertyName);
    }

    public static String toJsonFromJsonSerializable(JsonSerializable jsonSerializable) {
        return jsonSerializable.toJson();
    }

    public static ObjectNode getPropertyBagFromJsonSerializable(JsonSerializable jsonSerializable) {
        if (jsonSerializable == null) {
            return null;
        }
        return jsonSerializable.getPropertyBag();
    }

    public static void setFeedOptionsContinuationTokenAndMaxItemCount(FeedOptions feedOptions, String continuationToken, Integer maxItemCount) {
        feedOptions.setRequestContinuation(continuationToken);
        feedOptions.setMaxItemCount(maxItemCount);
    }

    public static void setFeedOptionsContinuationToken(FeedOptions feedOptions, String continuationToken) {
        feedOptions.setRequestContinuation(continuationToken);
    }

    public static void setFeedOptionsMaxItemCount(FeedOptions feedOptions, Integer maxItemCount) {
        feedOptions.setMaxItemCount(maxItemCount);
    }

    public static ByteBuffer serializeJsonToByteBuffer(JsonSerializableWrapper jsonSerializableWrapper) {
        jsonSerializableWrapper.populatePropertyBag();
        return jsonSerializableWrapper.jsonSerializable.serializeJsonToByteBuffer();
    }

    public static JsonSerializableWrapper instantiateJsonSerializableWrapper(ObjectNode objectNode, Class<?> klassType) {
        try {
            return (JsonSerializableWrapper) klassType.getDeclaredConstructor(String.class).newInstance(Utils.toJson(Utils.getSimpleObjectMapper(), objectNode));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void populatePropertyBagJsonSerializableWrapper(JsonSerializableWrapper jsonSerializableWrapper) {
        jsonSerializableWrapper.populatePropertyBag();
    }

    public static Resource getResourceFromResourceWrapper(ResourceWrapper resourceWrapper) {
        if (resourceWrapper == null) {
            return null;
        }
        return resourceWrapper.getResource();
    }

    public static JsonSerializable getJsonSerializableFromIndex(Index index) {
        return index.getJsonSerializable();
    }

    public static JsonSerializable getJsonSerializableFromJsonSerializableWrapper(JsonSerializableWrapper jsonSerializableWrapper) {
        return jsonSerializableWrapper.getJsonSerializable();
    }

    public static Offer getOfferFromThroughputProperties(ThroughputProperties properties) {
        return properties.getOffer();
    }

    public static ThroughputResponse createThroughputRespose(ResourceResponse<Offer> offerResourceResponse) {
        return new ThroughputResponse(offerResourceResponse);
    }
}
