// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Cosmos user properties.
 */
public final class CosmosUserProperties extends ResourceWrapper{

    private User user;
    /**
     * Initialize a user object.
     */
    public CosmosUserProperties() {
        this.user = new User();
    }

    @Override
    Resource getResource() {
        return this.user;
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current instance of cosmos user properties
     */
    public CosmosUserProperties setId(String id) {
        this.user.setId(id);
        return this;
    }

    /**
     * Initialize a user object from json string.
     *
     * @param jsonString the json string that represents the database user.
     */
    CosmosUserProperties(String jsonString) {
        this.user = new User(jsonString);
    }

    // Converting document collection to CosmosContainerProperties
    CosmosUserProperties(User user) {
        this.user = user;
    }

    /**
     * Gets the self-link of the permissions associated with the user.
     *
     * @return the permissions link.
     */
    String getPermissionsLink() {
        return this.user.getPermissionsLink();
    }

    User getV2User() {
        return new User(this.user.toJson());
    }

    static List<CosmosUserProperties> getFromV2Results(List<User> results) {
        return results.stream().map(CosmosUserProperties::new).collect(Collectors.toList());
    }
}
