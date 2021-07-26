package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.KeyVaultConnectionsImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class, isAsync = true)
public final class KeyVaultConnectionsAsyncClient {
    private final KeyVaultConnectionsImpl serviceClient;

    /**
     * Initializes an instance of KeyVaultConnections client.
     *
     * @param serviceClient the service client implementation.
     */
    KeyVaultConnectionsAsyncClient(KeyVaultConnectionsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Gets key vault information.
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
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getWithResponse(String keyVaultName, RequestOptions requestOptions) {
        return this.serviceClient.getWithResponseAsync(keyVaultName, requestOptions);
    }

    /**
     * Gets key vault information.
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
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> get(String keyVaultName, RequestOptions requestOptions) {
        return this.serviceClient.getAsync(keyVaultName, requestOptions);
    }

    /**
     * Creates an instance of a key vault connection.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createWithResponse(
            String keyVaultName, BinaryData body, RequestOptions requestOptions) {
        return this.serviceClient.createWithResponseAsync(keyVaultName, body, requestOptions);
    }

    /**
     * Creates an instance of a key vault connection.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     properties: {
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * (recursive schema, see above)
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> create(String keyVaultName, BinaryData body, RequestOptions requestOptions) {
        return this.serviceClient.createAsync(keyVaultName, body, requestOptions);
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteWithResponse(String keyVaultName, RequestOptions requestOptions) {
        return this.serviceClient.deleteWithResponseAsync(keyVaultName, requestOptions);
    }

    /**
     * Deletes the key vault connection associated with the account.
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
     *         baseUrl: String
     *         description: String
     *     }
     * }
     * }</pre>
     *
     * @param keyVaultName The keyVaultName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> delete(String keyVaultName, RequestOptions requestOptions) {
        return this.serviceClient.deleteAsync(keyVaultName, requestOptions);
    }

    /**
     * List key vault connections in account.
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
     *     value: [
     *         {
     *             id: String
     *             name: String
     *             properties: {
     *                 baseUrl: String
     *                 description: String
     *             }
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<BinaryData> listAll(RequestOptions requestOptions) {
        return this.serviceClient.listAllAsync(requestOptions);
    }
}
