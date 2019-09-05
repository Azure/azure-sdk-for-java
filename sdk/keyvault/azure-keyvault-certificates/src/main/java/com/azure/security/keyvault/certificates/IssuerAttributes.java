// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    private OffsetDateTime created;

    /**
     * Last updated time in UTC.
     */
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
     * Unpacks the deletedDate json response. Converts the {@link Long created} epoch second value to OffsetDateTime and updates the
     * value of class variable created.
     */
    @JsonProperty("created")
    private void unpackCreatedDate(Long created) {
        this.created = OffsetDateTime.ofInstant(Instant.ofEpochMilli(created * 1000L), ZoneOffset.UTC);
    }

    /**
     * Unpacks the updated json response. Converts the {@link Long updated} epoch second value to OffsetDateTime and updates the
     * value of class variable updated.
     */
    @JsonProperty("updated")
    private void unpackUpdatedDate(Long updated) {
        this.updated = OffsetDateTime.ofInstant(Instant.ofEpochMilli(updated * 1000L), ZoneOffset.UTC);
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
