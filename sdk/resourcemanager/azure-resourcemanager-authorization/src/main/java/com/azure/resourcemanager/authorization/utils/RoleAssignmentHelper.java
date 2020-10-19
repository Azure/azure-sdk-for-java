// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.utils;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

/**
 * A utility class to operate on role assignments for a resource with service principal (object id). This type is used
 * for internal implementations, client should not take dependency on this as the method signature and behaviour can
 * change in future releases.
 */
public class RoleAssignmentHelper {
    /**
     * A type that provide the service principal id (object id) and ARM resource id of the resource for which role
     * assignments needs to be done.
     */
    public interface IdProvider {
        /** @return the service principal id (object id) */
        String principalId();
        /** @return ARM resource id of the resource */
        String resourceId();
    }

    private static final String CURRENT_RESOURCE_GROUP_SCOPE = "CURRENT_RESOURCE_GROUP";

    private final AuthorizationManager authorizationManager;
    private final IdProvider idProvider;
    private final TaskGroup preRunTaskGroup;

    /**
     * Creates RoleAssignmentHelper.
     *
     * @param authorizationManager the graph rbac manager
     * @param taskGroup the pre-run task group after which role assignments create/remove tasks should run
     * @param idProvider the provider that provides service principal id and resource id
     */
    public RoleAssignmentHelper(
        final AuthorizationManager authorizationManager, TaskGroup taskGroup, IdProvider idProvider) {
        this.authorizationManager = Objects.requireNonNull(authorizationManager);
        this.idProvider = Objects.requireNonNull(idProvider);
        this.preRunTaskGroup = Objects.requireNonNull(taskGroup);
    }

    /**
     * Specifies that applications running on an Azure service with this identity requires the given access role with
     * scope of access limited to the current resource group that the identity resides.
     *
     * @param asRole access role to assigned to the identity
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withAccessToCurrentResourceGroup(BuiltInRole asRole) {
        return this.withAccessTo(CURRENT_RESOURCE_GROUP_SCOPE, asRole);
    }

    /**
     * Specifies that applications running on an Azure service with this identity requires the given access role with
     * scope of access limited to the ARM resource identified by the resource ID specified in the scope parameter.
     *
     * @param scope scope of the access represented in ARM resource ID format
     * @param asRole access role to assigned to the identity
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withAccessTo(final String scope, final BuiltInRole asRole) {
        FunctionalTaskItem creator =
            cxt -> {
                final String principalId = idProvider.principalId();
                if (principalId == null) {
                    return cxt.voidMono();
                }
                final String roleAssignmentName = authorizationManager.internalContext().randomUuid();
                final String resourceScope;
                if (scope.equals(CURRENT_RESOURCE_GROUP_SCOPE)) {
                    resourceScope = resourceGroupId(idProvider.resourceId());
                } else {
                    resourceScope = scope;
                }
                return authorizationManager
                    .roleAssignments()
                    .define(roleAssignmentName)
                    .forObjectId(principalId)
                    .withBuiltInRole(asRole)
                    .withScope(resourceScope)
                    .createAsync()
                    .cast(Indexable.class)
                    .onErrorResume(
                        throwable -> {
                            if (isRoleAssignmentExists(throwable)) {
                                return cxt.voidMono();
                            }
                            return Mono.error(throwable);
                        });
            };
        this.preRunTaskGroup.addPostRunDependent(creator, authorizationManager.internalContext());
        return this;
    }

    /**
     * Specifies that applications running on an Azure service with this identity requires the given access role with
     * scope of access limited to the current resource group that the identity resides.
     *
     * @param roleDefinitionId access role definition to assigned to the identity
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withAccessToCurrentResourceGroup(String roleDefinitionId) {
        return this.withAccessTo(CURRENT_RESOURCE_GROUP_SCOPE, roleDefinitionId);
    }

    /**
     * Specifies that applications running on an Azure service with this identity requires the access described in the
     * given role definition with scope of access limited to an ARM resource.
     *
     * @param scope scope of the access represented in ARM resource ID format
     * @param roleDefinitionId access role definition to assigned to the identity
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withAccessTo(final String scope, final String roleDefinitionId) {
        FunctionalTaskItem creator =
            cxt -> {
                final String principalId = idProvider.principalId();
                if (principalId == null) {
                    return cxt.voidMono();
                }
                final String roleAssignmentName = authorizationManager.internalContext().randomUuid();
                final String resourceScope;
                if (scope.equals(CURRENT_RESOURCE_GROUP_SCOPE)) {
                    resourceScope = resourceGroupId(idProvider.resourceId());
                } else {
                    resourceScope = scope;
                }
                return authorizationManager
                    .roleAssignments()
                    .define(roleAssignmentName)
                    .forObjectId(principalId)
                    .withRoleDefinition(roleDefinitionId)
                    .withScope(resourceScope)
                    .createAsync()
                    .cast(Indexable.class)
                    .onErrorResume(
                        throwable -> {
                            if (isRoleAssignmentExists(throwable)) {
                                return cxt.voidMono();
                            }
                            return Mono.error(throwable);
                        });
            };
        this.preRunTaskGroup.addPostRunDependent(creator, authorizationManager.internalContext());
        return this;
    }

    /**
     * Specifies that an access role assigned to the identity should be removed.
     *
     * @param roleAssignment a role assigned to the identity
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withoutAccessTo(final RoleAssignment roleAssignment) {
        String principalId = roleAssignment.principalId();
        if (principalId == null || !principalId.equalsIgnoreCase(idProvider.principalId())) {
            return this;
        }
        FunctionalTaskItem remover =
            cxt -> authorizationManager.roleAssignments().deleteByIdAsync(roleAssignment.id()).then(cxt.voidMono());
        this.preRunTaskGroup.addPostRunDependent(remover);
        return this;
    }

    /**
     * Specifies that an access role assigned to the identity should be removed.
     *
     * @param scope the scope of the role assignment
     * @param asRole the role of the role assignment
     * @return RoleAssignmentHelper
     */
    public RoleAssignmentHelper withoutAccessTo(final String scope, final BuiltInRole asRole) {
        FunctionalTaskItem remover =
            cxt ->
                authorizationManager
                    .roleDefinitions()
                    .getByScopeAndRoleNameAsync(scope, asRole.toString())
                    .flatMap(
                        (Function<RoleDefinition, Mono<RoleAssignment>>)
                        roleDefinition ->
                            authorizationManager
                                .roleAssignments()
                                .listByScopeAsync(scope)
                                .filter(
                                    roleAssignment -> {
                                        if (roleDefinition != null && roleAssignment != null) {
                                            return roleAssignment
                                                    .roleDefinitionId()
                                                    .equalsIgnoreCase(roleDefinition.id())
                                                && roleAssignment
                                                    .principalId()
                                                    .equalsIgnoreCase(idProvider.principalId());
                                        } else {
                                            return false;
                                        }
                                    })
                                .last())
                    .flatMap(
                        (Function<RoleAssignment, Mono<Indexable>>)
                        roleAssignment ->
                            authorizationManager
                                .roleAssignments()
                                .deleteByIdAsync(roleAssignment.id())
                                .then(cxt.voidMono()));
        this.preRunTaskGroup.addPostRunDependent(remover);
        return this;
    }

    /**
     * This method returns ARM id of the resource group from the given ARM id of a resource in the resource group.
     *
     * @param id ARM id
     * @return the ARM id of resource group
     */
    private static String resourceGroupId(String id) {
        final ResourceId resourceId = ResourceId.fromString(id);
        final StringBuilder builder = new StringBuilder();
        builder
            .append("/subscriptions/")
            .append(resourceId.subscriptionId())
            .append("/resourceGroups/")
            .append(resourceId.resourceGroupName());
        return builder.toString();
    }

    /**
     * Checks whether the given exception indicates role assignment already exists or not.
     *
     * @param throwable the exception to check
     * @return true if role assignment exists, false otherwise
     */
    private static boolean isRoleAssignmentExists(Throwable throwable) {
        if (throwable instanceof ManagementException) {
            ManagementException exception = (ManagementException) throwable;
            if (exception.getValue() != null
                && exception.getValue().getCode() != null
                && exception.getValue().getCode().equalsIgnoreCase("RoleAssignmentExists")) {
                return true;
            }
        }
        return false;
    }
}
