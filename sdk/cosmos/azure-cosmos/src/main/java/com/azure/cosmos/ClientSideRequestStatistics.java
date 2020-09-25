// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.azure.cosmos.implementation.cpu.CpuMonitor;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

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
class ClientSideRequestStatistics {
    private static final int MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING = 10;
    private ConnectionMode connectionMode;

    private List<StoreResponseStatistics> responseStatisticsList;
    private List<StoreResponseStatistics> supplementalResponseStatisticsList;
    private Map<String, AddressResolutionStatistics> addressResolutionStatistics;

    private List<URI> contactedReplicas;
    private Set<URI> failedReplicas;
    private Instant requestStartTimeUTC;
    private Instant requestEndTimeUTC;
    private Set<URI> regionsContacted;
    private RetryContext retryContext;
    private GatewayStatistics gatewayStatistics;
    private RequestTimeline transportRequestTimeline;
    private MetadataDiagnosticsContext metadataDiagnosticsContext;
    private SerializationDiagnosticsContext serializationDiagnosticsContext;

    ClientSideRequestStatistics() {
        this.requestStartTimeUTC = Instant.now();
        this.requestEndTimeUTC = Instant.now();
        this.responseStatisticsList = new ArrayList<>();
        this.supplementalResponseStatisticsList = new ArrayList<>();
        this.addressResolutionStatistics = new HashMap<>();
        this.contactedReplicas = Collections.synchronizedList(new ArrayList<>());
        this.failedReplicas = Collections.synchronizedSet(new HashSet<>());
        this.regionsContacted = Collections.synchronizedSet(new HashSet<>());
        this.connectionMode = ConnectionMode.DIRECT;
        this.metadataDiagnosticsContext = new MetadataDiagnosticsContext();
        this.serializationDiagnosticsContext = new SerializationDiagnosticsContext();
    }

    Duration getDuration() {
        return Duration.between(requestStartTimeUTC, requestEndTimeUTC);
    }

    void recordResponse(RxDocumentServiceRequest request, StoreResult storeResult) {
        Objects.requireNonNull(request, "request is required and cannot be null.");
        Instant responseTime = Instant.now();
        connectionMode = ConnectionMode.DIRECT;

        StoreResponseStatistics storeResponseStatistics = new StoreResponseStatistics();
        storeResponseStatistics.requestResponseTimeUTC = responseTime;
        storeResponseStatistics.storeResult = storeResult;
        storeResponseStatistics.requestOperationType = request.getOperationType();
        storeResponseStatistics.requestResourceType = request.getResourceType();

        URI locationEndPoint = null;
        if (request.requestContext != null) {
            this.retryContext = new RetryContext(request.requestContext.retryContext);
            if (request.requestContext.locationEndpointToRoute != null) {
                locationEndPoint = request.requestContext.locationEndpointToRoute;
            }
        }

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            if (locationEndPoint != null) {
                this.regionsContacted.add(locationEndPoint);
            }

            if (storeResponseStatistics.requestOperationType == OperationType.Head
                    || storeResponseStatistics.requestOperationType == OperationType.HeadFeed) {
                this.supplementalResponseStatisticsList.add(storeResponseStatistics);
            } else {
                this.responseStatisticsList.add(storeResponseStatistics);
            }
        }
    }

    void recordGatewayResponse(
        RxDocumentServiceRequest rxDocumentServiceRequest, StoreResponse storeResponse,
        CosmosException exception) {
        Instant responseTime = Instant.now();
        connectionMode = ConnectionMode.GATEWAY;
        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTimeUTC)) {
                this.requestEndTimeUTC = responseTime;
            }

            if (rxDocumentServiceRequest != null
                    && rxDocumentServiceRequest.requestContext != null
                    && rxDocumentServiceRequest.requestContext.retryContext != null) {
                rxDocumentServiceRequest.requestContext.retryContext.retryEndTime = Instant.now();
                this.retryContext = new RetryContext(rxDocumentServiceRequest.requestContext.retryContext);
            }

            this.gatewayStatistics = new GatewayStatistics();
            if (rxDocumentServiceRequest != null) {
                this.gatewayStatistics.operationType = rxDocumentServiceRequest.getOperationType();
            }
            if (storeResponse != null) {
                this.gatewayStatistics.statusCode = storeResponse.getStatus();
                this.gatewayStatistics.subStatusCode = DirectBridgeInternal.getSubStatusCode(storeResponse);
                this.gatewayStatistics.sessionToken = storeResponse
                                                          .getHeaderValue(HttpConstants.HttpHeaders.SESSION_TOKEN);
                this.gatewayStatistics.requestCharge = storeResponse
                                                           .getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE);
                this.gatewayStatistics.requestTimeline = DirectBridgeInternal.getRequestTimeline(storeResponse);
            } else if (exception != null) {
                this.gatewayStatistics.statusCode = exception.getStatusCode();
                this.gatewayStatistics.subStatusCode = exception.getSubStatusCode();
                this.gatewayStatistics.requestTimeline = this.transportRequestTimeline;
            }
        }
    }

    void setTransportClientRequestTimeline(RequestTimeline transportRequestTimeline) {
        this.transportRequestTimeline = transportRequestTimeline;
    }

    String recordAddressResolutionStart(URI targetEndpoint) {
        String identifier = Utils.randomUUID().toString();

        AddressResolutionStatistics resolutionStatistics = new AddressResolutionStatistics();
        resolutionStatistics.startTimeUTC = Instant.now();
        resolutionStatistics.endTimeUTC = null;
        resolutionStatistics.targetEndpoint = targetEndpoint == null ? "<NULL>" : targetEndpoint.toString();

        synchronized (this) {
            this.addressResolutionStatistics.put(identifier, resolutionStatistics);
        }

        return identifier;
    }

    void recordAddressResolutionEnd(String identifier, String errorMessage) {
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
            resolutionStatistics.errorMessage = errorMessage;
            resolutionStatistics.inflightRequest = false;
        }
    }

    List<URI> getContactedReplicas() {
        return contactedReplicas;
    }

    void setContactedReplicas(List<URI> contactedReplicas) {
        this.contactedReplicas = Collections.synchronizedList(contactedReplicas);
    }

    Set<URI> getFailedReplicas() {
        return failedReplicas;
    }

    void setFailedReplicas(Set<URI> failedReplicas) {
        this.failedReplicas = Collections.synchronizedSet(failedReplicas);
    }

    Set<URI> getRegionsContacted() {
        return regionsContacted;
    }

    void setRegionsContacted(Set<URI> regionsContacted) {
        this.regionsContacted = Collections.synchronizedSet(regionsContacted);
    }

    MetadataDiagnosticsContext getMetadataDiagnosticsContext(){
        return this.metadataDiagnosticsContext;
    }

    SerializationDiagnosticsContext getSerializationDiagnosticsContext(){
        return this.serializationDiagnosticsContext;
    }

    void recordRetryContext(RxDocumentServiceRequest request) {
        if(request.requestContext.retryContext != null) {
            request.requestContext.retryContext.retryEndTime =  Instant.now();
            this.retryContext = new RetryContext(request.requestContext.retryContext);
        }
    }

    static class StoreResponseStatistics {
        @JsonSerialize(using = StoreResult.StoreResultSerializer.class)
        StoreResult storeResult;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        Instant requestResponseTimeUTC;
        @JsonSerialize
        ResourceType requestResourceType;
        @JsonSerialize
        OperationType requestOperationType;
    }

    private static class SystemInformation {
        String usedMemory;
        String availableMemory;
        String systemCpuLoad;

        public String getUsedMemory() {
            return usedMemory;
        }

        public String getAvailableMemory() {
            return availableMemory;
        }

        public String getSystemCpuLoad() {
            return systemCpuLoad;
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
            long requestLatency = statistics.getDuration().toMillis();
            generator.writeStringField("userAgent", CosmosDiagnostics.USER_AGENT);
            generator.writeNumberField("requestLatencyInMs", requestLatency);
            generator.writeStringField("requestStartTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestStartTimeUTC));
            generator.writeStringField("requestEndTimeUTC", DiagnosticsInstantSerializer.fromInstant(statistics.requestEndTimeUTC));
            generator.writeObjectField("connectionMode", statistics.connectionMode);
            generator.writeObjectField("responseStatisticsList", statistics.responseStatisticsList);
            int supplementalResponseStatisticsListCount = statistics.supplementalResponseStatisticsList.size();
            int initialIndex =
                Math.max(supplementalResponseStatisticsListCount - MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING, 0);
            if (initialIndex != 0) {
                List<StoreResponseStatistics> subList = statistics.supplementalResponseStatisticsList
                                                            .subList(initialIndex,
                                                                     supplementalResponseStatisticsListCount);
                generator.writeObjectField("supplementalResponseStatisticsList", subList);
            } else {
                generator
                    .writeObjectField("supplementalResponseStatisticsList",
                                      statistics.supplementalResponseStatisticsList);
            }

            generator.writeObjectField("addressResolutionStatistics", statistics.addressResolutionStatistics);
            generator.writeObjectField("regionsContacted", statistics.regionsContacted);
            generator.writeObjectField("retryContext", statistics.retryContext);
            generator.writeObjectField("metadataDiagnosticsContext", statistics.getMetadataDiagnosticsContext());
            generator.writeObjectField("serializationDiagnosticsContext", statistics.getSerializationDiagnosticsContext());
            generator.writeObjectField("gatewayStatistics", statistics.gatewayStatistics);

            try {
                SystemInformation systemInformation = new SystemInformation();
                long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
                long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
                long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
                systemInformation.usedMemory = totalMemory - freeMemory + " KB";
                systemInformation.availableMemory = (maxMemory - (totalMemory - freeMemory)) + " KB";

                // TODO: other system related info also can be captured using a similar approach
                systemInformation.systemCpuLoad = CpuMonitor.getCpuLoad().toString();
                generator.writeObjectField("systemInformation", systemInformation);
            } catch (Exception e) {
                // Error while evaluating system information, do nothing
            }
            generator.writeEndObject();
        }
    }

    private static class AddressResolutionStatistics {
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        Instant startTimeUTC;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        Instant endTimeUTC;
        @JsonSerialize
        String targetEndpoint;
        @JsonSerialize
        String errorMessage;

        // If one replica return error we start address call in parallel,
        // on other replica  valid response, we end the current user request,
        // indicating background addressResolution is still inflight
        @JsonSerialize
        boolean inflightRequest = true;
    }

    private static class GatewayStatistics {
        String sessionToken;
        OperationType operationType;
        int statusCode;
        int subStatusCode;
        String requestCharge;
        RequestTimeline requestTimeline;

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

        public String getRequestCharge() {
            return requestCharge;
        }

        public RequestTimeline getRequestTimeline() {
            return requestTimeline;
        }
    }
}