/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.management.datalake.analytics.DataLakeAnalyticsJobManagementClient;
import com.microsoft.azure.management.datalake.analytics.Jobs;
import com.microsoft.azure.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Initializes a new instance of the DataLakeAnalyticsJobManagementClientImpl class.
 */
public final class DataLakeAnalyticsJobManagementClientImpl extends AzureServiceClient implements DataLakeAnalyticsJobManagementClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Client Api Version. */
    private String apiVersion;

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String apiVersion() {
        return this.apiVersion;
    }

    /** Gets the DNS suffix used as the base for all Azure Data Lake Analytics Job service requests. */
    private String adlaJobDnsSuffix;

    /**
     * Gets Gets the DNS suffix used as the base for all Azure Data Lake Analytics Job service requests.
     *
     * @return the adlaJobDnsSuffix value.
     */
    public String adlaJobDnsSuffix() {
        return this.adlaJobDnsSuffix;
    }

    /**
     * Sets Gets the DNS suffix used as the base for all Azure Data Lake Analytics Job service requests.
     *
     * @param adlaJobDnsSuffix the adlaJobDnsSuffix value.
     * @return the service client itself
     */
    public DataLakeAnalyticsJobManagementClientImpl withAdlaJobDnsSuffix(String adlaJobDnsSuffix) {
        this.adlaJobDnsSuffix = adlaJobDnsSuffix;
        return this;
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
     * @return the service client itself
     */
    public DataLakeAnalyticsJobManagementClientImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
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
     * @return the service client itself
     */
    public DataLakeAnalyticsJobManagementClientImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
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
     * @return the service client itself
     */
    public DataLakeAnalyticsJobManagementClientImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
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
     * Initializes an instance of DataLakeAnalyticsJobManagementClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public DataLakeAnalyticsJobManagementClientImpl(ServiceClientCredentials credentials) {
        this("https://{accountName}.{adlaJobDnsSuffix}", credentials);
    }

    /**
     * Initializes an instance of DataLakeAnalyticsJobManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    private DataLakeAnalyticsJobManagementClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        this(new RestClient.Builder()
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .build());
    }

    /**
     * Initializes an instance of DataLakeAnalyticsJobManagementClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public DataLakeAnalyticsJobManagementClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2016-03-20-preview";
        this.adlaJobDnsSuffix = "azuredatalakeanalytics.net";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.jobs = new JobsImpl(restClient().retrofit(), this);
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
                "DataLakeAnalyticsJobManagementClient, 2016-03-20-preview");
    }
}
