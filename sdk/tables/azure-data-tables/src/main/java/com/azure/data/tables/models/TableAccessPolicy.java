// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * A table's access policy.
 */
@Fluent
public final class TableAccessPolicy {
    /*
     * The date-time the policy is active.
     */
    private OffsetDateTime startsOn;

    /*
     * The date-time the policy expires.
     */
    private OffsetDateTime expiresOn;

    /*
     * The permissions for the acl policy.
     */
    private String permissions;

    /**
     * Get the {@link OffsetDateTime date-time} the policy is active.
     *
     * @return The {@link OffsetDateTime date-time} the policy is active.
     */
    public OffsetDateTime getStartsOn() {
        return this.startsOn;
    }

    /**
     * Set the {@link OffsetDateTime date-time} the policy is active.
     *
     * @param startsOn The {@link OffsetDateTime startsOn} value to set.
     *
     * @return The updated {@link TableAccessPolicy} object.
     */
    public TableAccessPolicy setStartsOn(OffsetDateTime startsOn) {
        this.startsOn = startsOn;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime date-time} the policy expires.
     *
     * @return The {@link OffsetDateTime date-time} the policy expires.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime date-time} the policy expires.
     *
     * @param expiresOn The {@link OffsetDateTime expiresOn} value to set.
     *
     * @return The updated {@link TableAccessPolicy} object.
     */
    public TableAccessPolicy setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;

        return this;
    }

    /**
     * Get the permissions for the acl policy.
     *
     * @return The {@link TableAccessPolicy}'s permissions.
     */
    public String getPermissions() {
        return this.permissions;
    }

    /**
     * Set the permissions for the acl policy.
     *
     * @param permissions The permissions to set.
     *
     * @return The updated {@link TableAccessPolicy} object.
     */
    public TableAccessPolicy setPermissions(String permissions) {
        this.permissions = permissions;

        return this;
    }
}
