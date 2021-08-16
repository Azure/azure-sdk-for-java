// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ThroughputRequestThrottlerTests {

    @Test(groups = "unit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void processRequest() {
        double requestChargePerRequest = 2.0;
        double scheduledThroughput = 1.0;
        double availableThroughput = scheduledThroughput;

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(OperationType.Read).when(requestMock).getOperationType();
        requestMock.requestContext = new DocumentServiceRequestContext();

        StoreResponse responseMock = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(requestChargePerRequest).when(responseMock).getRequestCharge();

        ThroughputRequestThrottler requestThrottler = new ThroughputRequestThrottler(scheduledThroughput, StringUtils.EMPTY);

        // Request1: pass through
        TestPublisher<StoreResponse> requestPublisher1 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher1.mono()))
            .then(() -> requestPublisher1.emit(responseMock))
            .expectNext(responseMock)
            .verifyComplete();

        availableThroughput -= requestChargePerRequest;
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // Request2: will get throttled since there is no available throughput
        requestMock.requestContext.throughputControlCycleId = StringUtils.EMPTY;
        TestPublisher requestPublisher2 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher2.mono()))
            .verifyError(RequestRateTooLargeException.class);

        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // reset the throughput
        requestThrottler.renewThroughputUsageCycle(scheduledThroughput);
        availableThroughput = Math.min(availableThroughput + scheduledThroughput, scheduledThroughput);
        assertThat(requestThrottler.getAvailableThroughput()).isEqualTo(availableThroughput);

        // Request 3: will get throttled since there is no available throughput
        requestMock.requestContext.throughputControlCycleId = StringUtils.EMPTY;
        TestPublisher requestPublisher3 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher3.mono()))
            .verifyErrorSatisfies((t) -> {
                assertThat(t).isInstanceOf(RequestRateTooLargeException.class);
                RequestRateTooLargeException throttlingException = (RequestRateTooLargeException)t;
                assertThat(throttlingException.getSubStatusCode())
                    .isEqualTo(HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_REQUEST_RATE_TOO_LARGE);
            });
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // Request 4: will also get throttled since there is no available throughput but will have
        // different sub status code indicating that this is a bulk request
        RxDocumentServiceRequest bulkRequestMock = Mockito.mock(RxDocumentServiceRequest.class);
        HashMap<String, String> mockHeaders = new HashMap<>();
        mockHeaders.put(HttpConstants.HttpHeaders.IS_BATCH_ATOMIC, "FALSE");
        Mockito.doReturn(OperationType.Batch).when(bulkRequestMock).getOperationType();
        Mockito.doReturn(ResourceType.Document).when(bulkRequestMock).getResourceType();
        Mockito.doReturn(mockHeaders).when(bulkRequestMock).getHeaders();
        bulkRequestMock.requestContext = new DocumentServiceRequestContext();

        bulkRequestMock.requestContext.throughputControlCycleId = StringUtils.EMPTY;
        TestPublisher requestPublisher4 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(bulkRequestMock, requestPublisher4.mono()))
                    .verifyErrorSatisfies((t) -> {
                        assertThat(t).isInstanceOf(RequestRateTooLargeException.class);
                        RequestRateTooLargeException throttlingException = (RequestRateTooLargeException)t;
                        assertThat(throttlingException.getSubStatusCode())
                            .isEqualTo(HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_BULK_REQUEST_RATE_TOO_LARGE);
                    });

        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        // reset the throughput usage
        requestThrottler.renewThroughputUsageCycle(scheduledThroughput);
        availableThroughput = Math.min(availableThroughput + scheduledThroughput, scheduledThroughput);
        assertThat(requestThrottler.getAvailableThroughput()).isEqualTo(availableThroughput);

        // Request 5: will pass the request, and record the charge from exception
        requestMock.requestContext.throughputControlCycleId = StringUtils.EMPTY;
        NotFoundException notFoundException = Mockito.mock(NotFoundException.class);
        Mockito.doReturn(requestChargePerRequest).when(notFoundException).getRequestCharge();
        TestPublisher requestPublisher5 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher5.mono()))
            .then(() -> requestPublisher5.error(notFoundException))
            .verifyError(NotFoundException.class);

        availableThroughput -= requestChargePerRequest;
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);
    }

    @Test(groups = "unit")
    public void responseOutOfCycle() {
        double requestChargePerRequest = 2.0;
        double scheduledThroughput = 1.0;
        double availableThroughput = scheduledThroughput;
        OperationType operationType = OperationType.Read;

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(operationType).when(requestMock).getOperationType();
        requestMock.requestContext = new DocumentServiceRequestContext();

        StoreResponse responseMock = Mockito.mock(StoreResponse.class);
        Mockito.doReturn(requestChargePerRequest).when(responseMock).getRequestCharge();

        ThroughputRequestThrottler requestThrottler = new ThroughputRequestThrottler(scheduledThroughput, StringUtils.EMPTY);

        // Request1: pass through
        TestPublisher<StoreResponse> requestPublisher1 = TestPublisher.create();
        StepVerifier.create(requestThrottler.processRequest(requestMock, requestPublisher1.mono()))
            .then(() -> {
                requestMock.requestContext.throughputControlCycleId = UUID.randomUUID().toString();
                requestPublisher1.emit(responseMock);
            })
            .expectNext(responseMock)
            .verifyComplete();

        // verify no throughput will be deducted from available throughput because the response came back during a different throughput cycle
        this.assertRequestThrottlerState(requestThrottler, availableThroughput, scheduledThroughput);

        ConcurrentHashMap<OperationType, ThroughputControlTrackingUnit> trackingUnitDictionary =
            ReflectionUtils.getThroughputControlTrackingDictionary(requestThrottler);
        assertThat(trackingUnitDictionary).isNotNull();
        assertThat(trackingUnitDictionary.size()).isEqualTo(1);
        ThroughputControlTrackingUnit readOperationTrackingUnit = trackingUnitDictionary.get(operationType);
        assertThat(readOperationTrackingUnit).isNotNull();
        assertThat(readOperationTrackingUnit.getRejectedRequests()).isEqualTo(0);
        assertThat(readOperationTrackingUnit.getPassedRequests()).isEqualTo(1);
        assertThat(readOperationTrackingUnit.getSuccessRuUsage()).isEqualTo(requestChargePerRequest);
        assertThat(readOperationTrackingUnit.getSuccessResponse()).isEqualTo(1);
        assertThat(readOperationTrackingUnit.getFailedResponse()).isEqualTo(0);
        assertThat(readOperationTrackingUnit.getOutOfCycleResponse()).isEqualTo(1);
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
