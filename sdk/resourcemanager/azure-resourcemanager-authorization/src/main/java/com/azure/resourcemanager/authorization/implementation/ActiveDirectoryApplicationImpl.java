// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.ApplicationCreateParameters;
import com.azure.resourcemanager.authorization.models.ApplicationUpdateParameters;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.authorization.fluent.inner.ApplicationInner;
import com.azure.resourcemanager.authorization.fluent.inner.KeyCredentialInner;
import com.azure.resourcemanager.authorization.fluent.inner.PasswordCredentialInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/** Implementation for ServicePrincipal and its parent interfaces. */
class ActiveDirectoryApplicationImpl
    extends CreatableUpdatableImpl<ActiveDirectoryApplication, ApplicationInner, ActiveDirectoryApplicationImpl>
    implements ActiveDirectoryApplication,
        ActiveDirectoryApplication.Definition,
        ActiveDirectoryApplication.Update,
        HasCredential<ActiveDirectoryApplicationImpl> {
    private AuthorizationManager manager;
    private ApplicationCreateParameters createParameters;
    private ApplicationUpdateParameters updateParameters;
    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;

    ActiveDirectoryApplicationImpl(ApplicationInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ApplicationCreateParameters().withDisplayName(innerObject.displayName());
        this.updateParameters = new ApplicationUpdateParameters().withDisplayName(innerObject.displayName());
    }

    @Override
    public boolean isInCreateMode() {
        return this.id() == null;
    }

    @Override
    public Mono<ActiveDirectoryApplication> createResourceAsync() {
        if (createParameters.identifierUris() == null) {
            createParameters.withIdentifierUris(new ArrayList<String>());
            createParameters.identifierUris().add(createParameters.homepage());
        }
        return manager
            .inner()
            .getApplications()
            .createAsync(createParameters)
            .map(innerToFluentMap(this))
            .flatMap(application -> refreshCredentialsAsync());
    }

    @Override
    public Mono<ActiveDirectoryApplication> updateResourceAsync() {
        return manager.inner().getApplications().patchAsync(id(), updateParameters).then(Mono.just(this));
    }

    Mono<ActiveDirectoryApplication> refreshCredentialsAsync() {
        final Mono<ActiveDirectoryApplication> keyCredentials =
            manager
                .inner()
                .getApplications()
                .listKeyCredentialsAsync(id())
                .map(
                    (Function<KeyCredentialInner, CertificateCredential>)
                    keyCredentialInner ->
                        new CertificateCredentialImpl<ActiveDirectoryApplication>(keyCredentialInner))
                .collectMap(certificateCredential -> certificateCredential.name())
                .map(
                    stringCertificateCredentialMap -> {
                        ActiveDirectoryApplicationImpl.this.cachedCertificateCredentials =
                            stringCertificateCredentialMap;
                        return ActiveDirectoryApplicationImpl.this;
                    });

        final Mono<ActiveDirectoryApplication> passwordCredentials =
            manager
                .inner()
                .getApplications()
                .listPasswordCredentialsAsync(id())
                .map(
                    (Function<PasswordCredentialInner, PasswordCredential>)
                    passwordCredentialInner ->
                        new PasswordCredentialImpl<ActiveDirectoryApplication>(passwordCredentialInner))
                .collectMap(passwordCredential -> passwordCredential.name())
                .map(
                    stringPasswordCredentialMap -> {
                        ActiveDirectoryApplicationImpl.this.cachedPasswordCredentials = stringPasswordCredentialMap;
                        return ActiveDirectoryApplicationImpl.this;
                    });

        return keyCredentials.mergeWith(passwordCredentials).last();
    }

    @Override
    public Mono<ActiveDirectoryApplication> refreshAsync() {
        return getInnerAsync().map(innerToFluentMap(this)).flatMap(application -> refreshCredentialsAsync());
    }

    @Override
    public String applicationId() {
        return inner().appId();
    }

    @Override
    public List<String> applicationPermissions() {
        if (inner().appPermissions() == null) {
            return null;
        }
        return Collections.unmodifiableList(inner().appPermissions());
    }

    @Override
    public boolean availableToOtherTenants() {
        return inner().availableToOtherTenants();
    }

    @Override
    public Set<String> identifierUris() {
        if (inner().identifierUris() == null) {
            return null;
        }
        return Collections.unmodifiableSet(new HashSet(inner().identifierUris()));
    }

    @Override
    public Set<String> replyUrls() {
        if (inner().replyUrls() == null) {
            return null;
        }
        return Collections.unmodifiableSet(new HashSet(inner().replyUrls()));
    }

    @Override
    public URL signOnUrl() {
        try {
            return new URL(inner().homepage());
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
    protected Mono<ApplicationInner> getInnerAsync() {
        return manager.inner().getApplications().getAsync(id());
    }

    @Override
    public ActiveDirectoryApplicationImpl withSignOnUrl(String signOnUrl) {
        if (isInCreateMode()) {
            createParameters.withHomepage(signOnUrl);
        } else {
            updateParameters.withHomepage(signOnUrl);
        }
        return withReplyUrl(signOnUrl);
    }

    @Override
    public ActiveDirectoryApplicationImpl withReplyUrl(String replyUrl) {
        if (isInCreateMode()) {
            if (createParameters.replyUrls() == null) {
                createParameters.withReplyUrls(new ArrayList<>());
            }
            createParameters.replyUrls().add(replyUrl);
        } else {
            if (updateParameters.replyUrls() == null) {
                updateParameters.withReplyUrls(new ArrayList<>(replyUrls()));
            }
            updateParameters.replyUrls().add(replyUrl);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withoutReplyUrl(String replyUrl) {
        if (updateParameters.replyUrls() != null) {
            updateParameters.replyUrls().remove(replyUrl);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withIdentifierUrl(String identifierUrl) {
        if (isInCreateMode()) {
            if (createParameters.identifierUris() == null) {
                createParameters.withIdentifierUris(new ArrayList<>());
            }
            createParameters.identifierUris().add(identifierUrl);
        } else {
            if (updateParameters.identifierUris() == null) {
                updateParameters.withIdentifierUris(new ArrayList<>(identifierUris()));
            }
            updateParameters.identifierUris().add(identifierUrl);
        }
        return this;
    }

    @Override
    public Update withoutIdentifierUrl(String identifierUrl) {
        if (updateParameters.identifierUris() != null) {
            updateParameters.identifierUris().remove(identifierUrl);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CertificateCredentialImpl defineCertificateCredential(String name) {
        return new CertificateCredentialImpl<>(name, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PasswordCredentialImpl definePasswordCredential(String name) {
        return new PasswordCredentialImpl<>(name, this);
    }

    @Override
    public ActiveDirectoryApplicationImpl withoutCredential(final String name) {
        if (cachedPasswordCredentials.containsKey(name)) {
            cachedPasswordCredentials.remove(name);
            List<PasswordCredentialInner> updatePasswordCredentials = new ArrayList<>();
            for (PasswordCredential passwordCredential : cachedPasswordCredentials.values()) {
                updatePasswordCredentials.add(passwordCredential.inner());
            }
            updateParameters.withPasswordCredentials(updatePasswordCredentials);
        } else if (cachedCertificateCredentials.containsKey(name)) {
            cachedCertificateCredentials.remove(name);
            List<KeyCredentialInner> updateCertificateCredentials = new ArrayList<>();
            for (CertificateCredential certificateCredential : cachedCertificateCredentials.values()) {
                updateCertificateCredentials.add(certificateCredential.inner());
            }
            updateParameters.withKeyCredentials(updateCertificateCredentials);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.keyCredentials() == null) {
                createParameters.withKeyCredentials(new ArrayList<>());
            }
            createParameters.keyCredentials().add(credential.inner());
        } else {
            if (updateParameters.keyCredentials() == null) {
                updateParameters.withKeyCredentials(new ArrayList<>());
            }
            updateParameters.keyCredentials().add(credential.inner());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.passwordCredentials() == null) {
                createParameters.withPasswordCredentials(new ArrayList<>());
            }
            createParameters.passwordCredentials().add(credential.inner());
        } else {
            if (updateParameters.passwordCredentials() == null) {
                updateParameters.withPasswordCredentials(new ArrayList<>());
            }
            updateParameters.passwordCredentials().add(credential.inner());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withAvailableToOtherTenants(boolean availableToOtherTenants) {
        if (isInCreateMode()) {
            createParameters.withAvailableToOtherTenants(availableToOtherTenants);
        } else {
            updateParameters.withAvailableToOtherTenants(availableToOtherTenants);
        }
        return this;
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
