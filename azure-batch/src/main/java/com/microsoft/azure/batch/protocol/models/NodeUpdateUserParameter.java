/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;

/**
 * Parameters for a ComputeNodeOperations.UpdateUser request.
 */
public class NodeUpdateUserParameter {
    /**
     * Sets the password of the account.
     */
    private String password;

    /**
     * Sets the time at which the account should expire. If omitted, the
     * default is 1 day from the current time.
     */
    private DateTime expiryTime;

    /**
     * Gets or sets the SSH public key that can be used for remote login to
     * the compute node.
     */
    private String sshPublicKey;

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

    /**
     * Get the sshPublicKey value.
     *
     * @return the sshPublicKey value
     */
    public String getSshPublicKey() {
        return this.sshPublicKey;
    }

    /**
     * Set the sshPublicKey value.
     *
     * @param sshPublicKey the sshPublicKey value to set
     */
    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

}
