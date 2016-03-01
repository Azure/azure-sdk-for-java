/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import java.util.List;

/**
 * SSH configuration for Linux based VMs running on Azure.
 */
public class SshConfiguration {
    /**
     * Gets or sets the list of SSH public keys used to authenticate with
     * linux based VMs.
     */
    private List<SshPublicKey> publicKeys;

    /**
     * Get the publicKeys value.
     *
     * @return the publicKeys value
     */
    public List<SshPublicKey> getPublicKeys() {
        return this.publicKeys;
    }

    /**
     * Set the publicKeys value.
     *
     * @param publicKeys the publicKeys value to set
     */
    public void setPublicKeys(List<SshPublicKey> publicKeys) {
        this.publicKeys = publicKeys;
    }

}
