// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.circuitBreaker.LocationSpecificHealthContext;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@JsonSerialize(using = ClientSideRequestStatistics.ClientSideRequestStatisticsSerializer.class)
public class ClientSideRequestStatistics {
    private static final int MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING = 10;
    private final DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig;
    private String activityId;
    private Collection<StoreResponseStatistics> responseStatisticsList;
    private Collection<StoreResponseStatistics> supplementalResponseStatisticsList;
    private Map<String, AddressResolutionStatistics> addressResolutionStatistics;

    private List<URI> contactedReplicas;
    private Set<URI> failedReplicas;
    private Instant requestStartTimeUTC;
    private Instant requestEndTimeUTC;
    private Set<String> regionsContacted;
    private NavigableSet<RegionWithContext> regionsContactedWithContext;
    private Set<URI> locationEndpointsContacted;
    private RetryContext retryContext;
    private FaultInjectionRequestContext requestContext;
    private List<GatewayStatistics> gatewayStatisticsList;
    private MetadataDiagnosticsContext metadataDiagnosticsContext;
    private SerializationDiagnosticsContext serializationDiagnosticsContext;
    private int requestPayloadSizeInBytes = 0;
    private final String userAgent;

    private double samplingRateSnapshot = 1;
    private long approximateInsertionCountInBloomFilter = 0;
    private Set<String> keywordIdentifiers;

    public ClientSideRequestStatistics(DiagnosticsClientContext diagnosticsClientContext) {
        this.diagnosticsClientConfig = diagnosticsClientContext.getConfig();
        this.requestStartTimeUTC = Instant.now();
        this.requestEndTimeUTC = Instant.now();
        this.responseStatisticsList = new ConcurrentLinkedDeque<>();
        this.supplementalResponseStatisticsList = new ConcurrentLinkedDeque<>();
        this.gatewayStatisticsList = new ArrayList<>();
        this.addressResolutionStatistics = new HashMap<>();
        this.contactedReplicas = Collections.synchronizedList(new ArrayList<>());
        this.failedReplicas = Collections.synchronizedSet(new HashSet<>());
        this.regionsContacted = Collections.synchronizedSet(new HashSet<>());
        this.regionsContactedWithContext = Collections.synchronizedNavigableSet(new TreeSet<>());
        this.locationEndpointsContacted = Collections.synchronizedSet(new HashSet<>());
        this.metadataDiagnosticsContext = new MetadataDiagnosticsContext();
        this.serializationDiagnosticsContext = new SerializationDiagnosticsContext();
        this.retryContext = new RetryContext();
        this.requestPayloadSizeInBytes = 0;
        this.userAgent = diagnosticsClientContext.getUserAgent();
        this.samplingRateSnapshot = 1;
        this.approximateInsertionCountInBloomFilter = 0;
        this.keywordIdentifiers = new HashSet<>();
    }

    public ClientSideRequestStatistics(ClientSideRequestStatistics toBeCloned) {
        this.diagnosticsClientConfig = toBeCloned.diagnosticsClientConfig;
        this.requestStartTimeUTC = toBeCloned.requestStartTimeUTC;
        this.requestEndTimeUTC = toBeCloned.requestEndTimeUTC;
        this.responseStatisticsList = new ArrayList<>(toBeCloned.responseStatisticsList);
        this.supplementalResponseStatisticsList = new ArrayList<>(toBeCloned.supplementalResponseStatisticsList);
        this.gatewayStatisticsList = new ArrayList<>(toBeCloned.gatewayStatisticsList);
        this.addressResolutionStatistics = new HashMap<>(toBeCloned.addressResolutionStatistics);
        this.contactedReplicas = Collections.synchronizedList(new ArrayList<>(toBeCloned.contactedReplicas));
        this.failedReplicas = Collections.synchronizedSet(new HashSet<>(toBeCloned.failedReplicas));
        this.regionsContacted = Collections.synchronizedSet(new HashSet<>(toBeCloned.regionsContacted));
        this.regionsContactedWithContext = Collections.synchronizedNavigableSet(new TreeSet<>(toBeCloned.regionsContactedWithContext));
        this.locationEndpointsContacted = Collections.synchronizedSet(
            new HashSet<>(toBeCloned.locationEndpointsContacted));
        this.metadataDiagnosticsContext = new MetadataDiagnosticsContext(toBeCloned.metadataDiagnosticsContext);
        this.serializationDiagnosticsContext =
            new SerializationDiagnosticsContext(toBeCloned.serializationDiagnosticsContext);
        this.retryContext = new RetryContext(toBeCloned.retryContext);
        this.requestPayloadSizeInBytes = toBeCloned.requestPayloadSizeInBytes;
        this.userAgent = toBeCloned.userAgent;
        this.samplingRateSnapshot = toBeCloned.samplingRateSnapshot;
        this.approximateInsertionCountInBloomFilter = toBeCloned.approximateInsertionCountInBloomFilter;
        this.keywordIdentifiers = toBeCloned.keywordIdentifiers;
    }

    @JsonIgnore
    public Duration getDuration() {
        if (requestStartTimeUTC == null ||
            requestEndTimeUTC == null ||
            requestEndTimeUTC.isBefore(requestStartTimeUTC)) {
            return null;
        }

        if (requestStartTimeUTC == requestEndTimeUTC) {
            return Duration.ZERO;
        }

        return Duration.between(requestStartTimeUTC, requestEndTimeUTC);
    }

    public Instant getRequestStartTimeUTC() {
        return requestStartTimeUTC;
    }

    public Instant getRequestEndTimeUTC() {
        return requestEndTimeUTC;
    }

    public DiagnosticsClientContext.DiagnosticsClientConfig getDiagnosticsClientConfig() {
        return diagnosticsClientConfig;
    }

    public void recordResponse(RxDocumentServiceRequest request, StoreResultDiagnostics storeResultDiagnostics, GlobalEndpointManager globalEndpointManager) {
        Objects.requireNonNull(request, "request is required and cannot be null.");
        Instant responseTime = Instant.now();

        StoreResponseStatistics storeResponseStatistics = new StoreResponseStatistics();
        storeResponseStatistics.requestStartTimeUTC = this.extractRequestStartTime(storeResultDiagnostics);
        storeResponseStatistics.requestResponseTimeUTC = responseTime;
        storeResponseStatistics.storeResult = storeResultDiagnostics;
        storeResponseStatistics.requestOperationType = request.getOperationType();
        storeResponseStatistics.requestResourceType = request.getResourceType();
        storeResponseStatistics.requestSessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        storeResponseStatistics.e2ePolicyCfg = null;
        storeResponseStatistics.excludedRegions = null;
        activityId = request.getActivityId().toString();

        if (request.getContentLength() > 0) {
            this.requestPayloadSizeInBytes = request.getContentLength();
        } else if (storeResultDiagnostics != null && storeResultDiagnostics.getStoreResponseDiagnostics() != null) {
            this.requestPayloadSizeInBytes = storeResultDiagnostics
                .getStoreResponseDiagnostics()
                .getRntbdRequestLength();
        } else {
            this.requestPayloadSizeInBytes = 0;
        }

        URI locationEndPoint = null;
        if (request.requestContext != null) {

            this.approximateInsertionCountInBloomFilter = request.requestContext.getApproximateBloomFilterInsertionCount();
            storeResponseStatistics.sessionTokenEvaluationResults = request.requestContext.getSessionTokenEvaluationResults();
            storeResponseStatistics.locationToLocationSpecificHealthContext = request.requestContext.getLocationToLocationSpecificHealthContext();

            if (request.requestContext.getEndToEndOperationLatencyPolicyConfig() != null) {
                storeResponseStatistics.e2ePolicyCfg =
                    request.requestContext.getEndToEndOperationLatencyPolicyConfig().toString();
            }

            locationEndPoint = request.requestContext.locationEndpointToRoute;

            List<String> excludedRegions = request.requestContext.getExcludeRegions();
            if (excludedRegions != null && !excludedRegions.isEmpty()) {
                storeResponseStatistics.excludedRegions = String.join(", ", excludedRegions);
            }
            this.keywordIdentifiers = request.requestContext.getKeywordIdentifiers();
        }

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            if (locationEndPoint != null) {
                storeResponseStatistics.regionName =
                    globalEndpointManager.getRegionName(locationEndPoint, request.getOperationType());
                this.regionsContacted.add(storeResponseStatistics.regionName);
                this.locationEndpointsContacted.add(locationEndPoint);
                this.regionsContactedWithContext.add(new RegionWithContext(storeResponseStatistics.regionName, locationEndPoint));
            }

            if (storeResponseStatistics.requestOperationType == OperationType.Head
                || storeResponseStatistics.requestOperationType == OperationType.HeadFeed) {
                this.supplementalResponseStatisticsList.add(storeResponseStatistics);
            } else {
                this.responseStatisticsList.add(storeResponseStatistics);
            }
        }
    }

    public void recordGatewayResponse(
        RxDocumentServiceRequest rxDocumentServiceRequest,
        StoreResponseDiagnostics storeResponseDiagnostics,
        GlobalEndpointManager globalEndpointManager) {

        Instant responseTime = Instant.now();

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            URI locationEndPoint = null;
            if (rxDocumentServiceRequest != null && rxDocumentServiceRequest.requestContext != null) {
                locationEndPoint = rxDocumentServiceRequest.requestContext.locationEndpointToRoute;
                this.approximateInsertionCountInBloomFilter = rxDocumentServiceRequest.requestContext.getApproximateBloomFilterInsertionCount();
                this.keywordIdentifiers = rxDocumentServiceRequest.requestContext.getKeywordIdentifiers();
            }
            this.recordRetryContextEndTime();

            if (locationEndPoint != null) {

                String regionName = globalEndpointManager.getRegionName(locationEndPoint, rxDocumentServiceRequest.getOperationType());

                this.regionsContacted.add(regionName);
                this.locationEndpointsContacted.add(locationEndPoint);

                this.regionsContactedWithContext.add(new RegionWithContext(regionName, locationEndPoint));
            }

            GatewayStatistics gatewayStatistics = new GatewayStatistics();
            if (rxDocumentServiceRequest != null) {
                gatewayStatistics.operationType = rxDocumentServiceRequest.getOperationType();
                gatewayStatistics.resourceType = rxDocumentServiceRequest.getResourceType();
                this.requestPayloadSizeInBytes = rxDocumentServiceRequest.getContentLength();

                if (rxDocumentServiceRequest.requestContext != null) {
                    gatewayStatistics.sessionTokenEvaluationResults = rxDocumentServiceRequest.requestContext.getSessionTokenEvaluationResults();
                    gatewayStatistics.locationToLocationSpecificHealthContext = rxDocumentServiceRequest.requestContext.getLocationToLocationSpecificHealthContext();
                }
            }
            gatewayStatistics.statusCode = storeResponseDiagnostics.getStatusCode();
            gatewayStatistics.subStatusCode = storeResponseDiagnostics.getSubStatusCode();
            gatewayStatistics.sessionToken = storeResponseDiagnostics.getSessionTokenAsString();
            gatewayStatistics.requestCharge = storeResponseDiagnostics.getRequestCharge();
            gatewayStatistics.requestTimeline = storeResponseDiagnostics.getRequestTimeline();
            gatewayStatistics.partitionKeyRangeId = storeResponseDiagnostics.getPartitionKeyRangeId();
            gatewayStatistics.exceptionMessage = storeResponseDiagnostics.getExceptionMessage();
            gatewayStatistics.exceptionResponseHeaders = storeResponseDiagnostics.getExceptionResponseHeaders();
            gatewayStatistics.responsePayloadSizeInBytes = storeResponseDiagnostics.getResponsePayloadLength();
            gatewayStatistics.faultInjectionRuleId = storeResponseDiagnostics.getFaultInjectionRuleId();
            gatewayStatistics.faultInjectionEvaluationResults = storeResponseDiagnostics.getFaultInjectionEvaluationResults();

            this.activityId = storeResponseDiagnostics.getActivityId() != null ? storeResponseDiagnostics.getActivityId() :
                rxDocumentServiceRequest.getActivityId().toString();

            this.gatewayStatisticsList.add(gatewayStatistics);
        }
    }

    public int getRequestPayloadSizeInBytes() {
        return this.requestPayloadSizeInBytes;
    }

    public void mergeMetadataDiagnosticsContext(MetadataDiagnosticsContext other) {
        if (other == null || other.metadataDiagnosticList == null || other.metadataDiagnosticList.isEmpty()) {
            return;
        }

        for (MetadataDiagnosticsContext.MetadataDiagnostics metadataDiagnostics : other.metadataDiagnosticList) {
            this.metadataDiagnosticsContext.addMetaDataDiagnostic(metadataDiagnostics);
        }
    }

    public void mergeSerializationDiagnosticsContext(SerializationDiagnosticsContext other) {
        if (other == null || other.serializationDiagnosticsList == null || other.serializationDiagnosticsList.isEmpty()) {
            return;
        }

        for (SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics : other.serializationDiagnosticsList) {
            this.serializationDiagnosticsContext.addSerializationDiagnostics(serializationDiagnostics);
        }
    }

    public String recordAddressResolutionStart(
        URI targetEndpoint,
        boolean forceRefresh,
        boolean forceCollectionRoutingMapRefresh) {
        String identifier = UUID
            .randomUUID()
            .toString();

        AddressResolutionStatistics resolutionStatistics = new AddressResolutionStatistics();
        resolutionStatistics.startTimeUTC = Instant.now();
        resolutionStatistics.endTimeUTC = null;
        resolutionStatistics.targetEndpoint = targetEndpoint == null ? "<NULL>" : targetEndpoint.toString();
        resolutionStatistics.forceRefresh = forceRefresh;
        resolutionStatistics.forceCollectionRoutingMapRefresh = forceCollectionRoutingMapRefresh;

        synchronized (this) {
            this.addressResolutionStatistics.put(identifier, resolutionStatistics);
        }

        return identifier;
    }

    public void recordAddressResolutionEnd(
        String identifier,
        String exceptionMessage,
        String faultInjectionId,
        List<String> faultInjectionEvaluationResult) {
        if (StringUtils.isEmpty(identifier)) {
            return;
        }
        Instant responseTime = Instant.now();

        synchronized (this) {
            if (!this.addressResolutionStatistics.containsKey(identifier)) {
                throw new IllegalArgumentException("Identifier " + identifier + " does not exist. Please call start "
                    + "before calling end");
            }

            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            AddressResolutionStatistics resolutionStatistics = this.addressResolutionStatistics.get(identifier);
            resolutionStatistics.endTimeUTC = responseTime;
            resolutionStatistics.exceptionMessage = exceptionMessage;
            resolutionStatistics.inflightRequest = false;
            resolutionStatistics.faultInjectionRuleId = faultInjectionId;
            resolutionStatistics.faultInjectionEvaluationResults = faultInjectionEvaluationResult;
        }
    }

    private void mergeContactedReplicas(List<URI> otherContactedReplicas) {
        if (otherContactedReplicas == null) {
            return;
        }

        if (this.contactedReplicas == null || this.contactedReplicas.isEmpty()) {
            this.contactedReplicas = otherContactedReplicas;
            return;
        }

        LinkedHashSet<URI> totalContactedReplicas = new LinkedHashSet<>();
        totalContactedReplicas.addAll(otherContactedReplicas);
        totalContactedReplicas.addAll(this.contactedReplicas);

        this.setContactedReplicas(new ArrayList<>(totalContactedReplicas));
    }

    // Called under lock
    private void mergeSupplementalResponses(Collection<StoreResponseStatistics> other) {
        if (other == null) {
            return;
        }

        if (this.supplementalResponseStatisticsList == null || this.supplementalResponseStatisticsList.isEmpty()) {
            this.supplementalResponseStatisticsList = other;
            return;
        }

        this.supplementalResponseStatisticsList.addAll(other);
    }

    // Called under lock
    private void mergeResponseStatistics(Collection<StoreResponseStatistics> other) {
        if (other == null) {
            return;
        }

        if (this.responseStatisticsList == null || this.responseStatisticsList.isEmpty()) {
            this.responseStatisticsList = other;
            return;
        }

        ArrayList<StoreResponseStatistics> temp = new ArrayList<>(this.responseStatisticsList);
        temp.addAll(other);
        temp.sort(
            (StoreResponseStatistics left, StoreResponseStatistics right) -> {
                if (left == null || left.requestStartTimeUTC == null) {
                    return -1;
                }

                if (right == null || right.requestStartTimeUTC == null) {
                    return 1;
                }
                return left.requestStartTimeUTC.compareTo(right.requestStartTimeUTC);
            }
        );
        this.responseStatisticsList = new ConcurrentLinkedDeque<>(temp);
    }

    private void mergeAddressResolutionStatistics(
        Map<String, AddressResolutionStatistics> otherAddressResolutionStatistics) {
        if (otherAddressResolutionStatistics == null) {
            return;
        }

        if (this.addressResolutionStatistics == null || this.addressResolutionStatistics.isEmpty()) {
            this.addressResolutionStatistics = otherAddressResolutionStatistics;
            return;
        }

        for (Map.Entry<String, AddressResolutionStatistics> pair : otherAddressResolutionStatistics.entrySet()) {
            this.addressResolutionStatistics.putIfAbsent(
                pair.getKey(),
                pair.getValue());
        }
    }

    private void mergeFailedReplica(Set<URI> other) {
        if (other == null) {
            return;
        }

        if (this.failedReplicas == null || this.failedReplicas.isEmpty()) {
            this.failedReplicas = other;
            return;
        }

        for (URI uri : other) {
            this.failedReplicas.add(uri);
        }
    }

    private void mergeLocationEndpointsContacted(Set<URI> other) {
        if (other == null) {
            return;
        }

        if (this.locationEndpointsContacted == null || this.locationEndpointsContacted.isEmpty()) {
            this.locationEndpointsContacted = other;
            return;
        }

        for (URI uri : other) {
            this.locationEndpointsContacted.add(uri);
        }
    }

    private void mergeRegionWithContextSet(NavigableSet<RegionWithContext> other) {
        if (other == null) {
            return;
        }

        if (this.regionsContactedWithContext == null || this.regionsContactedWithContext.isEmpty()) {
            this.regionsContactedWithContext = other;
            return;
        }

        for (RegionWithContext regionWithContext : other) {
            this.regionsContactedWithContext.add(regionWithContext);
        }
    }

    private void mergeRegionsContacted(Set<String> other) {
        if (other == null) {
            return;
        }

        if (this.regionsContacted == null || this.regionsContacted.isEmpty()) {
            this.regionsContacted = other;
            return;
        }

        for (String region : other) {
            this.regionsContacted.add(region);
        }
    }

    private void mergeStartTime(Instant other) {
        if (other == null) {
            return;
        }

        if (this.requestStartTimeUTC == null || this.requestStartTimeUTC.isAfter(other)) {
            this.requestStartTimeUTC = other;
        }
    }

    private void mergeEndTime(Instant other) {
        if (other == null || this.requestEndTimeUTC == null) {
            return;
        }

        if (this.requestEndTimeUTC.isBefore(other)) {
            this.requestEndTimeUTC = other;
        }
    }

    private Instant extractRequestStartTime(StoreResultDiagnostics storeResultDiagnostics) {
        if (storeResultDiagnostics == null
            || storeResultDiagnostics.getStoreResponseDiagnostics() == null) {
            return null;
        }

        RequestTimeline requestTimeline = storeResultDiagnostics.getStoreResponseDiagnostics().getRequestTimeline();

        return requestTimeline != null ? requestTimeline.getRequestStartTimeUTC() : null;
    }

    public void recordContributingPointOperation(ClientSideRequestStatistics other) {
        this.mergeClientSideRequestStatistics(other);
    }

    // Called under lock
    public void mergeClientSideRequestStatistics(ClientSideRequestStatistics other) {
        if (other == null) {
            return;
        }

        this.mergeAddressResolutionStatistics(other.addressResolutionStatistics);
        this.mergeContactedReplicas(other.contactedReplicas);
        this.mergeFailedReplica(other.failedReplicas);
        this.mergeLocationEndpointsContacted(other.locationEndpointsContacted);
        this.mergeRegionsContacted(other.regionsContacted);
        this.mergeRegionWithContextSet(other.regionsContactedWithContext);
        this.mergeStartTime(other.requestStartTimeUTC);
        this.mergeEndTime(other.requestEndTimeUTC);
        this.mergeSupplementalResponses(other.supplementalResponseStatisticsList);
        this.mergeResponseStatistics(other.responseStatisticsList);
        this.requestPayloadSizeInBytes = Math.max(this.requestPayloadSizeInBytes, other.requestPayloadSizeInBytes);

        if (this.retryContext == null) {
            this.retryContext = other.retryContext;
        } else {
            this.retryContext.merge(other.retryContext);
        }
    }

    public List<URI> getContactedReplicas() {
        return contactedReplicas;
    }

    public void setContactedReplicas(List<URI> contactedReplicas) {
        this.contactedReplicas = Collections.synchronizedList(contactedReplicas);
    }

    public Set<URI> getFailedReplicas() {
        return failedReplicas;
    }

    public void setFailedReplicas(Set<URI> failedReplicas) {
        this.failedReplicas = Collections.synchronizedSet(failedReplicas);
    }

    public Set<String> getContactedRegionNames() {
        return regionsContacted;
    }

    public void setRegionsContacted(Set<String> regionsContacted) {
        this.regionsContacted = Collections.synchronizedSet(regionsContacted);
    }

    public Set<URI> getLocationEndpointsContacted() {
        return locationEndpointsContacted;
    }

    public void setLocationEndpointsContacted(Set<URI> locationEndpointsContacted) {
        this.locationEndpointsContacted = locationEndpointsContacted;
    }

    public MetadataDiagnosticsContext getMetadataDiagnosticsContext() {
        return this.metadataDiagnosticsContext;
    }

    public SerializationDiagnosticsContext getSerializationDiagnosticsContext() {
        return this.serializationDiagnosticsContext;
    }

    public void recordRetryContextEndTime() {
        this.retryContext.updateEndTime();
    }

    public RetryContext getRetryContext() {
        return retryContext;
    }

    public Collection<StoreResponseStatistics> getResponseStatisticsList() {
        return responseStatisticsList;
    }

    @JsonIgnore
    public String getUserAgent() {
        return this.userAgent;
    }

    public int getMaxResponsePayloadSizeInBytes() {
        if (responseStatisticsList == null || responseStatisticsList.isEmpty()) {
            return this.getMaxResponsePayloadSizeInBytesFromGateway();
        }

        int maxResponsePayloadSizeInBytes = 0;
        int currentResponsePayloadSizeInBytes = 0;
        for (StoreResponseStatistics responseDiagnostic : responseStatisticsList) {
            StoreResultDiagnostics storeResultDiagnostics;
            StoreResponseDiagnostics storeResponseDiagnostics;
            if ((storeResultDiagnostics = responseDiagnostic.getStoreResult()) != null &&
                (storeResponseDiagnostics = storeResultDiagnostics.getStoreResponseDiagnostics()) != null &&
                (currentResponsePayloadSizeInBytes = storeResponseDiagnostics.getResponsePayloadLength()) > maxResponsePayloadSizeInBytes) {

                maxResponsePayloadSizeInBytes = currentResponsePayloadSizeInBytes;
            }
        }

        return maxResponsePayloadSizeInBytes;
    }

    private int getMaxResponsePayloadSizeInBytesFromGateway() {
        if (this.gatewayStatisticsList == null || this.gatewayStatisticsList.size() == 0) {
            return 0;
        }

        int maxResponsePayloadSizeInBytes = 0;
        for (GatewayStatistics gatewayStatistics : this.gatewayStatisticsList) {
            maxResponsePayloadSizeInBytes = Math.max(maxResponsePayloadSizeInBytes, gatewayStatistics.responsePayloadSizeInBytes);
        }

        return maxResponsePayloadSizeInBytes;
    }

    public Collection<StoreResponseStatistics> getSupplementalResponseStatisticsList() {
        return supplementalResponseStatisticsList;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public Map<String, AddressResolutionStatistics> getAddressResolutionStatistics() {
        return addressResolutionStatistics;
    }

    public List<GatewayStatistics> getGatewayStatisticsList() {
        return this.gatewayStatisticsList;
    }

    public ClientSideRequestStatistics setSamplingRateSnapshot(double samplingRateSnapshot) {
        this.samplingRateSnapshot = samplingRateSnapshot;

        return this;
    }

    public String getFirstContactedRegion() {
        if (this.regionsContactedWithContext == null || this.regionsContactedWithContext.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return this.regionsContactedWithContext.first().regionContacted;
    }

    public URI getFirstContactedLocationEndpoint() {
        if (this.regionsContactedWithContext == null || this.regionsContactedWithContext.isEmpty()) {
            return null;
        }

        return this.regionsContactedWithContext.first().locationEndpointsContacted;
    }

    public static class StoreResponseStatistics {
        @JsonSerialize(using = StoreResultDiagnostics.StoreResultDiagnosticsSerializer.class)
        private StoreResultDiagnostics storeResult;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private Instant requestResponseTimeUTC;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private Instant requestStartTimeUTC;
        @JsonSerialize
        private ResourceType requestResourceType;
        @JsonSerialize
        private OperationType requestOperationType;
        @JsonSerialize
        private String requestSessionToken;

        @JsonSerialize
        private String e2ePolicyCfg;

        @JsonSerialize
        private String excludedRegions;

        @JsonIgnore
        private String regionName;

        @JsonSerialize
        private Set<String> sessionTokenEvaluationResults;

        @JsonSerialize
        private Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> locationToLocationSpecificHealthContext;

        public String getExcludedRegions() {
            return this.excludedRegions;
        }

        public StoreResultDiagnostics getStoreResult() {
            return storeResult;
        }

        public Instant getRequestResponseTimeUTC() {
            return requestResponseTimeUTC;
        }

        public Instant getRequestStartTimeUTC() {
            return requestStartTimeUTC;
        }

        public ResourceType getRequestResourceType() {
            return requestResourceType;
        }

        public OperationType getRequestOperationType() {
            return requestOperationType;
        }

        public String getRegionName() {
            return regionName;
        }

        public String getRequestSessionToken() {
            return requestSessionToken;
        }

        public Set<String> getSessionTokenEvaluationResults() {
            return sessionTokenEvaluationResults;
        }

        public Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> getLocationToLocationSpecificHealthContext() {
            return locationToLocationSpecificHealthContext;
        }

        @JsonIgnore
        public Duration getDuration() {
            if (requestStartTimeUTC == null ||
                requestResponseTimeUTC == null ||
                requestResponseTimeUTC.isBefore(requestStartTimeUTC)) {
                return null;
            }

            if (requestStartTimeUTC == requestResponseTimeUTC) {
                return Duration.ZERO;
            }

            return Duration.between(requestStartTimeUTC, requestResponseTimeUTC);
        }

    }

    public static class ClientSideRequestStatisticsSerializer extends StdSerializer<ClientSideRequestStatistics> {

        private static final long serialVersionUID = -2746532297176812860L;

        ClientSideRequestStatisticsSerializer() {
            super(ClientSideRequestStatistics.class);
        }

        @Override
        public void serialize(
            ClientSideRequestStatistics statistics, JsonGenerator generator, SerializerProvider provider) throws
            IOException {
            generator.writeStartObject();
            Duration duration = statistics
                .getDuration();
            long requestLatency = duration != null ? duration.toMillis() : 0;
            generator.writeStringField("userAgent", statistics.userAgent);
            generator.writeStringField("activityId", statistics.activityId);
            generator.writeNumberField("requestLatencyInMs", requestLatency);
            generator.writeStringField("requestStartTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestStartTimeUTC));
            generator.writeStringField("requestEndTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestEndTimeUTC));
            generator.writeObjectField("responseStatisticsList", statistics.responseStatisticsList);
            generator.writeObjectField("supplementalResponseStatisticsList", getCappedSupplementalResponseStatisticsList(statistics.supplementalResponseStatisticsList));
            generator.writeObjectField("addressResolutionStatistics", statistics.addressResolutionStatistics);
            generator.writeObjectField("regionsContacted", statistics.getContactedRegionNames());
            generator.writeObjectField("retryContext", statistics.retryContext);
            generator.writeObjectField("metadataDiagnosticsContext", statistics.getMetadataDiagnosticsContext());
            generator.writeObjectField("serializationDiagnosticsContext", statistics.getSerializationDiagnosticsContext());
            generator.writeObjectField("gatewayStatisticsList", statistics.gatewayStatisticsList);
            generator.writeObjectField("samplingRateSnapshot", statistics.samplingRateSnapshot);
            generator.writeNumberField("bloomFilterInsertionCountSnapshot", statistics.approximateInsertionCountInBloomFilter);
            if (statistics.keywordIdentifiers != null && !statistics.keywordIdentifiers.isEmpty()) {
                generator.writeObjectField("keywordIdentifiers", statistics.keywordIdentifiers);
            }

            try {
                CosmosDiagnosticsSystemUsageSnapshot systemInformation = fetchSystemInformation();
                generator.writeObjectField("systemInformation", systemInformation);
            } catch (Exception e) {
                // Error while evaluating system information, do nothing
            }

            long diagnosticsProviderFatalErrorExecutionCount =
                DiagnosticsProviderJvmFatalErrorMapper.getMapper().getMapperExecutionCount();
            if (diagnosticsProviderFatalErrorExecutionCount > 0) {
                generator.writeNumberField("jvmFatalErrorMapperExecutionCount", diagnosticsProviderFatalErrorExecutionCount);
            }

            generator.writeObjectField("clientCfgs", statistics.diagnosticsClientConfig);
            generator.writeEndObject();
        }
    }

    public static Collection<StoreResponseStatistics> getCappedSupplementalResponseStatisticsList(Collection<StoreResponseStatistics> supplementalResponseStatisticsList) {
        int supplementalResponseStatisticsListCount = supplementalResponseStatisticsList.size();
        int initialIndex =
            Math.max(supplementalResponseStatisticsListCount - MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING, 0);
        if (initialIndex != 0) {
            return supplementalResponseStatisticsList
                .stream()
                .skip(initialIndex)
                .limit(supplementalResponseStatisticsListCount)
                .collect(Collectors.toCollection(ConcurrentLinkedDeque<StoreResponseStatistics>::new));
        }
        return supplementalResponseStatisticsList;
    }

    public static class AddressResolutionStatistics {
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private Instant startTimeUTC;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private Instant endTimeUTC;
        @JsonSerialize
        private String targetEndpoint;
        @JsonSerialize
        private String exceptionMessage;
        @JsonSerialize
        private boolean forceRefresh;
        @JsonSerialize
        private boolean forceCollectionRoutingMapRefresh;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String faultInjectionRuleId;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> faultInjectionEvaluationResults;

        // If one replica return error we start address call in parallel,
        // on other replica  valid response, we end the current user request,
        // indicating background addressResolution is still inflight
        @JsonSerialize
        private boolean inflightRequest = true;

        public Instant getStartTimeUTC() {
            return startTimeUTC;
        }

        public Instant getEndTimeUTC() {
            return endTimeUTC;
        }

        public String getTargetEndpoint() {
            return targetEndpoint;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        public boolean isInflightRequest() {
            return inflightRequest;
        }

        public boolean isForceRefresh() {
            return forceRefresh;
        }

        public boolean isForceCollectionRoutingMapRefresh() {
            return forceCollectionRoutingMapRefresh;
        }

        public String getFaultInjectionRuleId() {
            return faultInjectionRuleId;
        }

        public List<String> getFaultInjectionEvaluationResults() {
            return faultInjectionEvaluationResults;
        }
    }

    @JsonSerialize(using = GatewayStatistics.GatewayStatisticsSerializer.class)
    public static class GatewayStatistics {
        private String sessionToken;
        private OperationType operationType;
        private ResourceType resourceType;
        private int statusCode;
        private int subStatusCode;
        private double requestCharge;
        private RequestTimeline requestTimeline;
        private String partitionKeyRangeId;
        private String exceptionMessage;
        private String exceptionResponseHeaders;

        private int responsePayloadSizeInBytes;
        private String faultInjectionRuleId;
        private List<String> faultInjectionEvaluationResults;
        private Set<String> sessionTokenEvaluationResults;
        private Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> locationToLocationSpecificHealthContext;

        public String getSessionToken() {
            return sessionToken;
        }

        public OperationType getOperationType() {
            return operationType;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public int getSubStatusCode() {
            return subStatusCode;
        }

        public double getRequestCharge() {
            return requestCharge;
        }

        public RequestTimeline getRequestTimeline() {
            return requestTimeline;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public String getPartitionKeyRangeId() {
            return partitionKeyRangeId;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        public String getExceptionResponseHeaders() {
            return exceptionResponseHeaders;
        }

        public int getResponsePayloadSizeInBytes() {
            return this.responsePayloadSizeInBytes;
        }

        public String getFaultInjectionRuleId() {
            return faultInjectionRuleId;
        }

        public List<String> getFaultInjectionEvaluationResults() {
            return faultInjectionEvaluationResults;
        }

        public Set<String> getSessionTokenEvaluationResults() {
            return sessionTokenEvaluationResults;
        }

        public Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> getLocationToLocationSpecificHealthContext() {
            return locationToLocationSpecificHealthContext;
        }

        public static class GatewayStatisticsSerializer extends StdSerializer<GatewayStatistics> {
            private static final long serialVersionUID = 1L;

            public GatewayStatisticsSerializer() {
                super(GatewayStatistics.class);
            }

            @Override
            public void serialize(GatewayStatistics gatewayStatistics,
                                  JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("sessionToken", gatewayStatistics.getSessionToken());
                jsonGenerator.writeStringField("operationType", gatewayStatistics.getOperationType().toString());
                jsonGenerator.writeStringField("resourceType", gatewayStatistics.getResourceType().toString());
                jsonGenerator.writeNumberField("statusCode", gatewayStatistics.getStatusCode());
                jsonGenerator.writeNumberField("subStatusCode", gatewayStatistics.getSubStatusCode());
                jsonGenerator.writeNumberField("requestCharge", gatewayStatistics.getRequestCharge());
                jsonGenerator.writeObjectField("requestTimeline", gatewayStatistics.getRequestTimeline());
                jsonGenerator.writeStringField("partitionKeyRangeId", gatewayStatistics.getPartitionKeyRangeId());
                jsonGenerator.writeNumberField("responsePayloadSizeInBytes", gatewayStatistics.getResponsePayloadSizeInBytes());
                this.writeNonNullStringField(jsonGenerator, "exceptionMessage", gatewayStatistics.getExceptionMessage());
                this.writeNonNullStringField(jsonGenerator, "exceptionResponseHeaders", gatewayStatistics.getExceptionResponseHeaders());
                this.writeNonNullStringField(jsonGenerator, "faultInjectionRuleId", gatewayStatistics.getFaultInjectionRuleId());

                if (StringUtils.isEmpty(gatewayStatistics.getFaultInjectionRuleId())) {
                    this.writeNonEmptyStringArrayField(
                        jsonGenerator,
                        "faultInjectionEvaluationResults",
                        gatewayStatistics.getFaultInjectionEvaluationResults());
                }

                this.writeNonEmptyStringSetField(jsonGenerator, "sessionTokenEvaluationResults", gatewayStatistics.getSessionTokenEvaluationResults());
                this.writeNonNullObjectField(jsonGenerator, "locationToLocationSpecificHealthContext", gatewayStatistics.getLocationToLocationSpecificHealthContext());
                jsonGenerator.writeEndObject();
            }

            private void writeNonNullStringField(JsonGenerator jsonGenerator, String fieldName, String value) throws IOException {
                if (value == null) {
                    return;
                }

                jsonGenerator.writeStringField(fieldName, value);
            }

            private void writeNonEmptyStringArrayField(JsonGenerator jsonGenerator, String fieldName, List<String> values) throws IOException {
                if (values == null || values.isEmpty()) {
                    return;
                }

                jsonGenerator.writeObjectField(fieldName, values);
            }

            private void writeNonEmptyStringSetField(JsonGenerator jsonGenerator, String fieldName, Set<String> values) throws IOException {
                if (values == null || values.isEmpty()) {
                    return;
                }

                jsonGenerator.writePOJOField(fieldName, values);
            }

            private void writeNonNullObjectField(JsonGenerator jsonGenerator, String fieldName, Object object) throws IOException {
                if (object == null) {
                    return;
                }

                jsonGenerator.writePOJOField(fieldName, object);
            }
        }
    }

    public static CosmosDiagnosticsSystemUsageSnapshot fetchSystemInformation() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        long maxMemory = runtime.maxMemory() / 1024;


        // TODO: other system related info also can be captured using a similar approach
        String systemCpu = CpuMemoryMonitor
            .getCpuLoad()
            .toString();

        return ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .getCosmosDiagnosticsContextAccessor()
            .createSystemUsageSnapshot(
                systemCpu,
                totalMemory - freeMemory + " KB",
                (maxMemory - (totalMemory - freeMemory)) + " KB",
                runtime.availableProcessors());
    }

    static class RegionWithContext implements Comparable<RegionWithContext> {

        private final String regionContacted;
        private final URI locationEndpointsContacted;
        private final long recordedTimestamp;

        RegionWithContext(String regionContacted, URI locationEndpointsContacted) {
            this.regionContacted = regionContacted;
            this.locationEndpointsContacted = locationEndpointsContacted;
            this.recordedTimestamp = System.currentTimeMillis();
        }

        @Override
        public int compareTo(RegionWithContext o) {

            if (o == null || this.recordedTimestamp > o.recordedTimestamp) {
                return 1;
            }

            if (this.recordedTimestamp == o.recordedTimestamp) {
                return 0;
            }

            return -1;
        }
    }
}
