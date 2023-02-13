// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

public final class CosmosMetricNames {

    private CosmosMetricNames() {}

    public final static class Direct {
        public final static class AddressResolution {
            // Latency of the RNTBD address resolution request (Timer)
            public final static String Latency = "rntbd.addressResolution.latency";

            // Number of RNTBD address resolution requests (Counter)
            public final static String Requests = "rntbd.addressResolution.requests";
        }

        public final static class Channels {
            // Snapshot of the number of acquired channels for this endpoint (FunctionCounter)
            public final static String Acquired = "rntbd.channels.acquired.count";

            // Snapshot of the number of available channels for this endpoint (Gauge)
            public final static String Available = "rntbd.channels.available.count";

            // Snapshot of the number of closed channels for this endpoint (FunctionCounter)
            public final static String Closed = "rntbd.channels.closed.count";
        }

        public final static class Requests {
            // Latency of RNTBD requests for this endpoint (Timer)
            public final static String Latency = "rntbd.requests.latency";

            // Latency of failed RNTBD requests for this endpoint (Timer)
            public final static String FailedRequestLatency = "rntbd.requests.failed.latency";

            // Latency of successful RNTBD requests for this endpoint (Timer)
            public final static String SuccessRequestLatency = "rntbd.requests.successful.latency";

            // Snapshot of number of concurrent RNTBD requests for this endpoint (Gauge)
            public final static String Concurrent = "rntbd.requests.concurrent.count";

            // Snapshot of number of queued RNTBD requests for this endpoint (Gauge)
            public final static String Queued = "rntbd.requests.queued.count";

            // Size of the request payload (DistributionSummary)
            public final static String RequestPayloadSize = "rntbd.req.reqSize";

            // Size of the response payload (DistributionSummary)
            public final static String ResponsePayloadSize = "rntbd.req.rspSize";
        }

        public final static class Endpoints {
            // Snapshot of the number of endpoints (Gauge)
            public final static String Count = "rntbd.endpoints.count";

            // Snapshot of the number of evicted/closed endpoints (FunctionCounter)
            public final static String EvictedCount = "rntbd.endpoints.evicted";
        }
    }

    public final static class OperationSummary {
        // Number of operation calls (Counter)
        public final static String Calls = "op.calls";

        // Total latency (across requests including retries) of the operation (Timer)
        public final static String Latency = "op.latency";

        // Actual item count - relevant for non-point-operations - indicating the actual number of
        // docs returned in the response (DistributionSummary)
        public final static String ActualItemCount = "op.actualItemCount";

        // Max. item count - relevant for non-point-operations - indicating the requested max. number of
        // docs returned in a single response (DistributionSummary)
        public final static String MaxItemCount = "op.maxItemCount";

        // Request charge for the operation (DistributionSummary)
        public final static String RequestCharge = "op.RUs";
    }

    public final static class OperationDetails {
        // Number of regions contacted for processing the operation (DistributionSummary)
        public final static String RegionsContacted = "op.regionsContacted";
    }

    public final static class RequestDetails {
        public final static class Direct {
            // Set of latencies in different steps of the request pipeline (all Timers)
            public final static String TimelinePrefix = "req.rntbd.timeline.";
        }

        public final static class Gateway {
            // Set of latencies in different steps of the request pipeline (all Timers)
            public final static String TimelinePrefix = "req.gw.timeline.";
        }
    }

    public final static class RequestSummary {

        // Size of the request payload (DistributionSummary)
        public final static String RequestPayloadSize = "req.reqPayloadSize";

        // Size of the response payload (DistributionSummary)
        public final static String ResponsePayloadSize = "req.rspPayloadSize";

        public final static class Direct {
            // Backend-latency of the request (DistributionSummary)
            public final static String BackendLatency = "req.rntbd.backendLatency";

            // Latency of the request (Timer)
            public final static String Latency = "req.rntbd.latency";

            // Request charge for a request (DistributionSummary)
            public final static String RequestCharge = "req.rntbd.RUs";

            // Number of requests (Counter)
            public final static String Requests = "req.rntbd.requests";
        }

        public final static class Gateway {
            // Latency of the request (Timer)
            public final static String Latency = "req.gw.latency";

            // Request charge for a request (DistributionSummary)
            public final static String RequestCharge = "req.gw.RUs";

            // Number of requests (Counter)
            public final static String Requests = "req.gw.requests";
        }
    }

    public final static class System {
        // JVM's Free available memory (DistributionSummary)
        public final static String FreeMemoryAvailable = "system.freeMemoryAvailable";

        // Avg. system-wide CPU load (DistributionSummary)
        public final static String AvgCpuLoad = "system.avgCpuLoad";
    }

    public final static class DisabledByDefaultLegacy {

        public final static String LegacyDirectTcpMetricsPrefix = "azure.cosmos.directTcp.";

        public final static class RntbdRequestEndpointStatistics {

            // Snapshot of acquired channels for the endpoint at time of request (DistributionSummary)
            public final static String AcquiredChannels = "req.rntbd.stats.endpoint.acquiredChannels";

            // Snapshot of available channels for the endpoint at time of request (DistributionSummary)
            public final static String AvailableChannels = "req.rntbd.stats.endpoint.availableChannels";

            // Snapshot of number of inflight requests for the endpoint at time of request (DistributionSummary)
            public final static String InflightRequests = "req.rntbd.stats.endpoint.inflightRequests";
        }
    }
}
