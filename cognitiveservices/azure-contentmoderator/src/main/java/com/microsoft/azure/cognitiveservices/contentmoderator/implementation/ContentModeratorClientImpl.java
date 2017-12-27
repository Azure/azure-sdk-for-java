/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;

/**
 * Initializes a new instance of the ContentModeratorClientImpl class.
 */
public class ContentModeratorClientImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Supported Azure regions for Content Moderator endpoints. Possible values include: 'westus.api.cognitive.microsoft.com', 'westus2.api.cognitive.microsoft.com', 'eastus.api.cognitive.microsoft.com', 'eastus2.api.cognitive.microsoft.com', 'westcentralus.api.cognitive.microsoft.com', 'southcentralus.api.cognitive.microsoft.com', 'westeurope.api.cognitive.microsoft.com', 'northeurope.api.cognitive.microsoft.com', 'southeastasia.api.cognitive.microsoft.com', 'eastasia.api.cognitive.microsoft.com', 'australiaeast.api.cognitive.microsoft.com', 'brazilsouth.api.cognitive.microsoft.com', 'contentmoderatortest.azure-api.net'. */
    private String baseUrl;

    /**
     * Gets Supported Azure regions for Content Moderator endpoints. Possible values include: 'westus.api.cognitive.microsoft.com', 'westus2.api.cognitive.microsoft.com', 'eastus.api.cognitive.microsoft.com', 'eastus2.api.cognitive.microsoft.com', 'westcentralus.api.cognitive.microsoft.com', 'southcentralus.api.cognitive.microsoft.com', 'westeurope.api.cognitive.microsoft.com', 'northeurope.api.cognitive.microsoft.com', 'southeastasia.api.cognitive.microsoft.com', 'eastasia.api.cognitive.microsoft.com', 'australiaeast.api.cognitive.microsoft.com', 'brazilsouth.api.cognitive.microsoft.com', 'contentmoderatortest.azure-api.net'.
     *
     * @return the baseUrl value.
     */
    public String baseUrl() {
        return this.baseUrl;
    }

    /**
     * Sets Supported Azure regions for Content Moderator endpoints. Possible values include: 'westus.api.cognitive.microsoft.com', 'westus2.api.cognitive.microsoft.com', 'eastus.api.cognitive.microsoft.com', 'eastus2.api.cognitive.microsoft.com', 'westcentralus.api.cognitive.microsoft.com', 'southcentralus.api.cognitive.microsoft.com', 'westeurope.api.cognitive.microsoft.com', 'northeurope.api.cognitive.microsoft.com', 'southeastasia.api.cognitive.microsoft.com', 'eastasia.api.cognitive.microsoft.com', 'australiaeast.api.cognitive.microsoft.com', 'brazilsouth.api.cognitive.microsoft.com', 'contentmoderatortest.azure-api.net'.
     *
     * @param baseUrl the baseUrl value.
     * @return the service client itself
     */
    public ContentModeratorClientImpl withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
    public ContentModeratorClientImpl withAcceptLanguage(String acceptLanguage) {
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
    public ContentModeratorClientImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
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
    public ContentModeratorClientImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * The ImageModerationsInner object to access its operations.
     */
    private ImageModerationsInner imageModerations;

    /**
     * Gets the ImageModerationsInner object to access its operations.
     * @return the ImageModerationsInner object.
     */
    public ImageModerationsInner imageModerations() {
        return this.imageModerations;
    }

    /**
     * The TextModerationsInner object to access its operations.
     */
    private TextModerationsInner textModerations;

    /**
     * Gets the TextModerationsInner object to access its operations.
     * @return the TextModerationsInner object.
     */
    public TextModerationsInner textModerations() {
        return this.textModerations;
    }

    /**
     * The ListManagementImageListsInner object to access its operations.
     */
    private ListManagementImageListsInner listManagementImageLists;

    /**
     * Gets the ListManagementImageListsInner object to access its operations.
     * @return the ListManagementImageListsInner object.
     */
    public ListManagementImageListsInner listManagementImageLists() {
        return this.listManagementImageLists;
    }

    /**
     * The ListManagementTermListsInner object to access its operations.
     */
    private ListManagementTermListsInner listManagementTermLists;

    /**
     * Gets the ListManagementTermListsInner object to access its operations.
     * @return the ListManagementTermListsInner object.
     */
    public ListManagementTermListsInner listManagementTermLists() {
        return this.listManagementTermLists;
    }

    /**
     * The ListManagementImagesInner object to access its operations.
     */
    private ListManagementImagesInner listManagementImages;

    /**
     * Gets the ListManagementImagesInner object to access its operations.
     * @return the ListManagementImagesInner object.
     */
    public ListManagementImagesInner listManagementImages() {
        return this.listManagementImages;
    }

    /**
     * The ListManagementTermsInner object to access its operations.
     */
    private ListManagementTermsInner listManagementTerms;

    /**
     * Gets the ListManagementTermsInner object to access its operations.
     * @return the ListManagementTermsInner object.
     */
    public ListManagementTermsInner listManagementTerms() {
        return this.listManagementTerms;
    }

    /**
     * The ReviewsInner object to access its operations.
     */
    private ReviewsInner reviews;

    /**
     * Gets the ReviewsInner object to access its operations.
     * @return the ReviewsInner object.
     */
    public ReviewsInner reviews() {
        return this.reviews;
    }

    /**
     * Initializes an instance of ContentModeratorClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ContentModeratorClientImpl(ServiceClientCredentials credentials) {
        this("https://{baseUrl}", credentials);
    }

    /**
     * Initializes an instance of ContentModeratorClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    private ContentModeratorClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of ContentModeratorClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ContentModeratorClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.imageModerations = new ImageModerationsInner(restClient().retrofit(), this);
        this.textModerations = new TextModerationsInner(restClient().retrofit(), this);
        this.listManagementImageLists = new ListManagementImageListsInner(restClient().retrofit(), this);
        this.listManagementTermLists = new ListManagementTermListsInner(restClient().retrofit(), this);
        this.listManagementImages = new ListManagementImagesInner(restClient().retrofit(), this);
        this.listManagementTerms = new ListManagementTermsInner(restClient().retrofit(), this);
        this.reviews = new ReviewsInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s)", super.userAgent(), "ContentModeratorClient", "1.0");
    }
}
