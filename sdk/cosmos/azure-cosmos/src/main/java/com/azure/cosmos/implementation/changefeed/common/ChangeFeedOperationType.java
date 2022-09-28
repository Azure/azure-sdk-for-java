// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Change feed operation type
 */
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
