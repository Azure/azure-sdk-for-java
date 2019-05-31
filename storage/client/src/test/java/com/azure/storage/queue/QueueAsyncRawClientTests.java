package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueueAsyncRawClientTests extends QueueClientTestsBase {
    private QueueAsyncRawClient client;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(connectionString -> QueueAsyncClient.builder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build()
                .getRawClient());
        } else {
            client = clientSetup(connectionString -> QueueAsyncClient.builder()
                .connectionString(connectionString)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .build()
                .getRawClient());
        }
    }

    @Override
    protected void afterTest() {
        try {
            client.delete(Context.NONE);
        } catch (StorageErrorException ex) {
            // Already delete, that's what we wanted anyways.
        }
    }

    @Override
    public void createQueue() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();
    }

    @Override
    public void createQueueAlreadyExists() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        // There is another possible response code of 409 when the queue metadata doesn't match the already created one
        // but we don't have a create method that takes in metadata.
    }

    @Override
    public void deleteQueue() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.delete(Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();
    }

    @Override
    public void deleteQueueDoesNotExist() {
        StepVerifier.create(client.delete(Context.NONE))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof StorageErrorException);
                StorageErrorException storageErrorException = (StorageErrorException) throwable;
                assertEquals(404, storageErrorException.response().statusCode());
            });
    }

    @Override
    public void getProperties() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getProperties(Context.NONE))
            .assertNext(response -> {
                assertEquals(200, response.statusCode());
                assertTrue(response.value().metadata().isEmpty());
            })
            .verifyComplete();
    }

    @Override
    public void setMetadata() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        Map<String, String> metadata = new HashMap<>();
        StepVerifier.create(client.setMetadata(metadata, Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getProperties(Context.NONE))
            .assertNext(response -> {
                assertEquals(200, response.statusCode());
                assertTrue(response.value().metadata().isEmpty());
            })
            .verifyComplete();

        metadata.put("sample1", "sample1");
        metadata.put("sample2", "sample2");
        StepVerifier.create(client.setMetadata(metadata, Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getProperties(Context.NONE))
            .assertNext(response -> {
                assertEquals(200, response.statusCode());
                Map<String, String> responseMetadata = response.value().metadata();
                assertEquals(2, responseMetadata.size());
                assertEquals("sample1", responseMetadata.get("sample1"));
                assertEquals("sample2", responseMetadata.get("sample2"));
            })
            .verifyComplete();
    }

    @Override
    public void getAccessPolicy() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy(Context.NONE))
            .verifyComplete();
    }

    @Override
    public void setAccessPolicy() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        StepVerifier.create(client.setAccessPolicy(permissions, Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy(Context.NONE))
            .verifyComplete();

        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(1900, 1, 1, 0, 0), ZoneOffset.UTC);
        OffsetDateTime expiry = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC);
        AccessPolicy policy = new AccessPolicy()
            .start(start)
            .expiry(expiry)
            .permission("raup");
        SignedIdentifier twentiethCenturyRAUP = new SignedIdentifier()
            .id("myAccessPolicy")
            .accessPolicy(policy);

        // Limit on the policy ID is 64 characters, add a test for that.
        permissions.add(twentiethCenturyRAUP);
        StepVerifier.create(client.setAccessPolicy(permissions, Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy(Context.NONE))
            .assertNext(response -> {
                assertEquals(twentiethCenturyRAUP.id(), response.id());
                AccessPolicy responsePolicy = response.accessPolicy();
                assertEquals(policy.permission(), responsePolicy.permission());
                assertEquals(policy.start(), responsePolicy.start());
                assertEquals(policy.expiry(), responsePolicy.expiry());
            })
            .verifyComplete();
    }
}
