// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Connected;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unhealthy;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.UnhealthyPending;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unknown;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UriTests {
    @Test(groups = "unit")
    public void setHealthyStatusTests() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Uri testUri = new Uri("https://127.0.0.1:8080");

        List<Uri.HealthStatus> statusCanBeOverwritten =
                Arrays.asList(Unknown, Connected, UnhealthyPending);
        AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);

        for (Uri.HealthStatus initialStatus : statusCanBeOverwritten) {
            healthStatus.set(initialStatus);
            testUri.setHealthStatus(Connected);
            assertThat(testUri.getHealthStatus()).isEqualTo(Connected);
        }

        // if the status is unhealthy, it can only be overwritten to connected after some extended time
        Field lastUnhealthyTimestampField = Uri.class.getDeclaredField("lastUnhealthyTimestamp");
        lastUnhealthyTimestampField.setAccessible(true);
        healthStatus.set(Unhealthy);

        lastUnhealthyTimestampField.set(testUri, Instant.now());
        testUri.setHealthStatus(Connected);
        assertThat(testUri.getHealthStatus()).isEqualTo(Unhealthy);

        lastUnhealthyTimestampField.set(testUri, Instant.now().minusMillis(Duration.ofMinutes(2).toMillis()));
        testUri.setHealthStatus(Connected);
        assertThat(testUri.getHealthStatus()).isEqualTo(Connected);
    }

    @Test(groups = "unit")
    public void setUnhealthyStatusTests() throws NoSuchFieldException, IllegalAccessException {

        Field lastUnhealthyTimestampField = Uri.class.getDeclaredField("lastUnhealthyTimestamp");
        lastUnhealthyTimestampField.setAccessible(true);

        // Unhealthy status can override any other status
        for (Uri.HealthStatus initialStatus : Uri.HealthStatus.values()) {

            Uri testUri = new Uri("https://127.0.0.1:8080");
            AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);
            healthStatus.set(initialStatus);
            Instant lastUnhealthyTimestampBefore = (Instant) lastUnhealthyTimestampField.get(testUri);

            testUri.setHealthStatus(Unhealthy);
            Instant lastUnhealthyTimestampAfter = (Instant) lastUnhealthyTimestampField.get(testUri);

            assertThat(testUri.getHealthStatus()).isEqualTo(Unhealthy);
            assertThat(lastUnhealthyTimestampAfter).isNotEqualTo(lastUnhealthyTimestampBefore);
        }
    }

    @Test(groups = "unit")
    public void setUnhealthyPendingStatusTests() throws NoSuchFieldException, IllegalAccessException {
        List<Uri.HealthStatus> statusCanBeOverwritten = Arrays.asList(UnhealthyPending, Unhealthy);
        List<Uri.HealthStatus> statusSkipped = Arrays.asList(Unknown, Connected);

        Field lastUnhealthyPendingTimestampField = Uri.class.getDeclaredField("lastUnhealthyPendingTimestamp");
        lastUnhealthyPendingTimestampField.setAccessible(true);

        for (Uri.HealthStatus initialHealthStatus : Uri.HealthStatus.values()) {
            Uri testUri = new Uri("https://127.0.0.1:8080");
            AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);

            healthStatus.set(initialHealthStatus);
            Instant lastUnhealthyPendingTimestampBefore = (Instant) lastUnhealthyPendingTimestampField.get(testUri);

            testUri.setHealthStatus(UnhealthyPending);
            Instant lastUnhealthyPendingTimestampAfter = (Instant) lastUnhealthyPendingTimestampField.get(testUri);

            if (statusCanBeOverwritten.contains(initialHealthStatus)) {
                assertThat(testUri.getHealthStatus()).isEqualTo(UnhealthyPending);
                assertThat(lastUnhealthyPendingTimestampAfter).isNotEqualTo(lastUnhealthyPendingTimestampBefore);

            } else if (statusSkipped.contains(initialHealthStatus)) {
                assertThat(testUri.getHealthStatus()).isEqualTo(initialHealthStatus);
                if (lastUnhealthyPendingTimestampBefore == null) {
                    assertThat(lastUnhealthyPendingTimestampAfter).isNull();
                } else {
                    assertThat(lastUnhealthyPendingTimestampAfter).isEqualTo(lastUnhealthyPendingTimestampBefore);
                }

            } else {
                throw new IllegalStateException("Unknown health status: " + initialHealthStatus.toString());
            }
        }
    }

    @Test(groups = "unit")
    public void setUnknownStatusTests() throws NoSuchFieldException, IllegalAccessException {
        Uri testUri = new Uri("https://127.0.0.1:8080");
        AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);

        Field lastUnknownTimestampField = Uri.class.getDeclaredField("lastUnknownTimestamp");
        lastUnknownTimestampField.setAccessible(true);

        assertThat(lastUnknownTimestampField).isNotNull();

        for (Uri.HealthStatus initialHealthStatus : Uri.HealthStatus.values()) {
            healthStatus.set(initialHealthStatus);
            Instant lastUnknownTimestampBefore = (Instant) lastUnknownTimestampField.get(testUri);

            assertThatThrownBy(() -> testUri.setHealthStatus(Unknown))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("It is impossible to set to unknown status");

            Instant lastUnknownTimestampAfter = (Instant) lastUnknownTimestampField.get(testUri);
            assertThat(lastUnknownTimestampBefore).isEqualTo(lastUnknownTimestampAfter);
        }
    }

    @Test(groups = "unit")
    public void setRefreshTests() throws NoSuchFieldException, IllegalAccessException {

        Field lastUnknownTimestampField = Uri.class.getDeclaredField("lastUnknownTimestamp");
        lastUnknownTimestampField.setAccessible(true);

        Field lastUnhealthyPendingTimestampField = Uri.class.getDeclaredField("lastUnhealthyPendingTimestamp");
        lastUnhealthyPendingTimestampField.setAccessible(true);

        Field lastUnhealthyTimestampField = Uri.class.getDeclaredField("lastUnhealthyTimestamp");
        lastUnhealthyTimestampField.setAccessible(true);

        for (Uri.HealthStatus initialHealthStatus : Uri.HealthStatus.values()) {
            Uri testUri = new Uri("https://127.0.0.1:8080");
            AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);
            healthStatus.set(initialHealthStatus);
            Instant time = Instant.now().minusSeconds(2);

            lastUnknownTimestampField.set(testUri, time);
            lastUnhealthyPendingTimestampField.set(testUri, time);
            lastUnhealthyTimestampField.set(testUri, time);

            testUri.setRefreshed();

            Instant lastUnknownTimestampAfter = (Instant) lastUnknownTimestampField.get(testUri);
            Instant lastUnhealthyPendingTimestampAfter = (Instant) lastUnhealthyPendingTimestampField.get(testUri);
            Instant lastUnhealthyTimestampAfter = (Instant) lastUnhealthyTimestampField.get(testUri);

            switch (initialHealthStatus) {
                case Unknown:
                case Connected:
                case UnhealthyPending:
                    assertThat(testUri.getHealthStatus()).isEqualTo(initialHealthStatus);
                    assertThat(lastUnknownTimestampAfter).isEqualTo(time);
                    assertThat(lastUnhealthyPendingTimestampAfter).isEqualTo(time);
                    assertThat(lastUnhealthyTimestampAfter).isEqualTo(time);
                    break;
                case Unhealthy:
                    assertThat(testUri.getHealthStatus()).isEqualTo(UnhealthyPending);
                    assertThat(lastUnknownTimestampAfter).isEqualTo(time);
                    assertThat(lastUnhealthyPendingTimestampAfter).isAfter(time);
                    assertThat(lastUnhealthyTimestampAfter).isEqualTo(time);
                    break;
            }
        }
    }

    @Test(groups = "unit")
    public void getEffectiveHealthStatusTest() throws NoSuchFieldException, IllegalAccessException {
        Uri testUri = new Uri("https://127.0.0.1:8080");
        AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);

        for (Uri.HealthStatus initialStatus : Uri.HealthStatus.values()) {
            healthStatus.set(initialStatus);

            switch (initialStatus) {
                case Unhealthy:
                case Connected:
                    assertThat(testUri.getEffectiveHealthStatus()).isEqualTo(initialStatus);
                    break;
                case Unknown:
                    Field lastUnknownTimestampField = Uri.class.getDeclaredField("lastUnknownTimestamp");
                    lastUnknownTimestampField.setAccessible(true);
                    // if within the DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS time window, then return the status as it is
                    lastUnknownTimestampField.set(testUri, Instant.now());
                    assertThat(testUri.getEffectiveHealthStatus()).isEqualTo(Unknown);
                    // if already passed the DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS, then return rolling into healthy category
                    lastUnknownTimestampField.set(testUri, Instant.now().minusMillis(Duration.ofMinutes(2).toMillis()));
                    assertThat(testUri.getEffectiveHealthStatus()).isEqualTo(Connected);
                    break;
                case UnhealthyPending:
                    Field lastUnhealthyPendingTimestampField = Uri.class.getDeclaredField("lastUnhealthyPendingTimestamp");
                    lastUnhealthyPendingTimestampField.setAccessible(true);
                    // if within the DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS time window, then return the status as it is
                    lastUnhealthyPendingTimestampField.set(testUri, Instant.now());
                    assertThat(testUri.getEffectiveHealthStatus()).isEqualTo(UnhealthyPending);
                    // if already passed the DEFAULT_NON_HEALTHY_RESET_TIME_IN_MILLISECONDS, then rolling into healthy category
                    lastUnhealthyPendingTimestampField.set(testUri, Instant.now().minusMillis(Duration.ofMinutes(2).toMillis()));
                    assertThat(testUri.getEffectiveHealthStatus()).isEqualTo(Connected);
                    break;
                default:
                    throw new IllegalStateException("Unknown health status: " + initialStatus);
            }
        }
    }

    @Test(groups = "unit")
    public void shouldRefreshHealthStatusTests() throws NoSuchFieldException, IllegalAccessException {
        Uri testUri = new Uri("https://127.0.0.1:8080");
        AtomicReference<Uri.HealthStatus> healthStatus = ReflectionUtils.getHealthStatus(testUri);

        for (Uri.HealthStatus initialStatus : Uri.HealthStatus.values()) {
            healthStatus.set(initialStatus);

            switch (initialStatus) {
                case Unknown:
                case Connected:
                case UnhealthyPending:
                    assertThat(testUri.shouldRefreshHealthStatus()).isFalse();
                    break;
                case Unhealthy:
                    Field lastUnhealthyTimestampField = Uri.class.getDeclaredField("lastUnhealthyTimestamp");
                    lastUnhealthyTimestampField.setAccessible(true);
                    lastUnhealthyTimestampField.set(testUri, Instant.now());
                    assertThat(testUri.shouldRefreshHealthStatus()).isFalse();

                    lastUnhealthyTimestampField.set(testUri, Instant.now().minusMillis(Duration.ofMinutes(2).toMillis()));
                    assertThat(testUri.shouldRefreshHealthStatus()).isTrue();
                    break;
                default:
                    throw new IllegalStateException("Unknown health status: " + initialStatus);
            }
        }
    }
}
