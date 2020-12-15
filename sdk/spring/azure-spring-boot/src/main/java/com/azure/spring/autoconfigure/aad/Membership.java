// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is used to deserialize json to object.
 * Refs: https://docs.microsoft.com/en-us/previous-versions/azure/ad/graph/api/api-catalog
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Membership implements Serializable {
    private static final long serialVersionUID = 9064197572478554735L;
    public static final String OBJECT_TYPE_GROUP = "#microsoft.graph.group";

    private final String objectID;
    private final String objectType;
    private final String displayName;

    @JsonCreator
    public Membership(
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
        if (!(o instanceof Membership)) {
            return false;
        }
        final Membership group = (Membership) o;
        return this.getDisplayName().equals(group.getDisplayName())
            && this.getObjectID().equals(group.getObjectID())
            && this.getObjectType().equals(group.getObjectType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectID, objectType, displayName);
    }
}
