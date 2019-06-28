package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.User;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosUserProperties extends Resource {
    /**
     * Initialize a user object.
     */
    public CosmosUserProperties() {
        super();
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current instance of cosmos user properties
     */
    public CosmosUserProperties id(String id) {
        return (CosmosUserProperties) super.id(id);
    }

    /**
     * Initialize a user object from json string.
     *
     * @param jsonString the json string that represents the database user.
     */
    CosmosUserProperties(String jsonString) {
        super(jsonString);
    }

    CosmosUserProperties(ResourceResponse<User> response) {
        super(response.getResource().toJson()); 
    }

    // Converting document collection to CosmosContainerProperties
    CosmosUserProperties(User user){
        super(user.toJson());
    }

    /**
     * Gets the self-link of the permissions associated with the user.
     *
     * @return the permissions link.
     */
    public String getPermissionsLink() {
        String selfLink = this.selfLink();
        if (selfLink.endsWith("/")) {
            return selfLink + super.getString(Constants.Properties.PERMISSIONS_LINK);
        } else {
            return selfLink + "/" + super.getString(Constants.Properties.PERMISSIONS_LINK);
        }
    }

    public User getV2User() {
        return new User(this.toJson());
    }

    static List<CosmosUserProperties> getFromV2Results(List<User> results) {
        return results.stream().map(CosmosUserProperties::new).collect(Collectors.toList());
    }
}