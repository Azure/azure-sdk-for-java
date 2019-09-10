// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    /**
     * The organization Id.
     */
    private String organizationId;

    /**
     * The administrators.
     */
    private List<Administrator> administrators;

    /**
     * Creates an instance of the issuer.
     *
     * @param name The name of the issuer.
     * @param provider The provider of the issuer.
     */
    public Issuer(String name, String provider) {
        super(name, provider);
    }

    Issuer() { }

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
        this.administrators =  orgDetails.containsKey("admin_details") ? parseAdministrators((List<Object>) orgDetails.get("admin_details")) : null;
        this.organizationId = (String) orgDetails.get("id");
    }

    @SuppressWarnings("unchecked")
    private List<Administrator> parseAdministrators(List<Object> admins) {
        List<Administrator> output = new ArrayList<>();

        for (Object admin : admins) {
            LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) admin;
            String firstName = map.containsKey("first_name") ? map.get("first_name") : "";
            String lastName = map.containsKey("last_name") ? map.get("last_name") : "";
            String email = map.containsKey("email") ? map.get("email") : "";
            String phone = map.containsKey("phone") ? map.get("phone") : "";
            output.add(new Administrator(firstName, lastName, email, phone));
        }
        return  output;
    }
}
