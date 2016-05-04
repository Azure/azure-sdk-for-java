/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * AutoHealCustomAction - Describes the custom action to be executed
 * when an auto heal rule is triggered.
 */
public class AutoHealCustomAction {
    /**
     * Executable to be run.
     */
    private String exe;

    /**
     * Parameters for the executable.
     */
    private String parameters;

    /**
     * Get the exe value.
     *
     * @return the exe value
     */
    public String exe() {
        return this.exe;
    }

    /**
     * Set the exe value.
     *
     * @param exe the exe value to set
     * @return the AutoHealCustomAction object itself.
     */
    public AutoHealCustomAction setExe(String exe) {
        this.exe = exe;
        return this;
    }

    /**
     * Get the parameters value.
     *
     * @return the parameters value
     */
    public String parameters() {
        return this.parameters;
    }

    /**
     * Set the parameters value.
     *
     * @param parameters the parameters value to set
     * @return the AutoHealCustomAction object itself.
     */
    public AutoHealCustomAction setParameters(String parameters) {
        this.parameters = parameters;
        return this;
    }

}
