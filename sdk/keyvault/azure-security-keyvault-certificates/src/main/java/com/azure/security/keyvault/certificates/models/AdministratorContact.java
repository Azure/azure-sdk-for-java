// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an administrator in {@link CertificateIssuer}
 */
public final class AdministratorContact {

    /**
     * First name.
     */
    @JsonProperty(value = "first_name")
    private String firstName;

    /**
     * Last name.
     */
    @JsonProperty(value = "last_name")
    private String lastName;

    /**
     * Email address.
     */
    @JsonProperty(value = "email")
    private String email;

    /**
     * Phone number.
     */
    @JsonProperty(value = "phone")
    private String phone;

    /**
     * Get the first name of the admin.
     * @return the first name of admin.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Get the last name of the admin.
     * @return the last name of admin.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Get the email of the admin.
     * @return the email of admin.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the contact of the admin.
     * @return the contact of admin.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the first name of the admin.
     * @param firstName the first name of the admin to set.
     * @return the updated AdministratorContact object itself.
     */
    public AdministratorContact setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Set the last name of the admin.
     * @param lastName the last name of the admin to set.
     * @return the updated AdministratorContact object itself.
     */
    public AdministratorContact setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Set the email of the admin.
     * @param email the email of the admin to set.
     * @return the updated AdministratorContact object itself.
     */
    public AdministratorContact setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Set the contact of the admin.
     * @param phone the enabled status to set
     * @return the updated AdministratorContact object itself.
     */
    public AdministratorContact setPhone(String phone) {
        this.phone = phone;
        return this;
    }
}
