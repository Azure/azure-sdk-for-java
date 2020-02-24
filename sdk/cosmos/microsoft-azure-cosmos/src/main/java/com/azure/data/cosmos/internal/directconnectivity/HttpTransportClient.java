// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConflictException;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.ForbiddenException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.InternalServerErrorException;
import com.azure.data.cosmos.InvalidPartitionException;
import com.azure.data.cosmos.LockedException;
import com.azure.data.cosmos.MethodNotAllowedException;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.PartitionIsMigratingException;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.PreconditionFailedException;
import com.azure.data.cosmos.RequestEntityTooLargeException;
import com.azure.data.cosmos.RequestRateTooLargeException;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.RetryWithException;
import com.azure.data.cosmos.ServiceUnavailableException;
import com.azure.data.cosmos.UnauthorizedException;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.Integers;
import com.azure.data.cosmos.internal.Lists;
import com.azure.data.cosmos.internal.Longs;
import com.azure.data.cosmos.internal.MutableVolatile;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RuntimeConstants;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpClientConfig;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.data.cosmos.internal.Utils.trimBeginningAndEndingSlashes;
/*
 * The following code only support Document Write without any error handling support.
 */
public class HttpTransportClient extends TransportClient {
    private final Logger logger = LoggerFactory.getLogger(HttpTransportClient.class);
    private final HttpClient httpClient;
    private final Map<String, String> defaultHeaders;
    private final Configs configs;

    HttpClient createHttpClient(int requestTimeout) {
        // TODO: use one instance of SSL context everywhere
        HttpClientConfig httpClientConfig = new HttpClientConfig(this.configs);
        httpClientConfig.withRequestTimeoutInMillis(requestTimeout * 1000);
        httpClientConfig.withPoolSize(configs.getDirectHttpsMaxConnectionLimit());

        return HttpClient.createFixed(httpClientConfig);
    }

    public HttpTransportClient(Configs configs, int requestTimeout, UserAgentContainer userAgent) {
        this.configs = configs;
        this.httpClient = createHttpClient(requestTimeout);

        this.defaultHeaders = new HashMap<>();

        // Set requested API version header for version enforcement.
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL, HttpConstants.HeaderValues.NoCache);

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
        URI physicalAddress,
        RxDocumentServiceRequest request) {

        try {
            ResourceOperation resourceOperation = new ResourceOperation(request.getOperationType(), request.getResourceType());
            // uuid correlation manager
            UUID activityId = UUID.fromString(request.getActivityId());

            if (resourceOperation.operationType == OperationType.Recreate) {
                Map<String, String> errorResponseHeaders = new HashMap<>();
                errorResponseHeaders.put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

                logger.error("Received Recreate request on Http client");
                throw new InternalServerErrorException(RMResources.InternalServerError, null, errorResponseHeaders, null);
            }

            HttpRequest httpRequest = prepareHttpMessage(activityId, physicalAddress, resourceOperation, request);

            MutableVolatile<Instant> sendTimeUtc = new MutableVolatile<>();

            Mono<HttpResponse> httpResponseMono = this.httpClient
                    .send(httpRequest)
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
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            RMResources.ServiceUnavailable),
                                    exception,
                                    null,
                                    physicalAddress.toString());
                            serviceUnavailableException.responseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");
                            serviceUnavailableException.responseHeaders().put(HttpConstants.HttpHeaders.WRITE_REQUEST_TRIGGER_ADDRESS_REFRESH, "1");
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

            return httpResponseMono.flatMap(rsp -> processHttpResponse(request.getResourceAddress(),
                    httpRequest, activityId.toString(), rsp, physicalAddress));

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private void beforeRequest(UUID activityId, URI uri, ResourceType resourceType, HttpHeaders requestHeaders) {
        // TODO: perf counters
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
    }

    private void afterRequest(UUID activityId,
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
            case Update:
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
        UUID activityId,
        URI physicalAddress,
        ResourceOperation resourceOperation,
        RxDocumentServiceRequest request) throws Exception {

        HttpRequest httpRequestMessage;
        URI requestUri;
        HttpMethod method;

        // The StreamContent created below will own and dispose its underlying stream, but we may need to reuse the stream on the
        // RxDocumentServiceRequest for future requests. Hence we need to clone without incurring copy cost, so that when
        // HttpRequestMessage -> StreamContent -> MemoryStream all get disposed, the original stream will be left open.
        switch (resourceOperation.operationType) {
            case Create:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                break;

            case ExecuteJavaScript:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                break;

            case Delete:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.DELETE;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                break;

            case Read:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.GET;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                break;

            case ReadFeed:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.GET;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                break;

            case Replace:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.PUT;
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                break;

            case Update:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = new HttpMethod("PATCH");
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                break;

            case Query:
            case SqlQuery:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                HttpTransportClient.addHeader(httpRequestMessage.headers(), HttpConstants.HttpHeaders.CONTENT_TYPE, request);
                break;

            case Upsert:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                httpRequestMessage.withBody(request.getContent());
                break;

            case Head:
                requestUri = getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.HEAD;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
                break;

            case HeadFeed:
                requestUri = getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.HEAD;
                httpRequestMessage = new HttpRequest(method, requestUri.toString(), physicalAddress.getPort());
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
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ACTIVITY_ID, activityId.toString());
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, request);

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

        return httpRequestMessage;
    }

    static URI getResourceFeedUri(ResourceType resourceType, URI physicalAddress, RxDocumentServiceRequest request) throws Exception {
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

    private static URI getResourceEntryUri(ResourceType resourceType, URI physicalAddress, RxDocumentServiceRequest request) throws Exception {
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

    private static URI createURI(URI baseAddress, String resourcePath) {
        return baseAddress.resolve(HttpUtils.urlEncode(trimBeginningAndEndingSlashes(resourcePath)));
    }

    static URI getRootFeedUri(URI baseAddress) {
        return baseAddress;
    }

    private static URI getDatabaseFeedUri(URI baseAddress) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, StringUtils.EMPTY, true));
    }

    private static URI getDatabaseEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, request, false));
    }

    private static URI getCollectionFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, true));
    }

    private static URI getStoredProcedureFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, true));
    }

    private static URI getTriggerFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, true));
    }

    private static URI getUserDefinedFunctionFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, true));
    }

    private static URI getCollectionEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, false));
    }

    private static URI getStoredProcedureEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, false));
    }

    private static URI getTriggerEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, false));
    }

    private static URI getUserDefinedFunctionEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, false));
    }

    private static URI getDocumentFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, true));
    }

    private static URI getDocumentEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, false));
    }

    private static URI getConflictFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, true));
    }

    private static URI getConflictEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, false));
    }

    private static URI getAttachmentFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, true));
    }

    private static URI getAttachmentEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, false));
    }

    private static URI getUserFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, true));
    }

    private static URI getUserEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, false));
    }

    private static URI getPermissionFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, true));
    }

    private static URI getPermissionEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, false));
    }

    private static URI getOfferFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Offer, request, true));
    }

    private static URI getSchemaFeedUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, true));
    }

    private static URI getSchemaEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, false));
    }

    private static URI getOfferEntryUri(URI baseAddress, RxDocumentServiceRequest request) {
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
            exception.responseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                    activityId);
            exception.responseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

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
                        lsnValues = com.google.common.collect.Lists.newArrayList(headerValues);
                    }

                    if (lsnValues != null) {
                        String temp = lsnValues.isEmpty() ? null : lsnValues.get(0);
                        responseLSN = Longs.tryParse(temp, responseLSN);
                    }

                    String responsePartitionKeyRangeId = null;
                    List<String> partitionKeyRangeIdValues = null;
                    headerValues = response.headers().values(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);
                    if (headerValues != null) {
                        partitionKeyRangeIdValues = com.google.common.collect.Lists.newArrayList(headerValues);
                    }
                    if (partitionKeyRangeIdValues != null) {
                        responsePartitionKeyRangeId = Lists.firstOrDefault(partitionKeyRangeIdValues, null);
                    }

                    CosmosClientException exception;

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
                                exception.responseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
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
                                exception = new GoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                RMResources.Gone),
                                        response.headers(),
                                        request.uri());

                                exception.responseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                                        activityId);
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
                            exception = new ServiceUnavailableException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.ServiceUnavailable : errorMessage),
                                    response.headers(),
                                    request.uri());
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
                                values = com.google.common.collect.Lists.newArrayList(headerValues);
                            }
                            if (values == null || values.isEmpty()) {
                                logger.warn("RequestRateTooLargeException being thrown without RetryAfter.");
                            } else {
                                exception.responseHeaders().put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, values.get(0));
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
