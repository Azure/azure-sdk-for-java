// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link EntityCertainty} model.
 */
@Immutable
public enum EntityCertainty {
    /** Enum value Positive. */
    POSITIVE,

    /** Enum value Positive Possible. */
    POSITIVE_POSSIBLE,

    /** Enum value Neutral Possible. */
    NEUTRAL_POSSIBLE,

    /** Enum value Negative Possible. */
    NEGATIVE_POSSIBLE,

    /** Enum value Negative. */
    NEGATIVE
}
