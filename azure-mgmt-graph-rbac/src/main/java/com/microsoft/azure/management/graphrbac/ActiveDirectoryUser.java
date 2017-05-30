/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.UserInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure AD user.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta
public interface ActiveDirectoryUser extends
        HasInner<UserInner>,
        HasId,
        HasName,
        HasManager<GraphRbacManager> {
    /**
     * @return Gets or sets user principal name.
     */
    String userPrincipalName();

    /**
     * @return Gets or sets user signIn name.
     */
    String signInName();

    /**
     * @return Gets or sets user mail.
     */
    String mail();

    /**
     * @return The mail alias for the user.
     */
    String mailNickname();
}
