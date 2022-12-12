// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.TelemetryAttributes;

final class NoopAttributes implements TelemetryAttributes {
    static final NoopAttributes INSTANCE = new NoopAttributes();
    private NoopAttributes() {
    }
}
