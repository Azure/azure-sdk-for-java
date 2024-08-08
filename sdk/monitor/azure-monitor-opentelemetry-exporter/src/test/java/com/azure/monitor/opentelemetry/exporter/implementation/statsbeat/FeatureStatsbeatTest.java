// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureStatsbeatTest {

    @Test
    public void testAadEnabled() {
        testFeatureTrackingEnablement(Feature.AAD);
    }

    @Test
    public void testLegacyPropagationEnabled() {
        testFeatureTrackingEnablement(Feature.LEGACY_PROPAGATION_ENABLED);
    }

    @Test
    public void testCassandraEnabled() {
        testFeatureTrackingDisablement(Feature.CASSANDRA_DISABLED);
    }

    @Test
    public void testJdbcEnabled() {
        testFeatureTrackingDisablement(Feature.JDBC_DISABLED);
    }

    @Test
    public void testJmsEnabled() {
        testFeatureTrackingDisablement(Feature.JMS_DISABLED);
    }

    @Test
    public void testKafkaEnabled() {
        testFeatureTrackingDisablement(Feature.KAFKA_DISABLED);
    }

    @Test
    public void testMicrometerEnabled() {
        testFeatureTrackingDisablement(Feature.MICROMETER_DISABLED);
    }

    @Test
    public void testMongoEnabled() {
        testFeatureTrackingDisablement(Feature.MONGO_DISABLED);
    }

    @Test
    public void testRedisEnabled() {
        testFeatureTrackingDisablement(Feature.REDIS_DISABLED);
    }

    @Test
    public void testSpringSchedulingEnabled() {
        testFeatureTrackingDisablement(Feature.SPRING_SCHEDULING_DISABLED);
    }

    @Test
    public void testAddInstrumentationFirstLong() {
        FeatureStatsbeat instrumentationStatsbeat
            = new FeatureStatsbeat(new CustomDimensions(), FeatureType.INSTRUMENTATION);
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.jdbc");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.tomcat-7.0");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.http-url-connection");
        long[] expectedLongArray = new long[1];
        expectedLongArray[0] = (long) (Math.pow(2, 5) + Math.pow(2, 13) + Math.pow(2, 21)); // Exponents are keys from StatsbeatTestUtils.INSTRUMENTATION_MAP_DECODING
        assertThat(instrumentationStatsbeat.getInstrumentation()).isEqualTo(expectedLongArray);
    }

    @Test
    public void testAddInstrumentationToSecondLongOnly() {
        FeatureStatsbeat instrumentationStatsbeat
            = new FeatureStatsbeat(new CustomDimensions(), FeatureType.INSTRUMENTATION);
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.undertow-1.4");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.play-ws-2.0");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.vertx-kafka-client-3.5");
        long[] expectedLongArray = new long[2];
        expectedLongArray[0] = 0;
        expectedLongArray[1] = (long) (Math.pow(2, 67 - 64) + Math.pow(2, 69 - 64) + Math.pow(2, 71 - 64)); // Exponents are keys from
        // StatsbeatTestUtils.INSTRUMENTATION_MAP_DECODING - 1
        assertThat(instrumentationStatsbeat.getInstrumentation()).isEqualTo(expectedLongArray);
    }

    @Test
    public void testAddInstrumentationToBoth() {
        FeatureStatsbeat instrumentationStatsbeat
            = new FeatureStatsbeat(new CustomDimensions(), FeatureType.INSTRUMENTATION);
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.undertow-1.4");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.play-ws-2.0");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.vertx-kafka-client-3.5");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.jdbc");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.tomcat-7.0");
        instrumentationStatsbeat.addInstrumentation("io.opentelemetry.http-url-connection");
        long[] expectedLongArray = new long[2];
        expectedLongArray[0] = (long) (Math.pow(2, 5) + Math.pow(2, 13) + Math.pow(2, 21));
        expectedLongArray[1] = (long) (Math.pow(2, 67 - 64) + Math.pow(2, 69 - 64) + Math.pow(2, 71 - 64));
        assertThat(instrumentationStatsbeat.getInstrumentation()).isEqualTo(expectedLongArray);
    }

    private static void testFeatureTrackingEnablement(Feature feature) {
        testFeature(feature, false);
        testFeature(feature, true);
    }

    private static void testFeatureTrackingDisablement(Feature feature) {
        testFeature(feature, true);
        testFeature(feature, false);
    }

    private static void testFeature(Feature feature, boolean expected) {
        // given
        FeatureStatsbeat featureStatsbeat = new FeatureStatsbeat(new CustomDimensions(), FeatureType.FEATURE);

        // when
        if (expected) {
            featureStatsbeat.trackConfigurationOptions(Collections.singleton(feature));
        }

        // then
        boolean actual = getBitAtIndex(featureStatsbeat.getFeature(), feature.getBitmapIndex());
        assertThat(actual).isEqualTo(expected);
    }

    private static boolean getBitAtIndex(long feature, int index) {
        BitSet bitSet = BitSet.valueOf(new long[] { feature });
        return bitSet.get(index);
    }
}
