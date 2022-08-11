// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Change feed operation type
 */
@Beta(value = Beta.SinceVersion.V4_35_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum ChangeFeedOperationType {
    /**
     * Represents Create operation
     */
    @JsonProperty("create")
    CREATE,
    /**
     * Represents Replace operation
     */
    @JsonProperty("replace")
    REPLACE,
    /**
     * Represents Delete operation
     */
    @JsonProperty("delete")
    DELETE;
}
