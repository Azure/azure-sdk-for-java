/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.models;

import java.util.List;

/**
 * Data Lake Store file or directory Access Control List information.
 */
public class AclStatus {
    /**
     * Gets or sets the list of ACLSpec entries on a file or directory.
     */
    private List<String> entries;

    /**
     * Gets or sets the group owner, an AAD Object ID.
     */
    private String group;

    /**
     * Gets or sets the user owner, an AAD Object ID.
     */
    private String owner;

    /**
     * Gets or sets the indicator of whether the sticky bit is on or off.
     */
    private Boolean stickyBit;

    /**
     * Get the entries value.
     *
     * @return the entries value
     */
    public List<String> getEntries() {
        return this.entries;
    }

    /**
     * Set the entries value.
     *
     * @param entries the entries value to set
     */
    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    /**
     * Get the group value.
     *
     * @return the group value
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Set the group value.
     *
     * @param group the group value to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get the owner value.
     *
     * @return the owner value
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Set the owner value.
     *
     * @param owner the owner value to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Get the stickyBit value.
     *
     * @return the stickyBit value
     */
    public Boolean getStickyBit() {
        return this.stickyBit;
    }

    /**
     * Set the stickyBit value.
     *
     * @param stickyBit the stickyBit value to set
     */
    public void setStickyBit(Boolean stickyBit) {
        this.stickyBit = stickyBit;
    }

}
