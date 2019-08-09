// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.ConflictException;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.data.cosmos.ForbiddenException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.LockedException;
import com.azure.data.cosmos.MethodNotAllowedException;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.PreconditionFailedException;
import com.azure.data.cosmos.RequestEntityTooLargeException;
import com.azure.data.cosmos.RequestRateTooLargeException;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.RetryWithException;
import com.azure.data.cosmos.ServiceUnavailableException;
import com.azure.data.cosmos.UnauthorizedException;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdContext;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdContextNegotiator;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequest;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestEncoder;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestTimer;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdResponse;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdResponseDecoder;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdUUID;
import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.InternalServerErrorException;
import com.azure.data.cosmos.InvalidPartitionException;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.PartitionIsMigratingException;
import com.azure.data.cosmos.PartitionKeyRangeIsSplittingException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.azure.data.cosmos.internal.HttpConstants.HttpHeaders;
import static com.azure.data.cosmos.internal.HttpConstants.HttpMethods;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public final class RntbdTransportClientTest {

    private static final Logger logger = LoggerFactory.getLogger(RntbdTransportClientTest.class);
    private static final int lsn = 5;
    private static final ByteBuf noContent = Unpooled.wrappedBuffer(new byte[0]);
    private static final String partitionKeyRangeId = "3";
    private static final URI physicalAddress = URI.create("rntbd://host:10251/replica-path/");
    private static final Duration requestTimeout = Duration.ofSeconds(1000);

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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_SPLIT),
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
                RxDocumentServiceRequest.create(
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
    @Test(enabled = false, groups = { "direct" })
    public void verifyGoneResponseMapsToGoneException() throws Exception {

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(requestTimeout).build();
        final SslContext sslContext = SslContextBuilder.forClient().build();

        try (final RntbdTransportClient transportClient = new RntbdTransportClient(options, sslContext)) {

            final BaseAuthorizationTokenProvider authorizationTokenProvider = new BaseAuthorizationTokenProvider(
                new CosmosKeyCredential(RntbdTestConfiguration.AccountKey)
            );

            final URI physicalAddress = new URI("rntbd://"
                + RntbdTestConfiguration.RntbdAuthority
                + "/apps/DocDbApp/services/DocDbMaster0/partitions/780e44f4-38c8-11e6-8106-8cdcd42c33be/replicas/1p/"
            );

            final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            builder.put(HttpHeaders.X_DATE, Utils.nowAsRFC1123());

            final String token = authorizationTokenProvider.generateKeyAuthorizationSignature(HttpMethods.GET,
                Paths.DATABASE_ACCOUNT_PATH_SEGMENT,
                ResourceType.DatabaseAccount,
                builder.build()
            );

            builder.put(HttpHeaders.AUTHORIZATION, token);

            final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                ResourceType.DatabaseAccount,
                Paths.DATABASE_ACCOUNT_PATH_SEGMENT,
                builder.build()
            );

            final Mono<StoreResponse> responseMono = transportClient.invokeStoreAsync(physicalAddress, request);

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
        final CosmosClientException exception
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
    //FIXME: Test inconsistently flakes with assertion error.
    @Ignore
    @Test(enabled = true, groups = { "unit" }, dataProvider = "fromMockedRntbdResponseToExpectedDocumentClientException")
    public void verifyRequestFailures(
        final FailureValidator.Builder builder,
        final RxDocumentServiceRequest request,
        final RntbdResponse response
    ) {
        final UserAgentContainer userAgent = new UserAgentContainer();
        final Duration timeout = Duration.ofMillis(1000);

        try (final RntbdTransportClient client = getRntbdTransportClientUnderTest(userAgent, timeout, response)) {

            final Mono<StoreResponse> responseMono;

            try {
                responseMono = client.invokeStoreAsync(physicalAddress, request);
            } catch (final Exception error) {
                throw new AssertionError(String.format("%s: %s", error.getClass(), error));
            }

            this.validateFailure(responseMono, builder.build());
        }
    }

    private static RntbdTransportClient getRntbdTransportClientUnderTest(
        final UserAgentContainer userAgent,
        final Duration requestTimeout,
        final RntbdResponse expected
    ) {

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(requestTimeout)
            .userAgent(userAgent)
            .build();

        final SslContext sslContext;

        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (final Exception error) {
            throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
        }

        return new RntbdTransportClient(new FakeEndpoint.Provider(options, sslContext, expected));
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
        final URI physicalAddress;

        private FakeEndpoint(
            final Config config, final RntbdRequestTimer timer, final URI physicalAddress,
            final RntbdResponse... expected
        ) {

            final ArrayBlockingQueue<RntbdResponse> responses = new ArrayBlockingQueue<>(
                expected.length, true, Arrays.asList(expected)
            );

            RntbdRequestManager requestManager = new RntbdRequestManager(30);
            this.physicalAddress = physicalAddress;
            this.requestTimer = timer;

            this.fakeChannel = new FakeChannel(responses,
                new RntbdContextNegotiator(requestManager, config.getUserAgent()),
                new RntbdRequestEncoder(),
                new RntbdResponseDecoder(),
                requestManager
            );
        }

        @Override
        public String getName() {
            return "FakeEndpoint";
        }

        @Override
        public void close() {
            this.fakeChannel.close().syncUninterruptibly();
        }

        @Override
        public RntbdRequestRecord request(final RntbdRequestArgs requestArgs) {
            final RntbdRequestRecord requestRecord = new RntbdRequestRecord(requestArgs, this.requestTimer);
            this.fakeChannel.writeOutbound(requestRecord);
            return requestRecord;
        }

        static class Provider implements RntbdEndpoint.Provider {

            final Config config;
            final RntbdResponse expected;
            final RntbdRequestTimer timer;

            Provider(RntbdTransportClient.Options options, SslContext sslContext, RntbdResponse expected) {
                this.config = new Config(options, sslContext, LogLevel.WARN);
                this.timer = new RntbdRequestTimer(config.getRequestTimeout());
                this.expected = expected;
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
            public RntbdEndpoint get(URI physicalAddress) {
                return new FakeEndpoint(config, timer, physicalAddress, expected);
            }

            @Override
            public Stream<RntbdEndpoint> list() {
                return Stream.empty();
            }
        }
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
