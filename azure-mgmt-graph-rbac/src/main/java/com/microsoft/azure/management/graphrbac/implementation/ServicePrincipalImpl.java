/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

import java.util.List;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
class ServicePrincipalImpl
        extends CreatableUpdatableImpl<ServicePrincipal, ServicePrincipalInner, ServicePrincipalImpl>
        implements
            ServicePrincipal,
            ServicePrincipal.Definition,
            ServicePrincipal.Update {
    private ServicePrincipalsInner client;
    private ServicePrincipalCreateParametersInner createParameters;

    ServicePrincipalImpl(String appId, ServicePrincipalsInner client) {
        super(appId, new ServicePrincipalInner());
        this.client = client;
        this.createParameters = new ServicePrincipalCreateParametersInner().withAppId(appId);
    }

    ServicePrincipalImpl(ServicePrincipalInner innerObject, ServicePrincipalsInner client) {
        super(innerObject.appId(), innerObject);
        this.client = client;
        this.createParameters = new ServicePrincipalCreateParametersInner();
    }

    @Override
    public String objectId() {
        return inner().objectId();
    }

    @Override
    public String objectType() {
        return inner().objectType();
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public String appId() {
        return inner().appId();
    }

    @Override
    public List<String> servicePrincipalNames() {
        return inner().servicePrincipalNames();
    }

    @Override
    public ServicePrincipalImpl withAccountEnabled(boolean enabled) {
        createParameters.withAccountEnabled(enabled);
        return this;
    }

    @Override
    public ServicePrincipal refresh() throws Exception {
        return null;
    }

    @Override
    public Observable<ServicePrincipal> createResourceAsync() {
        return null;
    }

    @Override
    public Observable<ServicePrincipal> applyAsync() {
        return null;
    }
}
