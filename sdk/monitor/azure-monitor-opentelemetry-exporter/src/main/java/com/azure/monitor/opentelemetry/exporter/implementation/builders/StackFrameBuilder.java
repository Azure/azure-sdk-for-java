// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.StackFrame;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class StackFrameBuilder {

    private static final int MAX_FILE_NAME_LENGTH = 1024;
    private static final int MAX_METHOD_NAME_LENGTH = 1024;
    private static final int MAX_ASSEMBLY_NAME_LENGTH = 1024;

    private final StackFrame data = new StackFrame();

    public void setLevel(int level) {
        data.setLevel(level);
    }

    public void setMethod(String method) {
        data.setMethod(truncateTelemetry(method, MAX_METHOD_NAME_LENGTH, "StackFrame.method"));
    }

    public void setAssembly(String assembly) {
        data.setAssembly(truncateTelemetry(assembly, MAX_ASSEMBLY_NAME_LENGTH, "StackFrame.assembly"));
    }

    public void setFileName(String fileName) {
        data.setFileName(truncateTelemetry(fileName, MAX_FILE_NAME_LENGTH, "StackFrame.fileName"));
    }

    public void setLine(Integer line) {
        data.setLine(line);
    }

    StackFrame build() {
        return data;
    }
}
