/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureClient;
import com.microsoft.rest.AutoRestBaseUrl;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.serializer.JacksonMapperAdapter;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * The interface for ComputeManagementClient class.
 */
public interface ComputeManagementClient {
    /**
     * Gets the URL used as the base for all cloud service requests.
     *
     * @return the BaseUrl object.
     */
    AutoRestBaseUrl getBaseUrl();

    /**
     * Gets the list of interceptors the OkHttp client will execute.
     * @return the list of interceptors.
     */
    List<Interceptor> getClientInterceptors();

    /**
     * Sets the logging level for OkHttp client.
     *
     * @param logLevel the logging level enum.
     */
    void setLogLevel(Level logLevel);

    /**
     * Gets the adapter for {@link com.fasterxml.jackson.databind.ObjectMapper} for serialization
     * and deserialization operations..
     *
     * @return the adapter.
     */
    JacksonMapperAdapter getMapperAdapter();

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    AzureClient getAzureClient();

    /**
     * Gets Gets Azure subscription credentials..
     *
     * @return the credentials value.
     */
    ServiceClientCredentials getCredentials();

    /**
     * Gets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call..
     *
     * @return the subscriptionId value.
     */
    String getSubscriptionId();

    /**
     * Sets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call..
     *
     * @param subscriptionId the subscriptionId value.
     */
    void setSubscriptionId(String subscriptionId);

    /**
     * Gets Client Api Version..
     *
     * @return the apiVersion value.
     */
    String getApiVersion();

    /**
     * Gets Gets or sets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    String getAcceptLanguage();

    /**
     * Sets Gets or sets the preferred language for the response..
     *
     * @param acceptLanguage the acceptLanguage value.
     */
    void setAcceptLanguage(String acceptLanguage);

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    int getLongRunningOperationRetryTimeout();

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    void setLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout);

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @return the generateClientRequestId value.
     */
    boolean getGenerateClientRequestId();

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    void setGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * Gets the AvailabilitySets object to access its operations.
     * @return the AvailabilitySets object.
     */
    AvailabilitySets availabilitySets();

    /**
     * Gets the VirtualMachineExtensionImages object to access its operations.
     * @return the VirtualMachineExtensionImages object.
     */
    VirtualMachineExtensionImages virtualMachineExtensionImages();

    /**
     * Gets the VirtualMachineExtensions object to access its operations.
     * @return the VirtualMachineExtensions object.
     */
    VirtualMachineExtensions virtualMachineExtensions();

    /**
     * Gets the VirtualMachineImages object to access its operations.
     * @return the VirtualMachineImages object.
     */
    VirtualMachineImages virtualMachineImages();

    /**
     * Gets the Usages object to access its operations.
     * @return the Usages object.
     */
    Usages usages();

    /**
     * Gets the VirtualMachineSizes object to access its operations.
     * @return the VirtualMachineSizes object.
     */
    VirtualMachineSizes virtualMachineSizes();

    /**
     * Gets the VirtualMachines object to access its operations.
     * @return the VirtualMachines object.
     */
    VirtualMachines virtualMachines();

    /**
     * Gets the VirtualMachineScaleSets object to access its operations.
     * @return the VirtualMachineScaleSets object.
     */
    VirtualMachineScaleSets virtualMachineScaleSets();

    /**
     * Gets the VirtualMachineScaleSetVMs object to access its operations.
     * @return the VirtualMachineScaleSetVMs object.
     */
    VirtualMachineScaleSetVMs virtualMachineScaleSetVMs();

}
