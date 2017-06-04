/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.RoleAssignmentPropertiesWithScope;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class RoleAssignmentImpl
        extends CreatableImpl<RoleAssignment, RoleAssignmentInner, RoleAssignmentImpl>
        implements
            RoleAssignment,
            RoleAssignment.Definition {
    private GraphRbacManager manager;
    // Active Directory identify info
    private String objectId;
    private String userName;
    private String servicePrincipalName;
    // role info
    private String roleDefinitionId;
    private String roleName;

    RoleAssignmentImpl(RoleAssignmentInner innerObject, GraphRbacManager manager) {
        super(innerObject.name(), innerObject);
        this.manager = manager;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public boolean isInCreateMode() {
        return inner().id() == null;
    }

    @Override
    public Observable<RoleAssignment> createResourceAsync() {
        Observable<String> objectIdObservable;
        if (objectId != null) {
            objectIdObservable = Observable.just(objectId);
        } else if (userName != null) {
            objectIdObservable = manager.users().getByNameAsync(userName)
                    .map(new Func1<ActiveDirectoryUser, String>() {
                        @Override
                        public String call(ActiveDirectoryUser user) {
                            return user.id();
                        }
                    });
        } else if (servicePrincipalName != null) {
            objectIdObservable = manager.servicePrincipals().getByNameAsync(servicePrincipalName)
                    .map(new Func1<ServicePrincipal, String>() {
                        @Override
                        public String call(ServicePrincipal sp) {
                            return sp.id();
                        }
                    });
        } else {
            throw new IllegalArgumentException("Please pass a non-null value for either object Id, user, group, or service principal");
        }

        Observable<String> roleDefinitionIdObservable;
        if (roleDefinitionId != null) {
            roleDefinitionIdObservable = Observable.just(roleDefinitionId);
        } else if (roleName != null) {
            roleDefinitionIdObservable = manager().roleDefinitions().getByScopeAndRoleNameAsync(scope(), roleName)
                    .map(new Func1<RoleDefinition, String>() {
                        @Override
                        public String call(RoleDefinition roleDefinition) {
                            return roleDefinition.id();
                        }
                    });
        } else {
            throw new IllegalArgumentException("Please pass a non-null value for either role name or role definition ID");
        }

        return Observable.zip(objectIdObservable, roleDefinitionIdObservable, new Func2<String, String, RoleAssignmentPropertiesInner>() {
            @Override
            public RoleAssignmentPropertiesInner call(String objectId, String roleDefinitionId) {
                return new RoleAssignmentPropertiesInner()
                        .withPrincipalId(objectId).withRoleDefinitionId(roleDefinitionId);
            }
        }).flatMap(new Func1<RoleAssignmentPropertiesInner, Observable<RoleAssignmentInner>>() {
            @Override
            public Observable<RoleAssignmentInner> call(RoleAssignmentPropertiesInner roleAssignmentPropertiesInner) {
                return manager().roleInner().roleAssignments()
                        .createAsync(scope(), name(), roleAssignmentPropertiesInner);
            }
        }).map(innerToFluentMap(this));
    }

    @Override
    protected Observable<RoleAssignmentInner> getInnerAsync() {
        return manager.roleInner().roleAssignments().getAsync(scope(), name());
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }

    @Override
    public String scope() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().scope();
    }

    @Override
    public String roleDefinitionId() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().roleDefinitionId();
    }

    @Override
    public String principalId() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().principalId();
    }

    @Override
    public RoleAssignmentImpl forObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    @Override
    public RoleAssignmentImpl forUser(ActiveDirectoryUser user) {
        this.objectId = user.id();
        return this;
    }

    @Override
    public RoleAssignmentImpl forUser(String name) {
        this.userName = name;
        return this;
    }

    @Override
    public RoleAssignmentImpl forGroup(ActiveDirectoryGroup activeDirectoryGroup) {
        this.objectId = activeDirectoryGroup.id();
        return this;
    }

    @Override
    public RoleAssignmentImpl forServicePrincipal(ServicePrincipal servicePrincipal) {
        this.objectId = servicePrincipal.id();
        return this;
    }

    @Override
    public RoleAssignmentImpl forServicePrincipal(String servicePrincipalName) {
        this.servicePrincipalName = servicePrincipalName;
        return this;
    }

    @Override
    public RoleAssignmentImpl withBuiltInRole(BuiltInRole role) {
        this.roleName = role.toString();
        return this;
    }

    @Override
    public RoleAssignmentImpl withRoleDefinition(String roleDefinitionId) {
        this.roleDefinitionId = roleDefinitionId;
        return this;
    }

    @Override
    public RoleAssignmentImpl withScope(String scope) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new RoleAssignmentPropertiesWithScope());
        }
        this.inner().properties().withScope(scope);
        return this;
    }

    @Override
    public RoleAssignmentImpl withResourceGroupScope(ResourceGroup resourceGroup) {
        return withScope(resourceGroup.id());
    }

    @Override
    public RoleAssignmentImpl withResourceScope(Resource resource) {
        return withScope(resource.id());
    }

    @Override
    public RoleAssignmentImpl withSubscriptionScope(String subscriptionId) {
        return withScope("subscriptions/" + subscriptionId);
    }
}
