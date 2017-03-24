/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.NamespacesInner;

/**
 * Entry point to namespace authorization rules management API.
 */
@Fluent
@Beta
public interface NamespaceAuthorizationRules extends
        AuthorizationRules<NamespaceAuthorizationRule>,
        SupportsCreating<NamespaceAuthorizationRule.DefinitionStages.Blank>,
        HasInner<NamespacesInner> {
}