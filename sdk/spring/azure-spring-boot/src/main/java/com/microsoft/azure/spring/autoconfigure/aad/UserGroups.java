// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGroups {

    @JsonProperty("odata.nextLink")
    private Optional<String> odataNextLink;

    @JsonProperty("value")
    private List<UserGroup> value;

    public UserGroups(Optional<String> odataNextLink, List<UserGroup> value) {
        this.odataNextLink = odataNextLink;
        this.value = value;
    }

    public UserGroups() {
    }

    public Optional<String> getOdataNextLink() {
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
