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

package com.microsoft.azure.cosmosdb.rx.internal;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.RetryOptions;
import io.reactivex.netty.client.PoolExhaustedException;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionPoolExhaustedRetryTest {
    private static final int TIMEOUT = 10000;

    @DataProvider(name = "exceptionProvider")
    public Object[][] exceptionProvider() {
        return new Object[][]{
                {Mockito.mock(PoolExhaustedException.class)},
                {new DocumentClientException(-1, Mockito.mock(PoolExhaustedException.class))},
        };
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT, dataProvider = "exceptionProvider")
    public void retryOnConnectionPoolExhausted(Exception exception) throws Exception {
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URL("http://localhost")).when(globalEndpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(globalEndpointManager, false, Mockito.mock(RetryOptions.class));

        clientRetryPolicy.onBeforeSendRequest(Mockito.mock(RxDocumentServiceRequest.class));
        IRetryPolicy.ShouldRetryResult shouldRetryResult = clientRetryPolicy.shouldRetry(exception).toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.backOffTime).isGreaterThanOrEqualTo(ConnectionPoolExhaustedRetry.RETRY_WAIT_TIME);

        Mockito.verify(globalEndpointManager, Mockito.times(1)).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.verify(globalEndpointManager, Mockito.times(1)).CanUseMultipleWriteLocations(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.verifyNoMoreInteractions(globalEndpointManager);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT, dataProvider = "exceptionProvider")
    public void retryOnConnectionPoolExhausted_Exhausted(Exception exception) throws Exception {
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URL("http://localhost")).when(globalEndpointManager).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        ClientRetryPolicy clientRetryPolicy = new ClientRetryPolicy(globalEndpointManager, false, Mockito.mock(RetryOptions.class));

        clientRetryPolicy.onBeforeSendRequest(Mockito.mock(RxDocumentServiceRequest.class));
        for (int i = 0; i < ConnectionPoolExhaustedRetry.MAX_RETRY_COUNT; i++) {
            IRetryPolicy.ShouldRetryResult shouldRetryResult = clientRetryPolicy.shouldRetry(exception).toBlocking().value();
            assertThat(shouldRetryResult.shouldRetry).isTrue();
            assertThat(shouldRetryResult.backOffTime).isGreaterThanOrEqualTo(ConnectionPoolExhaustedRetry.RETRY_WAIT_TIME);
        }

        IRetryPolicy.ShouldRetryResult shouldRetryResult = clientRetryPolicy.shouldRetry(exception).toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.backOffTime).isNull();
        // no interaction with global endpoint manager
        Mockito.verify(globalEndpointManager, Mockito.times(1)).resolveServiceEndpoint(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.verify(globalEndpointManager, Mockito.times(1)).CanUseMultipleWriteLocations(Mockito.any(RxDocumentServiceRequest.class));
        Mockito.verifyNoMoreInteractions(globalEndpointManager);
    }
}
