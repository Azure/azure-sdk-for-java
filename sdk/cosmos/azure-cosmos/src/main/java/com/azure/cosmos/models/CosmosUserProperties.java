// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.User;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Cosmos user properties.
 */
public final class CosmosUserProperties {

    private User user;

    /**
     * Initialize a user object.
     */
    public CosmosUserProperties() {
        this.user = new User();
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
     * @param jsonNode the json node that represents the database user.
     */
    CosmosUserProperties(ObjectNode jsonNode) {
        this.user = new User(jsonNode);
    }

    // Converting container to CosmosContainerProperties
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
        return new User(this.user.getPropertyBag());
    }


    Resource getResource() {
        return this.user;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.user.getId();
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.user.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.user.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.user.getETag();
    }

    static List<CosmosUserProperties> getFromV2Results(List<User> results) {
        return results.stream().map(CosmosUserProperties::new).collect(Collectors.toList());
    }
}
