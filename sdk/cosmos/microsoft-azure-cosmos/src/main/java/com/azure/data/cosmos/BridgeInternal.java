// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.ReplicationPolicy;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.StoredProcedureResponse;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.directconnectivity.StoreResult;
import com.azure.data.cosmos.internal.query.metrics.ClientSideMetrics;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.azure.data.cosmos.internal.Constants.QueryExecutionContext.INCREMENTAL_FEED_HEADER_VALUE;

/**
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.data.cosmos
 **/
public class BridgeInternal {

    public static CosmosError createCosmosError(ObjectNode objectNode) {
        return new CosmosError(objectNode);
    }

    public static CosmosError createCosmosError(String jsonString) {
        return new CosmosError(jsonString);
    }

    public static Document documentFromObject(Object document, ObjectMapper mapper) {
        return Document.FromObject(document, mapper);
    }

    public static <T extends Resource> ResourceResponse<T> toResourceResponse(RxDocumentServiceResponse response,
                                                                              Class<T> cls) {
        return new ResourceResponse<T>(response, cls);
    }

    public static <T extends Resource> FeedResponse<T> toFeedResponsePage(RxDocumentServiceResponse response,
            Class<T> cls) {
        return new FeedResponse<T>(response.getQueryResponse(cls), response.getResponseHeaders());
    }

    public static <T extends Resource> FeedResponse<T> toChaneFeedResponsePage(RxDocumentServiceResponse response,
            Class<T> cls) {
        return new FeedResponse<T>(noChanges(response) ? Collections.emptyList() : response.getQueryResponse(cls),
                response.getResponseHeaders(), noChanges(response));
    }

    public static StoredProcedureResponse toStoredProcedureResponse(RxDocumentServiceResponse response) {
        return new StoredProcedureResponse(response);
    }

    public static DatabaseAccount toDatabaseAccount(RxDocumentServiceResponse response) {
        DatabaseAccount account = response.getResource(DatabaseAccount.class);

        // read the headers and set to the account
        Map<String, String> responseHeader = response.getResponseHeaders();

        account.setMaxMediaStorageUsageInMB(
                Long.valueOf(responseHeader.get(HttpConstants.HttpHeaders.MAX_MEDIA_STORAGE_USAGE_IN_MB)));
        account.setMediaStorageUsageInMB(
                Long.valueOf(responseHeader.get(HttpConstants.HttpHeaders.CURRENT_MEDIA_STORAGE_USAGE_IN_MB)));

        return account;
    }

    public static Map<String, String> getFeedHeaders(ChangeFeedOptions options) {

        if (options == null)
            return new HashMap<>();

        Map<String, String> headers = new HashMap<>();

        if (options.maxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, options.maxItemCount().toString());
        }

        String ifNoneMatchValue = null;
        if (options.requestContinuation() != null) {
            ifNoneMatchValue = options.requestContinuation();
        } else if (!options.startFromBeginning()) {
            ifNoneMatchValue = "*";
        }
        // On REST level, change feed is using IF_NONE_MATCH/ETag instead of
        // continuation.
        if (ifNoneMatchValue != null) {
            headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, ifNoneMatchValue);
        }

        headers.put(HttpConstants.HttpHeaders.A_IM, INCREMENTAL_FEED_HEADER_VALUE);

        return headers;
    }

    public static Map<String, String> getFeedHeaders(FeedOptions options) {

        if (options == null)
            return new HashMap<>();

        Map<String, String> headers = new HashMap<>();

        if (options.maxItemCount() != null) {
            headers.put(HttpConstants.HttpHeaders.PAGE_SIZE, options.maxItemCount().toString());
        }

        if (options.requestContinuation() != null) {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, options.requestContinuation());
        }

        if (options != null) {
            if (options.sessionToken() != null) {
                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, options.sessionToken());
            }

            if (options.enableScanInQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY, options.enableScanInQuery().toString());
            }

            if (options.emitVerboseTracesInQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.EMIT_VERBOSE_TRACES_IN_QUERY,
                        options.emitVerboseTracesInQuery().toString());
            }

            if (options.enableCrossPartitionQuery() != null) {
                headers.put(HttpConstants.HttpHeaders.ENABLE_CROSS_PARTITION_QUERY,
                        options.enableCrossPartitionQuery().toString());
            }

            if (options.maxDegreeOfParallelism() != 0) {
                headers.put(HttpConstants.HttpHeaders.PARALLELIZE_CROSS_PARTITION_QUERY, Boolean.TRUE.toString());
            }

            if (options.responseContinuationTokenLimitInKb() > 0) {
                headers.put(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB,
                        Strings.toString(options.responseContinuationTokenLimitInKb()));
            }

            if (options.populateQueryMetrics()) {
                headers.put(HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS,
                        String.valueOf(options.populateQueryMetrics()));
            }
        }

        return headers;
    }

    public static <T extends Resource> boolean noChanges(FeedResponse<T> page) {
        return page.nochanges;
    }

    public static <T extends Resource> boolean noChanges(RxDocumentServiceResponse rsp) {
        return rsp.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED;
    }

    public static <T extends Resource> FeedResponse<T> createFeedResponse(List<T> results,
            Map<String, String> headers) {
        return new FeedResponse<>(results, headers);
    }

    public static <T extends Resource> FeedResponse<T> createFeedResponseWithQueryMetrics(List<T> results,
            Map<String, String> headers, ConcurrentMap<String, QueryMetrics> queryMetricsMap) {
        return new FeedResponse<>(results, headers, queryMetricsMap);
    }

    public static <E extends CosmosClientException> E setResourceAddress(E e, String resourceAddress) {
        e.resourceAddress = resourceAddress;
        return e;
    }

    public static <E extends CosmosClientException> long getLSN(E e) {
        return e.lsn;
    }

    public static <E extends CosmosClientException> String getPartitionKeyRangeId(E e) {
        return e.partitionKeyRangeId;
    }

    public static <E extends CosmosClientException> String getResourceAddress(E e) {
        return e.resourceAddress;
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
        return account.isEnableMultipleWriteLocations();
    }

    public static boolean getUseMultipleWriteLocations(ConnectionPolicy policy) {
        return policy.usingMultipleWriteLocations();
    }

    public static void setUseMultipleWriteLocations(ConnectionPolicy policy, boolean value) {
        policy.usingMultipleWriteLocations(value);
    }

    public static <E extends CosmosClientException> URI getRequestUri(CosmosClientException cosmosClientException) {
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

    public static Map<String, Object> getQueryEngineConfiuration(DatabaseAccount databaseAccount) {
        return databaseAccount.getQueryEngineConfiuration();
    }

    public static ReplicationPolicy getReplicationPolicy(DatabaseAccount databaseAccount) {
        return databaseAccount.getReplicationPolicy();
    }

    public static ReplicationPolicy getSystemReplicationPolicy(DatabaseAccount databaseAccount) {
        return databaseAccount.getSystemReplicationPolicy();
    }

    public static ConsistencyPolicy getConsistencyPolicy(DatabaseAccount databaseAccount) {
        return databaseAccount.getConsistencyPolicy();
    }

    public static String getAltLink(Resource resource) {
        return resource.altLink();
    }

    public static void setAltLink(Resource resource, String altLink) {
        resource.altLink(altLink);
    }

    public static void setMaxReplicaSetSize(ReplicationPolicy replicationPolicy, int value) {
        replicationPolicy.setMaxReplicaSetSize(value);
    }

    public static <T extends Resource> void putQueryMetricsIntoMap(FeedResponse<T> response, String partitionKeyRangeId,
            QueryMetrics queryMetrics) {
        response.queryMetricsMap().put(partitionKeyRangeId, queryMetrics);
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

    public static PartitionKeyInternal getNonePartitionKey(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getNonePartitionKeyValue();
    }

    public static PartitionKey getPartitionKey(PartitionKeyInternal partitionKeyInternal) {
        return new PartitionKey(partitionKeyInternal);
    }

    public static <T> void setProperty(JsonSerializable jsonSerializable, String propertyName, T value) {
        jsonSerializable.set(propertyName, value);
    }

    public static ObjectNode getObject(JsonSerializable jsonSerializable, String propertyName) {
        return jsonSerializable.getObject(propertyName);
    }

    public static void remove(JsonSerializable jsonSerializable, String propertyName) {
        jsonSerializable.remove(propertyName);
    }

    public static CosmosStoredProcedureProperties createCosmosStoredProcedureProperties(String jsonString) {
        return new CosmosStoredProcedureProperties(jsonString);
    }

    public static Object getValue(JsonNode value) {
        return JsonSerializable.getValue(value);
    }

    public static CosmosClientException setCosmosResponseDiagnostics(CosmosClientException cosmosClientException, CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        return cosmosClientException.cosmosResponseDiagnostics(cosmosResponseDiagnostics);
    }

    public static CosmosClientException createCosmosClientException(int statusCode) {
        return new CosmosClientException(statusCode, null, null, null);
    }

    public static CosmosClientException createCosmosClientException(int statusCode, String errorMessage) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode, errorMessage, null, null);
        cosmosClientException.error(new CosmosError());
        cosmosClientException.error().set(Constants.Properties.MESSAGE, errorMessage);
        return cosmosClientException;
    }

    public static CosmosClientException createCosmosClientException(int statusCode, Exception innerException) {
        return new CosmosClientException(statusCode, null, null, innerException);
    }

    public static CosmosClientException createCosmosClientException(int statusCode, CosmosError cosmosErrorResource, Map<String, String> responseHeaders) {
        return new CosmosClientException(/* resourceAddress */ null, statusCode, cosmosErrorResource, responseHeaders);
    }

    public static CosmosClientException createCosmosClientException(String resourceAddress, int statusCode, CosmosError cosmosErrorResource, Map<String, String> responseHeaders) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode, cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, null);
        cosmosClientException.resourceAddress = resourceAddress;
        cosmosClientException.error(cosmosErrorResource);
        return cosmosClientException;
    }

    public static CosmosClientException createCosmosClientException(String message, Exception exception, Map<String, String> responseHeaders, int statusCode, String resourceAddress) {
        CosmosClientException cosmosClientException = new CosmosClientException(statusCode, message, responseHeaders, exception);
        cosmosClientException.resourceAddress = resourceAddress;
        return cosmosClientException;
    }

    public static Configs extractConfigs(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.configs();
    }

    public static CosmosClientBuilder injectConfigs(CosmosClientBuilder cosmosClientBuilder, Configs configs) {
        return cosmosClientBuilder.configs(configs);
    }

    public static String extractContainerSelfLink(CosmosContainer container) {
        return container.getLink();
    }

    public static String extractResourceSelfLink(Resource resource) { return resource.selfLink(); }

    public static void setResourceSelfLink(Resource resource, String selfLink) { resource.selfLink(selfLink); }

    public static void populatePropertyBagJsonSerializable(JsonSerializable jsonSerializable) { jsonSerializable.populatePropertyBag(); }

    public static void setMapper(JsonSerializable jsonSerializable, ObjectMapper om) {
        jsonSerializable.setMapper(om);
    }

    public static void setTimestamp(Resource resource, OffsetDateTime date) {
        resource.timestamp(date);
    }
    
    public static CosmosResponseDiagnostics createCosmosResponseDiagnostics() {
        return new CosmosResponseDiagnostics();
    }

    public static void recordResponse(CosmosResponseDiagnostics cosmosResponseDiagnostics,
                                      RxDocumentServiceRequest request, StoreResult storeResult) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().recordResponse(request, storeResult);
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

    public static void setContactedReplicas(CosmosResponseDiagnostics cosmosResponseDiagnostics, List<URI> contactedReplicas) {
        cosmosResponseDiagnostics.clientSideRequestStatistics().setContactedReplicas(contactedReplicas);
    }

    public static Set<URI> getFailedReplicas(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        return cosmosResponseDiagnostics.clientSideRequestStatistics().getFailedReplicas();
    }

    public static ConcurrentMap<String, QueryMetrics> queryMetricsFromFeedResponse(FeedResponse feedResponse) {
        return feedResponse.queryMetrics();
    }
}
