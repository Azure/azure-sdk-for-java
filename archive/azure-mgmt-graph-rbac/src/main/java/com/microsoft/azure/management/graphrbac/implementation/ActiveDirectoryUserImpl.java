/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.PasswordProfile;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for User and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryUserImpl
        extends CreatableUpdatableImpl<ActiveDirectoryUser, UserInner, ActiveDirectoryUserImpl>
        implements
        ActiveDirectoryUser,
        ActiveDirectoryUser.Definition,
        ActiveDirectoryUser.Update {

    private final GraphRbacManager manager;
    private UserCreateParametersInner createParameters;
    private UserUpdateParametersInner updateParameters;
    private String emailAlias;

    ActiveDirectoryUserImpl(UserInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new UserCreateParametersInner().withDisplayName(name()).withAccountEnabled(true);
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
    public CountryIsoCode usageLocation() {
        return CountryIsoCode.fromString(inner().usageLocation());
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
    public ActiveDirectoryUserImpl withEmailAlias(String emailAlias) {
        this.emailAlias = emailAlias;
        return this;
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
        Observable<ActiveDirectoryUserImpl> domain;
        if (emailAlias != null) {
            domain = manager().inner().domains().listAsync()
                .map(new Func1<List<DomainInner>, ActiveDirectoryUserImpl>() {
                    @Override
                    public ActiveDirectoryUserImpl call(List<DomainInner> domainInners) {
                        for (DomainInner inner : domainInners) {
                            if (inner.isVerified() && inner.isDefault()) {
                                if (emailAlias != null) {
                                    withUserPrincipalName(emailAlias + "@" + inner.name());
                                    break;
                                }
                            }
                        }
                        return ActiveDirectoryUserImpl.this;
                    }
                });
        } else {
            domain = Observable.just(this);
        }
        return domain.flatMap(new Func1<ActiveDirectoryUserImpl, Observable<UserInner>>() {
            @Override
            public Observable<UserInner> call(ActiveDirectoryUserImpl activeDirectoryUser) {
                return manager().inner().users().createAsync(createParameters);
            }
        })
        .map(innerToFluentMap(this));
    }

    public Observable<ActiveDirectoryUser> updateResourceAsync() {
        return manager().inner().users().updateAsync(id(), updateParameters)
                .flatMap(new Func1<Void, Observable<ActiveDirectoryUser>>() {
                    @Override
                    public Observable<ActiveDirectoryUser> call(Void aVoid) {
                        return refreshAsync();
                    }
                });
    }

    private void withMailNickname(String mailNickname) {
        createParameters.withMailNickname(mailNickname);
        updateParameters.withMailNickname(mailNickname);
    }

    @Override
    public ActiveDirectoryUserImpl withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin) {
        createParameters.passwordProfile().withForceChangePasswordNextLogin(promptToChangePasswordOnLogin);
        return this;
    }

    @Override
    public String toString() {
        return name() + " - " + userPrincipalName();
    }

    @Override
    public ActiveDirectoryUserImpl withAccountEnabled(boolean accountEnabled) {
        createParameters.withAccountEnabled(accountEnabled);
        updateParameters.withAccountEnabled(accountEnabled);
        return this;
    }

    @Override
    public ActiveDirectoryUserImpl withUsageLocation(CountryIsoCode usageLocation) {
        createParameters.withUsageLocation(usageLocation.toString());
        updateParameters.withUsageLocation(usageLocation.toString());
        return this;
    }
}
