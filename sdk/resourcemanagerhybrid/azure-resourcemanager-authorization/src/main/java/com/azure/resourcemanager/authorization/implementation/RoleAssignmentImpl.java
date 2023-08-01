// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleAssignmentCreateParameters;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.authorization.fluent.models.RoleAssignmentInner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Locale;

/** Implementation for ServicePrincipal and its parent interfaces. */
class RoleAssignmentImpl extends CreatableImpl<RoleAssignment, RoleAssignmentInner, RoleAssignmentImpl>
    implements RoleAssignment, RoleAssignment.Definition {
    private AuthorizationManager manager;
    // Active Directory identify info
    private String objectId;
    private String userName;
    private String servicePrincipalName;
    // role info
    private String roleDefinitionId;
    private String roleName;
    private final ClientLogger logger = new ClientLogger(RoleAssignmentImpl.class);

    RoleAssignmentImpl(String name, RoleAssignmentInner innerObject, AuthorizationManager manager) {
        super(name, innerObject);
        this.manager = manager;
    }

    @Override
    public boolean isInCreateMode() {
        return innerModel().id() == null;
    }

    @Override
    public Mono<RoleAssignment> createResourceAsync() {
        Mono<String> objectIdObservable;
        if (objectId != null) {
            objectIdObservable = Mono.just(objectId);
        } else if (userName != null) {
            objectIdObservable = manager.users().getByNameAsync(userName).map(user -> user.id());
        } else if (servicePrincipalName != null) {
            objectIdObservable = manager.servicePrincipals().getByNameAsync(servicePrincipalName).map(sp -> sp.id());
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Please pass a non-null value for either object Id, user, group, or service principal"));
        }

        Mono<String> roleDefinitionIdObservable;
        if (roleDefinitionId != null) {
            roleDefinitionIdObservable = Mono.just(roleDefinitionId);
        } else if (roleName != null) {
            roleDefinitionIdObservable =
                manager()
                    .roleDefinitions()
                    .getByScopeAndRoleNameAsync(scope(), roleName)
                    .map(roleDefinition -> roleDefinition.id());
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Please pass a non-null value for either role name or role definition ID"));
        }

        return Mono
            .zip(
                objectIdObservable,
                roleDefinitionIdObservable,
                (objectId, roleDefinitionId) ->
                    new RoleAssignmentCreateParameters()
                        .withPrincipalId(objectId)
                        .withRoleDefinitionId(roleDefinitionId))
            .flatMap(
                roleAssignmentPropertiesInner ->
                    manager()
                        .roleServiceClient()
                        .getRoleAssignments()
                        .createAsync(scope(), name(), roleAssignmentPropertiesInner)
                        .retryWhen(Retry.withThrowable(
                            throwableFlux ->
                                throwableFlux
                                    .zipWith(
                                        Flux.range(1, 30),
                                        (throwable, integer) -> {
                                            if (throwable instanceof ManagementException) {
                                                ManagementException managementException =
                                                    (ManagementException) throwable;
                                                String exceptionMessage =
                                                    managementException.getMessage().toLowerCase(Locale.ROOT);
                                                if (exceptionMessage.contains("principalnotfound")
                                                    || exceptionMessage.contains("does not exist in the directory")) {
                                                    /*
                                                     * ref:
                                                     * https://github.com/Azure/azure-cli/blob/dev/src/command_modules/azure-cli-role/azure/cli/command_modules/role/custom.py#L1048-L1065
                                                     */
                                                    return integer;
                                                } else {
                                                    throw logger.logExceptionAsError(Exceptions.propagate(throwable));
                                                }
                                            } else {
                                                throw logger.logExceptionAsError(Exceptions.propagate(throwable));
                                            }
                                        })
                                    .flatMap(i -> Mono.delay(ResourceManagerUtils.InternalRuntimeContext
                                        .getDelayDuration(Duration.ofSeconds(i)))))))
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<RoleAssignmentInner> getInnerAsync() {
        return manager.roleServiceClient().getRoleAssignments().getAsync(scope(), name());
    }

    @Override
    public String scope() {
        return innerModel().scope();
    }

    @Override
    public String roleDefinitionId() {
        return innerModel().roleDefinitionId();
    }

    @Override
    public String principalId() {
        return innerModel().principalId();
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
        this.innerModel().withScope(scope);
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
        return innerModel().id();
    }

    @Override
    public AuthorizationManager manager() {
        return this.manager;
    }
}
