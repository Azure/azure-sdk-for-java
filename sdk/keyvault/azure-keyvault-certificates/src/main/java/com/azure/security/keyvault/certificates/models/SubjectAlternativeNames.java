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
    private List<String> upns;

    /**
     * Get the emails.
     *
     * @return the list of emails
     */
    public List<String> getEmails() {
        return this.emails;
    }

    /*
     * Constructor to setup the SubjectAlternativeNames
     * @param sans the subject alternative names content
     * @param sansType the type of the content.
     */
    SubjectAlternativeNames(List<String> sans, SubjectAlternativeNamesType sansType) {
        switch (sansType) {
            case EMAILS:
                this.emails = sans;
                break;
            case DNS_NAMES:
                this.dnsNames = sans;
                break;
            case UPNS:
                this.upns = sans;
                break;
            default:
                //should never reach here
                return;
        }
    }

    /**
     * Create Subject Alternative names with emails.
     *
     * @param emails the emails to set
     * @return the SubjectAlternativeNames.
     */
    public static SubjectAlternativeNames fromEmails(List<String> emails) {
        return new SubjectAlternativeNames(emails, SubjectAlternativeNamesType.EMAILS);
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
     * Create Subject Alternative names with dns names.
     *
     * @param dnsNames the dns names to set
     * @return the SubjectAlternativeNames.
     */
    public static SubjectAlternativeNames fromDnsNames(List<String> dnsNames) {
        return new SubjectAlternativeNames(dnsNames, SubjectAlternativeNamesType.DNS_NAMES);
    }

    /**
     * Get the User Principal Names.
     *
     * @return the list of  User Principal Names
     */
    public List<String> getUserPrincipalNames() {
        return this.upns;
    }

    /**
     * Create Subject Alternative names with User Principal names.
     *
     * @param upns the user principal names value to set
     * @return the SubjectAlternativeNames.
     */
    public static SubjectAlternativeNames fromUserPrincipalNames(List<String> upns) {
        return new SubjectAlternativeNames(upns, SubjectAlternativeNamesType.UPNS);
    }

    private enum SubjectAlternativeNamesType {
        EMAILS,
        DNS_NAMES,
        UPNS;
    }
}
