// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is used to deserialize json to object.
 *
 * @see <a href="https://docs.microsoft.com/previous-versions/azure/ad/graph/api/api-catalog">reference doc</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Membership implements Serializable {
    private static final long serialVersionUID = 9064197572478554735L;

    /**
     * The object typ group
     */
    public static final String OBJECT_TYPE_GROUP = "#microsoft.graph.group";

    /**
     * The object ID
     */
    private final String objectID;

    /**
     * The object type
     */
    private final String objectType;

    /**
     * The display name
     */
    private final String displayName;

    /**
     * Creates a new instance of {@link Membership}.
     *
     * @param objectID the object ID
     * @param objectType the object type
     * @param displayName the display name
     */
    @JsonCreator
    public Membership(
        @JsonProperty("objectId") @JsonAlias("id") String objectID,
        @JsonProperty("objectType") @JsonAlias("@odata.type") String objectType,
        @JsonProperty("displayName") String displayName) {
        this.objectID = objectID;
        this.objectType = objectType;
        this.displayName = displayName;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the object type.
     *
     * @return the object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Gets the object ID.
     *
     * @return the object ID
     */
    public String getObjectID() {
        return objectID;
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
        if (!(o instanceof Membership)) {
            return false;
        }
        final Membership group = (Membership) o;
        return this.getDisplayName().equals(group.getDisplayName())
            && this.getObjectID().equals(group.getObjectID())
            && this.getObjectType().equals(group.getObjectType());
    }

    /**
     * Get hashCode value
     *
     * @return hashCode value
     */
    @Override
    public int hashCode() {
        return Objects.hash(objectID, objectType, displayName);
    }
}
