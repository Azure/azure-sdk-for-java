// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.models.UserDelegationKey;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;

import static com.azure.storage.common.test.shared.StorageCommonTestUtils.getOidFromToken;
import static com.azure.storage.queue.QueueTestHelper.assertExceptionStatusCodeAndMessage;
import static com.azure.storage.queue.QueueTestHelper.assertResponseStatusCode;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueueSasClientTests extends QueueTestBase {
    private QueueClient sasClient;
    private SendMessageResult resp;

    @BeforeEach
    public void setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient();
        sasClient = primaryQueueServiceClient.getQueueClient(getRandomName(50));
        sasClient.create();
        resp = sasClient.sendMessage("test");
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

        QueueClient clientPermissions = queueBuilderHelper().endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasClient.generateSas(sasValues))
            .buildClient();
        clientPermissions.sendMessage("sastest");

        Iterator<QueueMessageItem> dequeueMsgIterPermissions = clientPermissions.receiveMessages(2).iterator();
        assertEquals("test", dequeueMsgIterPermissions.next().getMessageText());
        assertEquals("sastest", dequeueMsgIterPermissions.next().getMessageText());

        assertThrows(QueueStorageException.class, () -> clientPermissions.updateMessage(resp.getMessageId(),
            resp.getPopReceipt(), "testing", Duration.ofHours(1)));
    }

    @Test
    public void queueSasUpdateWithPerm() {
        QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true);
        QueueServiceSasSignatureValues sasValues = generateValues(permissions);

        QueueClient clientPermissions = queueBuilderHelper().endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasClient.generateSas(sasValues))
            .buildClient();

        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO);

        Iterator<QueueMessageItem> dequeueMsgIterPermissions = clientPermissions.receiveMessages(1).iterator();
        assertEquals("testing", dequeueMsgIterPermissions.next().getMessageText());

        assertThrows(QueueStorageException.class, clientPermissions::delete);
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
        sasClient.setAccessPolicy(Arrays.asList(identifier));

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfRunningAgainstService(30000);

        QueueServiceSasSignatureValues sasValues = new QueueServiceSasSignatureValues(identifier.getId());

        QueueClient clientIdentifier = queueBuilderHelper().endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasClient.generateSas(sasValues))
            .buildClient();
        clientIdentifier.sendMessage("sastest");

        Iterator<QueueMessageItem> dequeueMsgIterIdentifier = clientIdentifier.receiveMessages(2).iterator();
        assertEquals("test", dequeueMsgIterIdentifier.next().getMessageText());
        assertEquals("sastest", dequeueMsgIterIdentifier.next().getMessageText());
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

        QueueServiceClient sc = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(primaryQueueServiceClient.generateAccountSas(sasValues))
            .buildClient();

        String queueName = getRandomName(50);
        assertDoesNotThrow(() -> sc.createQueue(queueName));
        assertDoesNotThrow(() -> sc.deleteQueue(queueName));
    }

    @Test
    public void accountSasListQueues() {
        AccountSasService service = new AccountSasService().setQueueAccess(true);
        AccountSasResourceType resourceType
            = new AccountSasResourceType().setContainer(true).setService(true).setObject(true);
        AccountSasPermission permissions = new AccountSasPermission().setListPermission(true);
        AccountSasSignatureValues sasValues
            = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1), permissions, service, resourceType);

        QueueServiceClient sc = queueServiceBuilderHelper().endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(primaryQueueServiceClient.generateAccountSas(sasValues))
            .buildClient();

        assertDoesNotThrow(() -> sc.listQueues().iterator().hasNext());
    }

    /**
     * If this test fails it means that non-deprecated string to sign has new components. In that case we should
     * hardcode version used for deprecated string to sign like we did for blobs.
     */
    @Test
    public void rememberAboutStringToSignDeprecation() {
        QueueClient client
            = queueBuilderHelper().credential(ENVIRONMENT.getPrimaryAccount().getCredential()).buildClient();
        QueueServiceSasSignatureValues values
            = new QueueServiceSasSignatureValues(testResourceNamer.now(), new QueueSasPermission());
        values.setQueueName(client.getQueueName());

        String deprecatedStringToSign
            = values.generateSasQueryParameters(ENVIRONMENT.getPrimaryAccount().getCredential()).encode();

        assertEquals(deprecatedStringToSign, client.generateSas(values));
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = QueueServiceVersion.class, min = "2026-02-06")
    public void queueSasUserDelegationDelegatedObjectId() {
        liveTestScenarioWithRetry(() -> {
            QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            QueueServiceSasSignatureValues sasValues
                = new QueueServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential.
            QueueClient client = instrument(
                new QueueClientBuilder().endpoint(sasClient.getQueueUrl()).sasToken(sas).credential(tokenCredential))
                    .buildClient();

            Response<QueueProperties> response = client.getPropertiesWithResponse(null, Context.NONE);
            assertResponseStatusCode(response, 200);
        });
    }

    // RBAC replication lag
    @Test
    @LiveOnly
    @RequiredServiceVersion(clazz = QueueServiceVersion.class, min = "2026-02-06")
    public void queueSasUserDelegationDelegatedObjectIdFail() {
        liveTestScenarioWithRetry(() -> {
            QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            TokenCredential tokenCredential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

            // We need to get the object ID from the token credential used to authenticate the request
            String oid = getOidFromToken(tokenCredential);
            QueueServiceSasSignatureValues sasValues
                = new QueueServiceSasSignatureValues(expiryTime, permissions).setDelegatedUserObjectId(oid);
            String sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
            // token credential. Token credential is not provided here, so the request should fail.
            QueueClient client
                = instrument(new QueueClientBuilder().endpoint(sasClient.getQueueUrl()).sasToken(sas)).buildClient();

            QueueStorageException e
                = assertThrows(QueueStorageException.class, () -> client.getPropertiesWithResponse(null, Context.NONE));
            assertExceptionStatusCodeAndMessage(e, 403, QueueErrorCode.AUTHENTICATION_FAILED);
        });
    }

    @Test
    @RequiredServiceVersion(clazz = QueueServiceVersion.class, min = "2026-02-06")
    public void sendMessageUserDelegationSAS() {
        liveTestScenarioWithRetry(() -> {
            QueueSasPermission permissions = new QueueSasPermission().setReadPermission(true)
                .setAddPermission(true)
                .setProcessPermission(true)
                .setUpdatePermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusHours(1);

            QueueServiceSasSignatureValues sasValues = new QueueServiceSasSignatureValues(expiryTime, permissions);
            String sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo());

            QueueClient client
                = instrument(new QueueClientBuilder().endpoint(sasClient.getQueueUrl()).sasToken(sas)).buildClient();

            client.sendMessage(DATA.getDefaultBinaryData());
            Iterator<QueueMessageItem> dequeueMsgIter = client.receiveMessages(2).iterator();
            assertTrue(dequeueMsgIter.hasNext());
            dequeueMsgIter.next(); // Skip the first message, which is the one we sent in the setup
            assertArrayEquals(DATA.getDefaultBytes(), dequeueMsgIter.next().getBody().toBytes());
        });
    }

    protected UserDelegationKey getUserDelegationInfo() {
        UserDelegationKey key = getOAuthQueueServiceClient().getUserDelegationKey(testResourceNamer.now().minusDays(1),
            testResourceNamer.now().plusDays(1));
        String keyOid = testResourceNamer.recordValueFromConfig(key.getSignedObjectId());
        key.setSignedObjectId(keyOid);
        String keyTid = testResourceNamer.recordValueFromConfig(key.getSignedTenantId());
        key.setSignedTenantId(keyTid);
        return key;
    }
}
