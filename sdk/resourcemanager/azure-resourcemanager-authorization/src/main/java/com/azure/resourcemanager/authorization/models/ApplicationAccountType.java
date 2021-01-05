// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for Application Account Type */
public final class ApplicationAccountType extends ExpandableStringEnum<ApplicationAccountType> {

    /** Users with a Microsoft work or school account in my organization’s Azure AD tenant (single tenant) */
    public static final ApplicationAccountType AZURE_AD_MY_ORG = fromString("AzureADMyOrg");

    /** Users with a Microsoft work or school account in any organization’s Azure AD tenant (multi-tenant). */
    public static final ApplicationAccountType AZURE_AD_MULTIPLE_ORGS = fromString("AzureADMultipleOrgs");

    /** Users with a personal Microsoft account, or a work or school account in any organization’s Azure AD tenant. */
    public static final ApplicationAccountType AZURE_AD_AND_PERSONAL_MICROSOFT_ACCOUNT =
        fromString("AzureADandPersonalMicrosoftAccount");

    /** Users with a personal Microsoft account only. */
    public static final ApplicationAccountType PERSONAL_MICROSOFT_ACCOUNT = fromString("PersonalMicrosoftAccount");

    /**
     * Creates or finds a ApplicationAccountType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ApplicationAccountType.
     */
    public static ApplicationAccountType fromString(String name) {
        return fromString(name, ApplicationAccountType.class);
    }

    /** @return known ApplicationAccountType values. */
    public static Collection<ApplicationAccountType> values() {
        return values(ApplicationAccountType.class);
    }
}
