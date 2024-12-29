// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;

public final class OTelUtils {
    private static final ClientLogger LOGGER = new ClientLogger(OTelUtils.class);

    public static Object getOTelContext(Context context) throws Throwable {
        Object parent = context.get(TelemetryProvider.TRACE_CONTEXT_KEY);
        if (CONTEXT_CLASS.isInstance(parent)) {
            return parent;
        } else if (parent instanceof OTelSpan) {
            return ((OTelSpan) parent).getOtelContext();
        } else if (parent != null) {
            LOGGER.atVerbose()
                .addKeyValue("expectedType", CONTEXT_CLASS.getName())
                .addKeyValue("actualType", parent.getClass().getName())
                .log("Context does not contain an OpenTelemetry context. Ignoring it.");
        }

        return OTelContext.getCurrent();
    }

    private OTelUtils() {
    }
}
