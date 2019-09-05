// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents certificate Issuer with all of its properties.
 */
public final class Issuer extends IssuerBase {

    /**
     * The user name/account name/account id.
     */
    private String accountId;

    /**
     * The password/secret/account key.
     */
    private String password;

    private String organizationId;
    private List<Administrator> administrators;

    public Issuer(String name, String provider) {
        super(name, provider);
    }

    /**
     * Get the account id of the isssuer.
     * @return the account id
     */
    public String accountId() {
        return accountId;
    }

    /**
     * Set the account id of the isssuer.
     * @param accountId the account id to set.
     * @return the Issuer object itself.
     */
    public Issuer accountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    /**
     * Get the password of the isssuer.
     * @return the password
     */
    public String password() {
        return password;
    }

    /**
     * Set the password id of the isssuer.
     * @param password the password set.
     * @return the Issuer object itself.
     */
    public Issuer password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the organization id of the isssuer.
     * @return the organization id
     */
    public String organizationId() {
        return organizationId;
    }

    /**
     * Set the organization id of the issuer.
     * @param organizationId the org id to set.
     * @return the Issuer object itself.
     */
    public Issuer organizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Get the administrators of the isssuer.
     * @return the administrators
     */
    public List<Administrator> administrators() {
        return administrators;
    }

    /**
     * Set the administrators of the isssuer.
     * @param administrators the administrators to set.
     * @return the Issuer object itself.
     */
    public Issuer administrators(List<Administrator> administrators) {
        this.administrators = administrators;
        return this;
    }

    @JsonProperty(value = "credentials")
    private void unpackCredentials(Map<String, Object> credentials) {
        this.accountId = (String) credentials.get("account_id");
        this.password = (String) credentials.get("pwd");
    }

    @JsonProperty(value = "org_details")
    @SuppressWarnings("unchecked")
    private void unpacOrganizationalDetails(Map<String, Object> orgDetails) {
        this.administrators =  (List<Administrator>) orgDetails.get("admin_details");
        this.organizationId = (String) orgDetails.get("id");
    }
}
