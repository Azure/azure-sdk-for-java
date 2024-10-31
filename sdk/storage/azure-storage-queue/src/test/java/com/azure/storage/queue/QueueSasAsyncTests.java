// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.test.TestMode;
import com.azure.storage.common.StorageSharedKeyCredential;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueueSasAsyncTests extends QueueTestBase {
    private QueueAsyncClient queueAsyncClient;

    @BeforeEach
    public void setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient();
        queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(getRandomName(60));
    }

    @Test
    public void queueServiceSasCanonicalizedResource() {
        String queueName = queueAsyncClient.getQueueName();

        assertEquals(queueName, new QueueServiceSasSignatureValues().setQueueName(queueName).getQueueName());
    }

    @Test
    public void queueSasEnqueueDequeueWithPermissions() {
        queueAsyncClient.create().block();
        SendMessageResult resp = queueAsyncClient.sendMessage("test").block();

        QueueSasPermission permissions
            = new QueueSasPermission().setReadPermission(true).setAddPermission(true).setProcessPermission(true);
        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sasPermissions = new QueueServiceSasSignatureValues().setPermissions(permissions)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setQueueName(queueAsyncClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueAsyncClient clientPermissions = queueBuilderHelper().endpoint(queueAsyncClient.getQueueUrl())
            .queueName(queueAsyncClient.getQueueName())
            .sasToken(sasPermissions)
            .buildAsyncClient();
        clientPermissions.sendMessage("sastest").block();

        StepVerifier.create(clientPermissions.receiveMessages(2).collectList()).assertNext(messageItemList -> {
            assertEquals("test", messageItemList.get(0).getBody().toString());
            assertEquals("sastest", messageItemList.get(1).getBody().toString());
        }).verifyComplete();

        assertThrows(QueueStorageException.class, () -> {
            assert resp != null;
            clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ofHours(1))
                .block();
        });
    }

    @Test
    public void queueSasUpdateDeleteWithPermissions() {
        queueAsyncClient.create().block();
        SendMessageResult resp = queueAsyncClient.sendMessage("test").block();

        QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sasPermissions = new QueueServiceSasSignatureValues().setPermissions(permissions)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setQueueName(queueAsyncClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueAsyncClient clientPermissions = queueBuilderHelper().endpoint(queueAsyncClient.getQueueUrl())
            .queueName(queueAsyncClient.getQueueName())
            .sasToken(sasPermissions)
            .buildAsyncClient();
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO).block();

        StepVerifier.create(clientPermissions.receiveMessages(1))
            .assertNext(messageItem -> assertEquals("testing", messageItem.getBody().toString()))
            .verifyComplete();

        StepVerifier.create(clientPermissions.delete()).verifyError(QueueStorageException.class);
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    @Test
    public void queueSasEnqueueDequeueWithIdentifier() {
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage("test").block();

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
        queueAsyncClient.setAccessPolicy(Arrays.asList(identifier)).block();

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfRunningAgainstService(30000);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sasIdentifier = new QueueServiceSasSignatureValues().setIdentifier(identifier.getId())
            .setQueueName(queueAsyncClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueAsyncClient clientIdentifier = queueBuilderHelper().endpoint(queueAsyncClient.getQueueUrl())
            .queueName(queueAsyncClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildAsyncClient();
        clientIdentifier.sendMessage("sastest").block();

        StepVerifier.create(clientIdentifier.receiveMessages(2).collectList()).assertNext(messageItemList -> {
            assertEquals("test", messageItemList.get(0).getBody().toString());
            assertEquals("sastest", messageItemList.get(1).getBody().toString());
        }).verifyComplete();
    }

    @Test
    public void accountSasCreateDeleteQueue() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions
            = new AccountSasPermission().setReadPermission(true).setCreatePermission(true).setDeletePermission(true);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        QueueServiceAsyncClient sc
            = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
                .sasToken(sas)
                .buildAsyncClient();
        String queueName = getRandomName(60);

        assertDoesNotThrow(() -> sc.createQueue(queueName).block());
        assertDoesNotThrow(() -> sc.deleteQueue(queueName).block());
    }

    @Test
    public void accountSasListQueues() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setListPermission(true);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        QueueServiceAsyncClient sc
            = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
                .sasToken(sas)
                .buildAsyncClient();

        assertDoesNotThrow(() -> sc.listQueues().next().block() != null);
    }

    @Test
    public void accountSasNetworkOnEndpoint() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setDeletePermission(true);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
            .credential(credential)
            .buildAsyncClient()
            .generateAccountSas(sasValues);

        String queueName = getRandomName(60);

        assertDoesNotThrow(
            () -> getServiceClientBuilder(null, primaryQueueServiceAsyncClient.getQueueServiceUrl() + "?" + sas)
                .buildAsyncClient()
                .createQueue(queueName)
                .block());

        assertDoesNotThrow(() -> getQueueClientBuilder(
            primaryQueueServiceAsyncClient.getQueueServiceUrl() + "/" + queueName + "?" + sas).buildAsyncClient()
                .delete()
                .block());
    }

    /*
    Ensures that we don't break the functionality of the deprecated means of generating an AccountSas.
    Only run in live mode because recordings would frequently get messed up when we update recordings to new sas version
     */
    @Test
    public void accountSasDeprecated() {
        if (getTestMode() != TestMode.LIVE) {
            return;
        }

        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions
            = new AccountSasPermission().setReadPermission(true).setCreatePermission(true).setDeletePermission(true);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sas = new AccountSasSignatureValues().setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .generateSasQueryParameters(credential)
            .encode();

        QueueServiceAsyncClient sc
            = queueServiceBuilderHelper().endpoint(primaryQueueServiceAsyncClient.getQueueServiceUrl())
                .sasToken(sas)
                .buildAsyncClient();
        String queueName = getRandomName(60);

        assertDoesNotThrow(() -> sc.createQueue(queueName).block());
        assertDoesNotThrow(() -> sc.deleteQueue(queueName).block());
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);
        String sas = primaryQueueServiceAsyncClient.generateAccountSas(sasValues);

        queueAsyncClient.create().block();

        assertDoesNotThrow(
            () -> instrument(new QueueClientBuilder().endpoint(queueAsyncClient.getQueueUrl()).sasToken(sas))
                .buildAsyncClient()
                .getProperties()
                .block());

        assertDoesNotThrow(() -> instrument(
            new QueueClientBuilder().endpoint(queueAsyncClient.getQueueUrl()).credential(new AzureSasCredential(sas)))
                .buildAsyncClient()
                .getProperties()
                .block());

        assertDoesNotThrow(
            () -> instrument(new QueueClientBuilder().endpoint(queueAsyncClient.getQueueUrl() + "?" + sas))
                .buildAsyncClient()
                .getProperties()
                .block());

        assertDoesNotThrow(
            () -> instrument(new QueueServiceClientBuilder().endpoint(queueAsyncClient.getQueueUrl()).sasToken(sas))
                .buildAsyncClient()
                .getProperties()
                .block());

        assertDoesNotThrow(() -> instrument(new QueueServiceClientBuilder().endpoint(queueAsyncClient.getQueueUrl())
            .credential(new AzureSasCredential(sas))).buildAsyncClient().getProperties().block());

        assertDoesNotThrow(
            () -> instrument(new QueueServiceClientBuilder().endpoint(queueAsyncClient.getQueueUrl() + "?" + sas))
                .buildAsyncClient()
                .getProperties()
                .block());
    }
}
