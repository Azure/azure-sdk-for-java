// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * This class is used to deserialize json to object.
 *
 * @see <a href="https://docs.microsoft.com/previous-versions/azure/ad/graph/api/api-catalog">reference doc</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Memberships {

    private final String odataNextLink;
    private final List<Membership> value;

    /**
     * Creates a new instance of {@link Memberships}/
     *
     * @param odataNextLink the OData next link
     * @param value the list of memberships
     */
    @JsonCreator
    public Memberships(
        @JsonAlias("odata.nextLink")
        @JsonProperty("@odata.nextLink") String odataNextLink,
        @JsonProperty("value") List<Membership> value) {
        this.odataNextLink = odataNextLink;
        this.value = value;
    }

    /**
     * Gets the OData next link.
     *
     * @return the OData next link
     */
    public String getOdataNextLink() {
        return odataNextLink;
    }

    /**
     * Gets the list of memberships.
     *
     * @return the list of memberships
     */
    public List<Membership> getValue() {
        return value;
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param o the current object
     * @return true if the specified object is equal to the current object; otherwise, false.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Memberships)) {
            return false;
        }
        final Memberships groups = (Memberships) o;
        return this.getOdataNextLink().equals(groups.getOdataNextLink())
            && this.getValue().equals(groups.getValue());
    }

    /**
     * Get hashCode value
     *
     * @return hashCode value
     */
    @Override
    public int hashCode() {
        return Objects.hash(odataNextLink, value);
    }
}
