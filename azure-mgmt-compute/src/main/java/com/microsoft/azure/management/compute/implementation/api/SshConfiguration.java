/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

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
    public List<SshPublicKey> publicKeys() {
        return this.publicKeys;
    }

    /**
     * Set the publicKeys value.
     *
     * @param publicKeys the publicKeys value to set
     * @return the SshConfiguration object itself.
     */
    public SshConfiguration withPublicKeys(List<SshPublicKey> publicKeys) {
        this.publicKeys = publicKeys;
        return this;
    }

}
