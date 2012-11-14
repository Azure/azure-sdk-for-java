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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.CreateJobOperation;
import com.microsoft.windowsazure.services.media.implementation.MediaBatchOperations;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultActionOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Job entities.
 * 
 */
public class Job {
    private static final String ENTITY_SET = "Jobs";

    // Prevent instantiation
    private Job() {
    }

    public static Creator create() {
        return new Creator();
    }

    public static class Creator extends EntityOperationSingleResultBase<JobInfo> implements
            EntityCreationOperation<JobInfo> {
        private Date startTime;
        private String name;
        private Integer priority;
        private final List<String> inputMediaAssets;
        private URI serviceUri;
        private final List<Task.Creator> taskCreators;

        private CreateJobOperation createCreateJobOperation() {
            CreateJobOperation createJobOperation = new CreateJobOperation(serviceUri);
            return createJobOperation;
        }

        public Creator() {
            super(ENTITY_SET, JobInfo.class);
            this.inputMediaAssets = new ArrayList<String>();
            this.taskCreators = new ArrayList<Task.Creator>();
        }

        @Override
        public Object getRequestContents() {
            MediaBatchOperations mediaBatchOperations = null;

            mediaBatchOperations = new MediaBatchOperations(serviceUri);

            CreateJobOperation createJobOperation = createCreateJobOperation();

            mediaBatchOperations.addOperation(createJobOperation);
            for (EntityCreationOperation<Task> taskCreator : taskCreateOperations) {
                mediaBatchOperations.addOperation(taskCreator);
            }

            MimeMultipart mimeMultipart;
            mimeMultipart = mediaBatchOperations.getMimeMultipart();
            return mimeMultipart;
        }

        public Creator addTaskCreator(Task.Creator taskCreator) {
            this.taskCreators.add(taskCreator);
            return this;
        }

        /**
         * Set the name of the job to be created
         * 
         * @param name
         *            The name
         * @return The creator object (for call chaining)
         */
        public Creator setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public Creator setPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Integer getPriority() {
            return this.priority;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Creator setStartTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public List<String> getInputMediaAssets() {
            return inputMediaAssets;
        }

        public List<Task.Creator> getTaskCreators() {
            return this.taskCreators;
        }

        public Creator addInputMediaAsset(String assetId) {
            this.inputMediaAssets.add(assetId);
            return this;
        }
    }

    /**
     * Create an operation object that will get the state of the given job.
     * 
     * @param jobId
     *            id of job to retrieve
     * @return the get operation
     */
    public static EntityGetOperation<JobInfo> get(String jobId) {
        return new DefaultGetOperation<JobInfo>(ENTITY_SET, jobId, JobInfo.class);
    }

    /**
     * Create an operation that will list all the jobs.
     * 
     * @return The list operation
     */
    public static EntityListOperation<JobInfo> list() {
        return new DefaultListOperation<JobInfo>(ENTITY_SET, new GenericType<ListResult<JobInfo>>() {
        });
    }

    /**
     * Create an operation that will list all the jobs which match the given query parameters
     * 
     * @param queryParameters
     *            query parameters to pass to the server.
     * @return the list operation.
     */
    public static EntityListOperation<JobInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<JobInfo>(ENTITY_SET, new GenericType<ListResult<JobInfo>>() {
        }, queryParameters);
    }

    /**
     * Create an operation to delete the given job
     * 
     * @param jobId
     *            id of job to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String jobId) {
        return new DefaultDeleteOperation(ENTITY_SET, jobId);
    }

    public static EntityActionOperation cancel(String jobId) {
        return new DefaultActionOperation(ENTITY_SET, "Cancel")
                .addQueryParameter("jobId", String.format("'%s'", jobId));
    }
}
