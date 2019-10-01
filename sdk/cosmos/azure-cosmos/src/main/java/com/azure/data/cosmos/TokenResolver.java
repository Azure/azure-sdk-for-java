// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import java.util.Map;

/**
 * This interface is for client side implementation, which can be used for initializing
 * AsyncDocumentClient without passing master key, resource token and permission feed.<br>
 * <br>
 * Each time the SDK create request for CosmosDB, authorization token is generated based on that
 * request at client side which enables creation of one AsyncDocumentClient per application shared across various users
 * with different resource permissions.
 */
@FunctionalInterface
public interface TokenResolver {

    /**
     * This method will consume the request information and based on that it will generate the authorization token.
     * @param properties the user properties.
     * @param requestVerb Request verb i.e. GET, POST, PUT etc.
     * @param resourceIdOrFullName ResourceID or resource full name.
     * @param resourceType Resource type i.e. Database, DocumentCollection, Document etc.
     * @return The authorization token.
     */
    public String getAuthorizationToken(String requestVerb, String resourceIdOrFullName, CosmosResourceType resourceType, Map<String, Object> properties);

}
