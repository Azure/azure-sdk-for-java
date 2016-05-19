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

    public BatchServiceClient getProtocolLayer() {
        return this.protocolLayer;
    }

    private BatchClient(BatchSharedKeyCredentials credentials) {
        this.protocolLayer = new BatchServiceClientImpl(credentials.getBaseUrl(), credentials);
        this.customBehaviors = new LinkedList<>();
        this.customBehaviors.add(new ClientRequestIdInterceptor());
        this.certificateOperations = new CertificateOperations(this, getCustomBehaviors());
        this.jobOperations = new JobOperations(this, getCustomBehaviors());
        this.taskOperations = new TaskOperations(this, getCustomBehaviors());
        this.jobScheduleOperations = new JobScheduleOperations(this, getCustomBehaviors());
        this.poolOperations = new PoolOperations(this, getCustomBehaviors());
        this.fileOperations = new FileOperations(this, getCustomBehaviors());
        this.applicationOperations = new ApplicationOperations(this, getCustomBehaviors());
        this.accountOperations = new AccountOperations(this, getCustomBehaviors());
        this.computeNodeOperations = new ComputeNodeOperations(this, getCustomBehaviors());
    }

    public static BatchClient Open(BatchSharedKeyCredentials credentials) {
        return new BatchClient(credentials);
    }

    public CertificateOperations getCertificateOperations() {
        return certificateOperations;
    }

    public JobOperations getJobOperations() {
        return jobOperations;
    }

    public TaskOperations getTaskOperations() {
        return taskOperations;
    }

    public JobScheduleOperations getJobScheduleOperations() {
        return jobScheduleOperations;
    }

    public FileOperations getFileOperations() {
        return fileOperations;
    }

    public PoolOperations getPoolOperations() {
        return poolOperations;
    }

    public ComputeNodeOperations getComputeNodeOperations() {
        return computeNodeOperations;
    }

    public ApplicationOperations getApplicationOperations() {
        return applicationOperations;
    }

    public AccountOperations getAccountOperations() {
        return accountOperations;
    }

    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return customBehaviors;
    }

    public void setCustomBehaviors(Collection<BatchClientBehavior> customBehaviors) {
        this.customBehaviors = customBehaviors;
    }
}

