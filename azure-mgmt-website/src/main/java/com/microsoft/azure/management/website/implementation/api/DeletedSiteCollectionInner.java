/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Collection of deleted sites.
 */
public class DeletedSiteCollectionInner {
    /**
     * Collection of resources.
     */
    private List<DeletedSite> value;

    /**
     * Link to next page of resources.
     */
    private String nextLink;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<DeletedSite> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the DeletedSiteCollectionInner object itself.
     */
    public DeletedSiteCollectionInner withValue(List<DeletedSite> value) {
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
     * @return the DeletedSiteCollectionInner object itself.
     */
    public DeletedSiteCollectionInner withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

}
