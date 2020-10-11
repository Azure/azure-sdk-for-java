// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * entity class of UserGroups
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGroups {

    private String odataNextLink;
    private List<UserGroup> value;

    @JsonCreator
    public UserGroups(
            @JsonProperty("odata.nextLink") String odataNextLink,
            @JsonProperty("value") List<UserGroup> value) {
        this.odataNextLink = odataNextLink;
        this.value = value;
    }

    public String getOdataNextLink() {
        return odataNextLink;
    }

    public List<UserGroup> getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserGroups)) {
            return false;
        }
        final UserGroups groups = (UserGroups) o;
        return this.getOdataNextLink().equals(groups.getOdataNextLink())
                && this.getValue().equals(groups.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(odataNextLink, value);
    }
}
