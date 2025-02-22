// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.FiltersImpl;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

/**
 * Initializes a new instance of the synchronous PurviewScanningClient type.
 */
@ServiceClient(builder = PurviewScanningClientBuilder.class)
public final class FiltersClient {
    @Generated
    private final FiltersImpl serviceClient;

    /**
     * Initializes an instance of FiltersClient class.
     * 
     * @param serviceClient the service client implementation.
     */
    @Generated
    FiltersClient(FiltersImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get a filter.
     * <p><strong>Response Body Schema</strong></p>
     * 
     * <pre>
     * {@code
     * {
     *     id: String (Optional)
     *     name: String (Optional)
     *     properties (Optional): {
     *         excludeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *         includeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *     }
     * }
     * }
     * </pre>
     * 
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a filter along with {@link Response}.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.getWithResponse(dataSourceName, scanName, requestOptions);
    }

    /**
     * Creates or updates a filter.
     * <p><strong>Header Parameters</strong></p>
     * <table border="1">
     * <caption>Header Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>Content-Type</td><td>String</td><td>No</td><td>The content type. Allowed values:
     * "application/json".</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addHeader}
     * <p><strong>Request Body Schema</strong></p>
     * 
     * <pre>
     * {@code
     * {
     *     id: String (Optional)
     *     name: String (Optional)
     *     properties (Optional): {
     *         excludeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *         includeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *     }
     * }
     * }
     * </pre>
     * 
     * <p><strong>Response Body Schema</strong></p>
     * 
     * <pre>
     * {@code
     * {
     *     id: String (Optional)
     *     name: String (Optional)
     *     properties (Optional): {
     *         excludeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *         includeUriPrefixes (Optional): [
     *             String (Optional)
     *         ]
     *     }
     * }
     * }
     * </pre>
     * 
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response}.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(String dataSourceName, String scanName,
        RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateWithResponse(dataSourceName, scanName, requestOptions);
    }
}
