// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.models.ApplicationsAddPasswordRequestBodyInner;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphApplicationInner;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphWebApplication;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ApplicationAccountType;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Implementation for ServicePrincipal and its parent interfaces. */
class ActiveDirectoryApplicationImpl
    extends CreatableUpdatableImpl<
        ActiveDirectoryApplication,
        MicrosoftGraphApplicationInner,
        ActiveDirectoryApplicationImpl>
    implements ActiveDirectoryApplication,
        ActiveDirectoryApplication.Definition,
        ActiveDirectoryApplication.Update,
        HasCredential<ActiveDirectoryApplicationImpl> {
    private AuthorizationManager manager;
    private final Map<String, PasswordCredential> cachedPasswordCredentials;
    private final Map<String, CertificateCredential> cachedCertificateCredentials;

    private final List<PasswordCredentialImpl<?>> passwordCredentialToCreate;
    private final List<CertificateCredentialImpl<?>> certificateCredentialToCreate;

    ActiveDirectoryApplicationImpl(MicrosoftGraphApplicationInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.cachedPasswordCredentials = new HashMap<>();
        this.cachedCertificateCredentials = new HashMap<>();
        this.passwordCredentialToCreate = new ArrayList<>();
        this.certificateCredentialToCreate = new ArrayList<>();
        refreshCredentials(innerObject);
    }

    @Override
    public boolean isInCreateMode() {
        return this.id() == null;
    }

    @Override
    public Mono<ActiveDirectoryApplication> createResourceAsync() {
        return manager
            .serviceClient()
            .getApplicationsApplications()
            .createApplicationAsync(innerModel())
            .map(innerToFluentMap(this))
            .flatMap(app -> submitCredentialAsync().doOnComplete(this::postRequest).then(refreshAsync()));
    }

    @Override
    public Mono<ActiveDirectoryApplication> updateResourceAsync() {
        return manager.serviceClient().getApplicationsApplications().updateApplicationAsync(id(), innerModel())
            .then(submitCredentialAsync().doOnComplete(this::postRequest).then(refreshAsync()));
    }

    void refreshCredentials(MicrosoftGraphApplicationInner inner) {
        cachedCertificateCredentials.clear();
        cachedPasswordCredentials.clear();

        if (inner.keyCredentials() != null) {
            inner.keyCredentials().forEach(keyCredentialInner -> {
                CertificateCredential certificateCredential = new CertificateCredentialImpl<>(keyCredentialInner);
                cachedCertificateCredentials.put(certificateCredential.name(), certificateCredential);
            });
        }

        if (inner.passwordCredentials() != null) {
            inner.passwordCredentials().forEach(passwordCredentialInner -> {
                PasswordCredential passwordCredential = new PasswordCredentialImpl<>(passwordCredentialInner);
                cachedPasswordCredentials.put(passwordCredential.name(), passwordCredential);
            });
        }
    }

    Flux<?> submitCredentialAsync() {
        return Flux.defer(() ->
                Flux.fromIterable(passwordCredentialToCreate)
                .flatMap(passwordCredential -> manager().serviceClient().getApplications()
                    .addPasswordAsync(id(), new ApplicationsAddPasswordRequestBodyInner()
                        .withPasswordCredential(passwordCredential.innerModel()))
                    .doOnNext(passwordCredential::setInner)
                )
            );
    }

    void postRequest() {
        passwordCredentialToCreate.forEach(passwordCredential -> passwordCredential.exportAuthFile(this));
        passwordCredentialToCreate.forEach(PasswordCredentialImpl::consumeSecret);
        passwordCredentialToCreate.clear();
        certificateCredentialToCreate.forEach(certificateCredential -> certificateCredential.exportAuthFile(this));
        certificateCredentialToCreate.clear();
    }

    @Override
    public Mono<ActiveDirectoryApplication> refreshAsync() {
        return getInnerAsync().map(innerToFluentMap(this));
    }

    @Override
    public String applicationId() {
        return innerModel().appId();
    }

    @Override
    public boolean availableToOtherTenants() {
        return accountType() != ApplicationAccountType.AZURE_AD_MY_ORG;
    }

    @Override
    public ApplicationAccountType accountType() {
        return ApplicationAccountType.fromString(innerModel().signInAudience());
    }

    @Override
    public Set<String> identifierUris() {
        if (innerModel().identifierUris() == null) {
            return null;
        }
        return new HashSet<>(innerModel().identifierUris());
    }

    @Override
    public Set<String> replyUrls() {
        if (innerModel().web() == null || innerModel().web().redirectUris() == null) {
            return null;
        }
        return new HashSet<>(innerModel().web().redirectUris());
    }

    @Override
    public URL signOnUrl() {
        try {
            return new URL(innerModel().web().homePageUrl());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public Map<String, PasswordCredential> passwordCredentials() {
        if (cachedPasswordCredentials == null) {
            return null;
        }
        return Collections.unmodifiableMap(cachedPasswordCredentials);
    }

    @Override
    public Map<String, CertificateCredential> certificateCredentials() {
        if (cachedCertificateCredentials == null) {
            return null;
        }
        return Collections.unmodifiableMap(cachedCertificateCredentials);
    }

    @Override
    protected Mono<MicrosoftGraphApplicationInner> getInnerAsync() {
        return manager.serviceClient().getApplicationsApplications().getApplicationAsync(id())
            .doOnSuccess(this::refreshCredentials);
    }

    @Override
    public ActiveDirectoryApplicationImpl withSignOnUrl(String signOnUrl) {
        if (innerModel().web() == null) {
            innerModel().withWeb(new MicrosoftGraphWebApplication());
        }
        innerModel().web().withHomePageUrl(signOnUrl);
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withReplyUrl(String replyUrl) {
        if (innerModel().web() == null) {
            innerModel().withWeb(new MicrosoftGraphWebApplication());
        }
        if (innerModel().web().redirectUris() == null) {
            innerModel().web().withRedirectUris(new ArrayList<>());
        }
        innerModel().web().redirectUris().add(replyUrl);
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withoutReplyUrl(String replyUrl) {
        if (innerModel().web() != null && innerModel().web().redirectUris() != null) {
            innerModel().web().redirectUris().remove(replyUrl);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withIdentifierUrl(String identifierUrl) {
        if (innerModel().identifierUris() == null) {
            innerModel().withIdentifierUris(new ArrayList<>());
        }
        innerModel().identifierUris().add(identifierUrl);
        return this;
    }

    @Override
    public Update withoutIdentifierUrl(String identifierUrl) {
        if (innerModel().identifierUris() != null) {
            innerModel().identifierUris().remove(identifierUrl);
        }
        return this;
    }

    @Override
    public CertificateCredentialImpl<ActiveDirectoryApplicationImpl> defineCertificateCredential(String name) {
        return new CertificateCredentialImpl<>(name, this);
    }

    @Override
    public PasswordCredentialImpl<ActiveDirectoryApplicationImpl> definePasswordCredential(String name) {
        return new PasswordCredentialImpl<>(name, this);
    }

    @Override
    public ActiveDirectoryApplicationImpl withoutCredential(final String name) {
        if (cachedPasswordCredentials.containsKey(name) && innerModel().passwordCredentials() != null) {
            innerModel().passwordCredentials().remove(cachedPasswordCredentials.get(name).innerModel());
        } else if (cachedCertificateCredentials.containsKey(name) && innerModel().keyCredentials() != null) {
            innerModel().keyCredentials().remove(cachedCertificateCredentials.get(name).innerModel());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        this.certificateCredentialToCreate.add(credential);
        if (innerModel().keyCredentials() == null) {
            innerModel().withKeyCredentials(new ArrayList<>());
        }
        innerModel().keyCredentials().add(credential.innerModel());
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        this.passwordCredentialToCreate.add(credential);
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withAvailableToOtherTenants(boolean availableToOtherTenants) {
        return availableToOtherTenants
            ? withAccountType(ApplicationAccountType.AZURE_AD_MULTIPLE_ORGS)
            : withAccountType(ApplicationAccountType.AZURE_AD_MY_ORG);
    }

    @Override
    public ActiveDirectoryApplicationImpl withAccountType(ApplicationAccountType accountType) {
        return withAccountType(accountType.toString());
    }

    @Override
    public ActiveDirectoryApplicationImpl withAccountType(String accountType) {
        innerModel().withSignInAudience(accountType);
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
