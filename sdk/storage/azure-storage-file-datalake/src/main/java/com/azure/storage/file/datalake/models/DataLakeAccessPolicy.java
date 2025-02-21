// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

/**
 * An Access policy.
 */
public final class DataLakeAccessPolicy {
    /*
     * the date-time the policy is active
     */
    private OffsetDateTime startsOn;

    /*
     * the date-time the policy expires
     */
    private OffsetDateTime expiresOn;

    /*
     * the permissions for the acl policy
     */
    private String permissions;

    /**
     * Get the startsOn property: the date-time the policy is active.
     *
     * @return the startsOn value.
     */
    public OffsetDateTime getStartsOn() {
        return this.startsOn;
    }

    /**
     * Set the startsOn property: the date-time the policy is active.
     *
     * @param startsOn the startsOn value to set.
     * @return the DataLakeAccessPolicy object itself.
     */
    public DataLakeAccessPolicy setStartsOn(OffsetDateTime startsOn) {
        this.startsOn = startsOn;
        return this;
    }

    /**
     * Get the expiresOn property: the date-time the policy expires.
     *
     * @return the expiresOn value.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the expiresOn property: the date-time the policy expires.
     *
     * @param expiresOn the expiresOn value to set.
     * @return the DataLakeAccessPolicy object itself.
     */
    public DataLakeAccessPolicy setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Get the permissions property: the permissions for the acl policy.
     *
     * @return the permissions value.
     */
    public String getPermissions() {
        return this.permissions;
    }

    /**
     * Set the permissions property: the permissions for the acl policy.
     *
     * @param permissions the permissions value to set.
     * @return the DataLakeAccessPolicy object itself.
     */
    public DataLakeAccessPolicy setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }
}
