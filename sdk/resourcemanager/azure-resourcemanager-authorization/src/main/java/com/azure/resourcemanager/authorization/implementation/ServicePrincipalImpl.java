// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.authorization.models.ServicePrincipalCreateParameters;
import com.azure.resourcemanager.authorization.fluent.inner.KeyCredentialInner;
import com.azure.resourcemanager.authorization.fluent.inner.PasswordCredentialInner;
import com.azure.resourcemanager.authorization.fluent.inner.ServicePrincipalInner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for ServicePrincipal and its parent interfaces. */
class ServicePrincipalImpl extends CreatableUpdatableImpl<ServicePrincipal, ServicePrincipalInner, ServicePrincipalImpl>
    implements ServicePrincipal,
        ServicePrincipal.Definition,
        ServicePrincipal.Update,
        HasCredential<ServicePrincipalImpl> {
    private AuthorizationManager manager;

    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;
    private Map<String, RoleAssignment> cachedRoleAssignments;

    private ServicePrincipalCreateParameters createParameters;
    private Creatable<ActiveDirectoryApplication> applicationCreatable;
    private Map<String, BuiltInRole> rolesToCreate;
    private Set<String> rolesToDelete;

    String assignedSubscription;
    private List<CertificateCredentialImpl<?>> certificateCredentialsToCreate;
    private List<PasswordCredentialImpl<?>> passwordCredentialsToCreate;
    private Set<String> certificateCredentialsToDelete;
    private Set<String> passwordCredentialsToDelete;

    ServicePrincipalImpl(ServicePrincipalInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ServicePrincipalCreateParameters();
        this.createParameters.withAccountEnabled(true);
        this.cachedRoleAssignments = new HashMap<>();
        this.rolesToCreate = new HashMap<>();
        this.rolesToDelete = new HashSet<>();
        this.cachedCertificateCredentials = new HashMap<>();
        this.certificateCredentialsToCreate = new ArrayList<>();
        this.certificateCredentialsToDelete = new HashSet<>();
        this.cachedPasswordCredentials = new HashMap<>();
        this.passwordCredentialsToCreate = new ArrayList<>();
        this.passwordCredentialsToDelete = new HashSet<>();
    }

    @Override
    public String applicationId() {
        return inner().appId();
    }

    @Override
    public List<String> servicePrincipalNames() {
        return inner().servicePrincipalNames();
    }

    @Override
    public Map<String, PasswordCredential> passwordCredentials() {
        return Collections.unmodifiableMap(cachedPasswordCredentials);
    }

    @Override
    public Map<String, CertificateCredential> certificateCredentials() {
        return Collections.unmodifiableMap(cachedCertificateCredentials);
    }

    @Override
    public Set<RoleAssignment> roleAssignments() {
        return Collections.unmodifiableSet(new HashSet<>(cachedRoleAssignments.values()));
    }

    @Override
    protected Mono<ServicePrincipalInner> getInnerAsync() {
        return manager.inner().getServicePrincipals().getAsync(id());
    }

    @Override
    public Mono<ServicePrincipal> createResourceAsync() {
        Mono<ServicePrincipal> sp = Mono.just(this);
        if (isInCreateMode()) {
            if (applicationCreatable != null) {
                ActiveDirectoryApplication application = this.taskResult(applicationCreatable.key());
                createParameters.withAppId(application.applicationId());
            }
            sp = manager.inner().getServicePrincipals().createAsync(createParameters).map(innerToFluentMap(this));
        }
        return sp
            .flatMap(
                servicePrincipal ->
                    submitCredentialsAsync(servicePrincipal).mergeWith(submitRolesAsync(servicePrincipal)).last())
            .map(
                servicePrincipal -> {
                    for (PasswordCredentialImpl<?> passwordCredential : passwordCredentialsToCreate) {
                        passwordCredential.exportAuthFile((ServicePrincipalImpl) servicePrincipal);
                    }
                    for (CertificateCredentialImpl<?> certificateCredential : certificateCredentialsToCreate) {
                        certificateCredential.exportAuthFile((ServicePrincipalImpl) servicePrincipal);
                    }
                    passwordCredentialsToCreate.clear();
                    certificateCredentialsToCreate.clear();
                    return servicePrincipal;
                });
    }

    private Mono<ServicePrincipal> submitCredentialsAsync(final ServicePrincipal sp) {
        Mono<ServicePrincipal> mono = Mono.empty();
        if (!certificateCredentialsToCreate.isEmpty() || !certificateCredentialsToDelete.isEmpty()) {
            Map<String, CertificateCredential> newCerts = new HashMap<>(cachedCertificateCredentials);
            for (String delete : certificateCredentialsToDelete) {
                newCerts.remove(delete);
            }
            for (CertificateCredential create : certificateCredentialsToCreate) {
                newCerts.put(create.name(), create);
            }
            List<KeyCredentialInner> updateKeyCredentials = new ArrayList<>();
            for (CertificateCredential certificateCredential : newCerts.values()) {
                updateKeyCredentials.add(certificateCredential.inner());
            }
            mono =
                mono
                    .concatWith(
                        manager()
                            .inner()
                            .getServicePrincipals()
                            .updateKeyCredentialsAsync(sp.id(), updateKeyCredentials)
                            .then(Mono.just(ServicePrincipalImpl.this)))
                    .last();
        }
        if (!passwordCredentialsToCreate.isEmpty() || !passwordCredentialsToDelete.isEmpty()) {
            Map<String, PasswordCredential> newPasses = new HashMap<>(cachedPasswordCredentials);
            for (String delete : passwordCredentialsToDelete) {
                newPasses.remove(delete);
            }
            for (PasswordCredential create : passwordCredentialsToCreate) {
                newPasses.put(create.name(), create);
            }
            List<PasswordCredentialInner> updatePasswordCredentials = new ArrayList<>();
            for (PasswordCredential passwordCredential : newPasses.values()) {
                updatePasswordCredentials.add(passwordCredential.inner());
            }
            mono =
                mono
                    .concatWith(
                        manager()
                            .inner()
                            .getServicePrincipals()
                            .updatePasswordCredentialsAsync(sp.id(), updatePasswordCredentials)
                            .then(Mono.just(ServicePrincipalImpl.this)))
                    .last();
        }
        return mono
            .flatMap(
                servicePrincipal -> {
                    passwordCredentialsToDelete.clear();
                    certificateCredentialsToDelete.clear();
                    return refreshCredentialsAsync();
                });
    }

    private Mono<ServicePrincipal> submitRolesAsync(final ServicePrincipal servicePrincipal) {
        Mono<ServicePrincipal> create;
        if (rolesToCreate.isEmpty()) {
            create = Mono.just(servicePrincipal);
        } else {
            create =
                Flux
                    .fromIterable(rolesToCreate.entrySet())
                    .flatMap(
                        roleEntry ->
                            manager()
                                .roleAssignments()
                                .define(this.manager().sdkContext().randomUuid())
                                .forServicePrincipal(servicePrincipal)
                                .withBuiltInRole(roleEntry.getValue())
                                .withScope(roleEntry.getKey())
                                .createAsync())
                    .doOnNext(
                        indexable ->
                            cachedRoleAssignments.put(((RoleAssignment) indexable).id(), (RoleAssignment) indexable))
                    .last()
                    .map(
                        indexable -> {
                            rolesToCreate.clear();
                            return servicePrincipal;
                        });
        }
        Mono<ServicePrincipal> delete;
        if (rolesToDelete.isEmpty()) {
            delete = Mono.just(servicePrincipal);
        } else {
            delete =
                Flux
                    .fromIterable(rolesToDelete)
                    .flatMap(
                        role ->
                            manager()
                                .roleAssignments()
                                .deleteByIdAsync(cachedRoleAssignments.get(role).id())
                                .thenReturn(role))
                    .doOnNext(s -> cachedRoleAssignments.remove(s))
                    .last()
                    .map(
                        s -> {
                            rolesToDelete.clear();
                            return servicePrincipal;
                        });
        }
        return create.mergeWith(delete).last();
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    Mono<ServicePrincipal> refreshCredentialsAsync() {
        return Mono
            .just(ServicePrincipalImpl.this)
            .map(
                (Function<ServicePrincipalImpl, ServicePrincipal>)
                servicePrincipal -> {
                    servicePrincipal.cachedCertificateCredentials.clear();
                    servicePrincipal.cachedPasswordCredentials.clear();
                    return servicePrincipal;
                })
            .concatWith(
                manager()
                    .inner()
                    .getServicePrincipals()
                    .listKeyCredentialsAsync(id())
                    .map(
                        keyCredentialInner -> {
                            CertificateCredential credential = new CertificateCredentialImpl<>(keyCredentialInner);
                            ServicePrincipalImpl.this.cachedCertificateCredentials.put(credential.name(), credential);
                            return ServicePrincipalImpl.this;
                        }))
            .concatWith(
                manager()
                    .inner()
                    .getServicePrincipals()
                    .listPasswordCredentialsAsync(id())
                    .map(
                        passwordCredentialInner -> {
                            PasswordCredential credential = new PasswordCredentialImpl<>(passwordCredentialInner);
                            ServicePrincipalImpl.this.cachedPasswordCredentials.put(credential.name(), credential);
                            return ServicePrincipalImpl.this;
                        }))
            .last();
    }

    @Override
    public Mono<ServicePrincipal> refreshAsync() {
        return getInnerAsync().map(innerToFluentMap(this)).flatMap(application -> refreshCredentialsAsync());
    }

    @Override
    public CertificateCredentialImpl<ServicePrincipalImpl> defineCertificateCredential(String name) {
        return new CertificateCredentialImpl<>(name, this);
    }

    @Override
    public PasswordCredentialImpl<ServicePrincipalImpl> definePasswordCredential(String name) {
        return new PasswordCredentialImpl<>(name, this);
    }

    @Override
    public ServicePrincipalImpl withoutCredential(String name) {
        if (cachedPasswordCredentials.containsKey(name)) {
            passwordCredentialsToDelete.add(name);
        } else if (cachedCertificateCredentials.containsKey(name)) {
            certificateCredentialsToDelete.add(name);
        }
        return this;
    }

    @Override
    public ServicePrincipalImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        this.certificateCredentialsToCreate.add(credential);
        return this;
    }

    @Override
    public ServicePrincipalImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {

        this.passwordCredentialsToCreate.add(credential);
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(String id) {
        createParameters.withAppId(id);
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(ActiveDirectoryApplication application) {
        createParameters.withAppId(application.applicationId());
        return this;
    }

    @Override
    public ServicePrincipalImpl withNewApplication(Creatable<ActiveDirectoryApplication> applicationCreatable) {
        this.addDependency(applicationCreatable);
        this.applicationCreatable = applicationCreatable;
        return this;
    }

    @Override
    public ServicePrincipalImpl withNewApplication(String signOnUrl) {
        return withNewApplication(
            manager.applications().define(name()).withSignOnUrl(signOnUrl).withIdentifierUrl(signOnUrl));
    }

    @Override
    public ServicePrincipalImpl withNewRole(BuiltInRole role, String scope) {
        this.rolesToCreate.put(scope, role);
        return this;
    }

    @Override
    public ServicePrincipalImpl withNewRoleInSubscription(BuiltInRole role, String subscriptionId) {
        this.assignedSubscription = subscriptionId;
        return withNewRole(role, "subscriptions/" + subscriptionId);
    }

    @Override
    public ServicePrincipalImpl withNewRoleInResourceGroup(BuiltInRole role, ResourceGroup resourceGroup) {
        return withNewRole(role, resourceGroup.id());
    }

    @Override
    public Update withoutRole(RoleAssignment roleAssignment) {
        this.rolesToDelete.add(roleAssignment.id());
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
