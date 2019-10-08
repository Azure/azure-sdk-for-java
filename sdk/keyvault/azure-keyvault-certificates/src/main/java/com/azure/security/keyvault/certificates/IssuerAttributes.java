// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * The attributes of an issuer managed by the Key Vault service.
 */
class IssuerAttributes {
    /**
     * Determines whether the issuer is enabled.
     */
    @JsonProperty(value = "enabled")
    private Boolean enabled;

    /**
     * Creation time in UTC.
     */
    @JsonProperty("created")
    private OffsetDateTime created;

    /**
     * Last updated time in UTC.
     */
    @JsonProperty("updated")
    private OffsetDateTime updated;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the IssuerAttributes object itself.
     */
    IssuerAttributes enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the created UTC time.
     *
     * @return the created value
     */
    OffsetDateTime created() {
        return created;
    }

    /**
     * Get the updated UTC time.
     *
     * @return the updated UTC time.
     */
    OffsetDateTime updated() {
        return updated;
    }
}
