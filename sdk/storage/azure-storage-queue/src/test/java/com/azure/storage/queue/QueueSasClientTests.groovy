package com.azure.storage.queue

import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException
import com.azure.storage.queue.sas.QueueSasPermission
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues

import java.time.Duration
import java.time.temporal.ChronoUnit

class QueueSasClientTests extends APISpec {

    QueueClient sasClient
    def resp

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient()
        sasClient = primaryQueueServiceClient.getQueueClient(namer.getRandomName(50))
        sasClient.create()
        resp = sasClient.sendMessage("test")
    }

    QueueServiceSasSignatureValues generateValues(QueueSasPermission permission) {
        return new QueueServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission)
            .setStartTime(namer.getUtcNow().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
    }

    def "QueueSAS enqueue with perm"() {
        setup:
        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sasPermissions = sasClient.generateSas(sasValues)

        def clientPermissions = queueBuilderHelper()
            .endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.sendMessage("sastest")
        def dequeueMsgIterPermissions = clientPermissions.receiveMessages(2).iterator()

        then:
        notThrown(QueueStorageException)
        "test" == dequeueMsgIterPermissions.next().getMessageText()
        "sastest" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ofHours(1))

        then:
        thrown(QueueStorageException)
    }

    def "QueueSAS update with perm"() {
        setup:
        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sasPermissions = sasClient.generateSas(sasValues)

        def clientPermissions = queueBuilderHelper()
            .endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO)
        def dequeueMsgIterPermissions = clientPermissions.receiveMessages(1).iterator()

        then:
        notThrown(QueueStorageException)
        "testing" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.delete()

        then:
        thrown(QueueStorageException)
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    def "QueueSAS enqueue with id"() {
        setup:

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
        def startTime = namer.getUtcNow().minusDays(1).truncatedTo(ChronoUnit.SECONDS)

        QueueSignedIdentifier identifier = new QueueSignedIdentifier()
            .setId(namer.getRandomUuid())
            .setAccessPolicy(new QueueAccessPolicy().setPermissions(permissions.toString())
                .setExpiresOn(expiryTime).setStartsOn(startTime))
        sasClient.setAccessPolicy(Arrays.asList(identifier))

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfLive(30000)

        when:
        def sasValues = new QueueServiceSasSignatureValues(identifier.getId())

        def sasIdentifier = sasClient.generateSas(sasValues)

        def clientBuilder = queueBuilderHelper()
        def clientIdentifier = clientBuilder
            .endpoint(sasClient.getQueueUrl())
            .queueName(sasClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildClient()
        clientIdentifier.sendMessage("sastest")
        def dequeueMsgIterIdentifier = clientIdentifier.receiveMessages(2).iterator()

        then:
        notThrown(QueueStorageException)
        "test" == dequeueMsgIterIdentifier.next().getMessageText()
        "sastest" == dequeueMsgIterIdentifier.next().getMessageText()
    }

    def "AccountSAS create queue"() {
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryQueueServiceClient.generateAccountSas(sasValues)

        def scBuilder = queueServiceBuilderHelper()
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        def queueName = namer.getRandomName(50)
        sc.createQueue(queueName)

        then:
        notThrown(QueueStorageException)

        when:
        sc.deleteQueue(queueName)

        then:
        notThrown(QueueStorageException)
    }

    def "AccountSAS list queues"() {
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setListPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryQueueServiceClient.generateAccountSas(sasValues)

        def scBuilder = queueServiceBuilderHelper()
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(QueueStorageException)
    }

    /**
     * If this test fails it means that non-deprecated string to sign has new components.
     * In that case we should hardcode version used for deprecated string to sign like we did for blobs.
     */
    def "Remember about string to sign deprecation"() {
        setup:
        def client = queueBuilderHelper().credential(env.primaryAccount.credential).buildClient()
        def values = new QueueServiceSasSignatureValues(namer.getUtcNow(), new QueueSasPermission())
        values.setQueueName(client.getQueueName())

        when:
        def deprecatedStringToSign = values.generateSasQueryParameters(env.primaryAccount.credential).encode()
        def stringToSign = client.generateSas(values)

        then:
        deprecatedStringToSign == stringToSign
    }
}
