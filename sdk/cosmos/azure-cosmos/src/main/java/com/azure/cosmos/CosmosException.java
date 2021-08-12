// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.exception.AzureException;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.CosmosDiagnostics.USER_AGENT_KEY;

/**
 * This class defines a custom exception type for all operations on
 * CosmosClient in the Azure Cosmos DB database service. Applications are
 * expected to catch CosmosException and handle errors as appropriate when
 * calling methods on CosmosClient.
 * <p>
 * Errors coming from the service during normal execution are converted to
 * CosmosException before returning to the application with the following
 * exception:
 * <p>
 * When a BE error is encountered during a QueryIterable&lt;T&gt; iteration, an
 * IllegalStateException is thrown instead of CosmosException.
 * <p>
 * When a transport level error happens that request is not able to reach the
 * service, an IllegalStateException is thrown instead of CosmosException.
 */
public class CosmosException extends AzureException {
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String USER_AGENT = Utils.getUserAgent();
    private final int statusCode;
    private final Map<String, String> responseHeaders;

    private CosmosDiagnostics cosmosDiagnostics;
    private RequestTimeline requestTimeline;
    private RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
    private CosmosError cosmosError;
    private int rntbdChannelTaskQueueSize;

    private RntbdEndpointStatistics rntbdEndpointStatistics;

    long lsn;
    String partitionKeyRangeId;
    Map<String, String> requestHeaders;
    Uri requestUri;
    String resourceAddress;
    private int requestPayloadLength;
    private int rntbdPendingRequestQueueSize;
    private int rntbdRequestLength;
    private int rntbdResponseLength;
    private boolean sendingRequestHasStarted;

    protected CosmosException(int statusCode, String message, Map<String, String> responseHeaders, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders == null ? new HashMap<>() : new HashMap<>(responseHeaders);
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param statusCode the http status code of the response.
     * @param errorMessage the error message.
     */
    protected CosmosException(int statusCode, String errorMessage) {
        this(statusCode, errorMessage, null, null);
        this.cosmosError = new CosmosError();
        ModelBridgeInternal.setProperty(cosmosError, Constants.Properties.MESSAGE, errorMessage);
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param statusCode the http status code of the response.
     * @param innerException the original exception.
     */
    protected CosmosException(int statusCode, Exception innerException) {
        this(statusCode, null, null, innerException);
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param statusCode the http status code of the response.
     * @param cosmosErrorResource the error resource object.
     * @param responseHeaders the response headers.
     */
    protected CosmosException(int statusCode, CosmosError cosmosErrorResource, Map<String, String> responseHeaders) {
        this(/* resourceAddress */ null, statusCode, cosmosErrorResource, responseHeaders);
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param resourceAddress the address of the resource the request is associated with.
     * @param statusCode the http status code of the response.
     * @param cosmosErrorResource the error resource object.
     * @param responseHeaders the response headers.
     */

    protected CosmosException(String resourceAddress,
                              int statusCode,
                              CosmosError cosmosErrorResource,
                              Map<String, String> responseHeaders) {
        this(statusCode, cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, null);
        this.resourceAddress = resourceAddress;
        this.cosmosError = cosmosErrorResource;
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param resourceAddress the address of the resource the request is associated with.
     * @param statusCode the http status code of the response.
     * @param cosmosErrorResource the error resource object.
     * @param responseHeaders the response headers.
     * @param cause the inner exception
     */

    protected CosmosException(String resourceAddress,
                              int statusCode,
                              CosmosError cosmosErrorResource,
                              Map<String, String> responseHeaders,
                              Throwable cause) {
        this(statusCode, cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, cause);
        this.resourceAddress = resourceAddress;
        this.cosmosError = cosmosErrorResource;
    }

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param message the string message.
     * @param statusCode the http status code of the response.
     * @param exception the exception object.
     * @param responseHeaders the response headers.
     * @param resourceAddress the address of the resource the request is associated with.
     */
    protected CosmosException(String message, Exception exception, Map<String, String> responseHeaders, int statusCode,
                              String resourceAddress) {
        this(statusCode, message, responseHeaders, exception);
        this.resourceAddress = resourceAddress;
    }

    @Override
    public String getMessage() {
        try {
            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("innerErrorMessage", innerErrorMessage());
            if (cosmosDiagnostics != null) {
                cosmosDiagnostics.fillCosmosDiagnostics(messageNode, null);
            }
            return mapper.writeValueAsString(messageNode);
        } catch (JsonProcessingException e) {
            if (cosmosDiagnostics == null) {
                return innerErrorMessage();
            }
            return innerErrorMessage() + ", " + cosmosDiagnostics.toString();
        }
    }

    /**
     * Gets the activity ID associated with the request.
     *
     * @return the activity ID.
     */
    public String getActivityId() {
        if (this.responseHeaders != null) {
            return this.responseHeaders.get(HttpConstants.HttpHeaders.ACTIVITY_ID);
        }

        return null;
    }

    /**
     * Gets the http status code.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the sub status code.
     *
     * @return the status code.
     */
    public int getSubStatusCode() {
        int code = HttpConstants.SubStatusCodes.UNKNOWN;
        if (this.responseHeaders != null) {
            String subStatusString = this.responseHeaders.get(HttpConstants.HttpHeaders.SUB_STATUS);
            if (StringUtils.isNotEmpty(subStatusString)) {
                try {
                    code = Integer.parseInt(subStatusString);
                } catch (NumberFormatException e) {
                    // If value cannot be parsed as Integer, return Unknown.
                }
            }
        }

        return code;
    }

    void setSubStatusCode(int subStatusCode) {
        this.responseHeaders.put(HttpConstants.HttpHeaders.SUB_STATUS, Integer.toString(subStatusCode));
    }

    /**
     * Gets the error code associated with the exception.
     *
     * @return the error.
     */
    CosmosError getError() {
        return this.cosmosError;
    }

    void setError(CosmosError cosmosError) {
        this.cosmosError = cosmosError;
    }

    /**
     * Gets the recommended time duration after which the client can retry failed
     * requests
     *
     * @return the recommended time duration after which the client can retry failed
     * requests.
     */
    public Duration getRetryAfterDuration() {
        long retryIntervalInMilliseconds = 0;

        if (this.responseHeaders != null) {
            String header = this.responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);

            if (StringUtils.isNotEmpty(header)) {
                try {
                    retryIntervalInMilliseconds = Long.parseLong(header);
                } catch (NumberFormatException e) {
                    // If the value cannot be parsed as long, return 0.
                }
            }
        }

        //
        // In the absence of explicit guidance from the backend, don't introduce
        // any unilateral retry delays here.
        return Duration.ofMillis(retryIntervalInMilliseconds);
    }

    /**
     * Gets the response headers as key-value pairs
     *
     * @return the response headers
     */
    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Gets the resource address associated with this exception.
     *
     * @return the resource address associated with this exception.
     */
    String getResourceAddress() {
        return this.resourceAddress;
    }

    /**
     * Gets the Cosmos Diagnostic Statistics associated with this exception.
     *
     * @return Cosmos Diagnostic Statistics associated with this exception.
     */
    public CosmosDiagnostics getDiagnostics() {
        return cosmosDiagnostics;
    }

    CosmosException setDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
        return this;
    }

    /**
     * Gets the request charge as request units (RU) consumed by the operation.
     * <p>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    public double getRequestCharge() {
        String value = this.getResponseHeaders().get(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    @Override
    public String toString() {
        try {
            ObjectNode exceptionMessageNode = mapper.createObjectNode();
            exceptionMessageNode.put("ClassName", getClass().getSimpleName());
            exceptionMessageNode.put(USER_AGENT_KEY, USER_AGENT);
            exceptionMessageNode.put("statusCode", statusCode);
            exceptionMessageNode.put("resourceAddress", resourceAddress);
            if (cosmosError != null) {
                exceptionMessageNode.put("error", cosmosError.toJson());
            }

            exceptionMessageNode.put("innerErrorMessage", innerErrorMessage());
            exceptionMessageNode.put("causeInfo", causeInfo());
            if (responseHeaders != null) {
                exceptionMessageNode.put("responseHeaders", responseHeaders.toString());
            }

            List<Map.Entry<String, String>> filterRequestHeaders = filterSensitiveData(requestHeaders);
            if (filterRequestHeaders != null) {
                exceptionMessageNode.put("requestHeaders", filterRequestHeaders.toString());
            }

            if(this.cosmosDiagnostics != null) {
                cosmosDiagnostics.fillCosmosDiagnostics(exceptionMessageNode, null);
            }

            return mapper.writeValueAsString(exceptionMessageNode);
        } catch (JsonProcessingException ex) {
            return getClass().getSimpleName() + "{" + USER_AGENT_KEY +"=" + USER_AGENT + ", error=" + cosmosError + ", " +
                "resourceAddress='"
                + resourceAddress + ", statusCode=" + statusCode + ", message=" + getMessage()
                + ", causeInfo=" + causeInfo() + ", responseHeaders=" + responseHeaders + ", requestHeaders="
                + filterSensitiveData(requestHeaders) + '}';
        }
    }

    String innerErrorMessage() {
        String innerErrorMessage = super.getMessage();
        if (cosmosError != null) {
            innerErrorMessage = cosmosError.getMessage();
            if (innerErrorMessage == null) {
                innerErrorMessage = String.valueOf(
                    ModelBridgeInternal.getObjectFromJsonSerializable(cosmosError, "Errors"));
            }
        }
        return innerErrorMessage;
    }

    private String causeInfo() {
        Throwable cause = getCause();
        if (cause != null) {
            return String.format("[class: %s, message: %s]", cause.getClass(), cause.getMessage());
        }
        return null;
    }

    private List<Map.Entry<String, String>> filterSensitiveData(Map<String, String> requestHeaders) {
        if (requestHeaders == null) {
            return null;
        }
        return requestHeaders.entrySet().stream().filter(entry -> !HttpConstants.HttpHeaders.AUTHORIZATION.equalsIgnoreCase(entry.getKey()))
                             .collect(Collectors.toList());
    }

    RequestTimeline getRequestTimeline() {
        return this.requestTimeline;
    }

    void setRequestTimeline(RequestTimeline requestTimeline) {
        this.requestTimeline = requestTimeline;
    }

    RntbdChannelAcquisitionTimeline getChannelAcquisitionTimeline() {
        return this.channelAcquisitionTimeline;
    }

    void setChannelAcquisitionTimeline(RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        this.channelAcquisitionTimeline = channelAcquisitionTimeline;
    }

    void setResourceAddress(String resourceAddress) {
        this.resourceAddress = resourceAddress;
    }

    void setRntbdServiceEndpointStatistics(RntbdEndpointStatistics rntbdEndpointStatistics) {
        this.rntbdEndpointStatistics = rntbdEndpointStatistics;
    }

    RntbdEndpointStatistics getRntbdServiceEndpointStatistics() {
        return this.rntbdEndpointStatistics;
    }

    void setRntbdRequestLength(int rntbdRequestLength) {
        this.rntbdRequestLength = rntbdRequestLength;
    }

    int getRntbdRequestLength() {
        return this.rntbdRequestLength;
    }

    void setRntbdResponseLength(int rntbdResponseLength) {
        this.rntbdResponseLength = rntbdResponseLength;
    }

    int getRntbdResponseLength() {
        return this.rntbdResponseLength;
    }

    void setRequestPayloadLength(int requestBodyLength) {
        this.requestPayloadLength = requestBodyLength;
    }

    int getRequestPayloadLength() {
        return this.requestPayloadLength;
    }

    boolean hasSendingRequestStarted() {
        return this.sendingRequestHasStarted;
    }

    void setSendingRequestHasStarted(boolean hasSendingRequestStarted) {
        this.sendingRequestHasStarted = hasSendingRequestStarted;
    }

    int getRntbdChannelTaskQueueSize() {
        return this.rntbdChannelTaskQueueSize;
    }

    void setRntbdChannelTaskQueueSize(int rntbdChannelTaskQueueSize) {
        this.rntbdChannelTaskQueueSize = rntbdChannelTaskQueueSize;
    }

    int getRntbdPendingRequestQueueSize() {
        return this.rntbdChannelTaskQueueSize;
    }

    void setRntbdPendingRequestQueueSize(int rntbdPendingRequestQueueSize) {
        this.rntbdPendingRequestQueueSize = rntbdPendingRequestQueueSize;
    }
}
