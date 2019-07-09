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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityBatchOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.implementation.MediaBatchOperations;
import com.microsoft.windowsazure.services.media.implementation.content.JobNotificationSubscriptionType;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Job entities.
 * 
 */
public final class Job {

    /** The Constant ENTITY_SET. */
    private static final String ENTITY_SET = "Jobs";

    // Prevent instantiation
    /**
     * Instantiates a new job.
     */
    private Job() {
    }

    /**
     * Creates an operation to create a new job.
     * 
     * @return the creator
     */
    public static Creator create() {
        return new Creator();
    }

    /**
     * The Class Creator.
     */
    public static class Creator extends
            EntityOperationSingleResultBase<JobInfo> implements
            EntityCreateOperation<JobInfo> {

        /** The name. */
        private String name;

        /** The priority. */
        private Integer priority;

        /** The input media assets. */
        private final List<String> inputMediaAssets;

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

        /** The job notification subscriptions. */
        private final List<JobNotificationSubscription> jobNotificationSubscriptions = new ArrayList<JobNotificationSubscription>();

        /**
         * Builds the mime multipart.
         * 
         * @param serviceUri
         *            the service uri
         */
        private void buildMimeMultipart(URI serviceUri) {
            mediaBatchOperations = null;
            CreateBatchOperation createJobBatchOperation = CreateBatchOperation
                    .create(serviceUri, this);

            try {
                mediaBatchOperations = new MediaBatchOperations(serviceUri);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }

            mediaBatchOperations.addOperation(createJobBatchOperation);
            for (Task.CreateBatchOperation taskCreateBatchOperation : taskCreateBatchOperations) {
                mediaBatchOperations.addOperation(taskCreateBatchOperation);
            }

            try {
                mimeMultipart = mediaBatchOperations.getMimeMultipart();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }

            this.contentType = mediaBatchOperations.getContentType();
            this.fresh = false;
        }

        /**
         * Instantiates a new creator.
         * 
         */
        public Creator() {
            super(ENTITY_SET, JobInfo.class);
            this.inputMediaAssets = new ArrayList<String>();
            this.taskCreateBatchOperations = new ArrayList<Task.CreateBatchOperation>();
            this.fresh = true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityCreateOperation#getRequestContents()
         */
        @Override
        public Object getRequestContents() throws ServiceException {
            if (fresh) {
                buildMimeMultipart(getProxyData().getServiceUri());
            }
            return mimeMultipart;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityOperationSingleResultBase#getResponseClass()
         */
        @Override
        public Class<?> getResponseClass() {
            return ClientResponse.class;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityCreateOperation#processResponse(java.lang.Object)
         */
        @Override
        public Object processResponse(Object clientResponse)
                throws ServiceException {
            try {
                this.mediaBatchOperations
                        .parseBatchResult((ClientResponse) clientResponse);
            } catch (IOException e) {
                throw new ServiceException(e);
            }
            JobInfo jobInfo = null;
            for (EntityBatchOperation entityBatchOperation : this.mediaBatchOperations
                    .getOperations()) {
                if (entityBatchOperation instanceof Job.CreateBatchOperation) {
                    jobInfo = ((Job.CreateBatchOperation) entityBatchOperation)
                            .getJobInfo();
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
        public Creator addTaskCreator(
                Task.CreateBatchOperation taskCreateBatchOperation) {
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

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityOperationBase#getContentType()
         */
        @Override
        public MediaType getContentType() {
            if (fresh) {
                buildMimeMultipart(getProxyData().getServiceUri());
            }
            return this.contentType;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityOperationBase#getUri()
         */
        @Override
        public String getUri() {
            return "$batch";
        }

        /**
         * Adds the job notification subscription.
         * 
         * @param jobNotificationSubscription
         *            the job notification subscription
         * @return the creator
         */
        public Creator addJobNotificationSubscription(
                JobNotificationSubscription jobNotificationSubscription) {
            this.jobNotificationSubscriptions.add(jobNotificationSubscription);
            this.fresh = true;
            return this;
        }

        /**
         * Gets the job notification subscription.
         * 
         * @return the job notification subscription
         */
        public List<JobNotificationSubscription> getJobNotificationSubscription() {
            return this.jobNotificationSubscriptions;
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
            this.setVerb("POST");
        }

        /**
         * Creates the.
         * 
         * @param serviceUri
         *            the service uri
         * @param creator
         *            the creator
         * @return the creates the batch operation
         */
        public static CreateBatchOperation create(URI serviceUri,
                Creator creator) {
            CreateBatchOperation createBatchOperation = new CreateBatchOperation(
                    serviceUri);

            JobType jobType = new JobType();
            jobType.setName(creator.getName());
            jobType.setPriority(creator.getPriority());
            for (JobNotificationSubscription jobNotificationSubscription : creator
                    .getJobNotificationSubscription()) {
                JobNotificationSubscriptionType jobNotificationSubscriptionType = new JobNotificationSubscriptionType();
                jobNotificationSubscriptionType
                        .setNotificationEndPointId(jobNotificationSubscription
                                .getNotificationEndPointId());
                jobNotificationSubscriptionType
                        .setTargetJobState(jobNotificationSubscription
                                .getTargetJobState().getCode());
                jobType.addJobNotificationSubscriptionType(jobNotificationSubscriptionType);
            }

            for (String inputMediaAsset : creator.getInputMediaAssets()) {
                createBatchOperation
                        .addLink(
                                "InputMediaAssets",
                                String.format("%s/Assets('%s')",
                                        createBatchOperation.getServiceUri()
                                                .toString(), inputMediaAsset),
                                "application/atom+xml;type=feed",
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
        return new DefaultGetOperation<JobInfo>(ENTITY_SET, jobId,
                JobInfo.class);
    }

    /**
     * Create an operation that will list all the jobs.
     * 
     * @return The list operation
     */
    public static DefaultListOperation<JobInfo> list() {
        return new DefaultListOperation<JobInfo>(ENTITY_SET,
                new GenericType<ListResult<JobInfo>>() {
                });
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
        return new DefaultActionOperation("CancelJob").addQueryParameter(
                "jobId", String.format("'%s'", jobId));
    }
}
