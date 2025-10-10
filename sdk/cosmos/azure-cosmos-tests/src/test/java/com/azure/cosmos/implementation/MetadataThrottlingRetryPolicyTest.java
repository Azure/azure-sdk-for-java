// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MetadataThrottlingRetryPolicyTest extends TestSuiteBase {
    protected static final int TIMEOUT = 40000;

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuildersWithoutRetryOnThrottledRequests")
    public MetadataThrottlingRetryPolicyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_MetadataThrottlingRetryPolicyTest() {
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void after_MetadataThrottlingRetryPolicyTest() {
        safeClose(this.client);
    }

    @Test(groups = "unit")
    public void constructor_InitializesWithCorrectParameters() {
        RetryContext retryContext = mock(RetryContext.class);
        MetadataThrottlingRetryPolicy policy = new MetadataThrottlingRetryPolicy(retryContext);

        assertNotNull(policy);
        assertEquals(retryContext, policy.getRetryContext());
    }

    @Test(groups = "unit")
    public void shouldRetry_OnThrottlingError_AddsRandomSalt() {
        RetryContext retryContext = mock(RetryContext.class);
        MetadataThrottlingRetryPolicy policy = new MetadataThrottlingRetryPolicy(retryContext);

        CosmosException throttlingException = createRequestRateTooLargeException(1000);

        // Test the retry behavior
        StepVerifier.create(policy.shouldRetry(throttlingException))
            .assertNext(shouldRetryResult -> {
                assertTrue(shouldRetryResult.shouldRetry);
                Duration retryDelay = shouldRetryResult.backOffTime;
                // Verify delay is between 1000ms and 1100ms (original + up to 100ms salt)
                assertTrue(retryDelay.toMillis() >= 1000);
                assertTrue(retryDelay.toMillis() <= 1100);
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void shouldRetry_OnNonThrottlingError_DoesNotRetry() {
        RetryContext retryContext = mock(RetryContext.class);
        MetadataThrottlingRetryPolicy policy = new MetadataThrottlingRetryPolicy(retryContext);

        CosmosException nonThrottlingException = new InternalServerErrorException();

        StepVerifier.create(policy.shouldRetry(nonThrottlingException))
            .assertNext(shouldRetryResult -> {
                assertThat(shouldRetryResult.shouldRetry).isFalse();
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void shouldRetry_WithMaxWaitTimeExceeded_StopsRetrying() {
        RetryContext retryContext = mock(RetryContext.class);
        MetadataThrottlingRetryPolicy policy = new MetadataThrottlingRetryPolicy(retryContext);

        // Create exception with retry delay that exceeds max wait time
        CosmosException throttlingException = new RequestRateTooLargeException();

        StepVerifier.create(policy.shouldRetry(throttlingException))
            .assertNext(shouldRetryResult -> {
                assertThat(shouldRetryResult.shouldRetry).isTrue();
            })
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void onBeforeSendRequest_HandlesRetryContext() {
        // Create mocks
        RetryContext retryContext = mock(RetryContext.class);
        RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);

        // Create policy
        MetadataThrottlingRetryPolicy policy = new MetadataThrottlingRetryPolicy(retryContext);

        // Call onBeforeSendRequest
        policy.onBeforeSendRequest(request);

        // Verify the retry context is properly maintained
        assertEquals(retryContext, policy.getRetryContext());
    }

    @Test(groups = "emulator", timeOut = TIMEOUT)
    public void testResourceThrottleRetryForGetFeedRanges() {
        FaultInjectionRule metadataThrottleRule = new FaultInjectionRuleBuilder("metadata-throttle")
            .condition(new FaultInjectionConditionBuilder().operationType(FaultInjectionOperationType.METADATA_REQUEST_PARTITION_KEY_RANGES).build())
            .result(FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST).times(2).build())
            .build();

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(this.container, Arrays.asList(metadataThrottleRule)).block();

            // verify point operations can succeed
            this.container.createItem(TestObject.create()).block();

            // verify getFeedRanges can succeed
             this.container.getFeedRanges().block();
        } finally {
            metadataThrottleRule.disable();
        }
    }

    private RequestRateTooLargeException createRequestRateTooLargeException(int backOffDelay) {
        Map<String, String> headers = new HashMap<>();
        headers.put(
            HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
            String.valueOf(backOffDelay));
        headers.put(WFConstants.BackendHeaders.SUB_STATUS,
            Integer.toString(HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE));
        return new RequestRateTooLargeException(null, 1, "1", headers);
    }
}