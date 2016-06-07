/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.AzureClient;
import com.microsoft.rest.RestClient;

/**
 * The interface for BatchServiceClient class.
 */
public interface BatchServiceClient {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    AzureClient getAzureClient();

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    String userAgent();

    /**
     * Gets Client API Version..
     *
     * @return the apiVersion value.
     */
    String apiVersion();

    /**
     * Gets Gets or sets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    String acceptLanguage();

    /**
     * Sets Gets or sets the preferred language for the response..
     *
     * @param acceptLanguage the acceptLanguage value.
     */
    void withAcceptLanguage(String acceptLanguage);

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    int longRunningOperationRetryTimeout();

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    void withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout);

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @return the generateClientRequestId value.
     */
    boolean generateClientRequestId();

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    void withGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * Gets the Applications object to access its operations.
     * @return the Applications object.
     */
    Applications applications();

    /**
     * Gets the Pools object to access its operations.
     * @return the Pools object.
     */
    Pools pools();

    /**
     * Gets the Accounts object to access its operations.
     * @return the Accounts object.
     */
    Accounts accounts();

    /**
     * Gets the Jobs object to access its operations.
     * @return the Jobs object.
     */
    Jobs jobs();

    /**
     * Gets the Certificates object to access its operations.
     * @return the Certificates object.
     */
    Certificates certificates();

    /**
     * Gets the Files object to access its operations.
     * @return the Files object.
     */
    Files files();

    /**
     * Gets the JobSchedules object to access its operations.
     * @return the JobSchedules object.
     */
    JobSchedules jobSchedules();

    /**
     * Gets the Tasks object to access its operations.
     * @return the Tasks object.
     */
    Tasks tasks();

    /**
     * Gets the ComputeNodes object to access its operations.
     * @return the ComputeNodes object.
     */
    ComputeNodes computeNodes();

}
