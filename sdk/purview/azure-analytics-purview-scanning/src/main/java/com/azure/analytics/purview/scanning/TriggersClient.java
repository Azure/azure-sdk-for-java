package com.azure.analytics.purview.scanning;

import com.azure.analytics.purview.scanning.implementation.TriggersImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

/** Initializes a new instance of the synchronous MicrosoftScanningClient type. */
@ServiceClient(builder = MicrosoftScanningClientBuilder.class)
public final class TriggersClient {
    private final TriggersImpl serviceClient;

    /**
     * Initializes an instance of Triggers client.
     *
     * @param serviceClient the service client implementation.
     */
    TriggersClient(TriggersImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Gets trigger information.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return trigger information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData getTrigger(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.getTrigger(dataSourceName, scanName, requestOptions);
    }

    /**
     * Gets trigger information.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @param context The context to associate with this operation.
     * @return trigger information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getTriggerWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.getTriggerWithResponse(dataSourceName, scanName, requestOptions, context);
    }

    /**
     * Creates an instance of a trigger.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
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
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param body The body parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData createTrigger(
            String dataSourceName, String scanName, BinaryData body, RequestOptions requestOptions) {
        return this.serviceClient.createTrigger(dataSourceName, scanName, body, requestOptions);
    }

    /**
     * Creates an instance of a trigger.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
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
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param body The body parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @param context The context to associate with this operation.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createTriggerWithResponse(
            String dataSourceName, String scanName, BinaryData body, RequestOptions requestOptions, Context context) {
        return this.serviceClient.createTriggerWithResponse(dataSourceName, scanName, body, requestOptions, context);
    }

    /**
     * Deletes the trigger associated with the scan.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData deleteTrigger(String dataSourceName, String scanName, RequestOptions requestOptions) {
        return this.serviceClient.deleteTrigger(dataSourceName, scanName, requestOptions);
    }

    /**
     * Deletes the trigger associated with the scan.
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
     *         recurrence: {
     *             frequency: String(Week/Month)
     *             interval: Integer
     *             startTime: OffsetDateTime
     *             endTime: OffsetDateTime
     *             schedule: {
     *                 additionalProperties: {
     *                     String: Object
     *                 }
     *                 minutes: [
     *                     int
     *                 ]
     *                 hours: [
     *                     int
     *                 ]
     *                 weekDays: [
     *                     String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                 ]
     *                 monthDays: [
     *                     int
     *                 ]
     *                 monthlyOccurrences: [
     *                     {
     *                         additionalProperties: {
     *                             String: Object
     *                         }
     *                         day: String(Sunday/Monday/Tuesday/Wednesday/Thursday/Friday/Saturday)
     *                         occurrence: Integer
     *                     }
     *                 ]
     *             }
     *             timeZone: String
     *         }
     *         recurrenceInterval: String
     *         createdAt: OffsetDateTime
     *         lastModifiedAt: OffsetDateTime
     *         lastScheduled: OffsetDateTime
     *         scanLevel: String(Full/Incremental)
     *         incrementalScanStartTime: OffsetDateTime
     *     }
     * }
     * }</pre>
     *
     * @param dataSourceName The dataSourceName parameter.
     * @param scanName The scanName parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @param context The context to associate with this operation.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteTriggerWithResponse(
            String dataSourceName, String scanName, RequestOptions requestOptions, Context context) {
        return this.serviceClient.deleteTriggerWithResponse(dataSourceName, scanName, requestOptions, context);
    }
}
