// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalStorageTelemetryPipelineListenerTest {

    @Test
    void splitBytesByNewline_splitsCorrectly() {
        byte[] input = "line1\nline2\nline3".getBytes();
        List<byte[]> result = LocalStorageTelemetryPipelineListener.splitBytesByNewline(input);
        assertThat(result).hasSize(3);
        assertThat(new String(result.get(0))).isEqualTo("line1");
        assertThat(new String(result.get(1))).isEqualTo("line2");
        assertThat(new String(result.get(2))).isEqualTo("line3");
    }

    @Test
    void splitBytesByNewline_handlesEmptyInput() {
        byte[] input = "".getBytes();
        List<byte[]> result = LocalStorageTelemetryPipelineListener.splitBytesByNewline(input);
        assertThat(result).isEmpty();
    }

    @Test
    void splitBytesByNewline_handlesNoNewline() {
        byte[] input = "singleline".getBytes();
        List<byte[]> result = LocalStorageTelemetryPipelineListener.splitBytesByNewline(input);
        assertThat(result).hasSize(1);
        assertThat(new String(result.get(0))).isEqualTo("singleline");
    }

    @Test
    void splitBytesByNewline_handlesTrailingNewline() {
        byte[] input = "line1\nline2\n".getBytes();
        List<byte[]> result = LocalStorageTelemetryPipelineListener.splitBytesByNewline(input);
        assertThat(result).hasSize(2);
        assertThat(new String(result.get(0))).isEqualTo("line1");
        assertThat(new String(result.get(1))).isEqualTo("line2");
    }

    @Test
    void splitBytesByNewline_handlesMultipleNewlines() {
        byte[] input = "\n\n\n".getBytes();
        List<byte[]> result = LocalStorageTelemetryPipelineListener.splitBytesByNewline(input);
        assertThat(result).hasSize(3);
        assertThat(new String(result.get(0))).isEmpty();
        assertThat(new String(result.get(1))).isEmpty();
        assertThat(new String(result.get(2))).isEmpty();
    }
}
