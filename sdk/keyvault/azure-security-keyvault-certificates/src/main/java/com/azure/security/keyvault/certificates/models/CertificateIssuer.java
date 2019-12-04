// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
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
public final class CertificateIssuer {

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
    private List<AdministratorContact> administratorContacts;

    /**
     * The issuer id.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * The issuer provider.
     */
    @JsonProperty(value = "provider")
    private String provider;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    @JsonProperty(value = "name")
    String name;

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
    public CertificateIssuer(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    CertificateIssuer() { }

    /**
     * Get the id of the issuer.
     * @return the identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the issuer provider
     * @return the issuer provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Get the issuer name
     * @return the issuer name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the account id of the issuer.
     * @return the account id
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Set the account id of the issuer.
     * @param accountId the account id to set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    /**
     * Get the password of the issuer.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password id of the issuer.
     * @param password the password set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the organization id of the issuer.
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
    public CertificateIssuer setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Get the administrators of the issuer.
     * @return the administrators
     */
    public List<AdministratorContact> getAdministratorContacts() {
        return administratorContacts;
    }

    /**
     * Set the administrators of the issuer.
     * @param administratorContacts the administrators to set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setAdministratorContacts(List<AdministratorContact> administratorContacts) {
        this.administratorContacts = administratorContacts;
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
    public CertificateIssuer setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the created UTC time.
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
    private void unpackOrganizationalDetails(Map<String, Object> orgDetails) {
        this.administratorContacts =  orgDetails.containsKey("admin_details") ? parseAdministrators((List<Object>) orgDetails.get("admin_details")) : null;
        this.organizationId = (String) orgDetails.get("id");
    }

    @SuppressWarnings("unchecked")
    private List<AdministratorContact> parseAdministrators(List<Object> admins) {
        List<AdministratorContact> output = new ArrayList<>();

        for (Object admin : admins) {
            LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) admin;
            String firstName = map.containsKey("first_name") ? map.get("first_name") : "";
            String lastName = map.containsKey("last_name") ? map.get("last_name") : "";
            String email = map.containsKey("email") ? map.get("email") : "";
            String phone = map.containsKey("phone") ? map.get("phone") : "";
            output.add(new AdministratorContact(firstName, lastName, email, phone));
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
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
