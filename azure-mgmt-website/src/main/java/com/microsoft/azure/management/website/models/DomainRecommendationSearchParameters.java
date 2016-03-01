/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


/**
 * Domain recommendation search parameters.
 */
public class DomainRecommendationSearchParameters {
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
    public String getKeywords() {
        return this.keywords;
    }

    /**
     * Set the keywords value.
     *
     * @param keywords the keywords value to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Get the maxDomainRecommendations value.
     *
     * @return the maxDomainRecommendations value
     */
    public Integer getMaxDomainRecommendations() {
        return this.maxDomainRecommendations;
    }

    /**
     * Set the maxDomainRecommendations value.
     *
     * @param maxDomainRecommendations the maxDomainRecommendations value to set
     */
    public void setMaxDomainRecommendations(Integer maxDomainRecommendations) {
        this.maxDomainRecommendations = maxDomainRecommendations;
    }

}
