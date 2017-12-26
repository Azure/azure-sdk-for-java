/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.azure.cognitiveservices.faceapi.AzureRegions;

/**
 * Initializes a new instance of the FaceAPIImpl class.
 */
public class FaceAPIImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'. */
    private AzureRegions azureRegion;

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    public AzureRegions azureRegion() {
        return this.azureRegion;
    }

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    public FaceAPIImpl withAzureRegion(AzureRegions azureRegion) {
        this.azureRegion = azureRegion;
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
    public FaceAPIImpl withAcceptLanguage(String acceptLanguage) {
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
    public FaceAPIImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
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
    public FaceAPIImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * The FacesInner object to access its operations.
     */
    private FacesInner faces;

    /**
     * Gets the FacesInner object to access its operations.
     * @return the FacesInner object.
     */
    public FacesInner faces() {
        return this.faces;
    }

    /**
     * The PersonsInner object to access its operations.
     */
    private PersonsInner persons;

    /**
     * Gets the PersonsInner object to access its operations.
     * @return the PersonsInner object.
     */
    public PersonsInner persons() {
        return this.persons;
    }

    /**
     * The PersonGroupsInner object to access its operations.
     */
    private PersonGroupsInner personGroups;

    /**
     * Gets the PersonGroupsInner object to access its operations.
     * @return the PersonGroupsInner object.
     */
    public PersonGroupsInner personGroups() {
        return this.personGroups;
    }

    /**
     * The FaceListsInner object to access its operations.
     */
    private FaceListsInner faceLists;

    /**
     * Gets the FaceListsInner object to access its operations.
     * @return the FaceListsInner object.
     */
    public FaceListsInner faceLists() {
        return this.faceLists;
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param credentials the management credentials for Azure
     */
    public FaceAPIImpl(ServiceClientCredentials credentials) {
        this("https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0", credentials);
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    private FaceAPIImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public FaceAPIImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.faces = new FacesInner(restClient().retrofit(), this);
        this.persons = new PersonsInner(restClient().retrofit(), this);
        this.personGroups = new PersonGroupsInner(restClient().retrofit(), this);
        this.faceLists = new FaceListsInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s)", super.userAgent(), "FaceAPI", "1.0");
    }
}
