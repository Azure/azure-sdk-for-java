// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
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
    private QueueAsyncClientBuilder builder;

    @Override
    protected void beforeTest() {
        builder = clientSetup((connectionString, endpoint) ->
            QueueAsyncClient.builder()
                .endpoint(endpoint)
                .connectionString(connectionString)
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        client = buildClient(false);
    }

    private QueueAsyncRawClient buildClient(boolean includeMetadataHeader) {
        if (includeMetadataHeader) {
            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.put("x-ms-meta-sample1", "sample1")
                .put("x-ms-meta-sample2", "sample2");
            builder.addPolicy(new AddHeadersPolicy(metadataHeaders));
        }

        if (interceptorManager.isPlaybackMode()) {
            return client = builder.httpClient(interceptorManager.getPlaybackClient())
                .build()
                .getRawClient();
        } else {
            return client = builder.httpClient(HttpClient.createDefault().wiretap(true))
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .build()
                .getRawClient();
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
    public void createQueueAlreadyExistsSameMetadata() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();
    }

    @Override
    public void createQueueAlreadyExistsDifferentMetadata() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        QueueAsyncRawClient metadataClient = buildClient(true);

        StepVerifier.create(metadataClient.create(Context.NONE))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof StorageErrorException);
                StorageErrorException storageErrorException = (StorageErrorException) throwable;
                assertEquals(409, storageErrorException.response().statusCode());
            });
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
    public void setEmptyMetadata() {
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
    }

    @Override
    public void setFilledMetadata() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        Map<String, String> metadata = new HashMap<>();
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
    public void setEmptyAccessPolicy() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        StepVerifier.create(client.setAccessPolicy(permissions, Context.NONE))
            .assertNext(response -> assertEquals(204, response.statusCode()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy(Context.NONE))
            .verifyComplete();
    }

    @Override
    public void setFilledAccessPolicy() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(1900, 1, 1, 0, 0), ZoneOffset.UTC);
        OffsetDateTime expiry = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC);
        AccessPolicy policy = new AccessPolicy()
            .start(start)
            .expiry(expiry)
            .permission("raup");
        SignedIdentifier twentiethCenturyRAUP = new SignedIdentifier()
            .id("myAccessPolicy")
            .accessPolicy(policy);

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

    @Override
    public void setAccessPolicyIdTooLong() {
        StepVerifier.create(client.create(Context.NONE))
            .assertNext(response -> assertEquals(201, response.statusCode()))
            .verifyComplete();

        List<SignedIdentifier> permissions = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.of(LocalDateTime.of(1900, 1, 1, 0, 0), ZoneOffset.UTC);
        OffsetDateTime expiry = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC);

        AccessPolicy policy = new AccessPolicy()
            .start(start)
            .expiry(expiry)
            .permission("raup");

        SignedIdentifier longNamedPermission = new SignedIdentifier()
            .id("IDecidedToHaveAVeryLongAccessPolicyNameWhichEndsUpBeingLargerThanAllowedByTheService")
            .accessPolicy(policy);

        permissions.add(longNamedPermission);
        StepVerifier.create(client.setAccessPolicy(permissions, Context.NONE))
            .verifyErrorSatisfies(throwable -> {
                assertTrue(throwable instanceof StorageErrorException);
                StorageErrorException storageErrorException = (StorageErrorException) throwable;
                assertEquals(409, storageErrorException.response().statusCode());
            });

        StepVerifier.create(client.getAccessPolicy(Context.NONE))
            .verifyComplete();
    }
}
