package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class QuickPulseConfiguration {
    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);
    private  AtomicReference<String> etag = new AtomicReference<>();
    private ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> derivedMetrics = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public synchronized String getEtag() {
        return this.etag.get();
    }

    public synchronized void setEtag(String etag) {
        this.etag.set(etag);
    }

    public synchronized ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> getDerivedMetrics() {
        return this.derivedMetrics;
    }

    public synchronized void setDerivedMetrics(ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> derivedMetrics) {
        this.derivedMetrics = derivedMetrics;
    }

    public synchronized void updateConfig(String etagValue, ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> otelMetrics) {
        if (!Objects.equals(this.getEtag(), etagValue)){
            this.setEtag(etagValue);
            this.setDerivedMetrics(otelMetrics);
        }

    }

    public ConcurrentHashMap<String,  ArrayList<DerivedMetricInfo>> parseDerivedMetrics(HttpResponse response) {

        ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> requestedMetrics = new ConcurrentHashMap<>();
        try {

            String responseBody = response.getBodyAsString().block();
            if (responseBody == null || responseBody.isEmpty()) {
                return new ConcurrentHashMap<String,  ArrayList<DerivedMetricInfo>>();
            }
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode metricsNode = rootNode.get("Metrics");

            if (metricsNode instanceof ArrayNode) {
                ArrayNode metricsArray = (ArrayNode) metricsNode;

                for (JsonNode metricNode : metricsArray) {
                    DerivedMetricInfo metric = new DerivedMetricInfo();
                    metric.setId(metricNode.get("Id").asText());
                    metric.setAggregation(metricNode.get("Aggregation").asText());
                    metric.setTelemetryType(metricNode.get("TelemetryType").asText());
                    metric.setProjection(metricNode.get("Projection").asText());
                    requestedMetrics.computeIfAbsent(metric.getProjection(), k -> new ArrayList<>()).add(metric);

                }
            }
            return requestedMetrics;
        } catch (Exception e) {
            logger.verbose("Failed to parse metrics from response: %s", e.getMessage());
        }
        return new ConcurrentHashMap<String,  ArrayList<DerivedMetricInfo>>();

    }

    public synchronized void reset() {
        this.setEtag(null);
        this.derivedMetrics.clear();
    }

    class DerivedMetricInfo {
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


