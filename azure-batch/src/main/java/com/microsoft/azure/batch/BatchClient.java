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

    public static BatchClient open(BatchSharedKeyCredentials credentials) {
        return new BatchClient(credentials);
    }

    public CertificateOperations certificateOperations() {
        return certificateOperations;
    }

    public JobOperations jobOperations() {
        return jobOperations;
    }

    public TaskOperations taskOperations() {
        return taskOperations;
    }

    public JobScheduleOperations jobScheduleOperations() {
        return jobScheduleOperations;
    }

    public FileOperations fileOperations() {
        return fileOperations;
    }

    public PoolOperations poolOperations() {
        return poolOperations;
    }

    public ComputeNodeOperations computeNodeOperations() {
        return computeNodeOperations;
    }

    public ApplicationOperations applicationOperations() {
        return applicationOperations;
    }

    public AccountOperations accountOperations() {
        return accountOperations;
    }

    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    public BatchClient withCustomBehaviors(Collection<BatchClientBehavior> customBehaviors) {
        this.customBehaviors = customBehaviors;
        return this;
    }
}

