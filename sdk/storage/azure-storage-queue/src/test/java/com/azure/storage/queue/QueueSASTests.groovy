package com.azure.storage.queue


import com.azure.storage.common.AccountSasPermission
import com.azure.storage.common.AccountSasResourceType
import com.azure.storage.common.AccountSasService
import com.azure.storage.common.AccountSasSignatureValues
import com.azure.storage.common.IpRange
import com.azure.storage.common.SasProtocol
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.queue.models.EnqueuedMessage
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.StorageException
import org.junit.Test
import spock.lang.Unroll

import java.time.Duration
import java.time.temporal.ChronoUnit

class QueueSASTests extends APISpec {

    QueueClient queueClient

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
        queueClient = primaryQueueServiceClient.getQueueClient(testResourceName.randomName(methodName, 60))
    }

    @Unroll
    def "QueueSASPermission parse"() {
        when:
        def perms = QueueSasPermission.parse(permString)

        then:
        perms.getReadPermission() == read
        perms.getAddPermission() == add
        perms.getUpdatePermission() == update
        perms.getProcessPermission() == process

        where:
        permString || read  | add   | update | process
        "r"        || true  | false | false  | false
        "a"        || false | true  | false  | false
        "u"        || false | false | true   | false
        "p"        || false | false | false  | true
        "raup"     || true  | true  | true   | true
        "apru"     || true  | true  | true   | true
        "rap"      || true  | true  | false  | true
        "ur"       || true  | false | true   | false
    }

    @Unroll
    def "QueueSASPermission toString"() {
        setup:
        def perms = new QueueSasPermission()
            .setReadPermission(read)
            .setAddPermission(add)
            .setUpdatePermission(update)
            .setProcessPermission(process)

        expect:
        perms.toString() == expectedString

        where:
        read  | add   | update | process || expectedString
        true  | false | false  | false   || "r"
        false | true  | false  | false   || "a"
        false | false | true   | false   || "u"
        false | false | false  | true    || "p"
        true  | false | true   | false   || "ru"
        true  | true  | true   | true    || "raup"
    }

    def "QueueSASPermission parse IA"() {
        when:
        QueueSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "queueServiceSASSignatureValues canonicalizedResource"() {
        setup:
        def queueName = queueClient.getQueueName()
        def accountName = "account"

        when:
        def serviceSASSignatureValues = new QueueServiceSasSignatureValues().setCanonicalName(queueName, accountName)

        then:
        serviceSASSignatureValues.getCanonicalName() == "/queue/" + accountName + "/" + queueName
    }

    @Test
    def "Test QueueSAS enqueue dequeue with permissions"() {
        setup:
        queueClient.create()
        EnqueuedMessage resp = queueClient.enqueueMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions.toString())
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setIpRange(ipRange)
            .setCanonicalName(queueClient.getQueueName(), credential.getAccountName())
            .generateSASQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.enqueueMessage("sastest")
        def dequeueMsgIterPermissions = clientPermissions.dequeueMessages(2).iterator()

        then:
        notThrown(StorageException)
        "test" == dequeueMsgIterPermissions.next().getMessageText()
        "sastest" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.updateMessage("testing", resp.getMessageId(), resp.getPopReceipt(), Duration.ofHours(1))

        then:
        thrown(StorageException)
    }

    @Test
    def "Test QueueSAS update delete with permissions"() {
        setup:
        queueClient.create()
        EnqueuedMessage resp = queueClient.enqueueMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions.toString())
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setIpRange(ipRange)
            .setCanonicalName(queueClient.getQueueName(), credential.getAccountName())
            .generateSASQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.updateMessage("testing", resp.getMessageId(), resp.getPopReceipt(), Duration.ZERO)
        def dequeueMsgIterPermissions = clientPermissions.dequeueMessages(1).iterator()

        then:
        notThrown(StorageException)
        "testing" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.delete()

        then:
        thrown(StorageException)
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    @Test
    def "Test QueueSAS enqueue dequeue with identifier"() {
        setup:
        queueClient.create()
        queueClient.enqueueMessage("test")

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
        queueClient.setAccessPolicy(Arrays.asList(identifier))

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sasIdentifier = new QueueServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setCanonicalName(queueClient.getQueueName(), credential.getAccountName())
            .generateSASQueryParameters(credential)
            .encode()

        def clientBuilder = queueBuilderHelper(interceptorManager)
        def clientIdentifier = clientBuilder
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildClient()
        clientIdentifier.enqueueMessage("sastest")
        def dequeueMsgIterIdentifier = clientIdentifier.dequeueMessages(2).iterator()

        then:
        notThrown(StorageException)
        "test" == dequeueMsgIterIdentifier.next().getMessageText()
        "sastest" == dequeueMsgIterIdentifier.next().getMessageText()
    }

    @Test
    def "Test Account QueueServiceSAS create queue delete queue"() {
        def service = new AccountSasService()
            .setQueue(true)
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
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sas = AccountSasSignatureValues.generateAccountSas(credential, service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        sc.createQueue("queue")

        then:
        notThrown(StorageException)

        when:
        sc.deleteQueue("queue")

        then:
        notThrown(StorageException)
    }

    @Test
    def "Test Account QueueServiceSAS list queues"() {
        def service = new AccountSasService()
            .setQueue(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setListPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sas = AccountSasSignatureValues.generateAccountSas(credential, service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(StorageException)
    }

}
