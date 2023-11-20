// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines action values for type of {@link LifetimeAction} in {@link CertificatePolicy}.
 */
public final class CertificatePolicyAction extends ExpandableStringEnum<CertificatePolicyAction> {

    /**
     * Static value EmailContacts for CertificatePolicyAction.
     */
    public static final CertificatePolicyAction EMAIL_CONTACTS = fromString("EmailContacts");

    /**
     * Static valueAutoRenew for CertificatePolicyAction.
     */
    public static final CertificatePolicyAction AUTO_RENEW = fromString("AutoRenew");

    /**
     * Creates or finds a CertificatePolicyAction from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CertificatePolicyAction.
     */
    @JsonCreator
    public static CertificatePolicyAction fromString(String name) {
        return fromString(name, CertificatePolicyAction.class);
    }

    /**
     * @return known CertificatePolicyAction values.
     */
    public static Collection<CertificatePolicyAction> values() {
        return values(CertificatePolicyAction.class);
    }
}
