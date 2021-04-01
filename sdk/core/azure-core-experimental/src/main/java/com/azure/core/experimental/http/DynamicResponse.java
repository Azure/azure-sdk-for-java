// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;

/**
 * A response received from sending a DynamicRequest. This class enables inspecting the HTTP response status,
 * response headers and the response body. Response body is represented as a {@link BinaryData} which can then to
 * deserialized into a string representation, an object or just bytes. If the response is a JSON, then the string
 * representation will return the JSON.
 *
 * <p>
 * To demonstrate how this class can be used to read the response, let's use Pet Store service as an example. The
 * list of APIs available on this service are <a href="https://petstore.swagger.io/#/pet">documented in the swagger definition.</a>
 * </p>
 *
 * <p><strong>Reading the response of a HTTP GET request to get a pet from a petId</strong></p>
 * The structure of the JSON response for the GET call is shown below:
 * <pre>{@code
 * {
 *   "id": 0,
 *   "category": {
 *     "id": 0,
 *     "name": "string"
 *   },
 *   "name": "doggie",
 *   "photoUrls": [
 *     "string"
 *   ],
 *   "tags": [
 *     {
 *       "id": 0,
 *       "name": "string"
 *     }
 *   ],
 *   "status": "available"
 * }
 * }</pre>
 *
 * This sample shows how to read the JSON response from the service and inspecting specific properties of the response.
 *
 * {@codesnippet com.azure.core.experimental.http.dynamicresponse.readresponse}
 */
public final class DynamicResponse {
    private final HttpResponse response;
    private final BinaryData body;

    /**
     * Creates an instance of the DynamicResponse.
     * @param response the underlying HTTP response
     * @param body the full HTTP response body
     */
    public DynamicResponse(HttpResponse response, BinaryData body) {
        this.response = response;
        this.body = body;
    }

    /**
     * Returns the HTTP status code of the response.
     * @return the HTTP status code of the response
     */
    public int getStatusCode() {
        return response.getStatusCode();
    }

    /**
     * Returns the HTTP headers of the response.
     * @return the HTTP headers of the response
     */
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    /**
     * Returns the original HTTP request sent to the service.
     * @return the original HTTP request sent to get this response
     */
    public HttpRequest getRequest() {
        return response.getRequest();
    }

    /**
     * Returns the HTTP response body represented as a {@link BinaryData}.
     * @return the response body
     */
    public BinaryData getBody() {
        return body;
    }
}
