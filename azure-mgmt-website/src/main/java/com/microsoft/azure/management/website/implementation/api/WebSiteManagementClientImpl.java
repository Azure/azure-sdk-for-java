/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;

/**
 * Initializes a new instance of the WebSiteManagementClientImpl class.
 */
public final class WebSiteManagementClientImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Subscription Id. */
    private String subscriptionId;

    /**
     * Gets Subscription Id.
     *
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Sets Subscription Id.
     *
     * @param subscriptionId the subscriptionId value.
     */
    public void withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /** API Version. */
    private String apiVersion;

    /**
     * Gets API Version.
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
     * The CertificateOrdersInner object to access its operations.
     */
    private CertificateOrdersInner certificateOrders;

    /**
     * Gets the CertificateOrdersInner object to access its operations.
     * @return the CertificateOrdersInner object.
     */
    public CertificateOrdersInner certificateOrders() {
        return this.certificateOrders;
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
     * The ClassicMobileServicesInner object to access its operations.
     */
    private ClassicMobileServicesInner classicMobileServices;

    /**
     * Gets the ClassicMobileServicesInner object to access its operations.
     * @return the ClassicMobileServicesInner object.
     */
    public ClassicMobileServicesInner classicMobileServices() {
        return this.classicMobileServices;
    }

    /**
     * The DomainsInner object to access its operations.
     */
    private DomainsInner domains;

    /**
     * Gets the DomainsInner object to access its operations.
     * @return the DomainsInner object.
     */
    public DomainsInner domains() {
        return this.domains;
    }

    /**
     * The GlobalsInner object to access its operations.
     */
    private GlobalsInner globals;

    /**
     * Gets the GlobalsInner object to access its operations.
     * @return the GlobalsInner object.
     */
    public GlobalsInner globals() {
        return this.globals;
    }

    /**
     * The GlobalCertificateOrdersInner object to access its operations.
     */
    private GlobalCertificateOrdersInner globalCertificateOrders;

    /**
     * Gets the GlobalCertificateOrdersInner object to access its operations.
     * @return the GlobalCertificateOrdersInner object.
     */
    public GlobalCertificateOrdersInner globalCertificateOrders() {
        return this.globalCertificateOrders;
    }

    /**
     * The GlobalDomainRegistrationsInner object to access its operations.
     */
    private GlobalDomainRegistrationsInner globalDomainRegistrations;

    /**
     * Gets the GlobalDomainRegistrationsInner object to access its operations.
     * @return the GlobalDomainRegistrationsInner object.
     */
    public GlobalDomainRegistrationsInner globalDomainRegistrations() {
        return this.globalDomainRegistrations;
    }

    /**
     * The GlobalResourceGroupsInner object to access its operations.
     */
    private GlobalResourceGroupsInner globalResourceGroups;

    /**
     * Gets the GlobalResourceGroupsInner object to access its operations.
     * @return the GlobalResourceGroupsInner object.
     */
    public GlobalResourceGroupsInner globalResourceGroups() {
        return this.globalResourceGroups;
    }

    /**
     * The HostingEnvironmentsInner object to access its operations.
     */
    private HostingEnvironmentsInner hostingEnvironments;

    /**
     * Gets the HostingEnvironmentsInner object to access its operations.
     * @return the HostingEnvironmentsInner object.
     */
    public HostingEnvironmentsInner hostingEnvironments() {
        return this.hostingEnvironments;
    }

    /**
     * The ManagedHostingEnvironmentsInner object to access its operations.
     */
    private ManagedHostingEnvironmentsInner managedHostingEnvironments;

    /**
     * Gets the ManagedHostingEnvironmentsInner object to access its operations.
     * @return the ManagedHostingEnvironmentsInner object.
     */
    public ManagedHostingEnvironmentsInner managedHostingEnvironments() {
        return this.managedHostingEnvironments;
    }

    /**
     * The ProvidersInner object to access its operations.
     */
    private ProvidersInner providers;

    /**
     * Gets the ProvidersInner object to access its operations.
     * @return the ProvidersInner object.
     */
    public ProvidersInner providers() {
        return this.providers;
    }

    /**
     * The RecommendationsInner object to access its operations.
     */
    private RecommendationsInner recommendations;

    /**
     * Gets the RecommendationsInner object to access its operations.
     * @return the RecommendationsInner object.
     */
    public RecommendationsInner recommendations() {
        return this.recommendations;
    }

    /**
     * The ServerFarmsInner object to access its operations.
     */
    private ServerFarmsInner serverFarms;

    /**
     * Gets the ServerFarmsInner object to access its operations.
     * @return the ServerFarmsInner object.
     */
    public ServerFarmsInner serverFarms() {
        return this.serverFarms;
    }

    /**
     * The SitesInner object to access its operations.
     */
    private SitesInner sites;

    /**
     * Gets the SitesInner object to access its operations.
     * @return the SitesInner object.
     */
    public SitesInner sites() {
        return this.sites;
    }

    /**
     * The TopLevelDomainsInner object to access its operations.
     */
    private TopLevelDomainsInner topLevelDomains;

    /**
     * Gets the TopLevelDomainsInner object to access its operations.
     * @return the TopLevelDomainsInner object.
     */
    public TopLevelDomainsInner topLevelDomains() {
        return this.topLevelDomains;
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
     * Initializes an instance of WebSiteManagementClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public WebSiteManagementClientImpl(ServiceClientCredentials credentials) {
        this("https://management.azure.com", credentials);
    }

    /**
     * Initializes an instance of WebSiteManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public WebSiteManagementClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        this(new RestClient.Builder(baseUrl)
                .withMapperAdapter(new AzureJacksonMapperAdapter())
                .withCredentials(credentials)
                .build());
    }

    /**
     * Initializes an instance of WebSiteManagementClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public WebSiteManagementClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2015-08-01";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.certificateOrders = new CertificateOrdersInner(restClient().retrofit(), this);
        this.certificates = new CertificatesInner(restClient().retrofit(), this);
        this.classicMobileServices = new ClassicMobileServicesInner(restClient().retrofit(), this);
        this.domains = new DomainsInner(restClient().retrofit(), this);
        this.globals = new GlobalsInner(restClient().retrofit(), this);
        this.globalCertificateOrders = new GlobalCertificateOrdersInner(restClient().retrofit(), this);
        this.globalDomainRegistrations = new GlobalDomainRegistrationsInner(restClient().retrofit(), this);
        this.globalResourceGroups = new GlobalResourceGroupsInner(restClient().retrofit(), this);
        this.hostingEnvironments = new HostingEnvironmentsInner(restClient().retrofit(), this);
        this.managedHostingEnvironments = new ManagedHostingEnvironmentsInner(restClient().retrofit(), this);
        this.providers = new ProvidersInner(restClient().retrofit(), this);
        this.recommendations = new RecommendationsInner(restClient().retrofit(), this);
        this.serverFarms = new ServerFarmsInner(restClient().retrofit(), this);
        this.sites = new SitesInner(restClient().retrofit(), this);
        this.topLevelDomains = new TopLevelDomainsInner(restClient().retrofit(), this);
        this.usages = new UsagesInner(restClient().retrofit(), this);
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
                "WebSiteManagementClient, 2015-08-01");
    }
}
