// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueueSasAsyncClientTests extends QueueTestBase {
    private QueueAsyncClient asyncSasClient;
    private SendMessageResult resp;

    @BeforeEach
    public void setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient();
        asyncSasClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(getRandomName(50));
        asyncSasClient.create().block();
        resp = asyncSasClient.sendMessage("test").block();
    }

    private QueueServiceSasSignatureValues generateValues(QueueSasPermission permission) {
        return new QueueServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP);
    }

    @Test
    public void queueSasEnqueueWithPerm() {
        QueueSasPermission permissions
            = new QueueSasPermission().setReadPermission(true).setAddPermission(true).setProcessPermission(true);
        QueueServiceSasSignatureValues sasValues = generateValues(permissions);

        QueueAsyncClient clientPermissions = queueBuilderHelper().endpoint(asyncSasClient.getQueueUrl())
            .queueName(asyncSasClient.getQueueName())
            .sasToken(asyncSasClient.generateSas(sasValues))
            .buildAsyncClient();
        clientPermissions.sendMessage("sastest").block();

        StepVerifier.create(clientPermissions.receiveMessages(2).collectList()).assertNext(messageItemList -> {
            assertEquals("test", messageItemList.get(0).getBody().toString());
            assertEquals("sastest", messageItemList.get(1).getBody().toString());
        }).verifyComplete();

        StepVerifier
            .create(clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing",
                Duration.ofHours(1)))
            .verifyError(QueueStorageException.class);

    }

    @Test
    public void queueSasUpdateWithPerm() {
        QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true);
        QueueServiceSasSignatureValues sasValues = generateValues(permissions);

        QueueAsyncClient clientPermissions = queueBuilderHelper().endpoint(asyncSasClient.getQueueUrl())
            .queueName(asyncSasClient.getQueueName())
            .sasToken(asyncSasClient.generateSas(sasValues))
            .buildAsyncClient();

        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO).block();

        StepVerifier.create(clientPermissions.receiveMessages(1))
            .assertNext(messageItem -> assertEquals("testing", messageItem.getBody().toString()))
            .verifyComplete();

        StepVerifier.create(clientPermissions.delete()).verifyError(QueueStorageException.class);
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    @Test
    public void queueSasEnqueueWithId() {
        QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        OffsetDateTime startTime = testResourceNamer.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS);

        QueueSignedIdentifier identifier = new QueueSignedIdentifier().setId(testResourceNamer.randomUuid())
            .setAccessPolicy(new QueueAccessPolicy().setPermissions(permissions.toString())
                .setExpiresOn(expiryTime)
                .setStartsOn(startTime));
        asyncSasClient.setAccessPolicy(Arrays.asList(identifier)).block();

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfRunningAgainstService(30000);

        QueueServiceSasSignatureValues sasValues = new QueueServiceSasSignatureValues(identifier.getId());

        QueueAsyncClient clientIdentifier = queueBuilderHelper().endpoint(asyncSasClient.getQueueUrl())
            .queueName(asyncSasClient.getQueueName())
            .sasToken(asyncSasClient.generateSas(sasValues))
            .buildAsyncClient();
        clientIdentifier.sendMessage("sastest").block();

        StepVerifier.create(clientIdentifier.receiveMessages(2).collectList()).assertNext(messageItemList -> {
            assertEquals("test", messageItemList.get(0).getBody().toString());
            assertEquals("sastest", messageItemList.get(1).getBody().toString());
        }).verifyComplete();
    }

    @Test
    public void accountSasCreateQueue() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions
            = new AccountSasPermission().setReadPermission(true).setCreatePermission(true).setDeletePermission(true);

        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);

        QueueServiceAsyncClient sc
            = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
                .sasToken(primaryQueueServiceAsyncClient.generateAccountSas(sasValues))
                .buildAsyncClient();

        String queueName = getRandomName(50);
        assertDoesNotThrow(() -> sc.createQueue(queueName).block());
        assertDoesNotThrow(() -> sc.deleteQueue(queueName).block());
    }

    @Test
    public void accountSasListQueues() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setListPermission(true);
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);

        QueueServiceAsyncClient sc
            = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
                .sasToken(primaryQueueServiceAsyncClient.generateAccountSas(sasValues))
                .buildAsyncClient();

        assertDoesNotThrow(() -> sc.listQueues().next().block() != null);
    }
}
