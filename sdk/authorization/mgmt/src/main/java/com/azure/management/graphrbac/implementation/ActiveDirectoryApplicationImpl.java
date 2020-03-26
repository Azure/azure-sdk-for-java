/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.management.graphrbac.ActiveDirectoryApplication;
import com.azure.management.graphrbac.ApplicationCreateParameters;
import com.azure.management.graphrbac.ApplicationUpdateParameters;
import com.azure.management.graphrbac.CertificateCredential;
import com.azure.management.graphrbac.PasswordCredential;
import com.azure.management.graphrbac.models.ApplicationInner;
import com.azure.management.graphrbac.models.KeyCredentialInner;
import com.azure.management.graphrbac.models.PasswordCredentialInner;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
class ActiveDirectoryApplicationImpl
        extends CreatableUpdatableImpl<ActiveDirectoryApplication, ApplicationInner, ActiveDirectoryApplicationImpl>
        implements
            ActiveDirectoryApplication,
            ActiveDirectoryApplication.Definition,
            ActiveDirectoryApplication.Update,
            HasCredential<ActiveDirectoryApplicationImpl> {
    private GraphRbacManager manager;
    private ApplicationCreateParameters createParameters;
    private ApplicationUpdateParameters updateParameters;
    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;

    ActiveDirectoryApplicationImpl(ApplicationInner innerObject, GraphRbacManager manager) {
        super(innerObject.getDisplayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ApplicationCreateParameters().setDisplayName(innerObject.getDisplayName());
        this.updateParameters = new ApplicationUpdateParameters().setDisplayName(innerObject.getDisplayName());
    }

    @Override
    public boolean isInCreateMode() {
        return this.id() == null;
    }

    @Override
    public Mono<ActiveDirectoryApplication> createResourceAsync() {
        if (createParameters.getIdentifierUris() == null) {
            createParameters.setIdentifierUris(new ArrayList<String>());
            createParameters.getIdentifierUris().add(createParameters.getHomepage());
        }
        return manager.inner().applications().createAsync(createParameters)
                .map(innerToFluentMap(this))
                .flatMap(application -> refreshCredentialsAsync());
    }

    @Override
    public Mono<ActiveDirectoryApplication> updateResourceAsync() {
        return manager.inner().applications().patchAsync(id(), updateParameters).then(Mono.just(this));
    }

    Mono<ActiveDirectoryApplication> refreshCredentialsAsync() {
        final Mono<ActiveDirectoryApplication> keyCredentials = manager.inner().applications().listKeyCredentialsAsync(id())
                .map((Function<KeyCredentialInner, CertificateCredential>) keyCredentialInner -> new CertificateCredentialImpl<ActiveDirectoryApplication>(keyCredentialInner))
                .collectMap(certificateCredential -> certificateCredential.name())
                .map(stringCertificateCredentialMap -> {
                    ActiveDirectoryApplicationImpl.this.cachedCertificateCredentials = stringCertificateCredentialMap;
                    return ActiveDirectoryApplicationImpl.this;
                });

        final Mono<ActiveDirectoryApplication> passwordCredentials = manager.inner().applications().listPasswordCredentialsAsync(id())
                .map((Function<PasswordCredentialInner, PasswordCredential>) passwordCredentialInner -> new PasswordCredentialImpl<ActiveDirectoryApplication>(passwordCredentialInner))
                .collectMap(passwordCredential -> passwordCredential.name())
                .map(stringPasswordCredentialMap -> {
                    ActiveDirectoryApplicationImpl.this.cachedPasswordCredentials = stringPasswordCredentialMap;
                    return ActiveDirectoryApplicationImpl.this;
                });

        return keyCredentials.mergeWith(passwordCredentials).last();
    }

    @Override
    public Mono<ActiveDirectoryApplication> refreshAsync() {
        return getInnerAsync()
                .map(innerToFluentMap(this))
                .flatMap(application -> refreshCredentialsAsync());
    }

    @Override
    public String applicationId() {
        return inner().getAppId();
    }

    @Override
    public List<String> applicationPermissions() {
        if (inner().getAppPermissions() == null) {
            return null;
        }
        return Collections.unmodifiableList(inner().getAppPermissions());
    }

    @Override
    public boolean availableToOtherTenants() {
        return inner().isAvailableToOtherTenants();
    }

    @Override
    public Set<String> identifierUris() {
        if (inner().getIdentifierUris() == null) {
            return null;
        }
        return Collections.unmodifiableSet(new HashSet(inner().getIdentifierUris()));
    }

    @Override
    public Set<String> replyUrls() {
        if (inner().getReplyUrls() == null) {
            return null;
        }
        return Collections.unmodifiableSet(new HashSet(inner().getReplyUrls()));
    }

    @Override
    public URL signOnUrl() {
        try {
            return new URL(inner().getHomepage());
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
        return manager.inner().applications().getAsync(id());
    }

    @Override
    public ActiveDirectoryApplicationImpl withSignOnUrl(String signOnUrl) {
        if (isInCreateMode()) {
            createParameters.setHomepage(signOnUrl);
        } else {
            updateParameters.setHomepage(signOnUrl);
        }
        return withReplyUrl(signOnUrl);
    }

    @Override
    public ActiveDirectoryApplicationImpl withReplyUrl(String replyUrl) {
        if (isInCreateMode()) {
            if (createParameters.getReplyUrls() == null) {
                createParameters.setReplyUrls(new ArrayList<>());
            }
            createParameters.getReplyUrls().add(replyUrl);
        } else {
            if (updateParameters.getReplyUrls() == null) {
                updateParameters.setReplyUrls(new ArrayList<>(replyUrls()));
            }
            updateParameters.getReplyUrls().add(replyUrl);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withoutReplyUrl(String replyUrl) {
        if (updateParameters.getReplyUrls() != null) {
            updateParameters.getReplyUrls().remove(replyUrl);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withIdentifierUrl(String identifierUrl) {
        if (isInCreateMode()) {
            if (createParameters.getIdentifierUris() == null) {
                createParameters.setIdentifierUris(new ArrayList<>());
            }
            createParameters.getIdentifierUris().add(identifierUrl);
        } else {
            if (updateParameters.getIdentifierUris() == null) {
                updateParameters.setIdentifierUris(new ArrayList<>(identifierUris()));
            }
            updateParameters.getIdentifierUris().add(identifierUrl);
        }
        return this;
    }

    @Override
    public Update withoutIdentifierUrl(String identifierUrl) {
        if (updateParameters.getIdentifierUris() != null) {
            updateParameters.getIdentifierUris().remove(identifierUrl);
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
            for (PasswordCredential passwordCredential: cachedPasswordCredentials.values()) {
                updatePasswordCredentials.add(passwordCredential.inner());
            }
            updateParameters.setPasswordCredentials(updatePasswordCredentials);
        } else if (cachedCertificateCredentials.containsKey(name)) {
            cachedCertificateCredentials.remove(name);
            List<KeyCredentialInner> updateCertificateCredentials = new ArrayList<>();
            for (CertificateCredential certificateCredential: cachedCertificateCredentials.values()) {
                updateCertificateCredentials.add(certificateCredential.inner());
            }
            updateParameters.setKeyCredentials(updateCertificateCredentials);
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.getKeyCredentials() == null) {
                createParameters.setKeyCredentials(new ArrayList<>());
            }
            createParameters.getKeyCredentials().add(credential.inner());
        } else {
            if (updateParameters.getKeyCredentials() == null) {
                updateParameters.setKeyCredentials(new ArrayList<>());
            }
            updateParameters.getKeyCredentials().add(credential.inner());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.getPasswordCredentials() == null) {
                createParameters.setPasswordCredentials(new ArrayList<>());
            }
            createParameters.getPasswordCredentials().add(credential.inner());
        } else {
            if (updateParameters.getPasswordCredentials() == null) {
                updateParameters.setPasswordCredentials(new ArrayList<>());
            }
            updateParameters.getPasswordCredentials().add(credential.inner());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withAvailableToOtherTenants(boolean availableToOtherTenants) {
        if (isInCreateMode()) {
            createParameters.setAvailableToOtherTenants(availableToOtherTenants);
        } else {
            updateParameters.setAvailableToOtherTenants(availableToOtherTenants);
        }
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
