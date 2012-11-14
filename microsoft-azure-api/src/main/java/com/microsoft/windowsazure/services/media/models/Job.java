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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.MediaBatchOperations;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultActionOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityActionOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityBatchOperation;
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

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Jobs";

    // Prevent instantiation
    /**
     * Instantiates a new job.
     */
    private Job() {
    }

    /**
     * Creates the.
     * 
     * @return the creator
     */
    public static Creator create() {
        return new Creator();
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends EntityOperationSingleResultBase<JobInfo> implements
            EntityCreationOperation<JobInfo> {

        /** The start time. */
        private Date startTime;

        /** The name. */
        private String name;

        /** The priority. */
        private Integer priority;

        /** The input media assets. */
        private final List<String> inputMediaAssets;

        /** The service uri. */
        private URI serviceUri;

        /** The task create batch operations. */
        private final List<Task.CreateBatchOperation> taskCreateBatchOperations;

        /**
         * Instantiates a new creator.
         */
        public Creator() {
            super(ENTITY_SET, JobInfo.class);
            this.inputMediaAssets = new ArrayList<String>();
            this.taskCreateBatchOperations = new ArrayList<Task.CreateBatchOperation>();
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() throws ServiceException {
            MediaBatchOperations mediaBatchOperations = null;

            try {
                mediaBatchOperations = new MediaBatchOperations(serviceUri);
            }
            catch (JAXBException e) {
                throw new ServiceException(e);
            }
            catch (ParserConfigurationException e) {
                throw new ServiceException(e);
            }

            CreateBatchOperation createJobBatchOperation = CreateBatchOperation.create(this);

            mediaBatchOperations.addOperation(createJobBatchOperation);
            for (Task.CreateBatchOperation taskCreateBatchOperation : taskCreateBatchOperations) {
                mediaBatchOperations.addOperation(taskCreateBatchOperation);
            }

            MimeMultipart mimeMultipart;
            try {
                mimeMultipart = mediaBatchOperations.getMimeMultipart();
            }
            catch (MessagingException e) {
                throw new ServiceException(e);
            }
            catch (IOException e) {
                throw new ServiceException(e);
            }
            catch (JAXBException e) {
                throw new ServiceException(e);
            }
            return mimeMultipart;
        }

        /**
         * Adds the task creator.
         * 
         * @param taskCreateBatchOperation
         *            the task create batch operation
         * @return the creator
         */
        public Creator addTaskCreator(Task.CreateBatchOperation taskCreateBatchOperation) {
            this.taskCreateBatchOperations.add(taskCreateBatchOperation);
            return this;
        }

        /**
         * Set the name of the job to be created.
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
         * @return the creator
         */
        public Creator setPriority(Integer priority) {
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
         * @return the creator
         */
        public Creator setStartTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        /**
         * Gets the input media assets.
         * 
         * @return the input media assets
         */
        public List<String> getInputMediaAssets() {
            return inputMediaAssets;
        }

        /**
         * Gets the task creators.
         * 
         * @return the task creators
         */
        public List<Task.CreateBatchOperation> getTaskCreators() {
            return this.taskCreateBatchOperations;
        }

        /**
         * Adds the input media asset.
         * 
         * @param assetId
         *            the asset id
         * @return the creator
         */
        public Creator addInputMediaAsset(String assetId) {
            this.inputMediaAssets.add(assetId);
            return this;
        }
    }

    /**
     * The Class CreateBatchOperation.
     */
    public static class CreateBatchOperation extends EntityBatchOperation {

        /**
         * Creates the.
         * 
         * @param creator
         *            the creator
         * @return the creates the batch operation
         */
        public static CreateBatchOperation create(Creator creator) {
            CreateBatchOperation createBatchOperation = new CreateBatchOperation();
            return createBatchOperation;
        }

        private JobInfo jobInfo;

        public CreateBatchOperation setJobInfo(JobInfo jobInfo) {
            this.jobInfo = jobInfo;
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
     * Create an operation that will list all the jobs which match the given query parameters.
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
     * Create an operation to delete the given job.
     * 
     * @param jobId
     *            id of job to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String jobId) {
        return new DefaultDeleteOperation(ENTITY_SET, jobId);
    }

    /**
     * Cancel.
     * 
     * @param jobId
     *            the job id
     * @return the entity action operation
     */
    public static EntityActionOperation cancel(String jobId) {
        return new DefaultActionOperation(ENTITY_SET, "Cancel")
                .addQueryParameter("jobId", String.format("'%s'", jobId));
    }
}
