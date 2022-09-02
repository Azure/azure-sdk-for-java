/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
