// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.SocketException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

public class RxGatewayStoreModelTest {
    private final static int TIMEOUT = 10000;

    @DataProvider(name = "sessionTokenConfigProvider")
    public Object[][] sessionTokenConfigProvider() {
        return new Object[][]{
            // defaultConsistencyLevel, requestConsistencyLevel,requestOperationType, requestResourceType, sessionTokenFromUser, finalSessionTokenType

            // Skip applying session token for master operation
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Offer, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Database, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.User, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.UserDefinedType, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Permission, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Topology, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.DatabaseAccount, true, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.ReadFeed, ResourceType.PartitionKeyRange, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.DocumentCollection, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Trigger, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.UserDefinedFunction, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.Create, ResourceType.StoredProcedure, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, null, OperationType.QueryPlan, ResourceType.Document, false, SessionTokenType.NONE},

            // skip applying the session token when Eventual Consistency is explicitly requested
            // on request-level for data plane operations.
            {ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, OperationType.Read, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, OperationType.Query, ResourceType.Document, true, SessionTokenType.NONE},

            // skip applying the session token if default and request consistency level is not session
            {ConsistencyLevel.EVENTUAL, null, OperationType.Read, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.BOUNDED_STALENESS, null, OperationType.Create, ResourceType.Document, true, SessionTokenType.NONE},
            {ConsistencyLevel.STRONG, null, OperationType.Query, ResourceType.Document, false, SessionTokenType.NONE},
            {ConsistencyLevel.CONSISTENT_PREFIX, null, OperationType.Delete, ResourceType.Document, true, SessionTokenType.NONE},

            // Apply session token for other scenarios
            {ConsistencyLevel.SESSION, null, OperationType.Read, ResourceType.Document, true, SessionTokenType.USER},
            {ConsistencyLevel.BOUNDED_STALENESS, ConsistencyLevel.SESSION, OperationType.Create, ResourceType.Document, true, SessionTokenType.USER},
            {ConsistencyLevel.SESSION, ConsistencyLevel.SESSION, OperationType.Query, ResourceType.Document, false, SessionTokenType.SDK},
            {ConsistencyLevel.STRONG, ConsistencyLevel.SESSION, OperationType.ExecuteJavaScript, ResourceType.StoredProcedure, false, SessionTokenType.SDK}
        };
    }

    @Test(groups = "unit")
    public void readTimeout() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManager = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);

        Mockito.doReturn(new URI("https://localhost"))
                .when(globalEndpointManager).resolveServiceEndpoint(any());
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
                .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.SESSION)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();
        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
                sessionContainer,
                ConsistencyLevel.SESSION,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = new DocumentServiceRequestContext();


        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                .instanceOf(CosmosException.class)
                .causeInstanceOf(ReadTimeoutException.class)
                .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                .statusCode(HttpConstants.StatusCodes.REQUEST_TIMEOUT)
                .subStatusCode(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT)
                .build());
    }

    @Test(groups = "unit")
    public void serviceUnavailable() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManager = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);
        Mockito.doReturn(new URI("https://localhost"))
               .when(globalEndpointManager).resolveServiceEndpoint(any());
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(new SocketException("Dummy SocketException")))
               .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(ConsistencyLevel.SESSION)
               .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();
        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            queryCompatibilityMode,
            userAgentContainer,
            globalEndpointManager,
            httpClient,
            null);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = new DocumentServiceRequestContext();


        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                                              .instanceOf(CosmosException.class)
                                              .causeInstanceOf(SocketException.class)
                                              .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                                              .statusCode(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE)
                                              .subStatusCode(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE)
                                              .build());
    }

    @Test(groups = "unit", dataProvider = "sessionTokenConfigProvider")
    public void applySessionToken(
        ConsistencyLevel defaultConsistency,
        ConsistencyLevel requestConsistency,
        OperationType operationType,
        ResourceType resourceType,
        boolean sessionTokenFromUser,
        SessionTokenType finalSessionTokenType) throws Exception {

        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        String userControlledSessionToken = "1#99";
        ApiType apiType = ApiType.SQL;
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManager = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);

        Mockito.doReturn(new URI("https://localhost"))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
            .when(httpClient).send(any(HttpRequest.class), any(Duration.class));

        GatewayServiceConfigurationReader gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        Mockito.doReturn(defaultConsistency)
            .when(gatewayServiceConfigurationReader).getDefaultConsistencyLevel();

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            defaultConsistency,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            apiType);
        storeModel.setGatewayServiceConfigurationReader(gatewayServiceConfigurationReader);

        httpClient = ReflectionUtils.getHttpClient(storeModel);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            operationType,
            "/fakeResourceFullName",
            resourceType);
        if (sessionTokenFromUser) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, userControlledSessionToken);
        }
        if (requestConsistency != null) {
            dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, requestConsistency.toString());
        }

        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
            .instanceOf(CosmosException.class)
            .causeInstanceOf(ReadTimeoutException.class)
            .statusCode(HttpConstants.StatusCodes.REQUEST_TIMEOUT).build());

        if (finalSessionTokenType == SessionTokenType.USER) {
            // Session token is passed only for read request, unless its batch operation, or its multi master create
            if(!dsr.isReadOnlyRequest() && dsr.getOperationType() != OperationType.Batch){
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
            } else {
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(userControlledSessionToken);
            }
        } else if(finalSessionTokenType == SessionTokenType.SDK) {
            // Session token is passed only for read request, unless its batch operation, or its multi master create
            if(!dsr.isReadOnlyRequest() && dsr.getOperationType() != OperationType.Batch){
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
            } else {
                assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(sdkGlobalSessionToken);
            }
        } else {
            assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
        }
    }

    @Test(groups = "unit")
    public void validateApiType() throws Exception {
        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        ApiType apiType = ApiType.SQL;
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker globalPartitionEndpointManager = Mockito.mock(GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker.class);

        Mockito.doReturn(new URI("https://localhost"))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> httpClientRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            QueryCompatibilityMode.Default,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            apiType);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Query,
            "/fakeResourceFullName",
            ResourceType.Document);

        try {
            storeModel.performRequest(dsr, HttpMethod.POST).block();
            fail("Request should fail");
        } catch (Exception e) {
            //no-op
        }

        Mockito.verify(httpClient).send(httpClientRequestCaptor.capture(), any());
        HttpRequest httpRequest = httpClientRequestCaptor.getValue();
        HttpHeaders headers = ReflectionUtils.getHttpHeaders(httpRequest);
        assertThat(headers.toMap().get(HttpConstants.HttpHeaders.API_TYPE)).isEqualTo(apiType.toString());
    }

    public void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                FailureValidator validator) {
        validateFailure(observable, validator, TIMEOUT);
    }

    public static void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                       FailureValidator validator,
                                       long timeout) {
        TestSubscriber<RxDocumentServiceResponse> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }

    enum SessionTokenType {
        NONE, // no session token applied
        USER, // userControlled session token
        SDK  // SDK maintained session token
    }
}
