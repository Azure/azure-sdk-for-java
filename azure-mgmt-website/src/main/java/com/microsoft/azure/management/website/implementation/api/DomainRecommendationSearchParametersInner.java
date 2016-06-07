/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Domain recommendation search parameters.
 */
public class DomainRecommendationSearchParametersInner {
    /**
     * Keywords to be used for generating domain recommendations.
     */
    private String keywords;

    /**
     * Maximum number of recommendations.
     */
    private Integer maxDomainRecommendations;

    /**
     * Get the keywords value.
     *
     * @return the keywords value
     */
    public String keywords() {
        return this.keywords;
    }

    /**
     * Set the keywords value.
     *
     * @param keywords the keywords value to set
     * @return the DomainRecommendationSearchParametersInner object itself.
     */
    public DomainRecommendationSearchParametersInner withKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    /**
     * Get the maxDomainRecommendations value.
     *
     * @return the maxDomainRecommendations value
     */
    public Integer maxDomainRecommendations() {
        return this.maxDomainRecommendations;
    }

    /**
     * Set the maxDomainRecommendations value.
     *
     * @param maxDomainRecommendations the maxDomainRecommendations value to set
     * @return the DomainRecommendationSearchParametersInner object itself.
     */
    public DomainRecommendationSearchParametersInner withMaxDomainRecommendations(Integer maxDomainRecommendations) {
        this.maxDomainRecommendations = maxDomainRecommendations;
        return this;
    }

}
