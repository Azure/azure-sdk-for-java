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
     * The password of the account.
     */
    private String password;

    /**
     * The time at which the account should expire. If omitted, the default is
     * 1 day from the current time.
     */
    private DateTime expiryTime;

    /**
     * The SSH public key that can be used for remote login to the compute
     * node.
     */
    private String sshPublicKey;

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the NodeUpdateUserParameter object itself.
     */
    public NodeUpdateUserParameter withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the expiryTime value.
     *
     * @return the expiryTime value
     */
    public DateTime expiryTime() {
        return this.expiryTime;
    }

    /**
     * Set the expiryTime value.
     *
     * @param expiryTime the expiryTime value to set
     * @return the NodeUpdateUserParameter object itself.
     */
    public NodeUpdateUserParameter withExpiryTime(DateTime expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * Get the sshPublicKey value.
     *
     * @return the sshPublicKey value
     */
    public String sshPublicKey() {
        return this.sshPublicKey;
    }

    /**
     * Set the sshPublicKey value.
     *
     * @param sshPublicKey the sshPublicKey value to set
     * @return the NodeUpdateUserParameter object itself.
     */
    public NodeUpdateUserParameter withSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
        return this;
    }

}
