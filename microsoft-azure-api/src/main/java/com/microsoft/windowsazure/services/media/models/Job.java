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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.print.attribute.standard.JobState;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.CreateJobOperation;
import com.microsoft.windowsazure.services.media.implementation.CreateTaskOperation;
import com.microsoft.windowsazure.services.media.implementation.MediaBatchOperations;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.DefaultListOperation;
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
        private String name;
        private String alternateId;
        private EncryptionOption options;
        private JobState state;

        public Creator() {
            super(ENTITY_SET, JobInfo.class);
        }

        @Override
        public Object getRequestContents() {
            MediaBatchOperations mediaBatchOperations = null;
            try {
                mediaBatchOperations = new MediaBatchOperations(getBaseURI());
            }
            catch (JAXBException e) {
                throw new ServiceException(e);
            }
            catch (ParserConfigurationException e) {
                throw new ServiceException(e);
            }

            CreateJobOperation createJobOperation = createCreateJobOperation(createJobOptions);

            mediaBatchOperations.addOperation(createJobOperation);
            for (CreateTaskOptions createTaskOptionsInstance : createTaskOptions) {
                CreateTaskOperation createTaskOperation = createTaskOperation(createTaskOptionsInstance);
                mediaBatchOperations.addOperation(createTaskOperation);
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

        /**
         * Sets the alternate id of the job to be created.
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

        public Creator setState(JobState state) {
            this.state = state;
            return this;
        }

        public Object setPriority(int i) {
            // TODO Auto-generated method stub
            return null;
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

    public static Object cancel(String jobId) {
        return null;
    }
}
