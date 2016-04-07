/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Classic Administrator properties.
 */
public class ClassicAdministratorProperties {
    /**
     * Gets or sets the email address.
     */
    private String emailAddress;

    /**
     * Gets or sets the role.
     */
    private String role;

    /**
     * Get the emailAddress value.
     *
     * @return the emailAddress value
     */
    public String emailAddress() {
        return this.emailAddress;
    }

    /**
     * Set the emailAddress value.
     *
     * @param emailAddress the emailAddress value to set
     * @return the ClassicAdministratorProperties object itself.
     */
    public ClassicAdministratorProperties setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    /**
     * Get the role value.
     *
     * @return the role value
     */
    public String role() {
        return this.role;
    }

    /**
     * Set the role value.
     *
     * @param role the role value to set
     * @return the ClassicAdministratorProperties object itself.
     */
    public ClassicAdministratorProperties setRole(String role) {
        this.role = role;
        return this;
    }

}
