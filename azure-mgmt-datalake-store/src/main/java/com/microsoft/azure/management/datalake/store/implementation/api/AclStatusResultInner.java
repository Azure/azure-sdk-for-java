/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store file or directory Access Control List information.
 */
public class AclStatusResultInner {
    /**
     * Gets or sets the AclStatus object for a given file or directory.
     */
    @JsonProperty(value = "AclStatus")
    private AclStatus aclStatus;

    /**
     * Get the aclStatus value.
     *
     * @return the aclStatus value
     */
    public AclStatus aclStatus() {
        return this.aclStatus;
    }

    /**
     * Set the aclStatus value.
     *
     * @param aclStatus the aclStatus value to set
     * @return the AclStatusResultInner object itself.
     */
    public AclStatusResultInner withAclStatus(AclStatus aclStatus) {
        this.aclStatus = aclStatus;
        return this;
    }

}
