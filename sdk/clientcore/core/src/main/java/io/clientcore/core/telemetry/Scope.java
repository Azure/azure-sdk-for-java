// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

/**
 * An {@code AutoCloseable} scope that controls implicit tracing context lifetime.
 * <p>
 *
 * The scope MUST be closed. It also MUST be closed on the same thread it was created.
 * <p>
 *
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
@FunctionalInterface
public interface Scope extends AutoCloseable {
    @Override
    void close();
}
