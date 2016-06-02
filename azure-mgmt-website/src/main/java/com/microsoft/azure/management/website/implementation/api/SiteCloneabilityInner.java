/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents whether or not a web app is cloneable.
 */
public class SiteCloneabilityInner {
    /**
     * Name of web app. Possible values include: 'Cloneable',
     * 'PartiallyCloneable', 'NotCloneable'.
     */
    @JsonProperty(required = true)
    private CloneAbilityResult result;

    /**
     * List of features enabled on web app that prevent cloning.
     */
    private List<SiteCloneabilityCriterion> blockingFeatures;

    /**
     * List of features enabled on web app that are non-blocking but cannot be
     * cloned. The web app can still be cloned
     * but the features in this list will not be set up on cloned
     * web app.
     */
    private List<SiteCloneabilityCriterion> unsupportedFeatures;

    /**
     * List of blocking application characteristics.
     */
    private List<SiteCloneabilityCriterion> blockingCharacteristics;

    /**
     * Get the result value.
     *
     * @return the result value
     */
    public CloneAbilityResult result() {
        return this.result;
    }

    /**
     * Set the result value.
     *
     * @param result the result value to set
     * @return the SiteCloneabilityInner object itself.
     */
    public SiteCloneabilityInner withResult(CloneAbilityResult result) {
        this.result = result;
        return this;
    }

    /**
     * Get the blockingFeatures value.
     *
     * @return the blockingFeatures value
     */
    public List<SiteCloneabilityCriterion> blockingFeatures() {
        return this.blockingFeatures;
    }

    /**
     * Set the blockingFeatures value.
     *
     * @param blockingFeatures the blockingFeatures value to set
     * @return the SiteCloneabilityInner object itself.
     */
    public SiteCloneabilityInner withBlockingFeatures(List<SiteCloneabilityCriterion> blockingFeatures) {
        this.blockingFeatures = blockingFeatures;
        return this;
    }

    /**
     * Get the unsupportedFeatures value.
     *
     * @return the unsupportedFeatures value
     */
    public List<SiteCloneabilityCriterion> unsupportedFeatures() {
        return this.unsupportedFeatures;
    }

    /**
     * Set the unsupportedFeatures value.
     *
     * @param unsupportedFeatures the unsupportedFeatures value to set
     * @return the SiteCloneabilityInner object itself.
     */
    public SiteCloneabilityInner withUnsupportedFeatures(List<SiteCloneabilityCriterion> unsupportedFeatures) {
        this.unsupportedFeatures = unsupportedFeatures;
        return this;
    }

    /**
     * Get the blockingCharacteristics value.
     *
     * @return the blockingCharacteristics value
     */
    public List<SiteCloneabilityCriterion> blockingCharacteristics() {
        return this.blockingCharacteristics;
    }

    /**
     * Set the blockingCharacteristics value.
     *
     * @param blockingCharacteristics the blockingCharacteristics value to set
     * @return the SiteCloneabilityInner object itself.
     */
    public SiteCloneabilityInner withBlockingCharacteristics(List<SiteCloneabilityCriterion> blockingCharacteristics) {
        this.blockingCharacteristics = blockingCharacteristics;
        return this;
    }

}
