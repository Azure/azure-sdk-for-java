/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import java.net.URI;
import java.net.URISyntaxException;
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

import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.directconnectivity.StoreResult;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import org.apache.commons.lang3.StringUtils;

public class ClientSideRequestStatistics {

    private final static int MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING = 10;

    private final static DateTimeFormatter responseTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss.SSS").withLocale(Locale.US);

    private ZonedDateTime requestStartTime;
    private ZonedDateTime requestEndTime;

    private List<StoreResponseStatistics> responseStatisticsList;
    private List<StoreResponseStatistics> supplementalResponseStatisticsList;
    private Map<String, AddressResolutionStatistics> addressResolutionStatistics;

    private List<URI> contactedReplicas;
    private Set<URI> failedReplicas;
    private Set<URI> regionsContacted;

    public ClientSideRequestStatistics() {
        this.requestStartTime = ZonedDateTime.now(ZoneOffset.UTC);
        this.requestEndTime = ZonedDateTime.now(ZoneOffset.UTC);
        this.responseStatisticsList = new ArrayList<>();
        this.supplementalResponseStatisticsList = new ArrayList<>();
        this.addressResolutionStatistics = new HashMap<>();
        this.contactedReplicas = new ArrayList<>();
        this.failedReplicas = new HashSet<>();
        this.regionsContacted = new HashSet<>();
    }

    public Duration getRequestLatency() {
        return Duration.between(requestStartTime, requestEndTime);
    }

    private boolean isCPUOverloaded() {
        //  NOTE: CPUMonitor and CPULoadHistory is not implemented in async SDK yet.
        return false;
    }

    public void recordResponse(RxDocumentServiceRequest request, StoreResult storeResult) {
        ZonedDateTime responseTime = ZonedDateTime.now(ZoneOffset.UTC);

        StoreResponseStatistics storeResponseStatistics = new StoreResponseStatistics();
        storeResponseStatistics.requestResponseTime = responseTime;
        storeResponseStatistics.storeResult = storeResult;
        storeResponseStatistics.requestOperationType = request.getOperationType();
        storeResponseStatistics.requestResourceType = request.getResourceType();

        URI locationEndPoint = null;
        if (request.requestContext.locationEndpointToRoute != null) {
            try {
                locationEndPoint = request.requestContext.locationEndpointToRoute.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
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

    public String recordAddressResolutionStart(URI targetEndpoint) {
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

    public void recordAddressResolutionEnd(String identifier) {
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
        StringBuilder stringBuilder = new StringBuilder();

        //  need to lock in case of concurrent operations. this should be extremely rare since toString()
        //  should only be called at the end of request.
        synchronized (this) {

            //  first trace request start time, as well as total non-head/headfeed requests made.
            stringBuilder.append("RequestStartTime: ")
                    .append("\"").append(this.requestStartTime.format(responseTimeFormatter)).append("\"")
                    .append(", ")
                    .append("RequestEndTime: ")
                    .append("\"").append(this.requestEndTime.format(responseTimeFormatter)).append("\"")
                    .append(", ")
                    .append("Duration: ")
                    .append(Duration.between(requestStartTime, requestEndTime).toMillis())
                    .append(" ms, ")
                    .append("NUMBER of regions attempted: ")
                    .append(this.regionsContacted.isEmpty() ? 1 : this.regionsContacted.size())
                    .append(System.lineSeparator());

            //  take all responses here - this should be limited in number and each one contains relevant information.
            for (StoreResponseStatistics storeResponseStatistics : this.responseStatisticsList) {
                stringBuilder.append(storeResponseStatistics.toString()).append(System.lineSeparator());
            }

            //  take all responses here - this should be limited in number and each one is important.
            for (AddressResolutionStatistics value : this.addressResolutionStatistics.values()) {
                stringBuilder.append(value.toString()).append(System.lineSeparator());
            }

            //  only take last 10 responses from this list - this has potential of having large number of entries.
            //  since this is for establishing consistency, we can make do with the last responses to paint a meaningful picture.
            int supplementalResponseStatisticsListCount = this.supplementalResponseStatisticsList.size();
            int initialIndex = Math.max(supplementalResponseStatisticsListCount - MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING, 0);
            if (initialIndex != 0) {
                stringBuilder.append("  -- Displaying only the last ")
                        .append(MAX_SUPPLEMENTAL_REQUESTS_FOR_TO_STRING)
                        .append(" head/headfeed requests. Total head/headfeed requests: ")
                        .append(supplementalResponseStatisticsListCount);
            }
            for (int i = initialIndex; i < supplementalResponseStatisticsListCount; i++) {
                stringBuilder.append(this.supplementalResponseStatisticsList.get(i).toString()).append(System.lineSeparator());
            }
        }
        String requestStatsString = stringBuilder.toString();
        if (!requestStatsString.isEmpty()) {
            return System.lineSeparator() + requestStatsString;
        }
        return StringUtils.EMPTY;
    }

    public List<URI> getContactedReplicas() {
        return contactedReplicas;
    }

    public void setContactedReplicas(List<URI> contactedReplicas) {
        this.contactedReplicas = contactedReplicas;
    }

    public Set<URI> getFailedReplicas() {
        return failedReplicas;
    }

    public void setFailedReplicas(Set<URI> failedReplicas) {
        this.failedReplicas = failedReplicas;
    }

    public Set<URI> getRegionsContacted() {
        return regionsContacted;
    }

    public void setRegionsContacted(Set<URI> regionsContacted) {
        this.regionsContacted = regionsContacted;
    }

    private static String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(responseTimeFormatter);
    }

    private class StoreResponseStatistics {

        private ZonedDateTime requestResponseTime;
        private StoreResult storeResult;
        private ResourceType requestResourceType;
        private OperationType requestOperationType;

        @Override
        public String toString() {
            return "StoreResponseStatistics{" +
                    "requestResponseTime=\"" + formatDateTime(requestResponseTime) + "\"" +
                    ", storeResult=" + storeResult +
                    ", requestResourceType=" + requestResourceType +
                    ", requestOperationType=" + requestOperationType +
                    '}';
        }
    }

    private class AddressResolutionStatistics {
        private ZonedDateTime startTime;
        private ZonedDateTime endTime;
        private String targetEndpoint;

        AddressResolutionStatistics() {
        }

        @Override
        public String toString() {
            return "AddressResolutionStatistics{" +
                    "startTime=\"" + formatDateTime(startTime) + "\"" +
                    ", endTime=\"" + formatDateTime(endTime) + "\"" +
                    ", targetEndpoint='" + targetEndpoint + '\'' +
                    '}';
        }
    }
}
