package com.azure.messaging.eventgrid.namespaces;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventgrid.namespaces.models.ReleaseDelay;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class EventGridReceiverAsyncClientTests extends EventGridClientTestBase {


    EventGridReceiverAsyncClient buildReceiverAsyncClient(boolean useManagedIdentity) {
        if (useManagedIdentity) {
            return receiverBuilder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        return receiverBuilder.credential(getKey(EVENTGRID_KEY)).buildAsyncClient();
    }

    EventGridSenderAsyncClient buildSenderAsyncClient(boolean useManagedIdentity) {
        if (useManagedIdentity) {
            return senderBuilder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        return senderBuilder.credential(getKey(EVENTGRID_KEY)).buildAsyncClient();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void receiveBatchOfCloudEvents(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent())
            .then(client.receive(10, Duration.ofSeconds(10)))
            .as(StepVerifier::create)
            .assertNext(receiveResult -> {
                assertNotNull(receiveResult);
                assertFalse(receiveResult.getDetails().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void acknowledgeBatchOfCloudEvents(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent()).then(client.receive(10, Duration.ofSeconds(10)))
            .flatMap(receiveResult -> {
                return client.acknowledge(Arrays.asList(receiveResult.getDetails().get(0).getBrokerProperties().getLockToken()));
            })
            .as(StepVerifier::create)
            .assertNext(receiveResult -> {
                assertNotNull(receiveResult);
                assertTrue(receiveResult.getFailedLockTokens().isEmpty());
                assertFalse(receiveResult.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void releaseBatchOfCloudEvents(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent()).then(client.receive(10, Duration.ofSeconds(10)))
            .flatMap(receiveResult -> {
                return client.release(Arrays.asList(receiveResult.getDetails().get(0).getBrokerProperties().getLockToken()));
            })
            .as(StepVerifier::create)
            .assertNext(result -> {
                assertNotNull(result);
                assertTrue(result.getFailedLockTokens().isEmpty());
                assertFalse(result.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void releaseBatchOfCloudEventsWithDelay(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent()).then(client.receive(10, Duration.ofSeconds(10)))
            .flatMap(receiveResult -> {
                return client.release(Arrays.asList(receiveResult.getDetails().get(0).getBrokerProperties().getLockToken()), ReleaseDelay.TEN_SECONDS);
            })
            .as(StepVerifier::create)
            .assertNext(result -> {
                assertNotNull(result);
                assertTrue(result.getFailedLockTokens().isEmpty());
                assertFalse(result.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();


    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectBatchOfCloudEvents(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent()).then(client.receive(10, Duration.ofSeconds(10)))
            .flatMap(receiveResult -> {
                return client.reject(Arrays.asList(receiveResult.getDetails().get(0).getBrokerProperties().getLockToken()));
            })
            .as(StepVerifier::create)
            .assertNext(result -> {
                assertNotNull(result);
                assertTrue(result.getFailedLockTokens().isEmpty());
                assertFalse(result.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void renewBatchOfCloudEvents(boolean useManagedIdentity) {
        if (interceptorManager.isLiveMode()) {
            assumeTrue(useManagedIdentity);
        } else {
            assumeFalse(useManagedIdentity);
        }

        EventGridReceiverAsyncClient client = buildReceiverAsyncClient(useManagedIdentity);
        EventGridSenderAsyncClient senderClient = buildSenderAsyncClient(useManagedIdentity);

        senderClient.send(getCloudEvent()).then(client.receive(10, Duration.ofSeconds(10)))
            .flatMap(receiveResult -> {
                return client.renewLocks(Arrays.asList(receiveResult.getDetails().get(0).getBrokerProperties().getLockToken()));
            })
            .as(StepVerifier::create)
            .assertNext(result -> {
                assertNotNull(result);
                assertTrue(result.getFailedLockTokens().isEmpty());
                assertFalse(result.getSucceededLockTokens().isEmpty());
            })
            .verifyComplete();
    }
}
