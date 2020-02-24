// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.ConflictException;
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
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.channel.ConnectTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

;

/**
 * Tests validating {@link HttpTransportClient}
 */
public class HttpTransportClientTest {
    private final static Configs configs = new Configs();
    private final static int TIMEOUT = 1000;

    private final URI physicalAddress = URI.create(
            "https://by4prdddc03-docdb-1.documents.azure.com:9056" +
                    "/apps/b76af614-5421-4318-4c9e-33056ff5a2bf/services/e7c8d429-c379-40c9-9486-65b89b70be2f" +
                    "/partitions/5f5b8766-3bdf-4713-b85a-a55ac2ccd62c/replicas/131828696163674404p/");

    private final long lsn = 5;
    private final String partitionKeyRangeId = "3";

    @Test(groups = "unit")
    public void getResourceFeedUri_Document() throws Exception {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "dbs/db/colls/col", ResourceType.Document);
        URI res = HttpTransportClient.getResourceFeedUri(req.getResourceType(), physicalAddress, req);
        assertThat(res.toString()).isEqualTo(physicalAddress.toString() + HttpUtils.urlEncode("dbs/db/colls/col/docs"));
    }

    @Test(groups = "unit")
    public void getResourceFeedUri_Attachment() throws Exception {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "dbs/db/colls/col", ResourceType.Attachment);
        URI res = HttpTransportClient.getResourceFeedUri(req.getResourceType(), physicalAddress, req);
        assertThat(res.toString()).isEqualTo(physicalAddress.toString() + HttpUtils.urlEncode("dbs/db/colls/col/attachments"));
    }

    @Test(groups = "unit")
    public void getResourceFeedUri_Collection() throws Exception {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "dbs/db", ResourceType.DocumentCollection);
        URI res = HttpTransportClient.getResourceFeedUri(req.getResourceType(), physicalAddress, req);
        assertThat(res.toString()).isEqualTo(physicalAddress.toString() + HttpUtils.urlEncode("dbs/db/colls"));
    }

    @Test(groups = "unit")
    public void getResourceFeedUri_Conflict() throws Exception {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col", ResourceType.Conflict);
        URI res = HttpTransportClient.getResourceFeedUri(req.getResourceType(), physicalAddress, req);
        assertThat(res.toString()).isEqualTo(physicalAddress.toString() + HttpUtils.urlEncode("dbs/db/colls/col/conflicts"));
    }

    @Test(groups = "unit")
    public void getResourceFeedUri_Database() throws Exception {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/", ResourceType.Database);
        URI res = HttpTransportClient.getResourceFeedUri(req.getResourceType(), physicalAddress, req);
        assertThat(res.toString()).isEqualTo(physicalAddress.toString() + "dbs");
    }

    public static HttpTransportClient getHttpTransportClientUnderTest(int requestTimeout,
                                                                      UserAgentContainer userAgent,
                                                                      HttpClient httpClient) {
        class HttpTransportClientUnderTest extends HttpTransportClient {
            public HttpTransportClientUnderTest(int requestTimeout, UserAgentContainer userAgent) {
                super(configs, requestTimeout, userAgent);
            }

            @Override
            HttpClient createHttpClient(int requestTimeout) {
                return httpClient;
            }
        }

        return new HttpTransportClientUnderTest(requestTimeout, userAgent);
    }

    @Test(groups = "unit")
    public void validateDefaultHeaders() {
        HttpResponse mockedResponse = new HttpClientMockWrapper.HttpClientBehaviourBuilder()
                .withContent("").withStatus(200)
                .withHeaders(new HttpHeaders())
                .asHttpResponse();
        HttpClientMockWrapper httpClientMockWrapper = new HttpClientMockWrapper(mockedResponse);

        UserAgentContainer userAgentContainer = new UserAgentContainer();
        userAgentContainer.setSuffix("i am suffix");

        HttpTransportClient transportClient = getHttpTransportClientUnderTest(100,
                userAgentContainer,
                httpClientMockWrapper.getClient());

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "dbs/db/colls/col", ResourceType.Document);
        request.setContentBytes(new byte[0]);

        transportClient.invokeStoreAsync(physicalAddress, request).block();

        assertThat(httpClientMockWrapper.getCapturedInvocation()).asList().hasSize(1);
        HttpRequest httpRequest = httpClientMockWrapper.getCapturedInvocation().get(0);

        assertThat(httpRequest.headers().value(HttpConstants.HttpHeaders.USER_AGENT)).endsWith("i am suffix");
        assertThat(httpRequest.headers().value(HttpConstants.HttpHeaders.CACHE_CONTROL)).isEqualTo("no-cache");
        assertThat(httpRequest.headers().value(HttpConstants.HttpHeaders.ACCEPT)).isEqualTo("application/json");
        assertThat(httpRequest.headers().value(HttpConstants.HttpHeaders.VERSION)).isEqualTo(HttpConstants.Versions.CURRENT_VERSION);

    }

    @DataProvider(name = "fromMockedHttpResponseToExpectedDocumentClientException")
    public Object[][] fromMockedHttpResponseToExpectedDocumentClientException() {
        return new Object[][]{
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(401)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(UnauthorizedException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(403)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(ForbiddenException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(404)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(NotFoundException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)

                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(404)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaders(HttpConstants.HttpHeaders.CONTENT_TYPE, "text/html"),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)

                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(400)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(BadRequestException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(405)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(MethodNotAllowedException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(409)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(ConflictException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(412)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(PreconditionFailedException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(412)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(PreconditionFailedException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(413)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(RequestEntityTooLargeException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(423)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(LockedException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(503)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(ServiceUnavailableException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(408)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(RequestTimeoutException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(449)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(RetryWithException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(429)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(RequestRateTooLargeException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(500)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId),

                        FailureValidator.builder()
                                .instanceOf(InternalServerErrorException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(410)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaderSubStatusCode(HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE),

                        FailureValidator.builder()
                                .instanceOf(InvalidPartitionException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(410)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaderSubStatusCode(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE),

                        FailureValidator.builder()
                                .instanceOf(PartitionKeyRangeGoneException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(410)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaderSubStatusCode(HttpConstants.SubStatusCodes.COMPLETING_SPLIT),

                        FailureValidator.builder()
                                .instanceOf(PartitionKeyRangeIsSplittingException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(410)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaderSubStatusCode(HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION),

                        FailureValidator.builder()
                                .instanceOf(PartitionIsMigratingException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
                {
                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withContent("").withStatus(410)
                                .withHeaderLSN(lsn)
                                .withHeaderPartitionKeyRangeId(partitionKeyRangeId)
                                .withHeaderSubStatusCode(0),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                                .resourceAddress("dbs/db/colls/col")
                                .lsn(lsn)
                                .partitionKeyRangeId(partitionKeyRangeId)
                },
        };
    }

    /**
     * Validates the error handling behaviour of HttpTransportClient for https status codes >= 400
     * @param mockedResponseBuilder
     * @param failureValidatorBuilder
     */
    @Test(groups = "unit", dataProvider = "fromMockedHttpResponseToExpectedDocumentClientException")
    public void failuresWithHttpStatusCodes(HttpClientMockWrapper.HttpClientBehaviourBuilder mockedResponseBuilder,
                                            FailureValidator.Builder failureValidatorBuilder) {
        HttpClientMockWrapper httpClientMockWrapper = new HttpClientMockWrapper(mockedResponseBuilder);
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        HttpTransportClient transportClient = getHttpTransportClientUnderTest(
                100,
                userAgentContainer,
                httpClientMockWrapper.getClient());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "dbs/db/colls/col", ResourceType.Document);
        request.setContentBytes(new byte[0]);

        Mono<StoreResponse> storeResp = transportClient.invokeStoreAsync(
                physicalAddress,
                request);

        validateFailure(storeResp, failureValidatorBuilder.build());
    }

    @DataProvider(name = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public Object[][] fromMockedNetworkFailureToExpectedDocumentClientException() {
        return new Object[][]{
                // create request, retriable network exception
                {
                        createRequestFromName(
                            OperationType.Create, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new UnknownHostException()),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                },

                // create request, retriable network exception
                {
                        createRequestFromName(
                                OperationType.Create, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new UnknownHostException()),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                },

                // create request, retriable network exception
                {
                        createRequestFromName(
                                OperationType.Create, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new ConnectTimeoutException()),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                },

                // read request, retriable network exception
                {
                        createRequestFromName(
                                OperationType.Read, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new ConnectTimeoutException()),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                },

                // create request, non-retriable network exception
                {
                        createRequestFromName(
                                OperationType.Create, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new RuntimeException()),

                        FailureValidator.builder()
                                .instanceOf(ServiceUnavailableException.class)
                },

                // read request, non-retriable network exception
                {
                        createRequestFromName(
                                OperationType.Read, "dbs/db/colls/col", ResourceType.Document),

                        HttpClientMockWrapper.
                                httpClientBehaviourBuilder()
                                .withNetworkFailure(new RuntimeException()),

                        FailureValidator.builder()
                                .instanceOf(GoneException.class)
                },
        };
    }

    /**
     * Validates the error handling behaviour of HttpTransportClient for network failures from which http status codes
     * cannot be derived. For example Socket Connection failure.
     * @param request
     * @param mockedResponseBuilder
     * @param failureValidatorBuilder
     */
    @Test(groups = "unit", dataProvider = "fromMockedNetworkFailureToExpectedDocumentClientException")
    public void networkFailures(RxDocumentServiceRequest request,
                                HttpClientMockWrapper.HttpClientBehaviourBuilder mockedResponseBuilder,
                                FailureValidator.Builder failureValidatorBuilder) {
        HttpClientMockWrapper httpClientMockWrapper = new HttpClientMockWrapper(mockedResponseBuilder);
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        HttpTransportClient transportClient = getHttpTransportClientUnderTest(
                100,
                userAgentContainer,
                httpClientMockWrapper.getClient());

        Mono<StoreResponse> storeResp = transportClient.invokeStoreAsync(
                physicalAddress,
                request);

        validateFailure(storeResp, failureValidatorBuilder.build());
    }

    private static RxDocumentServiceRequest createRequestFromName(
            OperationType operationType,
            String resourceFullName,
            ResourceType resourceType) {
        return createRequestFromName(operationType, resourceFullName, resourceType, new byte[0]);
    }

    private static RxDocumentServiceRequest createRequestFromName(
            OperationType operationType,
            String resourceFullName,
            ResourceType resourceType,
            byte[] content) {
        RxDocumentServiceRequest req = RxDocumentServiceRequest.create(
                operationType,
                resourceType,
                resourceFullName,
                new HashMap<>());

        req.setContentBytes(content);
        return req;
    }

    public void validateSuccess(Mono<StoreResponse> single, StoreResponseValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public void validateFailure(Mono<StoreResponse> single,
                                FailureValidator validator) {
        validateFailure(single, validator, TIMEOUT);
    }

    public static void validateFailure(Mono<StoreResponse> single,
                                       FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        Assertions.assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }
}
