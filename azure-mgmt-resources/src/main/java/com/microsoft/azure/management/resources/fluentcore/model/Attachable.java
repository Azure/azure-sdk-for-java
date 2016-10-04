/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;

/**
 * The final stage of the child object definition, at which it can be attached to the parent, using {@link Attachable#attach()}.
 *
 * @param <ParentT> the parent definition {@link Attachable#attach()} returns to
 */
@LangDefinition(ContainerName = "ChildResourceActions")
public interface Attachable<ParentT> {
    /**
     * Attaches this child object's definition to its parent's definition.
     * @return the next stage of the parent object's definition
     */
    @Method
    ParentT attach();

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     * @param <ParentT> the parent definition
     */
    @LangDefinition(ContainerName = "~/ChildResource.Definition", ContainerFileName = "IDefinition")
    interface InDefinition<ParentT> {
        /**
         * Attaches the child definition to the parent resource definiton.
         * @return the next stage of the parent definition
         */
        @Method
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     * @param <ParentT> the parent definition
     */
    interface InDefinitionAlt<ParentT> {
        /**
         * Attaches the child definition to the parent resource definition.
         * @return the next stage of the parent definition
         */
        @Method
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     * @param <ParentT> the parent definition
     */
    @LangDefinition(ContainerName = "~/ChildResource.Update", ContainerFileName = "IUpdate")
    interface InUpdate<ParentT> {
        /**
         * Attaches the child definition to the parent resource update.
         * @return the next stage of the parent definition
         */
        @Method
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     * @param <ParentT> the parent definition
     */
    interface InUpdateAlt<ParentT> {
        /**
         * Attaches the child definition to the parent resource update.
         * @return the next stage of the parent definition
         */
        @Method
        ParentT attach();
    }
}
