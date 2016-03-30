/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about the settings required for multi-instance task.
 */
public class MultiInstanceSettings {
    /**
     * Gets or sets the number of compute node instances used for
     * multi-instance task.
     */
    @JsonProperty(required = true)
    private int numberOfInstances;

    /**
     * Gets or sets the command to be run on the compute node instances to
     * setup coordination among the subtasks.
     */
    private String coordinationCommandLine;

    /**
     * Gets or sets a list of files that Batch will download on all subtasks.
     */
    private List<ResourceFile> commonResourceFiles;

    /**
     * Get the numberOfInstances value.
     *
     * @return the numberOfInstances value
     */
    public int getNumberOfInstances() {
        return this.numberOfInstances;
    }

    /**
     * Set the numberOfInstances value.
     *
     * @param numberOfInstances the numberOfInstances value to set
     */
    public void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    /**
     * Get the coordinationCommandLine value.
     *
     * @return the coordinationCommandLine value
     */
    public String getCoordinationCommandLine() {
        return this.coordinationCommandLine;
    }

    /**
     * Set the coordinationCommandLine value.
     *
     * @param coordinationCommandLine the coordinationCommandLine value to set
     */
    public void setCoordinationCommandLine(String coordinationCommandLine) {
        this.coordinationCommandLine = coordinationCommandLine;
    }

    /**
     * Get the commonResourceFiles value.
     *
     * @return the commonResourceFiles value
     */
    public List<ResourceFile> getCommonResourceFiles() {
        return this.commonResourceFiles;
    }

    /**
     * Set the commonResourceFiles value.
     *
     * @param commonResourceFiles the commonResourceFiles value to set
     */
    public void setCommonResourceFiles(List<ResourceFile> commonResourceFiles) {
        this.commonResourceFiles = commonResourceFiles;
    }

}
