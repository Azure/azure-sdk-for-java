// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.StackFrame;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionDetails;

import java.util.ArrayList;
import java.util.List;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class ExceptionDetailBuilder {

    private static final int MAX_NAME_LENGTH = 1024;
    private static final int MAX_MESSAGE_LENGTH = 32768;

    private final TelemetryExceptionDetails data = new TelemetryExceptionDetails();

    public void setId(Integer id) {
        data.setId(id);
    }

    public void setOuter(ExceptionDetailBuilder outer) {
        data.setOuterId(outer.data.getId());
    }

    public void setTypeName(String typeName) {
        data.setTypeName(truncateTelemetry(typeName, MAX_NAME_LENGTH, "ExceptionDetail.typeName"));
    }

    public void setMessage(String message) {
        data.setMessage(truncateTelemetry(message, MAX_MESSAGE_LENGTH, "ExceptionDetail.message"));
    }

    public void setHasFullStack(Boolean hasFullStack) {
        data.setHasFullStack(hasFullStack);
    }

    public void setStack(String stack) {
        data.setStack(truncateTelemetry(stack, MAX_MESSAGE_LENGTH, "ExceptionDetail.stack"));
    }

    public void setParsedStack(List<StackFrameBuilder> builders) {
        List<StackFrame> stackFrames = new ArrayList<>();
        for (StackFrameBuilder builder : builders) {
            stackFrames.add(builder.build());
        }
        data.setParsedStack(stackFrames);
    }

    // visible (beyond package protected) for testing
    public TelemetryExceptionDetails build() {
        return data;
    }
}
