/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

public interface ResourcesInGroup<T, DefinitionT> extends
    SupportsListing<T>,
    SupportsCreating<DefinitionT>,
    SupportsDeleting {}
