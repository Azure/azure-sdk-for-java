// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.guava25.base.Supplier;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test file will cover various exception on GoneAndRetryWithRetryPolicy.
 *
 */
public class GoneAndRetryWithRetryPolicyTest {
    protected static final int TIMEOUT = 60000;

    @DataProvider(name = "exceptionProvider")
    public static Object[][] exceptionProvider() {
        return new Object[][]{
            // request type, exception type
            { OperationType.Read, ServerBasedGoneException.class },
            { OperationType.Read, ClientBasedGoneException.class },
            { OperationType.Read, PartitionIsMigratingException.class },
            { OperationType.Read, PartitionKeyRangeIsSplittingException.class },
            { OperationType.Create, ServerBasedGoneException.class },
            { OperationType.Create, ClientBasedGoneException.class }
        };
    }

    /**
     * Retry with GoneException for read, retried 4 times and verified the returned
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryReadWithGoneException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new GoneException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
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
     * Retry with GoneException for write which is not yet sent to the wire,
     * retried 4 times and verified the returned
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryNotYetFlushedWriteWithGoneException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);

        Supplier<GoneException> goneExceptionForNotYetFlushedRequestSupplier = () -> {
            GoneException goneExceptionForNotYetFlushedRequest = new GoneException();
            BridgeInternal.setSendingRequestStarted(goneExceptionForNotYetFlushedRequest, false);

            return goneExceptionForNotYetFlushedRequest;
        };

        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
            .shouldRetry(goneExceptionForNotYetFlushedRequestSupplier.get());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(1);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(0);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForNotYetFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(2);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(1);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForNotYetFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(3);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(2);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForNotYetFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(4);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(4);
    }

    /**
     * GoneException for write which is already sent to the wire, should not result in retry,
     * but an address refresh should be triggered
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldNotRetryFlushedWriteWithGoneExceptionButForceAddressRefresh() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            ResourceType.Document);

        Supplier<GoneException> goneExceptionForFlushedRequestSupplier = () -> {
            GoneException goneExceptionForFlushedRequest = new GoneException();
            BridgeInternal.setSendingRequestStarted(goneExceptionForFlushedRequest, true);

            return goneExceptionForFlushedRequest;
        };

        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
            .shouldRetry(goneExceptionForFlushedRequestSupplier.get());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();

        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.policyArg).isNotNull();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.backOffTime).isNull();
    }

    /**
     * GoneException for write which is already sent to the wire but based on receiving
     * an actual response from the Service with 410 Status Code and SubStatusCode 0
     * should result in retry
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryFlushedWriteWithGoneExceptionFromService() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy =
            new GoneAndRetryWithRetryPolicy(request, 30);

        Supplier<GoneException> goneExceptionForFlushedRequestSupplier = () -> {
            GoneException goneExceptionForFlushedRequest = new GoneException();
            BridgeInternal.setSendingRequestStarted(goneExceptionForFlushedRequest, true);
            goneExceptionForFlushedRequest.setIsBasedOn410ResponseFromService();
            return goneExceptionForFlushedRequest;
        };

        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
            .shouldRetry(goneExceptionForFlushedRequestSupplier.get());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(1);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(0);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(2);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(1);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(3);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(2);

        singleShouldRetry = goneAndRetryWithRetryPolicy.shouldRetry(goneExceptionForFlushedRequestSupplier.get());
        shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue3()).isEqualTo(4);
        assertThat(shouldRetryResult.backOffTime.getSeconds()).isEqualTo(4);
    }

    /**
     * RequestTimeoutExceptions should not be retried for read or write - no address cache refresh expected
     * shouldRetryResult. ShouldRetryResult
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldNotRetryRequestTimeoutException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);

        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
            .shouldRetry(new RequestTimeoutException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();

        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.policyArg).isNull();
        assertThat(shouldRetryResult.backOffTime).isNull();

        request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            ResourceType.Document);

        goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        singleShouldRetry = goneAndRetryWithRetryPolicy
            .shouldRetry(new RequestTimeoutException());
        shouldRetryResult = singleShouldRetry.block();

        assertThat(shouldRetryResult.shouldRetry).isFalse();
        assertThat(shouldRetryResult.policyArg).isNull();
        assertThat(shouldRetryResult.backOffTime).isNull();
    }

    /**
     * When gave up retrying, it should return service unavailable exception to up caller for the following exception:
     * GoneException, PartitionIsMigratingException, PartitionKeyRangeIsSplittingException
     */
    @Test(groups = { "unit" }, dataProvider = "exceptionProvider", timeOut = TIMEOUT)
    public void shouldReturnServiceUnavailableException(OperationType operationType, Class exceptionType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            ResourceType.Document);

        CosmosException exception = null;
        if (exceptionType == ServerBasedGoneException.class) {
            GoneException goneException = new GoneException();
            BridgeInternal.setSendingRequestStarted(goneException, true);
            goneException.setIsBasedOn410ResponseFromService();
            exception = goneException;
        }
        if (exceptionType == ClientBasedGoneException.class) {
            exception = new GoneException();
        }
        if (exceptionType == PartitionIsMigratingException.class) {
            exception = new PartitionIsMigratingException();
        }
        if (exceptionType == PartitionKeyRangeIsSplittingException.class) {
            exception = new PartitionKeyRangeIsSplittingException();
        }

        if (exception == null) {
            throw new IllegalArgumentException(exceptionType + " is not supported");
        }

        boolean shouldRetry = true;
        ShouldRetryResult shouldRetryResult = null;

        // at most have 1 retry
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 0);
        while (shouldRetry) {
            shouldRetryResult = goneAndRetryWithRetryPolicy.shouldRetry(exception).block();
            shouldRetry = shouldRetryResult.shouldRetry;
        }
        assertThat(shouldRetryResult.exception).isInstanceOf(ServiceUnavailableException.class);
    }

    /**
     * Retry with PartitionIsMigratingException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithPartitionIsMigratingException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionIsMigratingException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.forceCollectionRoutingMapRefresh).isTrue();
        assertThat(shouldRetryResult.policyArg.getValue0()).isTrue();
    }

    /**
     * Retry with InvalidPartitionException
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void shouldRetryWithInvalidPartitionException() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new InvalidPartitionException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isTrue();
        assertThat(request.requestContext.quorumSelectedLSN).isEqualTo(-1);
        assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
        assertThat(request.requestContext.globalCommittedSelectedLSN).isEqualTo(-1);
        assertThat(shouldRetryResult.policyArg.getValue0()).isFalse();

        goneAndRetryWithRetryPolicy.shouldRetry(new InvalidPartitionException()).block();
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
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new PartitionKeyRangeIsSplittingException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
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
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            ResourceType.Document);
        GoneAndRetryWithRetryPolicy goneAndRetryWithRetryPolicy = new GoneAndRetryWithRetryPolicy(request, 30);
        Mono<ShouldRetryResult> singleShouldRetry = goneAndRetryWithRetryPolicy
                .shouldRetry(new BadRequestException());
        ShouldRetryResult shouldRetryResult = singleShouldRetry.block();
        assertThat(shouldRetryResult.shouldRetry).isFalse();
    }

    class ServerBasedGoneException {}
    class ClientBasedGoneException {}

}
