/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Options for retrieving the list of top level domain legal agreements.
 */
public class TopLevelDomainAgreementOption {
    /**
     * If true then the list of agreements will inclue agreements for domain
     * privacy as well.
     */
    private Boolean includePrivacy;

    /**
     * Get the includePrivacy value.
     *
     * @return the includePrivacy value
     */
    public Boolean includePrivacy() {
        return this.includePrivacy;
    }

    /**
     * Set the includePrivacy value.
     *
     * @param includePrivacy the includePrivacy value to set
     * @return the TopLevelDomainAgreementOption object itself.
     */
    public TopLevelDomainAgreementOption withIncludePrivacy(Boolean includePrivacy) {
        this.includePrivacy = includePrivacy;
        return this;
    }

}
