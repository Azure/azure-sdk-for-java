// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The subject alternate names of Certificate Policy.
 */
public final class SubjectAlternativeNames {
    /**
     * Email addresses.
     */
    @JsonProperty(value = "emails")
    private List<String> emails;

    /**
     * Domain names.
     */
    @JsonProperty(value = "dns_names")
    private List<String> dnsNames;

    /**
     * User principal names.
     */
    @JsonProperty(value = "upns")
    private List<String> userPrincipalNames;

    /**
     * Create an instance of SubjectAlternativeNames
     */
    public SubjectAlternativeNames() { }

    /**
     * Get the emails.
     *
     * @return the list of emails
     */
    public List<String> getEmails() {
        return this.emails;
    }

    /**
     * Set the emails.
     *
     * @param emails the emails to set
     * @return the updated SubjectAlternativeNames object itself.
     */
    public SubjectAlternativeNames setEmails(List<String> emails) {
        this.emails = emails;
        return this;
    }

    /**
     * Get the dnsNames.
     *
     * @return the list of dnsNames
     */
    public List<String> getDnsNames() {
        return this.dnsNames;
    }

    /**
     * Set the dns names.
     *
     * @param dnsNames the dns names to set
     * @return the updated SubjectAlternativeNames object itself.
     */
    public SubjectAlternativeNames setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
        return this;
    }

    /**
     * Get the User Principal Names.
     *
     * @return the list of  User Principal Names
     */
    public List<String> getUserPrincipalNames() {
        return this.userPrincipalNames;
    }

    /**
     * Set the User Principal Names.
     *
     * @param userPrincipalNames the user principal names to set
     * @return the updated SubjectAlternativeNames object itself.
     */
    public SubjectAlternativeNames setUserPrincipalNames(List<String> userPrincipalNames) {
        this.userPrincipalNames = userPrincipalNames;
        return this;
    }
}
