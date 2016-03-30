/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An user account on a compute node.
 */
public class ComputeNodeUser {
    /**
     * Gets or sets the user name of the account.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets whether the account should be an administrator on the
     * compute node.
     */
    private Boolean isAdmin;

    /**
     * Gets or sets the time at which the account should expire. If omitted,
     * the default is 1 day from the current time.
     */
    private DateTime expiryTime;

    /**
     * Gets or sets the password of the account.
     */
    @JsonProperty(required = true)
    private String password;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the isAdmin value.
     *
     * @return the isAdmin value
     */
    public Boolean getIsAdmin() {
        return this.isAdmin;
    }

    /**
     * Set the isAdmin value.
     *
     * @param isAdmin the isAdmin value to set
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Get the expiryTime value.
     *
     * @return the expiryTime value
     */
    public DateTime getExpiryTime() {
        return this.expiryTime;
    }

    /**
     * Set the expiryTime value.
     *
     * @param expiryTime the expiryTime value to set
     */
    public void setExpiryTime(DateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
