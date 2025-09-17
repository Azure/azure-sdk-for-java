// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.models.RoleDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Set;

/** An immutable client-side representation of an Azure AD role definition. */
@Fluent
public interface RoleDefinition
    extends HasInnerModel<RoleDefinitionInner>, HasId, HasName, HasManager<AuthorizationManager> {
    /**
     * Gets the role name.
     *
     * @return the role name
     */
    String roleName();

    /**
     * Gets the role definition description.
     *
     * @return the role definition description
     */
    String description();

    /**
     * Gets the role type.
     *
     * @return the role type
     */
    String type();

    /**
     * Gets role definition permissions
     *
     * @return role definition permissions
     */
    Set<Permission> permissions();

    /**
     * Gets role definition assignable scopes
     *
     * @return role definition assignable scopes
     */
    Set<String> assignableScopes();
}
