/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Application;
import com.microsoft.azure.management.graphrbac.CertificateCredential;
import com.microsoft.azure.management.graphrbac.PasswordCredential;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ServicePrincipalImpl
        extends CreatableUpdatableImpl<ServicePrincipal, ServicePrincipalInner, ServicePrincipalImpl>
        implements
            ServicePrincipal,
            ServicePrincipal.Definition,
            HasCredential<ServicePrincipalImpl> {
    private GraphRbacManager manager;
    private ServicePrincipalCreateParametersInner createParameters;
    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;
    private Creatable<Application> applicationCreatable;

    ServicePrincipalImpl(ServicePrincipalInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ServicePrincipalCreateParametersInner().withAccountEnabled(true);
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public String objectType() {
        return inner().objectType();
    }

    @Override
    public String appId() {
        return inner().appId();
    }

    @Override
    public List<String> servicePrincipalNames() {
        return inner().servicePrincipalNames();
    }

    @Override
    protected Observable<ServicePrincipalInner> getInnerAsync() {
        return manager.inner().servicePrincipals().getAsync(id());
    }

    @Override
    public Observable<ServicePrincipal> createResourceAsync() {
        Application application = (Application) ((Object) super.createdModel(applicationCreatable.key()));
        createParameters.withAppId(application.appId());
        return manager.inner().servicePrincipals().createAsync(createParameters)
                .map(innerToFluentMap(this))
                .flatMap(new Func1<ServicePrincipal, Observable<ServicePrincipal>>() {
                    @Override
                    public Observable<ServicePrincipal> call(ServicePrincipal servicePrincipal) {
                        return refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    Observable<ServicePrincipal> refreshCredentialsAsync() {
        final Observable<ServicePrincipal> keyCredentials = manager.inner().servicePrincipals().listKeyCredentialsAsync(id())
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
                }).map(new Func1<Map<String, CertificateCredential>, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(Map<String, CertificateCredential> stringCertificateCredentialMap) {
                        ServicePrincipalImpl.this.cachedCertificateCredentials = stringCertificateCredentialMap;
                        return ServicePrincipalImpl.this;
                    }
                });
        final Observable<ServicePrincipal> passwordCredentials = manager.inner().servicePrincipals().listPasswordCredentialsAsync(id())
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
                }).map(new Func1<Map<String, PasswordCredential>, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(Map<String, PasswordCredential> stringPasswordCredentialMap) {
                        ServicePrincipalImpl.this.cachedPasswordCredentials = stringPasswordCredentialMap;
                        return ServicePrincipalImpl.this;
                    }
                });
        return keyCredentials.mergeWith(passwordCredentials).last();
    }

    @Override
    public Observable<ServicePrincipal> refreshAsync() {
        return getInnerAsync()
                .map(innerToFluentMap(this))
                .flatMap(new Func1<ServicePrincipal, Observable<ServicePrincipal>>() {
                    @Override
                    public Observable<ServicePrincipal> call(ServicePrincipal application) {
                        return refreshCredentialsAsync();
                    }
                });
    }

    @Override
    public CertificateCredentialImpl<DefinitionStages.WithCreate> defineCertificateCredential(String name) {
        return new CertificateCredentialImpl<>(name, this);
    }

    @Override
    public PasswordCredentialImpl<DefinitionStages.WithCreate> definePasswordCredential(String name) {
        return new PasswordCredentialImpl<>(name, this);
    }

    @Override
    public ServicePrincipalImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        if (createParameters.keyCredentials() == null) {
            createParameters.withKeyCredentials(new ArrayList<KeyCredentialInner>());
        }
        createParameters.keyCredentials().add(credential.inner());
        return this;
    }

    @Override
    public ServicePrincipalImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        if (createParameters.passwordCredentials() == null) {
            createParameters.withPasswordCredentials(new ArrayList<PasswordCredentialInner>());
        }
        createParameters.passwordCredentials().add(credential.inner());
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(String id) {
        createParameters.withAppId(id);
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(Application application) {
        createParameters.withAppId(application.appId());
        return this;
    }

    @Override
    public ServicePrincipalImpl withNewApplication(Creatable<Application> applicationCreatable) {
        addCreatableDependency(applicationCreatable);
        this.applicationCreatable = applicationCreatable;
        return this;
    }

    @Override
    public ServicePrincipalImpl withNewApplication(String signOnUrl) {
        return withNewApplication(manager.applications().define(signOnUrl)
                .withSignOnUrl(signOnUrl)
                .withIdentifierUrl(signOnUrl));
    }
}
