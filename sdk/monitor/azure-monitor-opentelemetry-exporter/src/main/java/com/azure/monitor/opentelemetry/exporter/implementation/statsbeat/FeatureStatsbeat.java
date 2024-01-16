// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureStatsbeat extends BaseStatsbeat {

    private static final String FEATURE_METRIC_NAME = "Feature";

    private final Set<Feature> featureList = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> instrumentationList =
        Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final FeatureType type;

    FeatureStatsbeat(CustomDimensions customDimensions, FeatureType type) {
        // track java distribution
        super(customDimensions);
        this.type = type;
        String javaVendor = System.getProperty("java.vendor");
        featureList.add(Feature.fromJavaVendor(javaVendor));
    }

    /**
     * Returns a long that represents a list of features enabled. Each bitfield maps to a feature.
     */
    long getFeature() {
        return Feature.encode(featureList);
    }

    /**
     * Returns a long that represents a list of instrumentations. Each bitfield maps to an
     * instrumentation.
     */
    long[] getInstrumentation() {
        return Instrumentations.encode(instrumentationList);
    }

    // this is used by Exporter
    public void addInstrumentation(String instrumentation) {
        instrumentationList.add(instrumentation);
    }

    public void addFeature(Feature feature) {
        featureList.add(feature);
    }

    @Override
    protected void send(TelemetryItemExporter telemetryItemExporter) {
        String featureType;
        String featureValue = "";

        if (type == FeatureType.FEATURE) {
            featureValue = String.valueOf(getFeature());
            featureType = "0";
        } else {
            long[] encodedLongArray = getInstrumentation();
            if (encodedLongArray.length == 1) {
                featureValue = String.valueOf(encodedLongArray[0]);
            } else if (encodedLongArray.length == 2) {
                featureValue = encodedLongArray[0] + "," + encodedLongArray[1];
            }
            featureType = "1";
        }

        // don't send feature/instrumentation statsbeat when it's empty
        if (!featureValue.isEmpty()) {
            StatsbeatTelemetryBuilder telemetryBuilder = createStatsbeatTelemetry(FEATURE_METRIC_NAME, 0);
            telemetryBuilder.addProperty("feature", featureValue);
            telemetryBuilder.addProperty("type", featureType);

            telemetryItemExporter.send(Collections.singletonList(telemetryBuilder.build()));
        }
    }

    void trackConfigurationOptions(Set<Feature> featureSet) {
        featureList.addAll(featureSet);
    }
}
