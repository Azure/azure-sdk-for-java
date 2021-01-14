// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.ModelBridgeInternal;
import io.netty.handler.timeout.ReadTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.ClientRetryPolicyTest.validateSuccess;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class RenameCollectionAwareClientRetryPolicyTest {

    private final static int TIMEOUT = 10000;

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void onBeforeSendRequestNotInvoked() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));

        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(mockDiagnosticsClientContext(), endpointManager, ConnectionPolicy.getDefaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        Mono<ShouldRetryResult> shouldRetry =
                renameCollectionAwareClientRetryPolicy.shouldRetry(exception);
        validateSuccess(shouldRetry, ShouldRetryValidator.builder()
                .withException(exception)
                .shouldRetry(false)
                .build());

        Mockito.verifyZeroInteractions(endpointManager);
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void shouldRetryWithNotFoundStatusCode() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null),Mockito.eq(false));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(mockDiagnosticsClientContext(), endpointManager, ConnectionPolicy.getDefaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        NotFoundException notFoundException = new NotFoundException();

        Mono<ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
                .shouldRetry(notFoundException);
        validateSuccess(singleShouldRetry, ShouldRetryValidator.builder()
                .withException(notFoundException)
                .shouldRetry(false)
                .build());
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void shouldRetryWithNotFoundStatusCodeAndReadSessionNotAvailableSubStatusCode() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(mockDiagnosticsClientContext(), endpointManager, ConnectionPolicy.getDefaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.resolvedCollectionRid = "rid_0";
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        NotFoundException notFoundException = new NotFoundException();
        notFoundException.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS,
                Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));

        DocumentCollection documentCollection = new DocumentCollection();
        ModelBridgeInternal.setResourceId(documentCollection, "rid_1");

        Mockito.when(rxClientCollectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request)).thenReturn(Mono.just(new Utils.ValueHolder<>(documentCollection)));

        Mono<ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
                .shouldRetry(notFoundException);
        validateSuccess(singleShouldRetry, ShouldRetryValidator.builder()
                .nullException()
                .shouldRetry(true)
                .build());
    }

    /**
     * No retry on bad request exception
     */
    @Test(groups = "unit", timeOut = TIMEOUT)
    public void shouldRetryWithGenericException() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null), Mockito.eq(false));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(mockDiagnosticsClientContext(), endpointManager, ConnectionPolicy.getDefaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        Mono<ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
                .shouldRetry(new BadRequestException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }
}
