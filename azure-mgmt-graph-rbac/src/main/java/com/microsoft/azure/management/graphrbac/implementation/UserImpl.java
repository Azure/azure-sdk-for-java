/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class UserImpl
        extends WrapperImpl<UserInner>
        implements
            User,
            User.Definition,
            User.Update {
    private UsersInner client;
    private UserCreateParametersInner createParameters;

    protected UserImpl(UserInner innerObject, UsersInner client) {
        super(innerObject);
        this.client = client;
        this.createParameters = new UserCreateParametersInner();
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
    public String userPrincipalName() {
        return inner().userPrincipalName();
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public String signInName() {
        return inner().signInName();
    }

    @Override
    public String mail() {
        return inner().mail();
    }

    @Override
    public String mailNickname() {
        return inner().mailNickname();
    }

    @Override
    public UserImpl create() throws Exception {
        setInner(client.create(createParameters).getBody());
        return this;
    }

    @Override
    public ServiceCall createAsync(final ServiceCallback<User> callback) {
        final UserImpl self = this;
        return client.createAsync(createParameters, new ServiceCallback<UserInner>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<UserInner> result) {
                setInner(result.getBody());
                callback.success(new ServiceResponse<User>(self, result.getResponse()));
            }
        });
    }

    @Override
    public String key() {
        return objectId();
    }
}
