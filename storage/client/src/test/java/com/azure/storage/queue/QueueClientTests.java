package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
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

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(connectionString -> QueueClient.builder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            client = clientSetup(connectionString -> QueueClient.builder()
                .connectionString(connectionString)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .build());
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
    public void createQueueAlreadyExists() {
        try {
            client.create();
            client.create();
        } catch (StorageErrorException ex) {
            fail();
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
    public void setMetadata() {
        try {
            client.create();

            Map<String, String> metadata = new HashMap<>();
            client.setMetadata(metadata);

            QueueProperties queueProperties = client.getProperties();
            assertEquals(0, queueProperties.metadata().size());

            metadata.put("sample1", "sample1");
            metadata.put("sample2", "sample2");
            client.setMetadata(metadata);

            queueProperties = client.getProperties();
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
    public void setAccessPolicy() {
        try {
            client.create();

            List<SignedIdentifier> permissions = new ArrayList<>();
            client.setAccessPolicy(permissions);

            List<SignedIdentifier> responsePermissions = client.getAccessPolicy();
            assertEquals(0, responsePermissions.size());

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

            responsePermissions = client.getAccessPolicy();
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
}
