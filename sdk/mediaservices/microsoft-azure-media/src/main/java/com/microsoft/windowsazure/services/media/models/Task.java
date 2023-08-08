/**
 * Copyright Microsoft Corporation
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

import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityBatchOperation;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Task entities.
 * 
 */
public final class Task {

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
     * @return the creates the batch operation
     */
    public static CreateBatchOperation create(String mediaProcessorId,
            String taskBody) {
        return new CreateBatchOperation(mediaProcessorId, taskBody);
    }

    /**
     * Create an operation that will list all the tasks.
     * 
     * @return The list operation
     */
    public static DefaultListOperation<TaskInfo> list() {
        return new DefaultListOperation<TaskInfo>(ENTITY_SET,
                new GenericType<ListResult<TaskInfo>>() {
                });
    }

    /**
     * Create an operation that will list the tasks pointed to by the given
     * link.
     * 
     * @param link
     *            link to tasks
     * @return the list operation.
     */
    public static DefaultListOperation<TaskInfo> list(LinkInfo<TaskInfo> link) {
        return new DefaultListOperation<TaskInfo>(link.getHref(),
                new GenericType<ListResult<TaskInfo>>() {
                });
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
         */
        public CreateBatchOperation(String mediaProcessorId, String taskBody) {
            this.setVerb("POST");
            taskType = new TaskType();
            addContentObject(taskType);
            this.taskType.setMediaProcessorId(mediaProcessorId);
            this.taskType.setTaskBody(taskBody);
        }

        /**
         * Sets the options.
         * 
         * @param options
         *            the options
         * @return the creates the batch operation
         */
        public CreateBatchOperation setOptions(TaskOption options) {
            this.taskType.setOptions(options.getCode());
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
         * Sets the encryption version.
         * 
         * @param encryptionVersion
         *            the encryption version
         * @return the creates the batch operation
         */
        public CreateBatchOperation setEncryptionVersion(
                String encryptionVersion) {
            this.taskType.setEncryptionVersion(encryptionVersion);
            return this;
        }

        /**
         * Sets the initialization vector.
         * 
         * @param initializationVector
         *            the initialization vector
         * @return the creates the batch operation
         */
        public CreateBatchOperation setInitializationVector(
                String initializationVector) {
            this.taskType.setInitializationVector(initializationVector);
            return this;
        }

    }

}
