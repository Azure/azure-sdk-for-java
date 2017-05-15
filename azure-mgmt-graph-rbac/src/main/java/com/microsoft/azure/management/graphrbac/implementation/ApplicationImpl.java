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
import com.microsoft.azure.management.graphrbac.Application;
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
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ApplicationImpl
        extends CreatableUpdatableImpl<Application, ApplicationInner, ApplicationImpl>
        implements
            Application,
            Application.Definition,
            Application.Update,
            HasCredential<ApplicationImpl> {
    private GraphRbacManager manager;
    private ApplicationCreateParametersInner createParameters;
    private ApplicationUpdateParametersInner updateParameters;
    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;

    ApplicationImpl(ApplicationInner innerObject, GraphRbacManager manager) {
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
    public Observable<Application> createResourceAsync() {
        if (createParameters.identifierUris() == null) {
            createParameters.withIdentifierUris(new ArrayList<String>());
            createParameters.identifierUris().add(createParameters.homepage());
        }
        return manager.inner().applications().createAsync(createParameters)
                .map(innerToFluentMap(this))
                .flatMap(new Func1<Application, Observable<Application>>() {
                    @Override
                    public Observable<Application> call(Application application) {
                        return refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public Observable<Application> updateResourceAsync() {
        return manager.inner().applications().patchAsync(id(), updateParameters)
                .flatMap(new Func1<Void, Observable<Application>>() {
                    @Override
                    public Observable<Application> call(Void aVoid) {
                        return refreshAsync();
                    }
                });
    }

    Observable<Application> refreshCredentialsAsync() {
        final Observable<Application> keyCredentials = manager.inner().applications().listKeyCredentialsAsync(id())
                .flatMapIterable(new Func1<List<KeyCredentialInner>, Iterable<KeyCredentialInner>>() {
                    @Override
                    public Iterable<KeyCredentialInner> call(List<KeyCredentialInner> keyCredentialInners) {
                        return keyCredentialInners;
                    }
                })
                .map(new Func1<KeyCredentialInner, CertificateCredential>() {
                    @Override
                    public CertificateCredential call(KeyCredentialInner keyCredentialInner) {
                        return new CertificateCredentialImpl<Application>(keyCredentialInner);
                    }
                })
                .toMap(new Func1<CertificateCredential, String>() {
                    @Override
                    public String call(CertificateCredential certificateCredential) {
                        return certificateCredential.name();
                    }
                }).map(new Func1<Map<String, CertificateCredential>, Application>() {
                    @Override
                    public Application call(Map<String, CertificateCredential> stringCertificateCredentialMap) {
                        ApplicationImpl.this.cachedCertificateCredentials = stringCertificateCredentialMap;
                        return ApplicationImpl.this;
                    }
                });
        final Observable<Application> passwordCredentials = manager.inner().applications().listPasswordCredentialsAsync(id())
                .flatMapIterable(new Func1<List<PasswordCredentialInner>, Iterable<PasswordCredentialInner>>() {
                    @Override
                    public Iterable<PasswordCredentialInner> call(List<PasswordCredentialInner> passwordCredentialInners) {
                        return passwordCredentialInners;
                    }
                })
                .map(new Func1<PasswordCredentialInner, PasswordCredential>() {
                    @Override
                    public PasswordCredential call(PasswordCredentialInner passwordCredentialInner) {
                        return new PasswordCredentialImpl<Application>(passwordCredentialInner);
                    }
                })
                .toMap(new Func1<PasswordCredential, String>() {
                    @Override
                    public String call(PasswordCredential passwordCredential) {
                        return passwordCredential.name();
                    }
                }).map(new Func1<Map<String, PasswordCredential>, Application>() {
                    @Override
                    public Application call(Map<String, PasswordCredential> stringPasswordCredentialMap) {
                        ApplicationImpl.this.cachedPasswordCredentials = stringPasswordCredentialMap;
                        return ApplicationImpl.this;
                    }
                });
        return keyCredentials.mergeWith(passwordCredentials).last();
    }

    @Override
    public Observable<Application> refreshAsync() {
        return getInnerAsync()
                .map(innerToFluentMap(this))
                .flatMap(new Func1<Application, Observable<Application>>() {
                    @Override
                    public Observable<Application> call(Application application) {
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
    public ApplicationImpl withSignOnUrl(String signOnUrl) {
        if (isInCreateMode()) {
            createParameters.withHomepage(signOnUrl);
        } else {
            updateParameters.withHomepage(signOnUrl);
        }
        return withReplyUrl(signOnUrl);
    }

    @Override
    public ApplicationImpl withReplyUrl(String replyUrl) {
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
    public ApplicationImpl withoutReplyUrl(String replyUrl) {
        if (updateParameters.replyUrls() != null) {
            updateParameters.replyUrls().remove(replyUrl);
        }
        return this;
    }

    @Override
    public ApplicationImpl withIdentifierUrl(String identifierUrl) {
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
    public ApplicationImpl withoutCredential(final String name) {
        if (cachedPasswordCredentials.containsKey(name)) {
            cachedPasswordCredentials.remove(name);
            if (updateParameters.passwordCredentials() == null) {
                updateParameters.withPasswordCredentials(Lists.transform(
                        new ArrayList<>(cachedPasswordCredentials.values()),
                        new Function<PasswordCredential, PasswordCredentialInner>() {
                            @Override
                            public PasswordCredentialInner apply(PasswordCredential input) {
                                return input.inner();
                            }
                        }));
            }
        } else if (cachedCertificateCredentials.containsKey(name)) {
            cachedCertificateCredentials.remove(name);
            if (updateParameters.keyCredentials() == null) {
                updateParameters.withKeyCredentials(Lists.transform(
                        new ArrayList<>(cachedCertificateCredentials.values()),
                        new Function<CertificateCredential, KeyCredentialInner>() {
                            @Override
                            public KeyCredentialInner apply(CertificateCredential input) {
                                return input.inner();
                            }
                        }));
            }
        }
        return this;
    }

    @Override
    public ApplicationImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
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
    public ApplicationImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
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
    public ApplicationImpl withAvailableToOtherTenants(boolean availableToOtherTenants) {
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
