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

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.TaskType;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityBatchOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Task entities.
 * 
 */
public class Task {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Tasks";

    // Prevent instantiation
    /**
     * Instantiates a new task.
     */
    private Task() {
    }

    /**
     * Creates the.
     * 
     * @param mediaProcessorId
     *            the media processor id
     * @param taskBody
     *            the task body
     * @param options
     *            the options
     * @return the creates the batch operation
     */
    public static CreateBatchOperation create(String mediaProcessorId, String taskBody, int options) {
        return new CreateBatchOperation(mediaProcessorId, taskBody, options);
    }

    /**
     * Create an operation that will list all the tasks.
     * 
     * @return The list operation
     */
    public static EntityListOperation<TaskInfo> list() {
        return new DefaultListOperation<TaskInfo>(ENTITY_SET, new GenericType<ListResult<TaskInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the tasks which match the given query parameters.
     * 
     * @param queryParameters
     *            query parameters to pass to the server.
     * @return the list operation.
     */
    public static EntityListOperation<TaskInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<TaskInfo>(ENTITY_SET, new GenericType<ListResult<TaskInfo>>() {
        }, queryParameters);
    }

    /**
     * The Class CreateBatchOperation.
     */
    public static class CreateBatchOperation extends EntityBatchOperation {

        /** The task type. */
        private final TaskType taskType;

        /**
         * Instantiates a new creates the batch operation.
         * 
         * @param mediaProcessorId
         *            the media processor id
         * @param taskBody
         *            the task body
         * @param options
         *            the options
         */
        public CreateBatchOperation(String mediaProcessorId, String taskBody, int options) {
            this.verb = "POST";
            taskType = new TaskType();
            addContentObject(taskType);
            this.taskType.setMediaProcessorId(mediaProcessorId);
            this.taskType.setTaskBody(taskBody);
            this.taskType.setOptions(options);
        }

        /**
         * Gets the options.
         * 
         * @return the options
         */
        public int getOptions() {
            return this.taskType.getOptions();
        }

        /**
         * Sets the options.
         * 
         * @param options
         *            the options
         * @return the creates the batch operation
         */
        public CreateBatchOperation setOptions(int options) {
            this.taskType.setOptions(options);
            return this;
        }

        /**
         * Sets the configuration.
         * 
         * @param configuration
         *            the configuration
         * @return the creates the batch operation
         */
        public CreateBatchOperation setConfiguration(String configuration) {
            this.taskType.setConfiguration(configuration);
            return this;
        }

        /**
         * Gets the configuration.
         * 
         * @return the configuration
         */
        public String getConfiguration() {
            return this.taskType.getConfiguration();
        }

        /**
         * Sets the name.
         * 
         * @param name
         *            the name
         * @return the creates the batch operation
         */
        public CreateBatchOperation setName(String name) {
            this.taskType.setName(name);
            return this;
        }

        /**
         * Gets the name.
         * 
         * @return the name
         */
        public String getName() {
            return this.taskType.getName();
        }

        /**
         * Sets the task body.
         * 
         * @param taskBody
         *            the task body
         * @return the creates the batch operation
         */
        public CreateBatchOperation setTaskBody(String taskBody) {
            this.taskType.setTaskBody(taskBody);
            return this;
        }

        /**
         * Gets the task body.
         * 
         * @return the task body
         */
        public String getTaskBody() {
            return this.taskType.getTaskBody();
        }

        /**
         * Gets the media processor id.
         * 
         * @return the media processor id
         */
        public String getMediaProcessorId() {
            return this.taskType.getMediaProcessorId();
        }

        /**
         * Sets the media processor id.
         * 
         * @param mediaProcessorId
         *            the media processor id
         * @return the creates the batch operation
         */
        public CreateBatchOperation setMediaProcessorId(String mediaProcessorId) {
            this.taskType.setMediaProcessorId(mediaProcessorId);
            return this;
        }

        /**
         * Gets the priority.
         * 
         * @return the priority
         */
        public Integer getPriority() {
            return this.taskType.getPriority();
        }

        /**
         * Sets the priority.
         * 
         * @param priority
         *            the priority
         * @return the creates the batch operation
         */
        public CreateBatchOperation setPriority(int priority) {
            this.taskType.setPriority(priority);
            return this;
        }

        /**
         * Gets the encryption key id.
         * 
         * @return the encryption key id
         */
        public String getEncryptionKeyId() {
            return this.taskType.getEncryptionKeyId();
        }

        /**
         * Sets the encryption key id.
         * 
         * @param encryptionKeyId
         *            the encryption key id
         * @return the creates the batch operation
         */
        public CreateBatchOperation setEncryptionKeyId(String encryptionKeyId) {
            this.taskType.setEncryptionKeyId(encryptionKeyId);
            return this;
        }

        /**
         * Gets the encryption scheme.
         * 
         * @return the encryption scheme
         */
        public String getEncryptionScheme() {
            return this.taskType.getEncryptionScheme();
        }

        /**
         * Sets the encryption scheme.
         * 
         * @param encryptionScheme
         *            the encryption scheme
         * @return the creates the batch operation
         */
        public CreateBatchOperation setEncryptionScheme(String encryptionScheme) {
            this.taskType.setEncryptionScheme(encryptionScheme);
            return this;
        }

        /**
         * Gets the encryption version.
         * 
         * @return the encryption version
         */
        public String getEncryptionVersion() {
            return this.taskType.getEncryptionVersion();
        }

        /**
         * Sets the encryption version.
         * 
         * @param encryptionVersion
         *            the encryption version
         * @return the creates the batch operation
         */
        public CreateBatchOperation setEncryptionVersion(String encryptionVersion) {
            this.taskType.setEncryptionVersion(encryptionVersion);
            return this;
        }

        /**
         * Gets the initialization vector.
         * 
         * @return the initialization vector
         */
        public String getInitializationVector() {
            return this.taskType.getInitializationVector();
        }

        /**
         * Sets the initialization vector.
         * 
         * @param initializationVector
         *            the initialization vector
         * @return the creates the batch operation
         */
        public CreateBatchOperation setInitializationVector(String initializationVector) {
            this.taskType.setInitializationVector(initializationVector);
            return this;
        }
    }

    /**
     * Create an operation to delete the given task.
     * 
     * @param taskId
     *            id of task to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String taskId) {
        return new DefaultDeleteOperation(ENTITY_SET, taskId);
    }
}
