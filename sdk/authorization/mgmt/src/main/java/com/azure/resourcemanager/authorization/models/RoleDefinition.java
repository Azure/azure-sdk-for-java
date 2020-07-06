// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.inner.RoleDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.Set;

/** An immutable client-side representation of an Azure AD role definition. */
@Fluent
public interface RoleDefinition 
    extends HasInner<RoleDefinitionInner>, HasId, HasName, HasManager<AuthorizationManager> {
    /** @return the role name */
    String roleName();

    /** @return the role definition description */
    String description();

    /** @return the role type */
    String type();

    /** @return role definition permissions */
    Set<Permission> permissions();

    /** @return role definition assignable scopes */
    Set<String> assignableScopes();
}
