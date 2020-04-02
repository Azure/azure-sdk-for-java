/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.Indexable;

/**
 * Base interface used by child resources.
 *
 * @param <ParentT> parent interface
 */
@Fluent
public interface ChildResource<ParentT> extends
        Indexable,
        HasName,
        HasParent<ParentT> {
}
