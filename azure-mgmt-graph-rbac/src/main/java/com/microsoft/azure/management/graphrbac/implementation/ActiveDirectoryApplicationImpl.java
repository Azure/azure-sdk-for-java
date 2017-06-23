/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.CertificateCredential;
import com.microsoft.azure.management.graphrbac.PasswordCredential;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryApplicationImpl
        extends CreatableUpdatableImpl<ActiveDirectoryApplication, ApplicationInner, ActiveDirectoryApplicationImpl>
        implements
            ActiveDirectoryApplication,
            ActiveDirectoryApplication.Definition,
            ActiveDirectoryApplication.Update,
            HasCredential<ActiveDirectoryApplicationImpl> {
    private GraphRbacManager manager;
    private ApplicationCreateParametersInner createParameters;
    private ApplicationUpdateParametersInner updateParameters;
    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;

    ActiveDirectoryApplicationImpl(ApplicationInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ApplicationCreateParametersInner().withDisplayName(innerObject.displayName());
        this.updateParameters = new ApplicationUpdateParametersInner().withDisplayName(innerObject.displayName());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Observable<ActiveDirectoryApplication> createResourceAsync() {
        if (createParameters.identifierUris() == null) {
            createParameters.withIdentifierUris(new ArrayList<String>());
            createParameters.identifierUris().add(createParameters.homepage());
        }
        return manager.inner().applications().createAsync(createParameters)
                .map(innerToFluentMap(this))
                .flatMap(new Func1<ActiveDirectoryApplication, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(ActiveDirectoryApplication application) {
                        return refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public Observable<ActiveDirectoryApplication> updateResourceAsync() {
        return manager.inner().applications().patchAsync(id(), updateParameters)
                .flatMap(new Func1<Void, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(Void aVoid) {
                        return refreshAsync();
                    }
                });
    }

    Observable<ActiveDirectoryApplication> refreshCredentialsAsync() {
        final Observable<ActiveDirectoryApplication> keyCredentials = manager.inner().applications().listKeyCredentialsAsync(id())
                .flatMapIterable(new Func1<List<KeyCredentialInner>, Iterable<KeyCredentialInner>>() {
                    @Override
                    public Iterable<KeyCredentialInner> call(List<KeyCredentialInner> keyCredentialInners) {
                        return keyCredentialInners;
                    }
                })
                .map(new Func1<KeyCredentialInner, CertificateCredential>() {
                    @Override
                    public CertificateCredential call(KeyCredentialInner keyCredentialInner) {
                        return new CertificateCredentialImpl<ActiveDirectoryApplication>(keyCredentialInner);
                    }
                })
                .toMap(new Func1<CertificateCredential, String>() {
                    @Override
                    public String call(CertificateCredential certificateCredential) {
                        return certificateCredential.name();
                    }
                }).map(new Func1<Map<String, CertificateCredential>, ActiveDirectoryApplication>() {
                    @Override
                    public ActiveDirectoryApplication call(Map<String, CertificateCredential> stringCertificateCredentialMap) {
                        ActiveDirectoryApplicationImpl.this.cachedCertificateCredentials = stringCertificateCredentialMap;
                        return ActiveDirectoryApplicationImpl.this;
                    }
                });
        final Observable<ActiveDirectoryApplication> passwordCredentials = manager.inner().applications().listPasswordCredentialsAsync(id())
                .flatMapIterable(new Func1<List<PasswordCredentialInner>, Iterable<PasswordCredentialInner>>() {
                    @Override
                    public Iterable<PasswordCredentialInner> call(List<PasswordCredentialInner> passwordCredentialInners) {
                        return passwordCredentialInners;
                    }
                })
                .map(new Func1<PasswordCredentialInner, PasswordCredential>() {
                    @Override
                    public PasswordCredential call(PasswordCredentialInner passwordCredentialInner) {
                        return new PasswordCredentialImpl<ActiveDirectoryApplication>(passwordCredentialInner);
                    }
                })
                .toMap(new Func1<PasswordCredential, String>() {
                    @Override
                    public String call(PasswordCredential passwordCredential) {
                        return passwordCredential.name();
                    }
                }).map(new Func1<Map<String, PasswordCredential>, ActiveDirectoryApplication>() {
                    @Override
                    public ActiveDirectoryApplication call(Map<String, PasswordCredential> stringPasswordCredentialMap) {
                        ActiveDirectoryApplicationImpl.this.cachedPasswordCredentials = stringPasswordCredentialMap;
                        return ActiveDirectoryApplicationImpl.this;
                    }
                });
        return keyCredentials.mergeWith(passwordCredentials).last();
    }

    @Override
    public Observable<ActiveDirectoryApplication> refreshAsync() {
        return getInnerAsync()
                .map(innerToFluentMap(this))
                .flatMap(new Func1<ActiveDirectoryApplication, Observable<ActiveDirectoryApplication>>() {
                    @Override
                    public Observable<ActiveDirectoryApplication> call(ActiveDirectoryApplication application) {
                        return refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public String id() {
        return inner().objectId();
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
        return Collections.unmodifiableSet(Sets.newHashSet(inner().identifierUris()));
    }

    @Override
    public Set<String> replyUrls() {
        if (inner().replyUrls() == null) {
            return null;
        }
        return Collections.unmodifiableSet(Sets.newHashSet(inner().replyUrls()));
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
    protected Observable<ApplicationInner> getInnerAsync() {
        return manager.inner().applications().getAsync(id());
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
                createParameters.withReplyUrls(new ArrayList<String>());
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
                createParameters.withIdentifierUris(new ArrayList<String>());
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
            updateParameters.withPasswordCredentials(Lists.transform(
                    new ArrayList<>(cachedPasswordCredentials.values()),
                    new Function<PasswordCredential, PasswordCredentialInner>() {
                        @Override
                        public PasswordCredentialInner apply(PasswordCredential input) {
                            return input.inner();
                        }
                    }));
        } else if (cachedCertificateCredentials.containsKey(name)) {
            cachedCertificateCredentials.remove(name);
            updateParameters.withKeyCredentials(Lists.transform(
                    new ArrayList<>(cachedCertificateCredentials.values()),
                    new Function<CertificateCredential, KeyCredentialInner>() {
                        @Override
                        public KeyCredentialInner apply(CertificateCredential input) {
                            return input.inner();
                        }
                    }));
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.keyCredentials() == null) {
                createParameters.withKeyCredentials(new ArrayList<KeyCredentialInner>());
            }
            createParameters.keyCredentials().add(credential.inner());
        } else {
            if (updateParameters.keyCredentials() == null) {
                updateParameters.withKeyCredentials(new ArrayList<KeyCredentialInner>());
            }
            updateParameters.keyCredentials().add(credential.inner());
        }
        return this;
    }

    @Override
    public ActiveDirectoryApplicationImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        if (isInCreateMode()) {
            if (createParameters.passwordCredentials() == null) {
                createParameters.withPasswordCredentials(new ArrayList<PasswordCredentialInner>());
            }
            createParameters.passwordCredentials().add(credential.inner());
        } else {
            if (updateParameters.passwordCredentials() == null) {
                updateParameters.withPasswordCredentials(new ArrayList<PasswordCredentialInner>());
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
    public GraphRbacManager manager() {
        return manager;
    }
}
