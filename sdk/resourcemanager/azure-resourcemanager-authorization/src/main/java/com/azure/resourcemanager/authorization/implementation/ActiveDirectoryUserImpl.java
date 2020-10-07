// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.PasswordProfile;
import com.azure.resourcemanager.authorization.models.UserCreateParameters;
import com.azure.resourcemanager.authorization.models.UserUpdateParameters;
import com.azure.resourcemanager.authorization.fluent.models.UserInner;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

/** Implementation for User and its parent interfaces. */
class ActiveDirectoryUserImpl extends CreatableUpdatableImpl<ActiveDirectoryUser, UserInner, ActiveDirectoryUserImpl>
    implements ActiveDirectoryUser, ActiveDirectoryUser.Definition, ActiveDirectoryUser.Update {

    private final AuthorizationManager manager;
    private UserCreateParameters createParameters;
    private UserUpdateParameters updateParameters;
    private String emailAlias;

    ActiveDirectoryUserImpl(UserInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new UserCreateParameters().withDisplayName(name()).withAccountEnabled(true);
        this.updateParameters = new UserUpdateParameters().withDisplayName(name());
    }

    @Override
    public String userPrincipalName() {
        return innerModel().userPrincipalName();
    }

    @Override
    public String signInName() {
        if (innerModel().signInNames() != null && !innerModel().signInNames().isEmpty()) {
            return innerModel().signInNames().get(0).value();
        } else {
            return null;
        }
    }

    @Override
    public String mail() {
        return innerModel().mail();
    }

    @Override
    public String mailNickname() {
        return innerModel().mailNickname();
    }

    @Override
    public CountryIsoCode usageLocation() {
        return CountryIsoCode.fromString(innerModel().usageLocation());
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
        updateParameters.withPasswordProfile(new PasswordProfile().withPassword(password));
        return this;
    }

    @Override
    protected Mono<UserInner> getInnerAsync() {
        return manager.serviceClient().getUsers().getAsync(this.id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<ActiveDirectoryUser> createResourceAsync() {
        Mono<ActiveDirectoryUserImpl> domain = null;
        if (emailAlias != null) {
            domain =
                manager()
                    .serviceClient()
                    .getDomains()
                    .listAsync(null)
                    .map(
                        domainInner -> {
                            if (domainInner.isVerified() && domainInner.isDefault()) {
                                if (emailAlias != null) {
                                    withUserPrincipalName(emailAlias + "@" + domainInner.name());
                                }
                            }
                            return Mono.just(ActiveDirectoryUserImpl.this);
                        })
                    .blockLast();
        }
        if (domain == null) {
            domain = Mono.just(this);
        }
        return domain
            .flatMap(activeDirectoryUser -> manager().serviceClient().getUsers().createAsync(createParameters))
            .map(innerToFluentMap(this));
    }

    public Mono<ActiveDirectoryUser> updateResourceAsync() {
        return manager()
            .serviceClient()
            .getUsers()
            .updateAsync(id(), updateParameters)
            .then(ActiveDirectoryUserImpl.this.refreshAsync());
    }

    private void withMailNickname(String mailNickname) {
        createParameters.withMailNickname(mailNickname);
        updateParameters.withMailNickname(mailNickname);
    }

    @Override
    public ActiveDirectoryUserImpl withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin) {
        createParameters.passwordProfile().withForceChangePasswordNextLogin(promptToChangePasswordOnLogin);
        updateParameters.passwordProfile().withForceChangePasswordNextLogin(promptToChangePasswordOnLogin);
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
        return innerModel().objectId();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
