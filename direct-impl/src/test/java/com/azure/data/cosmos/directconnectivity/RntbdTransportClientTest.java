/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.BaseAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.InternalServerErrorException;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdContext;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdContextNegotiator;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdContextRequest;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdRequestEncoder;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdResponse;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdResponseDecoder;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdUUID;
import com.azure.data.cosmos.rx.FailureValidator;
import com.azure.data.cosmos.internal.BadRequestException;
import com.azure.data.cosmos.internal.InvalidPartitionException;
import com.azure.data.cosmos.internal.NotFoundException;
import com.azure.data.cosmos.internal.PartitionIsMigratingException;
import com.azure.data.cosmos.internal.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Single;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.azure.data.cosmos.internal.HttpConstants.HttpHeaders;
import static com.azure.data.cosmos.internal.HttpConstants.HttpMethods;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class RntbdTransportClientTest {

    final private static Logger logger = LoggerFactory.getLogger(RntbdTransportClientTest.class);
    final private static int lsn = 5;
    final private static ByteBuf noContent = Unpooled.wrappedBuffer(new byte[0]);
    final private static String partitionKeyRangeId = "3";
    final private static URI physicalAddress = URI.create("rntbd://host:10251/replica-path/");
    final private static Duration requestTimeout = Duration.ofSeconds(1000);

    @DataProvider(name = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public Object[][] fromMockedNetworkFailureToExpectedDocumentClientException() {

        return new Object[][] {
            // TODO: DANOBLE: add network failure exception test cases
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.NAME_CACHE_IS_STALE)
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
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.PARTITION_KEY_RANGE_GONE)
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
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_SPLIT)
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
                        HttpHeaders.SUB_STATUS, Integer.toString(SubStatusCodes.COMPLETING_PARTITION_MIGRATION)
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
                        HttpHeaders.SUB_STATUS, String.valueOf(SubStatusCodes.UNKNOWN)),
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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
                        HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId
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

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options(requestTimeout);
        final SslContext sslContext = SslContextBuilder.forClient().build();
        final UserAgentContainer userAgent = new UserAgentContainer();

        try (final RntbdTransportClient transportClient = new RntbdTransportClient(options, sslContext, userAgent)) {

            final BaseAuthorizationTokenProvider authorizationTokenProvider = new BaseAuthorizationTokenProvider(
                RntbdTestConfiguration.AccountKey
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

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                ResourceType.DatabaseAccount,
                Paths.DATABASE_ACCOUNT_PATH_SEGMENT,
                builder.build()
            );

            Single<StoreResponse> responseSingle = transportClient.invokeStoreAsync(physicalAddress, null, request);

            responseSingle.toObservable().toBlocking().subscribe(new Subscriber<StoreResponse>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable error) {
                    String cs = "Expected %s, not %s";
                    assertTrue(error instanceof GoneException, String.format(cs, GoneException.class, error.getClass()));
                    Throwable cause = error.getCause();
                    if (cause != null) {
                        // assumption: cosmos isn't listening on 10251
                        assertTrue(cause instanceof ConnectException, String.format(cs, ConnectException.class, error.getClass()));
                    }
                }

                @Override
                public void onNext(StoreResponse response) {
                    fail(String.format("Expected GoneException, not a StoreResponse: %s", response));
                }
            });

        } catch (Exception error) {
            String message = String.format("%s: %s", error.getClass(), error.getMessage());
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
    @Test(enabled = false, groups = "unit", dataProvider = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public void verifyNetworkFailure(
        FailureValidator.Builder builder,
        RxDocumentServiceRequest request,
        CosmosClientException exception
    ) {
        throw new UnsupportedOperationException("TODO: DANOBLE: Implement this test");
    }

    /**
     * Validates the error handling behavior of the {@link RntbdTransportClient} for HTTP status codes >= 400
     *
     * @param builder   A feature validator builder to confirm that response is correctly mapped to an exception
     * @param request   An RNTBD request instance
     * @param response  The RNTBD response instance to be returned as a result of the request
     */
    @Test(enabled = true, groups = "unit", dataProvider = "fromMockedRntbdResponseToExpectedDocumentClientException")
    public void verifyRequestFailures(
        FailureValidator.Builder builder,
        RxDocumentServiceRequest request,
        RntbdResponse response
    ) {
        final UserAgentContainer userAgent = new UserAgentContainer();
        final Duration timeout = Duration.ofMillis(1000);

        try (final RntbdTransportClient client = getRntbdTransportClientUnderTest(userAgent, timeout, response)) {

            Single<StoreResponse> responseSingle;

            try {
                responseSingle = client.invokeStoreAsync(
                    physicalAddress, new ResourceOperation(request.getOperationType(), request.getResourceType()), request
                );
            } catch (Exception error) {
                throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
            }

            validateFailure(responseSingle, builder.build());
        }
    }

    private static RntbdTransportClient getRntbdTransportClientUnderTest(
            UserAgentContainer userAgent,
            Duration requestTimeout,
            RntbdResponse expected
    ) {

        final RntbdTransportClient.Options options = new RntbdTransportClient.Options(requestTimeout);
        final SslContext sslContext;

        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (Exception error) {
            throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
        }

        final RntbdTransportClient.EndpointFactory endpointFactory = spy(new RntbdTransportClient.EndpointFactory(
            options, sslContext, userAgent
        ));

        final RntbdTransportClient client = new RntbdTransportClient(endpointFactory);

        doAnswer((Answer) invocation -> {

            RntbdTransportClient.EndpointFactory factory = (RntbdTransportClient.EndpointFactory) invocation.getMock();
            URI physicalAddress = invocation.getArgumentAt(0, URI.class);
            return new FakeEndpoint(factory, physicalAddress, expected);

        }).when(endpointFactory).createEndpoint(any());

        return client;
    }

    private void validateFailure(Single<StoreResponse> single, FailureValidator validator) {
        validateFailure(single, validator, requestTimeout.toMillis());
    }

    private static void validateFailure(Single<StoreResponse> single, FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        Assertions.assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }

    // region Types

    final private static class FakeChannel extends EmbeddedChannel {

        final private static ServerProperties serverProperties = new ServerProperties("agent", "3.0.0");
        final private BlockingQueue<RntbdResponse> responses;

        FakeChannel(BlockingQueue<RntbdResponse> responses, ChannelHandler... handlers) {
            super(handlers);
            this.responses = responses;
        }

        @Override
        protected void handleInboundMessage(Object message) {
            super.handleInboundMessage(message);
            assertTrue(message instanceof ByteBuf);
        }

        @Override
        protected void handleOutboundMessage(Object message) {

            assertTrue(message instanceof ByteBuf);

            final ByteBuf out = Unpooled.buffer();
            final ByteBuf in = (ByteBuf) message;

            // This is the end of the outbound pipeline and so we can do what we wish with the outbound message

            if (in.getUnsignedIntLE(4) == 0) {

                RntbdContextRequest request = RntbdContextRequest.decode(in.copy());
                RntbdContext rntbdContext = RntbdContext.from(request, serverProperties, HttpResponseStatus.OK);

                rntbdContext.encode(out);

            } else {

                RntbdResponse rntbdResponse;

                try {
                    rntbdResponse = this.responses.take();
                } catch (Exception error) {
                    throw new AssertionError(String.format("%s: %s", error.getClass(), error.getMessage()));
                }

                rntbdResponse.encode(out);
                out.setBytes(8, in.slice(8, 16));  // Overwrite activityId
            }

            this.writeInbound(out);
        }
    }

    final private static class FakeEndpoint implements RntbdTransportClient.Endpoint {

        final FakeChannel fakeChannel;
        final URI physicalAddress;
        final RntbdRequestManager requestManager;

        FakeEndpoint(
            RntbdTransportClient.EndpointFactory factory,
            URI physicalAddress,
            RntbdResponse... expected
        ) {

            ArrayBlockingQueue<RntbdResponse> responses = new ArrayBlockingQueue<RntbdResponse>(
                expected.length, true, Arrays.asList(expected)
            );

            this.requestManager = new RntbdRequestManager();
            this.physicalAddress = physicalAddress;

            this.fakeChannel = new FakeChannel(responses,
                new RntbdContextNegotiator(this.requestManager, factory.getUserAgent()),
                new RntbdRequestEncoder(),
                new RntbdResponseDecoder(),
                this.requestManager
            );
        }

        @Override
        public Future<?> close() {
            DefaultEventExecutor executor = new DefaultEventExecutor();
            Future<?> future = executor.newSucceededFuture(true);
            this.fakeChannel.close().syncUninterruptibly();
            return future;
        }

        @Override
        public CompletableFuture<StoreResponse> write(RntbdRequestArgs requestArgs) {
            final CompletableFuture<StoreResponse> responseFuture = this.requestManager.createStoreResponseFuture(requestArgs);
            this.fakeChannel.writeOutbound(requestArgs);
            return responseFuture;
        }
    }

    final private static class RntbdTestConfiguration {

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
