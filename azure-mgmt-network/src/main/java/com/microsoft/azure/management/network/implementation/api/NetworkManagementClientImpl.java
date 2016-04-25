/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.CustomHeaderInterceptor;
import com.microsoft.rest.AutoRestBaseUrl;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import java.io.IOException;
import java.util.UUID;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Initializes a new instance of the NetworkManagementClientImpl class.
 */
public final class NetworkManagementClientImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private NetworkManagementClientService service;
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
     * Gets the ApplicationGatewaysInner object to access its operations.
     * @return the ApplicationGatewaysInner object.
     */
    public ApplicationGatewaysInner applicationGateways() {
        return new ApplicationGatewaysInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the ExpressRouteCircuitAuthorizationsInner object to access its operations.
     * @return the ExpressRouteCircuitAuthorizationsInner object.
     */
    public ExpressRouteCircuitAuthorizationsInner expressRouteCircuitAuthorizations() {
        return new ExpressRouteCircuitAuthorizationsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the ExpressRouteCircuitPeeringsInner object to access its operations.
     * @return the ExpressRouteCircuitPeeringsInner object.
     */
    public ExpressRouteCircuitPeeringsInner expressRouteCircuitPeerings() {
        return new ExpressRouteCircuitPeeringsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the ExpressRouteCircuitsInner object to access its operations.
     * @return the ExpressRouteCircuitsInner object.
     */
    public ExpressRouteCircuitsInner expressRouteCircuits() {
        return new ExpressRouteCircuitsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the ExpressRouteServiceProvidersInner object to access its operations.
     * @return the ExpressRouteServiceProvidersInner object.
     */
    public ExpressRouteServiceProvidersInner expressRouteServiceProviders() {
        return new ExpressRouteServiceProvidersInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the LoadBalancersInner object to access its operations.
     * @return the LoadBalancersInner object.
     */
    public LoadBalancersInner loadBalancers() {
        return new LoadBalancersInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the LocalNetworkGatewaysInner object to access its operations.
     * @return the LocalNetworkGatewaysInner object.
     */
    public LocalNetworkGatewaysInner localNetworkGateways() {
        return new LocalNetworkGatewaysInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the NetworkInterfacesInner object to access its operations.
     * @return the NetworkInterfacesInner object.
     */
    public NetworkInterfacesInner networkInterfaces() {
        return new NetworkInterfacesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the NetworkSecurityGroupsInner object to access its operations.
     * @return the NetworkSecurityGroupsInner object.
     */
    public NetworkSecurityGroupsInner networkSecurityGroups() {
        return new NetworkSecurityGroupsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the PublicIPAddressesInner object to access its operations.
     * @return the PublicIPAddressesInner object.
     */
    public PublicIPAddressesInner publicIPAddresses() {
        return new PublicIPAddressesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the RouteTablesInner object to access its operations.
     * @return the RouteTablesInner object.
     */
    public RouteTablesInner routeTables() {
        return new RouteTablesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the RoutesInner object to access its operations.
     * @return the RoutesInner object.
     */
    public RoutesInner routes() {
        return new RoutesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the SecurityRulesInner object to access its operations.
     * @return the SecurityRulesInner object.
     */
    public SecurityRulesInner securityRules() {
        return new SecurityRulesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the SubnetsInner object to access its operations.
     * @return the SubnetsInner object.
     */
    public SubnetsInner subnets() {
        return new SubnetsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the UsagesInner object to access its operations.
     * @return the UsagesInner object.
     */
    public UsagesInner usages() {
        return new UsagesInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualNetworkGatewayConnectionsInner object to access its operations.
     * @return the VirtualNetworkGatewayConnectionsInner object.
     */
    public VirtualNetworkGatewayConnectionsInner virtualNetworkGatewayConnections() {
        return new VirtualNetworkGatewayConnectionsInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualNetworkGatewaysInner object to access its operations.
     * @return the VirtualNetworkGatewaysInner object.
     */
    public VirtualNetworkGatewaysInner virtualNetworkGateways() {
        return new VirtualNetworkGatewaysInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Gets the VirtualNetworksInner object to access its operations.
     * @return the VirtualNetworksInner object.
     */
    public VirtualNetworksInner virtualNetworks() {
        return new VirtualNetworksInner(this.retrofitBuilder.client(clientBuilder.build()).build(), this);
    }

    /**
     * Initializes an instance of NetworkManagementClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public NetworkManagementClientImpl(ServiceClientCredentials credentials) {
        this("https://management.azure.com", credentials);
    }

    /**
     * Initializes an instance of NetworkManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public NetworkManagementClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super();
        this.baseUrl = new AutoRestBaseUrl(baseUrl);
        this.credentials = credentials;
        initialize();
    }

    /**
     * Initializes an instance of NetworkManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     * @param clientBuilder the builder for building up an {@link OkHttpClient}
     * @param retrofitBuilder the builder for building up a {@link Retrofit}
     */
    public NetworkManagementClientImpl(String baseUrl, ServiceClientCredentials credentials, OkHttpClient.Builder clientBuilder, Retrofit.Builder retrofitBuilder) {
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
        initializeService();
    }

    private void initializeService() {
        service = this.retrofitBuilder.client(this.clientBuilder.build())
                .build()
                .create(NetworkManagementClientService.class);
    }

    /**
     * Sets the logging level for OkHttp client.
     *
     * @param logLevel the logging level enum
     */
    @Override
    public void setLogLevel(Level logLevel) {
        super.setLogLevel(logLevel);
        initializeService();
    }

    /**
     * The interface defining all the services for NetworkManagementClient to be
     * used by Retrofit to perform actually REST calls.
     */
    interface NetworkManagementClientService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Network/locations/{location}/CheckDnsNameAvailability")
        Call<ResponseBody> checkDnsNameAvailability(@Path("location") String location, @Path("subscriptionId") String subscriptionId, @Query("domainNameLabel") String domainNameLabel, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DnsNameAvailabilityResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DnsNameAvailabilityResultInner> checkDnsNameAvailability(String location) throws CloudException, IOException, IllegalArgumentException {
        if (location == null) {
            throw new IllegalArgumentException("Parameter location is required and cannot be null.");
        }
        if (this.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.getSubscriptionId() is required and cannot be null.");
        }
        if (this.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.getApiVersion() is required and cannot be null.");
        }
        final String domainNameLabel = null;
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.getSubscriptionId(), domainNameLabel, this.getApiVersion(), this.getAcceptLanguage());
        return checkDnsNameAvailabilityDelegate(call.execute());
    }

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkDnsNameAvailabilityAsync(String location, final ServiceCallback<DnsNameAvailabilityResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (location == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter location is required and cannot be null."));
            return null;
        }
        if (this.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.getApiVersion() is required and cannot be null."));
            return null;
        }
        final String domainNameLabel = null;
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.getSubscriptionId(), domainNameLabel, this.getApiVersion(), this.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DnsNameAvailabilityResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkDnsNameAvailabilityDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param domainNameLabel The domain name to be verified. It must conform to the following regular expression: ^[a-z][a-z0-9-]{1,61}[a-z0-9]$.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DnsNameAvailabilityResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DnsNameAvailabilityResultInner> checkDnsNameAvailability(String location, String domainNameLabel) throws CloudException, IOException, IllegalArgumentException {
        if (location == null) {
            throw new IllegalArgumentException("Parameter location is required and cannot be null.");
        }
        if (this.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.getSubscriptionId() is required and cannot be null.");
        }
        if (this.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.getSubscriptionId(), domainNameLabel, this.getApiVersion(), this.getAcceptLanguage());
        return checkDnsNameAvailabilityDelegate(call.execute());
    }

    /**
     * Checks whether a domain name in the cloudapp.net zone is available for use.
     *
     * @param location The location of the domain name
     * @param domainNameLabel The domain name to be verified. It must conform to the following regular expression: ^[a-z][a-z0-9-]{1,61}[a-z0-9]$.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkDnsNameAvailabilityAsync(String location, String domainNameLabel, final ServiceCallback<DnsNameAvailabilityResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (location == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter location is required and cannot be null."));
            return null;
        }
        if (this.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.getSubscriptionId(), domainNameLabel, this.getApiVersion(), this.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DnsNameAvailabilityResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkDnsNameAvailabilityDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DnsNameAvailabilityResultInner> checkDnsNameAvailabilityDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DnsNameAvailabilityResultInner, CloudException>(this.getMapperAdapter())
                .register(200, new TypeToken<DnsNameAvailabilityResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
