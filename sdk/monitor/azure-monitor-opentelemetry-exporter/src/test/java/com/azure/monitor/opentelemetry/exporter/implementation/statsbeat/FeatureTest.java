// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureTest {

    private static final Set<Feature> features;

    static {
        features = new HashSet<>();
        features.add(Feature.JAVA_VENDOR_ZULU);
        features.add(Feature.AAD);
        features.add(Feature.AZURE_SDK_DISABLED);
        features.add(Feature.JDBC_DISABLED);
        features.add(Feature.SPRING_INTEGRATION_DISABLED);
        features.add(Feature.STATSBEAT_DISABLED);
    }

    private static final long EXPECTED_FEATURE = (long) (Math.pow(2, 1) + Math.pow(2, 6) + Math.pow(2, 8)
        + Math.pow(2, 15) + Math.pow(2, 17) + Math.pow(2, 20)); // Exponents are keys from StatsbeatTestUtils.FEATURE_MAP_DECODING.)

    @Test
    public void tesEncodeAndDecodeFeature() {
        long number = Feature.encode(features);
        assertThat(number).isEqualTo(EXPECTED_FEATURE);
        Set<Feature> result = StatsbeatTestUtils.decodeFeature(number);
        assertThat(result).isEqualTo(features);
    }

    @Test
    public void testRabittmqDisabledDecodeFeature() {
        // long values with RABBITMQ_DISABLED on bit are from Statsbeat Kusto
        long[] numbers = new long[] {
            37495182980L,
            585629569L,
            37090492292L,
            3135444612L,
            15204225L,
            2732851076L,
            585367456L,
            2730753952L,
            37495185056L,
            37092585348L,
            174531509920L,
            70405836767105L };
        for (long number : numbers) {
            Set<Feature> result = StatsbeatTestUtils.decodeFeature(number);
            assertThat(result.contains(Feature.RABBITMQ_DISABLED)).isTrue();
        }
    }
}
