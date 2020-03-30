/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.management.CloudException;
import com.azure.management.graphrbac.ActiveDirectoryGroup;
import com.azure.management.graphrbac.ActiveDirectoryUser;
import com.azure.management.graphrbac.BuiltInRole;
import com.azure.management.graphrbac.RoleAssignment;
import com.azure.management.graphrbac.RoleAssignmentCreateParameters;
import com.azure.management.graphrbac.ServicePrincipal;
import com.azure.management.graphrbac.models.RoleAssignmentInner;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
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

    RoleAssignmentImpl(String name, RoleAssignmentInner innerObject, GraphRbacManager manager) {
        super(name, innerObject);
        this.manager = manager;
    }

    @Override
    public boolean isInCreateMode() {
        return inner().getId() == null;
    }

    @Override
    public Mono<RoleAssignment> createResourceAsync() {
        Mono<String> objectIdObservable;
        if (objectId != null) {
            objectIdObservable = Mono.just(objectId);
        } else if (userName != null) {
            objectIdObservable = manager.users().getByNameAsync(userName)
                    .map(user -> user.id());
        } else if (servicePrincipalName != null) {
            objectIdObservable = manager.servicePrincipals().getByNameAsync(servicePrincipalName)
                    .map(sp -> sp.id());
        } else {
            throw new IllegalArgumentException("Please pass a non-null value for either object Id, user, group, or service principal");
        }

        Mono<String> roleDefinitionIdObservable;
        if (roleDefinitionId != null) {
            roleDefinitionIdObservable = Mono.just(roleDefinitionId);
        } else if (roleName != null) {
            roleDefinitionIdObservable = manager().roleDefinitions().getByScopeAndRoleNameAsync(scope(), roleName)
                    .map(roleDefinition -> roleDefinition.id());
        } else {
            throw new IllegalArgumentException("Please pass a non-null value for either role name or role definition ID");
        }

        return Mono.zip(objectIdObservable,
                    roleDefinitionIdObservable,
                    (objectId, roleDefinitionId) -> new RoleAssignmentCreateParameters().setPrincipalId(objectId).setRoleDefinitionId(roleDefinitionId))
                .flatMap(roleAssignmentPropertiesInner -> manager().roleInner().roleAssignments()
                .createAsync(scope(), name(), roleAssignmentPropertiesInner)
                .retryWhen(throwableFlux -> throwableFlux.zipWith(Flux.range(1, 30), (throwable, integer) -> {
                    if (throwable instanceof  CloudException) {
                        CloudException cloudException = (CloudException) throwable;
                        String exceptionMessage = cloudException.getMessage().toLowerCase();
                        if (exceptionMessage.contains("principalnotfound") || exceptionMessage.contains("does not exist in the directory")) {
                            // ref: https://github.com/Azure/azure-cli/blob/dev/src/command_modules/azure-cli-role/azure/cli/command_modules/role/custom.py#L1048-L1065
                            return integer;
                        } else {
                            throw Exceptions.propagate(throwable);
                        }
                    } else {
                        throw Exceptions.propagate(throwable);
                    }
                }).flatMap(i -> Mono.delay(SdkContext.getDelayDuration(Duration.ofSeconds(i)))))).map(innerToFluentMap(this));
    }

    @Override
    protected Mono<RoleAssignmentInner> getInnerAsync() {
        return manager.roleInner().roleAssignments().getAsync(scope(), name());
    }

    @Override
    public String scope() {
        return inner().getScope();
    }

    @Override
    public String roleDefinitionId() {
        return inner().getRoleDefinitionId();
    }

    @Override
    public String principalId() {
        return inner().getPrincipalId();
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
        this.inner().setScope(scope);
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

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }
}
