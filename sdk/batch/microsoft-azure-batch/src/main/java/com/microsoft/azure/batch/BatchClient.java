// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.batch.auth.BatchCredentials;
import com.microsoft.azure.batch.interceptor.ClientRequestIdInterceptor;
import com.microsoft.azure.batch.protocol.BatchServiceClient;
import com.microsoft.azure.batch.protocol.implementation.BatchServiceClientImpl;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestException;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A client for an Azure Batch account, used to access the Batch service.
 */
public class BatchClient {

    private final BatchServiceClient protocolLayer;
    private final CertificateOperations certificateOperations;
    private final JobOperations jobOperations;
    private final TaskOperations taskOperations;
    private final JobScheduleOperations jobScheduleOperations;
    private final PoolOperations poolOperations;
    private final FileOperations fileOperations;
    private final ComputeNodeOperations computeNodeOperations;
    private final ApplicationOperations applicationOperations;
    private final AccountOperations accountOperations;
    private Collection<BatchClientBehavior> customBehaviors;

    /**
     * Gets the protocol layer service client that issues requests to the Azure Batch service.
     *
     * @return The protocol layer client.
     */
    public BatchServiceClient protocolLayer() {
        return this.protocolLayer;
    }

    private BatchClient(BatchCredentials credentials) {
        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(credentials.baseUrl())
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new ResponseBuilderFactory())
                .build();

        this.protocolLayer = new BatchServiceClientImpl(restClient).withBatchUrl(credentials.baseUrl());
        this.customBehaviors = new LinkedList<>();
        this.customBehaviors.add(new ClientRequestIdInterceptor());
        this.certificateOperations = new CertificateOperations(this, customBehaviors());
        this.jobOperations = new JobOperations(this, customBehaviors());
        this.taskOperations = new TaskOperations(this, customBehaviors());
        this.jobScheduleOperations = new JobScheduleOperations(this, customBehaviors());
        this.poolOperations = new PoolOperations(this, customBehaviors());
        this.fileOperations = new FileOperations(this, customBehaviors());
        this.applicationOperations = new ApplicationOperations(this, customBehaviors());
        this.accountOperations = new AccountOperations(this, customBehaviors());
        this.computeNodeOperations = new ComputeNodeOperations(this, customBehaviors());
    }

    private BatchClient(RestClient restClient, String baseUrl) {
        this.protocolLayer = new BatchServiceClientImpl(restClient).withBatchUrl(baseUrl);
        this.customBehaviors = new LinkedList<>();
        this.customBehaviors.add(new ClientRequestIdInterceptor());
        this.certificateOperations = new CertificateOperations(this, customBehaviors());
        this.jobOperations = new JobOperations(this, customBehaviors());
        this.taskOperations = new TaskOperations(this, customBehaviors());
        this.jobScheduleOperations = new JobScheduleOperations(this, customBehaviors());
        this.poolOperations = new PoolOperations(this, customBehaviors());
        this.fileOperations = new FileOperations(this, customBehaviors());
        this.applicationOperations = new ApplicationOperations(this, customBehaviors());
        this.accountOperations = new AccountOperations(this, customBehaviors());
        this.computeNodeOperations = new ComputeNodeOperations(this, customBehaviors());
    }

    /**
     * Creates an instance of {@link BatchClient} associated with the specified credentials.
     *
     * @param credentials A {@link BatchCredentials} object specifying the Batch account credentials.
     * @return The new {@link BatchClient} instance.
     */
    public static BatchClient open(BatchCredentials credentials) {
        return new BatchClient(credentials);
    }

    /**
     * Creates an instance of {@link BatchClient} associated with the specified restClient and baseurl.
     *
     * @param restClient A {@link RestClient} object specifying the definition of rest client used to
     *                   interact with batch endpoint.
     * @param baseUrl A  String object specifying the the batch end point.
     * @return The new {@link BatchClient} instance.
     */
    public static BatchClient open(RestClient restClient, String baseUrl) {
        return new BatchClient(restClient, baseUrl);
    }

    /**
     * Gets a {@link CertificateOperations} object for performing certificate-related operations on the associated account.
     *
     * @return An instance of the {@link CertificateOperations} class.
     */
    public CertificateOperations certificateOperations() {
        return certificateOperations;
    }

    /**
     * Gets a {@link JobOperations} object for performing job-related operations on the associated account.
     *
     * @return An instance of the {@link JobOperations} class.
     */
    public JobOperations jobOperations() {
        return jobOperations;
    }

    /**
     * Gets a {@link TaskOperations} object for performing task-related operations on the associated account.
     *
     * @return An instance of the {@link TaskOperations} class.
     */
    public TaskOperations taskOperations() {
        return taskOperations;
    }

    /**
     * Gets a {@link JobScheduleOperations} object for performing job schedule-related operations on the associated account.
     *
     * @return An instance of the {@link JobScheduleOperations} class.
     */
    public JobScheduleOperations jobScheduleOperations() {
        return jobScheduleOperations;
    }

    /**
     * Gets a {@link FileOperations} object for performing file-related operations on the associated account.
     *
     * @return An instance of the {@link FileOperations} class.
     */
    public FileOperations fileOperations() {
        return fileOperations;
    }

    /**
     * Gets a {@link PoolOperations} object for performing pool-related operations on the associated account.
     *
     * @return An instance of the {@link PoolOperations} class.
     */
    public PoolOperations poolOperations() {
        return poolOperations;
    }

    /**
     * Gets a {@link ComputeNodeOperations} object for performing compute node-related operations on the associated account.
     *
     * @return An instance of the {@link ComputeNodeOperations} class.
     */
    public ComputeNodeOperations computeNodeOperations() {
        return computeNodeOperations;
    }

    /**
     * Gets an {@link ApplicationOperations} object for performing application-related operations on the associated account.
     *
     * @return An instance of the {@link ApplicationOperations} class.
     */
    public ApplicationOperations applicationOperations() {
        return applicationOperations;
    }

    /**
     * Gets an {@link AccountOperations} object for performing account-related operations on the associated account.
     *
     * @return An instance of the {@link AccountOperations} class.
     */
    public AccountOperations accountOperations() {
        return accountOperations;
    }

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return The collection of {@link BatchClientBehavior} instances.
     */
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param customBehaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    public BatchClient withCustomBehaviors(Collection<BatchClientBehavior> customBehaviors) {
        this.customBehaviors = customBehaviors;
        return this;
    }

    private static class ResponseBuilderFactory implements ResponseBuilder.Factory {
        private final AzureResponseBuilder.Factory baseFactory = new AzureResponseBuilder.Factory();

        @Override
        public <T, E extends RestException> ResponseBuilder<T, E> newInstance(SerializerAdapter<?> serializerAdapter) {
            return baseFactory.<T, E>newInstance(serializerAdapter).withThrowOnGet404(true);
        }
    }
}

