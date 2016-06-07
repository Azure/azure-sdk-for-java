/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Represents a site cloneability criterion.
 */
public class SiteCloneabilityCriterion {
    /**
     * Name of criterion.
     */
    private String name;

    /**
     * Description of criterion.
     */
    private String description;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the SiteCloneabilityCriterion object itself.
     */
    public SiteCloneabilityCriterion withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the SiteCloneabilityCriterion object itself.
     */
    public SiteCloneabilityCriterion withDescription(String description) {
        this.description = description;
        return this;
    }

}
