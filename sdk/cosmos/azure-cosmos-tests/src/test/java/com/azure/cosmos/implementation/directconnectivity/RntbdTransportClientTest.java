// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.LockedException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.PreconditionFailedException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.rntbd.AsyncRntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.OpenConnectionRntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdClientChannelHealthChecker;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContext;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextNegotiator;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdDurableEndpointMetrics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestEncoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestTimer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponseDecoder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUUID;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUtils;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import io.micrometer.core.instrument.Tag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public final class RntbdTransportClientTest {

    private static final int lsn = 5;
    private static final ByteBuf noContent = Unpooled.wrappedBuffer(new byte[0]);
    private static final String partitionKeyRangeId = "3";
    private static final Uri addressUri = new Uri("rntbd://host:10251/replica-path/");
    private static final Duration requestTimeout = Duration.ofSeconds(1000);
    private static final int sslHandshakeTimeoutInMillis = 5000;
    private static final boolean timeoutDetectionEnabled = true;
    private static final Duration timeoutDetectionTimeLimit = Duration.ofSeconds(60L);
    private static final int timeoutDetectionHighFrequencyThreshold = 3;
    private static final Duration timeoutDetectionHighFrequencyTimeLimit = Duration.ofSeconds(10L);
    private static final int timeoutDetectionOnWriteThreshold = 1;
    private static final Duration timeoutDetectionOnWriteTimeLimit = Duration.ofSeconds(6L);
    private static final double timeoutDetectionDisableCPUThreshold = 90.0;

    @DataProvider(name = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public Object[][] fromMockedNetworkFailureToExpectedDocumentClientException() {

        return new Object[][] {
        };
    }

    @DataProvider(name = "fromMockedRntbdResponseToExpectedDocumentClientException")
    public Object[][] fromMockedRntbdResponseToExpectedDocumentClientException() {

        return new Object[][] {
            {
                // 1 BadRequestException

                FailureValidator.builder()
                    .instanceOf(BadRequestException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    400,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(1L)
                    ),
                    noContent)
            },
            {
                // 2 UnauthorizedException

                FailureValidator.builder()
                    .instanceOf(UnauthorizedException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    401,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(2L)
                    ),
                    noContent)
            },
            {
                // 3 ForbiddenException

                FailureValidator.builder()
                    .instanceOf(ForbiddenException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    403,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(3L)
                    ),
                    noContent)
            },
            {
                // 4 NotFoundException

                FailureValidator.builder()
                    .instanceOf(NotFoundException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    404,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(4L)
                    ),
                    noContent)
            },
            {
                // 5 MethodNotAllowedException

                FailureValidator.builder()
                    .instanceOf(MethodNotAllowedException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    405,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(5L)
                    ),
                    noContent)
            },
            {
                // 6 RequestTimeoutException

                FailureValidator.builder()
                    .instanceOf(RequestTimeoutException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    408,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(6L)
                    ),
                    noContent)
            },
            {
                // 7 ConflictException

                FailureValidator.builder()
                    .instanceOf(ConflictException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    409,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(7L)
                    ),
                    noContent)
            },
            {
                // 8 InvalidPartitionException

                FailureValidator.builder()
                    .instanceOf(InvalidPartitionException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.NAME_CACHE_IS_STALE),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(8L)
                    ),
                    noContent)
            },
            {
                // 9 PartitionKeyRangeGoneException

                FailureValidator.builder()
                    .instanceOf(PartitionKeyRangeGoneException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.PARTITION_KEY_RANGE_GONE),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(9L)
                    ),
                    noContent)
            },
            {
                // 10 PartitionKeyRangeIsSplittingException

                FailureValidator.builder()
                    .instanceOf(PartitionKeyRangeIsSplittingException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_SPLIT_OR_MERGE),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(10L)
                    ),
                    noContent)
            },
            {
                // 11 PartitionIsMigratingException

                FailureValidator.builder()
                    .instanceOf(PartitionIsMigratingException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_PARTITION_MIGRATION),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(11L)
                    ),
                    noContent)
            },
            {
                // 12 GoneException

                FailureValidator.builder()
                    .instanceOf(GoneException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    410,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.SUB_STATUS, String.valueOf(SubStatusCodes.UNKNOWN),
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(12L)
                    ),
                    noContent)
            },
            {
                // 13 PreconditionFailedException

                FailureValidator.builder()
                    .instanceOf(PreconditionFailedException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    412,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(13L)
                    ),
                    noContent)
            },
            {
                // 14 RequestEntityTooLargeException

                FailureValidator.builder()
                    .instanceOf(RequestEntityTooLargeException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    413,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(14L)
                    ),
                    noContent)
            },
            {
                // 15 LockedException

                FailureValidator.builder()
                    .instanceOf(LockedException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    423,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(15L)
                    ),
                    noContent)
            },
            {
                // 16 RequestRateTooLargeException

                FailureValidator.builder()
                    .instanceOf(RequestRateTooLargeException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    429,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(16L)
                    ),
                    noContent)
            },
            {
                // 17 RetryWithException

                FailureValidator.builder()
                    .instanceOf(RetryWithException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    449,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(17L)
                    ),
                    noContent)
            },
            {
                // 18 InternalServerErrorException

                FailureValidator.builder()
                    .instanceOf(InternalServerErrorException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    500,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(18L)
                    ),
                    noContent)
            },
            {
                // 19 ServiceUnavailableException

                FailureValidator.builder()
                    .instanceOf(ServiceUnavailableException.class)
                    .lsn(lsn)
                    .partitionKeyRangeId(partitionKeyRangeId)
                    .resourceAddress(null),
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.DocumentCollection,
                    "/dbs/db/colls/col",
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
                    )),
                new RntbdResponse(
                    RntbdUUID.EMPTY,
                    503,
                    ImmutableMap.of(
                        HttpHeaders.LSN, Integer.toString(lsn),
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(19L)
                    ),
                    noContent)
            },
        };
    }

    /**
     * Verifies that a request for a non-existent resource produces a {@link }GoneException}
     */
    @Test(enabled = false, groups = "direct")
    public void verifyGoneResponseMapsToGoneException() throws Exception {

        ConnectionPolicy connectionPolicy = ConnectionPolicy.getDefaultPolicy();
        connectionPolicy.setTcpNetworkRequestTimeout(requestTimeout);
        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy).build();
        final SslContext sslContext = SslContextBuilder.forClient().build();

        try (final RntbdTransportClient transportClient = new RntbdTransportClient(options, sslContext, null, null, null)) {

            final BaseAuthorizationTokenProvider authorizationTokenProvider = new BaseAuthorizationTokenProvider(
                new AzureKeyCredential(RntbdTestConfiguration.AccountKey)
            );

            final Uri physicalAddress = new Uri("rntbd://"
                + RntbdTestConfiguration.RntbdAuthority
                + "/apps/DocDbApp/services/DocDbMaster0/partitions/780e44f4-38c8-11e6-8106-8cdcd42c33be/replicas/1p/"
            );

            final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            builder.put(HttpHeaders.X_DATE, Utils.nowAsRFC1123());

            final String token = authorizationTokenProvider.generateKeyAuthorizationSignature(RequestVerb.GET,
                Paths.DATABASE_ACCOUNT_PATH_SEGMENT,
                ResourceType.DatabaseAccount,
                builder.build()
            );

            builder.put(HttpHeaders.AUTHORIZATION, token);

            final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                ResourceType.DatabaseAccount,
                Paths.DATABASE_ACCOUNT_PATH_SEGMENT,
                builder.build()
            );

            final Mono<StoreResponse> responseMono = transportClient.invokeResourceOperationAsync(physicalAddress, request);

            responseMono.subscribe(response -> { }, error -> {
                final String format = "Expected %s, not %s";
                assertTrue(error instanceof GoneException, String.format(format, GoneException.class, error.getClass()));
                final Throwable cause = error.getCause();
                if (cause != null) {
                    // assumption: cosmos isn't listening on 10251
                    assertTrue(cause instanceof ConnectException, String.format(format, ConnectException.class, error.getClass()));
                }
            });

        } catch (final Exception error) {
            final String message = String.format("%s: %s", error.getClass(), error.getMessage());
            fail(message, error);
        }
    }

    /**
     * Validates the error handling behavior of {@link RntbdTransportClient} for network failures
     * <p>
     * These are the exceptions that cannot be derived from server responses. They are mapped from Netty channel
     * failures simulated by {@link FakeChannel}.
     *
     * @param builder   A feature validator builder to confirm that response is correctly mapped to an exception
     * @param request   An RNTBD request instance
     * @param exception An exception mapping
     */
    @Test(enabled = false, groups = { "unit" }, dataProvider = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public void verifyNetworkFailure(
        final FailureValidator.Builder builder,
        final RxDocumentServiceRequest request,
        final CosmosException exception
    ) {
        // TODO: DANOBLE: Implement RntbdTransportClientTest.verifyNetworkFailure
        //  Links:
        //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/378750
        throw new UnsupportedOperationException("TODO: DANOBLE: Implement this test");
    }

    /**
     * Validates the error handling behavior of the {@link RntbdTransportClient} for HTTP status codes >= 400
     *
     * @param builder   A feature validator builder to confirm that response is correctly mapped to an exception
     * @param request   An RNTBD request instance
     * @param response  The RNTBD response instance to be returned as a result of the request
     */
    @Test(enabled = false, groups = "unit", dataProvider = "fromMockedRntbdResponseToExpectedDocumentClientException")
    public void verifyRequestFailures(
        final FailureValidator.Builder builder,
        final RxDocumentServiceRequest request,
        final RntbdResponse response
    ) {
        final UserAgentContainer userAgent = new UserAgentContainer();
        ConnectionPolicy connectionPolicy = ConnectionPolicy.getDefaultPolicy();
        connectionPolicy.setTcpNetworkRequestTimeout(Duration.ofMillis(1000));

        try (final RntbdTransportClient client = getRntbdTransportClientUnderTest(userAgent, connectionPolicy, response)) {

            final Mono<StoreResponse> responseMono;

            try {
                responseMono = client.invokeResourceOperationAsync(addressUri, request);
            } catch (final Exception error) {
                throw new AssertionError(String.format("%s: %s", error.getClass(), error));
            }

            this.validateFailure(responseMono, builder.build());
        }
    }

    // TODO: add validations for other properties
    @Test(groups = "unit")
    public void transportClientDefaultOptionsTests() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        UserAgentContainer userAgentContainer = new UserAgentContainer();

        RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy)
                .userAgent(userAgentContainer)
                .build();

        assertEquals(options.sslHandshakeTimeoutInMillis(), sslHandshakeTimeoutInMillis);
        assertEquals(options.timeoutDetectionEnabled(), timeoutDetectionEnabled);
        assertEquals(options.timeoutDetectionTimeLimit(), timeoutDetectionTimeLimit);
        assertEquals(options.timeoutDetectionHighFrequencyThreshold(), timeoutDetectionHighFrequencyThreshold);
        assertEquals(options.timeoutDetectionHighFrequencyTimeLimit(), timeoutDetectionHighFrequencyTimeLimit);
        assertEquals(options.timeoutDetectionOnWriteThreshold(), timeoutDetectionOnWriteThreshold);
        assertEquals(options.timeoutDetectionOnWriteTimeLimit(), timeoutDetectionOnWriteTimeLimit);
        assertEquals(options.timeoutDetectionDisableCPUThreshold(), timeoutDetectionDisableCPUThreshold);
    }

    // TODO: add validations for other properties
    // TODO: The default options in RntbdTransportClient.Options.Builder is initialized in static block, reenable this test when figure out how to reload the class.
    @Test(enabled = false, groups = "unit")
    public void transportClientCustomizedOptionsTests() {
        try {
            System.setProperty("COSMOS.TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED", "false");
            System.setProperty(
                "azure.cosmos.directTcp.defaultOptions",
                "{\"sslHandshakeTimeoutMinDuration\":\"PT15S\"," +
                    "\"timeoutDetectionTimeLimit\":\"PT61S\", \"timeoutDetectionHighFrequencyThreshold\":\"4\", " +
                    "\"timeoutDetectionHighFrequencyTimeLimit\":\"PT11S\", \"timeoutDetectionOnWriteThreshold\":\"2\"," +
                    "\"timeoutDetectionOnWriteTimeLimit\":\"PT7S\", \"timeoutDetectionDisableCPUThreshold\":\"80.0\"}");

            ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            UserAgentContainer userAgentContainer = new UserAgentContainer();

            RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy)
                    .userAgent(userAgentContainer)
                    .build();

            assertEquals(options.sslHandshakeTimeoutInMillis(), Duration.ofSeconds(15).toMillis());
            assertEquals(options.timeoutDetectionEnabled(), false);
            assertEquals(options.timeoutDetectionTimeLimit(), Duration.ofSeconds(61));
            assertEquals(options.timeoutDetectionHighFrequencyThreshold(), 4);
            assertEquals(options.timeoutDetectionHighFrequencyTimeLimit(), Duration.ofSeconds(11));
            assertEquals(options.timeoutDetectionOnWriteThreshold(), 2);
            assertEquals(options.timeoutDetectionOnWriteTimeLimit(), Duration.ofSeconds(7));
            assertEquals(options.timeoutDetectionDisableCPUThreshold(), 80.0);

        } finally {
            System.clearProperty("azure.cosmos.directTcp.defaultOptions");
            System.clearProperty("COSMOS.TCP_HEALTH_CHECK_TIMEOUT_DETECTION_ENABLED");
        }
    }

    @Test(groups = "unit")
    public void sslHandshakeTimeoutTests() throws IOException {
        try {
            //Test sslHandshakeTimeout is Math.max(sslHandshakeTimeoutMinDuration, connectionTimeout)
            // Test sslHandshakeTimeoutMinDuration > default connectionTimeout
            System.setProperty("azure.cosmos.directTcp.defaultOptions", "{\"sslHandshakeTimeoutMinDuration\":\"PT15S\"}");

            RntbdTransportClient.Options options =
                    RntbdObjectMapper.readValue(
                            System.getProperty("azure.cosmos.directTcp.defaultOptions"),
                            RntbdTransportClient.Options.class);

            assertEquals(options.sslHandshakeTimeoutInMillis(), Duration.ofSeconds(15).toMillis());

            // Test sslHandshakeTimeoutMinDuration < customized connectionTimeout
            System.setProperty("azure.cosmos.directTcp.defaultOptions", "{\"sslHandshakeTimeoutMinDuration\":\"PT3S\", \"connectTimeout\":\"PT5S\"}");
            options =
                    RntbdObjectMapper.readValue(
                            System.getProperty("azure.cosmos.directTcp.defaultOptions"),
                            RntbdTransportClient.Options.class);

            assertEquals(options.sslHandshakeTimeoutInMillis(), Duration.ofSeconds(5).toMillis());


        } finally {
            System.clearProperty("azure.cosmos.directTcp.defaultOptions");
        }
    }

    @Test(groups = "unit")
    public void cancelRequestMono() throws InterruptedException, URISyntaxException, IllegalAccessException, SSLException {
        RxDocumentServiceRequest request =
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        URI locationToRoute = new URI("http://localhost-west:8080");
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy).build();
        final SslContext sslContext = SslContextBuilder.forClient().build();
        request.requestContext.locationEndpointToRoute = locationToRoute;
        RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, addressUri);
        RntbdRequestTimer requestTimer = new RntbdRequestTimer(5000, 5000);
        RntbdRequestRecord rntbdRequestRecord = new AsyncRntbdRequestRecord(requestArgs, requestTimer);

        RntbdEndpoint rntbdEndpoint = Mockito.mock(RntbdServiceEndpoint.class);
        Mockito.when(rntbdEndpoint.request(any())).thenReturn(rntbdRequestRecord);

        RntbdEndpoint.Provider endpointProvider = Mockito.mock(RntbdEndpoint.Provider.class);

        RntbdTransportClient transportClient = new RntbdTransportClient(
            options,
            sslContext,
            null,
            null,
            null);

        ReflectionUtils.setEndpointProvider(transportClient, endpointProvider);
        AddressSelector addressSelector = (AddressSelector) FieldUtils.readField(transportClient, "addressSelector", true);

        Mockito.when(endpointProvider.createIfAbsent(locationToRoute, addressUri, transportClient.getProactiveOpenConnectionsProcessor(), Configs.getMinConnectionPoolSizePerEndpoint(), addressSelector)).thenReturn(rntbdEndpoint);

        transportClient
            .invokeStoreAsync(
                addressUri,
                request)
            .cancelOn(Schedulers.boundedElastic())
            .subscribe()
            .dispose();

        // wait for the cancel signal to propagate
        Thread.sleep(500);

        assertThat(rntbdRequestRecord.isCancelled()).isTrue();
        assertThat(rntbdRequestRecord.isCompletedExceptionally()).isTrue();
    }

    private static RntbdTransportClient getRntbdTransportClientUnderTest(
        final UserAgentContainer userAgent,
        final ConnectionPolicy connectionPolicy,
        final RntbdResponse expected
    ) {

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy)
            .userAgent(userAgent)
            .build();

        final SslContext sslContext;

        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (final Exception error) {
            throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
        }

        RntbdTransportClient rntbdTransportClient = new RntbdTransportClient(options, sslContext, null, null, null);
        FakeEndpoint.Provider endpointProvider = new FakeEndpoint.Provider(options, sslContext, expected, null);
        ReflectionUtils.setEndpointProvider(rntbdTransportClient, endpointProvider);
        return rntbdTransportClient;
    }

    private void validateFailure(final Mono<? extends StoreResponse> responseMono, final FailureValidator validator) {
        validateFailure(responseMono, validator, requestTimeout.toMillis());
    }

    private static void validateFailure(
        final Mono<? extends StoreResponse> mono, final FailureValidator validator, final long timeout
    ) {

        final TestSubscriber<StoreResponse> subscriber = new TestSubscriber<>();
        mono.subscribe(subscriber);

        subscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        assertThat(subscriber.errorCount()).isEqualTo(1);
        subscriber.assertSubscribed();
        subscriber.assertNoValues();
        validator.validate(subscriber.errors().get(0));
    }

    // region Types

    private static final class FakeChannel extends EmbeddedChannel {

        private static final ServerProperties serverProperties = new ServerProperties("agent", "3.0.0");
        private final BlockingQueue<RntbdResponse> responses;

        FakeChannel(final BlockingQueue<RntbdResponse> responses, final ChannelHandler... handlers) {
            super(handlers);
            this.responses = responses;
        }

        @Override
        protected void handleInboundMessage(final Object message) {
            super.handleInboundMessage(message);
            assertTrue(message instanceof ByteBuf);
        }

        @Override
        protected void handleOutboundMessage(final Object message) {

            assertTrue(message instanceof ByteBuf);

            final ByteBuf out = Unpooled.buffer();
            final ByteBuf in = (ByteBuf) message;

            // This is the end of the outbound pipeline and so we can do what we wish with the outbound message

            if (in.getUnsignedIntLE(4) == 0) {

                final RntbdContextRequest request = RntbdContextRequest.decode(in.copy());
                final RntbdContext rntbdContext = RntbdContext.from(request, serverProperties, HttpResponseStatus.OK);

                rntbdContext.encode(out);

            } else {

                final RntbdRequest rntbdRequest = RntbdRequest.decode(in.copy());
                final RntbdResponse rntbdResponse;

                try {
                    rntbdResponse = this.responses.take();
                } catch (final Exception error) {
                    throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
                }

                assertEquals(rntbdRequest.getTransportRequestId(), rntbdResponse.getTransportRequestId());
                rntbdResponse.encode(out);
                out.setBytes(8, in.slice(8, 16));  // Overwrite activityId
            }

            this.writeInbound(out);
        }
    }

    private static final class FakeEndpoint implements RntbdEndpoint {

        final RntbdRequestTimer requestTimer;
        final FakeChannel fakeChannel;
        final Uri addressUri;
        final URI remoteURI;
        final Tag tag;
        private final Tag clientMetricTag;
        private final RntbdDurableEndpointMetrics durableEndpointMetrics;

        private FakeEndpoint(
            final Config config, final RntbdRequestTimer timer, final Uri addressUri,
            final RntbdResponse... expected
        ) {

            URI physicalAddress = addressUri.getURI();

            try {
                this.addressUri = addressUri;
                this.remoteURI = new URI(
                    physicalAddress.getScheme(),
                    null,
                    physicalAddress.getHost(),
                    physicalAddress.getPort(),
                    null,
                    null,
                    null);
                this.durableEndpointMetrics = new RntbdDurableEndpointMetrics();
                this.durableEndpointMetrics.setEndpoint(this);
            } catch (URISyntaxException error) {
                throw new IllegalArgumentException(
                    lenientFormat("addressUri %s cannot be parsed as a server-based authority", addressUri),
                    error);
            }

            final ArrayBlockingQueue<RntbdResponse> responses = new ArrayBlockingQueue<>(
                expected.length, true, Arrays.asList(expected)
            );

            RntbdRequestManager requestManager = new RntbdRequestManager(
                    new RntbdClientChannelHealthChecker(config),
                    30,
                    null,
                    Duration.ofMillis(100).toNanos(),
                    null,
                    config.tcpNetworkRequestTimeoutInNanos());
            this.requestTimer = timer;

            this.fakeChannel = new FakeChannel(responses,
                new RntbdContextNegotiator(requestManager, config.userAgent()),
                new RntbdRequestEncoder(),
                new RntbdResponseDecoder(),
                requestManager
            );

            this.tag = Tag.of(FakeEndpoint.class.getSimpleName(), this.fakeChannel.remoteAddress().toString());
            this.clientMetricTag = Tag.of(
                TagName.ServiceEndpoint.toString(),
                String.format("%s_%d", physicalAddress.getHost(), physicalAddress.getPort()));
        }

        // region Accessors

        @Override
        public int channelsAcquiredMetric() {
            return 0;
        }

        @Override
        public RntbdDurableEndpointMetrics durableEndpointMetrics() {
            return new RntbdDurableEndpointMetrics();
        }

        @Override
        public int channelsAvailableMetric() {
            return 0;
        }

        @Override
        public int concurrentRequests() {
            return 0;
        }

        @Override
        public int gettingEstablishedConnectionsMetrics() {
            return 0;
        }

        @Override
        public Instant getCreatedTime() {
            return null;
        }

        @Override
        public long lastRequestNanoTime() {
            return 0;
        }

        @Override
        public long lastSuccessfulRequestNanoTime() {
            return 0;
        }

        @Override
        public int channelsMetrics() {
            return 0;
        }

        @Override
        public int executorTaskQueueMetrics() {
            return 0;
        }

        @Override
        public long id() {
            return 0L;
        }

        @Override
        public boolean isClosed() {
            return !this.fakeChannel.isOpen();
        }

        @Override
        public int maxChannels() {
            return 0;
        }

        @Override
        public SocketAddress remoteAddress() {
            return this.fakeChannel.remoteAddress();
        }

        @Override
        public URI serverKey() { return this.remoteURI; }

        @Override
        public int requestQueueLength() {
            return 0;
        }

        @Override
        public Tag tag() {
            return this.tag;
        }

        @Override
        public Tag clientMetricTag() { return this.clientMetricTag;}

        @Override
        public long usedDirectMemory() {
            return 0;
        }

        @Override
        public long usedHeapMemory() {
            return 0;
        }

        @Override
        public URI serviceEndpoint() {
            return null;
        }

        @Override
        public void injectConnectionErrors(String ruleId, double threshold, Class<?> eventType) {
            throw new NotImplementedException("injectConnectionErrors is not supported in FakeEndpoint");
        }

        @Override
        public int getMinChannelsRequired() {
            return Configs.getMinConnectionPoolSizePerEndpoint();
        }

        @Override
        public void setMinChannelsRequired(int minConnectionsRequired) {
            throw new NotImplementedException("setMinChannelsRequired is not implemented for FakeServiceEndpoint");
        }

        @Override
        public Uri getAddressUri() {
            return addressUri;
        }

        // endregion

        // region Methods

        @Override
        public void close() {
            this.fakeChannel.close().syncUninterruptibly();
        }

        @Override
        public RntbdRequestRecord request(final RntbdRequestArgs requestArgs) {
            final RntbdRequestRecord requestRecord = new AsyncRntbdRequestRecord(requestArgs, this.requestTimer);
            this.fakeChannel.writeOutbound(requestRecord);
            return requestRecord;
        }

        @Override
        public OpenConnectionRntbdRequestRecord openConnection(RntbdRequestArgs openConnectionRequestArgs) {
            throw new NotImplementedException("openConnection is not supported in FakeEndpoint.");
        }

        // endregion

        // region Types

        static class Provider implements RntbdEndpoint.Provider {

            final Config config;
            final RntbdResponse expected;
            final RntbdRequestTimer timer;
            final IAddressResolver addressResolver;

            Provider(RntbdTransportClient.Options options, SslContext sslContext, RntbdResponse expected, IAddressResolver addressResolver) {
                this.config = new Config(options, sslContext, LogLevel.WARN);
                this.timer = new RntbdRequestTimer(
                    config.tcpNetworkRequestTimeoutInNanos(),
                    config.requestTimerResolutionInNanos());
                this.expected = expected;
                this.addressResolver = addressResolver;
            }

            @Override
            public void close() throws RuntimeException {
                this.timer.close();
            }

            @Override
            public Config config() {
                return this.config;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public int evictions() {
                return 0;
            }

            @Override
            public RntbdEndpoint createIfAbsent(URI serviceEndpoint, Uri addressUri, ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor, int minRequiredChannelsForEndpoint, AddressSelector addressSelector) {
                return new FakeEndpoint(config, timer, addressUri, expected);
            }

            @Override
            public RntbdEndpoint get(URI physicalAddress) {
                return new FakeEndpoint(config, timer, new Uri(physicalAddress.toString()), expected);
            }

            @Override
            public IAddressResolver getAddressResolver() {
                return this.addressResolver;
            }

            @Override
            public Stream<RntbdEndpoint> list() {
                return Stream.empty();
            }

            @Override
            public boolean isClosed() {
                return false;
            }
        }

        // endregion
    }

    private static final class RntbdTestConfiguration {

        static String AccountHost = System.getProperty("ACCOUNT_HOST",
            StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("ACCOUNT_HOST")),
                "https://localhost:8081/"
            )
        );

        static String AccountKey = System.getProperty("ACCOUNT_KEY",
            StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("ACCOUNT_KEY")),
                "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="
            )
        );

        static String RntbdAuthority = System.getProperty("rntbd.authority",
            StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("RNTBD_AUTHORITY")),
                String.format("%s:10251", URI.create(AccountHost).getHost())
            )
        );

        private RntbdTestConfiguration() {
        }
    }

    // endregion
}
