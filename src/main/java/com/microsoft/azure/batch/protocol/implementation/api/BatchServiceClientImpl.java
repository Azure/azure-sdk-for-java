/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import java.util.UUID;

/**
 * Initializes a new instance of the BatchServiceClientImpl class.
 */
public final class BatchServiceClientImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Gets Azure subscription credentials. */
    private ServiceClientCredentials credentials;

    /**
     * Gets Gets Azure subscription credentials.
     *
     * @return the credentials value.
     */
    public ServiceClientCredentials credentials() {
        return this.credentials;
    }

    /** Client API Version. */
    private String apiVersion;

    /**
     * Gets Client API Version.
     *
     * @return the apiVersion value.
     */
    public String apiVersion() {
        return this.apiVersion;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     */
    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    public void setLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    public void setGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
    }

    /**
     * The ApplicationsInner object to access its operations.
     */
    private ApplicationsInner applications;

    /**
     * Gets the ApplicationsInner object to access its operations.
     * @return the ApplicationsInner object.
     */
    public ApplicationsInner applications() {
        return this.applications;
    }

    /**
     * The PoolsInner object to access its operations.
     */
    private PoolsInner pools;

    /**
     * Gets the PoolsInner object to access its operations.
     * @return the PoolsInner object.
     */
    public PoolsInner pools() {
        return this.pools;
    }

    /**
     * The AccountsInner object to access its operations.
     */
    private AccountsInner accounts;

    /**
     * Gets the AccountsInner object to access its operations.
     * @return the AccountsInner object.
     */
    public AccountsInner accounts() {
        return this.accounts;
    }

    /**
     * The JobsInner object to access its operations.
     */
    private JobsInner jobs;

    /**
     * Gets the JobsInner object to access its operations.
     * @return the JobsInner object.
     */
    public JobsInner jobs() {
        return this.jobs;
    }

    /**
     * The CertificatesInner object to access its operations.
     */
    private CertificatesInner certificates;

    /**
     * Gets the CertificatesInner object to access its operations.
     * @return the CertificatesInner object.
     */
    public CertificatesInner certificates() {
        return this.certificates;
    }

    /**
     * The FilesInner object to access its operations.
     */
    private FilesInner files;

    /**
     * Gets the FilesInner object to access its operations.
     * @return the FilesInner object.
     */
    public FilesInner files() {
        return this.files;
    }

    /**
     * The JobSchedulesInner object to access its operations.
     */
    private JobSchedulesInner jobSchedules;

    /**
     * Gets the JobSchedulesInner object to access its operations.
     * @return the JobSchedulesInner object.
     */
    public JobSchedulesInner jobSchedules() {
        return this.jobSchedules;
    }

    /**
     * The TasksInner object to access its operations.
     */
    private TasksInner tasks;

    /**
     * Gets the TasksInner object to access its operations.
     * @return the TasksInner object.
     */
    public TasksInner tasks() {
        return this.tasks;
    }

    /**
     * The ComputeNodesInner object to access its operations.
     */
    private ComputeNodesInner computeNodes;

    /**
     * Gets the ComputeNodesInner object to access its operations.
     * @return the ComputeNodesInner object.
     */
    public ComputeNodesInner computeNodes() {
        return this.computeNodes;
    }

    /**
     * Initializes an instance of BatchServiceClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public BatchServiceClientImpl(ServiceClientCredentials credentials) {
        this("https://batch.core.windows.net", credentials);
    }

    /**
     * Initializes an instance of BatchServiceClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public BatchServiceClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        this(new RestClient.Builder(baseUrl)
                .withMapperAdapter(new AzureJacksonMapperAdapter())
                .withCredentials(credentials)
                .build());
    }

    /**
     * Initializes an instance of BatchServiceClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public BatchServiceClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2016-02-01.3.0";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.applications = new ApplicationsInner(restClient().retrofit(), this);
        this.pools = new PoolsInner(restClient().retrofit(), this);
        this.accounts = new AccountsInner(restClient().retrofit(), this);
        this.jobs = new JobsInner(restClient().retrofit(), this);
        this.certificates = new CertificatesInner(restClient().retrofit(), this);
        this.files = new FilesInner(restClient().retrofit(), this);
        this.jobSchedules = new JobSchedulesInner(restClient().retrofit(), this);
        this.tasks = new TasksInner(restClient().retrofit(), this);
        this.computeNodes = new ComputeNodesInner(restClient().retrofit(), this);
        restClient().headers().addHeader("x-ms-client-request-id", UUID.randomUUID().toString());
        this.azureClient = new AzureClient(restClient());
    }
}
