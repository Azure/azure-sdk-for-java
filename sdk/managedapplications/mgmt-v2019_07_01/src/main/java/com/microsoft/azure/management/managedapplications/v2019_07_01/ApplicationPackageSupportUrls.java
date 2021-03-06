/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.managedapplications.v2019_07_01;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The appliance package support URLs.
 */
public class ApplicationPackageSupportUrls {
    /**
     * The public azure support URL.
     */
    @JsonProperty(value = "publicAzure")
    private String publicAzure;

    /**
     * The government cloud support URL.
     */
    @JsonProperty(value = "governmentCloud")
    private String governmentCloud;

    /**
     * Get the public azure support URL.
     *
     * @return the publicAzure value
     */
    public String publicAzure() {
        return this.publicAzure;
    }

    /**
     * Set the public azure support URL.
     *
     * @param publicAzure the publicAzure value to set
     * @return the ApplicationPackageSupportUrls object itself.
     */
    public ApplicationPackageSupportUrls withPublicAzure(String publicAzure) {
        this.publicAzure = publicAzure;
        return this;
    }

    /**
     * Get the government cloud support URL.
     *
     * @return the governmentCloud value
     */
    public String governmentCloud() {
        return this.governmentCloud;
    }

    /**
     * Set the government cloud support URL.
     *
     * @param governmentCloud the governmentCloud value to set
     * @return the ApplicationPackageSupportUrls object itself.
     */
    public ApplicationPackageSupportUrls withGovernmentCloud(String governmentCloud) {
        this.governmentCloud = governmentCloud;
        return this;
    }

}
