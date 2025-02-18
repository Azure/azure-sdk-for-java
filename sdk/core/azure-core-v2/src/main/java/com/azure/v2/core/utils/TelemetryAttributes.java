// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.utils;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.TypeConditions;

/**
 * Generic attribute collection applicable to metrics, tracing and logging implementations.
 * Implementation is capable of handling different attribute types, caching and optimizing the internal representation.
 */
@Metadata(conditions = TypeConditions.IMMUTABLE)
public interface TelemetryAttributes {
}
