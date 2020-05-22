// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

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
        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new GoneException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(1);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(0);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(2);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(1);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(3);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(2);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(new GoneException());
        shouldRetryResult = singleShouldRetry.block();
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
        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionIsMigratingException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
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
        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new InvalidPartitionException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.requestContext.quorumSelectedLSN).isEqualTo(-1);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        assertThat(request.requestContext.globalCommittedSelectedLSN).isEqualTo(-1);
        assertThat(shouldRetryResult.policyArg.getValue0()).isFalse();

        goneAndRetryWithRetryPolicy.shouldRetry(new InvalidPartitionException());
        // It will retry max till 3 attempts
        shouldRetryResult = goneAndRetryWithRetryPolicy.shouldRetry(new InvalidPartitionException()).block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
        CosmosException clientException = (CosmosException) shouldRetryResult.exception;
        assertThat(clientException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);

    }

    /**
     * Retry with PartitionKeyRangeIsSplittingException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithPartitionKeyRangeIsSplittingException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionKeyRangeIsSplittingException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
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
        Mono<IRetryPolicy.ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new BadRequestException());
        IRetryPolicy.ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }

}
