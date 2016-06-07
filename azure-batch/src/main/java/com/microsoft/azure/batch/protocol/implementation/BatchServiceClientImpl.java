/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.batch.protocol.Accounts;
import com.microsoft.azure.batch.protocol.Applications;
import com.microsoft.azure.batch.protocol.BatchServiceClient;
import com.microsoft.azure.batch.protocol.Certificates;
import com.microsoft.azure.batch.protocol.ComputeNodes;
import com.microsoft.azure.batch.protocol.Files;
import com.microsoft.azure.batch.protocol.Jobs;
import com.microsoft.azure.batch.protocol.JobSchedules;
import com.microsoft.azure.batch.protocol.Pools;
import com.microsoft.azure.batch.protocol.Tasks;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;

/**
 * Initializes a new instance of the BatchServiceClientImpl class.
 */
public final class BatchServiceClientImpl extends AzureServiceClient implements BatchServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
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
    public void withAcceptLanguage(String acceptLanguage) {
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
    public void withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
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
    public void withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
    }

    /**
     * The Applications object to access its operations.
     */
    private Applications applications;

    /**
     * Gets the Applications object to access its operations.
     * @return the Applications object.
     */
    public Applications applications() {
        return this.applications;
    }

    /**
     * The Pools object to access its operations.
     */
    private Pools pools;

    /**
     * Gets the Pools object to access its operations.
     * @return the Pools object.
     */
    public Pools pools() {
        return this.pools;
    }

    /**
     * The Accounts object to access its operations.
     */
    private Accounts accounts;

    /**
     * Gets the Accounts object to access its operations.
     * @return the Accounts object.
     */
    public Accounts accounts() {
        return this.accounts;
    }

    /**
     * The Jobs object to access its operations.
     */
    private Jobs jobs;

    /**
     * Gets the Jobs object to access its operations.
     * @return the Jobs object.
     */
    public Jobs jobs() {
        return this.jobs;
    }

    /**
     * The Certificates object to access its operations.
     */
    private Certificates certificates;

    /**
     * Gets the Certificates object to access its operations.
     * @return the Certificates object.
     */
    public Certificates certificates() {
        return this.certificates;
    }

    /**
     * The Files object to access its operations.
     */
    private Files files;

    /**
     * Gets the Files object to access its operations.
     * @return the Files object.
     */
    public Files files() {
        return this.files;
    }

    /**
     * The JobSchedules object to access its operations.
     */
    private JobSchedules jobSchedules;

    /**
     * Gets the JobSchedules object to access its operations.
     * @return the JobSchedules object.
     */
    public JobSchedules jobSchedules() {
        return this.jobSchedules;
    }

    /**
     * The Tasks object to access its operations.
     */
    private Tasks tasks;

    /**
     * Gets the Tasks object to access its operations.
     * @return the Tasks object.
     */
    public Tasks tasks() {
        return this.tasks;
    }

    /**
     * The ComputeNodes object to access its operations.
     */
    private ComputeNodes computeNodes;

    /**
     * Gets the ComputeNodes object to access its operations.
     * @return the ComputeNodes object.
     */
    public ComputeNodes computeNodes() {
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
        this.applications = new ApplicationsImpl(restClient().retrofit(), this);
        this.pools = new PoolsImpl(restClient().retrofit(), this);
        this.accounts = new AccountsImpl(restClient().retrofit(), this);
        this.jobs = new JobsImpl(restClient().retrofit(), this);
        this.certificates = new CertificatesImpl(restClient().retrofit(), this);
        this.files = new FilesImpl(restClient().retrofit(), this);
        this.jobSchedules = new JobSchedulesImpl(restClient().retrofit(), this);
        this.tasks = new TasksImpl(restClient().retrofit(), this);
        this.computeNodes = new ComputeNodesImpl(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("Azure-SDK-For-Java/%s (%s)",
                getClass().getPackage().getImplementationVersion(),
                "BatchServiceClient, 2016-02-01.3.0");
    }
}
