/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Diagnostics for a hosting environment (App Service Environment).
 */
public class HostingEnvironmentDiagnosticsInner {
    /**
     * Name/identifier of the diagnostics.
     */
    private String name;

    /**
     * Diagnostics output.
     */
    private String diagnosicsOutput;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the HostingEnvironmentDiagnosticsInner object itself.
     */
    public HostingEnvironmentDiagnosticsInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the diagnosicsOutput value.
     *
     * @return the diagnosicsOutput value
     */
    public String diagnosicsOutput() {
        return this.diagnosicsOutput;
    }

    /**
     * Set the diagnosicsOutput value.
     *
     * @param diagnosicsOutput the diagnosicsOutput value to set
     * @return the HostingEnvironmentDiagnosticsInner object itself.
     */
    public HostingEnvironmentDiagnosticsInner withDiagnosicsOutput(String diagnosicsOutput) {
        this.diagnosicsOutput = diagnosicsOutput;
        return this;
    }

}
