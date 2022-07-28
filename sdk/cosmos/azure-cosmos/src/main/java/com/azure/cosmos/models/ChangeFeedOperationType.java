// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Change feed operation type
 */
@Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum ChangeFeedOperationType {
    @JsonProperty("create")
    CREATE,
    @JsonProperty("replace")
    REPLACE,
    @JsonProperty("delete")
    DELETE;
}
