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
    private static QuickPulseConfiguration instance = new QuickPulseConfiguration();
    private  AtomicReference<String> etag = new AtomicReference<>();
    private ConcurrentHashMap<String, OpenTelMetricInfo> metrics = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private QuickPulseConfiguration() {
    }

    public static synchronized QuickPulseConfiguration getInstance() {
        if (instance == null) {
            instance = new QuickPulseConfiguration();
        }
        return instance;
    }

    public synchronized String getEtag() {
        return this.etag.get();
    }

    public synchronized void setEtag(String etag) {
        this.etag.set(etag);
    }

    public synchronized ConcurrentHashMap<String, OpenTelMetricInfo> getMetrics() {

        if (this.metrics.isEmpty()) {
            OpenTelMetricInfo metric = new OpenTelMetricInfo();
            metric.setId("my_gauge");
            metric.setAggregation("Avg");
            metric.setTelemetryType("Metric");
            metric.setProjection("my_gauge");

            OpenTelMetricInfo metric2 = new OpenTelMetricInfo();
            metric2.setId("MyFruitCounter");
            metric2.setAggregation("Avg");
            metric2.setTelemetryType("Metric");
            metric2.setProjection("MyFruitCounter");

            ConcurrentHashMap<String, OpenTelMetricInfo> sampleMetrics = new ConcurrentHashMap<>();
            sampleMetrics.put("my_gauge", metric);
            sampleMetrics.put("MyFruitCounter", metric2);
            return sampleMetrics;
        }
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
            JsonNode rootNode = objectMapper.readTree(responseBody);
            System.out.println("Metrics :" + rootNode.get("Metrics"));
            JsonNode metricsNode = rootNode.get("Metrics");
            if (metricsNode instanceof ArrayNode) {
                ArrayNode metricsArray = (ArrayNode) metricsNode;
                for (JsonNode metricNode : metricsArray) {
                    OpenTelMetricInfo metric = new OpenTelMetricInfo();
                    metric.setId(metricNode.get("Id").asText());
                    metric.setAggregation(metricNode.get("Aggregation").asText());
                    metric.setTelemetryType(metricNode.get("TelemetryType").asText());
                    metric.setProjection(metricNode.get("Projection").asText());
                    requestedMetrics.put(metricNode.get("Projection").asText(), metric);

                }
            }
            return requestedMetrics;
        } catch (Exception e) {
            logger.verbose("Failed to parse metrics from response: %s", e.getMessage());
        }
        return new ConcurrentHashMap<String, OpenTelMetricInfo>();

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


