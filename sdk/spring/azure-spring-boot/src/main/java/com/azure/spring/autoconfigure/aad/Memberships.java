// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * This class is used to deserialize json to object.
 * Refs: https://docs.microsoft.com/en-us/previous-versions/azure/ad/graph/api/api-catalog
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Memberships {

    private final String odataNextLink;
    private final List<Membership> value;

    @JsonCreator
    public Memberships(
        @JsonAlias("odata.nextLink")
        @JsonProperty("@odata.nextLink") String odataNextLink,
        @JsonProperty("value") List<Membership> value) {
        this.odataNextLink = odataNextLink;
        this.value = value;
    }

    public String getOdataNextLink() {
        return odataNextLink;
    }

    public List<Membership> getValue() {
        return value;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(odataNextLink, value);
    }
}
