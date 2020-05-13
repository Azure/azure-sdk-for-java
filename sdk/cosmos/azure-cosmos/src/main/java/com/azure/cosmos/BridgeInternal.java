// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ChangeFeedOptions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.ReplicationPolicy;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.StoredProcedureResponse;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.query.metrics.ClientSideMetrics;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosError;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.cosmos
 **/
public final class BridgeInternal {

    public static Document documentFromObject(Object document, ObjectMapper mapper) {
        return Document.fromObject(document, mapper);
    }

    public static ByteBuffer serializeJsonToByteBuffer(Object document, ObjectMapper mapper) {
        return CosmosItemProperties.serializeJsonToByteBuffer(document, mapper);
    }

    public static void monitorTelemetry(MeterRegistry registry) {
        CosmosAsyncClient.setMonitorTelemetry(registry);
    }

    public static <T extends Resource> ResourceResponse<T> toResourceResponse(RxDocumentServiceResponse response,
                                                                              Class<T> cls) {
        return new ResourceResponse<T>(response, cls);
    }

    public static <T extends Resource> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
                                                                          Class<T> cls) {
        return ModelBridgeInternal.toFeedResponsePage(response, cls);
    }

    public static <T> FeedResponse<T> toFeedResponsePage(List<T> results, Map<String, String> headers, boolean noChanges) {
        return ModelBridgeInternal.toFeedResponsePage(results, headers, noChanges);
    }

    public static <T extends Resource> FeedResponse<T> toChaneFeedResponsePage(RxDocumentServiceResponse response,
                                                                               Class<T> cls) {
        return ModelBridgeInternal.toChaneFeedResponsePage(response, cls);
    }

    public static StoredProcedureResponse toStoredProcedureResponse(RxDocumentServiceResponse response) {
        return new StoredProcedureResponse(response);
    }

    public static Map<String, String> getFeedHeaders(ChangeFeedOptions options) {

        if (options == null) {
            return new HashMap<>();
        }

        Map<String, String> headers = new HashMap<>();

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, options.getMaxItemCount().toString());
        }

        String ifNoneMatchValue = null;
        if (options.getRequestContinuation() != null) {
            ifNoneMatchValue = options.getRequestContinuation();
        } else if (!options.isStartFromBeginning()) {
            ifNoneMatchValue = "*";
        }
        // On REST level, change feed is using IF_NONE_MATCH/ETag instead of
        // continuation.
        if (ifNoneMatchValue != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, ifNoneMatchValue);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, Constants.QueryExecutionContext.INCREMENTAL_FEED_HEADER_VALUE);

        return headers;
    }

    public static Map<String, String> getFeedHeaders(FeedOptions options) {

        if (options == null) {
            return new HashMap<>();
        }

        Map<String, String> headers = new HashMap<>();

        if (options.getMaxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, options.getMaxItemCount().toString());
        }

        if (options.getRequestContinuation() != null) {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, options.getRequestContinuation());
        }

        if (options.getSessionToken() != null) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, options.getSessionToken());
        }

        if (options.isScanInQueryEnabled() != null) {
            headers.put(HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY, options.isScanInQueryEnabled().toString());
        }

        if (options.isEmitVerboseTracesInQuery() != null) {
            headers.put(HttpConstants.HttpHeaders.EMIT_VERBOSE_TRACES_IN_QUERY,
                options.isEmitVerboseTracesInQuery().toString());
        }

        if (options.getMaxDegreeOfParallelism() != 0) {
            headers.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Boolean.TRUE.toString());
        }

        if (options.setResponseContinuationTokenLimitInKb() > 0) {
            headers.put(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB,
                Strings.toString(options.setResponseContinuationTokenLimitInKb()));
        }

        if (options.isPopulateQueryMetrics()) {
            headers.put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS,
                String.valueOf(options.isPopulateQueryMetrics()));
        }

        return headers;
    }

    public static <T extends Resource> boolean noChanges(FeedResponse<T> page) {
        return ModelBridgeInternal.noChanges(page);
    }

    public static <T extends Resource> boolean noChanges(RxDocumentServiceResponse rsp) {
        return rsp.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED;
    }

    public static <T> FeedResponse<T> createFeedResponse(List<T> results,
            Map<String, String> headers) {
        return ModelBridgeInternal.createFeedResponse(results, headers);
    }

    public static <T> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results,
            Map<String, String> headers, ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        return ModelBridgeInternal.createFeedResponseWithQueryMetrics(results, headers, queryMetricsMap);
    }

    public static FeedResponseDiagnostics createFeedResponseDiagnostics(Map<String, QueryMetrics> queryMetricsMap) {
        return new FeedResponseDiagnostics(queryMetricsMap);
    }

    public static <E extends CosmosClientException> E setResourceAddress(E e, String resourceAddress) {
        e.setResourceAddress(resourceAddress);
        return e;
    }

    public static <E extends CosmosClientException> long getLSN(E e) {
        return e.lsn;
    }

    public static <E extends CosmosClientException> String getPartitionKeyRangeId(E e) {
        return e.partitionKeyRangeId;
    }

    public static <E extends CosmosClientException> String getResourceAddress(E e) {
        return e.getResourceAddress();
    }

    public static <E extends CosmosClientException> E setLSN(E e, long lsn) {
        e.lsn = lsn;
        return e;
    }

    public static <E extends CosmosClientException> E setPartitionKeyRangeId(E e, String partitionKeyRangeId) {
        e.partitionKeyRangeId = partitionKeyRangeId;
        return e;
    }

    public static boolean isEnableMultipleWriteLocations(DatabaseAccount account) {
        return account.getEnableMultipleWriteLocations();
    }

    public static <E extends CosmosClientException> Uri getRequestUri(CosmosClientException cosmosClientException) {
        return cosmosClientException.requestUri;
    }

    public static <E extends CosmosClientException> void setRequestHeaders(CosmosClientException cosmosClientException,
                                                                           Map<String, String> requestHeaders) {
        cosmosClientException.requestHeaders = requestHeaders;
    }

    public static <E extends CosmosClientException> Map<String, String> getRequestHeaders(
        CosmosClientException cosmosClientException) {
        return cosmosClientException.requestHeaders;
    }

    public static String getAltLink(Resource resource) {
        return ModelBridgeInternal.getAltLink(resource);
    }

    public static void setAltLink(Resource resource, String altLink) {
        ModelBridgeInternal.setAltLink(resource, altLink);
    }

    public static void setMaxReplicaSetSize(ReplicationPolicy replicationPolicy, int value) {
        replicationPolicy.setMaxReplicaSetSize(value);
    }

    public static <T extends Resource> void putQueryMetricsIntoMap(FeedResponse<T> response, String partitionKeyRangeId,
                                                                   QueryMetrics queryMetrics) {
        ModelBridgeInternal.queryMetricsMap(response).put(partitionKeyRangeId, queryMetrics);
    }

    public static QueryMetrics createQueryMetricsFromDelimitedStringAndClientSideMetrics(
        String queryMetricsDelimitedString, ClientSideMetrics clientSideMetrics, String activityId) {
        return QueryMetrics.createFromDelimitedStringAndClientSideMetrics(queryMetricsDelimitedString,
            clientSideMetrics, activityId);
    }

    public static QueryMetrics createQueryMetricsFromCollection(Collection<QueryMetrics> queryMetricsCollection) {
        return QueryMetrics.createFromCollection(queryMetricsCollection);
    }

    public static ClientSideMetrics getClientSideMetrics(QueryMetrics queryMetrics) {
        return queryMetrics.getClientSideMetrics();
    }

    public static String getInnerErrorMessage(CosmosClientException cosmosClientException) {
        if (cosmosClientException == null) {
            return null;
        }
        return cosmosClientException.innerErrorMessage();
    }

    public static PartitionKey getPartitionKey(PartitionKeyInternal partitionKeyInternal) {
        return new PartitionKey(partitionKeyInternal);
    }

    public static <T> void setProperty(JsonSerializable jsonSerializable, String propertyName, T value) {
        ModelBridgeInternal.setProperty(jsonSerializable, propertyName, value);
    }

    public static ObjectNode getObject(JsonSerializable jsonSerializable, String propertyName) {
        return ModelBridgeInternal.getObjectNodeFromJsonSerializable(jsonSerializable, propertyName);
    }

    public static void remove(JsonSerializable jsonSerializable, String propertyName) {
        ModelBridgeInternal.removeFromJsonSerializable(jsonSerializable, propertyName);
    }

    public static CosmosStoredProcedureProperties createCosmosStoredProcedureProperties(String jsonString) {
        return ModelBridgeInternal.createCosmosStoredProcedureProperties(jsonString);
    }

    public static Object getValue(JsonNode value) {
        return ModelBridgeInternal.getValue(value);
    }

    public static CosmosClientException setCosmosResponseDiagnostics(
                                            CosmosClientException cosmosClientException,
                                            CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        return cosmosClientException.setResponseDiagnostics(cosmosResponseDiagnostics);
    }

    public static CosmosClientException createCosmosClientException(int statusCode) {
        return new CosmosClientException(statusCode, null, null, null);
    }

    public static CosmosClientException createCosmosClientException(int statusCode, String errorMessage) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode, errorMessage, null, null);
        cosmosClientException.setError(new CosmosError());
        ModelBridgeInternal.setProperty(cosmosClientException.getError(), Constants.Properties.MESSAGE, errorMessage);
        return cosmosClientException;
    }

    public static CosmosClientException createCosmosClientException(int statusCode, Exception innerException) {
        return new CosmosClientException(statusCode, null, null, innerException);
    }

    public static CosmosClientException createCosmosClientException(int statusCode, CosmosError cosmosErrorResource,
                                                                    Map<String, String> responseHeaders) {
        return new CosmosClientException(/* resourceAddress */ null, statusCode, cosmosErrorResource, responseHeaders);
    }

    public static CosmosClientException createCosmosClientException(String resourceAddress,
                                                                    int statusCode,
                                                                    CosmosError cosmosErrorResource,
                                                                    Map<String, String> responseHeaders) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode,
            cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, null);
        cosmosClientException.setResourceAddress(resourceAddress);
        cosmosClientException.setError(cosmosErrorResource);
        return cosmosClientException;
    }

    public static CosmosClientException createCosmosClientException(String message,
                                                                    Exception exception,
                                                                    Map<String, String> responseHeaders,
                                                                    int statusCode,
                                                                    String resourceAddress) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode, message, responseHeaders,
            exception);
        cosmosClientException.setResourceAddress(resourceAddress);
        return cosmosClientException;
    }

    public static Configs extractConfigs(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.configs();
    }

    public static CosmosClientBuilder injectConfigs(CosmosClientBuilder cosmosClientBuilder, Configs configs) {
        return cosmosClientBuilder.configs(configs);
    }

    public static String extractContainerSelfLink(CosmosAsyncContainer container) {
        return container.getLink();
    }

    public static String extractResourceSelfLink(Resource resource) {
        return resource.getSelfLink();
    }

    public static void setResourceSelfLink(Resource resource, String selfLink) {
        ModelBridgeInternal.setResourceSelfLink(resource, selfLink);
    }

    public static void setTimestamp(Resource resource, OffsetDateTime date) {
        ModelBridgeInternal.setTimestamp(resource, date);
    }

    public static CosmosResponseDiagnostics createCosmosResponseDiagnostics() {
        return new CosmosResponseDiagnostics();
    }

    public static void setTransportClientRequestTimelineOnDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                                                      RequestTimeline requestTimeline) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().setTransportClientRequestTimeline(requestTimeline);
    }

    public static void recordResponse(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                           RxDocumentServiceRequest request, StoreResult storeResult) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().recordResponse(request, storeResult);
    }

    public static void recordRetryContext(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                      RxDocumentServiceRequest request) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().recordRetryContext(request);
    }

    public static MetadataDiagnosticsContext getMetaDataDiagnosticContext(CosmosResponseDiagnostics cosmosResponseDiagnostics){
        if(cosmosResponseDiagnostics == null) {
            return null;
        }

        return cosmosResponseDiagnostics.clientSideRequestStatistics().getMetadataDiagnosticsContext();
    }

    public static SerializationDiagnosticsContext getSerializationDiagnosticsContext(CosmosResponseDiagnostics cosmosResponseDiagnostics){
        if(cosmosResponseDiagnostics == null) {
            return null;
        }

        return cosmosResponseDiagnostics.clientSideRequestStatistics().getSerializationDiagnosticsContext();
    }

    public static void recordGatewayResponse(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                             RxDocumentServiceRequest rxDocumentServiceRequest,
                                             StoreResponse storeResponse,
                                             CosmosClientException exception) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().recordGatewayResponse(rxDocumentServiceRequest, storeResponse, exception);
    }

    public static String recordAddressResolutionStart(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                                      URI targetEndpoint) {
        return cosmosResponseDiagnostics.clientSideRequestStatistics().recordAddressResolutionStart(targetEndpoint);
    }

    public static void recordAddressResolutionEnd(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                                  String identifier) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().recordAddressResolutionEnd(identifier);
    }

    public static List<URI> getContactedReplicas(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        return cosmosResponseDiagnostics.clientSideRequestStatistics().getContactedReplicas();
    }

    public static void setContactedReplicas(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                            List<URI> contactedReplicas) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().setContactedReplicas(contactedReplicas);
    }

    public static Set<URI> getFailedReplicas(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        return cosmosResponseDiagnostics.clientSideRequestStatistics().getFailedReplicas();
    }

    public static <T> ConcurrentMap<String, QueryMetrics> queryMetricsFromFeedResponse(FeedResponse<T> feedResponse) {
        return ModelBridgeInternal.queryMetrics(feedResponse);
    }

    public static PartitionKeyInternal getPartitionKeyInternal(PartitionKey partitionKey) {
        return ModelBridgeInternal.getPartitionKeyInternal(partitionKey);
    }

    public static <T> CosmosItemProperties getProperties(CosmosAsyncItemResponse<T> cosmosItemResponse) {
        return ModelBridgeInternal.getCosmosItemProperties(cosmosItemResponse);
    }

    public static <T> CosmosItemProperties getProperties(CosmosItemResponse<T> cosmosItemResponse) {
        return ModelBridgeInternal.getCosmosItemProperties(cosmosItemResponse);
    }

    public static int getHashCode(CosmosKeyCredential keyCredential) {
        return keyCredential.getKeyHashCode();
    }

    public static String getLink(CosmosAsyncContainer cosmosAsyncContainer) {
        return cosmosAsyncContainer.getLink();
    }

    public static CosmosAsyncConflict createCosmosAsyncConflict(String id, CosmosAsyncContainer container) {
        return new CosmosAsyncConflict(id, container);
    }

    public static CosmosAsyncContainer createCosmosAsyncContainer(String id, CosmosAsyncDatabase database) {
        return new CosmosAsyncContainer(id, database);
    }

    public static CosmosAsyncDatabase createCosmosAsyncDatabase(String id, CosmosAsyncClient client) {
        return new CosmosAsyncDatabase(id, client);
    }

    public static CosmosAsyncPermission createCosmosAsyncPermission(String id, CosmosAsyncUser user) {
        return new CosmosAsyncPermission(id, user);
    }

    public static CosmosAsyncStoredProcedure createCosmosAsyncStoredProcedure(String id, CosmosAsyncContainer cosmosContainer) {
        return new CosmosAsyncStoredProcedure(id, cosmosContainer);
    }

    public static CosmosAsyncTrigger createCosmosAsyncTrigger(String id, CosmosAsyncContainer container) {
        return new CosmosAsyncTrigger(id, container);
    }

    public static CosmosAsyncUserDefinedFunction createCosmosAsyncUserDefinedFunction(String id, CosmosAsyncContainer container) {
        return new CosmosAsyncUserDefinedFunction(id, container);
    }

    public static CosmosAsyncUser createCosmosAsyncUser(String id, CosmosAsyncDatabase database) {
        return new CosmosAsyncUser(id, database);
    }

    public static CosmosContainer createCosmosContainer(String id, CosmosDatabase database, CosmosAsyncContainer container) {
        return new CosmosContainer(id, database, container);
    }

    public static CosmosDatabase createCosmosDatabase(String id, CosmosClient client, CosmosAsyncDatabase database) {
        return new CosmosDatabase(id, client, database);
    }

    public static CosmosUser createCosmosUser(CosmosAsyncUser asyncUser, CosmosDatabase database, String id) {
        return new CosmosUser(asyncUser, database, id);
    }

    public static ConsistencyLevel fromServiceSerializedFormat(String consistencyLevel) {
        return ConsistencyLevel.fromServiceSerializedFormat(consistencyLevel);
    }
}
