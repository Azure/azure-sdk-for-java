// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO (heya) identify and separate feature list between agent and exporter or add a flag to
// indicate which module that feature belongs
public enum Feature {
    JAVA_VENDOR_ORACLE(0),
    JAVA_VENDOR_ZULU(1),
    JAVA_VENDOR_MICROSOFT(2),
    JAVA_VENDOR_ADOPT_OPENJDK(3),
    JAVA_VENDOR_REDHAT(4),
    JAVA_VENDOR_OTHER(5),
    AAD(6),
    CASSANDRA_DISABLED(7),
    JDBC_DISABLED(8),
    JMS_DISABLED(9),
    KAFKA_DISABLED(10),
    MICROMETER_DISABLED(11),
    MONGO_DISABLED(12),
    REDIS_DISABLED(13),
    SPRING_SCHEDULING_DISABLED(14),
    AZURE_SDK_DISABLED(15),
    RABBITMQ_DISABLED(16),
    SPRING_INTEGRATION_DISABLED(17), // preview instrumentation, spring-integration is ON by default in OTEL
    LEGACY_PROPAGATION_ENABLED(18), // legacy propagation is disabled by default
    GRIZZLY_ENABLED(19), // preview instrumentation, grizzly is OFF by default in OTEL
    STATSBEAT_DISABLED(20), // disable non-essential statsbeat
    QUARTZ_DISABLED(21),
    APACHE_CAMEL_DISABLED(22), // preview instrumentation, apache camel is ON by default in OTEL
    AKKA_DISABLED(23), // preview instrumentation, akka is ON by default in OTEL
    PROPAGATION_DISABLED(24),
    PLAY_DISABLED(25), // preview instrumentation, play is ON by default in OTEL
    CAPTURE_HTTP_SERVER_4XX_AS_SUCCESS(26),
    CAPTURE_HTTP_SERVER_HEADERS(27),
    CAPTURE_HTTP_CLIENT_HEADERS(28),
    VERTX_DISABLED(29), // preview instrumentation, vertx is ON by default in OTEL
    CUSTOM_DIMENSIONS_ENABLED(30), // enable customDimensions
    JAXRS_ANNOTATIONS_DISABLED(31), // can cause startup slowness, jaxrs-annotations is ON by default in OTEL
    LOGGING_LEVEL_CUSTOM_PROPERTY_ENABLED(32), // preview opt-in to include LoggingLevel
    TELEMETRY_PROCESSOR_ENABLED(33),
    SDK_2X_BRIDGE_VIA_3X_AGENT(34), // track 2.x bridge usage via 3.x codeless agent
    PROFILER_ENABLED(35),
    BROWSER_SDK_LOADER(36), // track javascript snippet
    SAMPLING(37), // track sampling feature
    PREVIEW_CAPTURE_CONTROLLER_SPANS(38), // track captureControllerSpans
    PREVIEW_LIVE_METRICS(39), // track liveMetrics
    PREVIEW_LEGACY_REQUEST_ID_PROPAGATION(40), // track legacyRequestIdPropagation
    PREVIEW_DISABLE_PROPAGATION(41), // track disablePropagation
    PREVIEW_CAPTURE_LOGGING_LEVEL_AS_CUSTOM_DIMENSION(42), // track captureLoggingLevelAsCustomDimension
    PREVIEW_CAPTURE_LOGBACK_CODE_ATTRIBUTES(43), // track captureLogbackCodeAttributes
    PREVIEW_CAPTURE_LOGBACK_MARKER(44), // track captureLogbackMarker
    PREVIEW_CAPTURE_LOG4J_MARKER(45), // track captureLog4jMarker
    PREVIEW_INHERITED_ATTRIBUTES(46), // track inheritedAttributes
    PREVIEW_GC_EVENTS(47), // track gcEvents
    PREVIEW_CONNECTION_STRING_OVERRIDES(48), // track connectionStringOverrides
    PREVIEW_ROLE_NAME_OVERRIDES(49), // track roleNameOverrides
    PREVIEW_INSTRUMENTATION_OVERRIDES(50), // track instrumentationKeyOverrides
    PREVIEW_GENERAL_EXPORT_QUEUE_CAPACITY(51), // track generalExportQueueCapacity
    PREVIEW_METRICS_EXPORT_QUEUE_CAPACITY(52), // track metricsExportQueueCapacity
    PREVIEW_DISK_PERSISTENCE_MAX_SIZE_MB(53), // track diskPersistenceMaxSizeMb
    PREVIEW_DONT_USE_NORMALIZED(54), // track useNormalizedValueForNonNormalizedCpuPercentage
    PREVIEW_CUSTOM_INSTRUMENTATION(55), // track customInstrumentation
    PREVIEW_ADDITIONAL_PROPAGATORS(56), // track additionalPropagators
    GRAAL_VM_NATIVE(57),
    PEKKO_DISABLED(58) // preview instrumentation
    ;

    private static final Map<String, Feature> javaVendorFeatureMap;

    static {
        javaVendorFeatureMap = new HashMap<>();
        javaVendorFeatureMap.put("Oracle Corporation", Feature.JAVA_VENDOR_ORACLE); // https://www.oracle.com/technetwork/java/javase/downloads/index.html
        javaVendorFeatureMap.put("Azul Systems, Inc.", Feature.JAVA_VENDOR_MICROSOFT); // https://www.azul.com/downloads/zulu/
        javaVendorFeatureMap.put("Microsoft", Feature.JAVA_VENDOR_MICROSOFT); // https://www.azul.com/downloads/zulu/
        javaVendorFeatureMap.put("AdoptOpenJDK", Feature.JAVA_VENDOR_ADOPT_OPENJDK); // https://adoptopenjdk.net/
        javaVendorFeatureMap.put("Red Hat, Inc.", Feature.JAVA_VENDOR_REDHAT); // https://developers.redhat.com/products/openjdk/download/
    }

    private final int bitmapIndex;

    Feature(int bitmapIndex) {
        this.bitmapIndex = bitmapIndex;
    }

    static Feature fromJavaVendor(String javaVendor) {
        Feature feature = javaVendorFeatureMap.get(javaVendor);
        return feature != null ? feature : Feature.JAVA_VENDOR_OTHER;
    }

    static long encode(Set<Feature> features) {
        BitSet bitSet = new BitSet(64);
        for (Feature feature : features) {
            bitSet.set(feature.bitmapIndex);
        }

        long[] longArray = bitSet.toLongArray();
        if (longArray.length > 0) {
            return longArray[0];
        }

        return 0L;
    }

    // only used by tests
    int getBitmapIndex() {
        return bitmapIndex;
    }
}
