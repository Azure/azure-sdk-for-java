/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.models.DnsNameAvailabilityResult;
import com.microsoft.rest.AutoRestBaseUrl;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.serializer.JacksonMapperAdapter;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * The interface for NetworkManagementClient class.
 */
public interface NetworkManagementClient {
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
     * Gets the ApplicationGateways object to access its operations.
     * @return the ApplicationGateways object.
     */
    ApplicationGateways applicationGateways();

    /**
     * Gets the ExpressRouteCircuitAuthorizations object to access its operations.
     * @return the ExpressRouteCircuitAuthorizations object.
     */
    ExpressRouteCircuitAuthorizations expressRouteCircuitAuthorizations();

    /**
     * Gets the ExpressRouteCircuitPeerings object to access its operations.
     * @return the ExpressRouteCircuitPeerings object.
     */
    ExpressRouteCircuitPeerings expressRouteCircuitPeerings();

    /**
     * Gets the ExpressRouteCircuits object to access its operations.
     * @return the ExpressRouteCircuits object.
     */
    ExpressRouteCircuits expressRouteCircuits();

    /**
     * Gets the ExpressRouteServiceProviders object to access its operations.
     * @return the ExpressRouteServiceProviders object.
     */
    ExpressRouteServiceProviders expressRouteServiceProviders();

    /**
     * Gets the LoadBalancers object to access its operations.
     * @return the LoadBalancers object.
     */
    LoadBalancers loadBalancers();

    /**
     * Gets the LocalNetworkGateways object to access its operations.
     * @return the LocalNetworkGateways object.
     */
    LocalNetworkGateways localNetworkGateways();

    /**
     * Gets the NetworkInterfaces object to access its operations.
     * @return the NetworkInterfaces object.
     */
    NetworkInterfaces networkInterfaces();

    /**
     * Gets the NetworkSecurityGroups object to access its operations.
     * @return the NetworkSecurityGroups object.
     */
    NetworkSecurityGroups networkSecurityGroups();

    /**
     * Gets the PublicIPAddresses object to access its operations.
     * @return the PublicIPAddresses object.
     */
    PublicIPAddresses publicIPAddresses();

    /**
     * Gets the RouteTables object to access its operations.
     * @return the RouteTables object.
     */
    RouteTables routeTables();

    /**
     * Gets the Routes object to access its operations.
     * @return the Routes object.
     */
    Routes routes();

    /**
     * Gets the SecurityRules object to access its operations.
     * @return the SecurityRules object.
     */
    SecurityRules securityRules();

    /**
     * Gets the Subnets object to access its operations.
     * @return the Subnets object.
     */
    Subnets subnets();

    /**
     * Gets the Usages object to access its operations.
     * @return the Usages object.
     */
    Usages usages();

    /**
     * Gets the VirtualNetworkGatewayConnections object to access its operations.
     * @return the VirtualNetworkGatewayConnections object.
     */
    VirtualNetworkGatewayConnections virtualNetworkGatewayConnections();

    /**
     * Gets the VirtualNetworkGateways object to access its operations.
     * @return the VirtualNetworkGateways object.
     */
    VirtualNetworkGateways virtualNetworkGateways();

    /**
     * Gets the VirtualNetworks object to access its operations.
     * @return the VirtualNetworks object.
     */
    VirtualNetworks virtualNetworks();

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DnsNameAvailabilityResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<DnsNameAvailabilityResult> checkDnsNameAvailability(String location) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall checkDnsNameAvailabilityAsync(String location, final ServiceCallback<DnsNameAvailabilityResult> serviceCallback) throws IllegalArgumentException;
    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param domainNameLabel The domain name to be verified. It must conform to the following regular expression: ^[a-z][a-z0-9-]{1,61}[a-z0-9]$.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DnsNameAvailabilityResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<DnsNameAvailabilityResult> checkDnsNameAvailability(String location, String domainNameLabel) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param domainNameLabel The domain name to be verified. It must conform to the following regular expression: ^[a-z][a-z0-9-]{1,61}[a-z0-9]$.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall checkDnsNameAvailabilityAsync(String location, String domainNameLabel, final ServiceCallback<DnsNameAvailabilityResult> serviceCallback) throws IllegalArgumentException;

}
