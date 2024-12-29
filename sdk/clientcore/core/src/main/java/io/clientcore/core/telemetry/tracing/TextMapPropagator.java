// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

import io.clientcore.core.util.Context;

public interface TextMapPropagator {
    /**
     * Injects the context into the carrier.
     *
     * @param context The context to inject.
     * @param carrier The carrier to inject the context into.
     */
    <C> void inject(Context context, C carrier, TextMapSetter<C> setter);

    /**
     * Extracts the context from the carrier.
     *
     * @param carrier The carrier to extract the context from.
     * @param getter The getter to use to extract the context from the carrier.
     * @return The extracted context.
     */
    <C> Context extract(Context context, C carrier, TextMapGetter<C> getter);
}
