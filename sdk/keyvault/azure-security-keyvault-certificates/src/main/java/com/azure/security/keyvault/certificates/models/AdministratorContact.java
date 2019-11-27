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
     * Email addresss.
     */
    @JsonProperty(value = "email")
    private String email;

    /**
     * Phone number.
     */
    @JsonProperty(value = "phone")
    private String phone;

    /**
     * Creates an administrator of the issuer.
     * @param firstName the firstName of the issuer.
     * @param lastName the last name of the issuer.
     * @param email the email of the issuer.
     */
    public AdministratorContact(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Creates an administrator of the issuer.
     * @param firstName the firstName of the admin.
     * @param lastName the last name of the admin.
     * @param email the email of the admin.
     * @param phone the contact info of the admin.
     */
    public AdministratorContact(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

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
}
