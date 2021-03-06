// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for CertificateConfigurationStoreName. */
public final class CertificateConfigurationStoreName extends ExpandableStringEnum<CertificateConfigurationStoreName> {
    /** Static value CertificateAuthority for CertificateConfigurationStoreName. */
    public static final CertificateConfigurationStoreName CERTIFICATE_AUTHORITY = fromString("CertificateAuthority");

    /** Static value Root for CertificateConfigurationStoreName. */
    public static final CertificateConfigurationStoreName ROOT = fromString("Root");

    /**
     * Creates or finds a CertificateConfigurationStoreName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CertificateConfigurationStoreName.
     */
    @JsonCreator
    public static CertificateConfigurationStoreName fromString(String name) {
        return fromString(name, CertificateConfigurationStoreName.class);
    }

    /** @return known CertificateConfigurationStoreName values. */
    public static Collection<CertificateConfigurationStoreName> values() {
        return values(CertificateConfigurationStoreName.class);
    }
}
