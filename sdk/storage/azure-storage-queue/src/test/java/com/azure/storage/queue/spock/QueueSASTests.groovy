package com.azure.storage.queue.spock

import com.azure.storage.common.AccountSASPermission
import com.azure.storage.common.AccountSASResourceType
import com.azure.storage.common.AccountSASService
import com.azure.storage.common.IPRange
import com.azure.storage.common.SASProtocol
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.queue.QueueClient
import com.azure.storage.queue.QueueSASPermission
import com.azure.storage.queue.QueueServiceSASSignatureValues
import com.azure.storage.queue.models.AccessPolicy
import com.azure.storage.queue.models.EnqueuedMessage
import com.azure.storage.queue.models.SignedIdentifier
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
        def perms = QueueSASPermission.parse(permString)

        then:
        perms.getRead() == read
        perms.getAdd() == add
        perms.getUpdate() == update
        perms.getProcess() == process

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
        def perms = new QueueSASPermission()
            .setRead(read)
            .setAdd(add)
            .setUpdate(update)
            .setProcess(process)

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
        QueueSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "queueServiceSASSignatureValues canonicalizedResource"() {
        setup:
        def queueName = queueClient.client.name
        def accountName = "account"

        when:
        def serviceSASSignatureValues = new QueueServiceSASSignatureValues().setCanonicalName(queueName, accountName)

        then:
        serviceSASSignatureValues.getCanonicalName() == "/queue/" + accountName + "/" + queueName
    }

    @Test
    def "Test QueueSAS enqueue dequeue with permissions"() {
        setup:
        queueClient.create()
        EnqueuedMessage resp = queueClient.enqueueMessage("test")

        def permissions = new QueueSASPermission()
            .setRead(true)
            .setAdd(true)
            .setProcess(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SASProtocol.HTTPS_HTTP

        when:
        def sasPermissions = queueClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange)

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl().toString())
            .queueName(queueClient.client.name)
            .credential(SASTokenCredential.fromSASTokenString(sasPermissions))
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

        def permissions = new QueueSASPermission()
            .setRead(true)
            .setAdd(true)
            .setProcess(true)
            .setUpdate(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SASProtocol.HTTPS_HTTP

        when:
        def sasPermissions = queueClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange)

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl().toString())
            .queueName(queueClient.client.name)
            .credential(SASTokenCredential.fromSASTokenString(sasPermissions))
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

        def permissions = new QueueSASPermission()
            .setRead(true)
            .setAdd(true)
            .setUpdate(true)
            .setProcess(true)
        def expiryTime = getUTCNow().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
        def startTime = getUTCNow().minusDays(1).truncatedTo(ChronoUnit.SECONDS)

        SignedIdentifier identifier = new SignedIdentifier()
            .setId(testResourceName.randomUuid())
            .setAccessPolicy(new AccessPolicy().setPermission(permissions.toString())
                .setExpiry(expiryTime).setStart(startTime))
        queueClient.setAccessPolicy(Arrays.asList(identifier))

        when:
        def sasIdentifier = queueClient.generateSAS(identifier.getId())

        def clientBuilder = queueBuilderHelper(interceptorManager)
        def clientIdentifier = clientBuilder
            .endpoint(queueClient.getQueueUrl().toString())
            .queueName(queueClient.client.name)
            .credential(SASTokenCredential.fromSASTokenString(sasIdentifier))
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
        def service = new AccountSASService()
            .setQueue(true)
        def resourceType = new AccountSASResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSASPermission()
            .setRead(true)
            .setCreate(true)
            .setDelete(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sas = primaryQueueServiceClient.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sas))
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
        def service = new AccountSASService()
            .setQueue(true)
        def resourceType = new AccountSASResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSASPermission()
            .setList(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sas = primaryQueueServiceClient.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sas))
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(StorageException)
    }

}
