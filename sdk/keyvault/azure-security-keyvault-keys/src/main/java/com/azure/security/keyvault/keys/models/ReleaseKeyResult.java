// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The release result, containing the released key. */
@Immutable
public final class ReleaseKeyResult {
    /*
     * A signed object containing the released key.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private String value;

    /**
     * Get a signed object containing the released key.
     *
     * @return A signed object containing the released key.
     */
    public String getValue() {
        return this.value;
    }
}
