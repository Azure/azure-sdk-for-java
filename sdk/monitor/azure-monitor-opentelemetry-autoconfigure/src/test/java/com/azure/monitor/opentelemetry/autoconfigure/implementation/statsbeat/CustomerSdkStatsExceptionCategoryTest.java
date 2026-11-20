// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerSdkStatsExceptionCategoryTest {

    @Test
    public void testSocketTimeoutException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new SocketTimeoutException("Read timed out")))
            .isEqualTo("Timeout exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(new SocketTimeoutException("Read timed out")))
            .isTrue();
    }

    @Test
    public void testTimeoutException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new TimeoutException("timeout")))
            .isEqualTo("Timeout exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(new TimeoutException("timeout"))).isTrue();
    }

    @Test
    public void testUnknownHostException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new UnknownHostException("host.example.com")))
            .isEqualTo("Network exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(new UnknownHostException("host.example.com")))
            .isFalse();
    }

    @Test
    public void testConnectException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new ConnectException("Connection refused")))
            .isEqualTo("Network exception");
    }

    @Test
    public void testIOException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new IOException("Disk full")))
            .isEqualTo("Storage exception");
    }

    @Test
    public void testGenericRuntimeException() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(new RuntimeException("Something went wrong")))
            .isEqualTo("Client exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(new RuntimeException("Something went wrong")))
            .isFalse();
    }

    @Test
    public void testNullThrowable() {
        assertThat(CustomerSdkStatsExceptionCategory.categorize(null)).isEqualTo("Client exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(null)).isFalse();
    }

    @Test
    public void testWrappedSocketTimeoutException() {
        // A SocketTimeoutException wrapped in a RuntimeException should still be detected via cause
        RuntimeException wrapper = new RuntimeException("wrapper", new SocketTimeoutException("Read timed out"));
        assertThat(CustomerSdkStatsExceptionCategory.categorize(wrapper)).isEqualTo("Timeout exception");
        assertThat(CustomerSdkStatsExceptionCategory.containsTimeout(wrapper)).isTrue();
    }

    @Test
    public void testWrappedUnknownHostException() {
        RuntimeException wrapper = new RuntimeException("wrapper", new UnknownHostException("host.example.com"));
        assertThat(CustomerSdkStatsExceptionCategory.categorize(wrapper)).isEqualTo("Network exception");
    }
}
