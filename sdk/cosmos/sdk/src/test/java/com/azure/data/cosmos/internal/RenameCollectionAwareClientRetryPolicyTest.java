// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.caches.RxClientCollectionCache;
import io.netty.handler.timeout.ReadTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static com.azure.data.cosmos.internal.ClientRetryPolicyTest.validateSuccess;
import static org.assertj.core.api.Assertions.assertThat;

public class RenameCollectionAwareClientRetryPolicyTest {

    private final static int TIMEOUT = 10000;

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void onBeforeSendRequestNotInvoked() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null));

        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(endpointManager, ConnectionPolicy.defaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());

        Exception exception = ReadTimeoutException.INSTANCE;

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        Mono<IRetryPolicy.ShouldRetryResult> shouldRetry =
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
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(endpointManager, ConnectionPolicy.defaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        NotFoundException notFoundException = new NotFoundException();

        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
                .shouldRetry(notFoundException);
        validateSuccess(singleShouldRetry, ShouldRetryValidator.builder()
                .withException(notFoundException)
                .shouldRetry(false)
                .build());
    }

    @Test(groups = "unit", timeOut = TIMEOUT)
    public void shouldRetryWithNotFoundStatusCodeAndReadSessionNotAvailableSubStatusCode() {
        GlobalEndpointManager endpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(endpointManager, ConnectionPolicy.defaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        request.requestContext.resolvedCollectionRid = "rid_0";
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        NotFoundException notFoundException = new NotFoundException();
        notFoundException.responseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS,
                Integer.toString(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE));

        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.resourceId("rid_1");

        Mockito.when(rxClientCollectionCache.resolveCollectionAsync(request)).thenReturn(Mono.just(documentCollection));

        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
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
        Mockito.doReturn(Mono.empty()).when(endpointManager).refreshLocationAsync(Mockito.eq(null));
        IRetryPolicyFactory retryPolicyFactory = new RetryPolicy(endpointManager, ConnectionPolicy.defaultPolicy());
        RxClientCollectionCache rxClientCollectionCache = Mockito.mock(RxClientCollectionCache.class);

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        RenameCollectionAwareClientRetryPolicy renameCollectionAwareClientRetryPolicy = new RenameCollectionAwareClientRetryPolicy(sessionContainer
                , rxClientCollectionCache
                , retryPolicyFactory.getRequestPolicy());
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Create, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        renameCollectionAwareClientRetryPolicy.onBeforeSendRequest(request);

        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = renameCollectionAwareClientRetryPolicy
                .shouldRetry(new BadRequestException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }
}
