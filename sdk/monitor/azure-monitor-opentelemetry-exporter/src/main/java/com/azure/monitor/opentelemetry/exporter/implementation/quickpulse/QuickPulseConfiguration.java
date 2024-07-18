package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class QuickPulseConfiguration {
    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);
//    private static volatile QuickPulseConfiguration instance = new QuickPulseConfiguration();
    private  AtomicReference<String> etag = new AtomicReference<>();
    private ConcurrentHashMap<String, OpenTelMetricInfo> metrics = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Object lock = new Object();

/*
    private QuickPulseConfiguration() {
    }

    public static synchronized QuickPulseConfiguration getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new QuickPulseConfiguration();
                }
            }
        }
        return instance;
    }

 */

    public synchronized String getEtag() {
        return this.etag.get();
    }

    public synchronized void setEtag(String etag) {
        this.etag.set(etag);
    }

    public synchronized ConcurrentHashMap<String, OpenTelMetricInfo> getMetrics() {
        return this.metrics;
    }

    public synchronized void setMetrics(ConcurrentHashMap<String, OpenTelMetricInfo> metrics) {
        this.metrics = metrics;
    }

    public synchronized void updateConfig(String etagValue, ConcurrentHashMap<String, OpenTelMetricInfo> otelMetrics) {
        if (!Objects.equals(this.getEtag(), etagValue)){
            this.setEtag(etagValue);
            this.setMetrics(otelMetrics);
        }

    }

    public ConcurrentHashMap<String, OpenTelMetricInfo> parseMetrics(HttpResponse response) {

        HashSet<OpenTelMetricInfo> metricsSet = new HashSet<>();
        ConcurrentHashMap<String, OpenTelMetricInfo> requestedMetrics = new ConcurrentHashMap<>();
        try {

            String responseBody = response.getBodyAsString().block();
            if (responseBody == null || responseBody.isEmpty()) {
                return new ConcurrentHashMap<String, OpenTelMetricInfo>();
            }
            JsonNode rootNode = objectMapper.readTree(responseBody);
            //System.out.println("Metrics :" + rootNode.get("Metrics")); Debugging purposes
            JsonNode metricsNode = rootNode.get("Metrics");

            if (metricsNode instanceof ArrayNode) {
                ArrayNode metricsArray = (ArrayNode) metricsNode;
                for (JsonNode metricNode : metricsArray) {
                    OpenTelMetricInfo metric = new OpenTelMetricInfo();
                    metric.setId(metricNode.get("Id").asText());
                    metric.setAggregation(metricNode.get("Aggregation").asText());
                    metric.setTelemetryType(metricNode.get("TelemetryType").asText());
                    String projection = metricNode.get("Projection").asText();
                    metric.setProjection(projection);
                    if (Objects.equals(metricNode.get("TelemetryType").asText(), "Event")) {
                        int dotIndex = projection.indexOf(".");
                        if (dotIndex != -1) {
                            projection = projection.substring(dotIndex + 1);
                        }
                    }
                    metric.setProjection(projection);
                    requestedMetrics.put(projection, metric);

                }
            }
            return requestedMetrics;
        } catch (Exception e) {
            logger.verbose("Failed to parse metrics from response: %s", e.getMessage());
        }
        return new ConcurrentHashMap<String, OpenTelMetricInfo>();

    }

    public synchronized void reset() {
        this.setEtag(null);
        this.setMetrics(new ConcurrentHashMap<String, OpenTelMetricInfo>());
    }

    class OpenTelMetricInfo {
        private String id;
        private String projection;
        private String telemetryType;
        private String aggregation;

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProjection() {
            return projection;
        }

        public void setTelemetryType(String telemetryType) {
            this.telemetryType = telemetryType;
        }

        public String getTelemetryType() {
            return this.telemetryType;
        }


        public void setProjection(String projection) {
            this.projection = projection;
        }

        public String getAggregation() {
            return this.aggregation;
        }

        public void setAggregation(String aggregation) {
            this.aggregation = aggregation;
        }
    }
}


