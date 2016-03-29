/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models;

import java.util.List;

/**
 * Role definition permissions.
 */
public class Permission {
    /**
     * Role definition allowed actions.
     */
    private List<String> actions;

    /**
     * Role definition denied actions.
     */
    private List<String> notActions;

    /**
     * Get the actions value.
     *
     * @return the actions value
     */
    public List<String> getActions() {
        return this.actions;
    }

    /**
     * Set the actions value.
     *
     * @param actions the actions value to set
     */
    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    /**
     * Get the notActions value.
     *
     * @return the notActions value
     */
    public List<String> getNotActions() {
        return this.notActions;
    }

    /**
     * Set the notActions value.
     *
     * @param notActions the notActions value to set
     */
    public void setNotActions(List<String> notActions) {
        this.notActions = notActions;
    }

}
