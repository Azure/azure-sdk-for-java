// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The subject alternate names of a X509 object.
 */
class SubjectAlternativeNamesRequest {
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
     * Get the emails value.
     *
     * @return the emails value
     */
    public List<String> emails() {
        return this.emails;
    }

    SubjectAlternativeNamesRequest(SubjectAlternativeNames subjectAlternativeNames) {
        if (subjectAlternativeNames != null) {
            this.dnsNames = subjectAlternativeNames.getDnsNames();
            this.emails = subjectAlternativeNames.getEmails();
            this.upns = subjectAlternativeNames.getUserPrincipalNames();
        }
    }

    /**
     * Set the emails value.
     *
     * @param emails the emails value to set
     * @return the SubjectAlternativeNamesRequest object itself.
     */
    SubjectAlternativeNamesRequest emails(List<String> emails) {
        this.emails = emails;
        return this;
    }

    /**
     * Get the dnsNames value.
     *
     * @return the dnsNames value
     */
    List<String> dnsNames() {
        return this.dnsNames;
    }

    /**
     * Set the dnsNames value.
     *
     * @param dnsNames the dnsNames value to set
     * @return the SubjectAlternativeNamesRequest object itself.
     */
    SubjectAlternativeNamesRequest dnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
        return this;
    }

    /**
     * Get the upns value.
     *
     * @return the upns value
     */
    List<String> upns() {
        return this.upns;
    }

    /**
     * Set the upns value.
     *
     * @param upns the upns value to set
     * @return the SubjectAlternativeNamesRequest object itself.
     */
    SubjectAlternativeNamesRequest upns(List<String> upns) {
        this.upns = upns;
        return this;
    }
}
