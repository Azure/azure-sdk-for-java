// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.http.HttpPipeline;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.monitor.opentelemetry.AzureMonitor;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.*;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.resources.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestUtils {

    private static final String TRACE_CONNECTION_STRING = "InstrumentationKey=00000000-0000-0000-0000-000000000000;"
        + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;"
        + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

    public static TelemetryItem createMetricTelemetry(String name, int value, String connectionString) {
        return createMetricTelemetry(name, value, connectionString, "state", "blocked");
    }

    public static TelemetryItem createMetricTelemetry(String name, int value, String connectionString,
        String propertyKey, String propertyValue) {
        TelemetryItem telemetry = new TelemetryItem();
        telemetry.setVersion(1);
        telemetry.setName("Metric");
        telemetry.setConnectionString(connectionString);
        Map<String, String> tags = new HashMap<>();
        tags.put("ai.internal.sdkVersion", "test_version");
        tags.put("ai.internal.nodeName", "test_role_name");
        tags.put("ai.cloud.roleInstance", "test_cloud_name");
        telemetry.setTags(tags);

        MetricsData data = new MetricsData();
        List<MetricDataPoint> dataPoints = new ArrayList<>();
        MetricDataPoint dataPoint = new MetricDataPoint();
        dataPoint.setName(name);
        dataPoint.setValue(value);
        dataPoint.setCount(1);
        dataPoints.add(dataPoint);

        Map<String, String> properties = new HashMap<>();
        properties.put(propertyKey, propertyValue);

        data.setMetrics(dataPoints);
        data.setProperties(properties);

        MonitorBase monitorBase = new MonitorBase();
        monitorBase.setBaseType("MetricData");
        monitorBase.setBaseData(data);
        telemetry.setData(monitorBase);
        telemetry.setTime(FormattedTime.offSetDateTimeFromNow());

        telemetry.setResource(Resource.empty());

        return telemetry;
    }

    public static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline) {
        return createOpenTelemetrySdk(httpPipeline, Collections.emptyMap());
    }

    public static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline,
        Map<String, String> configuration) {
        return createOpenTelemetrySdk(httpPipeline, configuration, TRACE_CONNECTION_STRING);

    }

    public static OpenTelemetrySdk createOpenTelemetrySdk(HttpPipeline httpPipeline, Map<String, String> configuration,
        String connectionString) {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        AzureMonitorExporterBuilder azureMonitorExporterBuilder
            = new AzureMonitorExporterBuilder().connectionString(connectionString).pipeline(httpPipeline);
        AzureMonitor.configure(sdkBuilder, azureMonitorExporterBuilder);

        return sdkBuilder.addPropertiesSupplier(() -> configuration).build().getOpenTelemetrySdk();
    }

    // azure-json doesn't deserialize subtypes yet, so need to convert the abstract MonitorDomain to RemoteDependencyData
    public static RemoteDependencyData toRemoteDependencyData(MonitorDomain baseData) {
        try (JsonReader jsonReader = JsonProviders.createReader(baseData.toJsonString())) {
            return RemoteDependencyData.fromJson(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // azure-json doesn't deserialize subtypes yet, so need to convert the abstract MonitorDomain to MetricsData
    public static MetricsData toMetricsData(MonitorDomain baseData) {
        try (JsonReader jsonReader = JsonProviders.createReader(baseData.toJsonString())) {
            return MetricsData.fromJson(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // azure-json doesn't deserialize subtypes yet, so need to convert the abstract MonitorDomain to MessageData
    public static MessageData toMessageData(MonitorDomain baseData) {
        try (JsonReader jsonReader = JsonProviders.createReader(baseData.toJsonString())) {
            return MessageData.fromJson(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // deserialize multiple TelemetryItem raw bytes with newline delimiters to a list of TelemetryItems
    public static List<TelemetryItem> deserialize(byte[] rawBytes) {
        try (JsonReader jsonReader = JsonProviders.createReader(rawBytes)) {
            JsonToken token = jsonReader.currentToken();
            if (token == null) {
                token = jsonReader.nextToken();
            }

            List<TelemetryItem> result = new ArrayList<>();
            do {
                result.add(TelemetryItem.fromJson(jsonReader));
            } while (jsonReader.nextToken() == JsonToken.START_OBJECT);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TestUtils() {
    }
}
