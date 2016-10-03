/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.PasswordProfile;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

/**
 * Implementation for User and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class UserImpl
        extends CreatableUpdatableImpl<User, UserInner, UserImpl>
        implements
            User,
            User.Definition,
            User.Update {
    private UsersInner client;
    private UserCreateParametersInner createParameters;

    UserImpl(String userPrincipalName, UsersInner client) {
        super(userPrincipalName, new UserInner());
        this.client = client;
        this.createParameters = new UserCreateParametersInner().withUserPrincipalName(userPrincipalName);
    }

    UserImpl(UserInner innerObject, UsersInner client) {
        super(innerObject.userPrincipalName(), innerObject);
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
    public UserImpl withAccountEnabled(boolean enabled) {
        createParameters.withAccountEnabled(enabled);
        return this;
    }

    @Override
    public UserImpl withDisplayName(String displayName) {
        createParameters.withDisplayName(displayName);
        return this;
    }

    @Override
    public UserImpl withMailNickname(String mailNickname) {
        createParameters.withMailNickname(mailNickname);
        return this;
    }

    @Override
    public UserImpl withPassword(String password) {
        createParameters.withPasswordProfile(new PasswordProfile().withPassword(password));
        return this;
    }

    @Override
    public UserImpl withPassword(String password, boolean forceChangePasswordNextLogin) {
        createParameters.withPasswordProfile(new PasswordProfile().withPassword(password).withForceChangePasswordNextLogin(forceChangePasswordNextLogin));
        return this;
    }

    @Override
    public User refresh() {
        setInner(client.get(name()));
        return this;
    }

    @Override
    public Observable<User> createResourceAsync() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean isInCreateMode() {
        return false;
    }
}
