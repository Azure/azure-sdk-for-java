/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.PermissionInner;
import com.microsoft.azure.management.graphrbac.implementation.RoleDefinitionInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import java.util.Set;

/**
 * An immutable client-side representation of an Azure AD role definition.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta(SinceVersion.V1_1_0)
public interface RoleDefinition extends
        HasInner<RoleDefinitionInner>,
        HasId,
        HasName,
        HasManager<GraphRbacManager> {
    /**
     * @return the role name
     */
    String roleName();

    /**
     * @return the role definition description
     */
    String description();

    /**
     * @return the role type
     */
    String type();

    /**
     * @return role definition permissions
     */
    Set<PermissionInner> permissions();

    /**
     * @return role definition assignable scopes
     */
    Set<String> assignableScopes();
}
