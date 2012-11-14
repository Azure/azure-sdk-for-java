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

import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityBatchOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
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

    public static CreateBatchOperation create() {
        return new CreateBatchOperation();
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

    public static class CreateBatchOperation extends EntityBatchOperation {
        private String configuration;
        private String mediaProcessorId;
        private String name;
        private String taskBody;

        public CreateBatchOperation setConfiguration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public String getConfiguration() {
            return this.configuration;
        }

        public CreateBatchOperation setMediaProcessorId(String mediaProcessorId) {
            this.mediaProcessorId = mediaProcessorId;
            return this;
        }

        public CreateBatchOperation setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public CreateBatchOperation setTaskBody(String taskBody) {
            this.taskBody = taskBody;
            return this;
        }

        public String getTaskBody() {
            return this.taskBody;
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
