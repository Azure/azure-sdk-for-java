// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Cosmos user properties.
 */
public final class CosmosUserProperties extends Resource {
    /**
     * Initialize a user object.
     */
    public CosmosUserProperties() {
        super();
    }

    /**
     * Gets the id
     *
     * @return the id of the user
     */
    public String getId() {
        return super.getId();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current instance of cosmos user properties
     */
    public CosmosUserProperties setId(String id) {
        return (CosmosUserProperties) super.setId(id);
    }

    /**
     * Initialize a user object from json string.
     *
     * @param jsonString the json string that represents the database user.
     */
    CosmosUserProperties(String jsonString) {
        super(jsonString);
    }

    // Converting document collection to CosmosContainerProperties
    CosmosUserProperties(User user) {
        super(user.toJson());
    }

    /**
     * Gets the self-link of the permissions associated with the user.
     *
     * @return the permissions link.
     */
    String getPermissionsLink() {
        String selfLink = this.getSelfLink();
        if (selfLink.endsWith("/")) {
            return selfLink + super.getString(Constants.Properties.PERMISSIONS_LINK);
        } else {
            return selfLink + "/" + super.getString(Constants.Properties.PERMISSIONS_LINK);
        }
    }

    User getV2User() {
        return new User(this.toJson());
    }

    static List<CosmosUserProperties> getFromV2Results(List<User> results) {
        return results.stream().map(CosmosUserProperties::new).collect(Collectors.toList());
    }
}
