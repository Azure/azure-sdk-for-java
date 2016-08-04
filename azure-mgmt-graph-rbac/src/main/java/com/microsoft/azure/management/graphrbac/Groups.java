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
import java.util.List;

/**
 * Entry point to tenant management API.
 */
public interface Groups extends
        SupportsCreating<Group.DefinitionStages.Blank>,
        SupportsListing<Group>,
        SupportsDeleting {
    /**
     * Gets the information about a user.
     *
     * @param objectId the unique object id
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    Group getByObjectId(String objectId) throws CloudException, IOException;

    /**
     * Gets the information about a user.
     *
     * @param displayNamePrefix the partial prefix of the display name to search
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    List<Group> searchByDisplayName(String displayNamePrefix) throws CloudException, IOException;
}
