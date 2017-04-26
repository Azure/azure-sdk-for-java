/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;
import java.util.List;

/**
 * Entry point to AD group management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface Groups extends
        SupportsCreating<ActiveDirectoryGroup.DefinitionStages.Blank>,
        SupportsListing<ActiveDirectoryGroup>,
        SupportsDeletingById {
    /**
     * Gets the information about a group.
     *
     * @param objectId the unique object id
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    ActiveDirectoryGroup getByObjectId(String objectId) throws CloudException, IOException;

    /**
     * Gets the information about a group.
     *
     * @param displayNamePrefix the partial prefix of the display name to search
     * @return an immutable representation of the resource
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    List<ActiveDirectoryGroup> searchByDisplayName(String displayNamePrefix) throws CloudException, IOException;
}
