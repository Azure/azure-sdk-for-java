/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Collection of site instances.
 */
public class SiteInstanceCollectionInner {
    /**
     * Collection of resources.
     */
    private List<SiteInstance> value;

    /**
     * Link to next page of resources.
     */
    private String nextLink;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<SiteInstance> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the SiteInstanceCollectionInner object itself.
     */
    public SiteInstanceCollectionInner withValue(List<SiteInstance> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink value.
     *
     * @return the nextLink value
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink value.
     *
     * @param nextLink the nextLink value to set
     * @return the SiteInstanceCollectionInner object itself.
     */
    public SiteInstanceCollectionInner withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

}
