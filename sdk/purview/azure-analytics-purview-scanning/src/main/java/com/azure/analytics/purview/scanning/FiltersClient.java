package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.FiltersImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class)
public final class FiltersClient {
    private final FiltersImpl serviceClient;

    /**
     * Initializes an instance of Filters client.
     *
     * @param serviceClient the service client implementation.
     */
    FiltersClient(FiltersImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get a filter.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         excludeUriPrefixes: [
     *             String
     *         ]
     *         includeUriPrefixes: [
     *             String
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.get(dataSourceName, scanName, requestOptions);
    }

    /**
     * Get a filter.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         excludeUriPrefixes: [
     *             String
     *         ]
     *         includeUriPrefixes: [
     *             String
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getWithResponse(dataSourceName, scanName, requestOptions, context);
    }

    /**
     * Creates or updates a filter.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         excludeUriPrefixes: [
     *             String
     *         ]
     *         includeUriPrefixes: [
     *             String
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdate(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdate(dataSourceName, scanName, requestOptions);
    }

    /**
     * Creates or updates a filter.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         excludeUriPrefixes: [
     *             String
     *         ]
     *         includeUriPrefixes: [
     *             String
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createOrUpdateWithResponse(dataSourceName, scanName, requestOptions, context);
    }
}
