// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class ClientSideRequestStatistics {

    private final static int MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING = 10;

    private final static DateTimeFormatter responseTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss.SSS").withLocale(Locale.US);
    private final static OperatingSystemMXBean mbean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ConnectionMode connectionMode;

    public List<StoreResponseStatistics> responseStatisticsList;
    public List<StoreResponseStatistics> supplementalResponseStatisticsList;
    public Map<String, AddressResolutionStatistics> addressResolutionStatistics;

    private List<URI> contactedReplicas;
    private Set<URI> failedReplicas;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime requestStartTime;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime requestEndTime;
    public long requestLatency;
    public Set<URI> regionsContacted;
    public RetryContext retryContext;
    public GatewayStatistics gatewayStatistics;
    public SystemInformation systemInformation;

    ClientSideRequestStatistics() {
        this.requestStartTime = ZonedDateTime.now(ZoneOffset.UTC);
        this.requestEndTime = ZonedDateTime.now(ZoneOffset.UTC);
        this.responseStatisticsList = new ArrayList<>();
        this.supplementalResponseStatisticsList = new ArrayList<>();
        this.addressResolutionStatistics = new HashMap<>();
        this.contactedReplicas = new ArrayList<>();
        this.failedReplicas = new HashSet<>();
        this.regionsContacted = new HashSet<>();
        this.connectionMode = ConnectionMode.DIRECT;
        this.systemInformation = new SystemInformation();
        this.retryContext = retryContext;
    }

    Duration getRequestLatency() {
        return Duration.between(requestStartTime, requestEndTime);
    }

    void recordResponse(RxDocumentServiceRequest request, StoreResult storeResult) {
        ZonedDateTime responseTime = ZonedDateTime.now(ZoneOffset.UTC);
        connectionMode = ConnectionMode.DIRECT;

        StoreResponseStatistics storeResponseStatistics = new StoreResponseStatistics();
        storeResponseStatistics.requestResponseTime = responseTime;
        storeResponseStatistics.storeResult = storeResult;
        storeResponseStatistics.requestOperationType = request.getOperationType();
        storeResponseStatistics.requestResourceType = request.getResourceType();

        URI locationEndPoint = null;
        if(request != null && request.requestContext != null) {
            this.retryContext = new RetryContext(request.requestContext.retryContext);
            if (request.requestContext.locationEndpointToRoute != null) {
                locationEndPoint = request.requestContext.locationEndpointToRoute;
            }
        }

        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTime)) {
                this.requestEndTime = responseTime;
            }

            if (locationEndPoint != null) {
                this.regionsContacted.add(locationEndPoint);
            }

            if (storeResponseStatistics.requestOperationType == OperationType.Head ||
                storeResponseStatistics.requestOperationType == OperationType.HeadFeed) {
                this.supplementalResponseStatisticsList.add(storeResponseStatistics);
            } else {
                this.responseStatisticsList.add(storeResponseStatistics);
            }
        }
    }

    void recordGatewayResponse(RxDocumentServiceRequest rxDocumentServiceRequest, StoreResponse storeResponse, CosmosClientException exception) {
        ZonedDateTime responseTime = ZonedDateTime.now(ZoneOffset.UTC);
        connectionMode = ConnectionMode.GATEWAY;
        synchronized (this) {
            if (responseTime.isAfter(this.requestEndTime)) {
                this.requestEndTime = responseTime;
            }

            if(rxDocumentServiceRequest != null && rxDocumentServiceRequest.requestContext != null) {
                this.retryContext = new RetryContext(rxDocumentServiceRequest.requestContext.retryContext);
            }

            this.gatewayStatistics = new GatewayStatistics();
            this.gatewayStatistics.operationType = rxDocumentServiceRequest.getOperationType();
            if (storeResponse != null) {
                this.gatewayStatistics.statusCode = storeResponse.getStatus();
                this.gatewayStatistics.subStatusCode = DirectBridgeInternal.getSubStatusCode(storeResponse);
                this.gatewayStatistics.sessionToken = storeResponse.getHeaderValue(HttpConstants.HttpHeaders.SESSION_TOKEN);
                this.gatewayStatistics.requestCharge = storeResponse.getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE);
            } else if(exception != null){
                this.gatewayStatistics.statusCode = exception.getStatusCode();
                this.gatewayStatistics.subStatusCode = exception.getSubStatusCode();
            }
        }
    }


    String recordAddressResolutionStart(URI targetEndpoint) {
        String identifier = Utils.randomUUID().toString();

        AddressResolutionStatistics resolutionStatistics = new AddressResolutionStatistics();
        resolutionStatistics.startTime = ZonedDateTime.now(ZoneOffset.UTC);
        //  Very far in the future
        resolutionStatistics.endTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC);
        resolutionStatistics.targetEndpoint = targetEndpoint == null ? "<NULL>" : targetEndpoint.toString();

        synchronized (this) {
            this.addressResolutionStatistics.put(identifier, resolutionStatistics);
        }

        return identifier;
    }

    void recordAddressResolutionEnd(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return;
        }
        ZonedDateTime responseTime = ZonedDateTime.now(ZoneOffset.UTC);

        synchronized (this) {
            if (!this.addressResolutionStatistics.containsKey(identifier)) {
                throw new IllegalArgumentException("Identifier " + identifier + " does not exist. Please call start before calling end");
            }

            if (responseTime.isAfter(this.requestEndTime)) {
                this.requestEndTime = responseTime;
            }

            AddressResolutionStatistics resolutionStatistics = this.addressResolutionStatistics.get(identifier);
            resolutionStatistics.endTime = responseTime;
        }
    }

    @Override
    public String toString() {
        //  need to lock in case of concurrent operations. this should be extremely rare since toString()
        //  should only be called at the end of request.
        synchronized (this) {
            requestLatency= getRequestLatency().toMillis();
            //  only take last 10 responses from this list - this has potential of having large number of entries.
            //  since this is for establishing consistency, we can make do with the last responses to paint a meaningful picture.
            int supplementalResponseStatisticsListCount = this.supplementalResponseStatisticsList.size();
            int initialIndex = Math.max(supplementalResponseStatisticsListCount - MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING, 0);
            if (initialIndex != 0) {
                this.supplementalResponseStatisticsList.removeAll(this.supplementalResponseStatisticsList.subList(0, initialIndex));
            }
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            printSystemInformation();
        }
        return StringUtils.EMPTY;
    }

    List<URI> getContactedReplicas() {
        return contactedReplicas;
    }

    void setContactedReplicas(List<URI> contactedReplicas) {
        this.contactedReplicas = contactedReplicas;
    }

    Set<URI> getFailedReplicas() {
        return failedReplicas;
    }

    void setFailedReplicas(Set<URI> failedReplicas) {
        this.failedReplicas = failedReplicas;
    }

    Set<URI> getRegionsContacted() {
        return regionsContacted;
    }

    void setRegionsContacted(Set<URI> regionsContacted) {
        this.regionsContacted = regionsContacted;
    }

    private void printSystemInformation() {
        try {
            long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
            long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
            long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
            this.systemInformation.usedMemory = totalMemory - freeMemory + " KB";
            this.systemInformation.availableMemory = (maxMemory - (totalMemory - freeMemory)) + " KB";

            OperatingSystemMXBean mbean = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
            this.systemInformation.processCpuLoad = mbean.getProcessCpuLoad()*100 +  " %";
            this.systemInformation.systemCpuLoad = mbean.getSystemCpuLoad()*100 +  " %";
        } catch (Exception e) {
            // Error while evaluating system information, do nothing
        }
    }

    private static String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(responseTimeFormatter);
    }

    public void recordRetryContext(RxDocumentServiceRequest request) {
        this.retryContext = new RetryContext(request.requestContext.retryContext);
    }

    public static class StoreResponseStatistics {
        @JsonSerialize(using = StoreResult.StoreResultSerializer.class)
        public StoreResult storeResult;
        @JsonSerialize(using = ZonedDateTimeSerializer.class)
        public ZonedDateTime requestResponseTime;
        public ResourceType requestResourceType;
        public OperationType requestOperationType;
    }

    private class AddressResolutionStatistics {
        @JsonSerialize(using = ZonedDateTimeSerializer.class)
        public ZonedDateTime startTime;
        @JsonSerialize(using = ZonedDateTimeSerializer.class)
        public ZonedDateTime endTime;
        public String targetEndpoint;
    }

    private class GatewayStatistics {
        public String sessionToken;
        public OperationType operationType;
        public int statusCode;
        public int subStatusCode;
        public String requestCharge;
    }

    private class SystemInformation {
        public String usedMemory;
        public String availableMemory;
        public String processCpuLoad;
        public String systemCpuLoad;
    }

    private static class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

        public ZonedDateTimeSerializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public void serialize(ZonedDateTime zonedDateTime,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(formatDateTime(zonedDateTime));
        }
    }
}
