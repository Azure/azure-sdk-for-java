// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The contacts for the vault certificates.
 */
class Contacts {
    /**
     * Identifier for the contacts collection.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * The contact list for the vault certificates.
     */
    @JsonProperty(value = "contacts")
    private List<CertificateContact> contactList;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    String id() {
        return this.id;
    }

    /**
     * Get the contactList value.
     *
     * @return the contactList value
     */
    List<CertificateContact> contactList() {
        return this.contactList;
    }

    /**
     * Set the contactList value.
     *
     * @param contactList the contactList value to set
     * @return the Contacts object itself.
     */
    Contacts contactList(List<CertificateContact> contactList) {
        this.contactList = contactList;
        return this;
    }

}
