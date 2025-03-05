// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

/**
 * An {@code AutoCloseable} scope that controls implicit tracing context lifetime.
 * <p>
 *
 * The scope MUST be closed. It also MUST be closed on the same thread it was created.
 *
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
@FunctionalInterface
public interface TracingScope extends AutoCloseable {
    @Override
    void close();
}
