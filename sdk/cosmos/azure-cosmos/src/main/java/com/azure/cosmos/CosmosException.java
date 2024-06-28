// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.exception.AzureException;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.batch.BatchExecUtils;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final long MAX_RETRY_AFTER_IN_MS = BatchExecUtils.MAX_RETRY_AFTER_IN_MS;
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Status code
     */
    private final int statusCode;

    /**
     * Response headers
     */
    private final Map<String, String> responseHeaders;

    /**
     * Cosmos diagnostics
     */
    private CosmosDiagnostics cosmosDiagnostics;

    /**
     * Request timeline
     */
    private RequestTimeline requestTimeline;

    /**
     * Channel acquisition timeline
     */
    private RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;

    /**
     * Cosmos error
     */
    private CosmosError cosmosError;

    /**
     * RNTBD endpoint statistics
     */
    private RntbdEndpointStatistics rntbdEndpointStatistics;

    /**
     * RNTBD endpoint statistics
     */
    private RntbdChannelStatistics rntbdChannelStatistics;

    /**
     * LSN
     */
    long lsn;

    /**
     * Partition key range ID
     */
    String partitionKeyRangeId;

    /**
     * Request headers
     */
    Map<String, String> requestHeaders;

    /**
     * Request URI
     */
    private Uri requestUri;

    /**
     * Resource address
     */
    String resourceAddress;

    /**
     * Request payload length
     */
    private int requestPayloadLength;

    /**
     * RNTBD request length
     */
    private int rntbdRequestLength;

    /**
     * RNTBD response length
     */
    private int rntbdResponseLength;

    /**
     * Sending request has started
     */
    private boolean sendingRequestHasStarted;

    /***
     * All selectable replica status.
     */
    private final Map<String, Set<String>> replicaStatusList = new HashMap<>();

    /**
     * Fault injection ruleId
     */
    private String faultInjectionRuleId;

    /**
     * Fault injection rule not applicable evaluation result.
     */
    private List<String> faultInjectionEvaluationResults;

    /**
     * Creates a new instance of the CosmosException class.
     *
     * @param statusCode the http status code of the response.
     * @param message the string message.
     * @param responseHeaders the response headers.
     * @param cause the inner exception
     */
    protected CosmosException(int statusCode, String message, Map<String, String> responseHeaders, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseHeaders = new ConcurrentHashMap<>();

        //  Since ConcurrentHashMap only takes non-null entries, so filtering them before putting them in.
        if (responseHeaders != null) {
            for (Map.Entry<String, String> entry: responseHeaders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.responseHeaders.put(entry.getKey(), entry.getValue());
                }
            }
        }
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
        cosmosError.set(Constants.Properties.MESSAGE, errorMessage, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
     * Returns the error message without any diagnostics - using this method is only useful when
     * also logging the {@link CosmosException#getDiagnostics()} separately. Without diagnostics it will often
     * be impossible to determine the root cause of an error.
     * @return the error message without any diagnostics
     */
    public String getShortMessage() {
        return innerErrorMessage();
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
     * Gets the sub status code. The sub status code is exposed for informational purposes only - new sub status codes
     * can be added anytime and applications should never take a dependency on certain sub status codes. For
     * applications treating errors based on status code is sufficient.
     *
     * @return the sub status code.
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
        // if retry after is not being returned, use -1, so to differentiate with server returned 0
        long retryIntervalInMilliseconds = -1;

        if (this.responseHeaders != null) {
            String header = this.responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);

            if (StringUtils.isNotEmpty(header)) {
                try {
                    retryIntervalInMilliseconds = Math.min(Long.parseLong(header), MAX_RETRY_AFTER_IN_MS);
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

    void setRequestUri(Uri requestUri) {
        this.requestUri = requestUri;
    }

    Uri getRequestUri() {
        return this.requestUri;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    String toString(boolean includeDiagnostics) {
        try {
            ObjectNode exceptionMessageNode = mapper.createObjectNode();
            exceptionMessageNode.put("ClassName", getClass().getSimpleName());
            exceptionMessageNode.put(USER_AGENT_KEY, this.getUserAgent());
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

            if (StringUtils.isNotEmpty(this.faultInjectionRuleId)) {
                exceptionMessageNode.put("faultInjectionRuleId", this.faultInjectionRuleId);
            }

            if(includeDiagnostics && this.cosmosDiagnostics != null) {
                cosmosDiagnostics.fillCosmosDiagnostics(exceptionMessageNode, null);
            }

            return mapper.writeValueAsString(exceptionMessageNode);
        } catch (JsonProcessingException ex) {
            return String.format(
                "%s {%s=%s, error=%s, resourceAddress=%s, statusCode=%s, message=%s, causeInfo=%s, responseHeaders=%s, requestHeaders=%s, faultInjectionRuleId=[%s] }",
                getClass().getSimpleName(),
                USER_AGENT_KEY,
                this.getUserAgent(),
                cosmosError,
                resourceAddress,
                statusCode,
                getMessage(),
                causeInfo(),
                responseHeaders,
                filterSensitiveData(requestHeaders),
                this.faultInjectionRuleId);
        }
    }

    String innerErrorMessage() {
        String innerErrorMessage = super.getMessage();
        if (cosmosError != null) {
            innerErrorMessage = cosmosError.getMessage();
            if (innerErrorMessage == null) {
                innerErrorMessage = String.valueOf(cosmosError.get("Errors"));
            }
        }
        // if cosmosError is null as well, try to get the underlying error from the internal cause
        if (StringUtils.isEmpty(innerErrorMessage) && this.getCause() != null) {
            innerErrorMessage = this.getCause().getMessage();
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

    RntbdChannelStatistics getRntbdChannelStatistics() {
        return this.rntbdChannelStatistics;
    }

    void setRntbdChannelStatistics(RntbdChannelStatistics rntbdChannelStatistics) {
        this.rntbdChannelStatistics = rntbdChannelStatistics;
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

    private String getUserAgent() {
        String userAgent = Utils.getUserAgent();
        if (this.requestHeaders != null) {
            userAgent = this.requestHeaders.getOrDefault(HttpConstants.HttpHeaders.USER_AGENT, userAgent);
        }

        return userAgent;
    }

    void setFaultInjectionRuleId(String faultInjectionRUleId) {
        this.faultInjectionRuleId = faultInjectionRUleId;
    }

    String getFaultInjectionRuleId() {
        return this.faultInjectionRuleId;
    }

    void setFaultInjectionEvaluationResults(List<String> faultInjectionEvaluationResults) {
        this.faultInjectionEvaluationResults = faultInjectionEvaluationResults;
    }

    List<String> getFaultInjectionEvaluationResults() {
        return this.faultInjectionEvaluationResults;
    }

    Map<String, Set<String>> getReplicaStatusList() {
        return this.replicaStatusList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosExceptionHelper.setCosmosExceptionAccessor(
                new ImplementationBridgeHelpers.CosmosExceptionHelper.CosmosExceptionAccessor() {
                    @Override
                    public CosmosException createCosmosException(int statusCode, Exception innerException) {
                        return new CosmosException(statusCode, innerException);
                    }

                    @Override
                    public Map<String, Set<String>> getReplicaStatusList(CosmosException cosmosException) {
                        return cosmosException.getReplicaStatusList();
                    }

                    @Override
                    public CosmosException setRntbdChannelStatistics(
                        CosmosException cosmosException,
                        RntbdChannelStatistics rntbdChannelStatistics) {

                        cosmosException.setRntbdChannelStatistics(rntbdChannelStatistics);
                        return cosmosException;
                    }

                    @Override
                    public RntbdChannelStatistics getRntbdChannelStatistics(CosmosException cosmosException) {
                        return cosmosException.getRntbdChannelStatistics();
                    }

                    @Override
                    public void setFaultInjectionRuleId(CosmosException cosmosException, String faultInjectionRuleId) {
                        cosmosException.setFaultInjectionRuleId(faultInjectionRuleId);
                    }

                    @Override
                    public String getFaultInjectionRuleId(CosmosException cosmosException) {
                        return cosmosException.getFaultInjectionRuleId();
                    }

                    @Override
                    public void setFaultInjectionEvaluationResults(CosmosException cosmosException, List<String> faultInjectionRuleEvaluationResults) {
                        cosmosException.setFaultInjectionEvaluationResults(faultInjectionRuleEvaluationResults);
                    }

                    @Override
                    public List<String> getFaultInjectionEvaluationResults(CosmosException cosmosException) {
                        return cosmosException.getFaultInjectionEvaluationResults();
                    }

                    @Override
                    public void setRequestUri(CosmosException cosmosException, Uri requestUri) {
                        cosmosException.setRequestUri(requestUri);
                    }

                    @Override
                    public Uri getRequestUri(CosmosException cosmosException) {
                        return cosmosException.getRequestUri();
                    }
                });
    }

    static { initialize(); }
}
