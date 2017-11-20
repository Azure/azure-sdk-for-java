/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.microsoft.azure.cognitiveservices.faceapi.FaceAPI;
import com.microsoft.azure.cognitiveservices.faceapi.Faces;
import com.microsoft.azure.cognitiveservices.faceapi.Persons;
import com.microsoft.azure.cognitiveservices.faceapi.PersonGroups;
import com.microsoft.azure.cognitiveservices.faceapi.FaceLists;
import com.microsoft.azure.cognitiveservices.faceapi.models.AzureRegions;
import com.microsoft.rest.ServiceClient;
import com.microsoft.rest.RestClient;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Initializes a new instance of the FaceAPI class.
 */
public class FaceAPIImpl extends ServiceClient implements FaceAPI {

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

    /**
     * The Faces object to access its operations.
     */
    private Faces faces;

    /**
     * Gets the Faces object to access its operations.
     * @return the Faces object.
     */
    public Faces faces() {
        return this.faces;
    }

    /**
     * The Persons object to access its operations.
     */
    private Persons persons;

    /**
     * Gets the Persons object to access its operations.
     * @return the Persons object.
     */
    public Persons persons() {
        return this.persons;
    }

    /**
     * The PersonGroups object to access its operations.
     */
    private PersonGroups personGroups;

    /**
     * Gets the PersonGroups object to access its operations.
     * @return the PersonGroups object.
     */
    public PersonGroups personGroups() {
        return this.personGroups;
    }

    /**
     * The FaceLists object to access its operations.
     */
    private FaceLists faceLists;

    /**
     * Gets the FaceLists object to access its operations.
     * @return the FaceLists object.
     */
    public FaceLists faceLists() {
        return this.faceLists;
    }

    /**
     * Initializes an instance of FaceAPI client.
     */
    public FaceAPIImpl() {
        this("https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0");
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param baseUrl the base URL of the host
     */
    private FaceAPIImpl(String baseUrl) {
        super(baseUrl);
        initialize();
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param clientBuilder the builder for building an OkHttp client, bundled with user configurations
     * @param restBuilder the builder for building an Retrofit client, bundled with user configurations
     */
    public FaceAPIImpl(OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this("https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0", clientBuilder, restBuilder);
        initialize();
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param clientBuilder the builder for building an OkHttp client, bundled with user configurations
     * @param restBuilder the builder for building an Retrofit client, bundled with user configurations
     */
    private FaceAPIImpl(String baseUrl, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        super(baseUrl, clientBuilder, restBuilder);
        initialize();
    }

    /**
     * Initializes an instance of FaceAPI client.
     *
     * @param restClient the REST client containing pre-configured settings
     */
    public FaceAPIImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    private void initialize() {
        this.faces = new FacesImpl(retrofit(), this);
        this.persons = new PersonsImpl(retrofit(), this);
        this.personGroups = new PersonGroupsImpl(retrofit(), this);
        this.faceLists = new FaceListsImpl(retrofit(), this);
    }
}
