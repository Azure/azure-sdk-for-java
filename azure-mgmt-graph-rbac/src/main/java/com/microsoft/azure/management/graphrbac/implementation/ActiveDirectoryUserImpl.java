/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.GraphErrorException;
import com.microsoft.azure.management.graphrbac.PasswordProfile;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for User and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryUserImpl
        extends CreatableUpdatableImpl<ActiveDirectoryUser, UserInner, ActiveDirectoryUserImpl>
        implements
        ActiveDirectoryUser,
        ActiveDirectoryUser.Definition {

    private final GraphRbacManager manager;
    private UserCreateParametersInner createParameters;
    private UserUpdateParametersInner updateParameters;

    ActiveDirectoryUserImpl(UserInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new UserCreateParametersInner().withDisplayName(name());
        this.updateParameters = new UserUpdateParametersInner().withDisplayName(name());
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public String userPrincipalName() {
        return inner().userPrincipalName();
    }

    @Override
    public String name() {
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
    public GraphRbacManager manager() {
        return manager;
    }

    @Override
    public ActiveDirectoryUserImpl withUserPrincipalName(String userPrincipalName) {
        createParameters.withUserPrincipalName(userPrincipalName);
        if (isInCreateMode() || updateParameters.mailNickname() != null) {
            withMailNickname(userPrincipalName.replaceAll("@.+$", ""));
        }
        return this;
    }

    @Override
    public ActiveDirectoryUserImpl withEmailAddress(String emailAddress) {
        return withUserPrincipalName(emailAddress);
    }

    @Override
    public ActiveDirectoryUserImpl withPassword(String password) {
        createParameters.withPasswordProfile(new PasswordProfile().withPassword(password));
        return this;
    }

    @Override
    protected Observable<UserInner> getInnerAsync() {
        return manager.inner().users().getAsync(this.id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Observable<ActiveDirectoryUser> createResourceAsync() {
        return manager().inner().users().createAsync(createParameters)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends UserInner>>() {
                    @Override
                    public Observable<? extends UserInner> call(Throwable throwable) {
                        if (throwable instanceof GraphErrorException
                                && throwable.getMessage().contains("Property userPrincipalName is invalid")) {
                            // TODO: append #EXT#@domain.com
                        }
                        return Observable.error(throwable);
                    }
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public ActiveDirectoryUserImpl withMailNickname(String mailNickname) {
        createParameters.withMailNickname(mailNickname);
        updateParameters.withMailNickname(mailNickname);
        return this;
    }
}
