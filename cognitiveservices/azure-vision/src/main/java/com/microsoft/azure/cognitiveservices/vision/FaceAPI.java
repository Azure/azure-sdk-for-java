/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision;

import com.microsoft.rest.RestClient;
import com.microsoft.azure.cognitiveservices.vision.models.AzureRegions;

/**
 * The interface for FaceAPI class.
 */
public interface FaceAPI {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * The default base URL.
     */
    String DEFAULT_BASE_URL = "https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0";

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    AzureRegions azureRegion();

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    FaceAPI withAzureRegion(AzureRegions azureRegion);

    /**
     * Gets the Faces object to access its operations.
     * @return the Faces object.
     */
    Faces faces();

    /**
     * Gets the Persons object to access its operations.
     * @return the Persons object.
     */
    Persons persons();

    /**
     * Gets the PersonGroups object to access its operations.
     * @return the PersonGroups object.
     */
    PersonGroups personGroups();

    /**
     * Gets the FaceLists object to access its operations.
     * @return the FaceLists object.
     */
    FaceLists faceLists();

}
