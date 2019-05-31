// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class QueueClientTests extends QueueClientTestsBase {
    private QueueClient client;
    private QueueClientBuilder builder;

    @Override
    protected void beforeTest() {
        builder = clientSetup((connectionString, endpoint) ->
            QueueClient.builder()
                .endpoint(endpoint)
                .connectionString(connectionString)
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        client = buildClient(false);
    }

    private QueueClient buildClient(boolean includeMetadataHeader) {
        if (includeMetadataHeader) {
            HttpHeaders metadataHeaders = new HttpHeaders();
            metadataHeaders.put("x-ms-meta-sample1", "sample1")
                .put("x-ms-meta-sample2", "sample2");
            builder.addPolicy(new AddHeadersPolicy(metadataHeaders));
        }

        if (interceptorManager.isPlaybackMode()) {
            return client = builder.httpClient(interceptorManager.getPlaybackClient())
                .build();
        } else {
            return client = builder.httpClient(HttpClient.createDefault().wiretap(true))
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .build();
        }
    }

    @Override
    protected void afterTest() {
        try {
            client.delete();
        } catch (StorageErrorException ex) {
            // Already delete, that's what we wanted anyways.
        }
    }

    @Override
    public void createQueue() {
        try {
            client.create();
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void createQueueAlreadyExistsSameMetadata() {
        try {
            client.create();
            client.create();
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void createQueueAlreadyExistsDifferentMetadata() {
        try {
            client.create();

            QueueClient metadataClient = buildClient(true);
            metadataClient.create();

            fail();
        } catch (StorageErrorException ex) {
            assertEquals(409, ex.response().statusCode());
        }
    }

    @Override
    public void deleteQueue() {
        try {
            client.create();
            client.delete();
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void deleteQueueDoesNotExist() {
        try {
            client.delete();
            fail();
        } catch (StorageErrorException ex) {
            // Expected to throw.
        }
    }

    @Override
    public void getProperties() {
        try {
            client.create();
            QueueProperties queueProperties = client.getProperties();
            assertEquals(0, queueProperties.metadata().size());
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void setEmptyMetadata() {
        try {
            client.create();

            Map<String, String> metadata = new HashMap<>();
            client.setMetadata(metadata);

            QueueProperties queueProperties = client.getProperties();
            assertEquals(0, queueProperties.metadata().size());
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void setFilledMetadata() {
        try {
            client.create();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("sample1", "sample1");
            metadata.put("sample2", "sample2");
            client.setMetadata(metadata);

            QueueProperties queueProperties = client.getProperties();
            assertEquals(2, queueProperties.metadata().size());
            assertEquals("sample1", queueProperties.metadata().get("sample1"));
            assertEquals("sample2", queueProperties.metadata().get("sample2"));

        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void getAccessPolicy() {
        try {
            client.create();
            List<SignedIdentifier> permissions = client.getAccessPolicy();
            assertEquals(0, permissions.size());
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void setEmptyAccessPolicy() {
        try {
            client.create();

            List<SignedIdentifier> permissions = new ArrayList<>();
            client.setAccessPolicy(permissions);

            List<SignedIdentifier> responsePermissions = client.getAccessPolicy();
            assertEquals(0, responsePermissions.size());
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void setFilledAccessPolicy() {
        try {
            client.create();

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
            client.setAccessPolicy(permissions);

            List<SignedIdentifier> responsePermissions = client.getAccessPolicy();
            assertEquals(1, responsePermissions.size());

            SignedIdentifier responsePermission = responsePermissions.get(0);
            assertEquals(twentiethCenturyRAUP.id(), responsePermission.id());
            AccessPolicy responsePolicy = responsePermission.accessPolicy();
            assertEquals(policy.permission(), responsePolicy.permission());
            assertEquals(policy.start(), responsePolicy.start());
            assertEquals(policy.expiry(), responsePolicy.expiry());
        } catch (StorageErrorException ex) {
            fail();
        }
    }

    @Override
    public void setAccessPolicyIdTooLong() {
        try {
            client.create();

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
            client.setAccessPolicy(permissions);

            fail();
        } catch (StorageErrorException ex) {
            assertEquals(409, ex.response().statusCode());
            assertEquals(0, client.getAccessPolicy().size());
        }
    }
}
