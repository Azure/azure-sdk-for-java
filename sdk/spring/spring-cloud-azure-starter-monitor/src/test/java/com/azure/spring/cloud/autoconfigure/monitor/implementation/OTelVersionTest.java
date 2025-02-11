// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.monitor.implementation;

import io.opentelemetry.sdk.common.internal.OtelVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OTelVersionTest {

    @Test
    void lessThanWithSameVersions() {
        assertThat(new OTelVersion("1.28.0").isLessThan(new OTelVersion("1.28.0"))).isFalse();
    }

    @Test
    void lessThanWithSameMajorVersions() {
        assertThat(new OTelVersion("1.27.0").isLessThan(new OTelVersion("1.28.0"))).isTrue();
    }

    @Test
    void lessThanWithSameMajorAndMinorVersions() {
        assertThat(new OTelVersion("1.28.0").isLessThan(new OTelVersion("1.28.1"))).isTrue();
    }

    @Test
    void lessThanWithDifferentMajorVersions() {
        assertThat(new OTelVersion("1.27.0").isLessThan(new OTelVersion("2.28.0"))).isTrue();
    }

    @Test
    void greaterThanWithSameVersions() {
        assertThat(new OTelVersion("1.28.0").isGreaterThan(new OTelVersion("1.28.0"))).isFalse();
    }

    @Test
    void greaterThanWithSameMajorVersions() {
        assertThat(new OTelVersion("1.28.0").isGreaterThan(new OTelVersion("1.27.0"))).isTrue();
    }

    @Test
    void greaterThanWithSameMajorAndMinorVersions() {
        assertThat(new OTelVersion("1.28.1").isGreaterThan(new OTelVersion("1.28.0"))).isTrue();
    }

    @Test
    void greaterThanWithDifferentMajorVersions() {
        assertThat(new OTelVersion("2.28.0").isGreaterThan(new OTelVersion("1.28.0"))).isTrue();
    }

    @Test
    void verifyCurrentOpenTelemetryVersion() {
        String currentOTelVersion = OtelVersion.VERSION;
        assertThat(OpenTelemetryVersionCheckRunner.STARTER_OTEL_VERSION)
            .as(
                "Dear developer, You may have updated the OpenTelemetry dependencies of spring-cloud-azure-starter-monitor without updating the OTel starter version declared in "
                    + OpenTelemetryVersionCheckRunner.class
                    + ".")
            .isEqualTo(currentOTelVersion);
    }

}
