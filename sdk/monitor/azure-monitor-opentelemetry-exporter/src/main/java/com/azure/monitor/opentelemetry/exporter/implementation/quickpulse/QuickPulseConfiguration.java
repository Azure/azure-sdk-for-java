package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class QuickPulseConfiguration {
    private static final ClientLogger logger = new ClientLogger(QuickPulseDataFetcher.class);
    private AtomicReference<String> etag = new AtomicReference<>();
    private ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> derivedMetrics = new ConcurrentHashMap<>();

    public synchronized String getEtag() {
        return this.etag.get();
    }

    public synchronized void setEtag(String etag) {
        this.etag.set(etag);
    }

    public synchronized ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> getDerivedMetrics() {
        return this.derivedMetrics;
    }

    public synchronized void setDerivedMetrics(ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> metrics) {
        this.derivedMetrics = metrics;
    }

    public synchronized void updateConfig(String etagValue,
        ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> otelMetrics) {
        if (!Objects.equals(this.getEtag(), etagValue)) {
            this.setEtag(etagValue);
            this.setDerivedMetrics(otelMetrics);
        }

    }

    public ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> parseDerivedMetrics(HttpResponse response)
        throws IOException {

        ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>> requestedMetrics = new ConcurrentHashMap<>();
        try {

            String responseBody = response.getBodyAsString().block();
            if (responseBody == null || responseBody.isEmpty()) {
                return new ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>>();
            }

            try (JsonReader jsonReader = JsonProviders.createReader(responseBody)) {
                jsonReader.nextToken();
                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    if ("Metrics".equals(jsonReader.getFieldName())) {
                        jsonReader.nextToken();

                        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                            DerivedMetricInfo metric = new DerivedMetricInfo();

                            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {

                                String fieldName = jsonReader.getFieldName();
                                jsonReader.nextToken();

                                switch (fieldName) {
                                    case "Id":
                                        metric.setId(jsonReader.getString());
                                        break;

                                    case "Aggregation":
                                        metric.setAggregation(jsonReader.getString());
                                        break;

                                    case "TelemetryType":
                                        metric.setTelemetryType(jsonReader.getString());
                                        break;

                                    case "Projection":
                                        metric.setProjection(jsonReader.getString());
                                        break;

                                    case "FilterGroups":
                                        // Handle "FilterGroups" field
                                        if (jsonReader.currentToken() == JsonToken.START_ARRAY) {
                                            while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                                                if (jsonReader.currentToken() == JsonToken.START_OBJECT) {
                                                    while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                                                        if (jsonReader.currentToken() == JsonToken.FIELD_NAME
                                                            && jsonReader.getFieldName().equals("Filters")) {
                                                            jsonReader.nextToken();
                                                            if (jsonReader.currentToken() == JsonToken.START_ARRAY) {
                                                                while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                                                                    if (jsonReader.currentToken()
                                                                        == JsonToken.START_OBJECT) {
                                                                        String innerFieldName = "";
                                                                        String predicate = "";
                                                                        String comparand = "";

                                                                        while (jsonReader.nextToken()
                                                                            != JsonToken.END_OBJECT) {
                                                                            String filterFieldName
                                                                                = jsonReader.getFieldName();
                                                                            jsonReader.nextToken();

                                                                            switch (filterFieldName) {
                                                                                case "FieldName":
                                                                                    innerFieldName
                                                                                        = jsonReader.getString();
                                                                                    if (innerFieldName.contains(".")) {
                                                                                        innerFieldName = innerFieldName
                                                                                            .split("\\.")[1];
                                                                                    }
                                                                                    break;

                                                                                case "Predicate":
                                                                                    predicate = jsonReader.getString();
                                                                                    break;

                                                                                case "Comparand":
                                                                                    comparand = jsonReader.getString();
                                                                                    break;
                                                                            }
                                                                        }

                                                                        if (!innerFieldName.isEmpty()
                                                                            && !innerFieldName.equals("undefined")
                                                                            && !predicate.isEmpty()
                                                                            && !comparand.isEmpty()) {
                                                                            metric.addFilterGroup(innerFieldName,
                                                                                predicate, comparand);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        break;

                                    default:
                                        jsonReader.skipChildren();
                                        break;
                                }
                            }
                            requestedMetrics.computeIfAbsent(metric.getTelemetryType(), k -> new ArrayList<>())
                                .add(metric);
                        }
                    } else {
                        jsonReader.skipChildren();

                    }
                }
            }
            return requestedMetrics;
        } catch (Exception e) {
            logger.verbose("Failed to parse metrics from response: %s", e.getMessage());
        }
        return new ConcurrentHashMap<String, ArrayList<DerivedMetricInfo>>();
    }

    public class DerivedMetricInfo {
        private String id;
        private String projection;
        private String telemetryType;
        private String aggregation;
        private ArrayList<FilterGroup> filterGroups = new ArrayList<FilterGroup>();

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

        public ArrayList<FilterGroup> getFilterGroups() {
            return this.filterGroups;
        }

        public void addFilterGroup(String fieldName, String predicate, String comparand) {
            this.filterGroups.add(new FilterGroup(fieldName, predicate, comparand));
        }
    }

    class FilterGroup {
        private String fieldName;
        private String operator;
        private String comparand;

        public FilterGroup(String fieldName, String predicate, String comparand) {
            this.setFieldName(fieldName);
            this.setOperator(predicate);
            this.setComparand(comparand);
        }

        public String getFieldName() {
            return this.fieldName;
        }

        private void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getOperator() {
            return this.operator;
        }

        private void setOperator(String operator) {
            this.operator = operator;
        }

        public String getComparand() {
            return this.comparand;
        }

        public void setComparand(String comparand) {
            this.comparand = comparand;
        }
    }

}
