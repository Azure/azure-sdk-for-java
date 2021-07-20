package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.ClassificationRulesImpl;
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
public final class ClassificationRulesClient {
    private final ClassificationRulesImpl serviceClient;

    /**
     * Initializes an instance of ClassificationRules client.
     *
     * @param serviceClient the service client implementation.
     */
    ClassificationRulesClient(ClassificationRulesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData get(String classificationRuleName, RequestOptions requestOptions) {
        return this.serviceClient.get(classificationRuleName, requestOptions);
    }

    /**
     * Get a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getWithResponse(classificationRuleName, requestOptions, context);
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createOrUpdate(String classificationRuleName, RequestOptions requestOptions) {
        return this.serviceClient.createOrUpdate(classificationRuleName, requestOptions);
    }

    /**
     * Creates or Updates a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createOrUpdateWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createOrUpdateWithResponse(classificationRuleName, requestOptions, context);
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData delete(String classificationRuleName, RequestOptions requestOptions) {
        return this.serviceClient.delete(classificationRuleName, requestOptions);
    }

    /**
     * Deletes a classification rule.
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
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteWithResponse(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteWithResponse(classificationRuleName, requestOptions, context);
    }

    /**
     * List classification rules in Account.
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
     * List classification rules in Account.
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
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByClassificationRuleName(
            String classificationRuleName, RequestOptions requestOptions) {
        return this.serviceClient.listVersionsByClassificationRuleName(classificationRuleName, requestOptions);
    }

    /**
     * Lists the rule versions of a classification rule.
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
     *         }
     *     ]
     *     nextLink: String
     *     count: Long
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<BinaryData> listVersionsByClassificationRuleName(
            String classificationRuleName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.listVersionsByClassificationRuleName(classificationRuleName, requestOptions, context);
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData tagClassificationVersion(
            String classificationRuleName, int classificationRuleVersion, RequestOptions requestOptions) {
        return this.serviceClient.tagClassificationVersion(
                classificationRuleName, classificationRuleVersion, requestOptions);
    }

    /**
     * Sets Classification Action on a specific classification rule version.
     *
     * <p><strong>Query Parameters</strong>
     *
     * <table border="1">
     *     <caption>Query Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>action</td><td>String</td><td>Yes</td><td>The action parameter</td></tr>
     *     <tr><td>apiVersion</td><td>String</td><td>Yes</td><td>Api Version</td></tr>
     * </table>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     scanResultId: UUID
     *     startTime: OffsetDateTime
     *     endTime: OffsetDateTime
     *     status: String(Accepted/InProgress/TransientFailure/Succeeded/Failed/Canceled)
     *     error: {
     *         code: String
     *         message: String
     *         target: String
     *         details: [
     *             {
     *                 code: String
     *                 message: String
     *                 target: String
     *                 details: [
     *                     (recursive schema, see above)
     *                 ]
     *             }
     *         ]
     *     }
     * }
     * }</pre>
     *
     * @param classificationRuleName The classificationRuleName parameter.
     * @param classificationRuleVersion The classificationRuleVersion parameter.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> tagClassificationVersionWithResponse(
            String classificationRuleName,
            int classificationRuleVersion,
            RequestOptions requestOptions,
            Context context) {
        return this.serviceClient.tagClassificationVersionWithResponse(
                classificationRuleName, classificationRuleVersion, requestOptions, context);
    }
}
