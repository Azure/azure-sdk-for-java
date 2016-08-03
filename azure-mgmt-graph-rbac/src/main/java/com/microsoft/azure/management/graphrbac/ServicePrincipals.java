/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 * Entry point to tenant management API.
 */
public interface ServicePrincipals extends
        SupportsCreating<ServicePrincipal.DefinitionStages.Blank>,
        SupportsListing<ServicePrincipal>,
        SupportsDeleting {
    /**
     * Gets the information about a service principal.
     *
     * @param objectId the unique object id
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    ServicePrincipal getByObjectId(String objectId) throws CloudException, IOException;

    /**
     * Gets the information about a service principal.
     *
     * @param appId the application id (or the client id)
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    ServicePrincipal getByAppId(String appId) throws CloudException, IOException;

    /**
     * Gets the information about a service principal.
     *
     * @param spn the service principal name
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    ServicePrincipal getByServicePrincipalName(String spn) throws CloudException, IOException;
}
