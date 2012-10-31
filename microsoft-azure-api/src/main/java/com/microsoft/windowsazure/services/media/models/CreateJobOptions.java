/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media.models;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * The Class CreateJobOptions.
 */
public class CreateJobOptions {

    /** The start time. */
    private Date startTime;

    /** The name. */
    private String name;

    /** The priority. */
    private Integer priority;

    /** The input media assets. */
    private List<URI> inputMediaAssets;

    /** The output media assets. */
    private List<URI> outputMediaAssets;

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the creates the locator options
     */
    public CreateJobOptions setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the creates the job options
     */
    public CreateJobOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the priority.
     * 
     * @param priority
     *            the priority
     * @return the creates the job options
     */
    public CreateJobOptions setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * Sets the input media assets.
     * 
     * @param inputMediaAssets
     *            the input media assets
     * @return the creates the job options
     */
    public CreateJobOptions addInputMediaAsset(URI inputMediaAsset) {
        this.inputMediaAssets.add(inputMediaAsset);
        return this;
    }

    /**
     * Gets the input media assets.
     * 
     * @return the input media assets
     */
    public List<URI> getInputMediaAssets() {
        return this.inputMediaAssets;
    }

    /**
     * Sets the output media assets.
     * 
     * @param outputMediaAssets
     *            the output media assets
     * @return the creates the job options
     */
    public CreateJobOptions addOutputMediaAsset(URI outputMediaAsset) {
        this.outputMediaAssets.add(outputMediaAsset);
        return this;
    }

    /**
     * Gets the output media assets.
     * 
     * @return the output media assets
     */
    public List<URI> getOutputMediaAssets() {
        return this.outputMediaAssets;
    }

}
