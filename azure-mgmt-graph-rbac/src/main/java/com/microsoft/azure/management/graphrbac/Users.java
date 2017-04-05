/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * Entry point to AD user management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface Users extends
        SupportsListing<User>,
        HasManager<GraphRbacManager> {
    /**
     * Gets the information about a user.
     *
     * @param objectId the unique object id
     * @return an immutable representation of the resource
     */
    User getByObjectId(String objectId);

    /**
     * Gets the information about a user.
     *
     * @param upn the user principal name
     * @return an immutable representation of the resource
     */
    User getByUserPrincipalName(String upn);

    /**
     * Gets the information about a user.
     *
     * @param upn the user principal name
     * @param callback the callback to handle the response
     * @return an Future based service call
     */
    ServiceFuture<User> getByUserPrincipalNameAsync(String upn, ServiceCallback<User> callback);

    /**
     * Gets the information about a user.
     *
     * @param upn the user principal name
     * @return an Future based service call
     */
    Observable<User> getByUserPrincipalNameAsync(String upn);
}
