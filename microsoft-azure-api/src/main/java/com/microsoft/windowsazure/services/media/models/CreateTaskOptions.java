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
 * The Class CreateTaskOptions.
 */
public class CreateTaskOptions {

    /** The configuration. */
    private String configuration;

    /** The media processor id. */
    private String mediaProcessorId;

    /** The name. */
    private String name;

    /** The priority. */
    private Integer priority;

    /** The start time. */
    private Date startTime;

    /** The task body. */
    private String taskBody;

    /** The encryption key id. */
    private String encryptionKeyId;

    /** The encryption scheme. */
    private String encryptionScheme;

    /** The encryption version. */
    private String encryptionVersion;

    /** The initialization vector. */
    private String initializationVector;

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
    public CreateTaskOptions setStartTime(Date startTime) {
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
    public CreateTaskOptions setName(String name) {
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
    public CreateTaskOptions setPriority(Integer priority) {
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
    public CreateTaskOptions setInputMediaAssets(List<URI> inputMediaAssets) {
        this.inputMediaAssets = inputMediaAssets;
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
    public CreateTaskOptions setOutputMediaAssets(List<URI> outputMediaAssets) {
        this.outputMediaAssets = outputMediaAssets;
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

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     * 
     * @param configuration
     *            the new configuration
     */
    public CreateTaskOptions setConfiguration(String configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Gets the media processor id.
     * 
     * @return the media processor id
     */
    public String getMediaProcessorId() {
        return mediaProcessorId;
    }

    /**
     * Sets the media processor id.
     * 
     * @param mediaProcessorId
     *            the new media processor id
     */
    public CreateTaskOptions setMediaProcessorId(String mediaProcessorId) {
        this.mediaProcessorId = mediaProcessorId;
        return this;
    }

    /**
     * Gets the task body.
     * 
     * @return the task body
     */
    public String getTaskBody() {
        return taskBody;
    }

    /**
     * Sets the task body.
     * 
     * @param taskBody
     *            the new task body
     */
    public CreateTaskOptions setTaskBody(String taskBody) {
        this.taskBody = taskBody;
        return this;
    }

    /**
     * Gets the encryption key id.
     * 
     * @return the encryption key id
     */
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    /**
     * Sets the encryption key id.
     * 
     * @param encryptionKeyId
     *            the new encryption key id
     */
    public CreateTaskOptions setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        return this;
    }

    /**
     * Gets the encryption scheme.
     * 
     * @return the encryption scheme
     */
    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    /**
     * Sets the encryption scheme.
     * 
     * @param encryptionScheme
     *            the new encryption scheme
     */
    public CreateTaskOptions setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    /**
     * Gets the encryption version.
     * 
     * @return the encryption version
     */
    public String getEncryptionVersion() {
        return encryptionVersion;
    }

    /**
     * Sets the encryption version.
     * 
     * @param encryptionVersion
     *            the new encryption version
     */
    public CreateTaskOptions setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
        return this;
    }

    /**
     * Gets the initialization vector.
     * 
     * @return the initialization vector
     */
    public String getInitializationVector() {
        return initializationVector;
    }

    /**
     * Sets the initialization vector.
     * 
     * @param initializationVector
     *            the new initialization vector
     */
    public CreateTaskOptions setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
        return this;
    }

    public CreateTaskOptions addInputMediaAsset(URI inputMediaAsset) {
        this.inputMediaAssets.add(inputMediaAsset);
        return this;
    }

    public CreateTaskOptions addOutputMediaAsset(URI outputMediaAsset) {
        this.outputMediaAssets.add(outputMediaAsset);
        return this;
    }

}
