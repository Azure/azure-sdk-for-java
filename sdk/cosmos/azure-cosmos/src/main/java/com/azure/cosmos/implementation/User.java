// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * Represents a database user in the Azure Cosmos DB database service.
 */
public class User extends Resource {

    /**
     * Initialize a user object.
     */
    public User() {
        super();
    }

    /**
     * Initialize a user object from json string.
     *
     * @param jsonString the json string that represents the database user.
     */
    public User(String jsonString) {
        super(jsonString);
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current instance of User
     */
    public User setId(String id){
        super.setId(id);
        return this;
    }

    /**
     * Gets the self-link of the permissions associated with the user.
     *
     * @return the permissions link.
     */
    public String getPermissionsLink() {
        String selfLink = this.getSelfLink();
        if (selfLink.endsWith("/")) {
            return selfLink + super.getString(Constants.Properties.PERMISSIONS_LINK);
        } else {
            return selfLink + "/" + super.getString(Constants.Properties.PERMISSIONS_LINK);
        }
    }
}
