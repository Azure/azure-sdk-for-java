// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 9064197572478554735L;

    private String objectID;
    private String objectType;
    private String displayName;

    @JsonCreator
    public UserGroup(
            @JsonProperty("objectId") @JsonAlias("id") String objectID,
            @JsonProperty("objectType") @JsonAlias("@odata.type") String objectType,
            @JsonProperty("displayName") String displayName) {
        this.objectID = objectID;
        this.objectType = objectType;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getObjectID() {
        return objectID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserGroup)) {
            return false;
        }
        final UserGroup group = (UserGroup) o;
        return this.getDisplayName().equals(group.getDisplayName())
                && this.getObjectID().equals(group.getObjectID())
                && this.getObjectType().equals(group.getObjectType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectID, objectType, displayName);
    }
}
