/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.List;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class ServicePrincipalImpl
        extends WrapperImpl<ServicePrincipalInner>
        implements
            ServicePrincipal,
            ServicePrincipal.Definition,
            ServicePrincipal.Update {
    private ServicePrincipalsInner client;
    private ServicePrincipalCreateParametersInner createParameters;

    ServicePrincipalImpl(String appId, ServicePrincipalsInner client) {
        super(new ServicePrincipalInner());
        this.client = client;
        this.createParameters = new ServicePrincipalCreateParametersInner().withAppId(appId);
    }

    ServicePrincipalImpl(ServicePrincipalInner innerObject, ServicePrincipalsInner client) {
        super(innerObject);
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
    public ServicePrincipalImpl create() throws Exception {
        this.setInner(client.create(createParameters).getBody());
        return this;
    }

    @Override
    public ServiceCall createAsync(final ServiceCallback<ServicePrincipal> callback) {
        final ServicePrincipalImpl self = this;
        return client.createAsync(createParameters, new ServiceCallback<ServicePrincipalInner>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<ServicePrincipalInner> result) {
                self.setInner(result.getBody());
                callback.success(new ServiceResponse<ServicePrincipal>(self, result.getResponse()));
            }
        });
    }

    @Override
    public String key() {
        return objectId();
    }
}
