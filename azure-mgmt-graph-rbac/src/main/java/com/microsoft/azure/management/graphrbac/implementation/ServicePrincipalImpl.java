/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Application;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ServicePrincipalImpl
        extends CreatableUpdatableImpl<ServicePrincipal, ServicePrincipalInner, ServicePrincipalImpl>
        implements
            ServicePrincipal,
            ServicePrincipal.Definition,
            ServicePrincipal.Update,
            HasCredential<ServicePrincipalImpl> {
    private GraphRbacManager manager;
    private ServicePrincipalCreateParametersInner createParameters;
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
                .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    public CredentialImpl<ServicePrincipal.DefinitionStages.WithCreate> defineKey(String name) {
        return new CredentialImpl<>(name, this);
    }

    @Override
    public ServicePrincipalImpl withCredential(CredentialImpl<?> credential) {
        if (credential.passwordCredential() != null) {
            if (createParameters.passwordCredentials() == null) {
                createParameters.withPasswordCredentials(new ArrayList<PasswordCredentialInner>());
            }
            createParameters.passwordCredentials().add(credential.passwordCredential());
        } else if (credential.certificateCredential() != null) {
            if (createParameters.keyCredentials() == null) {
                createParameters.withKeyCredentials(new ArrayList<KeyCredentialInner>());
            }
            createParameters.keyCredentials().add(credential.certificateCredential());
        }
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
