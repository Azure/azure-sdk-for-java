/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.AzureClient;
import com.microsoft.rest.AutoRestBaseUrl;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.serializer.JacksonMapperAdapter;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * The interface for WebSiteManagementClient class.
 */
public interface WebSiteManagementClient {
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
     * Gets Subscription Id.
     *
     * @return the subscriptionId value.
     */
    String getSubscriptionId();

    /**
     * Sets Subscription Id.
     *
     * @param subscriptionId the subscriptionId value.
     */
    void setSubscriptionId(String subscriptionId);

    /**
     * Gets API Version.
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
     * Gets the Certificates object to access its operations.
     * @return the Certificates object.
     */
    Certificates certificates();

    /**
     * Gets the ClassicMobileServices object to access its operations.
     * @return the ClassicMobileServices object.
     */
    ClassicMobileServices classicMobileServices();

    /**
     * Gets the Domains object to access its operations.
     * @return the Domains object.
     */
    Domains domains();

    /**
     * Gets the Globals object to access its operations.
     * @return the Globals object.
     */
    Globals globals();

    /**
     * Gets the GlobalDomainRegistrations object to access its operations.
     * @return the GlobalDomainRegistrations object.
     */
    GlobalDomainRegistrations globalDomainRegistrations();

    /**
     * Gets the GlobalResourceGroups object to access its operations.
     * @return the GlobalResourceGroups object.
     */
    GlobalResourceGroups globalResourceGroups();

    /**
     * Gets the HostingEnvironments object to access its operations.
     * @return the HostingEnvironments object.
     */
    HostingEnvironments hostingEnvironments();

    /**
     * Gets the ManagedHostingEnvironments object to access its operations.
     * @return the ManagedHostingEnvironments object.
     */
    ManagedHostingEnvironments managedHostingEnvironments();

    /**
     * Gets the Providers object to access its operations.
     * @return the Providers object.
     */
    Providers providers();

    /**
     * Gets the Recommendations object to access its operations.
     * @return the Recommendations object.
     */
    Recommendations recommendations();

    /**
     * Gets the ServerFarms object to access its operations.
     * @return the ServerFarms object.
     */
    ServerFarms serverFarms();

    /**
     * Gets the Sites object to access its operations.
     * @return the Sites object.
     */
    Sites sites();

    /**
     * Gets the TopLevelDomains object to access its operations.
     * @return the TopLevelDomains object.
     */
    TopLevelDomains topLevelDomains();

    /**
     * Gets the Usages object to access its operations.
     * @return the Usages object.
     */
    Usages usages();

}
