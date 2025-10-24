// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static com.azure.storage.common.test.shared.StorageCommonTestUtils.getOidFromToken;
import static com.azure.storage.queue.QueueTestHelper.assertExceptionStatusCodeAndMessage;
import static com.azure.storage.queue.QueueTestHelper.assertResponseStatusCode;
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

            Flux<Response<QueueProperties>> response = getUserDelegationInfo().flatMapMany(key -> {
                String sas = asyncSasClient.generateUserDelegationSas(sasValues, key);

                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential.
                QueueAsyncClient client = instrument(new QueueClientBuilder().endpoint(asyncSasClient.getQueueUrl())
                    .sasToken(sas)
                    .credential(tokenCredential)).buildAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response).assertNext(r -> assertResponseStatusCode(r, 200)).verifyComplete();
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

            Flux<Response<QueueProperties>> response = getUserDelegationInfo().flatMapMany(key -> {
                String sas = asyncSasClient.generateUserDelegationSas(sasValues, key);

                // When a delegated user object ID is set, the client must be authenticated with both the SAS and the
                // token credential.
                QueueAsyncClient client
                    = instrument(new QueueClientBuilder().endpoint(asyncSasClient.getQueueUrl()).sasToken(sas))
                        .buildAsyncClient();

                return client.getPropertiesWithResponse();
            });

            StepVerifier.create(response)
                .verifyErrorSatisfies(
                    e -> assertExceptionStatusCodeAndMessage(e, 403, QueueErrorCode.AUTHENTICATION_FAILED));
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

            Mono<List<QueueMessageItem>> response = getUserDelegationInfo().flatMap(key -> {
                String sas = asyncSasClient.generateUserDelegationSas(sasValues, key);

                QueueAsyncClient client
                    = instrument(new QueueClientBuilder().endpoint(asyncSasClient.getQueueUrl()).sasToken(sas))
                        .buildAsyncClient();

                return client.sendMessage(DATA.getDefaultBinaryData()).then(client.receiveMessages(2).collectList());
            });

            StepVerifier.create(response).assertNext(messageItemList -> {
                // The first message is the one sent in setup.
                assertEquals(2, messageItemList.size());
                assertEquals("test", messageItemList.get(0).getBody().toString());
                assertEquals(DATA.getDefaultText(), messageItemList.get(1).getBody().toString());
            });
        });
    }

    private Mono<UserDelegationKey> getUserDelegationInfo() {
        return getOAuthQueueServiceAsyncClient()
            .getUserDelegationKey(testResourceNamer.now().minusDays(1), testResourceNamer.now().plusDays(1))
            .flatMap(r -> {
                String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                r.setSignedObjectId(keyOid);
                String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                r.setSignedTenantId(keyTid);
                return Mono.just(r);
            });
    }
}
