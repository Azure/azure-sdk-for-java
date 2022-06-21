// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.cpu.CpuMemoryMonitor;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonSerialize(using = ClientSideRequestStatistics.ClientSideRequestStatisticsSerializer.class)
public class ClientSideRequestStatistics {
    private static final int MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING = 10;
    private final DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig;
    private String activityId;
    private List<StoreResponseStatistics> responseStatisticsList;
    private List<StoreResponseStatistics> supplementalResponseStatisticsList;
    private Map<String, AddressResolutionStatistics> addressResolutionStatistics;

    private List<URI> contactedReplicas;
    private Set<URI> failedReplicas;
    private Instant requestStartTimeUTC;
    private Instant requestEndTimeUTC;
    private Set<String> regionsContacted;
    private Set<URI> locationEndpointsContacted;
    private RetryContext retryContext;
    private GatewayStatistics gatewayStatistics;
    private MetadataDiagnosticsContext metadataDiagnosticsContext;
    private SerializationDiagnosticsContext serializationDiagnosticsContext;

    public ClientSideRequestStatistics(DiagnosticsClientContext diagnosticsClientContext) {
        this.diagnosticsClientConfig = diagnosticsClientContext.getConfig();
        this.requestStartTimeUTC = Instant.now();
        this.requestEndTimeUTC = Instant.now();
        this.responseStatisticsList = new ArrayList<>();
        this.supplementalResponseStatisticsList = new ArrayList<>();
        this.addressResolutionStatistics = new HashMap<>();
        this.contactedReplicas = Collections.synchronizedList(new ArrayList<>());
        this.failedReplicas = Collections.synchronizedSet(new HashSet<>());
        this.regionsContacted = Collections.synchronizedSet(new HashSet<>());
        this.locationEndpointsContacted = Collections.synchronizedSet(new HashSet<>());
        this.metadataDiagnosticsContext = new MetadataDiagnosticsContext();
        this.serializationDiagnosticsContext = new SerializationDiagnosticsContext();
        this.retryContext = new RetryContext();
    }

    public ClientSideRequestStatistics(ClientSideRequestStatistics toBeCloned) {
        this.diagnosticsClientConfig = toBeCloned.diagnosticsClientConfig;
        this.requestStartTimeUTC = toBeCloned.requestStartTimeUTC;
        this.requestEndTimeUTC = toBeCloned.requestEndTimeUTC;
        this.responseStatisticsList = new ArrayList<>(toBeCloned.responseStatisticsList);
        this.supplementalResponseStatisticsList = new ArrayList<>(toBeCloned.supplementalResponseStatisticsList);
        this.addressResolutionStatistics = new HashMap<>(toBeCloned.addressResolutionStatistics);
        this.contactedReplicas = Collections.synchronizedList(new ArrayList<>(toBeCloned.contactedReplicas));
        this.failedReplicas = Collections.synchronizedSet(new HashSet<>(toBeCloned.failedReplicas));
        this.regionsContacted = Collections.synchronizedSet(new HashSet<>(toBeCloned.regionsContacted));
        this.locationEndpointsContacted = Collections.synchronizedSet(
            new HashSet<>(toBeCloned.locationEndpointsContacted));
        this.metadataDiagnosticsContext = new MetadataDiagnosticsContext(toBeCloned.metadataDiagnosticsContext);
        this.serializationDiagnosticsContext =
            new SerializationDiagnosticsContext(toBeCloned.serializationDiagnosticsContext);
        this.retryContext = new RetryContext(toBeCloned.retryContext);
    }

    public Duration getDuration() {
        return Duration.between(requestStartTimeUTC, requestEndTimeUTC);
    }

    public Instant getRequestStartTimeUTC() {
        return requestStartTimeUTC;
    }

    public DiagnosticsClientContext.DiagnosticsClientConfig getDiagnosticsClientConfig() {
        return diagnosticsClientConfig;
    }

    public void recordResponse(RxDocumentServiceRequest request, StoreResultDiagnostics storeResultDiagnostics, GlobalEndpointManager globalEndpointManager) {
        Objects.requireNonNull(request, "request is required and cannot be null.");
        Instant responseTime = Instant.now();

        StoreResponseStatistics storeResponseStatistics = new StoreResponseStatistics();
        storeResponseStatistics.requestResponseTimeUTC = responseTime;
        storeResponseStatistics.storeResult = storeResultDiagnostics;
        storeResponseStatistics.requestOperationType = request.getOperationType();
        storeResponseStatistics.requestResourceType = request.getResourceType();
        storeResponseStatistics.requestSessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        activityId = request.getActivityId().toString();


        URI locationEndPoint = null;
        if (request.requestContext != null) {
            if (request.requestContext.locationEndpointToRoute != null) {
                locationEndPoint = request.requestContext.locationEndpointToRoute;
            }
        }

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            if (locationEndPoint != null) {
                this.regionsContacted.add(globalEndpointManager.getRegionName(locationEndPoint, request.getOperationType()));
                this.locationEndpointsContacted.add(locationEndPoint);
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
        RxDocumentServiceRequest rxDocumentServiceRequest, StoreResponseDiagnostics storeResponseDiagnostics, GlobalEndpointManager globalEndpointManager) {
        Instant responseTime = Instant.now();

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            URI locationEndPoint = null;
            if (rxDocumentServiceRequest != null && rxDocumentServiceRequest.requestContext != null) {
                locationEndPoint = rxDocumentServiceRequest.requestContext.locationEndpointToRoute;
            }
            this.recordRetryContextEndTime();

            if (locationEndPoint != null) {
                this.regionsContacted.add(globalEndpointManager.getRegionName(locationEndPoint, rxDocumentServiceRequest.getOperationType()));
                this.locationEndpointsContacted.add(locationEndPoint);
            }

            this.gatewayStatistics = new GatewayStatistics();
            if (rxDocumentServiceRequest != null) {
                this.gatewayStatistics.operationType = rxDocumentServiceRequest.getOperationType();
                this.gatewayStatistics.resourceType = rxDocumentServiceRequest.getResourceType();
            }
            this.gatewayStatistics.statusCode = storeResponseDiagnostics.getStatusCode();
            this.gatewayStatistics.subStatusCode = storeResponseDiagnostics.getSubStatusCode();
            this.gatewayStatistics.sessionToken = storeResponseDiagnostics.getSessionTokenAsString();
            this.gatewayStatistics.requestCharge = storeResponseDiagnostics.getRequestCharge();
            this.gatewayStatistics.requestTimeline = storeResponseDiagnostics.getRequestTimeline();
            this.gatewayStatistics.partitionKeyRangeId = storeResponseDiagnostics.getPartitionKeyRangeId();
            this.gatewayStatistics.exceptionMessage = storeResponseDiagnostics.getExceptionMessage();
            this.gatewayStatistics.exceptionResponseHeaders = storeResponseDiagnostics.getExceptionResponseHeaders();
            this.activityId = storeResponseDiagnostics.getActivityId();
        }
    }

    public String recordAddressResolutionStart(
        URI targetEndpoint,
        boolean forceRefresh,
        boolean forceCollectionRoutingMapRefresh) {
        String identifier = Utils
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

    public void recordAddressResolutionEnd(String identifier, String exceptionMessage) {
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

    public MetadataDiagnosticsContext getMetadataDiagnosticsContext(){
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

    public List<StoreResponseStatistics> getResponseStatisticsList() {
        return responseStatisticsList;
    }

    public List<StoreResponseStatistics> getSupplementalResponseStatisticsList() {
        return supplementalResponseStatisticsList;
    }

    public Map<String, AddressResolutionStatistics> getAddressResolutionStatistics() {
        return addressResolutionStatistics;
    }

    public GatewayStatistics getGatewayStatistics() {
        return gatewayStatistics;
    }

    public static class StoreResponseStatistics {
        @JsonSerialize(using = StoreResultDiagnostics.StoreResultDiagnosticsSerializer.class)
        private StoreResultDiagnostics storeResult;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private Instant requestResponseTimeUTC;
        @JsonSerialize
        private ResourceType requestResourceType;
        @JsonSerialize
        private OperationType requestOperationType;
        @JsonSerialize
        private String requestSessionToken;

        public StoreResultDiagnostics getStoreResult() {
            return storeResult;
        }

        public Instant getRequestResponseTimeUTC() {
            return requestResponseTimeUTC;
        }

        public ResourceType getRequestResourceType() {
            return requestResourceType;
        }

        public OperationType getRequestOperationType() {
            return requestOperationType;
        }

        public String getRequestSessionToken() { return requestSessionToken; }
    }
    public static class SystemInformation {
        private String usedMemory;
        private String availableMemory;
        private String systemCpuLoad;
        private int availableProcessors;

        public String getUsedMemory() {
            return usedMemory;
        }

        public String getAvailableMemory() {
            return availableMemory;
        }

        public String getSystemCpuLoad() {
            return systemCpuLoad;
        }

        public int getAvailableProcessors() {
            return availableProcessors;
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
            long requestLatency = statistics
                .getDuration()
                .toMillis();
            generator.writeStringField("userAgent", Utils.getUserAgent());
            generator.writeStringField("activityId", statistics.activityId);
            generator.writeNumberField("requestLatencyInMs", requestLatency);
            generator.writeStringField("requestStartTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestStartTimeUTC));
            generator.writeStringField("requestEndTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestEndTimeUTC));
            generator.writeObjectField("responseStatisticsList", statistics.responseStatisticsList);
            generator.writeObjectField("supplementalResponseStatisticsList", getCappedSupplementalResponseStatisticsList(statistics.supplementalResponseStatisticsList));
            generator.writeObjectField("addressResolutionStatistics", statistics.addressResolutionStatistics);
            generator.writeObjectField("regionsContacted", statistics.regionsContacted);
            generator.writeObjectField("retryContext", statistics.retryContext);
            generator.writeObjectField("metadataDiagnosticsContext", statistics.getMetadataDiagnosticsContext());
            generator.writeObjectField("serializationDiagnosticsContext", statistics.getSerializationDiagnosticsContext());
            generator.writeObjectField("gatewayStatistics", statistics.gatewayStatistics);

            try {
                SystemInformation systemInformation = fetchSystemInformation();
                generator.writeObjectField("systemInformation", systemInformation);
            } catch (Exception e) {
                // Error while evaluating system information, do nothing
            }

            generator.writeObjectField("clientCfgs", statistics.diagnosticsClientConfig);
            generator.writeEndObject();
        }
    }

    public static List<StoreResponseStatistics> getCappedSupplementalResponseStatisticsList(List<StoreResponseStatistics> supplementalResponseStatisticsList) {
        int supplementalResponseStatisticsListCount = supplementalResponseStatisticsList.size();
        int initialIndex =
            Math.max(supplementalResponseStatisticsListCount - MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING, 0);
        if (initialIndex != 0) {
            List<StoreResponseStatistics> subList = supplementalResponseStatisticsList
                .subList(initialIndex,
                    supplementalResponseStatisticsListCount);
            return subList;
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
    }

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
    }

    public static SystemInformation fetchSystemInformation() {
        SystemInformation systemInformation = new SystemInformation();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        long maxMemory = runtime.maxMemory() / 1024;
        systemInformation.usedMemory = totalMemory - freeMemory + " KB";
        systemInformation.availableMemory = (maxMemory - (totalMemory - freeMemory)) + " KB";
        systemInformation.availableProcessors = runtime.availableProcessors();

        // TODO: other system related info also can be captured using a similar approach
        systemInformation.systemCpuLoad = CpuMemoryMonitor
            .getCpuLoad()
            .toString();
        return systemInformation;
    }
}
