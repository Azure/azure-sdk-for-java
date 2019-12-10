package com.azure.storage.queue

import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException
import com.azure.storage.queue.models.SendMessageResult
import com.azure.storage.queue.sas.QueueSasPermission
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues

import java.time.Duration
import java.time.temporal.ChronoUnit

class QueueSasClientTests extends APISpec {

    QueueClient sasClient
    def resp

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
        sasClient = primaryQueueServiceClient.getQueueClient(testResourceName.randomName(methodName, 10))
        sasClient.create()
        resp = sasClient.sendMessage("test")
    }

    QueueServiceSasSignatureValues generateValues(QueueSasPermission permission) {
        return new QueueServiceSasSignatureValues(getUTCNow().plusDays(1), permission)
            .setStartTime(getUTCNow().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setSasIpRange(new SasIpRange()
                .setIpMin("0.0.0.0")
                .setIpMax("255.255.255.255"))
    }

    def "Test QueueSAS enqueue dequeue with permissions"() {
        setup:
        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sasPermissions = sasClient.generateSas(sasValues)

        def clientPermissions = queueBuilderHelper(interceptorManager)
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

    def "Test QueueSAS update delete with permissions"() {
        setup:
        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sasPermissions = sasClient.generateSas(sasValues)

        def clientPermissions = queueBuilderHelper(interceptorManager)
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
    def "Test QueueSAS enqueue dequeue with identifier"() {
        setup:

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true)
        def expiryTime = getUTCNow().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
        def startTime = getUTCNow().minusDays(1).truncatedTo(ChronoUnit.SECONDS)

        QueueSignedIdentifier identifier = new QueueSignedIdentifier()
            .setId(testResourceName.randomUuid())
            .setAccessPolicy(new QueueAccessPolicy().setPermissions(permissions.toString())
                .setExpiresOn(expiryTime).setStartsOn(startTime))
        sasClient.setAccessPolicy(Arrays.asList(identifier))

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfLive(30000)

        when:
        def sasValues = new QueueServiceSasSignatureValues(identifier.getId())

        def sasIdentifier = sasClient.generateSas(sasValues)

        def clientBuilder = queueBuilderHelper(interceptorManager)
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

    def "Test Account QueueServiceSAS create queue delete queue"() {
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
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryQueueServiceClient.generateAccountSas(sasValues)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        sc.createQueue("queue")

        then:
        notThrown(QueueStorageException)

        when:
        sc.deleteQueue("queue")

        then:
        notThrown(QueueStorageException)
    }

    def "Test Account QueueServiceSAS list queues"() {
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setListPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryQueueServiceClient.generateAccountSas(sasValues)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(QueueStorageException)
    }
}
