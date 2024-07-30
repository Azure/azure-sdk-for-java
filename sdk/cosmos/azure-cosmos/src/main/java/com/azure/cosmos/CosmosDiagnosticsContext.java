// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.CosmosDiagnosticsSystemUsageSnapshot;
import com.azure.cosmos.implementation.DistinctClientSideRequestStatisticsCollection;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

/**
 * This class provides metadata for an operation in the Cosmos DB SDK that can be used
 * by diagnostic handlers
 */
public final class CosmosDiagnosticsContext {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final static ObjectMapper mapper = Utils.getSimpleObjectMapper();

    private final String spanName;
    private final String accountName;
    private final String endpoint;
    private final String databaseName;
    private final String collectionName;
    private final ResourceType resourceType;
    private final String resourceTypeString;
    private final OperationType operationType;
    private final String operationTypeString;
    private final ConsistencyLevel consistencyLevel;
    private final ConcurrentLinkedDeque<CosmosDiagnostics> diagnostics;
    private final Integer maxItemCount;
    private final CosmosDiagnosticsThresholds thresholds;
    private final String operationId;
    private final String trackingId;

    private final String connectionMode;

    private final String userAgent;
    private Throwable finalError;
    private Instant startTime = null;
    private Duration duration = null;
    private int statusCode = 0;
    private int subStatusCode = 0;
    private final AtomicInteger actualItemCount = new AtomicInteger(-1);
    private float totalRequestCharge = 0;
    private int maxRequestSize = 0;
    private int maxResponseSize = 0;
    private String cachedRequestDiagnostics = null;
    private final AtomicBoolean isCompleted = new AtomicBoolean(false);

    private Map<String, Object> systemUsage;

    private Double samplingRateSnapshot;

    private boolean isSampledOut;

    private ArrayList<CosmosDiagnosticsRequestInfo> requestInfo = null;

    private final Integer sequenceNumber;

    private String queryStatement;
    private OverridableRequestOptions requestOptions;

    CosmosDiagnosticsContext(
        String spanName,
        String accountName,
        String endpoint,
        String databaseName,
        String collectionName,
        ResourceType resourceType,
        OperationType operationType,
        String operationId,
        ConsistencyLevel consistencyLevel,
        Integer maxItemCount,
        CosmosDiagnosticsThresholds thresholds,
        String trackingId,
        String connectionMode,
        String userAgent,
        Integer sequenceNumber,
        String queryStatement,
        OverridableRequestOptions requestOptions) {

        checkNotNull(spanName, "Argument 'spanName' must not be null.");
        checkNotNull(accountName, "Argument 'accountName' must not be null.");
        checkNotNull(endpoint, "Argument 'endpoint' must not be null.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(operationType, "Argument 'operationType' must not be null.");
        checkNotNull(consistencyLevel, "Argument 'consistencyLevel' must not be null.");
        checkNotNull(thresholds, "Argument 'thresholds' must not be null.");
        checkNotNull(connectionMode, "Argument 'connectionMode' must not be null.");
        checkNotNull(userAgent, "Argument 'userAgent' must not be null.");

        this.spanName = spanName;
        this.accountName = accountName;
        this.endpoint = endpoint;
        this.databaseName = databaseName != null ? databaseName : "";
        this.collectionName = collectionName != null ? collectionName : "";
        this.resourceType = resourceType;
        this.resourceTypeString = resourceType.toString();
        this.operationType = operationType;
        this.operationTypeString = operationType.toString();
        this.operationId = operationId != null ? operationId : "";
        this.diagnostics = new ConcurrentLinkedDeque<>();
        this.consistencyLevel = consistencyLevel;
        this.maxItemCount = maxItemCount;
        this.thresholds = thresholds;
        this.trackingId = trackingId;
        this.userAgent = userAgent;
        this.connectionMode = connectionMode;
        this.sequenceNumber = sequenceNumber;
        this.isSampledOut = false;
        this.queryStatement = queryStatement;
        this.requestOptions = requestOptions;
    }

    /**
     * The name of the account related to the operation
     * @return the name of the account related to the operation
     */
    public String getAccountName() {
        return this.accountName;
    }

    String getEndpoint() { return this.endpoint; }

    /**
     * The name of the database related to the operation
     * @return the name of the database related to the operation
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * The name of the container related to the operation
     * @return the name of the collection related to the operation
     */
    public String getContainerName() {
        return this.collectionName;
    }

    /**
     * The resource type of the operation
     * @return the resource type of the operation
     */
    public String getResourceType() {
        return this.resourceTypeString;
    }

    ResourceType getResourceTypeInternal() {
        return this.resourceType;
    }

    /**
     * The operation type of the operation
     * @return the operation type of the operation
     */
    public String getOperationType() {
        return this.operationTypeString;
    }

    /**
     * The trackingId of a write operation. Will be null for read-/query- or feed operations or when non-idempotent
     * writes are disabled for writes or only enabled without trackingId propagation.
     * @return the trackingId of an operation
     */
    public String getTrackingId() {
        return this.trackingId;
    }

    /**
     * A flag indicating whether the operation is a point operation or not.
     * @return a flag indicating whether the operation is a point operation or not.
     */
    public boolean isPointOperation() {
        return this.operationType.isPointOperation();
    }

    OperationType getOperationTypeInternal() {
        return this.operationType;
    }

    /**
     * The operation identifier of the operation - this can be used to
     * add a dimension for feed operations - like queries -
     * so, metrics and diagnostics can be separated for different query types etc.
     * @return the operation identifier of the operation
     */
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * For feed operations the sequence number allows identifying the order of diagnostics. For each page produced
     * in the page flux the sequence number will be incremented by 1. For point operations the sequence number
     * is always null.
     * @return null for point operations or the monotonically increasing sequence number of pages/diagnostics
     */
    Integer getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     * The effective consistency level of the operation
     * @return the effective consistency level of the operation
     */
    public ConsistencyLevel getEffectiveConsistencyLevel() {
        return this.consistencyLevel;
    }

    /**
     * The max. number of items requested in a feed operation
     * @return the max. number of items requested in a feed operation. Will be null for point operations.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * The actual number of items returned by a feed operation
     * @return the actual number of items returned by a feed operation. Will be null for point operations.
     */
    public Integer getActualItemCount() {
        int snapshot = this.actualItemCount.get();
        if (snapshot < 0) {
            return null;
        }
        return snapshot;
    }

    /**
     * The span name as a logical identifier for an operation
     * @return the span name as a logical identifier for an operation
     */
    String getSpanName() {
        return this.spanName;
    }

    /**
     * The query statement send by client
     * @return the query statement
     */
    public String getQueryStatement() {
        return this.queryStatement;
    }

    /**
     * Indicates whether the latency, request charge or payload size of the operation exceeded the given threshold
     * @return a flag indicating whether the latency, request charge or payload size of the operation
     * exceeded its threshold.
     */
    public boolean isThresholdViolated() {
        if (!this.isCompleted()) {
            return false;
        }

        if (this.thresholds.isFailureCondition(this.statusCode, this.subStatusCode)) {
            return true;
        }

        if (this.operationType.isPointOperation()) {
            if (Duration.ZERO.equals(this.thresholds.getPointOperationLatencyThreshold())
                || this.thresholds.getPointOperationLatencyThreshold().compareTo(this.duration) < 0) {
                return true;
            }
        } else {
            if (Duration.ZERO.equals(this.thresholds.getNonPointOperationLatencyThreshold())
            || this.thresholds.getNonPointOperationLatencyThreshold().compareTo(this.duration) < 0) {
                return true;
            }
        }

        if (this.thresholds.getRequestChargeThreshold() < this.totalRequestCharge) {
            return true;
        }

        return this.thresholds.getPayloadSizeThreshold() < Math.max(this.maxRequestSize, this.maxResponseSize);
    }

    void addDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        checkNotNull(cosmosDiagnostics, "Argument 'cosmosDiagnostics' must not be null.");
        if (cosmosDiagnostics.getDiagnosticsContext() == this) {
            return;
        }

        if (cosmosDiagnostics.getFeedResponseDiagnostics() != null &&
            !diagAccessor.isDiagnosticsCapturedInPagedFlux(cosmosDiagnostics).get()) {

            return;
        }

        synchronized (this.spanName) {
            if (this.samplingRateSnapshot != null) {
                diagAccessor.setSamplingRateSnapshot(cosmosDiagnostics, this.samplingRateSnapshot);
            }
            this.addRequestSize(diagAccessor.getRequestPayloadSizeInBytes(cosmosDiagnostics));
            this.addResponseSize(diagAccessor.getTotalResponsePayloadSizeInBytes(cosmosDiagnostics));
            this.diagnostics.add(cosmosDiagnostics);
            this.cachedRequestDiagnostics = null;
            this.requestInfo = null;
            cosmosDiagnostics.setDiagnosticsContext(this);
        }
    }

    Collection<ClientSideRequestStatistics> getDistinctCombinedClientSideRequestStatistics() {
        DistinctClientSideRequestStatisticsCollection combinedClientSideRequestStatistics =
            new DistinctClientSideRequestStatisticsCollection();
        for (CosmosDiagnostics diagnostics: this.getDiagnostics()) {
            combinedClientSideRequestStatistics.addAll(
                diagnostics.getClientSideRequestStatistics());

            FeedResponseDiagnostics feedResponseDiagnostics =
                diagnostics.getFeedResponseDiagnostics();
            if (feedResponseDiagnostics != null) {
                combinedClientSideRequestStatistics.addAll(
                    feedResponseDiagnostics.getClientSideRequestStatistics());
            }
        }

        return combinedClientSideRequestStatistics;
    }

    /**
     * The final status code of the operation (possibly after retries)
     * @return the final status code of the operation (possibly after retries)
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * The final sub-status code of the operation (possibly after retries)
     * @return the final sub-status code of the operation (possibly after retries)
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * The final error when the operation failed
     * @return the final error when the operation failed
     */
    public Throwable getFinalError() {
        return this.finalError;
    }

    /**
     * The max. request payload size in bytes
     * @return the max. request payload size in bytes
     */
    public int getMaxRequestPayloadSizeInBytes() {
        return this.maxRequestSize;
    }

    /**
     * The max. response payload size in bytes.
     * @return the max. response payload size in bytes
     */
    public int getMaxResponsePayloadSizeInBytes() {
        return this.maxResponseSize;
    }

    /**
     * The total request charge across all retries.
     * @return the total request charge across all retries.
     */
    public float getTotalRequestCharge() {
        return this.totalRequestCharge;
    }

    /**
     * Returns the set of contacted regions
     * @return the set of contacted regions
     */
    public Set<String> getContactedRegionNames() {
        TreeSet<String> regionsContacted = new TreeSet<>();
        if (this.diagnostics == null) {
            return regionsContacted;
        }

        for (CosmosDiagnostics d: this.diagnostics) {
            regionsContacted.addAll(d.getContactedRegionNames());
        }

        return regionsContacted;
    }

    /**
     * Returns the system usage
     * NOTE: this information is not included in the json representation returned from {@link #toJson()} because it
     * is usually only relevant when thresholds are violated, in which case the entire diagnostics json-string is
     * included. Calling this method will lazily collect the system usage - which can be useful when writing
     * a custom {@link CosmosDiagnosticsHandler}
     * @return the system usage
     */
    public Map<String, Object> getSystemUsage() {
        synchronized (this.spanName) {
            Map<String, Object> snapshot = this.systemUsage;
            if (snapshot != null) {
                return snapshot;
            }

            return this.systemUsage = ClientSideRequestStatistics.fetchSystemInformation().toMap();
        }
    }

    /**
     * Returns the number of retries and/or attempts for speculative processing.
     * @return the number of retries and/or attempts for speculative processing.
     */
    public int getRetryCount() {
        if (this.diagnostics == null) {
            return 0;
        }

        int totalRetryCount = 0;
        for (ClientSideRequestStatistics c: this.getDistinctCombinedClientSideRequestStatistics()) {
            totalRetryCount += getRetryCount(c);
        }

        return Math.max(0, totalRetryCount);
    }

    private int getRetryCount(ClientSideRequestStatistics c) {
        if (c == null || c.getRetryContext() == null) {
            return 0;
        }

        return c.getRetryContext().getRetryCount();
    }

    void addRequestCharge(float requestCharge) {
        synchronized (this.spanName) {
            this.totalRequestCharge += requestCharge;
        }
    }

    void addRequestSize(int bytes) {
        synchronized (this.spanName) {
            this.maxRequestSize = Math.max(this.maxRequestSize, bytes);
        }
    }

    void addResponseSize(int bytes) {
        synchronized (this.spanName) {
            this.maxResponseSize = Math.max(this.maxResponseSize, bytes);
        }
    }

    /**
     * The diagnostic records for service interactions within the scope of this SDK operation
     * @return the diagnostic records for service interactions within the scope of this SDK operation
     */
    public Collection<CosmosDiagnostics> getDiagnostics() {
        return this.diagnostics;
    }

    /**
     * Returns a flag indicating whether the operation has been completed yet.
     * @return a flag indicating whether the operation has been completed yet.
     */
    public boolean isCompleted() {
        return this.isCompleted.get();
    }

    /**
     * The total end-to-end duration of the operation.
     * @return the total end-to-end duration of the operation.
     */
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * A flag indicating whether the operation should be considered failed or not based on the status code handling
     * rules in {@link CosmosDiagnosticsThresholds#setFailureHandler(java.util.function.BiPredicate)}
     * @return a flag indicating whether the operation should be considered failed or not
     */
    public boolean isFailure() {
        if (!this.isCompleted()) {
            return false;
        }

        return this.thresholds.isFailureCondition(this.statusCode, this.subStatusCode);
    }

    void startOperation() {
        synchronized (this.spanName) {
            checkState(
                this.startTime == null,
                "Method 'startOperation' must not be called multiple times.");
            this.startTime = Instant.now();

            this.cachedRequestDiagnostics = null;
        }
    }

    boolean endOperation(int statusCode,
                                      int subStatusCode,
                                      Integer actualItemCount,
                                      Double requestCharge,
                                      CosmosDiagnostics diagnostics,
                                      Throwable finalError) {
        synchronized (this.spanName) {
            boolean hasCompletedOperation = this.isCompleted.compareAndSet(false, true);
            if (hasCompletedOperation) {
                this.recordOperation(
                    statusCode, subStatusCode, actualItemCount, requestCharge, diagnostics, finalError);
            }

            return hasCompletedOperation;
        }
    }

    void recordOperation(int statusCode,
                                      int subStatusCode,
                                      Integer actualItemCount,
                                      Double requestCharge,
                                      CosmosDiagnostics diagnostics,
                                      Throwable finalError) {

        synchronized (this.spanName) {
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
            this.finalError = finalError;
            if (actualItemCount != null) {
                if (!this.actualItemCount.compareAndSet(-1, actualItemCount)) {
                    this.actualItemCount.addAndGet(actualItemCount);
                }
            }

            if (this.startTime != null) {
                this.duration = Duration.between(this.startTime, Instant.now());
            } else {
                this.duration = null;
            }

            if (diagnostics != null) {
                this.addDiagnostics(diagnostics);
            }

            if (requestCharge != null) {
                this.addRequestCharge(requestCharge.floatValue());
            }

            this.cachedRequestDiagnostics = null;
        }
    }

    void setSamplingRateSnapshot(double samplingRate, boolean isSampledOut) {
        synchronized (this.spanName) {
            this.samplingRateSnapshot = samplingRate;
            this.isSampledOut = isSampledOut;
            for (CosmosDiagnostics d : this.diagnostics) {
                diagAccessor.setSamplingRateSnapshot(d, samplingRate);
            }
        }
    }

    boolean isSampledOut() {
        return this.isSampledOut;
    }

    String getRequestDiagnostics() {
        ObjectNode ctxNode = mapper.createObjectNode();

        ctxNode.put("spanName", this.spanName);
        ctxNode.put("account", this.accountName);
        ctxNode.put("db", this.databaseName);
        if (!this.collectionName.isEmpty()) {
            ctxNode.put("container", this.collectionName);
        }
        ctxNode.put("resource", this.resourceType.toString());
        ctxNode.put("operation", this.operationType.toString());
        if (!this.operationId.isEmpty()) {
            ctxNode.put("operationId", this.operationId);
        }
        if (this.trackingId != null && !this.trackingId.isEmpty()) {
            ctxNode.put("trackingId", this.trackingId);
        }
        if (this.sequenceNumber != null) {
            ctxNode.put("sequenceNumber", this.sequenceNumber);
        }
        ctxNode.put("consistency", this.consistencyLevel.toString());
        ctxNode.put("status", this.statusCode);
        if (this.subStatusCode != 0) {
            ctxNode.put("subStatus", this.subStatusCode);
        }
        ctxNode.put("RUs", this.totalRequestCharge);
        ctxNode.put("maxRequestSizeInBytes", this.maxRequestSize);
        ctxNode.put("maxResponseSizeInBytes", this.maxResponseSize);

        if (this.requestOptions != null && this.requestOptions.getKeywordIdentifiers() != null) {
            ctxNode.put("keywordIdentifiers", String.join(",", this.requestOptions.getKeywordIdentifiers()));
        }

        if (this.maxItemCount != null) {
            ctxNode.put("maxItems", this.maxItemCount);
        }

        if (this.actualItemCount.get() >= 0) {
            ctxNode.put("actualItems", this.actualItemCount.get());
        }

        if (this.queryStatement != null && queryStatement.length() > 0) {
            ctxNode.put("queryStatement", this.queryStatement);
        }

        if (this.finalError != null) {
            if (this.finalError instanceof CosmosException) {
                ctxNode.put("exception", ((CosmosException)this.finalError).toString(false));
            } else {
                ctxNode.put("exception", this.finalError.getMessage());
            }
        }

        if (this.diagnostics != null && this.diagnostics.size() > 0) {
            ArrayNode diagnosticsNode = ctxNode.putArray("diagnostics");
            for (CosmosDiagnostics d: this.diagnostics) {

                ObjectNode childNode = mapper.createObjectNode();
                d.fillCosmosDiagnostics(childNode, null);

                diagnosticsNode.add(childNode);
            }
        }

        try {
            return mapper.writeValueAsString(ctxNode);
        } catch (JsonProcessingException e) {
            ctxNode = mapper.createObjectNode();
            ctxNode.put("exception", e.toString());
            try {
                return mapper.writeValueAsString(ctxNode);
            } catch (JsonProcessingException ex) {
                // should never happen
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Returns a json-string representation of the diagnostics context. This string uses json format for readability,
     * but it should be treated as an opaque string - the format can and will change between SDK versions - for any
     * automatic processing of the diagnostics information the get-properties of public API should be used.
     * @return a json-string representation of the diagnostics context. This string uses json format for readability,
     *      but it should be treated as an opaque string - the format can and will change between SDK versions -
     *      for any
     *      automatic processing of the diagnostics information the get-properties of public API should be used.
     */
    public String toJson() {
        String snapshot = this.cachedRequestDiagnostics;
        if (snapshot != null) {
            return snapshot;
        }

        synchronized (this.spanName) {
            snapshot = this.cachedRequestDiagnostics;
            if (snapshot != null) {
                return snapshot;
            }

            this.systemUsage = ClientSideRequestStatistics.fetchSystemInformation().toMap();
            return this.cachedRequestDiagnostics = getRequestDiagnostics();
        }
    }

    /**
     * Gets the UserAgent header value used by the client issuing this operation
     * NOTE: this information is not included in the json representation returned from {@link #toJson()} because it
     * is usually only relevant when thresholds are violated, in which case the entire diagnostics json-string is
     * included.
     * @return the UserAgent header value used for the client that issued this operation
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    /**
     * Returns the connection mode used in the client.
     * NOTE: this information is not included in the json representation returned from {@link #toJson()} because it
     * is usually only relevant when thresholds are violated, in which case the entire diagnostics json-string is
     * included.
     * @return the connection mode used in the client.
     */
    public String getConnectionMode() {
        return this.connectionMode;
    }

    private static void addRequestInfoForGatewayStatistics(
        ClientSideRequestStatistics requestStats,
        List<CosmosDiagnosticsRequestInfo> requestInfo) {

        List<ClientSideRequestStatistics.GatewayStatistics> gatewayStatsList = requestStats.getGatewayStatisticsList();

        if (gatewayStatsList == null || gatewayStatsList.size() == 0) {
            return;
        }

        for (ClientSideRequestStatistics.GatewayStatistics gatewayStats : gatewayStatsList) {
            CosmosDiagnosticsRequestInfo info = new CosmosDiagnosticsRequestInfo(
                requestStats.getActivityId(),
                null,
                gatewayStats.getPartitionKeyRangeId(),
                gatewayStats.getResourceType() + ":" + gatewayStats.getOperationType(),
                requestStats.getRequestStartTimeUTC(),
                requestStats.getDuration(),
                null,
                gatewayStats.getRequestCharge(),
                gatewayStats.getResponsePayloadSizeInBytes(),
                gatewayStats.getStatusCode(),
                gatewayStats.getSubStatusCode(),
                new ArrayList<>()
            );

            requestInfo.add(info);
        }
    }

    private static void addRequestInfoForStoreResponses(
        ClientSideRequestStatistics requestStats,
        List<CosmosDiagnosticsRequestInfo> requestInfo,
        Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponses) {

        for (ClientSideRequestStatistics.StoreResponseStatistics responseStats: storeResponses) {

            StoreResultDiagnostics resultDiagnostics = responseStats.getStoreResult();
            if (resultDiagnostics == null) {
                continue;
            }

            StoreResponseDiagnostics responseDiagnostics = resultDiagnostics.getStoreResponseDiagnostics();

            String partitionId = null;
            String[] partitionAndReplicaId = resultDiagnostics.getPartitionAndReplicaId();
            if (partitionAndReplicaId.length == 2) {
                partitionId = partitionAndReplicaId[0];
            }

            List<CosmosDiagnosticsRequestEvent> events = new ArrayList<>();
            String pkRangeId = "";
            double requestCharge = 0;
            int responsePayloadLength = 0;
            int statusCode = 0;
            int subStatusCode = 0;
            String activityId = requestStats.getActivityId();
            if (responseDiagnostics != null) {
                activityId = responseDiagnostics.getActivityId();
                requestCharge = responseDiagnostics.getRequestCharge();
                responsePayloadLength = responseDiagnostics.getResponsePayloadLength();
                statusCode = responseDiagnostics.getStatusCode();
                subStatusCode = responseDiagnostics.getSubStatusCode();
                if (responseDiagnostics.getPartitionKeyRangeId() != null) {
                    pkRangeId = responseDiagnostics.getPartitionKeyRangeId();
                }
                RequestTimeline timeline = responseDiagnostics.getRequestTimeline();
                timeline.forEach( e -> {
                    if (e.getStartTime() != null && e.getDuration() != null && !e.getDuration().equals(Duration.ZERO)) {
                        events.add(new CosmosDiagnosticsRequestEvent(e.getStartTime(), e.getDuration(), e.getName()));
                    }
                });
            }

            Duration backendLatency = null;
            if (resultDiagnostics.getBackendLatencyInMs() != null) {
                backendLatency = Duration.ofNanos((long)(resultDiagnostics.getBackendLatencyInMs() * 1000000d));
            }

            CosmosDiagnosticsRequestInfo info = new CosmosDiagnosticsRequestInfo(
                activityId,
                partitionId,
                pkRangeId,
                responseStats.getRequestResourceType() + ":" + responseStats.getRequestOperationType(),
                requestStats.getRequestStartTimeUTC(),
                responseStats.getDuration(),
                backendLatency,
                requestCharge,
                responsePayloadLength,
                statusCode,
                subStatusCode,
                events
            );

            requestInfo.add(info);
        }
    }

    private void addRequestInfoForAddressResolution(
        ClientSideRequestStatistics requestStats,
        List<CosmosDiagnosticsRequestInfo> requestInfo,
        Map<String, ClientSideRequestStatistics.AddressResolutionStatistics> addressResolutionStatisticsMap
    ) {
        if (addressResolutionStatisticsMap == null || addressResolutionStatisticsMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, ClientSideRequestStatistics.AddressResolutionStatistics> current
            : addressResolutionStatisticsMap.entrySet()) {

            ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics = current.getValue();
            String addressResolutionActivityId = current.getKey();

            if (addressResolutionStatistics.isInflightRequest() ||
                addressResolutionStatistics.getEndTimeUTC() == null) {

                // skipping inflight or failed address resolution statistics
                // capturing error count etc. won't make sense here - request diagnostic
                // logs are the right way to debug those - not metrics
                continue;
            }

            Duration latency = Duration.between(
                addressResolutionStatistics.getStartTimeUTC(),
                addressResolutionStatistics.getEndTimeUTC());

            StringBuilder sb = new StringBuilder();
            sb.append("AddressResolution|");
            sb.append(addressResolutionStatistics.getTargetEndpoint());
            sb.append("|");
            if (addressResolutionStatistics.isForceRefresh()) {
                sb.append("1|");
            } else {
                sb.append("0|");
            }

            if (addressResolutionStatistics.isForceCollectionRoutingMapRefresh()) {
                sb.append("1");
            } else {
                sb.append("0");
            }

            CosmosDiagnosticsRequestInfo info = new CosmosDiagnosticsRequestInfo(
                addressResolutionActivityId,
                null,
                null,
                sb.toString(),
                addressResolutionStatistics.getStartTimeUTC(),
                latency,
                null,
                0,
                0,
                0,
                0,
                new ArrayList<>()
            );

            requestInfo.add(info);
        }
    }

    /**
     * Gets a collection of {@link CosmosDiagnosticsRequestInfo} records providing more information about
     * individual requests issued in the transport layer to process this operation.
     * NOTE: this information is not included in the json representation returned from {@link #toJson()} because it
     * is usually only relevant when thresholds are violated, in which case the entire diagnostics json-string is
     * included. Calling this method will lazily collect the user agent - which can be useful when writing
     * a custom {@link CosmosDiagnosticsHandler}
     * @return a collection of {@link CosmosDiagnosticsRequestInfo} records providing more information about
     * individual requests issued in the transport layer to process this operation.
     */
    public Collection<CosmosDiagnosticsRequestInfo> getRequestInfo() {
        synchronized (this.spanName) {
            ArrayList<CosmosDiagnosticsRequestInfo> snapshot = this.requestInfo;
            if (snapshot != null) {
                return snapshot;
            }

            snapshot = new ArrayList<>();
            for (ClientSideRequestStatistics requestStats: this.getDistinctCombinedClientSideRequestStatistics()) {
                addRequestInfoForStoreResponses(
                    requestStats,
                    snapshot,
                    requestStats.getResponseStatisticsList());

                addRequestInfoForStoreResponses(
                    requestStats,
                    snapshot,
                    requestStats.getSupplementalResponseStatisticsList());

                addRequestInfoForGatewayStatistics(requestStats, snapshot);

                addRequestInfoForAddressResolution(
                    requestStats,
                    snapshot,
                    requestStats.getAddressResolutionStatistics());
            }

            this.requestInfo = snapshot;

            return snapshot;
        }
    }

    /**
     * Gets the custom ids.
     *
     * @return the custom ids.
     */
    public Set<String> getKeywordIdentifiers() {
        return this.requestOptions.getKeywordIdentifiers();
    }

    OverridableRequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    void setRequestOptions(OverridableRequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .setCosmosDiagnosticsContextAccessor(
                new ImplementationBridgeHelpers
                    .CosmosDiagnosticsContextHelper
                    .CosmosDiagnosticsContextAccessor() {

                    @Override
                    public CosmosDiagnosticsContext create(String spanName, String account, String endpoint,
                                                           String databaseId,String containerId,
                                                           ResourceType resourceType, OperationType operationType,
                                                           String operationId,
                                                           ConsistencyLevel consistencyLevel, Integer maxItemCount,
                                                           CosmosDiagnosticsThresholds thresholds, String trackingId,
                                                           String connectionMode, String userAgent,
                                                           Integer sequenceNumber,
                                                           String queryStatement,
                                                           OverridableRequestOptions requestOptions) {

                        return new CosmosDiagnosticsContext(
                            spanName,
                            account,
                            endpoint,
                            databaseId,
                            containerId,
                            resourceType,
                            operationType,
                            operationId,
                            consistencyLevel,
                            maxItemCount,
                            thresholds,
                            trackingId,
                            connectionMode,
                            userAgent,
                            sequenceNumber,
                            queryStatement,
                            requestOptions
                            );
                    }

                    @Override
                    public OverridableRequestOptions getRequestOptions(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getRequestOptions();
                    }

                    @Override
                    public void setRequestOptions(CosmosDiagnosticsContext ctx, OverridableRequestOptions requestOptions) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.setRequestOptions(requestOptions);
                    }

                    @Override
                    public CosmosDiagnosticsSystemUsageSnapshot createSystemUsageSnapshot(String cpu, String used, String available, int cpuCount) {
                        return new CosmosDiagnosticsSystemUsageSnapshot(cpu, used, available, cpuCount);
                    }

                    @Override
                    public void startOperation(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.startOperation();
                    }

                    @Override
                    public void recordOperation(CosmosDiagnosticsContext ctx, int statusCode, int subStatusCode,
                                                Integer actualItemCount, Double requestCharge,
                                                CosmosDiagnostics diagnostics, Throwable finalError) {
                        ctx.recordOperation(statusCode, subStatusCode, actualItemCount, requestCharge, diagnostics, finalError);
                    }

                    @Override
                    public boolean endOperation(CosmosDiagnosticsContext ctx, int statusCode, int subStatusCode,
                                             Integer actualItemCount, Double requestCharge,
                                             CosmosDiagnostics diagnostics, Throwable finalError) {

                        return ctx.endOperation(statusCode, subStatusCode, actualItemCount, requestCharge, diagnostics, finalError);
                    }

                    @Override
                    public void addRequestCharge(CosmosDiagnosticsContext ctx, float requestCharge) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addRequestCharge(requestCharge);
                    }

                    @Override
                    public void addRequestSize(CosmosDiagnosticsContext ctx, int bytes) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addRequestSize(bytes);
                    }

                    @Override
                    public void addResponseSize(CosmosDiagnosticsContext ctx, int bytes) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addResponseSize(bytes);
                    }

                    @Override
                    public void addDiagnostics(CosmosDiagnosticsContext ctx, CosmosDiagnostics diagnostics) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        checkNotNull(ctx, "Argument 'diagnostics' must not be null.");
                        ctx.addDiagnostics(diagnostics);
                    }

                    @Override
                    public Collection<CosmosDiagnostics> getDiagnostics(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getDiagnostics();
                    }

                    @Override
                    public ResourceType getResourceType(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getResourceTypeInternal();
                    }

                    @Override
                    public OperationType getOperationType(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getOperationTypeInternal();
                    }

                    @Override
                    public String getEndpoint(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getEndpoint();
                    }

                    @Override
                    public Collection<ClientSideRequestStatistics> getDistinctCombinedClientSideRequestStatistics(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getDistinctCombinedClientSideRequestStatistics();
                    }

                    @Override
                    public String getSpanName(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getSpanName();
                    }

                    @Override
                    public void setSamplingRateSnapshot(CosmosDiagnosticsContext ctx, double samplingRate, boolean isSampledOut) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.setSamplingRateSnapshot(samplingRate, isSampledOut);
                    }

                    @Override
                    public Integer getSequenceNumber(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getSequenceNumber();
                    }

                    @Override
                    public boolean isEmptyCompletion(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        Integer sequenceNumber = ctx.getSequenceNumber();
                        if (sequenceNumber == null || sequenceNumber == 1) {
                            return false;
                        }

                        return true;
                    }

                    @Override
                    public String getQueryStatement(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getQueryStatement();
                    }
                });
    }
}
