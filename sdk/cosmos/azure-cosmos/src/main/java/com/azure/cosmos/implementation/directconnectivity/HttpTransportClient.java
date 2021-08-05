// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.LockedException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.PreconditionFailedException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Integers;
import com.azure.cosmos.implementation.Lists;
import com.azure.cosmos.implementation.Longs;
import com.azure.cosmos.implementation.MutableVolatile;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RuntimeConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.Utils.trimBeginningAndEndingSlashes;
/*
 * The following code only support Document Write without any error handling support.
 */
public class HttpTransportClient extends TransportClient {
    private final Logger logger = LoggerFactory.getLogger(HttpTransportClient.class);
    private final HttpClient httpClient;
    private final Map<String, String> defaultHeaders;
    private final Configs configs;

    HttpClient createHttpClient(ConnectionPolicy connectionPolicy) {
        // TODO: use one instance of SSL context everywhere
        HttpClientConfig httpClientConfig = new HttpClientConfig(this.configs);
        httpClientConfig.withRequestTimeout(connectionPolicy.getRequestTimeout());
        httpClientConfig.withPoolSize(configs.getDirectHttpsMaxConnectionLimit());

        return HttpClient.createFixed(httpClientConfig);
    }

    public HttpTransportClient(Configs configs, ConnectionPolicy connectionPolicy, UserAgentContainer userAgent) {
        this.configs = configs;
        this.httpClient = createHttpClient(connectionPolicy);

        this.defaultHeaders = new HashMap<>();

        // Set requested API version header for version enforcement.
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL, HttpConstants.HeaderValues.NO_CACHE);

        if (userAgent == null) {
            userAgent = new UserAgentContainer();
        }

        this.defaultHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgent.getUserAgent());
        this.defaultHeaders.put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);
    }

    @Override
    public void close() {
        httpClient.shutdown();
    }

    public Mono<StoreResponse> invokeStoreAsync(
        Uri physicalAddressUri,
        RxDocumentServiceRequest request) {

        try {
            URI physicalAddress = physicalAddressUri.getURI();

            ResourceOperation resourceOperation = new ResourceOperation(request.getOperationType(), request.getResourceType());
            // uuid correlation manager
            String activityId = request.getActivityId().toString();

            if (resourceOperation.operationType == OperationType.Recreate) {
                Map<String, String> errorResponseHeaders = new HashMap<>();
                errorResponseHeaders.put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

                logger.error("Received Recreate request on Http client");
                throw new InternalServerErrorException(RMResources.InternalServerError, null, errorResponseHeaders, null);
            }

            HttpRequest httpRequest = prepareHttpMessage(activityId, physicalAddressUri, resourceOperation, request);

            MutableVolatile<Instant> sendTimeUtc = new MutableVolatile<>();

            Duration responseTimeout = Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
            if (OperationType.QueryPlan.equals(request.getOperationType())) {
                responseTimeout = Duration.ofSeconds(Configs.getQueryPlanResponseTimeoutInSeconds());
            } else if (request.isAddressRefresh()) {
                responseTimeout = Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds());
            }

            Mono<HttpResponse> httpResponseMono = this.httpClient
                    .send(httpRequest, responseTimeout)
                    .doOnSubscribe(subscription -> {
                        sendTimeUtc.v = Instant.now();
                        this.beforeRequest(
                                activityId,
                                httpRequest.uri(),
                                request.getResourceType(),
                                httpRequest.headers());
                    })
                    .onErrorResume(t -> {
                        Exception exception = Utils.as(t, Exception.class);
                        if (exception == null) {
                            logger.error("critical failure", t);
                            t.printStackTrace();
                            assert false : "critical failure";
                            return Mono.error(t);
                        }

                        //Trace.CorrelationManager.ActivityId = activityId;
                        if (WebExceptionUtility.isWebExceptionRetriable(exception)) {
                            logger.debug("Received retriable exception {} " +
                                            "sending the request to {}, will re-resolve the address " +
                                            "send time UTC: {}",
                                    exception,
                                    physicalAddress,
                                    sendTimeUtc);

                            GoneException goneException = new GoneException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            RMResources.Gone),
                                    exception,
                                    null,
                                    physicalAddress);

                            return Mono.error(goneException);
                        } else if (request.isReadOnlyRequest()) {
                            logger.trace("Received exception {} on readonly request" +
                                            "sending the request to {}, will reresolve the address " +
                                            "send time UTC: {}",
                                    exception,
                                    physicalAddress,
                                    sendTimeUtc);

                            GoneException goneException = new GoneException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            RMResources.Gone),
                                    exception,
                                    null,
                                    physicalAddress);

                            return Mono.error(goneException);
                        } else {
                            // We can't throw a GoneException here because it will cause retry and we don't
                            // know if the request failed before or after the message got sent to the server.
                            // So in order to avoid duplicating the request we will not retry.
                            // TODO: a possible solution for this is to add the ability to send a request to the server
                            // to check if the previous request was received or not and act accordingly.
                            ServiceUnavailableException serviceUnavailableException = new ServiceUnavailableException(
                                exception.getMessage(),
                                exception,
                                null,
                                physicalAddress.toString());
                            serviceUnavailableException.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");
                            serviceUnavailableException.getResponseHeaders().put(HttpConstants.HttpHeaders.WRITE_REQUEST_TRIGGER_ADDRESS_REFRESH, "1");
                            return Mono.error(serviceUnavailableException);
                        }})
                    .doOnSuccess(httpClientResponse -> {
                        Instant receivedTimeUtc = Instant.now();
                        double durationInMilliSeconds = (receivedTimeUtc.toEpochMilli() - sendTimeUtc.v.toEpochMilli());
                        this.afterRequest(
                                activityId,
                                httpClientResponse.statusCode(),
                                durationInMilliSeconds,
                                httpClientResponse.headers());
                    })
                    .doOnError(e -> {
                        Instant receivedTimeUtc = Instant.now();
                        double durationInMilliSeconds = (receivedTimeUtc.toEpochMilli() - sendTimeUtc.v.toEpochMilli());
                        this.afterRequest(
                                activityId,
                                0,
                                durationInMilliSeconds,
                                null);
                    });

            return httpResponseMono.flatMap(rsp -> processHttpResponse(request.requestContext.resourcePhysicalAddress,
                    httpRequest, activityId, rsp, physicalAddress));

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private void beforeRequest(String activityId, URI uri, ResourceType resourceType, HttpHeaders requestHeaders) {
        // TODO: perf counters
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
    }

    private void afterRequest(String activityId,
                              int statusCode,
                              double durationInMilliSeconds,
                              HttpHeaders responseHeaders) {
        // TODO: perf counters
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
    }

    private static void addHeader(HttpHeaders requestHeaders, String headerName, RxDocumentServiceRequest request) {
        String headerValue = request.getHeaders().get(headerName);
        if (!Strings.isNullOrEmpty(headerValue)) {
            requestHeaders.set(headerName, headerValue);
        }
    }

    private static void addHeader(HttpHeaders requestHeaders, String headerName, String headerValue) {
        if (!Strings.isNullOrEmpty(headerValue)) {
            requestHeaders.set(headerName, headerValue);
        }
    }

    private String getMatch(RxDocumentServiceRequest request, ResourceOperation resourceOperation) {
        switch (resourceOperation.operationType) {
            case Delete:
            case ExecuteJavaScript:
            case Replace:
            case Patch:
            case Upsert:
                return request.getHeaders().get(HttpConstants.HttpHeaders.IF_MATCH);

            case Read:
            case ReadFeed:
                return request.getHeaders().get(HttpConstants.HttpHeaders.IF_NONE_MATCH);

            default:
                return null;
        }
    }

    private HttpRequest prepareHttpMessage(
        String activityId,
        Uri physicalAddress,
        ResourceOperation resourceOperation,
        RxDocumentServiceRequest request) throws Exception {

        HttpRequest httpRequestMessage;
        String requestUri;
        HttpMethod method;

        // The StreamContent created below will own and dispose its underlying stream, but we may need to reuse the stream on the
        // RxDocumentServiceRequest for future requests. Hence we need to clone without incurring copy cost, so that when
        // HttpRequestMessage -> StreamContent -> MemoryStream all get disposed, the original stream will be left open.
        switch (resourceOperation.operationType) {
            case Create:
            case Batch:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.POST;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                break;

            case ExecuteJavaScript:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.POST;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                break;

            case Delete:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.DELETE;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                break;

            case Read:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.GET;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                break;

            case ReadFeed:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.GET;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                break;

            case Replace:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.PUT;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                break;

            case Patch:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.PATCH;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                break;

            case Query:
            case SqlQuery:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.POST;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                HttpTransportClient.addHeader(httpRequestMessage.headers(), HttpConstants.HttpHeaders.CONTENT_TYPE, request);
                break;

            case Upsert:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.POST;
                assert request.getContentAsByteArrayFlux() != null;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                httpRequestMessage.withBody(request.getContentAsByteArrayFlux());
                break;

            case Head:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.HEAD;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                break;

            case HeadFeed:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress.getURIAsString(), request);
                method = HttpMethod.HEAD;
                httpRequestMessage = new HttpRequest(method, requestUri, physicalAddress.getURI().getPort());
                break;

            default:
                assert false : "Unsupported operation type";
                throw new IllegalStateException();
        }

        Map<String, String> documentServiceRequestHeaders = request.getHeaders();
        HttpHeaders httpRequestHeaders = httpRequestMessage.headers();

        // add default headers
        for(Map.Entry<String, String> entry: defaultHeaders.entrySet()) {
            HttpTransportClient.addHeader(httpRequestHeaders, entry.getKey(), entry.getValue());
        }

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.VERSION, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.USER_AGENT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PAGE_SIZE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PRE_TRIGGER_INCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PRE_TRIGGER_EXCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POST_TRIGGER_INCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POST_TRIGGER_EXCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.AUTHORIZATION, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.INDEXING_DIRECTIVE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.MIGRATE_COLLECTION_DIRECTIVE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.SESSION_TOKEN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PREFER, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.RESOURCE_TOKEN_EXPIRY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.EMIT_VERBOSE_TRACES_IN_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_CHARGE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_THROTTLE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_LOW_PRECISION_ORDER_BY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_LOGGING, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_READ_ONLY_SCRIPT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONTENT_SERIALIZATION_FORMAT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONTINUATION, request.getContinuation());
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ACTIVITY_ID, activityId);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.START_EPK, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.END_EPK, request);

        String dateHeader = HttpUtils.getDateHeader(documentServiceRequestHeaders);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.X_DATE, dateHeader);
        HttpTransportClient.addHeader(httpRequestHeaders, "Match", this.getMatch(request, resourceOperation));
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IF_MODIFIED_SINCE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.A_IM, request);
        if (!request.getIsNameBased()) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.RESOURCE_ID, request.getResourceId());
        }

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.ENTITY_ID, request.entityId);

        String fanoutRequestHeader = request.getHeaders().get(WFConstants.BackendHeaders.IS_FANOUT_REQUEST);
        HttpTransportClient.addHeader(httpRequestMessage.headers(), WFConstants.BackendHeaders.IS_FANOUT_REQUEST, fanoutRequestHeader);

        if (request.getResourceType() == ResourceType.DocumentCollection) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_PARTITION_INDEX, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.COLLECTION_PARTITION_INDEX));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_SERVICE_INDEX, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.COLLECTION_SERVICE_INDEX));
        }

        if (documentServiceRequestHeaders.get(WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE) != null) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PRIMARY_MASTER_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.PRIMARY_MASTER_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SECONDARY_MASTER_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.SECONDARY_MASTER_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PRIMARY_READONLY_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.PRIMARY_READONLY_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SECONDARY_READONLY_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.SECONDARY_READONLY_KEY));
        }

        if (documentServiceRequestHeaders.get(HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE) != null) {
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE, documentServiceRequestHeaders.get(HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE));
        }

        //Query
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.QUERY, request);

        // Upsert
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_UPSERT, request);

        // SupportSpatialLegacyCoordinates
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.SUPPORT_SPATIAL_LEGACY_COORDINATES, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PARTITION_COUNT, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_RID, request);

        // Filter by schema
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.FILTER_BY_SCHEMA_RESOURCE_ID, request);

        // UsePolygonsSmallerThanAHemisphere
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.USE_POLYGONS_SMALLER_THAN_AHEMISPHERE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.GATEWAY_SIGNATURE, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.FORCE_QUERY_SCAN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.REMOTE_STORAGE_TYPE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SHARE_THROUGHPUT, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_PARTITION_STATISTICS, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_COLLECTION_THROUGHPUT_INFO, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.REMAINING_TIME_IN_MS_ON_CLIENT_REQUEST, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CLIENT_RETRY_ATTEMPT_COUNT, request);

        // target lsn for head requests.
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.TARGET_LSN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.TARGET_GLOBAL_COMMITTED_LSN, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.FEDERATION_ID_FOR_AUTH, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.FANOUT_OPERATION_STATE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.ALLOW_TENTATIVE_WRITES, request);

        HttpTransportClient.addHeader(httpRequestHeaders, CustomHeaders.HttpHeaders.EXCLUDE_SYSTEM_PROPERTIES, request);

        if (resourceOperation.operationType == OperationType.Batch) {
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_BATCH_REQUEST, request);
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.SHOULD_BATCH_CONTINUE_ON_ERROR, request);
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_BATCH_ORDERED, request);
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_BATCH_ATOMIC, request);
        }

        return httpRequestMessage;
    }

    static String getResourceFeedUri(ResourceType resourceType, String physicalAddress, RxDocumentServiceRequest request) throws Exception {
        switch (resourceType) {
            case Attachment:
                return getAttachmentFeedUri(physicalAddress, request);
            case DocumentCollection:
                return getCollectionFeedUri(physicalAddress, request);
            case Conflict:
                return getConflictFeedUri(physicalAddress, request);
            case Database:
                return getDatabaseFeedUri(physicalAddress);
            case Document:
                return getDocumentFeedUri(physicalAddress, request);
            case Permission:
                return getPermissionFeedUri(physicalAddress, request);
            case StoredProcedure:
                return getStoredProcedureFeedUri(physicalAddress, request);
            case Trigger:
                return getTriggerFeedUri(physicalAddress, request);
            case User:
                return getUserFeedUri(physicalAddress, request);

            case UserDefinedFunction:
                return getUserDefinedFunctionFeedUri(physicalAddress, request);
            case Schema:
                return getSchemaFeedUri(physicalAddress, request);
            case Offer:
                return getOfferFeedUri(physicalAddress, request);

//          Other types: Replica, Module, ModuleCommand, Record, UserDefinedType not applicable to SDK.

            default:
                assert false : "Unexpected resource type: " + resourceType;
                throw new NotFoundException();
        }
    }

    private static String getResourceEntryUri(ResourceType resourceType, String physicalAddress, RxDocumentServiceRequest request) throws Exception {
        switch (resourceType) {
            case Attachment:
                return getAttachmentEntryUri(physicalAddress, request);
            case DocumentCollection:
                return getCollectionEntryUri(physicalAddress, request);
            case Conflict:
                return getConflictEntryUri(physicalAddress, request);
            case Database:
                return getDatabaseEntryUri(physicalAddress, request);
            case Document:
                return getDocumentEntryUri(physicalAddress, request);
            case Permission:
                return getPermissionEntryUri(physicalAddress, request);
            case StoredProcedure:
                return getStoredProcedureEntryUri(physicalAddress, request);
            case Trigger:
                return getTriggerEntryUri(physicalAddress, request);
            case User:
                return getUserEntryUri(physicalAddress, request);
            case UserDefinedFunction:
                return getUserDefinedFunctionEntryUri(physicalAddress, request);
            case Schema:
                return getSchemaEntryUri(physicalAddress, request);
            case Offer:
                return getOfferEntryUri(physicalAddress, request);

//          Other types: Replica, Module, ModuleCommand, Record, UserDefinedType not applicable to SDK.

            default:
                assert false: "Unexpected resource type: " + resourceType;
                throw new IllegalStateException();
        }
    }

    static String createURI(String baseAddress, String resourcePath) {
        if (baseAddress.charAt(baseAddress.length() - 1) == '/') {
            return baseAddress + HttpUtils.urlEncode(trimBeginningAndEndingSlashes(resourcePath));
        } else {
            return baseAddress + '/' + HttpUtils.urlEncode(trimBeginningAndEndingSlashes(resourcePath));
        }
    }

    static String getRootFeedUri(String baseAddress) {
        return baseAddress;
    }

    private static String getDatabaseFeedUri(String baseAddress) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, StringUtils.EMPTY, true));
    }

    private static String getDatabaseEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, request, false));
    }

    private static String getCollectionFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, true));
    }

    private static String getStoredProcedureFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, true));
    }

    private static String getTriggerFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, true));
    }

    private static String getUserDefinedFunctionFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, true));
    }

    private static String getCollectionEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, false));
    }

    private static String getStoredProcedureEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, false));
    }

    private static String getTriggerEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, false));
    }

    private static String getUserDefinedFunctionEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, false));
    }

    private static String getDocumentFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, true));
    }

    private static String getDocumentEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, false));
    }

    private static String getConflictFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, true));
    }

    private static String getConflictEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, false));
    }

    private static String getAttachmentFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, true));
    }

    private static String getAttachmentEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, false));
    }

    private static String getUserFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, true));
    }

    private static String getUserEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, false));
    }

    private static String getPermissionFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, true));
    }

    private static String getPermissionEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, false));
    }

    private static String getOfferFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Offer, request, true));
    }

    private static String getSchemaFeedUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, true));
    }

    private static String getSchemaEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, false));
    }

    private static String getOfferEntryUri(String baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Offer, request, false));
    }

    static String getHeader(String[] names, String[] values, String name) {
        for (int idx = 0; idx < names.length; idx++) {
            if (Strings.areEqual(names[idx], name)) {
                return values[idx];
            }
        }

        return null;
    }

    private Mono<StoreResponse> processHttpResponse(String resourceAddress, HttpRequest httpRequest, String activityId, HttpResponse response, URI physicalAddress) {
        if (response == null) {
            InternalServerErrorException exception =
                    new InternalServerErrorException(
                            String.format(
                                    RMResources.ExceptionMessage,
                                    RMResources.InvalidBackendResponse),
                            null,
                            physicalAddress);
            exception.getResponseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                    activityId);
            exception.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

            return Mono.error(exception);
        }

        // If the status code is < 300 or 304 NotModified (we treat not modified as success) then it means that it's a success code and shouldn't throw.
        if (response.statusCode() < HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY ||
                response.statusCode() == HttpConstants.StatusCodes.NOT_MODIFIED) {
            return ResponseUtils.toStoreResponse(response, httpRequest);
        }
        else {
            return this.createErrorResponseFromHttpResponse(resourceAddress, activityId, httpRequest, response);
        }
    }

    private Mono<StoreResponse> createErrorResponseFromHttpResponse(String resourceAddress, String activityId,
                                                                      HttpRequest request,
                                                                      HttpResponse response) {
        int statusCode = response.statusCode();
        Mono<String> errorMessageObs = ErrorUtils.getErrorResponseAsync(response, request);

        return errorMessageObs.flatMap(
                errorMessage -> {
                    long responseLSN = -1;

                    List<String> lsnValues = null;
                    String[] headerValues = response.headers().values(WFConstants.BackendHeaders.LSN);
                    if (headerValues != null) {
                        lsnValues = com.azure.cosmos.implementation.guava25.collect.Lists.newArrayList(headerValues);
                    }

                    if (lsnValues != null) {
                        String temp = lsnValues.isEmpty() ? null : lsnValues.get(0);
                        responseLSN = Longs.tryParse(temp, responseLSN);
                    }

                    String responsePartitionKeyRangeId = null;
                    List<String> partitionKeyRangeIdValues = null;
                    headerValues = response.headers().values(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);
                    if (headerValues != null) {
                        partitionKeyRangeIdValues
                            = com.azure.cosmos.implementation.guava25.collect.Lists.newArrayList(headerValues);
                    }
                    if (partitionKeyRangeIdValues != null) {
                        responsePartitionKeyRangeId = Lists.firstOrDefault(partitionKeyRangeIdValues, null);
                    }

                    CosmosException exception;

                    switch (statusCode) {
                        case HttpConstants.StatusCodes.UNAUTHORIZED:
                            exception = new UnauthorizedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Unauthorized : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        case HttpConstants.StatusCodes.FORBIDDEN:
                            exception = new ForbiddenException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Forbidden : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        case HttpConstants.StatusCodes.NOTFOUND:
                            // HTTP.SYS returns NotFound (404) if the URI
                            // is not registered. This is really an indication that
                            // the replica which registered the URI is not
                            // available at the server. We detect this case by
                            // the presence of Content-Type header in the response
                            // and map it to HTTP Gone (410), which is the more
                            // appropriate response for this case.
                            if (response.body() != null && response.headers() != null && response.headers().value(HttpConstants.HttpHeaders.CONTENT_TYPE) != null &&
                                    !Strings.isNullOrEmpty(response.headers().value(HttpConstants.HttpHeaders.CONTENT_TYPE)) &&
                                    Strings.containsIgnoreCase(response.headers().value(HttpConstants.HttpHeaders.CONTENT_TYPE), RuntimeConstants.MediaTypes.TEXT_HTML)) {
                                // Have the request URL in the exception message for debugging purposes.
                                exception = new GoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                RMResources.Gone),
                                        request.uri().toString());
                                exception.getResponseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                                        activityId);

                                break;
                            } else {
                                exception = new NotFoundException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.NotFound : errorMessage),
                                        response.headers(),
                                        request.uri());
                                break;
                            }

                        case HttpConstants.StatusCodes.BADREQUEST:
                            exception = new BadRequestException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.BadRequest : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        case HttpConstants.StatusCodes.METHOD_NOT_ALLOWED:
                            exception = new MethodNotAllowedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.MethodNotAllowed : errorMessage),
                                    null,
                                    response.headers(),
                                    request.uri().toString());
                            break;

                        case HttpConstants.StatusCodes.GONE: {

                            // TODO: update perf counter
                            // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
                            ErrorUtils.logGoneException(request.uri(), activityId);

                            Integer nSubStatus = 0;
                            String valueSubStatus = response.headers().value(WFConstants.BackendHeaders.SUB_STATUS);
                            if (!Strings.isNullOrEmpty(valueSubStatus)) {
                                if ((nSubStatus = Integers.tryParse(valueSubStatus)) == null) {
                                    exception = new InternalServerErrorException(
                                            String.format(
                                                    RMResources.ExceptionMessage,
                                                    RMResources.InvalidBackendResponse),
                                            response.headers(),
                                            request.uri());
                                    break;
                                }
                            }

                            if (nSubStatus == HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE) {
                                exception = new InvalidPartitionException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.headers(),
                                        request.uri().toString());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE) {
                                exception = new PartitionKeyRangeGoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.headers(),
                                        request.uri().toString());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_SPLIT) {
                                exception = new PartitionKeyRangeIsSplittingException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.headers(),
                                        request.uri().toString());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION) {
                                exception = new PartitionIsMigratingException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.headers(),
                                        request.uri().toString());
                                break;
                            } else {
                                // Have the request URL in the exception message for debugging purposes.
                                GoneException goneExceptionFromService = new GoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                RMResources.Gone),
                                        response.headers(),
                                        request.uri());
                                goneExceptionFromService.setIsBasedOn410ResponseFromService();

                                goneExceptionFromService.getResponseHeaders().put(
                                    HttpConstants.HttpHeaders.ACTIVITY_ID,
                                    activityId);

                                exception = goneExceptionFromService;
                                break;
                            }
                        }

                        case HttpConstants.StatusCodes.CONFLICT:
                            exception = new ConflictException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.EntityAlreadyExists : errorMessage),
                                    response.headers(),
                                    request.uri().toString());
                            break;

                        case HttpConstants.StatusCodes.PRECONDITION_FAILED:
                            exception = new PreconditionFailedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.PreconditionFailed : errorMessage),
                                    response.headers(),
                                    request.uri().toString());
                            break;

                        case HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE:
                            exception = new RequestEntityTooLargeException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            String.format(
                                                    RMResources.RequestEntityTooLarge,
                                                    HttpConstants.HttpHeaders.PAGE_SIZE)),
                                    response.headers(),
                                    request.uri().toString());
                            break;

                        case HttpConstants.StatusCodes.LOCKED:
                            exception = new LockedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Locked : errorMessage),
                                    response.headers(),
                                    request.uri().toString());
                            break;

                        case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:
                            exception = new ServiceUnavailableException(errorMessage, response.headers(), request.uri());
                            break;

                        case HttpConstants.StatusCodes.REQUEST_TIMEOUT:
                            exception = new RequestTimeoutException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.RequestTimeout : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        case HttpConstants.StatusCodes.RETRY_WITH:
                            exception = new RetryWithException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.RetryWith : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        case HttpConstants.StatusCodes.TOO_MANY_REQUESTS:
                            exception =
                                    new RequestRateTooLargeException(
                                            String.format(
                                                    RMResources.ExceptionMessage,
                                                    Strings.isNullOrEmpty(errorMessage) ? RMResources.TooManyRequests : errorMessage),
                                            response.headers(),
                                            request.uri());

                            List<String> values = null;
                            headerValues = response.headers().values(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);
                            if (headerValues != null) {
                                values
                                    = com.azure.cosmos.implementation.guava25.collect.Lists.newArrayList(headerValues);
                            }
                            if (values == null || values.isEmpty()) {
                                logger.warn("RequestRateTooLargeException being thrown without RetryAfter.");
                            } else {
                                exception.getResponseHeaders().put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, values.get(0));
                            }

                            break;

                        case HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR:
                            exception = new InternalServerErrorException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.InternalServerError : errorMessage),
                                    response.headers(),
                                    request.uri());
                            break;

                        default:
                            logger.error("Unrecognized status code {} returned by backend. ActivityId {}", statusCode, activityId);
                            ErrorUtils.logException(request.uri(), activityId);
                            exception = new InternalServerErrorException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            RMResources.InvalidBackendResponse),
                                    response.headers(),
                                    request.uri());
                            break;
                    }

                    BridgeInternal.setLSN(exception, responseLSN);
                    BridgeInternal.setPartitionKeyRangeId(exception, responsePartitionKeyRangeId);
                    BridgeInternal.setResourceAddress(exception, resourceAddress);
                    BridgeInternal.setRequestHeaders(exception, HttpUtils.asMap(request.headers()));

                    return Mono.error(exception);
                }
        );
    }
}
