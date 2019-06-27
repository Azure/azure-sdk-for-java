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
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.directconnectivity.WFConstants;
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
