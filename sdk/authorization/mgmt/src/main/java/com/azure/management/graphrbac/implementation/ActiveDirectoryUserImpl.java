// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.graphrbac.implementation;

import com.azure.management.graphrbac.ActiveDirectoryUser;
import com.azure.management.graphrbac.PasswordProfile;
import com.azure.management.graphrbac.UserCreateParameters;
import com.azure.management.graphrbac.UserUpdateParameters;
import com.azure.management.graphrbac.models.UserInner;
import com.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for User and its parent interfaces.
 */
class ActiveDirectoryUserImpl
        extends CreatableUpdatableImpl<ActiveDirectoryUser, UserInner, ActiveDirectoryUserImpl>
        implements ActiveDirectoryUser,
            ActiveDirectoryUser.Definition,
            ActiveDirectoryUser.Update {

    private final GraphRbacManager manager;
    private UserCreateParameters createParameters;
    private UserUpdateParameters updateParameters;
    private String emailAlias;

    ActiveDirectoryUserImpl(UserInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new UserCreateParameters().withDisplayName(name()).withAccountEnabled(true);
        this.updateParameters = new UserUpdateParameters().withDisplayName(name());
    }

    @Override
    public String userPrincipalName() {
        return inner().userPrincipalName();
    }

    @Override
    public String signInName() {
        if (inner().signInNames() != null && !inner().signInNames().isEmpty()) {
            return inner().signInNames().get(0).value();
        } else {
            return null;
        }
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
    protected Mono<UserInner> getInnerAsync() {
        return manager.inner().users().getAsync(this.id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<ActiveDirectoryUser> createResourceAsync() {
        Mono<ActiveDirectoryUserImpl> domain;
        if (emailAlias != null) {
            domain = manager().inner().domains().listAsync(null)
                    .map(domainInner -> {
                        if (domainInner.isVerified() && domainInner.isDefault()) {
                            if (emailAlias != null) {
                                withUserPrincipalName(emailAlias + "@" + domainInner.name());
                            }
                        }
                        return Mono.just(ActiveDirectoryUserImpl.this);
                    }).blockLast();
        } else {
            domain = Mono.just(this);
        }
        return domain.flatMap(activeDirectoryUser -> manager().inner().users().createAsync(createParameters))
        .map(innerToFluentMap(this));
    }

    public Mono<ActiveDirectoryUser> updateResourceAsync() {
        return manager().inner().users().updateAsync(id(), updateParameters)
                .then(ActiveDirectoryUserImpl.this.refreshAsync());
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

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }
}
