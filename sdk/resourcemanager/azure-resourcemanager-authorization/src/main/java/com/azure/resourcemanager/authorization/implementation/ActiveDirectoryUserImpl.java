// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.models.Get2ItemsItem;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphPasswordProfile;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphUserInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/** Implementation for User and its parent interfaces. */
class ActiveDirectoryUserImpl
    extends CreatableUpdatableImpl<ActiveDirectoryUser, MicrosoftGraphUserInner, ActiveDirectoryUserImpl>
    implements ActiveDirectoryUser, ActiveDirectoryUser.Definition, ActiveDirectoryUser.Update {

    private final AuthorizationManager manager;
    private String emailAlias;

    ActiveDirectoryUserImpl(MicrosoftGraphUserInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
    }

    @Override
    public String userPrincipalName() {
        return innerModel().userPrincipalName();
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
        innerModel().withUserPrincipalName(userPrincipalName);
        if (isInCreateMode()) {
            innerModel().withMailNickname(userPrincipalName.replaceAll("@.+$", ""));
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
        if (innerModel().passwordProfile() == null) {
            innerModel().withPasswordProfile(new MicrosoftGraphPasswordProfile());
        }
        innerModel().passwordProfile().withPassword(password);
        return this;
    }

    @Override
    protected Mono<MicrosoftGraphUserInner> getInnerAsync() {
        return manager.serviceClient().getUsersUsers().getUserAsync(
            id(),
            null,
            Arrays.asList(
                Get2ItemsItem.ID,
                Get2ItemsItem.DISPLAY_NAME,
                Get2ItemsItem.USER_PRINCIPAL_NAME,
                Get2ItemsItem.MAIL,
                Get2ItemsItem.MAIL_NICKNAME,
                Get2ItemsItem.USAGE_LOCATION,
                Get2ItemsItem.ACCOUNT_ENABLED
            ),
            null);
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<ActiveDirectoryUser> createResourceAsync() {
        if (innerModel().accountEnabled() == null) {
            innerModel().withAccountEnabled(true);
        }
        Flux<Object> flux = Flux.empty();
        if (emailAlias != null) {
            flux = manager().serviceClient().getDomainsDomains().listDomainAsync()
                .flatMap(domainInner -> {
                    if (domainInner.isVerified() && domainInner.isDefault()) {
                        withUserPrincipalName(emailAlias + "@" + domainInner.id());
                    }
                    return Mono.empty();
                });
        }
        return flux.then(manager().serviceClient().getUsersUsers().createUserAsync(innerModel()))
            .map(innerToFluentMap(this));
    }

    public Mono<ActiveDirectoryUser> updateResourceAsync() {
        return manager()
            .serviceClient()
            .getUsersUsers()
            .updateUserAsync(id(), innerModel())
            .then(this.refreshAsync());
    }

    @Override
    public ActiveDirectoryUserImpl withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin) {
        if (innerModel().passwordProfile() == null) {
            innerModel().withPasswordProfile(new MicrosoftGraphPasswordProfile());
        }
        innerModel().passwordProfile().withForceChangePasswordNextSignIn(promptToChangePasswordOnLogin);
        return this;
    }

    @Override
    public String toString() {
        return name() + " - " + userPrincipalName();
    }

    @Override
    public ActiveDirectoryUserImpl withAccountEnabled(boolean accountEnabled) {
        innerModel().withAccountEnabled(accountEnabled);
        return this;
    }

    @Override
    public ActiveDirectoryUserImpl withUsageLocation(CountryIsoCode usageLocation) {
        innerModel().withUsageLocation(usageLocation.toString());
        return this;
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
