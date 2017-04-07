/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.ServicePrincipalsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * Entry point to service principal management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface ServicePrincipals extends
        SupportsListing<ServicePrincipal>,
        HasManager<GraphRbacManager>,
        HasInner<ServicePrincipalsInner> {
    /**
     * Gets the information about a service principal.
     *
     * @param objectId the unique object id
     * @return an immutable representation of the resource
     */
    ServicePrincipal getByObjectId(String objectId);

    /**
     * Gets the information about a service principal.
     *
     * @param appId the application id (or the client id)
     * @return an immutable representation of the resource
     */
    ServicePrincipal getByAppId(String appId);

    /**
     * Gets the information about a service principal.
     *
     * @param spn the service principal name
     * @return an immutable representation of the resource
     */
    ServicePrincipal getByServicePrincipalName(String spn);

    /**
     * Gets the information about a service principal.
     *
     * @param spn      the service principal name
     * @param callback the call back to handle response
     * @return the Future based service call
     */
    ServiceFuture<ServicePrincipal> getByServicePrincipalNameAsync(String spn, final ServiceCallback<ServicePrincipal> callback);

    /**
     * Gets the information about a service principal.
     *
     * @param spn      the service principal name
     * @return the Observable to the request
     */
    Observable<ServicePrincipal> getByServicePrincipalNameAsync(String spn);
}
