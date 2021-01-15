// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphServicePrincipalInner;
import com.azure.resourcemanager.authorization.fluent.models.ServicePrincipalsAddPasswordRequestBodyInner;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Implementation for ServicePrincipal and its parent interfaces. */
class ServicePrincipalImpl
    extends CreatableUpdatableImpl<ServicePrincipal, MicrosoftGraphServicePrincipalInner, ServicePrincipalImpl>
    implements ServicePrincipal,
        ServicePrincipal.Definition,
        ServicePrincipal.Update,
        HasCredential<ServicePrincipalImpl> {
    private AuthorizationManager manager;

    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;
    private Map<String, RoleAssignment> cachedRoleAssignments;

    private Creatable<ActiveDirectoryApplication> applicationCreatable;
    private Map<String, BuiltInRole> rolesToCreate;
    private Set<String> rolesToDelete;

    String assignedSubscription;
    private List<CertificateCredentialImpl<?>> certificateCredentialsToCreate;
    private List<PasswordCredentialImpl<?>> passwordCredentialsToCreate;

    ServicePrincipalImpl(MicrosoftGraphServicePrincipalInner innerObject, AuthorizationManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.cachedRoleAssignments = new HashMap<>();
        this.rolesToCreate = new HashMap<>();
        this.rolesToDelete = new HashSet<>();
        this.cachedCertificateCredentials = new HashMap<>();
        this.certificateCredentialsToCreate = new ArrayList<>();
        this.cachedPasswordCredentials = new HashMap<>();
        this.passwordCredentialsToCreate = new ArrayList<>();
        this.refreshCredentials(innerObject);
    }

    @Override
    public String applicationId() {
        return innerModel().appId();
    }

    @Override
    public List<String> servicePrincipalNames() {
        return innerModel().servicePrincipalNames();
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
    protected Mono<MicrosoftGraphServicePrincipalInner> getInnerAsync() {
        return manager.serviceClient().getServicePrincipalsServicePrincipals().getServicePrincipalAsync(id())
            .doOnSuccess(this::refreshCredentials);
    }

    @Override
    public Mono<ServicePrincipal> createResourceAsync() {
        Mono<ServicePrincipal> sp;
        if (isInCreateMode()) {
            innerModel().withAccountEnabled(true);
            if (applicationCreatable != null) {
                ActiveDirectoryApplication application = this.taskResult(applicationCreatable.key());
                innerModel().withAppId(application.applicationId());
            }
            sp = manager.serviceClient().getServicePrincipalsServicePrincipals()
                .createServicePrincipalAsync(innerModel()).map(innerToFluentMap(this));
        } else {
            sp = manager().serviceClient().getServicePrincipalsServicePrincipals()
                .updateServicePrincipalAsync(id(), new MicrosoftGraphServicePrincipalInner()
                    .withKeyCredentials(innerModel().keyCredentials())
                    .withPasswordCredentials(innerModel().passwordCredentials())
                ).then(refreshAsync());
        }
        return sp
            .flatMap(
                servicePrincipal ->
                    submitCredentialsAsync(servicePrincipal).mergeWith(submitRolesAsync(servicePrincipal)).last())
            .map(
                servicePrincipal -> {
                    for (PasswordCredentialImpl<?> passwordCredential : passwordCredentialsToCreate) {
                        passwordCredential.exportAuthFile((ServicePrincipalImpl) servicePrincipal);
                        passwordCredential.consumeSecret();
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
        return Flux.defer(() ->
//                    Flux.fromIterable(certificateCredentialsToCreate)
//                        .flatMap(certificateCredential ->
//                            manager().serviceClient().getServicePrincipals()
//                                .addKeyAsync(id(),
//                                    new ServicePrincipalsAddKeyRequestBodyInner()
//                                        .withKeyCredential(certificateCredential.innerModel()))),
//                    Flux.fromIterable(certificateCredentialsToDelete)
//                        .flatMap(id -> manager().serviceClient().getServicePrincipals()
//                            .removeKeyAsync(id(),
//                                new ServicePrincipalsRemoveKeyRequestBody()
//                                    .withKeyId(UUID.fromString(id)))),
                    Flux.fromIterable(passwordCredentialsToCreate)
                        .flatMap(passwordCredential ->
                            manager().serviceClient().getServicePrincipals()
                                .addPasswordAsync(id(),
                                    new ServicePrincipalsAddPasswordRequestBodyInner()
                                        .withPasswordCredential(passwordCredential.innerModel()))
                                .doOnNext(passwordCredential::setInner)
                        )
//                    Flux.fromIterable(passwordCredentialsToDelete)
//                        .flatMap(id -> manager().serviceClient().getServicePrincipals()
//                            .removePasswordAsync(id(),
//                                new ServicePrincipalsRemovePasswordRequestBody()
//                                    .withKeyId(UUID.fromString(id))))
            )
            .then(refreshAsync());
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
                                .define(this.manager().internalContext().randomUuid())
                                .forServicePrincipal(servicePrincipal)
                                .withBuiltInRole(roleEntry.getValue())
                                .withScope(roleEntry.getKey())
                                .createAsync())
                    .doOnNext(
                        indexable ->
                            cachedRoleAssignments.put(indexable.id(), indexable))
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

    void refreshCredentials(MicrosoftGraphServicePrincipalInner inner) {
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

    @Override
    public Mono<ServicePrincipal> refreshAsync() {
        return getInnerAsync().map(innerToFluentMap(this));
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
            innerModel().passwordCredentials().remove(cachedPasswordCredentials.get(name).innerModel());
        } else if (cachedCertificateCredentials.containsKey(name)) {
            innerModel().keyCredentials().remove(cachedCertificateCredentials.get(name).innerModel());
        }
        return this;
    }

    @Override
    public ServicePrincipalImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        this.certificateCredentialsToCreate.add(credential);
        if (innerModel().keyCredentials() == null) {
            innerModel().withKeyCredentials(new ArrayList<>());
        }
        innerModel().keyCredentials().add(credential.innerModel());
        return this;
    }

    @Override
    public ServicePrincipalImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        this.passwordCredentialsToCreate.add(credential);
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(String id) {
        innerModel().withAppId(id);
        return this;
    }

    @Override
    public ServicePrincipalImpl withExistingApplication(ActiveDirectoryApplication application) {
        innerModel().withAppId(application.applicationId());
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
    public ServicePrincipalImpl withNewApplication() {
        return withNewApplication(
            manager.applications().define(name()));
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
        return innerModel().id();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
