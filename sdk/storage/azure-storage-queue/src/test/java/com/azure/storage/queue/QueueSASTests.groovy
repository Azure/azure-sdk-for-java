// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.sas.SasIpRange

import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException
import com.azure.storage.queue.models.SendMessageResult
import com.azure.storage.queue.sas.QueueSasPermission
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues
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
        perms.hasReadPermission() == read
        perms.hasAddPermission() == add
        perms.hasUpdatePermission() == update
        perms.hasProcessPermission() == process

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

        when:
        def serviceSASSignatureValues = new QueueServiceSasSignatureValues().setQueueName(queueName)

        then:
        serviceSASSignatureValues.getQueueName() == queueName
    }

    @Test
    def "Test QueueSAS enqueue dequeue with permissions"() {
        setup:
        queueClient.create()
        SendMessageResult resp = queueClient.sendMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
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

    @Test
    def "Test QueueSAS update delete with permissions"() {
        setup:
        queueClient.create()
        SendMessageResult resp = queueClient.sendMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper(interceptorManager)
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
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
    @Test
    def "Test QueueSAS enqueue dequeue with identifier"() {
        setup:
        queueClient.create()
        queueClient.sendMessage("test")

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

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfLive(30000)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasIdentifier = new QueueServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientBuilder = queueBuilderHelper(interceptorManager)
        def clientIdentifier = clientBuilder
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildClient()
        clientIdentifier.sendMessage("sastest")
        def dequeueMsgIterIdentifier = clientIdentifier.receiveMessages(2).iterator()

        then:
        notThrown(QueueStorageException)
        "test" == dequeueMsgIterIdentifier.next().getMessageText()
        "sastest" == dequeueMsgIterIdentifier.next().getMessageText()
    }

    @Test
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
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode()

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

    @Test
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
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode()

        def scBuilder = queueServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(QueueStorageException)
    }

    def "accountSAS network account sas token on endpoint"() {
        setup:
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setDeletePermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()

        def queueName = testResourceName.randomName(methodName, 60)

        when:
        def sc = getServiceClientBuilder(null, primaryQueueServiceClient.getQueueServiceUrl() + "?" + sas, null).buildClient()
        sc.createQueue(queueName)

        def qc = getQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl() + "/" + queueName + "?" + sas).buildClient()
        qc.delete()

        then:
        notThrown(Exception)
    }

}
