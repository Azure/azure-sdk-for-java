package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.SystemScanRulesetsImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class)
public final class SystemScanRulesetsClient {
    private final SystemScanRulesetsImpl serviceClient;

    /**
     * Initializes an instance of SystemScanRulesets client.
     *
     * @param serviceClient the service client implementation.
     */
    SystemScanRulesetsClient(SystemScanRulesetsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listAll(RequestOptions requestOptions) {
        return this.serviceClient.listAll(requestOptions);
    }

    /**
     * List all system scan rulesets for an account.
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listAll(RequestOptions requestOptions, Context context) {
        return this.serviceClient.listAll(requestOptions, context);
    }

    /**
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String dataSourceType, RequestOptions requestOptions) {
        return this.serviceClient.get(dataSourceType, requestOptions);
    }

    /**
     * Get a system scan ruleset for a data source.
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
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param dataSourceType The dataSourceType parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(String dataSourceType, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getWithResponse(dataSourceType, requestOptions, context);
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getByVersion(int version, RequestOptions requestOptions) {
        return this.serviceClient.getByVersion(version, requestOptions);
    }

    /**
     * Get a scan ruleset by version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     *
     * @param version The version parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getByVersionWithResponse(int version, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getByVersionWithResponse(version, requestOptions, context);
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getLatest(RequestOptions requestOptions) {
        return this.serviceClient.getLatest(requestOptions);
    }

    /**
     * Get the latest version of a system scan ruleset.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String
     *     name: String
     *     scanRulesetType: String(Custom/System)
     *     status: String(Enabled/Disabled)
     *     version: Integer
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getLatestWithResponse(RequestOptions requestOptions, Context context) {
        return this.serviceClient.getLatestWithResponse(requestOptions, context);
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByDataSource(RequestOptions requestOptions) {
        return this.serviceClient.listVersionsByDataSource(requestOptions);
    }

    /**
     * List system scan ruleset versions in Data catalog.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>dataSourceType</td><td>String</td><td>No</td><td>The dataSourceType parameter</td></tr>
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
     *             scanRulesetType: String(Custom/System)
     *             status: String(Enabled/Disabled)
     *             version: Integer
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByDataSource(RequestOptions requestOptions, Context context) {
        return this.serviceClient.listVersionsByDataSource(requestOptions, context);
    }
}
