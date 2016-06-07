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
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.Response;

/**
 * Initializes a new instance of the NetworkManagementClientImpl class.
 */
public final class NetworkManagementClientImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private NetworkManagementClientService service;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call. */
    private String subscriptionId;

    /**
     * Gets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Sets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @param subscriptionId the subscriptionId value.
     */
    public void withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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
     * The ApplicationGatewaysInner object to access its operations.
     */
    private ApplicationGatewaysInner applicationGateways;

    /**
     * Gets the ApplicationGatewaysInner object to access its operations.
     * @return the ApplicationGatewaysInner object.
     */
    public ApplicationGatewaysInner applicationGateways() {
        return this.applicationGateways;
    }

    /**
     * The ExpressRouteCircuitAuthorizationsInner object to access its operations.
     */
    private ExpressRouteCircuitAuthorizationsInner expressRouteCircuitAuthorizations;

    /**
     * Gets the ExpressRouteCircuitAuthorizationsInner object to access its operations.
     * @return the ExpressRouteCircuitAuthorizationsInner object.
     */
    public ExpressRouteCircuitAuthorizationsInner expressRouteCircuitAuthorizations() {
        return this.expressRouteCircuitAuthorizations;
    }

    /**
     * The ExpressRouteCircuitPeeringsInner object to access its operations.
     */
    private ExpressRouteCircuitPeeringsInner expressRouteCircuitPeerings;

    /**
     * Gets the ExpressRouteCircuitPeeringsInner object to access its operations.
     * @return the ExpressRouteCircuitPeeringsInner object.
     */
    public ExpressRouteCircuitPeeringsInner expressRouteCircuitPeerings() {
        return this.expressRouteCircuitPeerings;
    }

    /**
     * The ExpressRouteCircuitsInner object to access its operations.
     */
    private ExpressRouteCircuitsInner expressRouteCircuits;

    /**
     * Gets the ExpressRouteCircuitsInner object to access its operations.
     * @return the ExpressRouteCircuitsInner object.
     */
    public ExpressRouteCircuitsInner expressRouteCircuits() {
        return this.expressRouteCircuits;
    }

    /**
     * The ExpressRouteServiceProvidersInner object to access its operations.
     */
    private ExpressRouteServiceProvidersInner expressRouteServiceProviders;

    /**
     * Gets the ExpressRouteServiceProvidersInner object to access its operations.
     * @return the ExpressRouteServiceProvidersInner object.
     */
    public ExpressRouteServiceProvidersInner expressRouteServiceProviders() {
        return this.expressRouteServiceProviders;
    }

    /**
     * The LoadBalancersInner object to access its operations.
     */
    private LoadBalancersInner loadBalancers;

    /**
     * Gets the LoadBalancersInner object to access its operations.
     * @return the LoadBalancersInner object.
     */
    public LoadBalancersInner loadBalancers() {
        return this.loadBalancers;
    }

    /**
     * The LocalNetworkGatewaysInner object to access its operations.
     */
    private LocalNetworkGatewaysInner localNetworkGateways;

    /**
     * Gets the LocalNetworkGatewaysInner object to access its operations.
     * @return the LocalNetworkGatewaysInner object.
     */
    public LocalNetworkGatewaysInner localNetworkGateways() {
        return this.localNetworkGateways;
    }

    /**
     * The NetworkInterfacesInner object to access its operations.
     */
    private NetworkInterfacesInner networkInterfaces;

    /**
     * Gets the NetworkInterfacesInner object to access its operations.
     * @return the NetworkInterfacesInner object.
     */
    public NetworkInterfacesInner networkInterfaces() {
        return this.networkInterfaces;
    }

    /**
     * The NetworkSecurityGroupsInner object to access its operations.
     */
    private NetworkSecurityGroupsInner networkSecurityGroups;

    /**
     * Gets the NetworkSecurityGroupsInner object to access its operations.
     * @return the NetworkSecurityGroupsInner object.
     */
    public NetworkSecurityGroupsInner networkSecurityGroups() {
        return this.networkSecurityGroups;
    }

    /**
     * The PublicIPAddressesInner object to access its operations.
     */
    private PublicIPAddressesInner publicIPAddresses;

    /**
     * Gets the PublicIPAddressesInner object to access its operations.
     * @return the PublicIPAddressesInner object.
     */
    public PublicIPAddressesInner publicIPAddresses() {
        return this.publicIPAddresses;
    }

    /**
     * The RouteTablesInner object to access its operations.
     */
    private RouteTablesInner routeTables;

    /**
     * Gets the RouteTablesInner object to access its operations.
     * @return the RouteTablesInner object.
     */
    public RouteTablesInner routeTables() {
        return this.routeTables;
    }

    /**
     * The RoutesInner object to access its operations.
     */
    private RoutesInner routes;

    /**
     * Gets the RoutesInner object to access its operations.
     * @return the RoutesInner object.
     */
    public RoutesInner routes() {
        return this.routes;
    }

    /**
     * The SecurityRulesInner object to access its operations.
     */
    private SecurityRulesInner securityRules;

    /**
     * Gets the SecurityRulesInner object to access its operations.
     * @return the SecurityRulesInner object.
     */
    public SecurityRulesInner securityRules() {
        return this.securityRules;
    }

    /**
     * The SubnetsInner object to access its operations.
     */
    private SubnetsInner subnets;

    /**
     * Gets the SubnetsInner object to access its operations.
     * @return the SubnetsInner object.
     */
    public SubnetsInner subnets() {
        return this.subnets;
    }

    /**
     * The UsagesInner object to access its operations.
     */
    private UsagesInner usages;

    /**
     * Gets the UsagesInner object to access its operations.
     * @return the UsagesInner object.
     */
    public UsagesInner usages() {
        return this.usages;
    }

    /**
     * The VirtualNetworkGatewayConnectionsInner object to access its operations.
     */
    private VirtualNetworkGatewayConnectionsInner virtualNetworkGatewayConnections;

    /**
     * Gets the VirtualNetworkGatewayConnectionsInner object to access its operations.
     * @return the VirtualNetworkGatewayConnectionsInner object.
     */
    public VirtualNetworkGatewayConnectionsInner virtualNetworkGatewayConnections() {
        return this.virtualNetworkGatewayConnections;
    }

    /**
     * The VirtualNetworkGatewaysInner object to access its operations.
     */
    private VirtualNetworkGatewaysInner virtualNetworkGateways;

    /**
     * Gets the VirtualNetworkGatewaysInner object to access its operations.
     * @return the VirtualNetworkGatewaysInner object.
     */
    public VirtualNetworkGatewaysInner virtualNetworkGateways() {
        return this.virtualNetworkGateways;
    }

    /**
     * The VirtualNetworksInner object to access its operations.
     */
    private VirtualNetworksInner virtualNetworks;

    /**
     * Gets the VirtualNetworksInner object to access its operations.
     * @return the VirtualNetworksInner object.
     */
    public VirtualNetworksInner virtualNetworks() {
        return this.virtualNetworks;
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
        this(new RestClient.Builder(baseUrl)
                .withMapperAdapter(new AzureJacksonMapperAdapter())
                .withCredentials(credentials)
                .build());
    }

    /**
     * Initializes an instance of NetworkManagementClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public NetworkManagementClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2015-06-15";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.applicationGateways = new ApplicationGatewaysInner(restClient().retrofit(), this);
        this.expressRouteCircuitAuthorizations = new ExpressRouteCircuitAuthorizationsInner(restClient().retrofit(), this);
        this.expressRouteCircuitPeerings = new ExpressRouteCircuitPeeringsInner(restClient().retrofit(), this);
        this.expressRouteCircuits = new ExpressRouteCircuitsInner(restClient().retrofit(), this);
        this.expressRouteServiceProviders = new ExpressRouteServiceProvidersInner(restClient().retrofit(), this);
        this.loadBalancers = new LoadBalancersInner(restClient().retrofit(), this);
        this.localNetworkGateways = new LocalNetworkGatewaysInner(restClient().retrofit(), this);
        this.networkInterfaces = new NetworkInterfacesInner(restClient().retrofit(), this);
        this.networkSecurityGroups = new NetworkSecurityGroupsInner(restClient().retrofit(), this);
        this.publicIPAddresses = new PublicIPAddressesInner(restClient().retrofit(), this);
        this.routeTables = new RouteTablesInner(restClient().retrofit(), this);
        this.routes = new RoutesInner(restClient().retrofit(), this);
        this.securityRules = new SecurityRulesInner(restClient().retrofit(), this);
        this.subnets = new SubnetsInner(restClient().retrofit(), this);
        this.usages = new UsagesInner(restClient().retrofit(), this);
        this.virtualNetworkGatewayConnections = new VirtualNetworkGatewayConnectionsInner(restClient().retrofit(), this);
        this.virtualNetworkGateways = new VirtualNetworkGatewaysInner(restClient().retrofit(), this);
        this.virtualNetworks = new VirtualNetworksInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
        initializeService();
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
                "NetworkManagementClient, 2015-06-15");
    }

    private void initializeService() {
        service = restClient().retrofit().create(NetworkManagementClientService.class);
    }

    /**
     * The interface defining all the services for NetworkManagementClient to be
     * used by Retrofit to perform actually REST calls.
     */
    interface NetworkManagementClientService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Network/locations/{location}/CheckDnsNameAvailability")
        Call<ResponseBody> checkDnsNameAvailability(@Path("location") String location, @Path("subscriptionId") String subscriptionId, @Query("domainNameLabel") String domainNameLabel, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

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
        if (this.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.subscriptionId() is required and cannot be null.");
        }
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        final String domainNameLabel = null;
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.subscriptionId(), domainNameLabel, this.apiVersion(), this.acceptLanguage(), this.userAgent());
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
        if (this.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null."));
            return null;
        }
        final String domainNameLabel = null;
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.subscriptionId(), domainNameLabel, this.apiVersion(), this.acceptLanguage(), this.userAgent());
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
        if (this.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.subscriptionId() is required and cannot be null.");
        }
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.subscriptionId(), domainNameLabel, this.apiVersion(), this.acceptLanguage(), this.userAgent());
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
        if (this.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.checkDnsNameAvailability(location, this.subscriptionId(), domainNameLabel, this.apiVersion(), this.acceptLanguage(), this.userAgent());
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
        return new AzureServiceResponseBuilder<DnsNameAvailabilityResultInner, CloudException>(this.restClient().mapperAdapter())
                .register(200, new TypeToken<DnsNameAvailabilityResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
