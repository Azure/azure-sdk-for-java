/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * Base interface used by child resources.
 * @param <ParentT> parent interface
 */
@LangDefinition()
public interface ChildResource<ParentT> extends Indexable {
    /**
     * @return the name of this child object
     */
    String name();

    /**
     * @return the parent of this child object
     */
    ParentT parent();
}
