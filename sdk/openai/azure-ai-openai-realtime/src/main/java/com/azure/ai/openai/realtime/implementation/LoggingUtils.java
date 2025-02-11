// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation;

import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

public final class LoggingUtils {

    private LoggingUtils() {
    }

    public static Map<String, Object> createContextWithApplicationId(String applicationId) {
        return createContextWithConnectionId(applicationId, null);
    }

    public static Map<String, Object> createContextWithConnectionId(String applicationId, String connectionId) {
        Map<String, Object> globalLoggingContext = new HashMap<>(2);
        if (applicationId != null) {
            globalLoggingContext.put("applicationId", applicationId);
        }
        if (connectionId != null) {
            globalLoggingContext.put("connectionId", connectionId);
        }
        return globalLoggingContext;
    }

    public static LoggingEventBuilder addSignalTypeAndResult(LoggingEventBuilder logBuilder, SignalType signalType,
        Sinks.EmitResult result) {
        return logBuilder.addKeyValue("signalType", signalType).addKeyValue("emitResult", result);
    }
}
