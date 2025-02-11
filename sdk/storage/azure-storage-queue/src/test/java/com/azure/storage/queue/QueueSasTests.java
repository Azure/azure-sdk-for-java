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
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueueSasTests extends QueueTestBase {
    private QueueClient queueClient;

    @BeforeEach
    public void setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient();
        queueClient = primaryQueueServiceClient.getQueueClient(getRandomName(60));
    }

    @ParameterizedTest
    @MethodSource("queueSasPermissionParseSupplier")
    public void queueSasPermissionParse(String permString, boolean read, boolean add, boolean update, boolean process) {
        QueueSasPermission perms = QueueSasPermission.parse(permString);

        assertEquals(read, perms.hasReadPermission());
        assertEquals(add, perms.hasAddPermission());
        assertEquals(update, perms.hasUpdatePermission());
        assertEquals(process, perms.hasProcessPermission());
    }

    private static Stream<Arguments> queueSasPermissionParseSupplier() {
        return Stream.of(Arguments.of("r", true, false, false, false), Arguments.of("a", false, true, false, false),
            Arguments.of("u", false, false, true, false), Arguments.of("p", false, false, false, true),
            Arguments.of("raup", true, true, true, true), Arguments.of("apru", true, true, true, true),
            Arguments.of("rap", true, true, false, true), Arguments.of("ur", true, false, true, false));
    }

    @ParameterizedTest
    @MethodSource("queueSasPermissionStringSupplier")
    public void queueSasPermissionString(boolean read, boolean add, boolean update, boolean process, String expected) {
        QueueSasPermission perms = new QueueSasPermission().setReadPermission(read)
            .setAddPermission(add)
            .setUpdatePermission(update)
            .setProcessPermission(process);

        assertEquals(expected, perms.toString());
    }

    private static Stream<Arguments> queueSasPermissionStringSupplier() {
        return Stream.of(Arguments.of(true, false, false, false, "r"), Arguments.of(false, true, false, false, "a"),
            Arguments.of(false, false, true, false, "u"), Arguments.of(false, false, false, true, "p"),
            Arguments.of(true, false, true, false, "ru"), Arguments.of(true, true, true, true, "raup"));
    }

    @Test
    public void queueSasPermissionParseIA() {
        assertThrows(IllegalArgumentException.class, () -> QueueSasPermission.parse("rwag"));
    }

    @Test
    public void queueServiceSasCanonicalizedResource() {
        String queueName = queueClient.getQueueName();

        assertEquals(queueName, new QueueServiceSasSignatureValues().setQueueName(queueName).getQueueName());
    }

    @Test
    public void queueSasEnqueueDequeueWithPermissions() {
        queueClient.create();
        SendMessageResult resp = queueClient.sendMessage("test");

        QueueSasPermission permissions
            = new QueueSasPermission().setReadPermission(true).setAddPermission(true).setProcessPermission(true);
        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sasPermissions = new QueueServiceSasSignatureValues().setPermissions(permissions)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueClient clientPermissions = queueBuilderHelper().endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient();
        clientPermissions.sendMessage("sastest");

        Iterator<QueueMessageItem> dequeueMsgIterPermissions = clientPermissions.receiveMessages(2).iterator();
        assertEquals("test", dequeueMsgIterPermissions.next().getMessageText());
        assertEquals("sastest", dequeueMsgIterPermissions.next().getMessageText());

        assertThrows(QueueStorageException.class, () -> clientPermissions.updateMessage(resp.getMessageId(),
            resp.getPopReceipt(), "testing", Duration.ofHours(1)));
    }

    @Test
    public void queueSasUpdateDeleteWithPermissions() {
        queueClient.create();
        SendMessageResult resp = queueClient.sendMessage("test");

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
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueClient clientPermissions = queueBuilderHelper().endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient();
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO);

        Iterator<QueueMessageItem> dequeueMsgIterPermissions = clientPermissions.receiveMessages(1).iterator();
        assertEquals("testing", dequeueMsgIterPermissions.next().getMessageText());

        assertThrows(QueueStorageException.class, clientPermissions::delete);
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    @Test
    public void queueSasEnqueueDequeueWithIdentifier() {
        queueClient.create();
        queueClient.sendMessage("test");

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
        queueClient.setAccessPolicy(Arrays.asList(identifier));

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfRunningAgainstService(30000);

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString());
        String sasIdentifier = new QueueServiceSasSignatureValues().setIdentifier(identifier.getId())
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode();

        QueueClient clientIdentifier = queueBuilderHelper().endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildClient();
        clientIdentifier.sendMessage("sastest");

        Iterator<QueueMessageItem> dequeueMsgIterIdentifier = clientIdentifier.receiveMessages(2).iterator();
        assertEquals("test", dequeueMsgIterIdentifier.next().getMessageText());
        assertEquals("sastest", dequeueMsgIterIdentifier.next().getMessageText());
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
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        QueueServiceClient sc = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
            .buildClient();
        String queueName = getRandomName(60);

        assertDoesNotThrow(() -> sc.createQueue(queueName));
        assertDoesNotThrow(() -> sc.deleteQueue(queueName));
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
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        QueueServiceClient sc = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
            .buildClient();

        assertDoesNotThrow(() -> sc.listQueues().iterator().hasNext());
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
        String sas = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .credential(credential)
            .buildClient()
            .generateAccountSas(sasValues);

        String queueName = getRandomName(60);

        assertDoesNotThrow(
            () -> getServiceClientBuilder(null, primaryQueueServiceClient.getQueueServiceUrl() + "?" + sas)
                .buildClient()
                .createQueue(queueName));

        assertDoesNotThrow(
            () -> getQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl() + "/" + queueName + "?" + sas)
                .buildClient()
                .delete());
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

        QueueServiceClient sc = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
            .buildClient();
        String queueName = getRandomName(60);

        assertDoesNotThrow(() -> sc.createQueue(queueName));
        assertDoesNotThrow(() -> sc.deleteQueue(queueName));
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setReadPermission(true);
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);
        String sas = primaryQueueServiceClient.generateAccountSas(sasValues);

        queueClient.create();

        assertDoesNotThrow(
            () -> instrument(new QueueClientBuilder().endpoint(queueClient.getQueueUrl()).sasToken(sas)).buildClient()
                .getProperties());

        assertDoesNotThrow(() -> instrument(
            new QueueClientBuilder().endpoint(queueClient.getQueueUrl()).credential(new AzureSasCredential(sas)))
                .buildClient()
                .getProperties());

        assertDoesNotThrow(
            () -> instrument(new QueueClientBuilder().endpoint(queueClient.getQueueUrl() + "?" + sas)).buildClient()
                .getProperties());

        assertDoesNotThrow(
            () -> instrument(new QueueServiceClientBuilder().endpoint(queueClient.getQueueUrl()).sasToken(sas))
                .buildClient()
                .getProperties());

        assertDoesNotThrow(() -> instrument(
            new QueueServiceClientBuilder().endpoint(queueClient.getQueueUrl()).credential(new AzureSasCredential(sas)))
                .buildClient()
                .getProperties());

        assertDoesNotThrow(
            () -> instrument(new QueueServiceClientBuilder().endpoint(queueClient.getQueueUrl() + "?" + sas))
                .buildClient()
                .getProperties());
    }
}
