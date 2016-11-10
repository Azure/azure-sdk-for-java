/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.interceptor.ClientRequestIdInterceptor;
import com.microsoft.azure.batch.protocol.BatchServiceClient;
import com.microsoft.azure.batch.protocol.implementation.BatchServiceClientImpl;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A client for an Azure Batch account, used to access the Batch service.
 */
public class BatchClient {

    private BatchServiceClient protocolLayer;
    private CertificateOperations certificateOperations;
    private JobOperations jobOperations;
    private TaskOperations taskOperations;
    private JobScheduleOperations jobScheduleOperations;
    private PoolOperations poolOperations;
    private FileOperations fileOperations;
    private ComputeNodeOperations computeNodeOperations;
    private ApplicationOperations applicationOperations;
    private AccountOperations accountOperations;
    private Collection<BatchClientBehavior> customBehaviors;

    /**
     * Gets the internal proxy layer to be used for this client instance
     *
     * @return The proxy layer client
     */
    public BatchServiceClient protocolLayer() {
        return this.protocolLayer;
    }

    private BatchClient(BatchSharedKeyCredentials credentials) {
        this.protocolLayer = new BatchServiceClientImpl(credentials.baseUrl(), credentials);
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
     * @param credentials The provided credential
     * @return The new instance of BatchClient
     */
    public static BatchClient open(BatchSharedKeyCredentials credentials) {
        return new BatchClient(credentials);
    }

    /**
     * Gets an {@link CertificateOperations} for performing certificate related operations on the associated account.
     *
     * @return An instance of CertificateOperations class
     */
    public CertificateOperations certificateOperations() {
        return certificateOperations;
    }

    /**
     * Gets an {@link JobOperations} for performing job related operations on the associated account.
     *
     * @return An instance of JobOperations class
     */
    public JobOperations jobOperations() {
        return jobOperations;
    }

    /**
     * Gets an {@link TaskOperations} for performing task related operations on the associated account.
     *
     * @return An instance of TaskOperations class
     */
    public TaskOperations taskOperations() {
        return taskOperations;
    }

    /**
     * Gets an {@link JobScheduleOperations} for performing job schedule related operations on the associated account.
     *
     * @return An instance of JobScheduleOperations class
     */
    public JobScheduleOperations jobScheduleOperations() {
        return jobScheduleOperations;
    }

    /**
     * Gets an {@link FileOperations} for performing file-related operations on the associated account.
     *
     * @return An instance of FileOperations class
     */
    public FileOperations fileOperations() {
        return fileOperations;
    }

    /**
     * Gets an {@link PoolOperations} for performing pool related operations on the associated account.
     *
     * @return An instance of PoolOperations class
     */
    public PoolOperations poolOperations() {
        return poolOperations;
    }

    /**
     * Gets an {@link ComputeNodeOperations} for performing compute node related operations on the associated account.
     *
     * @return An instance of ComputeNodeOperations class
     */
    public ComputeNodeOperations computeNodeOperations() {
        return computeNodeOperations;
    }

    /**
     * Gets an {@link ApplicationOperations} for performing application related operations on the associated account.
     *
     * @return An instance of ApplicationOperations class
     */
    public ApplicationOperations applicationOperations() {
        return applicationOperations;
    }

    /**
     * Gets an {@link AccountOperations} for performing account related operations on the associated account.
     *
     * @return An instance of AccountOperations class
     */
    public AccountOperations accountOperations() {
        return accountOperations;
    }

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return The collection of BatchClientBehavior classes.
     */
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param customBehaviors The collection of BatchClientBehavior classes.
     * @return A BatchClient instance
     */
    public BatchClient withCustomBehaviors(Collection<BatchClientBehavior> customBehaviors) {
        this.customBehaviors = customBehaviors;
        return this;
    }
}

