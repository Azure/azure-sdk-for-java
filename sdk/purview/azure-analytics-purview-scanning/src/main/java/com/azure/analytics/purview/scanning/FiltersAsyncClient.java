package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.FiltersImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class, isAsync = true)
public final class FiltersAsyncClient {
    private final FiltersImpl serviceClient;

    /**
     * Initializes an instance of Filters client.
     *
     * @param serviceClient the service client implementation.
     */
    FiltersAsyncClient(FiltersImpl serviceClient) {
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
    public Mono<Response<BinaryData>> getWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.getWithResponseAsync(dataSourceName, scanName, requestOptions);
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
    public Mono<BinaryData> get(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.getAsync(dataSourceName, scanName, requestOptions);
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
    public Mono<Response<BinaryData>> createOrUpdateWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateWithResponseAsync(dataSourceName, scanName, requestOptions);
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
    public Mono<BinaryData> createOrUpdate(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdateAsync(dataSourceName, scanName, requestOptions);
    }
}
