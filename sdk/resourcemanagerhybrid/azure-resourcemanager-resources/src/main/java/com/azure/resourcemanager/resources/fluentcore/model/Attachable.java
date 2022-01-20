// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * The final stage of the child object definition, at which it can be attached to the parent.
 *
 * @param <ParentT> the stage of the parent definition to return to after attaching this definition.
 */
public interface Attachable<ParentT> {
    /**
     * Attaches this child object's definition to its parent's definition.
     *
     * @return the next stage of the parent object's definition
     */
    ParentT attach();

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     *
     * @param <ParentT> the parent definition
     */
    interface InDefinition<ParentT> {
        /**
         * Attaches the child definition to the parent resource definiton.
         *
         * @return the next stage of the parent definition
         */
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     *
     * @param <ParentT> the parent definition
     */
    interface InDefinitionAlt<ParentT> {
        /**
         * Attaches the child definition to the parent resource definition.
         *
         * @return the next stage of the parent definition
         */
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     *
     * @param <ParentT> the parent definition
     */
    interface InUpdate<ParentT> {
        /**
         * Attaches the child definition to the parent resource update.
         *
         * @return the next stage of the parent definition
         */
        ParentT attach();
    }

    /**
     * The final stage of the child object definition, as which it can be attached to the parent.
     *
     * @param <ParentT> the parent definition
     */
    interface InUpdateAlt<ParentT> {
        /**
         * Attaches the child definition to the parent resource update.
         *
         * @return the next stage of the parent definition
         */
        ParentT attach();
    }
}
