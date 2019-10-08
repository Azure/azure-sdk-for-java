// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents certificate Issuer with all of its properties.
 */
public final class Issuer {

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
     * The Issuer properties
     */
    private final IssuerProperties properties;

    /**
     * Determines whether the issuer is enabled.
     */
    @JsonProperty(value = "enabled")
    private Boolean enabled;

    /**
     * The created UTC time.
     */
    private OffsetDateTime created;

    /**
     * The updated UTC time.
     */
    private OffsetDateTime updated;

    /**
     * Creates an instance of the issuer.
     *
     * @param name The name of the issuer.
     * @param provider The provider of the issuer.
     */
    public Issuer(String name, String provider) {
        properties = new IssuerProperties(name, provider);
    }

    Issuer() {
        properties = new IssuerProperties();
    }

    /**
     * Get the certificate properties.
     * @return the certificate properties.
     */
    public IssuerProperties getProperties() {
        return properties;
    }

    /**
     * Get the certificate identifier
     * @return the certificate identifier
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the certificate name
     * @return the certificate name
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the account id of the isssuer.
     * @return the account id
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Set the account id of the isssuer.
     * @param accountId the account id to set.
     * @return the Issuer object itself.
     */
    public Issuer setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    /**
     * Get the password of the isssuer.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password id of the isssuer.
     * @param password the password set.
     * @return the Issuer object itself.
     */
    public Issuer setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the organization id of the isssuer.
     * @return the organization id
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set the organization id of the issuer.
     * @param organizationId the org id to set.
     * @return the Issuer object itself.
     */
    public Issuer setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Get the administrators of the isssuer.
     * @return the administrators
     */
    public List<Administrator> getAdministrators() {
        return administrators;
    }

    /**
     * Set the administrators of the isssuer.
     * @param administrators the administrators to set.
     * @return the Issuer object itself.
     */
    public Issuer setAdministrators(List<Administrator> administrators) {
        this.administrators = administrators;
        return this;
    }

    /**
     * Get the enabled status
     * @return the enabled status
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the enabled status
     * @param enabled the enabled status to set
     * @return the Issuer object itself.
     */
    public Issuer setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get tje created UTC time.
     * @return the created UTC time.
     */
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * Get the updated UTC time.
     * @return the updated UTC time.
     */
    public OffsetDateTime getUpdated() {
        return updated;
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

    @JsonProperty("attributes")
    private void unpackBaseAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    @JsonProperty(value = "id")
    void unpackId(String id) {
        properties.unpackId(id);
    }
}
