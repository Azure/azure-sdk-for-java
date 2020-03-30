/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
        super(innerObject.getDisplayName(), innerObject);
        this.manager = manager;
        this.createParameters = new UserCreateParameters().setDisplayName(name()).setAccountEnabled(true);
        this.updateParameters = new UserUpdateParameters().setDisplayName(name());
    }

    @Override
    public String userPrincipalName() {
        return inner().getUserPrincipalName();
    }

    @Override
    public String signInName() {
        if (inner().getSignInNames() != null && !inner().getSignInNames().isEmpty()) {
            return inner().getSignInNames().get(0).getValue();
        } else {
            return null;
        }
    }

    @Override
    public String mail() {
        return inner().getMail();
    }

    @Override
    public String mailNickname() {
        return inner().getMailNickname();
    }

    @Override
    public CountryIsoCode usageLocation() {
        return CountryIsoCode.fromString(inner().getUsageLocation());
    }

    @Override
    public ActiveDirectoryUserImpl withUserPrincipalName(String userPrincipalName) {
        createParameters.setUserPrincipalName(userPrincipalName);
        if (isInCreateMode() || updateParameters.getMailNickname() != null) {
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
        createParameters.setPasswordProfile(new PasswordProfile().setPassword(password));
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
                                withUserPrincipalName(emailAlias + "@" + domainInner.getName());
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
        createParameters.setMailNickname(mailNickname);
        updateParameters.setMailNickname(mailNickname);
    }

    @Override
    public ActiveDirectoryUserImpl withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin) {
        createParameters.getPasswordProfile().setForceChangePasswordNextLogin(promptToChangePasswordOnLogin);
        return this;
    }

    @Override
    public String toString() {
        return name() + " - " + userPrincipalName();
    }

    @Override
    public ActiveDirectoryUserImpl withAccountEnabled(boolean accountEnabled) {
        createParameters.setAccountEnabled(accountEnabled);
        updateParameters.setAccountEnabled(accountEnabled);
        return this;
    }

    @Override
    public ActiveDirectoryUserImpl withUsageLocation(CountryIsoCode usageLocation) {
        createParameters.setUsageLocation(usageLocation.toString());
        updateParameters.setUsageLocation(usageLocation.toString());
        return this;
    }

    @Override
    public String id() {
        return inner().getObjectId();
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }
}
