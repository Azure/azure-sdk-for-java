/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import com.azure.core.annotation.Fluent;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.model.Indexable;

/**
 * An immutable client-side representation of an Azure AD object.
 */
@Fluent
public interface ActiveDirectoryObject extends
        Indexable,
        HasId,
        HasName,
        HasManager<GraphRbacManager> {
}
