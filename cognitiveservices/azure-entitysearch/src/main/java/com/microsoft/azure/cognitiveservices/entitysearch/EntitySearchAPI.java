/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.microsoft.rest.RestClient;

/**
 * The interface for EntitySearchAPI class.
 */
public interface EntitySearchAPI {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * The default base URL.
     */
    String DEFAULT_BASE_URL = "https://api.cognitive.microsoft.com/bing/v7.0";

    /**
     * Gets the Entities object to access its operations.
     * @return the Entities object.
     */
    Entities entities();

}
