/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a ComputeNodeOperations.UpdateUser request.
 */
public class NodeUpdateUserParameter {
    /**
     * Sets the password of the account.
     */
    @JsonProperty(required = true)
    private String password;

    /**
     * Sets the time at which the account should expire. If omitted, the
     * default is 1 day from the current time.
     */
    private DateTime expiryTime;

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

}
