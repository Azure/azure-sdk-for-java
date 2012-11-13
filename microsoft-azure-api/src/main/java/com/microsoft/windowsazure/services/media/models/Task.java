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
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationBase;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Task entities.
 * 
 */
public class Task {
    private static final String ENTITY_SET = "Tasks";

    // Prevent instantiation
    private Task() {
    }

    public static Creator create() {
        return new Creator();
    }

    public static class Creator extends EntityOperationSingleResultBase<TaskInfo> implements
            EntityCreationOperation<TaskInfo> {
        private String name;
        private String alternateId;
        private EncryptionOption options;
        private TaskState state;

        public Creator() {
            super(ENTITY_SET, TaskInfo.class);
        }

        @Override
        public Object getRequestContents() {
            TaskType taskType = new TaskType();
            taskType.setName(name);
            taskType.setAlternateId(alternateId);
            if (options != null) {
                taskType.setOptions(options.getCode());
            }
            if (state != null) {
                taskType.setState(state.getCode());
            }
            return taskType;
        }

        /**
         * Set the name of the task to be created
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the alternate id of the task to be created.
         * 
         * @param alternateId
         *            The id
         * 
         * @return The creator object (for call chaining)
         */
        public Creator setAlternateId(String alternateId) {
            this.alternateId = alternateId;
            return this;
        }

        public Creator setOptions(EncryptionOption options) {
            this.options = options;
            return this;
        }

        public Creator setState(TaskState state) {
            this.state = state;
            return this;
        }

        public Object setConfiguration(String string) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * Create an operation object that will get the state of the given task.
     * 
     * @param taskId
     *            id of task to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<TaskInfo> get(String taskId) {
        return new DefaultGetOperation<TaskInfo>(ENTITY_SET, taskId, TaskInfo.class);
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
     * Create an operation that will list all the tasks which match the given query parameters
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
     * Create an operation that will update the given task
     * 
     * @param taskId
     *            id of the task to update
     * @return the update operation
     */
    public static Updater update(String taskId) {
        return new Updater(taskId);
    }

    public static class Updater extends EntityOperationBase implements EntityUpdateOperation {
        private String name;
        private String alternateId;

        protected Updater(String taskId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET, taskId));
        }

        @Override
        public Object getRequestContents() {
            TaskType taskType = new TaskType();
            taskType.setName(name);
            taskType.setAlternateId(alternateId);
            return taskType;
        }

        /**
         * Sets new name for task
         * 
         * @param name
         *            The new name
         * @return Updater instance
         */
        public Updater setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets new alternate id for task
         * 
         * @param alternateId
         *            the new alternate id
         * @return Updater instance
         */
        public Updater setAlternateId(String alternateId) {
            this.alternateId = alternateId;
            return this;
        }
    }

    /**
     * Create an operation to delete the given task
     * 
     * @param taskId
     *            id of task to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String taskId) {
        return new DefaultDeleteOperation(ENTITY_SET, taskId);
    }
}
