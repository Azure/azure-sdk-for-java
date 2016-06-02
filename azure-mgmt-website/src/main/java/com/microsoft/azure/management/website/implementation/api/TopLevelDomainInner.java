/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A top level domain object.
 */
@JsonFlatten
public class TopLevelDomainInner extends Resource {
    /**
     * Name of the top level domain.
     */
    @JsonProperty(value = "properties.name")
    private String topLevelDomainName;

    /**
     * If true then the top level domain supports domain privacy.
     */
    @JsonProperty(value = "properties.privacy")
    private Boolean privacy;

    /**
     * Get the topLevelDomainName value.
     *
     * @return the topLevelDomainName value
     */
    public String topLevelDomainName() {
        return this.topLevelDomainName;
    }

    /**
     * Set the topLevelDomainName value.
     *
     * @param topLevelDomainName the topLevelDomainName value to set
     * @return the TopLevelDomainInner object itself.
     */
    public TopLevelDomainInner withTopLevelDomainName(String topLevelDomainName) {
        this.topLevelDomainName = topLevelDomainName;
        return this;
    }

    /**
     * Get the privacy value.
     *
     * @return the privacy value
     */
    public Boolean privacy() {
        return this.privacy;
    }

    /**
     * Set the privacy value.
     *
     * @param privacy the privacy value to set
     * @return the TopLevelDomainInner object itself.
     */
    public TopLevelDomainInner withPrivacy(Boolean privacy) {
        this.privacy = privacy;
        return this;
    }

}
