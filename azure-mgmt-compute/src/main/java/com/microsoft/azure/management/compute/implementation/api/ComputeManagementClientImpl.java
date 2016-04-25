/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.CustomHeaderInterceptor;
import com.microsoft.rest.AutoRestBaseUrl;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import java.util.UUID;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Initializes a new instance of the ComputeManagementClientImpl class.
 */
public final class ComputeManagementClientImpl extends AzureServiceClient {
    /** The URL used as the base for all cloud service requests. */
    private final AutoRestBaseUrl baseUrl;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the URL used as the base for all cloud service requests.
     *
     * @return The BaseUrl value.
     */
    public AutoRestBaseUrl getBaseUrl() {
        return this.baseUrl;
    }

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
    public ServiceClientCredentials getCredentials() {
        return this.credentials;
    }

    /** Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call. */
    private String subscriptionId;

    /**
     * Gets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @return the subscriptionId value.
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Sets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @param subscriptionId the subscriptionId value.
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /** Client Api Version. */
    private String apiVersion;

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String getAcceptLanguage() {
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
    public int getLongRunningOperationRetryTimeout() {
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
    public boolean getGenerateClientRequestId() {
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
     * Gets the AvailabilitySetsInner object to access its operations.
     * @return the AvailabilitySetsInner object.
     */
    public AvailabilitySetsInner availabilitySets() {
        return new AvailabilitySetsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineExtensionImagesInner object to access its operations.
     * @return the VirtualMachineExtensionImagesInner object.
     */
    public VirtualMachineExtensionImagesInner virtualMachineExtensionImages() {
        return new VirtualMachineExtensionImagesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineExtensionsInner object to access its operations.
     * @return the VirtualMachineExtensionsInner object.
     */
    public VirtualMachineExtensionsInner virtualMachineExtensions() {
        return new VirtualMachineExtensionsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineImagesInner object to access its operations.
     * @return the VirtualMachineImagesInner object.
     */
    public VirtualMachineImagesInner virtualMachineImages() {
        return new VirtualMachineImagesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the UsagesInner object to access its operations.
     * @return the UsagesInner object.
     */
    public UsagesInner usages() {
        return new UsagesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineSizesInner object to access its operations.
     * @return the VirtualMachineSizesInner object.
     */
    public VirtualMachineSizesInner virtualMachineSizes() {
        return new VirtualMachineSizesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachinesInner object to access its operations.
     * @return the VirtualMachinesInner object.
     */
    public VirtualMachinesInner virtualMachines() {
        return new VirtualMachinesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineScaleSetsInner object to access its operations.
     * @return the VirtualMachineScaleSetsInner object.
     */
    public VirtualMachineScaleSetsInner virtualMachineScaleSets() {
        return new VirtualMachineScaleSetsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualMachineScaleSetVMsInner object to access its operations.
     * @return the VirtualMachineScaleSetVMsInner object.
     */
    public VirtualMachineScaleSetVMsInner virtualMachineScaleSetVMs() {
        return new VirtualMachineScaleSetVMsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Initializes an instance of ComputeManagementClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ComputeManagementClientImpl(ServiceClientCredentials credentials) {
        this("https://management.azure.com", credentials);
    }

    /**
     * Initializes an instance of ComputeManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public ComputeManagementClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super();
        this.baseUrl = new AutoRestBaseUrl(baseUrl);
        this.credentials = credentials;
        initialize();
    }

    /**
     * Initializes an instance of ComputeManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     * @param retrofitBuilder the builder for building up a {@link Retrofit}
     */
    public ComputeManagementClientImpl(String baseUrl, ServiceClientCredentials credentials, OkHttpClient.Builder clientBuilder, Retrofit.Builder retrofitBuilder) {
        super(clientBuilder, retrofitBuilder);
        this.baseUrl = new AutoRestBaseUrl(baseUrl);
        this.credentials = credentials;
        initialize();
    }

    @Override
    protected void initialize() {
        this.apiVersion = "2015-06-15";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.clientBuilder.interceptors().add(new CustomHeaderInterceptor("x-ms-client-request-id", UUID.randomUUID().toString()));
        if (this.credentials != null) {
            this.credentials.applyCredentialsFilter(clientBuilder);
        }
        super.initialize();
        this.azureClient = new AzureClient(clientBuilder, retrofitBuilder, mapperAdapter);
        this.azureClient.setCredentials(this.credentials);
        this.retrofitBuilder.baseUrl(baseUrl);
    }
}
