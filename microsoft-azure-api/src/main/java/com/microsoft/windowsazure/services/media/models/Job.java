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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.MediaBatchOperations;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;
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
import com.sun.jersey.api.client.ClientResponse;
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
     * @param serviceUri
     *            the service uri
     * @return the creator
     */
    public static Creator create(URI serviceUri) {
        return new Creator(serviceUri);
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

        /** The content type. */
        private MediaType contentType;

        /** The task create batch operations. */
        private final List<Task.CreateBatchOperation> taskCreateBatchOperations;

        /** The fresh. */
        private Boolean fresh;

        /** The mime multipart. */
        private MimeMultipart mimeMultipart;;

        /** The media batch operations. */
        private MediaBatchOperations mediaBatchOperations;

        /**
         * Builds the mime multipart.
         * 
         * @throws ServiceException
         *             the service exception
         */
        private void buildMimeMultipart() {
            mediaBatchOperations = null;
            CreateBatchOperation createJobBatchOperation = CreateBatchOperation.create(this);

            try {
                mediaBatchOperations = new MediaBatchOperations(serviceUri);
            }
            catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }

            mediaBatchOperations.addOperation(createJobBatchOperation);
            for (Task.CreateBatchOperation taskCreateBatchOperation : taskCreateBatchOperations) {
                mediaBatchOperations.addOperation(taskCreateBatchOperation);
            }

            try {
                mimeMultipart = mediaBatchOperations.getMimeMultipart();
            }
            catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            this.contentType = mediaBatchOperations.getContentType();
            this.fresh = false;
        }

        /**
         * Instantiates a new creator.
         * 
         * @param serviceUri
         *            the service uri
         */
        public Creator(URI serviceUri) {
            super(ENTITY_SET, JobInfo.class);
            this.serviceUri = serviceUri;
            this.inputMediaAssets = new ArrayList<String>();
            this.taskCreateBatchOperations = new ArrayList<Task.CreateBatchOperation>();
            this.fresh = true;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() throws ServiceException {
            if (fresh) {
                buildMimeMultipart();
            }
            return mimeMultipart;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationSingleResultBase#getResponseClass()
         */
        @Override
        public Class getResponseClass() {
            return ClientResponse.class;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityCreationOperation#processResponse(java.lang.Object)
         */
        @Override
        public Object processResponse(Object clientResponse) throws ServiceException {
            try {
                this.mediaBatchOperations.parseBatchResult((ClientResponse) clientResponse);
            }
            catch (IOException e) {
                throw new ServiceException(e);
            }
            JobInfo jobInfo = null;
            for (EntityBatchOperation entityBatchOperation : this.mediaBatchOperations.getOperations()) {
                if (entityBatchOperation instanceof Job.CreateBatchOperation) {
                    jobInfo = ((Job.CreateBatchOperation) entityBatchOperation).getJobInfo();
                    break;
                }
            }
            return jobInfo;

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
            this.fresh = true;
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
            this.fresh = true;
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
            this.fresh = true;
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
            this.fresh = true;
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
            this.fresh = true;
            return this;
        }

        /**
         * Gets the service uri.
         * 
         * @return the service uri
         */
        public URI getServiceUri() {
            return this.serviceUri;
        }

        /**
         * Sets the service uri.
         * 
         * @param serviceUri
         *            the service uri
         * @return the creator
         */
        public Creator setServiceUri(URI serviceUri) {
            this.serviceUri = serviceUri;
            this.fresh = true;
            return this;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationBase#getContentType()
         */
        @Override
        public MediaType getContentType() throws ServiceException {
            if (fresh) {
                buildMimeMultipart();
            }
            return this.contentType;
        }

        /* (non-Javadoc)
         * @see com.microsoft.windowsazure.services.media.implementation.entities.EntityOperationBase#getUri()
         */
        @Override
        public String getUri() {
            return "$batch";
        }
    }

    /**
     * The Class CreateBatchOperation.
     */
    public static class CreateBatchOperation extends EntityBatchOperation {

        /** The service uri. */
        private final URI serviceUri;

        /** The job info. */
        private JobInfo jobInfo;

        /**
         * Instantiates a new creates the batch operation.
         * 
         * @param serviceUri
         *            the service uri
         */
        public CreateBatchOperation(URI serviceUri) {
            this.serviceUri = serviceUri;
            this.verb = "POST";
        }

        /**
         * Creates the.
         * 
         * @param creator
         *            the creator
         * @return the creates the batch operation
         */
        public static CreateBatchOperation create(Creator creator) {
            CreateBatchOperation createBatchOperation = new CreateBatchOperation(creator.getServiceUri());

            JobType jobType = new JobType();
            jobType.setStartTime(creator.getStartTime());
            jobType.setName(creator.getName());
            jobType.setPriority(creator.getPriority());

            for (String inputMediaAsset : creator.getInputMediaAssets()) {
                createBatchOperation.addLink("InputMediaAssets", String.format("%s/Assets('%s')", createBatchOperation
                        .getServiceUri().toString(), inputMediaAsset.toString()), "application/atom+xml;type=feed",
                        "http://schemas.microsoft.com/ado/2007/08/dataservices/related/InputMediaAssets");
            }
            createBatchOperation.addContentObject(jobType);
            return createBatchOperation;
        }

        /**
         * Gets the service uri.
         * 
         * @return the service uri
         */
        public URI getServiceUri() {
            return this.serviceUri;
        }

        /**
         * Sets the job info.
         * 
         * @param jobInfo
         *            the job info
         * @return the creates the batch operation
         */
        public CreateBatchOperation setJobInfo(JobInfo jobInfo) {
            this.jobInfo = jobInfo;
            return this;
        }

        /**
         * Gets the job info.
         * 
         * @return the job info
         */
        public JobInfo getJobInfo() {
            return this.jobInfo;
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
        return new DefaultActionOperation("CancelJob").addQueryParameter("jobId", String.format("'%s'", jobId));
    }
}
