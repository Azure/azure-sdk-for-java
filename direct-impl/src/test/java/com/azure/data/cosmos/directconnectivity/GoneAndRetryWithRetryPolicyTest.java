/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and permission notice shall be included in all
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

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.BadRequestException;
import com.azure.data.cosmos.internal.IRetryPolicy;
import com.azure.data.cosmos.internal.InvalidPartitionException;
import com.azure.data.cosmos.internal.PartitionIsMigratingException;
import com.azure.data.cosmos.internal.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import org.testng.annotations.Test;
import rx.Single;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test file will cover various exception on GoneAndRetryWithRetryPolicy.
 *
 */
public class GoneAndRetryWithRetryPolicyTest {
    protected static final int TIMEOUT = 60000;

    /**
     * Retry with GoneException , retried 4 times and verified the returned
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithGoneException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Single<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new GoneException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(1);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(0);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(2);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(1);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(3);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(2);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(4);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(4);

    }

    /**
     * Retry with PartitionIsMigratingException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithPartitionIsMigratingException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Single<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionIsMigratingException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.forceCollectionRoutingMapRefresh).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
    }

    /**
     * Retry with InvalidPartitionException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithInvalidPartitionException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Single<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new InvalidPartitionException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.requestContext.quorumSelectedLSN).isEqualTo(-1);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        assertThat(request.requestContext.globalCommittedSelectedLSN).isEqualTo(-1);
        assertThat(shouldRetryResult.policyArg.getValue0()).isFalse();

        goneAndRetryWithRetryPolicy.shouldRetry(new InvalidPartitionException());
        // It will retry max till 3 attempts
        shouldRetryResult = goneAndRetryWithRetryPolicy.shouldRetry(new InvalidPartitionException()).toBlocking()
                .value();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        CosmosClientException clientException = (CosmosClientException) shouldRetryResult.exception;
        assertThat(clientException.statusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);

    }

    /**
     * Retry with PartitionKeyRangeIsSplittingException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithPartitionKeyRangeIsSplittingException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Single<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionKeyRangeIsSplittingException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.forcePartitionKeyRangeRefresh).isTrue();
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        assertThat(request.requestContext.quorumSelectedLSN).isEqualTo(-1);
        assertThat(shouldRetryResult.policyArg.getValue0()).isFalse();

    }

    /**
     * No retry on bad request exception
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithGenericException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Single<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new BadRequestException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.toBlocking().value();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }

}
