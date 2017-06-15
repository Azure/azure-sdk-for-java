/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.CertificateCredential;
import com.microsoft.azure.management.graphrbac.PasswordCredential;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ServicePrincipalImpl
        extends CreatableUpdatableImpl<ServicePrincipal, ServicePrincipalInner, ServicePrincipalImpl>
        implements
            ServicePrincipal,
            ServicePrincipal.Definition,
            ServicePrincipal.Update,
            HasCredential<ServicePrincipalImpl> {
    private GraphRbacManager manager;

    private Map<String, PasswordCredential> cachedPasswordCredentials;
    private Map<String, CertificateCredential> cachedCertificateCredentials;
    private Map<String, RoleAssignment> cachedRoleAssignments;

    private ServicePrincipalCreateParametersInner createParameters;
    private Creatable<ActiveDirectoryApplication> applicationCreatable;
    private Map<String, BuiltInRole> rolesToCreate;
    private Set<String> rolesToDelete;

    String assignedSubscription;
    private List<CertificateCredentialImpl<?>> certificateCredentialsToCreate;
    private List<PasswordCredentialImpl<?>> passwordCredentialsToCreate;

    ServicePrincipalImpl(ServicePrincipalInner innerObject, GraphRbacManager manager) {
        super(innerObject.displayName(), innerObject);
        this.manager = manager;
        this.createParameters = new ServicePrincipalCreateParametersInner().withAccountEnabled(true);
        this.rolesToCreate = new HashMap<>();
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
    public List<String> servicePrincipalNames() {
        return inner().servicePrincipalNames();
    }

    @Override
    public Map<String, PasswordCredential> passwordCredentials() {
        if (cachedPasswordCredentials == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(cachedPasswordCredentials);
    }

    @Override
    public Map<String, CertificateCredential> certificateCredentials() {
        if (cachedCertificateCredentials == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(cachedCertificateCredentials);
    }

    @Override
    protected Observable<ServicePrincipalInner> getInnerAsync() {
        return manager.inner().servicePrincipals().getAsync(id());
    }

    @Override
    public Observable<ServicePrincipal> createResourceAsync() {
        Observable<ServicePrincipal> sp = Observable.just((ServicePrincipal) this);
        if (isInCreateMode()) {
            if (applicationCreatable != null) {
                ActiveDirectoryApplication application = (ActiveDirectoryApplication) ((Object) super.createdModel(applicationCreatable.key()));
                createParameters.withAppId(application.applicationId());
            }
            sp = manager.inner().servicePrincipals().createAsync(createParameters)
                    .map(innerToFluentMap(this));
        }
        return sp.flatMap(new Func1<ServicePrincipal, Observable<ServicePrincipal>>() {
            @Override
            public Observable<ServicePrincipal> call(ServicePrincipal servicePrincipal) {
                return submitCredentialsAsync(servicePrincipal).mergeWith(submitRolesAsync(servicePrincipal));
            }
        }).map(new Func1<ServicePrincipal, ServicePrincipal>() {
            @Override
            public ServicePrincipal call(ServicePrincipal servicePrincipal) {
                if (passwordCredentialsToCreate != null) {
                    for (PasswordCredentialImpl<?> passwordCredential : passwordCredentialsToCreate) {
                        passwordCredential.exportAuthFile((ServicePrincipalImpl) servicePrincipal);
                    }
                }
                if (certificateCredentialsToCreate != null) {
                    for (CertificateCredentialImpl<?> certificateCredential : certificateCredentialsToCreate) {
                        certificateCredential.exportAuthFile((ServicePrincipalImpl) servicePrincipal);
                    }
                }
                passwordCredentialsToCreate = null;
                certificateCredentialsToCreate = null;
                return servicePrincipal;
            }
        });
    }

    private Observable<ServicePrincipal> submitCredentialsAsync(final ServicePrincipal sp) {
        Observable<Void> observable = Observable.just(null);
        if (cachedCertificateCredentials != null && !cachedCertificateCredentials.isEmpty()) {
            observable = observable.mergeWith(manager().inner().servicePrincipals().updateKeyCredentialsAsync(sp.id(),
                    Lists.transform(new ArrayList<>(cachedCertificateCredentials.values()), new Function<CertificateCredential, KeyCredentialInner>() {
                        @Override
                        public KeyCredentialInner apply(CertificateCredential input) {
                            return input.inner();
                        }
                    })));
        }
        if (cachedPasswordCredentials != null && !cachedPasswordCredentials.isEmpty()) {
            observable = observable.mergeWith(manager().inner().servicePrincipals().updatePasswordCredentialsAsync(sp.id(),
                    Lists.transform(new ArrayList<>(cachedPasswordCredentials.values()), new Function<PasswordCredential, PasswordCredentialInner>() {
                        @Override
                        public PasswordCredentialInner apply(PasswordCredential input) {
                            return input.inner();
                        }
                    })));
        }
        return observable.last().flatMap(new Func1<Void, Observable<ServicePrincipal>>() {
            @Override
            public Observable<ServicePrincipal> call(Void aVoid) {
                return refreshCredentialsAsync();
            }
        });
    }

    private Observable<ServicePrincipal> submitRolesAsync(final ServicePrincipal servicePrincipal) {
        Observable<ServicePrincipal> create;
        if (rolesToCreate == null || rolesToCreate.isEmpty()) {
            create = Observable.just(servicePrincipal);
        } else {
            create = Observable.from(rolesToCreate.entrySet())
                    .flatMap(new Func1<Map.Entry<String, BuiltInRole>, Observable<?>>() {
                        @Override
                        public Observable<?> call(Map.Entry<String, BuiltInRole> role) {
                            return manager().roleAssignments().define(SdkContext.randomUuid())
                                    .forServicePrincipal(servicePrincipal)
                                    .withBuiltInRole(role.getValue())
                                    .withScope(role.getKey())
                                    .createAsync()
                                    .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(Observable<? extends Throwable> observable) {
                                            return observable.zipWith(Observable.range(1, 30), new Func2<Throwable, Integer, Integer>() {
                                                @Override
                                                public Integer call(Throwable throwable, Integer integer) {
                                                    if (throwable instanceof CloudException
                                                            && ((CloudException) throwable).body().code().equalsIgnoreCase("PrincipalNotFound")) {
                                                        return integer;
                                                    } else {
                                                        throw Exceptions.propagate(throwable);
                                                    }
                                                }
                                            }).flatMap(new Func1<Integer, Observable<?>>() {
                                                @Override
                                                public Observable<?> call(Integer i) {
                                                    return Observable.timer(i, TimeUnit.SECONDS);
                                                }
                                            });
                                        }
                                    });
                        }
                    })
                    .last()
                    .map(new Func1<Object, ServicePrincipal>() {
                        @Override
                        public ServicePrincipal call(Object o) {
                            return servicePrincipal;
                        }
                    });
        }
        Observable<ServicePrincipal> delete;
        if (rolesToDelete == null || rolesToDelete.isEmpty()) {
            delete =  Observable.just(servicePrincipal);
        } else {
            delete = Observable.from(rolesToDelete)
                    .flatMap(new Func1<String, Observable<?>>() {
                        @Override
                        public Observable<?> call(final String s) {
                            return manager().roleAssignments().deleteByIdAsync(cachedRoleAssignments.get(s).id()).toObservable()
                                    .doOnCompleted(new Action0() {
                                        @Override
                                        public void call() {
                                            cachedRoleAssignments.remove(s);
                                        }
                                    });
                        }
                    })
                    .last()
                    .map(new Func1<Object, ServicePrincipal>() {
                        @Override
                        public ServicePrincipal call(Object o) {
                            return servicePrincipal;
                        }
                    });
        }
        return create.mergeWith(delete).last();
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    Observable<ServicePrincipal> refreshCredentialsAsync() {
        final Observable<ServicePrincipal> keyCredentials = manager.inner().servicePrincipals().listKeyCredentialsAsync(id())
                .map(new Func1<List<KeyCredentialInner>, Map<String, CertificateCredential>>() {
                    @Override
                    public Map<String, CertificateCredential> call(List<KeyCredentialInner> keyCredentialInners) {
                        if (keyCredentialInners == null || keyCredentialInners.isEmpty()) {
                            return null;
                        }
                        Map<String, CertificateCredential> certificateCredentialMap = new HashMap<String, CertificateCredential>();
                        for (KeyCredentialInner inner : keyCredentialInners) {
                            CertificateCredential credential = new CertificateCredentialImpl<>(inner);
                            certificateCredentialMap.put(credential.name(), credential);
                        }
                        return certificateCredentialMap;
                    }
                })
                .map(new Func1<Map<String, CertificateCredential>, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(Map<String, CertificateCredential> stringCertificateCredentialMap) {
                        ServicePrincipalImpl.this.cachedCertificateCredentials = stringCertificateCredentialMap;
                        return ServicePrincipalImpl.this;
                    }
                });
        final Observable<ServicePrincipal> passwordCredentials = manager.inner().servicePrincipals().listPasswordCredentialsAsync(id())
                .map(new Func1<List<PasswordCredentialInner>, Map<String, PasswordCredential>>() {
                    @Override
                    public Map<String, PasswordCredential> call(List<PasswordCredentialInner> passwordCredentialInners) {
                        if (passwordCredentialInners == null || passwordCredentialInners.isEmpty()) {
                            return null;
                        }
                        Map<String, PasswordCredential> passwordCredentialMap = new HashMap<String, PasswordCredential>();
                        for (PasswordCredentialInner inner : passwordCredentialInners) {
                            PasswordCredential credential = new PasswordCredentialImpl<>(inner);
                            passwordCredentialMap.put(credential.name(), credential);
                        }
                        return passwordCredentialMap;
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
    public ServicePrincipalImpl withoutCredential(String name) {
        if (cachedPasswordCredentials.containsKey(name)) {
            cachedPasswordCredentials.remove(name);
        } else if (cachedCertificateCredentials.containsKey(name)) {
            cachedCertificateCredentials.remove(name);
        }
        return this;
    }

    @Override
    public ServicePrincipalImpl withCertificateCredential(CertificateCredentialImpl<?> credential) {
        if (certificateCredentialsToCreate == null) {
            certificateCredentialsToCreate = new ArrayList<>();
        }
        this.certificateCredentialsToCreate.add(credential);
        if (cachedCertificateCredentials == null) {
            cachedCertificateCredentials = new HashMap<>();
        }
        this.cachedCertificateCredentials.put(credential.name(), credential);
        return this;
    }

    @Override
    public ServicePrincipalImpl withPasswordCredential(PasswordCredentialImpl<?> credential) {
        if (passwordCredentialsToCreate == null) {
            passwordCredentialsToCreate = new ArrayList<>();
        }
        this.passwordCredentialsToCreate.add(credential);
        if (cachedPasswordCredentials == null) {
            cachedPasswordCredentials = new HashMap<>();
        }
        this.cachedPasswordCredentials.put(credential.name(), credential);
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

    @Override
    public GraphRbacManager manager() {
        return manager;
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
}
