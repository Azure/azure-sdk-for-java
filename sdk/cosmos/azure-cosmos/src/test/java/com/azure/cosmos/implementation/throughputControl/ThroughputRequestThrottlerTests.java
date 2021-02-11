// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static org.assertj.core.api.Assertions.assertThat;

public class ThroughputRequestThrottlerTests {

    @Test(groups = "unit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void processRequest() {
        double requestChargePerRequest = 2.0;
        double scheduledThroughput = 1.0;
        double availableThroughput = scheduledThroughput;

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        StoreResponse responseMock = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(requestChargePerRequest).when(responseMock).getRequestCharge();

        ThroughputRequestThrottler requestThrottler = new ThroughputRequestThrottler(scheduledThroughput);

        // Request1: pass through
        TestPublisher<StoreResponse> requestPublisher1 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher1.mono()))
            .then(() -> requestPublisher1.emit(responseMock))
            .expectNext(responseMock)
            .verifyComplete();

        availableThroughput -= requestChargePerRequest;
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // Request2: will get throttled since there is no available throughput
        TestPublisher requestPublisher2 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher2.mono()))
            .verifyError(RequestRateTooLargeException.class);

        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // reset the throughput
        requestThrottler.renewThroughputUsageCycle(scheduledThroughput);
        availableThroughput = Math.min(availableThroughput + scheduledThroughput, scheduledThroughput);
        assertThat(requestThrottler.getAvailableThroughput()).isEqualTo(availableThroughput);

        // Request 3: will get throttled since there is no available throughput
        TestPublisher requestPublisher3 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher3.mono()))
            .verifyError(RequestRateTooLargeException.class);

        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // reset the throughput usage
        requestThrottler.renewThroughputUsageCycle(scheduledThroughput);
        availableThroughput = Math.min(availableThroughput + scheduledThroughput, scheduledThroughput);
        assertThat(requestThrottler.getAvailableThroughput()).isEqualTo(availableThroughput);

        // Request 4: will pass the request, and record the charge from exception
        NotFoundException notFoundException = Mockito.mock(NotFoundException.class);
        Mockito.doReturn(requestChargePerRequest).when(notFoundException).getRequestCharge();
        TestPublisher requestPublisher4 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher4.mono()))
            .then(() -> requestPublisher4.error(notFoundException))
            .verifyError(NotFoundException.class);

        availableThroughput -= requestChargePerRequest;
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);
    }

    private void assertRequestThrottlerState(
        ThroughputRequestThrottler requestThrottler,
        double expectedAvailableThroughput,
        double expectedScheduledThroughput) {

        assertThat(requestThrottler).isNotNull();
        assertThat(requestThrottler.getScheduledThroughput()).isEqualTo(expectedScheduledThroughput);
        assertThat(requestThrottler.getAvailableThroughput()).isEqualTo(expectedAvailableThroughput);
    }
}
