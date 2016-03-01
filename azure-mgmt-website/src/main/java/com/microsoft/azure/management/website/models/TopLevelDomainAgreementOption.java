/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


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
    public Boolean getIncludePrivacy() {
        return this.includePrivacy;
    }

    /**
     * Set the includePrivacy value.
     *
     * @param includePrivacy the includePrivacy value to set
     */
    public void setIncludePrivacy(Boolean includePrivacy) {
        this.includePrivacy = includePrivacy;
    }

}
