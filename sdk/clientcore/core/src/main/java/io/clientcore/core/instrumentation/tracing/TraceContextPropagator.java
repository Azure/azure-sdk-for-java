// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

import io.clientcore.core.instrumentation.InstrumentationContext;

/**
 * A {@link TraceContextPropagator} injects and extracts tracing context from a carrier,
 * such as {@link io.clientcore.core.http.models.HttpRequest}.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface TraceContextPropagator {
    /**
     * Injects the context into the carrier.
     *
     * @param context The context to inject.
     * @param carrier The carrier to inject the context into.
     * @param setter The setter to use to inject the context into the carrier.
     * @param <C> The type of the carrier.
     */
    <C> void inject(InstrumentationContext context, C carrier, TraceContextSetter<C> setter);

    /**
     * Extracts the context from the carrier.
     *
     * @param context Initial context.
     * @param carrier The carrier to extract the context from.
     * @param getter The getter to use to extract the context from the carrier.
     * @param <C> The type of the carrier.
     *
     * @return The extracted context.
     */
    <C> InstrumentationContext extract(InstrumentationContext context, C carrier, TraceContextGetter<C> getter);
}
