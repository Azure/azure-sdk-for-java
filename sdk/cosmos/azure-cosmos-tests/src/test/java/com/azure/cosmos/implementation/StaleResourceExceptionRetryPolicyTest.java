// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class StaleResourceExceptionRetryPolicyTest {
    @DataProvider(name = "exceptionProvider")
    public Object[][] exceptionProvider() {
        return new Object[][] {
            //status code, subStatusCode, expectRetry
            { HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE, true },
            { HttpConstants.StatusCodes.BADREQUEST, HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS, true },
            { HttpConstants.StatusCodes.GONE, HttpConstants.SubStatusCodes.CLOSED_CLIENT, false }
        };
    }

    @Test(groups = "unit", dataProvider = "exceptionProvider")
    public void staledException(int statusCode, int subStatusCode, boolean expectRetry) {
        String testCollectionLink = "/dbs/test/colls/staledExceptionTest";
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setId("staledExceptionTest");
        documentCollection.setResourceId("staledExceptionTestRid");

        RxCollectionCache rxCollectionCache = Mockito.mock(RxCollectionCache.class);
        Mockito
            .when(rxCollectionCache.resolveByNameAsync(Mockito.any(), Mockito.any(), Mockito.isNull()))
            .thenReturn(Mono.just(documentCollection));
        doNothing().when(rxCollectionCache).refresh(Mockito.any(), Mockito.any(), Mockito.isNull());

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            rxCollectionCache,
            null,
            testCollectionLink,
            null,
            null,
            sessionContainer,
            TestUtils.mockDiagnosticsClientContext()
        );

        CosmosException exception = BridgeInternal.createCosmosException(statusCode);
        BridgeInternal.setSubStatusCode(exception, subStatusCode);
        ShouldRetryResult shouldRetryResult = staleResourceRetryPolicy.shouldRetry(exception).block();
        assertThat(shouldRetryResult.shouldRetry).isEqualTo(expectRetry);

        shouldRetryResult = staleResourceRetryPolicy.shouldRetry(exception).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }

    @Test(groups = "unit")
    public void suppressRetryForExternalCollectionRid() {
        String testCollectionLink = "/dbs/test/colls/staledExceptionTest";
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setId("staledExceptionTest");
        documentCollection.setResourceId("staledExceptionTestRid");

        RxCollectionCache rxCollectionCache = Mockito.mock(RxCollectionCache.class);
        Mockito
            .when(rxCollectionCache.resolveByNameAsync(Mockito.any(), Mockito.any(), Mockito.isNull()))
            .thenReturn(Mono.just(documentCollection));
        doNothing().when(rxCollectionCache).refresh(Mockito.any(), Mockito.any(), Mockito.isNull());

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(HttpConstants.HttpHeaders.INTENDED_COLLECTION_RID_HEADER, "staledExceptionTestRid");

        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            rxCollectionCache,
            null,
            testCollectionLink,
            null,
            customHeaders,
            sessionContainer,
            TestUtils.mockDiagnosticsClientContext()
        );

        InvalidPartitionException invalidPartitionException = new InvalidPartitionException();
        ShouldRetryResult shouldRetryResult = staleResourceRetryPolicy.shouldRetry(invalidPartitionException).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }

    @Test(groups = "unit")
    public void cleanSessionToken() {
        String testCollectionLink = "/dbs/test/colls/staledExceptionTest";
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setId("staledExceptionTest");
        documentCollection.setResourceId("staledExceptionTestRid");

        DocumentCollection documentCollectionAfterRefresh = new DocumentCollection();
        documentCollectionAfterRefresh.setId(documentCollection.getId());
        documentCollectionAfterRefresh.setResourceId(documentCollection.getResourceId() + "refreshed");

        RxCollectionCache rxCollectionCache = Mockito.mock(RxCollectionCache.class);
        Mockito
            .when(rxCollectionCache.resolveByNameAsync(Mockito.any(), Mockito.any(), Mockito.isNull()))
            .thenReturn(Mono.just(documentCollection))
            .thenReturn(Mono.just(documentCollectionAfterRefresh));

        doNothing().when(rxCollectionCache).refresh(Mockito.any(), Mockito.any(), Mockito.isNull());

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        doNothing().when(sessionContainer).clearTokenByResourceId(documentCollection.getResourceId());

        StaleResourceRetryPolicy staleResourceRetryPolicy = new StaleResourceRetryPolicy(
            rxCollectionCache,
            null,
            testCollectionLink,
            null,
            null,
            sessionContainer,
            TestUtils.mockDiagnosticsClientContext()
        );

        InvalidPartitionException invalidPartitionException = new InvalidPartitionException();
        ShouldRetryResult shouldRetryResult = staleResourceRetryPolicy.shouldRetry(invalidPartitionException).block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        verify(rxCollectionCache, Mockito.times(1)).refresh(Mockito.any(), Mockito.any(), Mockito.isNull());
        verify(sessionContainer, Mockito.times(1)).clearTokenByResourceId(documentCollection.getResourceId());
    }
}
